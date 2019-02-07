package edu.illinois.cs.cogcomp.classification.hierarchical.rcv.muse;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.Random;

import edu.illinois.cs.cogcomp.MultiLingualResourcesConfig;
import edu.illinois.cs.cogcomp.classification.hierarchical.rcv.data.LanguageMapping;
import edu.illinois.cs.cogcomp.classification.hierarchical.rcv.data.RCV1OriginalCorpusConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.AbstractConceptTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ConceptTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml.ConceptTreeTopDownML;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;
import edu.illinois.cs.cogcomp.classification.main.DatalessResourcesConfig;
import edu.illinois.cs.cogcomp.classification.representation.word2vec.MemoryBasedWordEmbedding;
import edu.illinois.cs.cogcomp.embedding.esa.index.ReadLangLinkingFile;

public class DumpEnglishEmbeddingData {
	
	
	public static void main (String[] args) {
		
		String embeddingSource = "muse";
		
		int lgID = 0;
		MultiLingualResourcesConfig.initialization();
		System.out.println(LanguageMapping.lgNames[lgID]);
		String outputFolder = "/home/data/corpora/rcv2/output/" + embeddingSource + "/";
		rcvDumpTreeData(lgID, outputFolder, embeddingSource);
		dumptCorpusEmbedding (lgID, outputFolder, embeddingSource);
	}
	
	
	public static void dumptCorpusEmbedding (int id, String outputFolder, String embeddingSource) 	{
		String lgName = LanguageMapping.lgNames[id];

		int conceptNum = 300;
		String outputData = outputFolder + lgName + ".rcv1.embedding." + embeddingSource + "." + conceptNum;
		
		String inputData = MultiLingualResourcesConfig.rcv1DataProcessed;
		RCV1OriginalCorpusConceptData ngData = new RCV1OriginalCorpusConceptData();
		Random random = new Random();
		ngData.readCorpusContentOnly(inputData, random, 0.5);
		
		writeCorpusEmbeddingData(ngData.getCorpusContentMap(), conceptNum, outputData, embeddingSource);
	}
	
	public static void writeCorpusEmbeddingData (HashMap<String, String> corpusContentMap, int numConcepts, String file, String embeddingSource) {
		String content = "";
		try {
			DatalessResourcesConfig.embeddingDimension = 300;
			DatalessResourcesConfig.memorybasedW2V = MultiLingualResourcesConfig.musePath + "wiki.multi.en.vec";
			if (embeddingSource.equals("babylon")) {
				DatalessResourcesConfig.memorybasedW2V = MultiLingualResourcesConfig.babylonPath + "wiki.en.vec";
			}
			MemoryBasedWordEmbedding lg_embedding = new MemoryBasedWordEmbedding();
			
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
	
	public static void rcvDumpTreeData (int id, String outputFolder, String embeddingSource) {
		
		DatalessResourcesConfig.embeddingDimension = 300;
		DatalessResourcesConfig.memorybasedW2V = MultiLingualResourcesConfig.musePath + "wiki.multi.en.vec";
		if (embeddingSource.equals("babylon")) {
			DatalessResourcesConfig.memorybasedW2V = MultiLingualResourcesConfig.babylonPath + "wiki.en.vec";
		}
		MemoryBasedWordEmbedding en_embedding = new MemoryBasedWordEmbedding();

		String lgName = LanguageMapping.lgNames[id];
		
		int conceptNum = 300;
		String outputData = outputFolder + lgName + ".tree.rcv1.embedding.muse." + conceptNum;

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
	
}
