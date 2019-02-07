package edu.illinois.cs.cogcomp.classification.lowresource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import edu.illinois.cs.cogcomp.classification.lowresource.features.LanguageFeatures;
import edu.illinois.cs.cogcomp.descartes.util.IOManager;

public class PairLg20NG {
	public static String systemNewLine = System.getProperty("line.separator");
	
	public static void main (String[] args) throws IOException {
		String inputFolder = "/home/data/corpora/20-newsgroups/output-panlex-org/";
		String outputFile = "/home/data/corpora/20-newsgroups/outputpanlex-org.txt";
		readFolder(inputFolder, outputFile);
		
//		String inputFolder = "/shared/shelley/yqsong/data/crossLingual/output-submission";
//		String outputFile = "matlab/lrlSummary_new.txt";
//		readFolder(inputFolder, outputFile);
//		String outputFileSim1 = "matlab/lrlSimilarities_lingustic_AAAI.txt";
//		String outputFileSim2 = "matlab/lrlSimilarities_wikisize_AAAI.txt";
//		String outputFileSim3 = "matlab/lrlSimilarities_combinedsize_AAAI.txt";
//		String outputFileSim4 = "matlab/lrlSimilarities_langlinks_AAAI.txt";
//		String outputFileSim5 = "matlab/lrlSimilarities_combinedlinks_AAAI.txt";
//		computePairSimilarities (outputFile, outputFileSim1, LanguageFeatures.simTypes[0]);
//		computePairSimilarities (outputFile, outputFileSim2, LanguageFeatures.simTypes[1]);
//		computePairSimilarities (outputFile, outputFileSim3, LanguageFeatures.simTypes[2]);
//		computePairSimilarities (outputFile, outputFileSim4, LanguageFeatures.simTypes[3]);
//		computePairSimilarities (outputFile, outputFileSim5, LanguageFeatures.simTypes[4]);
	}
	
	public static void computePairSimilarities (String summaryFile, String outputFile, String type) throws IOException {
		List<String> lines = IOManager.readLines(summaryFile);
		String line = lines.get(0);
		String[] tokens = line.split(",");
		List<String> hrlList = new ArrayList<String>();
		for (int i = 2; i < tokens.length; ++i) {
			hrlList.add(tokens[i]);
		}
		List<String> lrlList = new ArrayList<String>();
		HashMap<String, HashMap<String, Double>> lgSummaryMap = new 
				HashMap<String, HashMap<String, Double>>();
		for (int i = 1; i < lines.size(); ++i) {
			tokens = lines.get(i).split(",");
			lrlList.add(tokens[0]);
			
			lgSummaryMap.put(tokens[0], new HashMap<String, Double>());
			for (int j = 2; j < tokens.length; ++j) {
				lgSummaryMap.get(tokens[0]).put(hrlList.get(j - 2), Double.parseDouble(tokens[j]));	
			}
		}
		
		LanguageFeatures lf = new LanguageFeatures();
		
		FileWriter writer = new FileWriter(outputFile);
		writer.write("source" + "," + "sourceScore" + ",");
		for (String tLg : hrlList) {
			writer.write(tLg + ",");
		}
		writer.write(systemNewLine);
		for (String sLg : lrlList) {
			writer.write(sLg + "," + 1 + ",");
			
			System.out.println("[Source language] " + sLg);
			
			HashMap<String, Double> simMap = lf.getRankedLanguagesMap(sLg, type);
			if (simMap == null) {
				for (String tLg : hrlList) {
					writer.write(0 + ",");
				}
			} else {
				for (String tLg : hrlList) {
					if (simMap.containsKey(tLg))
						writer.write(simMap.get(tLg) + ",");
					else 
						writer.write(0 + ",");
				}
			}
			
			writer.write(systemNewLine);
		}
		writer.close();
	}
	
	public static void readFolder (String folderPath, String outputFile) throws IOException {
		HashMap<String, HashMap<String, Double>> bridgeMap = new HashMap<String, HashMap<String, Double>>();
		HashMap<String, Double> selfMap = new HashMap<String, Double>();
		File folder = new File (folderPath);
		File[] fileList = folder.listFiles();
		for (File file : fileList) {
			if (file.isDirectory() == true) 
				continue;
			readOneFile(file.getAbsolutePath(), selfMap, bridgeMap);
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
	
	public static void readOneFile (String filePath, HashMap<String, Double> selfMap, HashMap<String, HashMap<String, Double>> bridgeMap) {
		List<String> lines = IOManager.readLines(filePath);
		String sLg = "";
		HashMap<String, Double> map = new HashMap<String, Double>();
		for (int i = 1; i < lines.size(); ++i) {
			String[] tokens = lines.get(i).split("\t");
			double score = Double.parseDouble(tokens[2]);
			if (i == 1) {
				sLg = tokens[1];
				selfMap.put(sLg, score);
			} else {
				String tLg = tokens[1];
				map.put(tLg, score);
			}
		}
		bridgeMap.put(sLg, map);
	}
	
}
