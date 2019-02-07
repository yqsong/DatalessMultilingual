package edu.illinois.cs.cogcomp.classification.flat.ted.dataless;

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
import edu.illinois.cs.cogcomp.classification.flat.ted.data.TEDData;
import edu.illinois.cs.cogcomp.classification.flat.ted.data.TEDSimDataSplitting;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.jlis.CorpusDataProcessing;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.HashSort;
import edu.illinois.cs.cogcomp.classification.hierarchy.evaluation.StatUtils;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;

public class TEDDatalessClassificationNaive {
	public static void main (String args[]) {
		int fold = 1;
		int selected = 0;
		SolverType solver = SolverType.L2R_L2LOSS_SVC_DUAL;  //  ClassifierConstant.solver;

		String simFolder = "/shared/shelley/yqsong/data/tedcldc/output.5.3.0-standardAnalyzer-sim/";

		try {
			for (int i = 0; i < TEDData.lgNameArray.length; ++i) {
				int lgID = i;
				
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
		
		String trainPath = simFolder + lgName + "_" + label + "_training.txt";
		String testPath = simFolder + lgName + "_" + label + "_test.txt";
		
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
		
		int initialNum = 150;
		int maxIter = 10;
		int addNum = 5;
		double f1 = classify (ids, data, similarities, simData.testFullPaths, simData.labeledTestData, initialNum, maxIter, addNum, C, eps, solver);
				
		return f1;
	}
	
	public static double classify (List<String> ids, List<Pair<String, Boolean>> data, List<Double> similarities, 
			List<String> testIds, List<Pair<String, Boolean>> testData,
			int initialNumPredefine, int maxIter, int addNum,
			double C, double eps, SolverType solver) throws Exception {

		List<Boolean> goldLabels = new ArrayList<Boolean>();
		HashMap<String, String> corpusStringMap = new HashMap<String, String>();
		for (int i = 0; i < ids.size(); ++i) {
			String id = ids.get(i);
			String content = data.get(i).getFirst();
			corpusStringMap.put(id, content);
			goldLabels.add(data.get(i).getSecond());
		}
		
		HashMap<String, Double> dataSimMap = new HashMap<String, Double>();
		for (int i = 0; i < ids.size(); ++i) {
			dataSimMap.put(ids.get(i), similarities.get(i));
		}
		TreeMap<String, Double> sortedMap = HashSort.sortByValues(dataSimMap);
		
		for (int initialNum = 10; initialNum < 400; initialNum += 10) {
			HashSet<String> labeledPositive = new HashSet<String>();
			int count = 0;
			for (String id : sortedMap.keySet()) {
				labeledPositive.add(id);
				if (count > initialNum)
					break;
				count++;
			}
			List<Boolean> boostrappedLabels = new ArrayList<Boolean>();
			for (int i = 0; i < ids.size(); ++i) {
				if (labeledPositive.contains(ids.get(i))) {
					boostrappedLabels.add(true);
				} else {
					boostrappedLabels.add(false);
				}
			}
			double f1 = 0;
			System.out.print("[Dataless all] retrieved num " + initialNum);
			f1 = TEDData.computePositiveF1 (goldLabels, boostrappedLabels);
		}
		
		double bestTest = 0;
		for (int initialNum = 10; initialNum < 400; initialNum += 10) {
			HashSet<String> labeledPositive = new HashSet<String>();
			int count = 0;
			for (String id : sortedMap.keySet()) {
				labeledPositive.add(id);
				if (count > initialNum)
					break;
				count++;
			}
			List<Boolean> testPredList = new ArrayList<Boolean>();
			List<Boolean> testGoldList = new ArrayList<Boolean>();
			for (int i = 0; i < testIds.size(); ++i) {
				testGoldList.add(testData.get(i).getSecond());
				if (labeledPositive.contains(testIds.get(i))) {
					testPredList.add(true);
				} else {
					testPredList.add(false);
				}
			}
			double f1 = 0;
			System.out.print("[Dataless test] retrieved num " + initialNum);
			f1 = TEDData.computePositiveF1 (testGoldList, testPredList);
			if (f1 > bestTest)
				bestTest = f1;
		}
		
		HashSet<String> labeledPositive = new HashSet<String>();
		int count = 0;
		for (String id : sortedMap.keySet()) {
			labeledPositive.add(id);
			if (count > initialNumPredefine)
				break;
			count++;
		}
		List<Boolean> testPredList = new ArrayList<Boolean>();
		List<Boolean> testGoldList = new ArrayList<Boolean>();
		for (int i = 0; i < testIds.size(); ++i) {
			testGoldList.add(testData.get(i).getSecond());
			if (labeledPositive.contains(testIds.get(i))) {
				testPredList.add(true);
			} else {
				testPredList.add(false);
			}
		}
		double f1 = 0;
		System.out.print("[Dataless test] predefined num " + initialNumPredefine);
		f1 = TEDData.computePositiveF1 (testGoldList, testPredList);
		return f1;
	}
	
	
}
