package edu.illinois.cs.cogcomp.classification.lowresource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.lucene.util.Version;

import edu.illinois.cs.cogcomp.MultiLingualResourcesConfig;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ConceptTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml.ConceptTreeTopDownML;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.ConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;
import edu.illinois.cs.cogcomp.classification.main.DatalessResourcesConfig;
import edu.illinois.cs.cogcomp.classification.newsgroups.data.NGData;
import edu.illinois.cs.cogcomp.classification.representation.esa.AbstractESA;
import edu.illinois.cs.cogcomp.classification.representation.esa.simple.SimpleESALocal;
import edu.illinois.cs.cogcomp.descartes.AnalyzerFactory;
import edu.illinois.cs.cogcomp.descartes.util.IOManager;
import edu.illinois.cs.cogcomp.descartes.util.Utilities;
import edu.illinois.cs.cogcomp.embedding.esa.index.CrossLanguageWikipediaDocumentIndex;
import edu.illinois.cs.cogcomp.embedding.esa.multiesa.MultiLingalESA;

public class TranslatedNG20ClassificationWithMergedIndex {
	
	public static HashMap<String, Double> conceptWeights = new HashMap<String, Double>();
	
	
	public static void main (String[] args) {
	
	}
	
	public static double test (NGData data, int topK, HashMap<String, HashSet<String>> dictionary) {
		if (MultiLingualResourcesConfig.isInitialized == false)
			MultiLingualResourcesConfig.initialization();
		
		String lgName = data.lgName;
		String indexFolderName = "";
		if (lgName.equals("en")) {
			indexFolderName = MultiLingualResourcesConfig.englishESA530IndexFolder;
		} else {
			indexFolderName = MultiLingualResourcesConfig.multiLingualESAFolder + lgName + MultiLingualResourcesConfig.multiLingualESASuffix;
		}
		
		DatalessResourcesConfig.LabelfilePath = "/home/data/corpora/20-newsgroups/multilingual-20ng/label.en";
	
		List<String> labelsInLang = IOManager.readLines(DatalessResourcesConfig.LabelfilePath);
		HashMap<String, String> labelMap = new HashMap<String, String>();
		for (int i = 0; i < labelsInLang.size(); ++i) {
			labelMap.put(labelsInLang.get(i).trim(), NGData.NG20Labels[i]);
		}
		
		DatalessResourcesConfig.level = 1;
		int conceptNum = 500;
		HashMap<String, Double> conceptWeights = new HashMap<String, Double>();
		ConceptTreeTopDownML tree = new ConceptTreeTopDownML(DatalessResourcesConfig.CONST_DATA_CUSTOMIZED, "null", conceptWeights, true);
		tree.setDebug(false);
		tree.esa = new MultiLingalESA(lgName, indexFolderName);

//				new MultiLingalESA("standard", new String[] {CrossLanguageWikipediaDocumentIndex.englishField,
//				CrossLanguageWikipediaDocumentIndex.englishTitle}, indexFolderName);
		System.out.println("process tree...");
		tree.treeLabelData.readTreeHierarchy("");
		ConceptTreeNode rootNode = tree.initializeTree("root", 0);
		tree.setRootNode(rootNode);
		tree.aggregateChildrenDescription(rootNode);
		tree.setConceptNum(conceptNum);
		tree.conceptualizeTreeLabels(rootNode, ClassifierConstant.isBreakConcepts);
		
		AbstractESA esa = tree.esa;
		
		double correct = 0;
		for (int j = 0; j < data.labeledContent.size(); ++j) {
			String label = data.labeledContent.get(j).getFirst();
			String content = data.labeledContent.get(j).getSecond();
			
			content = NGData.extendContent(content, dictionary);
			
			List<ConceptData> concepts = null;
			List<String> conceptsList = new ArrayList<String>();
			List<Double> scores = new ArrayList<Double>();

			try {
				concepts = esa.retrieveConcepts(content, conceptNum);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (concepts != null) {
				for (int i = 0; i < concepts.size(); i++) {
					conceptsList.add(concepts.get(i).concept + "");
					scores.add(concepts.get(i).score);
				}
			}
			
			SparseVector document = new SparseVector(conceptsList, scores, ClassifierConstant.isBreakConcepts, conceptWeights);
			HashMap<Integer, List<LabelKeyValuePair>> labelResultsInDepth = tree.labelDocumentML(document);
			
			int depth = 1;
			boolean isHit = false;
			List<LabelKeyValuePair> kvp = labelResultsInDepth.get(depth);
			if (kvp != null) {
				for (int i = 0; i < Math.min(topK, kvp.size()); ++i) {
					String labelPred = labelMap.get(kvp.get(i).getLabel().trim());
					if (label.equals(labelPred))
						isHit = true;
				}
			}
			if (isHit)
				correct += 1;
		}
		double precision = correct / data.labeledContent.size();
		return precision;

	}

	public static List<String[]> annotate (NGData data, int topK, HashMap<String, HashSet<String>> dictionary) {
		if (MultiLingualResourcesConfig.isInitialized == false)
			MultiLingualResourcesConfig.initialization();
		
		String lgName = data.lgName;
		String indexFolderName = "";
		if (lgName.equals("en")) {
			indexFolderName = MultiLingualResourcesConfig.englishESA530IndexFolder;
		} else {
			indexFolderName = MultiLingualResourcesConfig.multiLingualESAFolder + lgName + MultiLingualResourcesConfig.multiLingualESASuffix;
		}
		
		DatalessResourcesConfig.LabelfilePath = "/shared/bronte/hpeng7/multilingual-embedding/label.en";
		
		List<String> labelsInLang = IOManager.readLines(DatalessResourcesConfig.LabelfilePath);
		HashMap<String, String> labelMap = new HashMap<String, String>();
		for (int i = 0; i < labelsInLang.size(); ++i) {
			labelMap.put(labelsInLang.get(i).trim(), NGData.NG20Labels[i]);
		}
		
		DatalessResourcesConfig.level = 1;
		int conceptNum = 500;
		HashMap<String, Double> conceptWeights = new HashMap<String, Double>();
		ConceptTreeTopDownML tree = new ConceptTreeTopDownML(DatalessResourcesConfig.CONST_DATA_CUSTOMIZED, "null", conceptWeights, true);
		tree.setDebug(false);
		tree.esa = new MultiLingalESA(lgName, indexFolderName);

//				new MultiLingalESA("standard", new String[] {CrossLanguageWikipediaDocumentIndex.englishField,
//				CrossLanguageWikipediaDocumentIndex.englishTitle}, indexFolderName);
		System.out.println("process tree...");
		tree.treeLabelData.readTreeHierarchy("");
		ConceptTreeNode rootNode = tree.initializeTree("root", 0);
		tree.setRootNode(rootNode);
		tree.aggregateChildrenDescription(rootNode);
		tree.setConceptNum(conceptNum);
		tree.conceptualizeTreeLabels(rootNode, ClassifierConstant.isBreakConcepts);
		
		AbstractESA esa = tree.esa;
		
		List<String[]> resultList = new ArrayList<String[]>();
		for (int j = 0; j < data.labeledContent.size(); ++j) {
			String[] results = new String[3];
			String id = data.ids.get(j);
			String label = data.labeledContent.get(j).getFirst();
			String content = data.labeledContent.get(j).getSecond();
			
			results[0] = id;
			results[1] = label;
			
			content = NGData.extendContent(content, dictionary);
			
			List<ConceptData> concepts = null;
			List<String> conceptsList = new ArrayList<String>();
			List<Double> scores = new ArrayList<Double>();

			try {
				concepts = esa.retrieveConcepts(content, conceptNum);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (concepts != null) {
				for (int i = 0; i < concepts.size(); i++) {
					conceptsList.add(concepts.get(i).concept + "");
					scores.add(concepts.get(i).score);
				}
			}
			
			SparseVector document = new SparseVector(conceptsList, scores, ClassifierConstant.isBreakConcepts, conceptWeights);
			HashMap<Integer, List<LabelKeyValuePair>> labelResultsInDepth = tree.labelDocumentML(document);
			
			int depth = 1;
			List<LabelKeyValuePair> kvp = labelResultsInDepth.get(depth);
			if (kvp != null) {
				String labelPred = labelMap.get(kvp.get(0).getLabel().trim());
				results[2] = labelPred;
			}
			
			resultList.add(results);
		}
		return resultList;

	}
}
