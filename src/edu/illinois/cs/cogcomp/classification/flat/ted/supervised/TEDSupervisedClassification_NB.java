package edu.illinois.cs.cogcomp.classification.flat.ted.supervised;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import LBJ2.classify.DiscretePrimitiveStringFeature;
import LBJ2.classify.FeatureVector;
import LBJ2.classify.Score;
import LBJ2.classify.ScoreSet;
import LBJ2.learn.Lexicon;
import edu.illinois.cs.cogcomp.MultiLingualResourcesConfig;
import edu.illinois.cs.cogcomp.classification.flat.ted.data.TEDData;
import edu.illinois.cs.cogcomp.classification.flat.ted.data.TEDDataSplitting;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.lbj.test.ConfigurableClassifier;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.jlis.CorpusDataProcessing;

public class TEDSupervisedClassification_NB {

	public static void main (String args[]) {
		MultiLingualResourcesConfig.initialization();
		int fold = 1;
		int selected = 0;
		try {
			for (int i = 0; i < TEDData.lgNameArray.length; ++i) {
				int lgID = i;
				
				if (lgID % fold == selected) {
					List<Double> f1List = new ArrayList<Double>();
					for (int j = 0; j < TEDData.labelNameArray.length; ++j) {
						double f1 = testOneLabelOneLanguage (lgID, j, 10000, 0.01);
						f1List.add(f1);
					}
					
					for (int j = 0; j < TEDData.labelNameArray.length; ++j) {
						System.out.println("[Summary] " + "\t" + TEDData.lgNameArray[lgID] + "\t" + TEDData.labelNameArray[j] + "\t" + f1List.get(j));
					}
				}
				
			}
//			int lgID = 12;
//			for (double C = 100000; C < 100000000; C = C * 10) {
//				List<Double> f1List = new ArrayList<Double>();
//				for (int j = 0; j < TEDData.labelNameArray.length; ++j) {
//					double f1 = testOneLabelOneLanguage (lgID, j, C, 0.0001);
//					f1List.add(f1);
//				}
//				for (int j = 0; j < TEDData.labelNameArray.length; ++j) {
//					System.out.println("[Summary] " + "\t" + TEDData.lgNameArray[lgID] + "\t" + TEDData.labelNameArray[j] + "\t" + f1List.get(j));
//				}
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static double testOneLabelOneLanguage (int lgID, int labelID, double C, double eps) {
		String lgName = TEDData.lgNameArray[lgID];
		String label = TEDData.labelNameArray[labelID];
		
		System.out.println("-----------------------------------C=" + C + "----------------------------------");
		System.out.println("Language: " + lgName + ", label: " + label);
		
		String upFolder = MultiLingualResourcesConfig.tedcldcPath;
		String trainPathPos = upFolder + lgName + "-en" + "/train/" + label + "/positive/";
		String trainPathNeg = upFolder + lgName + "-en" + "/train/" + label + "/negative/";
		String testPathPos = upFolder + lgName + "-en" + "/test/" + label + "/positive/";
		String testPathNeg = upFolder + lgName + "-en" + "/test/" + label + "/negative/";
		
		if (lgName.equals("en")) {
			
			trainPathPos = upFolder + "en-es"  + "/train/" + label + "/positive/";
			trainPathNeg = upFolder + "en-es"  + "/train/" + label + "/negative/";
			testPathPos = upFolder + "en-es"  + "/test/" + label + "/positive/";
			testPathNeg = upFolder + "en-es"  + "/test/" + label + "/negative/";
			
		} 
		
		TEDDataSplitting dataSplit = new TEDDataSplitting();
		dataSplit.loadOneLanguageOneLabel(lgName, trainPathPos, trainPathNeg, testPathPos, testPathNeg, false);

		HashMap<String, String> corpusStringMap = new HashMap<String, String>();
		for (int i = 0; i < dataSplit.labeledTrainIDs.size(); ++i) {
			String id = dataSplit.labeledTrainIDs.get(i);
			String content = dataSplit.labeledTrainData.get(i).getFirst();
			corpusStringMap.put(id, content);
		}
		
		CorpusDataProcessing corpus = new CorpusDataProcessing();
		corpus.startFromZero = true;
		HashMap<String, String> trainingDataLibSVMFormat = corpus.initializeTrainingDocumentFeatures (corpusStringMap, false, true);

		ConfigurableClassifier model = new ConfigurableClassifier("naiveBayes");;
		
		Lexicon featureLexicon = corpus.getGlobalLexicon();
		Lexicon labelLexicon = model.getLabelLexicon();//new Lexicon();
		
		for (int i = 0; i <= 1; ++i) {
			DiscretePrimitiveStringFeature feature = new DiscretePrimitiveStringFeature(
					"traininglabelpackage",
					"CorpusDataProcessing",
					i + "",
					i + "",
					(short) i,
					(short) 2
					);
			labelLexicon.lookup(feature, true);
		}

		List<Object> rawDataList = new ArrayList<Object>();
		List<FeatureVector> vectorList = new ArrayList<FeatureVector>();
		for (int i = 0; i < dataSplit.labeledTrainIDs.size(); ++i) {
			String docID = dataSplit.labeledTrainIDs.get(i);
			boolean lab = dataSplit.labeledTrainData.get(i).getSecond();
			int docLabel = 0;
			if (lab == true)
				docLabel = 1;
			
			// initialize data for libLinear
			String[] tokens = trainingDataLibSVMFormat.get(docID).split(" ");
			
			int[] indexArray = new int[tokens.length];
			double[] valueArray = new double[tokens.length];
			for (int j = 0; j < tokens.length; ++j) {
				String[] subTokens = tokens[j].trim().split(":");
				int index = Integer.parseInt(subTokens[0].trim());
				double value = Double.parseDouble(subTokens[1].trim());
				indexArray[j] = index;
				valueArray[j] = value;
			}
			
			int[] labelIndexArray = new int[1];
			double[] labelValueArray = new double[1];
			labelIndexArray[0] = docLabel;
			labelValueArray[0] = 1;
			
			Object[] dataSample = new Object[4];
			dataSample[0] = indexArray;
			dataSample[1] = valueArray;
			dataSample[2] = labelIndexArray;
			dataSample[3] = labelValueArray;

			FeatureVector vector =
			        new FeatureVector((Object[]) dataSample, featureLexicon, labelLexicon);
			vector.sort();
			
			vectorList.add(vector);
			
			rawDataList.add(dataSample);
		}
		
		Object[] dataArray = new Object[vectorList.size()];
		FeatureVector[] vectorArray = new FeatureVector[vectorList.size()];
		for (int i = 0; i < vectorList.size(); ++i) {
			vectorArray[i] = vectorList.get(i);
			dataArray[i] = rawDataList.get(i);
		}
		
		try {
			 Calendar cal = Calendar.getInstance();
			    long startTime = cal.getTimeInMillis();
				System.out.println("  [Training:] NB for: " + label);
				
				model.learn(dataArray);
				
				Calendar cal1 = Calendar.getInstance();
	    		long endTime = cal1.getTimeInMillis();
	    		long second = (endTime - startTime)/1000;
				System.out.println("  [Training:] finished," + " time: " + second + " seconds");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		List<Boolean> goldList = new ArrayList<Boolean>();
		List<Boolean> predList = new ArrayList<Boolean>();
		for (int i = 0; i < dataSplit.labeledTestIDs.size(); ++i) {
			String docID = dataSplit.labeledTestIDs.get(i);
			String docContent = dataSplit.labeledTestData.get(i).getFirst();
			boolean docLabel = dataSplit.labeledTestData.get(i).getSecond();
			goldList.add(docLabel);
			String docLibSVMFormat = corpus.convertTestDocContentToTFIDF (docContent, false, true);
			
			try {
				String[] tokens = docLibSVMFormat.trim().split(" ");
				int[] indexArray = new int[tokens.length];
				double[] valueArray = new double[tokens.length];
				for (int j = 0; j < tokens.length; ++j) {
					String[] subTokens = tokens[j].trim().split(":");
					if (subTokens.length < 2) {
						continue;
					}
					int index = Integer.parseInt(subTokens[0].trim());
					double value = Double.parseDouble(subTokens[1].trim());
					indexArray[j] = index;
					valueArray[j] = value;
				}

				int[] labelIndexArray = new int[1];
				double[] labelValueArray = new double[1];
				labelIndexArray[0] = 0;
				labelValueArray[0] = 1;
				
				Object[] dataSample = new Object[4];
				dataSample[0] = indexArray;
				dataSample[1] = valueArray;
				dataSample[2] = labelIndexArray;
				dataSample[3] = labelValueArray;

				ScoreSet scoreSet = model.scoresExplicit(indexArray, valueArray);//only for Naivebayes
//				ScoreSet scoreSet = model.scores(dataSample);
				Score[] scoreArray = scoreSet.toArray();
		        double[] decValues = new double[scoreArray.length];
		        String[] labels = new String[scoreArray.length];
		        HashMap<String, Double> scoreMap = new HashMap<String, Double>();
				for (int j = 0; j < scoreArray.length; ++j) {
					labels[j] = scoreArray[j].value;
					decValues[j] = scoreArray[j].score;
					scoreMap.put(labels[j], decValues[j]);
				}
				
		        boolean booleanPredLabel = false;
		        if (scoreMap.get("1") > scoreMap.get("0")) 
		        	booleanPredLabel = true;
		        
		        predList.add(booleanPredLabel);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		double f1 = 0;
		try {
			f1 = TEDData.computePositiveF1 (goldList, predList);
			System.out.println("[Testing ] " + "Language: " + lgName + ", label: " + label + ", F1: " + f1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return f1;
	}
}
