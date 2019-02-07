package edu.illinois.cs.cogcomp.classification.flat.ted.data;

import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.descartes.util.IOManager;

public class TEDSimDataSplitting {
	
	public List<String> trainFullPaths = new ArrayList<String>();
	public List<String> trainPaths = new ArrayList<String>();
	public List<String> trainData = new ArrayList<String>();
	public List<String> trainLabels = new ArrayList<String>();
	public List<Double> trainSimilarities = new ArrayList<Double>();
	
	public List<String> testFullPaths = new ArrayList<String>();
	public List<String> testPaths = new ArrayList<String>();
	public List<String> testData = new ArrayList<String>();
	public List<String> testLabels = new ArrayList<String>();
	public List<Double> testSimilarities = new ArrayList<Double>();
	
	public List<Pair<String, Boolean>> labeledTrainData = new ArrayList<Pair<String, Boolean>>();

	public List<Pair<String, Boolean>> labeledTestData = new ArrayList<Pair<String, Boolean>>();

	
	public void readSimData (String trainingFile, String testFile, String lgName) {
		List<String> lines = IOManager.readLines(trainingFile);
		for (int i = 0; i < lines.size(); ++i) {
			String line = lines.get(i);
			String[] tokens = line.split("\t");
			String fullPath = tokens[0];
			String[] pathTokens = fullPath.split("/");
			String fileName = pathTokens[pathTokens.length - 1];
			String label = pathTokens[pathTokens.length - 2];
			String filePath = fullPath.substring(0, fullPath.indexOf(label));
			trainFullPaths.add(fullPath);
			trainPaths.add(filePath);
			trainLabels.add(label);
			trainData.add(fileName);
			trainSimilarities.add(Double.parseDouble(tokens[1]));
			
			String content = TEDDataSplitting.readDocumentForClassifiers(fullPath, lgName);
			Pair<String, Boolean> pair;
			if (label.equals("positive")) {
				pair = new Pair<String, Boolean>(content, true);
			} else {
				pair = new Pair<String, Boolean>(content, false);
			}
			labeledTrainData.add(pair);
		}
		
		lines = IOManager.readLines(testFile);
		for (int i = 0; i < lines.size(); ++i) {
			String line = lines.get(i);
			String[] tokens = line.split("\t");
			String fullPath = tokens[0];
			String[] pathTokens = fullPath.split("/");
			String fileName = pathTokens[pathTokens.length - 1];
			String label = pathTokens[pathTokens.length - 2];
			String filePath = fullPath.substring(0, fullPath.indexOf(label));
			testFullPaths.add(fullPath);
			testPaths.add(filePath);
			testLabels.add(label);
			testData.add(fileName);
			testSimilarities.add(Double.parseDouble(tokens[1]));
			
			String content = TEDDataSplitting.readDocumentForClassifiers(fullPath, lgName);
			Pair<String, Boolean> pair;
			if (label.equals("positive")) {
				pair = new Pair<String, Boolean>(content, true);
			} else {
				pair = new Pair<String, Boolean>(content, false);
			}
			labeledTestData.add(pair);
		}
	}
	
}
