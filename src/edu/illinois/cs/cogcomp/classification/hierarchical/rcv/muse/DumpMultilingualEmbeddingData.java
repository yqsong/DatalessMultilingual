package edu.illinois.cs.cogcomp.classification.hierarchical.rcv.muse;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.lucene.analysis.Analyzer;

import edu.illinois.cs.cogcomp.MultiLingualResourcesConfig;
import edu.illinois.cs.cogcomp.classification.flat.ted.data.TEDData;
import edu.illinois.cs.cogcomp.classification.hierarchical.rcv.data.LanguageMapping;
import edu.illinois.cs.cogcomp.classification.hierarchical.rcv.data.RCV2Data;
import edu.illinois.cs.cogcomp.classification.hierarchical.rcv.data.RCV2DataReader;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.AbstractConceptTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ConceptTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml.ConceptTreeTopDownML;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.rcv.RCVCorpusConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.ConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;
import edu.illinois.cs.cogcomp.classification.main.DatalessResourcesConfig;
import edu.illinois.cs.cogcomp.classification.representation.esa.AbstractESA;
import edu.illinois.cs.cogcomp.classification.representation.word2vec.MemoryBasedWordEmbedding;
import edu.illinois.cs.cogcomp.classification.representation.word2vec.WordEmbeddingInterface;
import edu.illinois.cs.cogcomp.embedding.esa.index.AnalyzerFactory;
import edu.illinois.cs.cogcomp.embedding.esa.index.CrossLanguageWikipediaDocumentIndex;
import edu.illinois.cs.cogcomp.embedding.esa.index.ReadLangLinkingFile;
import edu.illinois.cs.cogcomp.embedding.esa.multiesa.MultiLingalESA;
import edu.illinois.cs.cogcomp.embedding.multiembedding.EmbeddingLanguages;

public class DumpMultilingualEmbeddingData {
	
	public static void main (String[] args) {
		MultiLingualResourcesConfig.initialization();

		int lgID = 8;
		
		String embeddingSource = "europarl";
		HashMap<String, String> availability = EmbeddingLanguages.europarl_availability;

		String lgName = LanguageMapping.lgMapping.get(LanguageMapping.lgNames[lgID]);
//		if (availability.containsKey(lgName) == true) {
//			System.out.println(LanguageMapping.lgNames[lgID]);
//			rcvDumpTreeData(lgID, embeddingSource);
//			dumpLabelData (lgID, embeddingSource);
//			dumptCorpusEmbedding (lgID, embeddingSource);
//		}
		
//		embeddingSource = "ted";
//		availability = EmbeddingLanguages.ted_availability;	
//		
//		lgName = LanguageMapping.lgMapping.get(LanguageMapping.lgNames[lgID]);
//		
//		String outputFolder = "/shared/shelley/yqsong/data/rcv2/output/";
//		if (availability.containsKey(lgName) == true) {
//			System.out.println(LanguageMapping.lgNames[lgID]);
//			rcvDumpTreeData(lgID, embeddingSource, outputFolder);
//			dumpLabelData (lgID, embeddingSource, outputFolder);
//			dumptCorpusEmbedding (lgID, embeddingSource, outputFolder);
//		}
		
		embeddingSource = "muse";
		availability = new HashMap<String, String>();	
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
		
		String outputFolder = "/home/data/corpora/rcv2/output/";
		
		for ( lgID = 1; lgID < LanguageMapping.lgNames.length; lgID++) {
			lgName = LanguageMapping.lgMapping.get(LanguageMapping.lgNames[lgID]);
			if (availability.containsKey(lgName) == true) {
				System.out.println(LanguageMapping.lgNames[lgID]);
				rcvDumpTreeData(lgID, embeddingSource, outputFolder);
				dumpLabelData (lgID, embeddingSource, outputFolder);
				dumptCorpusEmbedding (lgID, embeddingSource, outputFolder);
			}
		}
			
		
		
//		embeddingSource = "babylon";
//		availability = new HashMap<String, String>();	
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
//		
//		String outputFolder = "/home/data/corpora/rcv2/output/";
//		
//		for ( lgID = 1; lgID < LanguageMapping.lgNames.length; lgID++) {
//			lgName = LanguageMapping.lgMapping.get(LanguageMapping.lgNames[lgID]);
//			if (availability.containsKey(lgName) == true) {
//				System.out.println(LanguageMapping.lgNames[lgID]);
//				rcvDumpTreeData(lgID, embeddingSource, outputFolder);
//				dumpLabelData (lgID, embeddingSource, outputFolder);
//				dumptCorpusEmbedding (lgID, embeddingSource, outputFolder);
//			}
//		}
			
	}
	
	
	public static void dumptCorpusEmbedding (int id, String embeddingSource, String outputFolder) 	{
		String lgName = LanguageMapping.lgNames[id];
		
		int conceptNum = 300;
		String outputData = outputFolder + embeddingSource + "/" + lgName + ".rcv2.embeddings.muse." + conceptNum;
		
		String intputFolderStr = MultiLingualResourcesConfig.rcv2DataFolder + lgName + "/";
		Analyzer analyzer = AnalyzerFactory.initialize(LanguageMapping.lgMapping.get(lgName));
		
		HashMap<String, String> contentDataMap = new HashMap<String, String>();
		File inputFolder = new File (intputFolderStr);
		File[] folderList = inputFolder.listFiles();
		int total = 0;
		int labeled = 0;
		int unlabeled = 0;
		for (int i = 0; i < folderList.length; ++i) {
			File[] fileList = folderList[i].listFiles();
			for (int j = 0; j < fileList.length; ++j) {
				total++;
				String filePath = fileList[j].getAbsolutePath();
				RCV2Data data = RCV2DataReader.readDocument(filePath, analyzer);
				if (data.categories.size() > 0) {
					contentDataMap.put(filePath, data.tokenizedContent);
					labeled++;
				} else {
					unlabeled++;
				}
			}
		}
		System.out.println("[Count] total: " + total + ", labeled: " + labeled + ", unlabeled: " + unlabeled);
		writeCorpusEmbeddingData(contentDataMap, conceptNum, outputData, LanguageMapping.lgMapping.get(lgName), embeddingSource);
	}
	
	public static void writeCorpusEmbeddingData (HashMap<String, String> corpusContentMap, int numConcepts, String file, String lgName, String embeddingSource) {
		
		String lg = lgName;
		String content = "";
		try {
			DatalessResourcesConfig.embeddingDimension = MultiLingualResourcesConfig.word2vecDim;
			DatalessResourcesConfig.embeddingDimension = 300;
			WordEmbeddingInterface lg_embedding = null;
			if (embeddingSource.equals("ted")) {
				if (lgName.equals("pt")) {
					lg = "pb";
				}
				DatalessResourcesConfig.memorybasedW2V = MultiLingualResourcesConfig.bicvmTEDPath + lg + "-en-new.txt";
				lg_embedding = new MemoryBasedWordEmbedding();
			}
			if (embeddingSource.equals("europarl")) {
				if (lgName.equals("pb")) {
					lg = "pt";
				}
				DatalessResourcesConfig.memorybasedW2V = MultiLingualResourcesConfig.bicvmEuroparlPath + lg + "-en.txt";
				lg_embedding = new MemoryBasedWordEmbedding();
			}
			if (embeddingSource.equals("babylon")) {
				DatalessResourcesConfig.memorybasedW2V = MultiLingualResourcesConfig.musePath + "wiki." + lg + ".vec";
				lg_embedding = new MemoryBasedWordEmbedding();
			}
			if (embeddingSource.equals("muse")) {
				DatalessResourcesConfig.memorybasedW2V = MultiLingualResourcesConfig.musePath + "wiki.multi." + lg + ".vec";
				lg_embedding = new MemoryBasedWordEmbedding();
			}
			int count = 0;
			FileWriter writer = new FileWriter(file);
			for (String docID : corpusContentMap.keySet()) {
				count++;
				System.out.println("written " + count +  " documents with concepts");
				content = corpusContentMap.get(docID);
				
				double[] vector = lg_embedding.getDenseVectorSimpleAverage(content);
				
				String docContent = corpusContentMap.get(docID);
				writer.write(docID + "\t" + docContent + "\t");
				for (int i = 0; i < vector.length; ++i) {
					writer.write(i + "," + vector[i] + ";");
				}
				writer.write(ReadLangLinkingFile.systemNewLine);
			}
			writer.close();
		} catch (Exception e) {
			System.out.println(content);
			e.printStackTrace();
		}
			
	}
	
	public static void rcvDumpTreeData (int id, String embeddingSource, String outputFolder) {
		String lgName = LanguageMapping.lgNames[id];
		
		WordEmbeddingInterface en_embedding = null;
		DatalessResourcesConfig.embeddingDimension = MultiLingualResourcesConfig.word2vecDim;
		DatalessResourcesConfig.embeddingDimension = 300;
		String lg = LanguageMapping.lgMapping.get(lgName);
		
		if (embeddingSource.equals("ted")) {
			if (lg.equals("pt")) {
				lg = "pb";
			}
			DatalessResourcesConfig.memorybasedW2V = MultiLingualResourcesConfig.bicvmTEDPath + "en" + "-" + lg + "-new.txt";
			en_embedding = new MemoryBasedWordEmbedding();
		}
		if (embeddingSource.equals("europarl")) {
			DatalessResourcesConfig.memorybasedW2V = MultiLingualResourcesConfig.bicvmEuroparlPath + "en" + "-" + lg + ".txt";
			en_embedding = new MemoryBasedWordEmbedding();
		}
		if (embeddingSource.equals("babylon")) {
//			DatalessResourcesConfig.memorybasedW2V = MultiLingualResourcesConfig.bicvmEuroparlPath + "en" + "-" + lg + ".txt";
//			en_embedding = new MemoryBasedWordEmbedding();
			DatalessResourcesConfig.memorybasedW2V = MultiLingualResourcesConfig.musePath + "wiki.en.vec";
			en_embedding = new MemoryBasedWordEmbedding();
		}
		if (embeddingSource.equals("muse")) {
//			DatalessResourcesConfig.memorybasedW2V = MultiLingualResourcesConfig.bicvmEuroparlPath + "en" + "-" + lg + ".txt";
//			en_embedding = new MemoryBasedWordEmbedding();
			DatalessResourcesConfig.memorybasedW2V = MultiLingualResourcesConfig.musePath + "wiki.multi.en.vec";
			en_embedding = new MemoryBasedWordEmbedding();
		}
		
		int conceptNum = 300;
		String outputData = outputFolder + embeddingSource + "/" + lgName + ".tree.rcv2.embeddings.muse." + conceptNum;

		HashMap<String, Double> conceptWeights = new HashMap<String, Double>();
		String fileTopicHierarchyPath = MultiLingualResourcesConfig.rcv1Hierarchy;
		String fileTopicDescriptionPath = MultiLingualResourcesConfig.rcv1Topics;
		AbstractConceptTree tree = new ConceptTreeTopDownML(DatalessResourcesConfig.CONST_DATA_RCV, "wordDistSimple", conceptWeights, true);
		System.out.println("process tree...");
		tree.word2vec = en_embedding;
		tree.treeLabelData.readTreeHierarchy(fileTopicHierarchyPath);
		tree.treeLabelData.readTopicDescription(fileTopicDescriptionPath);
		ConceptTreeNode rootNode = tree.initializeTree("root", 0);
		tree.setRootNode(rootNode);
		tree.aggregateChildrenDescription(rootNode);
		tree.setConceptNum(conceptNum);
		tree.conceptualizeTreeLabels(rootNode, ClassifierConstant.isBreakConcepts);

		tree.dumpTree(outputData);
		System.out.println("process tree finished");
	}
	
	public static void dumpLabelData (int id, String embeddingSource, String outputFolder) {
		String lgName = LanguageMapping.lgNames[id];
		
		String outputData = outputFolder + embeddingSource + "/" + lgName + ".rcv2.labels";
		
		String intputFolderStr = MultiLingualResourcesConfig.rcv2DataFolder + lgName + "/";
		Analyzer analyzer = AnalyzerFactory.initialize(LanguageMapping.lgMapping.get(lgName));
		
		try {
			FileWriter writer = new FileWriter (outputData);
			HashMap<String, String> contentDataMap = new HashMap<String, String>();
			File inputFolder = new File (intputFolderStr);
			File[] folderList = inputFolder.listFiles();
			for (int i = 0; i < folderList.length; ++i) {
				File[] fileList = folderList[i].listFiles();
				for (int j = 0; j < fileList.length; ++j) {
					String filePath = fileList[j].getAbsolutePath();
					RCV2Data data = RCV2DataReader.readDocument(filePath, analyzer);
					contentDataMap.put(filePath, data.tokenizedContent);
					
					for (int k = 0; k < data.categories.size(); ++k) {
						writer.write(data.categories.get(k) + " " + filePath + " " + 1 + ReadLangLinkingFile.systemNewLine);
					}
				}
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		

	}
}
