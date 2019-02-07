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

public class StatThreshold {
	public static void main (String args[]) {
		int fold = 1;
		int selected = 0;
		SolverType solver = SolverType.L2R_LR_DUAL;  //  ClassifierConstant.solver;

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
					
//					for (int j = 0; j < TEDData.labelNameArray.length; ++j) {
//						System.out.println("[Summary] " + "\t" + TEDData.lgNameArray[lgID] + "\t" + TEDData.labelNameArray[j] + "\t" + f1List.get(j));
//					}
					
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
		
		System.out.print("Language: " + lgName + ", label: " + label + ":\t");
		
		String trainPath = simFolder + lgName + "_" + label + "_training.txt";
		String testPath = simFolder + lgName + "_" + label + "_test.txt";
		
		TEDSimDataSplitting simData = new TEDSimDataSplitting();
		simData.readSimData(trainPath, testPath, lgName);
		
		List<String> ids = new ArrayList<String>();
		List<Pair<String, Boolean>> data = new ArrayList<Pair<String, Boolean>>();
		List<Double> similarities = new ArrayList<Double>();
//		ids.addAll(simData.trainFullPaths);
		ids.addAll(simData.testFullPaths);
//		data.addAll(simData.labeledTrainData);
		data.addAll(simData.labeledTestData);
//		similarities.addAll(simData.trainSimilarities);
		similarities.addAll(simData.testSimilarities);
		
		double threshold = test (ids, data, similarities);
				
		return threshold;
	}
	
	public static double test (List<String> ids, List<Pair<String, Boolean>> data, List<Double> similarities) throws Exception {

		List<Boolean> goldLabels = new ArrayList<Boolean>();
		HashMap<String, String> corpusStringMap = new HashMap<String, String>();
		for (int i = 0; i < ids.size(); ++i) {
			String id = ids.get(i);
			String content = data.get(i).getFirst();
			corpusStringMap.put(id, content);
			goldLabels.add(data.get(i).getSecond());
		}
		
//		double bestThreshold = 0;
//		double bestF1 = 0;
//		for (int i = 0; i < 200; ++i) 
//		{
//			double threshold = 0.0001 * i;
//			List<Boolean> predList = new ArrayList<Boolean>();
//			for (int j = 0; j < ids.size(); ++j) {
//				if (similarities.get(j) > threshold) {
//					predList.add(true);
//				} else {
//					predList.add(false);
//				}
//			}
//			double f1 = TEDData.computePositiveF1 (goldLabels, predList);
//			if (bestF1 < f1) {
//				bestF1 = f1;
//				bestThreshold = threshold;
//			}
//		}
//		System.out.println("f1: " + bestF1 + ", bestThreshold: " + bestThreshold);

		List<Boolean> boostrappedLabels = new ArrayList<Boolean>();
		HashMap<String, Double> dataSimMap = new HashMap<String, Double>();
		for (int i = 0; i < ids.size(); ++i) {
			dataSimMap.put(ids.get(i), similarities.get(i));
		}
		TreeMap<String, Double> sortedMap = HashSort.sortByValues(dataSimMap);
		HashSet<String> labeledPositive = new HashSet<String>();
		int initialNum = ids.size() / 10;
		int count = 0;
		for (String id : sortedMap.keySet()) {
			labeledPositive.add(id);
			if (count > initialNum)
				break;
			count++;
		}
		for (int i = 0; i < ids.size(); ++i) {
			if (labeledPositive.contains(ids.get(i))) {
				boostrappedLabels.add(true);
			} else {
				boostrappedLabels.add(false);
			}
		}
		double f1 = TEDData.computePositiveF1 (goldLabels, boostrappedLabels);
		System.out.println("f1: " + f1);
		return f1;
	}
}
