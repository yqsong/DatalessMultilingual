package edu.illinois.cs.cogcomp.classification.newsgroups.muse;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.illinois.cs.cogcomp.MultiLingualResourcesConfig;
import edu.illinois.cs.cogcomp.classification.densification.representation.DenseVector;
import edu.illinois.cs.cogcomp.classification.flat.ted.data.TEDData;
import edu.illinois.cs.cogcomp.classification.flat.ted.data.TEDDataSplitting;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ConceptTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml.ConceptTreeTopDownML;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.ConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;
import edu.illinois.cs.cogcomp.classification.main.DatalessResourcesConfig;
import edu.illinois.cs.cogcomp.classification.newsgroups.NG20ClassificationWithMergedIndex;
import edu.illinois.cs.cogcomp.classification.newsgroups.NG20ClassificationWithOriginalIndex;
import edu.illinois.cs.cogcomp.classification.newsgroups.data.NGData;
import edu.illinois.cs.cogcomp.classification.representation.word2vec.MemoryBasedWordEmbedding;
import edu.illinois.cs.cogcomp.classification.representation.word2vec.WordEmbeddingInterface;
import edu.illinois.cs.cogcomp.descartes.util.IOManager;
import edu.illinois.cs.cogcomp.embedding.esa.multiesa.MultiLingalESA;
import edu.illinois.cs.cogcomp.embedding.multiembedding.EmbeddingLanguages;

public class NG20EmbeddingClassification {

	public static WordEmbeddingInterface en_embedding = null;
	public static WordEmbeddingInterface lg_embedding = null;

	public static void main (String[] args) {
		
		HashMap<String, Double> precisionMap = new HashMap<String, Double>();

		DatalessResourcesConfig.initialization();
		MultiLingualResourcesConfig.initialization();
		
		DatalessResourcesConfig.memorybasedW2V = MultiLingualResourcesConfig.musePath + "wiki.en.vec";
		File enfile = new File (DatalessResourcesConfig.memorybasedW2V);
		en_embedding = new MemoryBasedWordEmbedding();
		
		DatalessResourcesConfig.LabelfilePath = "/home/data/corpora/20-newsgroups/multilingual-20ng/label.en";
		List<String> labelsInEn = IOManager.readLines(DatalessResourcesConfig.LabelfilePath);
		HashMap<String, String> labelMapEn = new HashMap<String, String>();
		HashMap<String, DenseVector> labelVectorMap = new HashMap<String, DenseVector>();
		for (int i = 0; i < labelsInEn.size(); ++i) {
			labelMapEn.put(labelsInEn.get(i).trim(), NGData.NG20Labels[i]);
			DenseVector labelVec = getEmbeddingVector(labelsInEn.get(i).trim(), en_embedding);
			labelVectorMap.put(NGData.NG20Labels[i], labelVec);
			
		}
		
		File folder = new File(MultiLingualResourcesConfig.multiLingual20NGFolder);
		File[] fileList = folder.listFiles();
		for (int fi = 0; fi < fileList.length; ++fi) {
			File subFolder = fileList[fi];
			
			if (subFolder.isDirectory()) {
				System.out.println("------File------" + subFolder.getAbsolutePath());

				NGData data = NGData.readData(subFolder.getAbsolutePath());
				
				String lg = data.lgName;

				if (lg.equals("en")) {
					lg_embedding = en_embedding;
				} else {
					DatalessResourcesConfig.memorybasedW2V = MultiLingualResourcesConfig.musePath + "wiki." + lg + ".vec";
					File lgfile = new File (DatalessResourcesConfig.memorybasedW2V);
					if (lgfile.exists() == false)
						continue;
					lg_embedding = new MemoryBasedWordEmbedding();
				}

//				DatalessResourcesConfig.LabelfilePath = data.lgLabelPath;
//				List<String> labelsInLang = data.labels;
//				HashMap<String, String> labelMap = new HashMap<String, String>();
//				if (lg.equals("en")) {
//					for (int i = 0; i < labelsInEn.size(); ++i) {
//						labelMap.put(labelsInEn.get(i).trim(), NGData.NG20Labels[i]);
//					}
//				} else {
//					for (int i = 0; i < labelsInLang.size(); ++i) {
//						labelMap.put(labelsInLang.get(i).trim(), NGData.NG20Labels[i]);
//					}
//				}

				double correct = 0;
				
				for (int j = 0; j < data.labeledContent.size(); ++j) {
					String label = data.labeledContent.get(j).getFirst();
					String content = data.labeledContent.get(j).getSecond();
					DenseVector dataVec = getEmbeddingVector(content, lg_embedding);
					
					String pred = "";
					double maxCos = 0;
					for (String labelKey : labelVectorMap.keySet()) {
						DenseVector labelVector = labelVectorMap.get(labelKey);
						double cos = labelVector.cosine(dataVec);
						if (cos > maxCos) {
							pred = labelKey;
							maxCos = cos;
						}
					}
					
					if (pred.equals(label))
						correct += 1;
					
				}
				
				double precision = correct / data.labeledContent.size();
				precisionMap.put(lg, precision);
				System.out.println("[Embedding]\t" + data.lgName + "\t" + precision);
				System.out.println();
				
			}
		}
		
		System.out.println("-------------------Embedding--------------------");
		for (String lgName : precisionMap.keySet()){
			System.out.println(lgName + "\t" + precisionMap.get(lgName));
		}
	}
	

	
	public static DenseVector getEmbeddingVector (String text, WordEmbeddingInterface embedding) {
		double[] vector = null;
		try {
			vector = embedding.getDenseVectorSimpleAverage(text);
		} catch (Exception e) {
			e.printStackTrace();
		}
		DenseVector vec = new DenseVector(vector);
		return vec;
	}
	

	
	
	
}
