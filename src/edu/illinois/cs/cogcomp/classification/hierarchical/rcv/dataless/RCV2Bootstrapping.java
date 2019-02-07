package edu.illinois.cs.cogcomp.classification.hierarchical.rcv.dataless;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import edu.illinois.cs.cogcomp.classification.hierarchical.rcv.data.LanguageMapping;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.lbj.test.AbstractClassifierLBJTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.lbj.test.ml.ClassifierLBJTreeBottomUpML;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.lbj.test.ml.ClassifierLBJTreeTopDownML;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.liblinear.AbstractClassifierLibLinearTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.liblinear.ml.ClassifierLibLinearTreeBottomUpML;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.liblinear.ml.ClassifierLibLinearTreeTopDownML;
import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.AbstractLabelTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.InterfaceMultiLabelConceptClassificationTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.InterfaceMultiLabelContentClassificationTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.AbstractConceptTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ConceptTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml.ConceptTreeBottomUpML;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml.ConceptTreeTopDownML;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.rcv.RCVCorpusConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.rcv.RCVTopicDocMaps;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.rcv.RCVTreeLabelData;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.HashSort;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.StopWords;
import edu.illinois.cs.cogcomp.classification.hierarchy.evaluation.EvalResults;
import edu.illinois.cs.cogcomp.classification.hierarchy.evaluation.Evaluation;
import edu.illinois.cs.cogcomp.classification.hierarchy.evaluation.StatUtils;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;

public class RCV2Bootstrapping {
	public static HashMap<String, Double> conceptWeights = new HashMap<String, Double>();

	public static void main (String[] args) {
		String lgName = LanguageMapping.lgNames[0];
		System.out.println("-------------------------------" + lgName + "-------------------------------");
		testMix  (lgName, "topdown", 1);
	}
	
	static double bootstrappingThreshold = 0;
	static double conceptSimilaritygThreshold = 0.0; 

	
	public static void testMix (String lgName, String direction, int iter) {
		
		if (direction.equals("topdown")) {
			bootstrappingThreshold = 0;
		} else {
			bootstrappingThreshold = 0.0;
			conceptSimilaritygThreshold = 0.00;
		}
		
//		 = "topdown"; //"bottomup";
		double trainingRate = 1;
		try {
			List<Double> precisionList = new ArrayList<Double>();
			List<Double> recallList = new ArrayList<Double>();
			List<Double> mf1List = new ArrayList<Double>();
			List<Double> Mf1List = new ArrayList<Double>();
			for (int i = 0; i < iter; ++i) {
				double penalty = 10000;// Math.pow(10, -3+i);//1000;//10000;//
				
				int addNum = 100;
				int boostrappingMaxIter = 3;
				int topK = 1;
				EvalResults result = bootstrapping20NG_Mix(lgName, addNum, boostrappingMaxIter, topK, direction, penalty, i, trainingRate, i);
				
				precisionList.add(result.precision);
				recallList.add(result.recall);
				mf1List.add(result.mf1);
				Mf1List.add(result.Mf1);
				

			}

			System.out.println();
			
			System.out.println("[average mf1]:" + StatUtils.listAverage(mf1List) 
					+ "," + StatUtils.std(mf1List, StatUtils.listAverage(mf1List)) + "\n\r");
			System.out.println("[average Mf1]:" + StatUtils.listAverage(Mf1List) 
					+ "," + StatUtils.std(Mf1List, StatUtils.listAverage(Mf1List)) + "\n\r");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static List<Double> precisionListD1 = new ArrayList<Double>();
	static List<Double> recallListD1 = new ArrayList<Double>();
	static List<Double> mf1ListD1 = new ArrayList<Double>();
	static List<Double> Mf1ListD1 = new ArrayList<Double>();


	public static EvalResults bootstrapping20NG_Mix(String lgName,
			int selectTopNumDocs, 
			int boostrappingMaxIter, int topK, String direction, double penalty, int currentIter,
			double trainingRate,
			int seed) throws IOException {
		
		Random random = new Random(seed);
		
		double penaltyPara = penalty;

		String stopWordsFile = "";
		String docIDContentConceptFile = "";
		String docIDTopicMapFile = "";
		String treeConceptFile = "";
		String outputClassificationFile = "";
		String outputLabelComparisonFile = "";
		String method = "simple";
		String data = "rcv";
		
		stopWordsFile = "data/rcvTest/english.stop";
		if (lgName.equals("english")) {
			stopWordsFile = "data/rcvTest/english.stop";
			docIDContentConceptFile = "/shared/shelley/yqsong/data/rcv1v2/org_output/rcv_train.simple.esa.concepts.500";
//			docIDContentConceptFile = "/shared/shelley/yqsong/data/rcv1v2/output_train/rcv_train.simple.esa.concepts.500";
			docIDTopicMapFile = "data/rcvTest/rcv1-v2.topics.qrels";
			treeConceptFile = "/shared/shelley/yqsong/data/rcv1v2/output_tree/tree.rcv1.useDesc.simple.esa.concepts.500";
			outputClassificationFile = "/shared/shelley/yqsong/data/rcv1v2/output/result.concept.rcv.classification";
			outputLabelComparisonFile = "/shared/shelley/yqsong/data/rcv1v2/output/result.concept.rcv.labelComparison";
		} else {
			docIDContentConceptFile = "/shared/shelley/yqsong/data/rcv2/output/preprocessed/" + lgName + ".rcv2.esa.concepts.500";
			treeConceptFile = "/shared/shelley/yqsong/data/rcv2/output/preprocessed/" + lgName + ".tree.rcv2.esa.concepts.500";
			docIDTopicMapFile = "/shared/shelley/yqsong/data/rcv2/output/preprocessed/" + lgName + ".rcv2.labels";
			outputClassificationFile = "/shared/shelley/yqsong/data/rcv2/output/results." + lgName + ".supervised.classification";
			outputLabelComparisonFile = "/shared/shelley/yqsong/data/rcv2/output/results." + lgName + ".supervised.labelComparison";
		}
//		docIDContentConceptFile = "/shared/shelley/yqsong/data/rcv2/output/preprocessed/" + lgName + ".rcv2.esa.concepts.500";
//		treeConceptFile = "/shared/shelley/yqsong/data/rcv2/output/preprocessed/" + lgName + ".tree.rcv2.esa.concepts.500";
//		docIDTopicMapFile = "/shared/shelley/yqsong/data/rcv2/output/preprocessed/" + lgName + ".rcv2.labels";
//		outputClassificationFile = "/shared/shelley/yqsong/data/rcv2/output/results." + lgName + ".supervised.classification";
//		outputLabelComparisonFile = "/shared/shelley/yqsong/data/rcv2/output/results." + lgName + ".supervised.labelComparison";

		
		StopWords.rcvStopWords = StopWords.readStopWords (stopWordsFile);
		
		String fileTopicHierarchyPath = "data/rcvTest/rcv1.topics.hier.orig";
		String fileTopicDescriptionPath = "data/rcvTest/topics.rbb";

		
		// initialize data
		RCVCorpusConceptData corpusContentProc = new RCVCorpusConceptData();
		corpusContentProc.readCorpusContentAndConcepts(docIDContentConceptFile, ClassifierConstant.isBreakConcepts, random, trainingRate, conceptWeights);

		
		// initialize concept tree
		AbstractConceptTree tree = null;
		if (direction.equals("bottomup")) {
			tree = new ConceptTreeBottomUpML(data, method, conceptWeights, false);
		} else {
			tree = new ConceptTreeTopDownML(data, method, conceptWeights, false);
		}
		System.out.println("process tree...");
		tree.readLabelTreeFromDump(treeConceptFile, ClassifierConstant.isBreakConcepts);
		ConceptTreeNode rootNode = tree.initializeTreeWithConceptVector("root", 0, ClassifierConstant.isBreakConcepts);
		tree.setRootNode(rootNode);
		System.out.println("process tree finished");
		
		
		// classify testing data--> test data is the whole set
		
		// read test topic doc maps // this is only for test accuracy, not for training
		RCVTopicDocMaps testTopicDocMapData = new RCVTopicDocMaps();
		testTopicDocMapData.readFilteredTopicDocMap (docIDTopicMapFile, corpusContentProc.getCorpusContentMap().keySet());
		HashMap<String, HashSet<String>> topicDocMapTest = testTopicDocMapData.getTopicDocMap();
		HashMap<String, HashSet<String>> docTopicMapTest = testTopicDocMapData.getDocTopicMap();
		
		HashMap<String, EvalResults> resultMap = Evaluation.testMultiLabelConceptTreeResults(
				(InterfaceMultiLabelConceptClassificationTree) tree, 
				corpusContentProc.getCorpusConceptVectorMap(), 
				topicDocMapTest, 
				docTopicMapTest, 
				outputClassificationFile, 
				outputLabelComparisonFile, 
				topK); 
		
		int evalDepth = 1;

		precisionListD1.add(resultMap.get("depth" + evalDepth).precision);
		recallListD1.add(resultMap.get("depth" + evalDepth).recall);
		mf1ListD1.add(resultMap.get("depth" + evalDepth).mf1);
		Mf1ListD1.add(resultMap.get("depth" + evalDepth).Mf1);
		

		// classify training data using concept tree
		int count = 0;
		HashMap<String, HashMap<Integer, List<LabelKeyValuePair>>> conceptTreeClassificationResults = 
				new HashMap<String,HashMap<Integer, List<LabelKeyValuePair>>>();
		HashMap<String, HashMap<String, Double>> topicDocScoreMapConceptTree = new HashMap<String, HashMap<String, Double>>();
		for (String docID : corpusContentProc.getCorpusConceptVectorMap().keySet()) {
			SparseVector documentVector = corpusContentProc.getCorpusConceptVectorMap().get(docID);

			HashMap<Integer, List <LabelKeyValuePair>> treeLabelResult = 
					((InterfaceMultiLabelConceptClassificationTree) tree).labelDocumentML(documentVector);

			for (Integer depth : treeLabelResult.keySet()) {
				List<LabelKeyValuePair> classifiedLabelScoreList = treeLabelResult.get(depth);
				if (classifiedLabelScoreList == null) {
					classifiedLabelScoreList = new ArrayList<LabelKeyValuePair>();
				}
				List<LabelKeyValuePair> classifiedLabels = new ArrayList<LabelKeyValuePair>();
				for (int i = 0; i < Math.min(topK, classifiedLabelScoreList.size()); ++i) {
					classifiedLabels.add(classifiedLabelScoreList.get(i));
				}
				for (LabelKeyValuePair labelScore : classifiedLabels) {
					String label = labelScore.getLabel();//.split(":")[1];
					double score = labelScore.getScore();
					
					if (topicDocScoreMapConceptTree.containsKey(label) == false) {
						topicDocScoreMapConceptTree.put(label, new HashMap<String, Double>());
					}
					topicDocScoreMapConceptTree.get(label).put(docID, score);
				}
			}
			
			conceptTreeClassificationResults.put(docID, treeLabelResult);
			count++;
			if (count % 1000 == 0) {
				System.out.println("Classified " + count + " documents ...");
			}
		}
		
		// initialize training data
		RCVTopicDocMaps rcvTDM = new RCVTopicDocMaps();
		HashMap<String, String> trainingDataMap = new HashMap<String, String>();

		// generate training data for classifier tree
		int docAdded = 0;
		for (String topic : topicDocScoreMapConceptTree.keySet()) {
			
			HashMap<String, Double> docScoreMap = topicDocScoreMapConceptTree.get(topic);
			TreeMap<String, Double> sortedDocScoreMap = HashSort.sortByValues(docScoreMap);
			int topicSelected = 0;
			int topicDepth = tree.getLabelDepth(topic);
			
			if (topicDepth >= 2) {
				continue;
			}
			for (String docID : sortedDocScoreMap.keySet()) {
				if (topicSelected > selectTopNumDocs)
					break;
				topicSelected++;
				
//				System.out.println("[Debug:] " + topic + ": " + docID + ", " + docScoreMap.get(docID)); 
				
				HashMap<Integer, List<LabelKeyValuePair>> treeLabelResult = conceptTreeClassificationResults.get(docID);
				
				for (Integer depth : treeLabelResult.keySet()) {
					if (depth > topicDepth) {
						continue;
					}
					List<LabelKeyValuePair> classifiedLabelScoreList = treeLabelResult.get(depth);
					if (classifiedLabelScoreList == null) {
						classifiedLabelScoreList = new ArrayList<LabelKeyValuePair>();
					}
					List<LabelKeyValuePair> classifiedLabels = new ArrayList<LabelKeyValuePair>();
					for (int i = 0; i < Math.min(topK, classifiedLabelScoreList.size()); ++i) {
						classifiedLabels.add(classifiedLabelScoreList.get(i));
					}

					for (LabelKeyValuePair labelScore : classifiedLabels) {
						
						if (true) {
							String label = labelScore.getLabel();
							double score = labelScore.getScore();
							
//							System.out.println(topic + ":" + label + "," + score);
							
							if ((rcvTDM.getTopicDocMap().containsKey(label) == true && score > conceptSimilaritygThreshold) 
									|| rcvTDM.getTopicDocMap().containsKey(label) == false) {
								if (rcvTDM.getTopicDocMap().containsKey(label) == true) {
				    				if (rcvTDM.getTopicDocMap().get(label).contains(docID) == false) {
				    					rcvTDM.getTopicDocMap().get(label).add(docID);
				    					docAdded++;
				    				}
								} else {
									rcvTDM.getTopicDocMap().put(label, new HashSet<String>());
									rcvTDM.getTopicDocMap().get(label).add(docID);
									docAdded++;
								}
				    		
								if (rcvTDM.getDocTopicMap().containsKey(docID) == true) {
									if (rcvTDM.getDocTopicMap().get(docID).contains(label) == false) {
										rcvTDM.getDocTopicMap().get(docID).add(label);
									}
								} else {
									rcvTDM.getDocTopicMap().put(docID, new HashSet<String>());
									rcvTDM.getDocTopicMap().get(docID).add(label);
								}
							}
							
						}
					}
				}
				
				trainingDataMap.put(docID, corpusContentProc.getCorpusContentMap().get(docID));
			}
		}
		System.out.println("  [Bootstrapping: ]" + docAdded + " documents added." );
		
		///////////////////////////////////////////////////
		// start bootstrapping
		///////////////////////////////////////////////////
		for (int iter = 0; iter < boostrappingMaxIter; ++iter) {
			RCVTreeLabelData treeLabelData = new RCVTreeLabelData();
			treeLabelData.readTreeHierarchy(fileTopicHierarchyPath);
			treeLabelData.readTopicDescription(fileTopicDescriptionPath);

			
			AbstractLabelTree classifierTree = null;
			AbstractClassifierLibLinearTree classifierTreeUpdate = null;
			if (direction .equals("bottomup")) {
				classifierTreeUpdate = new ClassifierLibLinearTreeBottomUpML("rcv");
			} else {
				classifierTreeUpdate = new ClassifierLibLinearTreeTopDownML("rcv");
			}
			
			classifierTreeUpdate.initializeWithContentData(trainingDataMap, treeLabelData, rcvTDM);
			classifierTreeUpdate.setPenaltyParaC(penaltyPara);
			classifierTreeUpdate.trainAllTreeNodes();
			classifierTree = classifierTreeUpdate;
			
			System.out.println();
			System.out.println("***********Iteration " + iter + "************");
			System.out.println();

			// read test topic doc maps // this is only for evaluation, not for training
			
			resultMap = Evaluation.testMultiLabelContentTreeResults((InterfaceMultiLabelContentClassificationTree) classifierTree, 
					corpusContentProc.getCorpusContentMap(), 
					null,
					topicDocMapTest, 
					docTopicMapTest, 
					outputClassificationFile, 
					outputLabelComparisonFile, 
					topK, false); 
			
			precisionListD1.add(resultMap.get("depth" + evalDepth).precision);
			recallListD1.add(resultMap.get("depth" + evalDepth).recall);
			mf1ListD1.add(resultMap.get("depth" + evalDepth).mf1);
			Mf1ListD1.add(resultMap.get("depth" + evalDepth).Mf1);
			

			// test end
			
			// classify training documents
			count = 0;
			HashMap<String, HashMap<Integer, List<LabelKeyValuePair>>> classifierTreeClassificationResults = 
					new HashMap<String,HashMap<Integer, List<LabelKeyValuePair>>>();
			HashMap<String, HashMap<String, Double>> topicDocScoreMapClassifierTree = new HashMap<String, HashMap<String, Double>>();
			for (String docID : corpusContentProc.getCorpusConceptVectorMap().keySet()) {
				String documentVector = corpusContentProc.getCorpusContentMap().get(docID);
				
				HashMap<Integer, List<LabelKeyValuePair>> treeLabelResult = ((InterfaceMultiLabelContentClassificationTree) classifierTree).labelDocumentContentML(documentVector);

				
				for (Integer depth : treeLabelResult.keySet()) {
					List<LabelKeyValuePair> classifiedLabelScoreList = treeLabelResult.get(depth);
					if (classifiedLabelScoreList == null) {
						classifiedLabelScoreList = new ArrayList<LabelKeyValuePair>();
					}
					List<LabelKeyValuePair> classifiedLabels = new ArrayList<LabelKeyValuePair>();
					for (int i = 0; i < Math.min(topK, classifiedLabelScoreList.size()); ++i) {
						classifiedLabels.add(classifiedLabelScoreList.get(i));
					}
					for (LabelKeyValuePair labelScore : classifiedLabels) {
						String label = labelScore.getLabel();//.split(":")[1];
						double score = labelScore.getScore();
						
						if (topicDocScoreMapClassifierTree.containsKey(label) == false) {
							topicDocScoreMapClassifierTree.put(label, new HashMap<String, Double>());
						}
						topicDocScoreMapClassifierTree.get(label).put(docID, score);
					}
				}
				
				classifierTreeClassificationResults.put(docID, treeLabelResult);
				count++;
				if (count % 1000 == 0) {
					System.out.println("Classified " + count + " documents ...");
				}
			}
			
			// add more documents for training
			
			docAdded = 0;
			for (String topic : topicDocScoreMapClassifierTree.keySet()) {
				HashMap<String, Double> docScoreMap = topicDocScoreMapClassifierTree.get(topic);
				TreeMap<String, Double> sortedDocScoreMap = HashSort.sortByValues(docScoreMap);
				int topicSelected = 0;
				int topicDepth = classifierTree.getLabelDepth(topic);
				if (topicDepth >= 2) {
					continue;
				}
				for (String docID : sortedDocScoreMap.keySet()) {
					if (topicSelected > selectTopNumDocs * (iter + 1))
						break;
					topicSelected++;
					
					HashMap<Integer, List<LabelKeyValuePair>> treeLabelResult = classifierTreeClassificationResults.get(docID);

					for (Integer depth : treeLabelResult.keySet()) {
						if (depth > topicDepth) {
							continue;
						}
						
						List<LabelKeyValuePair> classifiedLabelScoreList = treeLabelResult.get(depth);
						if (classifiedLabelScoreList == null) {
							classifiedLabelScoreList = new ArrayList<LabelKeyValuePair>();
						}
						List<LabelKeyValuePair> classifiedLabels = new ArrayList<LabelKeyValuePair>();
						for (int i = 0; i < Math.min(topK, classifiedLabelScoreList.size()); ++i) {
							classifiedLabels.add(classifiedLabelScoreList.get(i));
						}
						
						for (LabelKeyValuePair labelScore : classifiedLabels) {
							if (true) { //if (labelScore.getLabel().equals(topic)) {
								String label = labelScore.getLabel();
								double score = labelScore.getScore();
								
								if (score > bootstrappingThreshold) {
									if (rcvTDM.getTopicDocMap().containsKey(label) == true) {
					    				if (rcvTDM.getTopicDocMap().get(label).contains(docID) == false) {
					    					rcvTDM.getTopicDocMap().get(label).add(docID);
					    					docAdded++;
					    				}
									} else {
										rcvTDM.getTopicDocMap().put(label, new HashSet<String>());
										rcvTDM.getTopicDocMap().get(label).add(docID);
										docAdded++;
									}
					    		
									if (rcvTDM.getDocTopicMap().containsKey(docID) == true) {
										if (rcvTDM.getDocTopicMap().get(docID).contains(label) == false) {
											rcvTDM.getDocTopicMap().get(docID).add(label);
										}
									} else {
										rcvTDM.getDocTopicMap().put(docID, new HashSet<String>());
										rcvTDM.getDocTopicMap().get(docID).add(label);
									}
								}
								
								
							}
						
						}
					}
					
					trainingDataMap.put(docID, corpusContentProc.getCorpusContentMap().get(docID));
				}
			}
			System.out.println("  [Bootstrapping: ]" + docAdded + " documents added." );
		}
		
		
		System.out.println();
		System.out.println("D1 precision");
		for (int i = 0; i < precisionListD1.size(); ++i) {
			System.out.print(precisionListD1.get(i) + "\t");
		}
		System.out.println();
		System.out.println("D1 recall");
		for (int i = 0; i < recallListD1.size(); ++i) {
			System.out.print(recallListD1.get(i) + "\t");
		}
		System.out.println();
		System.out.println("D1 mf1");
		for (int i = 0; i < mf1ListD1.size(); ++i) {
			System.out.print(mf1ListD1.get(i) + "\t");
		}
		System.out.println();
		System.out.println("D1 Mf1");
		for (int i = 0; i < Mf1ListD1.size(); ++i) {
			System.out.print(Mf1ListD1.get(i) + "\t");
		}
		
		
		System.out.println();
		System.out.println("finished");
		
		
		return resultMap.get("depth" + evalDepth);
	}
}
