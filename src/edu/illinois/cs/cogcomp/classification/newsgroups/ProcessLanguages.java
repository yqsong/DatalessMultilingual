package edu.illinois.cs.cogcomp.classification.newsgroups;

import java.util.HashMap;
import java.util.List;

import edu.illinois.cs.cogcomp.descartes.util.IOManager;

public class ProcessLanguages {

	public static void main (String[] args) {
		List<String> lines1 = IOManager.readLines("results/88languages-top3.txt");
		List<String> lines2 = IOManager.readLines("results/179languages-index.txt");
		
		HashMap<String, Double> orginalIndexResultsMap = new HashMap<String, Double>();
		HashMap<String, Double> filteredIndexResultsMap = new HashMap<String, Double>();
		HashMap<String, Double> mergedIndexResultsMap = new HashMap<String, Double>();
		for (int i = 0; i < lines1.size(); ++i) {
			String[] tokens = lines1.get(i).split("\t");
			orginalIndexResultsMap.put(tokens[1], Double.parseDouble(tokens[2]));
			filteredIndexResultsMap.put(tokens[1], Double.parseDouble(tokens[2]));
			mergedIndexResultsMap.put(tokens[1], Double.parseDouble(tokens[5]));
		}
		
		HashMap<String, Integer> orginalIndexMap = new HashMap<String, Integer>();
		HashMap<String, Integer> filteredIndexMap = new HashMap<String, Integer>();
		HashMap<String, Integer> mergedIndexMap = new HashMap<String, Integer>();
		for (int i = 0; i < lines2.size(); ++i) {
			String[] tokens = lines2.get(i).split("\t");
			String lgName = tokens[0];
			if (orginalIndexResultsMap.containsKey(lgName)) {
				orginalIndexMap.put(tokens[0], Integer.parseInt(tokens[1]));
				filteredIndexMap.put(tokens[0], Integer.parseInt(tokens[2]));
				mergedIndexMap.put(tokens[0], Integer.parseInt(tokens[3]));
			}
		}
		for (String key : orginalIndexResultsMap.keySet()) {
			System.out.println(key + "\t" + 
					orginalIndexMap.get(key) + "\t" + 
					filteredIndexMap.get(key) + "\t" + 
					mergedIndexMap.get(key) + "\t" + 
					orginalIndexResultsMap.get(key) + "\t" + 
					mergedIndexResultsMap.get(key) );
		}
	}
	
}
