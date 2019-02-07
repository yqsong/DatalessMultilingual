package edu.illinois.cs.cogcomp.classification.hierarchical.rcv.supervised;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import de.bwaldvogel.liblinear.SolverType;

import edu.illinois.cs.cogcomp.classification.hierarchical.rcv.data.LanguageMapping;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.liblinear.AbstractClassifierLibLinearTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.liblinear.ml.ClassifierLibLinearTreeBottomUpML;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.liblinear.ml.ClassifierLibLinearTreeTopDownML;
import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.AbstractLabelTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.InterfaceMultiLabelContentClassificationTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.rcv.RCVCorpusConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.rcv.RCVTopicDocMaps;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.StopWords;
import edu.illinois.cs.cogcomp.classification.hierarchy.evaluation.EvalResults;
import edu.illinois.cs.cogcomp.classification.hierarchy.evaluation.Evaluation;
import edu.illinois.cs.cogcomp.classification.hierarchy.evaluation.StatUtils;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;

public class RCV2SupervisedClassification {
	public static HashMap<String, Double> conceptWeights = new HashMap<String, Double>();

	
	// The ratios are computed to limit the training set as 1000 documents.
	public static HashMap<String, Double> sampleOne = new HashMap<String, Double>() {
		{
			put("english", 0.0431984103);
			put("chinese", 0.03452561801);
			put("danish", 0.08940545373);
			put("dutch", 0.5574136009);
			put("french", 0.01171056176);
			put("german", 0.008604963343);
			put("italian", 0.03520383018);
			put("japanese", 0.01526740866);
			put("norwegian", 0.1062812201);
			put("portuguese", 0.1131093768);
			put("russian", 0.05718533768);
			put("spanish", 0.05360493165);
			put("spanish-latam", 0.01253525541);
			put("swedish", 0.06356470887);
		}
	};
	
	public static void main (String[] args) {
		ClassifierConstant.solver = SolverType.L2R_L2LOSS_SVC_DUAL;
		
		int maxTrainingNum = 2000;
		for (int i = 0; i < LanguageMapping.lgNames.length; ++i) {
			String lgName = LanguageMapping.lgNames[i];
			System.out.println("-------------------------------" + lgName + "-------------------------------");
			testTop1 (lgName, "bottomup", sampleOne.get(lgName) * 5, maxTrainingNum);
			System.out.println("-------------------------------" + lgName + " [Finished]" + "-------------------------------");
			System.out.println();
		}
		
	}
	
	public static void testTop1 (String lgName, String direction, double trainingRate, int maxTrainingNum) {
		try {
						
			List<Double> precisionList = new ArrayList<Double>();
			List<Double> recallList = new ArrayList<Double>();
			List<Double> mf1List = new ArrayList<Double>();
			List<Double> Mf1List = new ArrayList<Double>();

			for (int i = 0; i < 10; ++i) {
				double penalty = 10000;//Math.pow(i+1, 10);
				int topK = 1;
				int seed = i;
				EvalResults result = testRCVData(lgName, direction, penalty, topK, seed, trainingRate, maxTrainingNum);
				
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
	
	public static EvalResults testRCVData (String lgName, String direction, double penaltyPara, int topK, int seed, double trainingRate, int maxTrainingNum) {
		Random random = new Random(seed);
		
		String stopWordsFile = "";
		stopWordsFile = "data/rcvTest/english.stop";
		StopWords.rcvStopWords = StopWords.readStopWords (stopWordsFile);

		String fileTopicHierarchyPath = "";
		String fileTopicDescriptionPath = "";
		String fileContentDataPath = "";
		String fileTopicDocMapPath = "";
		String outputClassificationFile = "";
		String outputLabelComparisonFile = "";
		fileTopicHierarchyPath = "data/rcvTest/rcv1.topics.hier.orig";
		if (lgName.equals("english")) {
			stopWordsFile = "data/rcvTest/english.stop";
			fileTopicDescriptionPath = "data/rcvTest/topics.rbb";
			fileContentDataPath = "/shared/shelley/yqsong/data/rcv1v2/org_output/rcv_train.simple.esa.concepts.500";
//			fileContentDataPath = "/shared/shelley/yqsong/data/rcv1v2/output_train/rcv_train.simple.esa.concepts.500";
			fileTopicDocMapPath = "data/rcvTest/rcv1-v2.topics.qrels";
			outputClassificationFile = "/shared/shelley/yqsong/data/rcv1v2/output/result.concept.rcv.classification";
			outputLabelComparisonFile = "/shared/shelley/yqsong/data/rcv1v2/output/result.concept.rcv.labelComparison";
		} else {
			fileTopicDescriptionPath = "data/rcvTest/topics.rbb";
			fileContentDataPath = "/shared/shelley/yqsong/data/rcv2/output/preprocessed/" + lgName + ".rcv2.esa.concepts.500";
			fileTopicDocMapPath = "/shared/shelley/yqsong/data/rcv2/output/preprocessed/" + lgName + ".rcv2.labels";
			outputClassificationFile = "/shared/shelley/yqsong/data/rcv2/output/results." + lgName + ".supervised.classification";
			outputLabelComparisonFile = "/shared/shelley/yqsong/data/rcv2/output/results." + lgName + ".supervised.labelComparison";
		}
		
//		fileTopicDescriptionPath = "data/rcvTest/topics.rbb";
//		fileContentDataPath = "/shared/shelley/yqsong/data/rcv2/output/preprocessed/" + lgName + ".rcv2.esa.concepts.500";
//		fileTopicDocMapPath = "/shared/shelley/yqsong/data/rcv2/output/preprocessed/" + lgName + ".rcv2.labels";
//		outputClassificationFile = "/shared/shelley/yqsong/data/rcv2/output/results." + lgName + ".supervised.classification";
//		outputLabelComparisonFile = "/shared/shelley/yqsong/data/rcv2/output/results." + lgName + ".supervised.labelComparison";
		
		RCVCorpusConceptData testData = new RCVCorpusConceptData();
		testData.readCorpusContentAndConcepts(fileContentDataPath, ClassifierConstant.isBreakConcepts, random, trainingRate, maxTrainingNum, conceptWeights);;

		Calendar cal1 = Calendar.getInstance();
	    long startTime = cal1.getTimeInMillis();
	    
		AbstractLabelTree classifierTree = null;

		AbstractClassifierLibLinearTree classifierTreeUpdate = null;
		if (direction .equals("bottomup")) {
			classifierTreeUpdate = new ClassifierLibLinearTreeBottomUpML("rcv");
		} else {
			classifierTreeUpdate = new ClassifierLibLinearTreeTopDownML("rcv");
		}
		
		classifierTreeUpdate.initializeWithContentData(testData.getCorpusContentMapTraining(),
				fileTopicHierarchyPath,
				fileTopicDescriptionPath,
				fileTopicDocMapPath);
		classifierTreeUpdate.setPenaltyParaC(penaltyPara);
		classifierTreeUpdate.trainAllTreeNodes();
		classifierTree = classifierTreeUpdate;
		
		Calendar cal2 = Calendar.getInstance();
		long endTime = cal2.getTimeInMillis();
		long secondTraining = (endTime - startTime)/1000;

		Calendar cal3 = Calendar.getInstance();
	    startTime = cal3.getTimeInMillis();
	    
		// read topic doc maps
		RCVTopicDocMaps testTopicDocMapData = new RCVTopicDocMaps();
		testTopicDocMapData.readFilteredTopicDocMap (fileTopicDocMapPath, testData.getCorpusContentMapTest().keySet());
		HashMap<String, HashSet<String>> topicDocMap = testTopicDocMapData.getTopicDocMap();
		HashMap<String, HashSet<String>> docTopicMap = testTopicDocMapData.getDocTopicMap();
		HashMap<String, EvalResults> resultsMap = Evaluation.testMultiLabelContentTreeResults((InterfaceMultiLabelContentClassificationTree) classifierTree, 
				testData.getCorpusContentMapTest(), 
				null,
				topicDocMap, 
				docTopicMap, 
				outputClassificationFile, 
				outputLabelComparisonFile, 
				topK, false); 
		
		Calendar cal4 = Calendar.getInstance();
		endTime = cal4.getTimeInMillis();
		long secondTesting = (endTime - startTime)/1000;
		System.out.println("  [Training time:] " + secondTraining + " seconds");
		System.out.println("  [Training time:] " + secondTesting + " seconds");
		
		int depth = 1;
		System.out.println("Final results for depth " + depth);
		System.out.println("Precision: " + String.format("%.4f", resultsMap.get("depth" + depth).precision));
		System.out.println("Recall: " + String.format("%.4f", resultsMap.get("depth" + depth).recall));
		System.out.println("mF1: " + String.format("%.4f", resultsMap.get("depth" + depth).mf1));
		System.out.println("MF1: " + String.format("%.4f", resultsMap.get("depth" + depth).Mf1));
		System.out.println(System.getProperty("line.separator").toString());	

		return resultsMap.get("depth" + depth);
	}
}
