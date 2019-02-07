package edu.illinois.cs.cogcomp.clustering;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import cern.colt.matrix.DoubleMatrix1D;
import edu.illinois.cs.cogcomp.MultiLingualResourcesConfig;
import edu.illinois.cs.cogcomp.classification.densification.representation.HungarianAlgorithm;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.jlis.CorpusDataProcessing;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.ConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.evaluation.StatUtils;
import edu.illinois.cs.cogcomp.classification.newsgroups.data.ExportData;
import edu.illinois.cs.cogcomp.embedding.esa.multiesa.MultiLingalESA;

public class TestKmeansESA {
	

	public static void main (String[] args) {
		
		int iter = 10;
		int conceptNum = 500;
		MultiLingualResourcesConfig.initialization();
		String inputFolder = MultiLingualResourcesConfig.multiLingual20NGFolder;
		File folder = new File(inputFolder);
		File[] fileList = folder.listFiles();
		HashMap<String, Double> meanMap = new HashMap<String, Double>();
		HashMap<String, Double> stdMap = new HashMap<String, Double>();
		for (int i = 0; i < fileList.length; ++i) {
			if (fileList[i].isDirectory()) {
				String inputLgFolderStr = fileList[i].getAbsolutePath();
				String lgName = fileList[i].getName();
				File inputLgFolder = new File(inputLgFolderStr);

				String indexFolderName = "";
				if (lgName.equals("en")) {
					indexFolderName = MultiLingualResourcesConfig.englishESA530IndexFolder;
				} else {
					indexFolderName = MultiLingualResourcesConfig.multiLingualESAFolder + lgName + MultiLingualResourcesConfig.multiLingualESASuffix;
				}
				MultiLingalESA esa = new MultiLingalESA(lgName, indexFolderName);

				HashMap<String, String> contentMap = ExportData.readAll(inputLgFolderStr);
				HashMap<String, Integer> conceptMap = new HashMap<String, Integer>();
				int globalIndex = 0;

				List<String> labelList = new ArrayList<String>();
				HashSet<String> labelSet = new HashSet<String>();
				File[] lgList = inputLgFolder.listFiles();
				List<HashMap<Integer, Double>> docTFIDFList = new ArrayList<HashMap<Integer, Double>>();
				for (int j = 0; j < lgList.length; ++j) {
					String[] fileNameTokens = lgList[j].getName().split("_");
					String label = fileNameTokens[0];
					if (contentMap.containsKey(lgList[j].getName())) {
						List<ConceptData> concepts = null;
						try {
							System.out.println("[ESA Doc] " + lgList[j].getName());
							concepts = esa.retrieveConcepts(contentMap.get(lgList[j].getName()), conceptNum);
						} catch (Exception e) {
							e.printStackTrace();
						}
						HashMap<Integer, Double> docTFIDF = new HashMap<Integer, Double>();
						if (concepts != null) {
							for (int k = 0; k < concepts.size(); k++) {
								int index = 0;
								if (conceptMap.containsKey(concepts.get(k).concept)) {
									index = conceptMap.get(concepts.get(k).concept);
								} else {
									index = globalIndex;
									conceptMap.put(concepts.get(k).concept, globalIndex);
									globalIndex++;
								}
								docTFIDF.put(index, concepts.get(k).score);
							}
						}
						docTFIDFList.add(docTFIDF);
						labelList.add(label);
						labelSet.add(label);
					}
				}
		        
		        List<DoubleMatrix1D> labelVectors = new  ArrayList<DoubleMatrix1D>(); 
		        for (int j = 0; j < docTFIDFList.size(); ++j) {
		        	DoubleMatrix1D vector = new ColtSparseVector(globalIndex + 1);
		        	HashMap<Integer, Double> docTokenCount = docTFIDFList.get(j);
		        	for (int key : docTokenCount.keySet()) {
		        		vector.set(key, docTokenCount.get(key));
		        	}
		        	labelVectors.add(vector);
		        }
		        
		        int cNum = labelSet.size();
			      
		        List<String> labelSetList = new ArrayList<String> (labelSet);
		        int[] truth = new int[labelList.size()];
		        for (int j = 0; j < labelList.size(); ++j) {
		        	truth[j] = labelSetList.indexOf(labelList.get(j));
		        }
		        
				List<Double> accList = new ArrayList<Double>();
				for (int j = 0; j < iter; ++j) {
					double acc = testKmeans (labelVectors, truth, cNum, j);
					accList.add(acc);
				}
				double avgAcc = StatUtils.listAverage(accList);
				double stdAcc = StatUtils.std(accList, avgAcc);
				meanMap.put(lgName, avgAcc);
				stdMap.put(lgName, stdAcc);
			}
		}
		
		for (String key : meanMap.keySet()) {
			System.out.println(key + "\t" + meanMap.get(key) + "\t" + stdMap.get(key));
		}
	}
	
	public static double testKmeans (List<DoubleMatrix1D> labelVectors, int[] truth, int cNum, int seed) {
        
		GeneralKmeans clusterer = null;
		clusterer = new GeneralKmeans(labelVectors, cNum, "maxmin", seed);
        clusterer.estimate(); 
        int[] labels = clusterer.getLabels();
        
        double[][] costMat = new double[cNum][cNum];
        for (int i = 0; i < truth.length; ++i) {
        	costMat[labels[i]][truth[i]]++;
        }
        double[][] costMatNew = new double[cNum][cNum];
        double maxValue = 0;
        for (int i = 0; i < cNum; ++i) {
        	for (int j = 0; j < cNum; ++j) {
        		if (maxValue < costMat[i][j]) {
        			maxValue = costMat[i][j];
        		}
        	}
        }
        for (int i = 0; i < cNum; ++i) {
//        	double maxValue = 0;
//        	for (int j = 0; j < cNum; ++j) {
//        		if (maxValue < costMat[i][j]) {
//        			maxValue = costMat[i][j];
//        		}
//        	}
        	for (int j = 0; j < cNum; ++j) {
        		costMatNew[i][j] = maxValue - costMat[i][j];
        	}
        }
        HungarianAlgorithm hungarian2 =new HungarianAlgorithm(costMatNew);
		int[] results = hungarian2.execute();
		double total = 0;
		for (int i = 0; i < cNum; ++i) {
			total += costMat[i][results[i]];
        }
		double acc = total / labels.length;
		double purity = Evaluators.Purity(truth, labels);
		double nmi = Evaluators.NormalizedMutualInfo(truth, labels);
		System.out.println ("ACC=" + acc + " Purity=" + purity + " NMI=" + nmi );
		return purity;
	}
}
