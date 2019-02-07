package edu.illinois.cs.cogcomp.clustering;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import cern.colt.matrix.DoubleMatrix1D;
import edu.illinois.cs.cogcomp.MultiLingualResourcesConfig;
import edu.illinois.cs.cogcomp.classification.densification.representation.HungarianAlgorithm;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ConceptTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml.ConceptTreeTopDownML;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.jlis.CorpusDataProcessing;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.ConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;
import edu.illinois.cs.cogcomp.classification.hierarchy.evaluation.StatUtils;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;
import edu.illinois.cs.cogcomp.classification.main.DatalessResourcesConfig;
import edu.illinois.cs.cogcomp.classification.newsgroups.data.ExportData;
import edu.illinois.cs.cogcomp.classification.newsgroups.data.NGData;
import edu.illinois.cs.cogcomp.descartes.util.IOManager;
import edu.illinois.cs.cogcomp.embedding.esa.multiesa.MultiLingalESA;

public class TestKmeansBOWAndNaming {
	


	public static void main (String[] args) {
		int iter = 10;
		MultiLingualResourcesConfig.initialization();
		String inputFolder = MultiLingualResourcesConfig.multiLingual20NGFolder;
		File folder = new File(inputFolder);
		File[] fileList = folder.listFiles();
		HashMap<String, Double> meanMap = new HashMap<String, Double>();
		for (int i = 0; i < fileList.length; ++i) {
			if (fileList[i].isDirectory()) {
				String inputLgFolderStr = fileList[i].getAbsolutePath();
				String lgName = fileList[i].getName();
				
				double avgAcc = testOneLg (inputLgFolderStr, lgName, iter, 500);
				meanMap.put(lgName, avgAcc);
			}
		}
		
		for (String key : meanMap.keySet()) {
			System.out.println(key + "\t" + meanMap.get(key));
		}
	}
	
	public static double testOneLg (String inputLgFolderStr, String lgName, int iter, int conceptNum) {
		
		File inputLgFolder = new File(inputLgFolderStr);
		
		//initialize ESA
		String indexFolderName = "";
		if (lgName.equals("en")) {
			indexFolderName = MultiLingualResourcesConfig.englishESA530IndexFolder;
		} else {
			indexFolderName = MultiLingualResourcesConfig.multiLingualESAFolder + lgName + MultiLingualResourcesConfig.multiLingualESASuffix;
		}
		MultiLingalESA esa = new MultiLingalESA(lgName, indexFolderName);
		
		//initialize labels
		
		DatalessResourcesConfig.LabelfilePath = "/shared/bronte/hpeng7/multilingual-embedding/label.en";
		DatalessResourcesConfig.level = 1;
		HashMap<String, Double> conceptWeights = new HashMap<String, Double>();

		HashMap<String, SparseVector> labelVectorMap = new HashMap<String, SparseVector>();
		List<String> labelsInLang = IOManager.readLines(DatalessResourcesConfig.LabelfilePath);
		HashMap<String, String> labelMap = new HashMap<String, String>();
		for (int j = 0; j < labelsInLang.size(); ++j) {
			labelMap.put(labelsInLang.get(j).trim(), NGData.NG20Labels[j]);
			
			List<ConceptData> concepts = null;
			List<String> conceptsList = new ArrayList<String>();
			List<Double> scores = new ArrayList<Double>();

			try {
				concepts = esa.retrieveConcepts(labelsInLang.get(j).trim(), conceptNum);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (concepts != null) {
				for (int l = 0; l < concepts.size(); l++) {
					conceptsList.add(concepts.get(l).concept + "");
					scores.add(concepts.get(l).score);
				}
			}
			
			SparseVector labelVector = new SparseVector(conceptsList, scores, ClassifierConstant.isBreakConcepts, conceptWeights);
			labelVectorMap.put(NGData.NG20Labels[j], labelVector);
		}
		
		// initialize data for kmeans
		HashMap<String, String> contentMap = ExportData.readAll(inputLgFolderStr);
		CorpusDataProcessing corpus = new CorpusDataProcessing();
		HashMap<String, String> libSVMFormatMap = corpus.initializeTrainingDocumentFeatures(contentMap, true, true);

		List<String> labelList = new ArrayList<String>();
		HashSet<String> labelSet = new HashSet<String>();
		File[] lgList = inputLgFolder.listFiles();
		List<HashMap<Integer, Double>> docTFIDFList = new ArrayList<HashMap<Integer, Double>>();
		List<String> docContent = new ArrayList<String>(); 
		for (int j = 0; j < lgList.length; ++j) {
			String[] fileNameTokens = lgList[j].getName().split("_");
			String label = fileNameTokens[0];
			if (libSVMFormatMap.containsKey(lgList[j].getName())) {
				String libSVMFormatStr = libSVMFormatMap.get(lgList[j].getName());
				String[] tokens = libSVMFormatStr.split(" ");
				HashMap<Integer, Double> docTFIDF = new HashMap<Integer, Double>();
				for (String token : tokens) {
					String[] toks = token.split(":");
					int t1 = Integer.parseInt(toks[0]);
					double t2 = Double.parseDouble(toks[1]);
					docTFIDF.put(t1, t2);
				}
				docTFIDFList.add(docTFIDF);
				labelList.add(label);
				labelSet.add(label);
				docContent.add(contentMap.get(lgList[j].getName()));
			}
		}
        
        List<DoubleMatrix1D> labelVectors = new  ArrayList<DoubleMatrix1D>(); 
        for (int j = 0; j < docTFIDFList.size(); ++j) {
        	DoubleMatrix1D vector = new ColtSparseVector(corpus.getDictSize() + 1);
        	HashMap<Integer, Double> docTokenCount = docTFIDFList.get(j);
        	for (int key : docTokenCount.keySet()) {
        		vector.set(key, docTokenCount.get(key));
        	}
        	labelVectors.add(vector);
        }
        
        // initialize integer labels for kmeans
        int cNum = labelSet.size();
	      
        List<String> labelSetList = new ArrayList<String> (labelSet);
        int[] truth = new int[labelList.size()];
        for (int j = 0; j < labelList.size(); ++j) {
        	truth[j] = labelSetList.indexOf(labelList.get(j));
        }
        
        // run kmeans for multiple times and average
		List<Double> accList = new ArrayList<Double>();
		for (int j = 0; j < iter; ++j) {
			GeneralKmeans clusterer = null;
			clusterer = new GeneralKmeans(labelVectors, cNum, "maxmin", j);
	        clusterer.estimate(); 
	        int[] kmeanslabels = clusterer.getLabels();
			
			int correct = 0;
			
			HashSet<Integer> intLabelSet = new HashSet<Integer>();
			for (int k = 0; k < kmeanslabels.length; ++k) {
				intLabelSet.add(kmeanslabels[k]);
			}
			
			double[][] labelPredConfMat = new double[cNum][cNum];
			for (int intLabel : intLabelSet) {
				String content = "";
				for (int k = 0; k < kmeanslabels.length; ++k) {
					if (kmeanslabels[k] == intLabel) {
						content += docContent.get(k);
					}
				}
				
				List<ConceptData> concepts = null;
				List<String> conceptsList = new ArrayList<String>();
				List<Double> scores = new ArrayList<Double>();

				try {
					concepts = esa.retrieveConcepts(content, conceptNum);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (concepts != null) {
					for (int l = 0; l < concepts.size(); l++) {
						conceptsList.add(concepts.get(l).concept + "");
						scores.add(concepts.get(l).score);
					}
				}
				
				SparseVector document = new SparseVector(conceptsList, scores, ClassifierConstant.isBreakConcepts, conceptWeights);
				
				for (String labelString : labelVectorMap.keySet()) {
					SparseVector labelVector = labelVectorMap.get(labelString);
					double sim = labelVector.cosine(document, conceptWeights);
					labelPredConfMat[intLabel][labelSetList.indexOf(labelString)] = 1-sim;
				}
		        		
			}
			HungarianAlgorithm hungarian2 =new HungarianAlgorithm(labelPredConfMat);
			int[] results = hungarian2.execute();
//			double total = 0;
//			for (int i = 0; i < cNum; ++i) {
//				total += labelPredConfMat[i][results[i]];
//	        }		
			for (int k = 0; k < kmeanslabels.length; ++k) {
				String predLabel = labelSetList.get(results[kmeanslabels[k]]);
				if (predLabel.equals(labelList.get(k))) {
					correct++;
				}
			}
			
			double acc = correct / (labelList.size() + Double.MIN_VALUE);
			System.out.println ("ACC=" + acc);

			accList.add(acc);
		}
		double avgAcc = StatUtils.listAverage(accList);
		return avgAcc;
	}
}
