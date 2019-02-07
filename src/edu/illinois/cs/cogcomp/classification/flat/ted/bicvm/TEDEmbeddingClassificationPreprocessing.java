package edu.illinois.cs.cogcomp.classification.flat.ted.bicvm;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.illinois.cs.cogcomp.MultiLingualResourcesConfig;
import edu.illinois.cs.cogcomp.classification.densification.representation.DenseVector;
import edu.illinois.cs.cogcomp.classification.flat.ted.data.TEDData;
import edu.illinois.cs.cogcomp.classification.flat.ted.data.TEDDataSplitting;
import edu.illinois.cs.cogcomp.classification.main.DatalessResourcesConfig;
import edu.illinois.cs.cogcomp.classification.representation.word2vec.MemoryBasedWordEmbedding;
import edu.illinois.cs.cogcomp.classification.representation.word2vec.WordEmbeddingInterface;
import edu.illinois.cs.cogcomp.embedding.multiembedding.EmbeddingLanguages;

public class TEDEmbeddingClassificationPreprocessing {

	public static WordEmbeddingInterface en_embedding = null;
	public static WordEmbeddingInterface lg_embedding = null;

	public static void main (String[] args) {
		DatalessResourcesConfig.initialization();
		MultiLingualResourcesConfig.initialization();
	
//		String resource = "europarl";
//		HashMap<String, String> availability = EmbeddingLanguages.europarl_availability;
		
		String resource = "ted";
		HashMap<String, String> availability = EmbeddingLanguages.ted_availability;
		
		String outputFolder = "";
		if (resource.equals("ted")) {
			outputFolder = "/shared/shelley/yqsong/data/tedcldc/output.embedding-ted-sim/";
		} else {
			outputFolder = "/shared/shelley/yqsong/data/tedcldc/output.embedding-euro-sim/";
		}
		
		int fold = 1;
		int selected = 0;
		try {
			for (int i = 0; i < TEDData.lgNameArray.length; ++i) 
			{
				int lgID = i;
				
				String lgName = TEDData.lgNameArray[i];
				
				if (availability.containsKey(lgName) == false && lgName.equals("en") == false)
					continue;
				
				if (lgID % fold == selected) {
					List<Double> f1List = new ArrayList<Double>();
					for (int j = 0; j < TEDData.labelNameArray.length; ++j) {
						double f1 = testOneLabel (lgID, j, false, resource, outputFolder);
						f1List.add(f1);
					}
					
					for (int j = 0; j < TEDData.labelNameArray.length; ++j) {
						System.out.println("[Summary] " + "\t" + TEDData.lgNameArray[lgID] + "\t" + TEDData.labelNameArray[j] + "\t" + f1List.get(j));
					}
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	

	public static double testOneLabel (int lgID, int labelID, boolean isUseDensificaiton, String embeddingSource, String outputFolder) throws Exception {
		
		
		String lgName = TEDData.lgNameArray[lgID];
		String label = TEDData.labelNameArray[labelID];
		
		System.out.println("---------------------------------------------------------------------");
		System.out.println("Language: " + lgName + ", label: " + label);
		
		String upFolder = MultiLingualResourcesConfig.tedcldcPath; // "/shared/shelley/yqsong/data/tedcldc/ted-cldc/";
		String trainPathPos = upFolder + lgName + "-en" + "/train/" + label + "/positive/";
		String trainPathNeg = upFolder + lgName + "-en" + "/train/" + label + "/negative/";
		String testPathPos = upFolder + lgName + "-en" + "/test/" + label + "/positive/";
		String testPathNeg = upFolder + lgName + "-en" + "/test/" + label + "/negative/";

		if (lgName.equals("en")) {
			
			trainPathPos = upFolder + "en-es"  + "/train/" + label + "/positive/";
			trainPathNeg = upFolder + "en-es"  + "/train/" + label + "/negative/";
			testPathPos = upFolder + "en-es"  + "/test/" + label + "/positive/";
			testPathNeg = upFolder + "en-es"  + "/test/" + label + "/negative/";
			
			DatalessResourcesConfig.embeddingDimension = MultiLingualResourcesConfig.word2vecDim;
			DatalessResourcesConfig.memorybasedW2V = MultiLingualResourcesConfig.word2vecPath;
			en_embedding = new MemoryBasedWordEmbedding();
			lg_embedding = en_embedding;
		} else {
			DatalessResourcesConfig.embeddingDimension = 128;
			String lg = lgName;
			
			if (embeddingSource.equals("ted")) {
				DatalessResourcesConfig.memorybasedW2V = MultiLingualResourcesConfig.bicvmTEDPath + lg + "-en-new.txt";
				lg_embedding = new MemoryBasedWordEmbedding();
				DatalessResourcesConfig.memorybasedW2V = MultiLingualResourcesConfig.bicvmTEDPath + "en" + "-" + lg + "-new.txt";
				en_embedding = new MemoryBasedWordEmbedding();
			}

			if (embeddingSource.equals("europarl")) {
				if (lgName.equals("pb")) {
					lg = "pt";
				}
				DatalessResourcesConfig.memorybasedW2V = MultiLingualResourcesConfig.bicvmEuroparlPath + lg + "-en.txt";
				lg_embedding = new MemoryBasedWordEmbedding();
				DatalessResourcesConfig.memorybasedW2V = MultiLingualResourcesConfig.bicvmEuroparlPath + "en" + "-" + lg + ".txt";
				en_embedding = new MemoryBasedWordEmbedding();
			}

		}
		
		TEDDataSplitting dataSplit = new TEDDataSplitting();
		dataSplit.loadOneLanguageOneLabel(lgName, trainPathPos, trainPathNeg, testPathPos, testPathNeg, true);
		
		DenseVector labelVec = getEmbeddingVector(label, en_embedding);
		
		FileWriter writerTraining = new FileWriter(outputFolder + lgName + "_" + label + "_training.txt");
		List<Double> trainScoreList = new ArrayList<Double>();
		for (int i = 0; i < dataSplit.labeledTrainData.size(); ++i) {
			String text = dataSplit.labeledTrainData.get(i).getFirst();
			DenseVector textVec = getEmbeddingVector(text, lg_embedding);
			
			double value = labelVec.cosine(textVec);
			
			trainScoreList.add(value);
			if (i % 10 == 0) {
				System.out.println("  [Training Data] processed training " + i);
			}
			writerTraining.write(dataSplit.labeledTrainIDs.get(i) + "\t" + value + "\t" + dataSplit.labeledTrainData.get(i).getSecond() + TEDData.systemNewLine);
		}
		writerTraining.flush();
		writerTraining.close();
		
		double bestScore = 0;
		double bestThreshold = 0;
		for (double threshold = 0.0; threshold < 1.0; threshold += 0.0001) {
			List<Boolean> goldList = new ArrayList<Boolean>();
			for (int i = 0; i < dataSplit.labeledTrainData.size(); ++i) {
				goldList.add(dataSplit.labeledTrainData.get(i).getSecond());
			}
			List<Boolean> predList = new ArrayList<Boolean>();
			for (int i = 0; i < dataSplit.labeledTrainData.size(); ++i) {
				if (trainScoreList.get(i) > threshold) {
					predList.add(true);
				} else {
					predList.add(false);
				}
			}
			double f1 = TEDData.computePositiveF1 (goldList, predList);
			if (bestScore < f1) {
				bestScore = f1;
				bestThreshold = threshold;
			}
			System.out.println("[Training Threshold]  " + threshold + ", F1: " + f1);
		}
		
		FileWriter writerTesting = new FileWriter(outputFolder + lgName + "_" + label + "_test.txt");
		List<Double> testScoreList = new ArrayList<Double>();
		List<Boolean> goldList = new ArrayList<Boolean>();
		for (int i = 0; i < dataSplit.labeledTestData.size(); ++i) {
			goldList.add(dataSplit.labeledTestData.get(i).getSecond());
		}
		List<Boolean> predList = new ArrayList<Boolean>();
		for (int i = 0; i < dataSplit.labeledTestData.size(); ++i) {
			String text = dataSplit.labeledTestData.get(i).getFirst();
			DenseVector textVec = getEmbeddingVector(text, lg_embedding);
			
			double value = labelVec.cosine(textVec);
			testScoreList.add(value);
			if (value > bestThreshold) {
				predList.add(true);
			} else {
				predList.add(false);
			}
			if (i % 10 == 0) {
				System.out.println("  [Test Data] processed test " + i);
			}
			writerTesting.write(dataSplit.labeledTestIDs.get(i) + "\t" + value + "\t" + dataSplit.labeledTestData.get(i).getSecond() + TEDData.systemNewLine);
		}
		writerTesting.flush();
		writerTesting.close();
		double f1 = TEDData.computePositiveF1 (goldList, predList);
		System.out.println("[Testing ] " + "Language: " + lgName + ", label: " + label + ", F1: " + f1);
		
		return f1;
		
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
