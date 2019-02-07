package edu.illinois.cs.cogcomp.classification.lowresource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.illinois.cs.cogcomp.classification.lowresource.features.LanguageFeatures;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.descartes.util.IOManager;

public class PairLg20NGVotingBasedonDetailedOutput {
	public static String systemNewLine = System.getProperty("line.separator");
	
	public static void main (String[] args) throws IOException {
		String inputFolder = "/shared/shelley/yqsong/data/crossLingual/output-detailed-labels";
//		String outputFile = "matlab/lrlSummary-based-on-detailed-info.txt";
//		readFolder(inputFolder, outputFile);
		String outputVoting = "matlab/majority_voting.txt";
		getMajorityVoting (inputFolder,  outputVoting);
	}
	
	public static void getMajorityVoting (String folderPath, String outputFile) throws IOException {
		HashMap<String, HashMap<String, Double>> bridgeMap = new HashMap<String, HashMap<String, Double>>();
		HashMap<String, Double> selfMap = new HashMap<String, Double>();
		HashMap<String, HashMap<String, List<Pair<String, Pair<String, String>>>>> detailedMap = new HashMap<String, HashMap<String, List<Pair<String, Pair<String, String>>>>> ();
		File folder = new File (folderPath);
		File[] fileList = folder.listFiles();
		for (File file : fileList) {
			if (file.isDirectory() == true) 
				continue;
			readOneFile(file.getAbsolutePath(), selfMap, bridgeMap, detailedMap);
		}
		
		FileWriter writer = new FileWriter(outputFile);
		for (String sLg : detailedMap.keySet()) {
			HashMap<String, List<Pair<String, Pair<String, String>>>> results = detailedMap.get(sLg);
			
			HashMap<String, HashMap<String, Integer>> idLabelCountMap = new HashMap<String, HashMap<String,Integer>>();
			HashMap<String, String> idTruthMap = new HashMap<String, String>();
			for (String tLg : results.keySet()) {
				List<Pair<String, Pair<String, String>>> rList = results.get(tLg);
				for (Pair<String, Pair<String, String>> pair : rList) {
					String id = pair.getFirst();
					String label = pair.getSecond().getFirst();
					String pred = pair.getSecond().getSecond();
					
					idTruthMap.put(id, label);
					if (idLabelCountMap.containsKey(id) == false) {
						idLabelCountMap.put(id, new HashMap<String, Integer>());
					}
					if (idLabelCountMap.get(id).containsKey(pred) == false) {
						idLabelCountMap.get(id).put(pred, 0);
					}
					idLabelCountMap.get(id).put(pred, idLabelCountMap.get(id).get(pred) + 1);
				}
			}
			int count = 0;
			int correct = 0;
			for (String id : idTruthMap.keySet() ) {
				HashMap<String, Integer> countMap = idLabelCountMap.get(id);
				int maxCount = 0;
				String votePred = "";
				for (String pred : countMap.keySet()) {
					if (countMap.get(pred) > maxCount) {
						maxCount = countMap.get(pred);
						votePred = pred;
					}
				}
				count++;
				if (votePred.equals(idTruthMap.get(id))) {
					correct++;
				}
			}
			double acc = (double) correct / (count + Double.MIN_VALUE);
			writer.write(sLg + "\t" + acc + systemNewLine);
		}
		writer.close();
	}
	
	public static void computePairSimilarities (String folderPath, String outputFile, String type) throws IOException {
		HashMap<String, HashMap<String, Double>> bridgeMap = new HashMap<String, HashMap<String, Double>>();
		HashMap<String, Double> selfMap = new HashMap<String, Double>();
		HashMap<String, HashMap<String, List<Pair<String, Pair<String, String>>>>> detailedMap = new HashMap<String, HashMap<String, List<Pair<String, Pair<String, String>>>>> ();
		File folder = new File (folderPath);
		File[] fileList = folder.listFiles();
		for (File file : fileList) {
			if (file.isDirectory() == true) 
				continue;
			readOneFile(file.getAbsolutePath(), selfMap, bridgeMap, detailedMap);
		}
		Set<String> sLgSet = selfMap.keySet();
		
		LanguageFeatures lf = new LanguageFeatures();
		
		FileWriter writer = new FileWriter(outputFile);
		int index = 0;
		Set<String> tLgSet = null;
		for (String sLg : sLgSet) {
			HashMap<String, Double> brigdeScoreMap = bridgeMap.get(sLg);
			if (index == 0) {
				tLgSet = brigdeScoreMap.keySet();
				writer.write("source" + "," + "sourceScore" + ",");
				for (String tLg : tLgSet) {
					writer.write(tLg + ",");
				}
				writer.write(systemNewLine);
			}
			double score = selfMap.get(sLg);
			writer.write(sLg + "," + 1 + ",");
			
			System.out.println("[Source language] " + sLg);
			
			HashMap<String, Double> simMap = lf.getRankedLanguagesMap(sLg, type);
			if (simMap == null) {
				for (String tLg : tLgSet) {
					writer.write(0 + ",");
				}
			} else {
				for (String tLg : tLgSet) {
					if (simMap.containsKey(tLg))
						writer.write(simMap.get(tLg) + ",");
					else 
						writer.write(0 + ",");
				}
			}
			
			writer.write(systemNewLine);
			index++;
		}
		writer.close();
	}
	
	public static void readFolder (String folderPath, String outputFile) throws IOException {
		HashMap<String, HashMap<String, Double>> bridgeMap = new HashMap<String, HashMap<String, Double>>();
		HashMap<String, Double> selfMap = new HashMap<String, Double>();
		HashMap<String, HashMap<String, List<Pair<String, Pair<String, String>>>>> detailedMap = new HashMap<String, HashMap<String, List<Pair<String, Pair<String, String>>>>> ();
		File folder = new File (folderPath);
		File[] fileList = folder.listFiles();
		for (File file : fileList) {
			if (file.isDirectory() == true) 
				continue;
			readOneFile(file.getAbsolutePath(), selfMap, bridgeMap, detailedMap);
		}
		Set<String> sLgSet = selfMap.keySet();
		
		FileWriter writer = new FileWriter(outputFile);
		int index = 0;
		Set<String> tLgSet = null;
		for (String sLg : sLgSet) {
			HashMap<String, Double> brigdeScoreMap = bridgeMap.get(sLg);
			if (index == 0) {
				tLgSet = brigdeScoreMap.keySet();
				writer.write("source" + "," + "sourceScore" + ",");
				for (String tLg : tLgSet) {
					writer.write(tLg + ",");
				}
				writer.write(systemNewLine);
			}
			double score = selfMap.get(sLg);
			writer.write(sLg + "," + score + ",");
			for (String tLg : tLgSet) {
				writer.write(brigdeScoreMap.get(tLg) + ",");
			}
			writer.write(systemNewLine);
			index++;
		}
		writer.close();
	}
	
	public static void readOneFile (String filePath, HashMap<String, Double> selfMap, 
			HashMap<String, HashMap<String, Double>> bridgeMap,
			HashMap<String, HashMap<String, List<Pair<String, Pair<String, String>>>>> detailedMap) {
		File file = new File (filePath);
		String fileName = file.getName();
		String[] ftoks = fileName.substring(0, fileName.length() - 4).split("_"); 
		String sLg = ftoks[1];
		String tLg = ftoks[2];
		List<String> lines = IOManager.readLines(filePath);
		int correct = 0;
		int count = lines.size();
		List<Pair<String, Pair<String, String>>> detailedResults = new ArrayList<Pair<String, Pair<String, String>>>();
		for (int i = 0; i < lines.size(); ++i) {
			String[] tokens = lines.get(i).split("\t");
			String truth = tokens[1];
			String pred = tokens[2];
			if (truth.equals(pred)) 
				correct++;
			Pair<String, String> pair = new Pair<String, String>(truth, pred);
			Pair<String, Pair<String, String>> idPair = new Pair<String, Pair<String,String>>(tokens[0], pair);
			detailedResults.add(idPair);
		}
		if (detailedMap.containsKey(sLg)) {
			detailedMap.get(sLg).put(tLg, detailedResults);
		} else {
			HashMap<String, List<Pair<String, Pair<String, String>>>> map = new HashMap<String, List<Pair<String, Pair<String, String>>>>();
			map.put(tLg, detailedResults);
			detailedMap.put(sLg, map);
		}
		
		double acc = (double) correct/(count + Double.MIN_VALUE);
		selfMap.put(sLg, (double) 0);
		
		if (bridgeMap.containsKey(sLg)) {
			bridgeMap.get(sLg).put(tLg, acc);
		} else {
			HashMap<String, Double> map = new HashMap<String, Double>();
			map.put(tLg, acc);
			bridgeMap.put(sLg, map);
		}
		
	}
	
}
