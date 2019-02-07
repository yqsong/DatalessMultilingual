package edu.illinois.cs.cogcomp.classification.hierarchical.rcv.muse;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import edu.illinois.cs.cogcomp.MultiLingualResourcesConfig;
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
import edu.illinois.cs.cogcomp.embedding.multiembedding.EmbeddingLanguages;

public class RCV2EmbeddingClassification {
public static HashMap<String, Double> conceptWeights = new HashMap<String, Double>();

	
	public static void main(String[] args) {
		MultiLingualResourcesConfig.initialization();
//		String embeddingSource = "ted";
//		HashMap<String, String> availability = EmbeddingLanguages.ted_availability;	
//		String embeddingSource = "europarl";
//		HashMap<String, String> availability = EmbeddingLanguages.europarl_availability;
		String embeddingSource = "muse";
		
		embeddingSource = "muse";
		HashMap<String, String> availability = new HashMap<String, String>();	
		String newMuseFolderStr = MultiLingualResourcesConfig.musePath;
		File newMuseFolder = new File (newMuseFolderStr);
		String[] fileList = newMuseFolder.list();
		for (int i = 0; i < fileList.length; ++i) {
			String file = fileList[i];
			System.out.println(file);
			String[] tokens = file.split("\\.");
			System.out.println(tokens[2]);
			availability.put(tokens[2], tokens[2]);
		}
		for (int i = 0; i < LanguageMapping.lgNames.length; ++i) {
		String lgName = LanguageMapping.lgNames[i];
		if (availability.containsKey(LanguageMapping.lgMapping.get(LanguageMapping.lgNames[i])) == true) {
			System.out.println("-------------------------------" + lgName + "-------------------------------");
			testSimpleConcepts (embeddingSource, lgName, "topdown", 1);
		}
	}
		
//		embeddingSource = "babylon";
//		HashMap<String, String> availability = EmbeddingLanguages.muse_availability;
//		availability = EmbeddingLanguages.muse_availability;	
//		String newMuseFolderStr = MultiLingualResourcesConfig.babylonPath;
//		File newMuseFolder = new File (newMuseFolderStr);
//		String[] fileList = newMuseFolder.list();
//		for (int i = 0; i < fileList.length; ++i) {
//			String file = fileList[i];
//			System.out.println(file);
//			String[] tokens = file.split("\\.");
//			System.out.println(tokens[1]);
//			availability.put(tokens[1], tokens[1]);
//		}
//		for (int i = 0; i < LanguageMapping.lgNames.length; ++i) {
//			String lgName = LanguageMapping.lgNames[i];
//			if (availability.containsKey(LanguageMapping.lgMapping.get(LanguageMapping.lgNames[i])) == true) {
//				System.out.println("-------------------------------" + lgName + "-------------------------------");
//				testSimpleConcepts (embeddingSource, lgName, "topdown", 1);
//			}
//		}
//		
//		for (int lgID = 2; lgID < LanguageMapping.lgNames.length; lgID++) {
//			String lgName = LanguageMapping.lgMapping.get(LanguageMapping.lgNames[lgID]);
//			testSimpleConcepts ("muse", lgName, "topdown", 1);
//		}
//		testSimpleConcepts ("", "english", "topdown", 1);
	}
	
	public static void testSimpleConcepts(String embeddingSource, String lgName, String direction, int topK) {
		
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
			docIDContentConceptFile = "/home/data/corpora/rcv2/output/" + embeddingSource + "/" + lgName + ".rcv1.embedding.muse.300";
			treeConceptFile = "/home/data/corpora/rcv2/output/" + embeddingSource + "/" + lgName + ".tree.rcv1.embedding.muse.300";
			docIDTopicMapFile = "data/rcvTest/rcv1-v2.topics.qrels";
			outputClassificationFile = "/home/data/corpora/rcv2/output/results." + lgName + ".supervised.classification";
			outputLabelComparisonFile = "/home/data/corpora/rcv2/output/results." + lgName + ".supervised.labelComparison";
		} else {
			docIDContentConceptFile = "/home/data/corpora/rcv2/output/" + embeddingSource + "/" + lgName + ".rcv2.embeddings.muse.300";
			treeConceptFile = "/home/data/corpora/rcv2/output/" + embeddingSource + "/" + lgName + ".tree.rcv2.embeddings.muse.300";
			docIDTopicMapFile = "/home/data/corpora/rcv2/output/" + embeddingSource + "/" + lgName + ".rcv2.labels";
			outputClassificationFile = "/home/data/corpora/rcv2/output/results." + lgName + ".supervised.classification";
			outputLabelComparisonFile = "/home/data/corpora/rcv2/output/results." + lgName + ".supervised.labelComparison";
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
