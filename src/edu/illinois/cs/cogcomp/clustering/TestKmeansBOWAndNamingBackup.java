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

public class TestKmeansBOWAndNamingBackup {
	


	public static void main (String[] args) {
		int topK = 1;
		int iter = 10;
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
				DatalessResourcesConfig.LabelfilePath = "/shared/bronte/hpeng7/multilingual-embedding/label.en";
				List<String> labelsInLang = IOManager.readLines(DatalessResourcesConfig.LabelfilePath);
				HashMap<String, String> labelMap = new HashMap<String, String>();
				for (int j = 0; j < labelsInLang.size(); ++j) {
					labelMap.put(labelsInLang.get(j).trim(), NGData.NG20Labels[j]);
				}
				DatalessResourcesConfig.level = 1;
				int conceptNum = 500;
				HashMap<String, Double> conceptWeights = new HashMap<String, Double>();
				ConceptTreeTopDownML tree = new ConceptTreeTopDownML(DatalessResourcesConfig.CONST_DATA_CUSTOMIZED, "null", conceptWeights, true);
				tree.setDebug(false);
				tree.esa = new MultiLingalESA(lgName, indexFolderName);
				System.out.println("process tree...");
				tree.treeLabelData.readTreeHierarchy("");
				ConceptTreeNode rootNode = tree.initializeTree("root", 0);
				tree.setRootNode(rootNode);
				tree.aggregateChildrenDescription(rootNode);
				tree.setConceptNum(conceptNum);
				tree.conceptualizeTreeLabels(rootNode, ClassifierConstant.isBreakConcepts);
				
				
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
		        
		        int cNum = labelSet.size();
			      
		        List<String> labelSetList = new ArrayList<String> (labelSet);
		        int[] truth = new int[labelList.size()];
		        for (int j = 0; j < labelList.size(); ++j) {
		        	truth[j] = labelSetList.indexOf(labelList.get(j));
		        }
		        
				List<Double> accList = new ArrayList<Double>();
				for (int j = 0; j < iter; ++j) {
					int[] labels = testKmeans (labelVectors, cNum, j);
					
					int correct = 0;
					
					HashSet<Integer> intLabelSet = new HashSet<Integer>();
					for (int k = 0; k < labels.length; ++k) {
						intLabelSet.add(labels[k]);
					}
					
					for (int intLabel : intLabelSet) {
						String content = "";
						for (int k = 0; k < labels.length; ++k) {
							if (labels[k] == intLabel) {
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
						HashMap<Integer, List<LabelKeyValuePair>> labelResultsInDepth = tree.labelDocumentML(document);
						
						for (int k = 0; k < labels.length; ++k) {
							if (labels[k] == intLabel) {
								String label = labelList.get(k);

								int depth = 1;
								boolean isHit = false;
								List<LabelKeyValuePair> kvp = labelResultsInDepth.get(depth);
								if (kvp != null) {
									for (int l = 0; l < Math.min(topK, kvp.size()); ++l) {
										String labelPred = labelMap.get(kvp.get(l).getLabel().trim());
										if (label.equals(labelPred))
											isHit = true;
									}
								}
								
								if (isHit == true) 
									correct++;
							}
						}
						
					}
					double acc = correct / (labelList.size() + Double.MIN_VALUE);
					System.out.println ("ACC=" + acc);

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
	
	public static int[] testKmeans (List<DoubleMatrix1D> labelVectors, int cNum, int seed) {
		GeneralKmeans clusterer = null;
		clusterer = new GeneralKmeans(labelVectors, cNum, "maxmin", seed);
        clusterer.estimate(); 
        int[] labels = clusterer.getLabels();
		return labels;
	}
}
