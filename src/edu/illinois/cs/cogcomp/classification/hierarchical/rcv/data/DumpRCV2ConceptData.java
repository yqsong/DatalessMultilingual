package edu.illinois.cs.cogcomp.classification.hierarchical.rcv.data;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.lucene.analysis.Analyzer;

import edu.illinois.cs.cogcomp.MultiLingualResourcesConfig;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.AbstractConceptTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ConceptTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml.ConceptTreeTopDownML;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.rcv.RCVCorpusConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.ConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;
import edu.illinois.cs.cogcomp.classification.main.DatalessResourcesConfig;
import edu.illinois.cs.cogcomp.classification.representation.esa.AbstractESA;
import edu.illinois.cs.cogcomp.embedding.esa.index.AnalyzerFactory;
import edu.illinois.cs.cogcomp.embedding.esa.index.CrossLanguageWikipediaDocumentIndex;
import edu.illinois.cs.cogcomp.embedding.esa.index.ReadLangLinkingFile;
import edu.illinois.cs.cogcomp.embedding.esa.multiesa.MultiLingalESA;

public class DumpRCV2ConceptData {


	
	public static void main (String[] args) {
		MultiLingualResourcesConfig.initialization();
		String outputFolder = "/shared/shelley/yqsong/data/rcv2/output/preprocessed_new/";
		dumpCorpusConcepts (12, outputFolder);
		
//		for (int i = 0 ; i < LanguageMapping.lgNames.length; ++i) {
//			System.out.println(LanguageMapping.lgNames[i]);
////			rcvDumpTreeData(i, outputFolder);
//			dumpLabelData (i, outputFolder);
//		}
	}
	

	
	
	public static void dumpCorpusConcepts (int id, String outputFolder) 	{
		String lgName = LanguageMapping.lgNames[id];
		
		int conceptNum = 500;
		String outputData = outputFolder + lgName + ".rcv2.esa.concepts." + conceptNum;
		
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
		writeCorpusSimpleConceptData(contentDataMap, conceptNum, outputData, LanguageMapping.lgMapping.get(lgName));
	}
	
	public static void writeCorpusSimpleConceptData (HashMap<String, String> corpusContentMap, int numConcepts, String file, String lgName) {
		String lgIndexFolder = MultiLingualResourcesConfig.multiLingualESAFolder;
		String indexFile = lgIndexFolder + lgName + MultiLingualResourcesConfig.multiLingualESASuffix;
		
		String content = "";
		try {
			MultiLingalESA esa_lg = new MultiLingalESA(lgName, new String[] {CrossLanguageWikipediaDocumentIndex.languageField,
					CrossLanguageWikipediaDocumentIndex.languageTitle}, indexFile);
			
			int count = 0;
			FileWriter writer = new FileWriter(file);
			for (String docID : corpusContentMap.keySet()) {
				count++;
				System.out.println("written " + count +  " documents with concepts");
				content = corpusContentMap.get(docID);
				List<ConceptData> concepts = esa_lg.retrieveConcepts(content, numConcepts);
				List<String> conceptsList = new ArrayList<String>();
				String docContent = corpusContentMap.get(docID);
				writer.write(docID + "\t" + docContent + "\t");
				for (int i = concepts.size() - 1; i >= 0; i--) {
					writer.write(concepts.get(i).concept + "," + concepts.get(i).score + ";");
				}
				writer.write(ReadLangLinkingFile.systemNewLine);
			}
			writer.close();
		} catch (Exception e) {
			System.out.println(content);
			e.printStackTrace();
		}
			
	}
	
	public static void rcvDumpTreeData (int id, String outputFolder) {
		String lgName = LanguageMapping.lgNames[id];
		
		int conceptNum = 500;
		String outputData = outputFolder + lgName + ".tree.rcv2.esa.concepts." + conceptNum;

		String lgIndexFolder = MultiLingualResourcesConfig.multiLingualESAFolder;
		String indexFile = lgIndexFolder + lgName + MultiLingualResourcesConfig.multiLingualESASuffix;

		AbstractESA esa_en = new MultiLingalESA("standard", new String[] {CrossLanguageWikipediaDocumentIndex.englishField,
				CrossLanguageWikipediaDocumentIndex.englishTitle}, indexFile);

		HashMap<String, Double> conceptWeights = new HashMap<String, Double>();
		String fileTopicHierarchyPath = MultiLingualResourcesConfig.rcv1Hierarchy;
		String fileTopicDescriptionPath = MultiLingualResourcesConfig.rcv1Topics;
		AbstractConceptTree tree = new ConceptTreeTopDownML(DatalessResourcesConfig.CONST_DATA_RCV, "null", conceptWeights, true);
		System.out.println("process tree...");
		tree.esa = esa_en;
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
	
	public static void dumpLabelData (int id, String outputFolder) {
		String lgName = LanguageMapping.lgNames[id];
		
		String outputData = outputFolder + lgName + ".rcv2.labels";
		
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
