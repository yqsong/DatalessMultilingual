package edu.illinois.cs.cogcomp.classification.flat.ted.dataless;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.illinois.cs.cogcomp.MultiLingualResourcesConfig;
import edu.illinois.cs.cogcomp.classification.densification.representation.DenseVector;
import edu.illinois.cs.cogcomp.classification.densification.representation.SparseSimilarityCondensation;
import edu.illinois.cs.cogcomp.classification.flat.ted.data.TEDData;
import edu.illinois.cs.cogcomp.classification.flat.ted.data.TEDDataSplitting;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.ConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;
import edu.illinois.cs.cogcomp.classification.main.DatalessResourcesConfig;
import edu.illinois.cs.cogcomp.classification.representation.esa.AbstractESA;
import edu.illinois.cs.cogcomp.classification.representation.esa.complex.MemoryBasedESA;
import edu.illinois.cs.cogcomp.classification.representation.esa.simple.SimpleESALocal;
import edu.illinois.cs.cogcomp.classification.representation.word2vec.MemoryBasedWordEmbedding;
import edu.illinois.cs.cogcomp.classification.representation.word2vec.WordEmbeddingInterface;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.descartes.retrieval.simple.Searcher;
import edu.illinois.cs.cogcomp.descartes.util.IOManager;
import edu.illinois.cs.cogcomp.embedding.esa.index.CrossLanguageWikipediaDocumentIndex;
import edu.illinois.cs.cogcomp.embedding.esa.multiesa.MultiLingalESA;

public class TEDDatalessClassificationPreprocessing {


	
	public static int conceptNum = 500;
	public static AbstractESA esa_en;
	public static AbstractESA esa_lg;
	public static HashMap<String, Double> conceptWeights = new HashMap<String, Double>();
	
	public static void main (String[] args) {
//		String docFile = "C:\\data\\ted-cldc\\fr-en\\train\\business\\positive\\5.ted";
		DatalessResourcesConfig.initialization();
		MultiLingualResourcesConfig.initialization();
	

		int fold = 4;
		int selected = 1;
		try {
			for (int i = 0; i < TEDData.lgNameArray.length - 1; ++i) {
				int lgID = i;
				String outputFolder = "/shared/shelley/yqsong/data/tedcldc/output.5.3.0-densification-sim/";
				String lgName = TEDData.lgNameArray[lgID];
				if (lgName.equals("en")) {
					outputFolder = "/shared/shelley/yqsong/data/tedcldc/output.enlish.memoryESA_dense/";
				}
				if (lgID % fold == selected) {
					List<Double> f1List = new ArrayList<Double>();
					for (int j = 0; j < TEDData.labelNameArray.length; ++j) {
						double f1 = testOneLabel (lgID, j, false, outputFolder);
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
		
//		try {
//			int lgID = 12;
//			int startCate = 0;
//			List<Double> f1List = new ArrayList<Double>();
//			for (int j = startCate; j < TEDData.labelNameArray.length; ++j) {
//				double f1 = testOneLabel (lgID, j, false);
//				f1List.add(f1);
//			}
//			
//			for (int j = startCate; j < TEDData.labelNameArray.length; ++j) {
//				System.out.println("[Summary] " + "\t" + TEDData.lgNameArray[lgID] + "\t" + TEDData.labelNameArray[j] + "\t" + f1List.get(j));
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
	}
	
	static SparseSimilarityCondensation densification = null;
	static WordEmbeddingInterface embedding = null;
	
	public static List<String> loadTitles (String contentIndexPath) throws Exception {
		
		Directory lgdir = FSDirectory.open(new File(contentIndexPath).toPath());
		DirectoryReader lgReader = DirectoryReader.open(lgdir);
		int maxLgNum = lgReader.maxDoc();

		HashSet<String> wikiTitles = new HashSet<String>();
		for(int i = 0; i < maxLgNum; i++){
		    	
			String wikiTitle = "";
			try {
				
				wikiTitle = lgReader.document(i).getField(CrossLanguageWikipediaDocumentIndex.englishTitle).stringValue();
	    		
	    		wikiTitles.add(wikiTitle);
		    }
		    catch(Exception e){
	    		e.printStackTrace();
	    		continue;
	    	}
		}
		return new ArrayList<String>(wikiTitles);
	}
	
	public static double testOneLabel (int lgID, int labelID, boolean isUseDensificaiton, String outputFolder) throws Exception {
		
		String lgName = TEDData.lgNameArray[lgID];
		String label = TEDData.labelNameArray[labelID];
		
		System.out.println("---------------------------------------------------------------------");
		System.out.println("Language: " + lgName + ", label: " + label);
		
		String upFolder = MultiLingualResourcesConfig.tedcldcPath;
		String trainPathPos = upFolder + lgName + "-en" + "/train/" + label + "/positive/";
		String trainPathNeg = upFolder + lgName + "-en" + "/train/" + label + "/negative/";
		String testPathPos = upFolder + lgName + "-en" + "/test/" + label + "/positive/";
		String testPathNeg = upFolder + lgName + "-en" + "/test/" + label + "/negative/";
		
		String lgIndexFolder = MultiLingualResourcesConfig.multiLingualESAFolder;
		String indexFile = lgIndexFolder + lgName + MultiLingualResourcesConfig.multiLingualESASuffix;

		String pbAlter = "br"; //pt or br
		if (lgName.equals("pb")) {
			indexFile = lgIndexFolder + pbAlter + MultiLingualResourcesConfig.multiLingualESASuffix;
		}
		
		if (lgName.equals("en")) {
			
			trainPathPos = upFolder + "en-es"  + "/train/" + label + "/positive/";
			trainPathNeg = upFolder + "en-es"  + "/train/" + label + "/negative/";
			testPathPos = upFolder + "en-es"  + "/test/" + label + "/positive/";
			testPathNeg = upFolder + "en-es"  + "/test/" + label + "/negative/";
			
			indexFile = MultiLingualResourcesConfig.englishESA530IndexFolder;
			esa_en = new MemoryBasedESA();
			esa_lg = esa_en;
		} else {
//			esa = new MultiLingalESA(lgName, indexFile);
			esa_en = new MultiLingalESA("standard", new String[] {CrossLanguageWikipediaDocumentIndex.englishField,
					CrossLanguageWikipediaDocumentIndex.englishTitle}, indexFile);
			esa_lg = new MultiLingalESA(lgName, new String[] {CrossLanguageWikipediaDocumentIndex.languageField,
					CrossLanguageWikipediaDocumentIndex.languageTitle}, indexFile);
		}
		
		if (isUseDensificaiton) {
			List<String> titleList = loadTitles (indexFile);
			if (lgName.equals("en")) {
				if (densification == null) {
					if (embedding == null) {
						embedding = new MemoryBasedWordEmbedding();
					}
					densification = new SparseSimilarityCondensation (SparseSimilarityCondensation.matchingTypes[1], 0.85, 0.03, titleList, embedding);
				}
			} else {
				if (embedding == null) {
					embedding = new MemoryBasedWordEmbedding();
				}
				densification = new SparseSimilarityCondensation (SparseSimilarityCondensation.matchingTypes[1], 0.85, 0.03, titleList, embedding);
			}

		}


		TEDDataSplitting dataSplit = new TEDDataSplitting();
		dataSplit.loadOneLanguageOneLabel(lgName, trainPathPos, trainPathNeg, testPathPos, testPathNeg, true);
		
		SparseVector labelVec = getESAVector(label, esa_en);
		
		if (lgName.equals("pb")) {
			lgName = "pb-" + pbAlter;
		}
		FileWriter writerTraining = new FileWriter(outputFolder + lgName + "_" + label + "_training.txt");
		List<Double> trainScoreList = new ArrayList<Double>();
		for (int i = 0; i < dataSplit.labeledTrainData.size(); ++i) {
			String text = dataSplit.labeledTrainData.get(i).getFirst();
			SparseVector textVec = getESAVector(text, esa_lg);
			
			double value = 0;
			if (isUseDensificaiton) {
				value = densification.similarityWithMaxMatching(textVec.getData(), labelVec.getData(), textVec.getNorm(), labelVec.getNorm());
			} else {
				value = labelVec.cosine(textVec, conceptWeights);
			}
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
			SparseVector textVec = getESAVector(text, esa_lg);
			double value = 0;
			if (isUseDensificaiton) {
				value = densification.similarityWithMaxMatching(textVec.getData(), labelVec.getData(), textVec.getNorm(), labelVec.getNorm());
			} else {
				value = labelVec.cosine(textVec, conceptWeights);
			}
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
	

	
	public static SparseVector getESAVector (String text, AbstractESA esa) {
		List<ConceptData> concepts = null;
		List<String> conceptsList = new ArrayList<String>();
		List<Double> scores = new ArrayList<Double>();

		try {
			concepts = esa.retrieveConcepts(text, conceptNum);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (concepts != null) {
			for (int i = 0; i < concepts.size(); i++) {
				conceptsList.add(concepts.get(i).concept + "");
				scores.add(concepts.get(i).score);
			}
		}
		SparseVector vec = new SparseVector(conceptsList, scores, ClassifierConstant.isBreakConcepts, conceptWeights);
		return vec;
	}
	

	
	
	
}
