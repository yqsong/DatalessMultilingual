	package edu.illinois.cs.cogcomp.classification.flat.ted.muse;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;
import edu.illinois.cs.cogcomp.MultiLingualResourcesConfig;
import edu.illinois.cs.cogcomp.classification.flat.ted.data.TEDData;
import edu.illinois.cs.cogcomp.classification.flat.ted.data.TEDSimDataSplitting;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.jlis.CorpusDataProcessing;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.HashSort;
import edu.illinois.cs.cogcomp.classification.hierarchy.evaluation.StatUtils;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.embedding.multiembedding.EmbeddingLanguages;

public class TEDEmbeddingBootstrapping {
	public static void main (String args[]) {
		int fold = 1;
		int selected = 0;
		SolverType solver = SolverType.L2R_L2LOSS_SVC_DUAL;  //  ClassifierConstant.solver;

		String simFolder = "/home/data/corpora/tedcldc/output.embedding-muse-sim/";
		String resource = "muse";
		HashMap<String, String> availability = EmbeddingLanguages.muse_availability;
		String newMuseFolderStr = MultiLingualResourcesConfig.musePath;
		File newMuseFolder = new File (newMuseFolderStr);
		String[] fileList = newMuseFolder.list();
		for (int i = 0; i < fileList.length; ++i) {
			String file = fileList[i];
			System.out.println(file);
			String[] tokens = file.split("\\.");
			System.out.println(tokens[1]);
			availability.put(tokens[1], tokens[1]);
		}

		try {
			for (int i = 0; i < TEDData.lgNameArray.length; ++i) {
				int lgID = i;
				
				if (availability.containsKey(TEDData.lgNameArray[lgID]) == false) {
					continue;
				}
				
				if (lgID % fold == selected) {
					List<Double> f1List = new ArrayList<Double>();
					for (int j = 0; j < TEDData.labelNameArray.length; ++j) {
						double f1 = testOneLabelOneLanguage (lgID, j, 10000, 0.01, solver, simFolder);
						f1List.add(f1);
					}
					
					for (int j = 0; j < TEDData.labelNameArray.length; ++j) {
						System.out.println("[Summary] " + "\t" + TEDData.lgNameArray[lgID] + "\t" + TEDData.labelNameArray[j] + "\t" + f1List.get(j));
					}
					
					double mean = StatUtils.listAverage(f1List);
					double std = StatUtils.std(f1List, mean);
					System.out.println("[Summary] " + "\t" + "ALL" + "\t" + mean + "\t" + std);
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
	
	public static double testOneLabelOneLanguage (int lgID, int labelID, double C, double eps, SolverType solver, String simFolder) throws Exception {
		String lgName = TEDData.lgNameArray[lgID];
		String label = TEDData.labelNameArray[labelID];
		
		System.out.println("-----------------------------------C=" + C + "----------------------------------");
		System.out.println("Language: " + lgName + ", label: " + label);
		
		if (lgName.equals("pb")) {
			lgName = "pb-pt";
		}
		
		String trainPath = simFolder + "output.embedding-muse-sim" + lgName + "_" + label + "_training.txt";
		String testPath = simFolder + "output.embedding-muse-sim" + lgName + "_" + label + "_test.txt";
		
		TEDSimDataSplitting simData = new TEDSimDataSplitting();
		simData.readSimData(trainPath, testPath, lgName);
		
		List<String> ids = new ArrayList<String>();
		List<Pair<String, Boolean>> data = new ArrayList<Pair<String, Boolean>>();
		List<Double> similarities = new ArrayList<Double>();
		ids.addAll(simData.trainFullPaths);
		ids.addAll(simData.testFullPaths);
		data.addAll(simData.labeledTrainData);
		data.addAll(simData.labeledTestData);
		similarities.addAll(simData.trainSimilarities);
		similarities.addAll(simData.testSimilarities);
		
		int initialPosNum = 50;
		int initialNegNum = 500;
		int initialDatalessNum = 200;
		int maxIter = 10;
		int addNum = 5;
		double f1 = bootstrapping (ids, data, similarities, simData.testFullPaths, simData.labeledTestData, 
				initialPosNum, initialNegNum, initialDatalessNum, maxIter, addNum, C, eps, solver);
				
		return f1;
	}
	
	public static double bootstrapping (List<String> ids, List<Pair<String, Boolean>> data, List<Double> similarities, 
			List<String> testIds, List<Pair<String, Boolean>> testData,
			int initialPosNum, int initialNegNum, int initialDatalessNum, int maxIter, int addNum,
			double C, double eps, SolverType solver) throws Exception {

		List<Boolean> goldLabels = new ArrayList<Boolean>();
		HashMap<String, String> corpusStringMap = new HashMap<String, String>();
		for (int i = 0; i < ids.size(); ++i) {
			String id = ids.get(i);
			String content = data.get(i).getFirst();
			corpusStringMap.put(id, content);
			goldLabels.add(data.get(i).getSecond());
		}
		
		CorpusDataProcessing corpus = new CorpusDataProcessing();
		corpus.startFromZero = false;
		HashMap<String, String> trainingDataLibSVMFormat = corpus.initializeTrainingDocumentFeatures (corpusStringMap, true, true);

		HashMap<String, Double> dataSimMap = new HashMap<String, Double>();
		for (int i = 0; i < ids.size(); ++i) {
			dataSimMap.put(ids.get(i), similarities.get(i));
		}
		TreeMap<String, Double> sortedMap = HashSort.sortByValues(dataSimMap);
		
		
		HashSet<String> labeledPositive = new HashSet<String>();
		HashSet<String> labeledNegative = new HashSet<String>();
		int count = 0;
		for (String id : sortedMap.keySet()) {
			if (count < initialPosNum)
				labeledPositive.add(id);
			if (count > sortedMap.size() - initialNegNum)
				labeledNegative.add(id);
			count++;
		}
		
		HashSet<String> labeledPositiveForTest = new HashSet<String>();
		count = 0;
		for (String id : sortedMap.keySet()) {
			labeledPositiveForTest.add(id);
			if (count > initialDatalessNum)
				break;
			count++;
		}
		List<Boolean> testPredList = new ArrayList<Boolean>();
		List<Boolean> testGoldList = new ArrayList<Boolean>();
		for (int i = 0; i < testIds.size(); ++i) {
			testGoldList.add(testData.get(i).getSecond());
			if (labeledPositiveForTest.contains(testIds.get(i))) {
				testPredList.add(true);
			} else {
				testPredList.add(false);
			}
		}
		double f1 = 0;
		System.out.print("[Initialization test] ");
		f1 = TEDData.computePositiveF1 (testGoldList, testPredList);
		

		
		List<FeatureNode[]> xList = new ArrayList<FeatureNode[]>();
		for (int i = 0; i < ids.size(); ++i) {
			String docID = ids.get(i);
			// initialize data for libLinear
			String[] tokens = trainingDataLibSVMFormat.get(docID).split(" ");
			FeatureNode[] feature = new FeatureNode[tokens.length];
			for (int j = 0; j < tokens.length; ++j) {
				String[] subTokens = tokens[j].trim().split(":");
				int index = Integer.parseInt(subTokens[0].trim());
				double value = Double.parseDouble(subTokens[1].trim());
				feature[j] = new FeatureNode(index, value);
			}
			xList.add(feature);
		}
		
		
		List<Model> modelList = new ArrayList<Model>();
		
		List<FeatureNode[]> testXList = new ArrayList<FeatureNode[]>();
		testGoldList = new ArrayList<Boolean>();
		testPredList = new ArrayList<Boolean>();
		for (int i = 0; i < testData.size(); ++i) {
			// initialize data for libLinear
			String svmFormat = corpus.convertTestDocContentToTFIDF(testData.get(i).getFirst(), true, true);
			String[] tokens = svmFormat.split(" ");
			FeatureNode[] feature = new FeatureNode[tokens.length];
			for (int j = 0; j < tokens.length; ++j) {
				String[] subTokens = tokens[j].trim().split(":");
				int index = Integer.parseInt(subTokens[0].trim());
				double value = Double.parseDouble(subTokens[1].trim());
				feature[j] = new FeatureNode(index, value);
			}
			testXList.add(feature);
			testGoldList.add(testData.get(i).getSecond());
		}
		
		for (int iter = 0; iter < maxIter; ++iter) {
			List<FeatureNode[]> xTrainList = new ArrayList<FeatureNode[]>();
			List<Double> yTrainList = new ArrayList<Double>();
			for (int i = 0; i < ids.size(); ++i) {
				String docID = ids.get(i);
				
				if (labeledPositive.contains(docID)) {
					yTrainList.add(1.0);
					xTrainList.add(xList.get(i));
				}
				if (labeledNegative.contains(docID)) {
					yTrainList.add(2.0);
					xTrainList.add(xList.get(i));
				}
			}
			
			Feature[][] xArray = new Feature[xTrainList.size()][];
			double[] yArray = new double[xTrainList.size()];
			for (int i = 0; i < xTrainList.size(); ++i) {
				yArray[i] = yTrainList.get(i);
				xArray[i] = xTrainList.get(i);
			}
			Problem problem = new Problem();
			problem.l = yTrainList.size(); // number of training examples
			problem.n = corpus.getDictSize(); // number of features
			problem.x = xArray; // feature nodes
			problem.y = yArray; // target values
			
			Model model = null;
			
			try {
				if (problem != null) {
				    Calendar cal = Calendar.getInstance();
				    long startTime = cal.getTimeInMillis();
					System.out.println("  [Training:] svm iteration: " + iter + "; data num: " + problem.l + "; feature num: " + problem.n);
					
					Parameter parameter = new Parameter(solver, C, eps);
					
					if (problem.l > 0) {
						Linear.disableDebugOutput();
						model = Linear.train(problem, parameter);
					}
					Calendar cal1 = Calendar.getInstance();
		    		long endTime = cal1.getTimeInMillis();
		    		long second = (endTime - startTime)/1000;
					System.out.println("  [Training:] finished," + " time: " + second + " seconds");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			modelList.add(model);
			
			int startModel = 0;
			if (modelList.size() >= 5) {
				startModel = modelList.size() - 5;
			}
			else {
				startModel = modelList.size() - 1;
			}
			
			List<Double> svmScores = new ArrayList<Double>();
			for (int i = 0; i < xList.size(); ++i) {
				svmScores.add(0.0);
			}
			for (int m = startModel; m < modelList.size(); ++m) {
				for (int i = 0; i < xList.size(); ++i) {
			        double[] decValues = new double[2];
			        double yTest = Linear.predictValues(modelList.get(m), xList.get(i), decValues);
			        int[] labels = modelList.get(m).getLabels();
			        int positiveIndex = 0;
			        for (int j = 0; j < labels.length; ++j) {
			        	if (labels[j] == 1) {
			        		positiveIndex = j;
			        	}
			        }
			        
			        double[] transformedValues = new double[decValues.length];
			        int nr_class = decValues.length;
			        int nr_w;
			        if (nr_class == 2)
			            nr_w = 1;
			        else
			            nr_w = nr_class;

			        //a key problem of original svm prob output	Output a lot of zeroes when lack of training data.        
			        for (int j = 0; j < nr_w; j++) {
			        	transformedValues[j] = 1 / (1 + Math.exp(-decValues[j] + Math.random() * 1e-10));
			        }
			        
			        for (int j = 0; j < nr_w; j++) {
			        	if (decValues[j] == 0) {
			        		transformedValues[j] = 0;
			        	}
			        }

			        if (nr_class == 2) // for binary classification
			        	transformedValues[1] = 1. - transformedValues[0];
			        else {
			            double sum = 0;
			            for (int j = 0; j < nr_class; j++)
			                sum += transformedValues[j];

			            for (int j = 0; j < nr_class; j++)
			            	transformedValues[j] = transformedValues[j] / sum;
			        }
			        
			        svmScores.set(i, svmScores.get(i) + transformedValues[positiveIndex]);
				}
			}
			
			
			List<Boolean> boostrappedLabels = new ArrayList<Boolean>();
			dataSimMap = new HashMap<String, Double>();
			for (int i = 0; i < ids.size(); ++i) {
				dataSimMap.put(ids.get(i), svmScores.get(i));
			}
			sortedMap = HashSort.sortByValues(dataSimMap);
//			labeledPositive = new HashSet<String>();
			int newPosNum = initialPosNum + addNum * iter;
			int newNegNum = initialNegNum + addNum * 10 * iter;
			count = 0;
			labeledPositive.clear();	
			labeledNegative.clear();
			for (String id : sortedMap.keySet()) {
				if (count < newPosNum)
					labeledPositive.add(id);
				if (count > sortedMap.size() - newNegNum)
					labeledNegative.add(id);
				count++;
			}
			
			for (int i = 0; i < ids.size(); ++i) {
				if (labeledPositive.contains(ids.get(i))) {
					boostrappedLabels.add(true);
				} else {
					boostrappedLabels.add(false);
				}
			}
			
			f1 = 0;
			System.out.print("[Iteration] " + iter);
			f1 = TEDData.computePositiveF1 (goldLabels, boostrappedLabels);
			
			
			testPredList = new ArrayList<Boolean>();
			int addedPos = 0;
			for (int i = 0; i < testData.size(); ++i) {
				double[] decValues = new double[2];
				double yTest = Linear.predictValues(model, testXList.get(i), decValues);
				if (yTest == 1 || labeledPositiveForTest.contains(testIds.get(i))) {
					testPredList.add(true);
					addedPos++;
				} else {
					testPredList.add(false);
				}
			}
			System.out.print("[Iteration] " + iter + " test");
			f1 = TEDData.computePositiveF1 (testGoldList, testPredList);
			System.out.println("[Added positive] " + addedPos);
			
		}
		
		testPredList = new ArrayList<Boolean>();
		for (int i = 0; i < testData.size(); ++i) {
			double[] decValues = new double[2];
			double yTest = Linear.predictValues(modelList.get(modelList.size() - 1), testXList.get(i), decValues);
			if (yTest == 1 || labeledPositiveForTest.contains(testIds.get(i))) {
				testPredList.add(true);
			} else {
				testPredList.add(false);
			}
			
//			int oneNum = 0;
//			int startModel = 0;
//			int voteGate = 0;
//			if (modelList.size() >= 5) {
//				startModel = modelList.size() - 5;
//				voteGate = 5;
//			}
//			else {
//				startModel = modelList.size() - 1;
//				voteGate = 1;
//			}
//			for (int j = startModel; j < modelList.size(); ++j) {
//				double[] decValues = new double[2];
//				double yTest = Linear.predictValues(modelList.get(j), testXList.get(i), decValues);
//				if (yTest == 1) 
//					oneNum++;
//			}
//			if (oneNum * 2 + 1 >= voteGate || labeledPositiveForTest.contains(testIds.get(i))) {
//				testPredList.add(true);
//			} else {
//				testPredList.add(false);
//			}
		}
		
		f1 = TEDData.computePositiveF1 (testGoldList, testPredList);
		return f1;
	}
	
	
}
