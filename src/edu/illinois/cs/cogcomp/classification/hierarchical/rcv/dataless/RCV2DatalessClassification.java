package edu.illinois.cs.cogcomp.classification.hierarchical.rcv.dataless;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

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
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.StopWords;
import edu.illinois.cs.cogcomp.classification.hierarchy.evaluation.EvalResults;
import edu.illinois.cs.cogcomp.classification.hierarchy.evaluation.Evaluation;
import edu.illinois.cs.cogcomp.classification.hierarchy.evaluation.StatUtils;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;

public class RCV2DatalessClassification {
public static HashMap<String, Double> conceptWeights = new HashMap<String, Double>();

	
	public static void main(String[] args) {
		
		for (int i = 0; i < LanguageMapping.lgNames.length; ++i) {
			String lgName = LanguageMapping.lgNames[i];
			System.out.println("-------------------------------" + lgName + "-------------------------------");
			testSimpleConcepts (lgName, "topdown", 1);
		}
		

	}
	
	public static void testSimpleConcepts(String lgName, String direction, int topK) {
		
		int seed = 0;
		Random random = new Random(seed);
		double trainingRate = 0.5;

		String stopWordsFile = "";
		String docIDContentConceptFile = "";
		String docIDTopicMapFile = "";
		String treeConceptFile = "";
		String outputClassificationFile = "";
		String outputLabelComparisonFile = "";
		String method = "null";
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
		
		StopWords.rcvStopWords = StopWords.readStopWords (stopWordsFile);
		
		RCVCorpusConceptData corpusContentProc = new RCVCorpusConceptData();
		corpusContentProc.readCorpusContentAndConcepts(docIDContentConceptFile, ClassifierConstant.isBreakConcepts, random, trainingRate, conceptWeights);

		// read topic doc maps
		RCVTopicDocMaps rcvTDM = new RCVTopicDocMaps();
		rcvTDM.readFilteredTopicDocMap (docIDTopicMapFile, corpusContentProc.getCorpusConceptVectorMap().keySet());
		
		HashMap<String, HashSet<String>> topicDocMap = rcvTDM.getTopicDocMap();
		HashMap<String, HashSet<String>> docTopicMap = rcvTDM.getDocTopicMap();
		
		// read tree
		AbstractConceptTree tree = null;
		if (direction .equals("bottomup")) {
			tree = new ConceptTreeBottomUpML(data, method, conceptWeights, false);
		} else {
			tree = new ConceptTreeTopDownML(data, method, conceptWeights, false);
		}
		
		System.out.println("process tree...");
		tree.readLabelTreeFromDump(treeConceptFile, ClassifierConstant.isBreakConcepts);
		ConceptTreeNode rootNode = tree.initializeTreeWithConceptVector("root", 0, ClassifierConstant.isBreakConcepts);
		tree.setRootNode(rootNode);
		System.out.println("process tree finished");
		
		HashMap<String, EvalResults> resultsMap = Evaluation.testMultiLabelConceptTreeResults ((InterfaceMultiLabelConceptClassificationTree) tree,
				corpusContentProc.getCorpusConceptVectorMap(), 
				topicDocMap, docTopicMap,
				outputClassificationFile,  outputLabelComparisonFile, 
				topK);
		int depth = 1;
		System.out.println("Final results for depth " + depth);
		System.out.println("Precision: " + String.format("%.4f", resultsMap.get("depth" + depth).precision));
		System.out.println("Recall: " + String.format("%.4f", resultsMap.get("depth" + depth).recall));
		System.out.println("mF1: " + String.format("%.4f", resultsMap.get("depth" + depth).mf1));
		System.out.println("MF1: " + String.format("%.4f", resultsMap.get("depth" + depth).Mf1));
		System.out.println(System.getProperty("line.separator").toString());	


	}

}
