package edu.illinois.cs.cogcomp.embedding.multiembedding;

import java.io.FileWriter;

import edu.illinois.cs.cogcomp.MultiLingualResourcesConfig;
import edu.illinois.cs.cogcomp.classification.flat.ted.data.TEDData;
import edu.illinois.cs.cogcomp.classification.main.DatalessResourcesConfig;
import edu.illinois.cs.cogcomp.classification.representation.word2vec.MemoryBasedWordEmbedding;
import edu.illinois.cs.cogcomp.classification.representation.word2vec.WordEmbeddingInterface;

public class ProcessTEDEmbedding {

	public static void main (String[] args) {
		
		MultiLingualResourcesConfig.initialization();
		
		for (int i = 6; i < 7; ++i) {
			
			String lgName = TEDData.lgNameArray[i];
			
			DatalessResourcesConfig.embeddingDimension = 128;
			String lg = lgName;
			DatalessResourcesConfig.memorybasedW2V = MultiLingualResourcesConfig.bicvmTEDPath + lg + "-en.txt";
			MemoryBasedWordEmbedding lg_embedding = new MemoryBasedWordEmbedding();
			DatalessResourcesConfig.memorybasedW2V = MultiLingualResourcesConfig.bicvmTEDPath + "en" + "-" + lg + ".txt";
			MemoryBasedWordEmbedding en_embedding = new MemoryBasedWordEmbedding();
			
			String outputEn = MultiLingualResourcesConfig.bicvmTEDPath + "en" + "-" + lg + "-new.txt";
			try {
				System.out.println("write en");
				FileWriter writerEn = new FileWriter(outputEn);
				for (String word : en_embedding.vectors.keySet()) {
					String cleanWord = word.replace("_en", "");
					writerEn.write(cleanWord + " ");
					double[] vec = en_embedding.vectors.get(word);
					for (int j = 0; j < vec.length; ++j) {
						writerEn.write(vec[j] + " ");
					}
					writerEn.write(TEDData.systemNewLine);
				}
				writerEn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			String outputLg = MultiLingualResourcesConfig.bicvmTEDPath + lg + "-en-new.txt";
			try {
				System.out.println("write " + lg);
				FileWriter writerLg = new FileWriter(outputLg);
				for (String word : lg_embedding.vectors.keySet()) {
					String cleanWord = word.replace("_" + lg, "");
					writerLg.write(cleanWord + " ");
					double[] vec = lg_embedding.vectors.get(word);
					for (int j = 0; j < vec.length; ++j) {
						writerLg.write(vec[j] + " ");
					}
					writerLg.write(TEDData.systemNewLine);
				}
				writerLg.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
			
	}
}
