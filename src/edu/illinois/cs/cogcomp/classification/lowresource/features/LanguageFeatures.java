package edu.illinois.cs.cogcomp.classification.lowresource.features;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.TreeMap;

import edu.illinois.cs.cogcomp.MultiLingualResourcesConfig;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.HashSort;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.descartes.util.IOManager;

public class LanguageFeatures {
	
	public static String systemNewLine = System.getProperty("line.separator");

//	[Code does not match] aze	az	Azerbaijani
//	[Code does not match] fas	fa	P(Farsi)
//	[Code does not match] hrv	hr	Croatian
//	[Code does not match] msa	ms	Malay
//	[Code does not match] nno	nn	Norwegian Nynorsk
//	[Code does not match] nor	no	Norwegian
//	[Code does not match] srp	sr	Serbian
//	[Code does not match] zho	zh	Chinese
	
//	[Code does not match] mlg	mg	Malagasy
//	[Code does not match] mon	mn	Mongolian
//	[Code does not match] sqi	sq	Albanian
//	[Code does not match] swa	sw	Swahili
//	[Code does not match] uzb	uz	Uzbek
//	[Code does not match] yid	yi	Yiddish
	HashMap<String, String> missMatchCodeMap = new HashMap<String, String>(){
		{
			put("ara", "arz");
			put("aze", "azb");
			put("fas", "pes");
			put("hrv", "bos");
			put("msa", "zsm");
			put("nno", "nob");
			put("nor", "nob");
			put("srp", "bos");
			put("zho", "csl");
			
			put("mlg", "plt");
			put("mon", "bxm"); // xal  mvf
			put("sqi", "als");
			put("swa", "swh");
			put("uzb", "uzn");
			put("yid", "yih");
		}
	};
	
	HashMap<String, List<String>> featureMap = new HashMap<String, List<String>>();
	List<Pair<String, String>> languageMap = new ArrayList<Pair<String, String>>();
	List<Pair<String, String>> languageNameMap = new ArrayList<Pair<String, String>>();
	HashSet<String> bridgeLangageSet = new HashSet<String>();
	HashMap<String, Integer> languageWikiSizeMap = new HashMap<String, Integer>();
	HashSet<String> lowResourceLanguageSet = new HashSet<String>();
	HashMap<String, HashMap<String, Integer>> lgLinkNumMap = new 
			HashMap<String, HashMap<String, Integer>>();
	HashMap<String, HashMap<String, Double>> lgSummaryMap = new 
			HashMap<String, HashMap<String, Double>>();
	List<String> hrlList = new ArrayList<String>();
	List<String> lrlList = new ArrayList<String>();

	
	HashMap<String, HashMap<String, Double>> lgSimilarities;
	public static String[] simTypes = new String[] {"lingustic", "wikisize", "combinedsize", "langlinks", "combinedlink"};

	public static void main (String[] args) {
		LanguageFeatures lf = new LanguageFeatures();
		lf.getRankedLanguagesMap ("uz", simTypes[0]);
		String outputFile = "matlab/languageFeatures.txt";
		String outputMappingFile = "matlab/languageFeatureMapping.txt";
		try {
			lf.exportLanguageFeatures(outputFile, outputMappingFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public LanguageFeatures () {
		if (MultiLingualResourcesConfig.isInitialized == false)
			MultiLingualResourcesConfig.initialization();
		readLanguageFeatures(MultiLingualResourcesConfig.languageFeatures);
		String languageListFile = MultiLingualResourcesConfig.allLanguageList;
		readBridgeLanguageCodes(languageListFile);
		readLowResourceLanguageCodes(MultiLingualResourcesConfig.lowResourceLanguageList);
		readLanguageWikiSize(MultiLingualResourcesConfig.languageWikiSize);
		readLanguageCodes(MultiLingualResourcesConfig.languageCodes);
		
		readWikiLanguageLinks("matlab/lrl_wiki_LangLinks.txt");
		readLanguageSummaryMap("matlab/lrlSummary_new.txt");
		
		computeNaiveSimilarities();
	}
	
	public void exportLanguageFeatures (String outputFile, String outputMappingFile) throws IOException {
		HashMap<String, Integer> globalMap = new HashMap<String, Integer>();
		HashMap<Integer, String> globalInverseMap = new HashMap<Integer, String>();
		int gIndex = 1;
		
		HashMap<Integer, Integer> categoryMapping = new HashMap<Integer, Integer>();
		for (String lgCode : bridgeLangageSet) {
			String newlgCode = replaceDepCodes (lgCode);
			String lgWALSCode = findLanguageWALSCode(newlgCode);
			List<String> features = featureMap.get(lgWALSCode);
			for (int i = 0; i < features.size(); ++i) {
				String feature = "featue-" + i + "-" + features.get(i);
				if (globalMap.containsKey(feature) == false) {
					globalMap.put(feature, gIndex);
					globalInverseMap.put(gIndex, feature);
					categoryMapping.put(gIndex, i + 1);
					gIndex++;
				}
			}
		}
		
		FileWriter cateWriter = new FileWriter(outputMappingFile);
		for (Integer key : categoryMapping.keySet()) {
			cateWriter.write(key + "," + categoryMapping.get(key) + systemNewLine);
			cateWriter.flush();		
		}
		cateWriter.close();
		FileWriter writer = new FileWriter(outputFile);
		for (String lgCode : bridgeLangageSet) {
			String newlgCode = replaceDepCodes (lgCode);
			String lgWALSCode = findLanguageWALSCode(newlgCode);
			List<String> features = featureMap.get(lgWALSCode);
			String line = lgCode + " ";
			HashMap<Integer, Integer> featureHash = new HashMap<Integer, Integer>();
			for (int i = 0; i < features.size(); ++i) {
				String feature = "featue-" + i + "-" + features.get(i);
				if (globalMap.containsKey(feature) == true) {
					int index = globalMap.get(feature);
					featureHash.put(index, 1);
				}
			}
			List<Integer> keyList = new ArrayList<Integer>(featureHash.keySet());
			Collections.sort(keyList);
			for (int i = 0; i < keyList.size(); ++i) {
				line += keyList.get(i) + ":" + 1 + " ";
			}
			line = line.trim();
			writer.write(line + systemNewLine);
			writer.flush();
		}
		writer.close();
	}
	
	public HashMap<String, Double> getRankedLanguagesMap (String lg, String type) {
		
		HashMap<String, Double> simMap = getSimilarities(lg);
		if (simMap == null) 
			return null; 
		TreeMap<String, Double> sortedMap1 = HashSort.sortByValues(simMap);
		int index = 0;
		HashMap<String, Integer> rankMap1 = new HashMap<String, Integer>();
		for (String key : sortedMap1.keySet()) {
			rankMap1.put(key, index);
			index++;
//			System.out.println(key + "\t" + lf.languageNameMap.get(key) + "\t" + rankMap1.get(key));
		}
		
		TreeMap<String, Integer> sortedMap2 = HashSort.sortByValues(languageWikiSizeMap);
		HashMap<String, Integer> rankMap2 = new HashMap<String, Integer>();
		index = 0;
		for (String key : sortedMap2.keySet()) {
			rankMap2.put(key, index);
			index++;
		}
		HashMap<String, Double> wikiSizeDoubleMap = new HashMap<String, Double>();
		for (String key : rankMap1.keySet()) {
			wikiSizeDoubleMap.put(key, (double)languageWikiSizeMap.get(key));
		}
		
		HashMap<String, Double> rankMap = new HashMap<String, Double>();
		for (String key : rankMap1.keySet()) {
			int v1 = rankMap1.get(key);
			int v2 = rankMap2.get(key);
			double value = 2 * (rankMap1.size() - v1) * (rankMap2.size() - v2) / 
					( (rankMap1.size() - v1) + (rankMap2.size() - v2) + Double.MIN_VALUE);
			rankMap.put(key, value);
		}
		
		
		HashMap<String, Double> langLinkDoubleMap = new HashMap<String, Double>();
		for (String key : rankMap1.keySet()) {
			langLinkDoubleMap.put(key, lgLinkNumMap.get(lg).get(key) + 0.0);
		}
		
		TreeMap<String, Integer> sortedMap3 = HashSort.sortByValues(lgLinkNumMap.get(lg));
		HashMap<String, Integer> rankMap3 = new HashMap<String, Integer>();
		index = 0;
		for (String key : sortedMap3.keySet()) {
			rankMap3.put(key, index);
			index++;
		}
		HashMap<String, Double> rankMapNew = new HashMap<String, Double>();
		for (String key : rankMap1.keySet()) {
			int v1 = rankMap1.get(key);
			int v2 = rankMap3.get(key);
			double value = 2 * (rankMap1.size() - v1) * (rankMap3.size() - v2) / 
					( (rankMap1.size() - v1) + (rankMap3.size() - v2) + Double.MIN_VALUE);
			rankMapNew.put(key, value);
		}
		


		if (type.equals(simTypes[0])) {
			rankMap = simMap;
		}
		if (type.equals(simTypes[1])) {
			rankMap = wikiSizeDoubleMap;
		}
		if (type.equals(simTypes[2])) {
		}
		if (type.equals(simTypes[3])) {
			rankMap = langLinkDoubleMap;
		}
		if (type.equals(simTypes[4])) {
			rankMap = rankMapNew;
		}
		
		TreeMap<String, Double> sortedMap = HashSort.sortByValues(rankMap);
		index = 0;
		for (String key : sortedMap.keySet()) {
			if (index > 10) {
				break;
			}
			index++;
			System.out.println(key + "\t" + findLanguageName(key) + "\t" + rankMap.get(key)
				+ "\t" + lgSummaryMap.get(lg).get(key));
		}
		
		return rankMap;
		
	}
	
	public String findLanguageCode (String lg) {
		String lgCode = "";
		for (int i = 0; i < languageMap.size(); ++i) {
			if (languageMap.get(i).getFirst().equals(lg))
				lgCode = languageMap.get(i).getSecond();
		}
		return lgCode;
	}
	
	public String findLanguageWALSCode (String lg) {
		String lgCode = "";
		lgCode = replaceDepCodes (lgCode);
		for (int i = 0; i < languageMap.size(); ++i) {
			if (languageMap.get(i).getSecond().equals(lg))
				lgCode = languageMap.get(i).getFirst();
		}
		return lgCode;
	}
	
	public String replaceDepCodes (String lgCode) {
		String newCode = lgCode;
		if (lgCode.equals("sh")) {
			newCode = "sr";
		}
		if (lgCode.equals("mo")) {
			newCode = "ro";
		}
		
		return newCode;
	}
	
	public String findLanguageName (String lg) {
		String lgCode = "";
		for (int i = 0; i < languageNameMap.size(); ++i) {
			if (languageNameMap.get(i).getFirst().equals(lg))
				lgCode = languageNameMap.get(i).getSecond();
		}
		return lgCode;
	}
	
	public HashMap<String, Double> getSimilarities (String lg) {
		return this.lgSimilarities.get(lg);
	}
	
	public void computeNaiveSimilarities () {
		lgSimilarities = new HashMap<String, HashMap<String, Double>>();
		
		for (String lgCodeI : lrlList) {
			if (lgSimilarities.containsKey(lgCodeI) == false) {
				lgSimilarities.put(lgCodeI, new HashMap<String, Double>());
			}
			for (String lgCodeJ : hrlList) {
				
				double sim = getSim(findLanguageWALSCode(lgCodeI), findLanguageWALSCode(lgCodeJ));
				lgSimilarities.get(lgCodeI).put(lgCodeJ, sim);
			}
		}
	}
	
//	public void computeNaiveSimilarities () {
//		List<String> lgList = new ArrayList<String>(featureMap.keySet());
//		lgSimilarities = new HashMap<String, HashMap<String, Double>>();
//		for (int i = 0 ; i < lgList.size() - 1; ++i) {
//			String lgCodeI = findLanguageMap(lgList.get(i));
//			System.out.println(lgCodeI);
//			if (lgCodeI != null && lgCodeI.equals("bs")) {
//				System.out.println();
//			}
//			if (bridgeLangageSet.contains(lgCodeI) == false) 
//				continue;
//			if (lgSimilarities.containsKey(lgCodeI) == false) {
//				lgSimilarities.put(lgCodeI, new HashMap<String, Double>());
//			}
//			for (int j = i + 1; j < lgList.size(); ++j) {
//				String lgCodeJ = findLanguageMap(lgList.get(j));
//				if (bridgeLangageSet.contains(lgCodeJ) == false) 
//					continue;
//				if (lgSimilarities.containsKey(lgCodeJ) == false) {
//					lgSimilarities.put(lgCodeJ, new HashMap<String, Double>());
//				}
//				double sim = getSim(lgList.get(i), lgList.get(j));
//				lgSimilarities.get(lgCodeI).put(lgCodeJ, sim);
//				lgSimilarities.get(lgCodeJ).put(lgCodeI, sim);
//			}
//		}
//	}
	
	public double getSim (String lg1, String lg2) {
		List<String> features1 = featureMap.get(lg1);
		List<String> features2 = featureMap.get(lg2);
		double count = 0;
		if (features1 == null || features2 == null)
			return 0;
		if (features1.size() < features2.size()) {
			for (int i = 0; i < features1.size(); ++i) {
				if (features1.get(i).equals(features2.get(i))) {
					if (i <= 3)
						count += 50;
					else
						count++;
				}
			}
		} else {
			for (int i = 0; i < features2.size(); ++i) {
				if (features1.get(i).equals(features2.get(i))) {
					if (i <= 3)
						count += 50;
					else
						count++;
				}
			}
		}
		return count;
	}
	
	public void readLanguageFeatures (String filePath) {
		List<String> lines = IOManager.readLines(filePath);
		String[] cates = lines.get(0).split(",");
		for (int i = 1; i < lines.size(); ++i) {
			String[] tokens = lines.get(i).split(",");
			String lgCode = tokens[1];
			if (lgCode.equals("") == true) {
				continue;
			}
			List<String> features = new ArrayList<String>();
			for (int j = 6; j < tokens.length; ++j) {
				features.add(tokens[j]);
			}
			featureMap.put(lgCode, features);
//			System.out.println("[" + lgCode + "] " + features.size());
		}
	}
	
	public void readLanguageCodes (String filePath) {
		List<String> lines = IOManager.readLines(filePath);
		for (int i = 1; i < lines.size(); ++i) {
			String[] tokens = lines.get(i).split(",");

			if (bridgeLangageSet.contains(tokens[2])) {
				if (featureMap.containsKey(tokens[3]) == false) {
					if (missMatchCodeMap.containsKey(tokens[3])) {
						languageMap.add(new Pair<String, String>(missMatchCodeMap.get(tokens[3]), tokens[2]));
						languageNameMap.add(new Pair<String, String>(tokens[2], tokens[1]));
					} else {
						System.out.println("[Code does not match] " + tokens[3] + "\t" + tokens[2] + "\t" + tokens[1]);
					}
				} else {
					languageMap.add(new Pair<String, String>(tokens[3], tokens[2]));
					languageNameMap.add(new Pair<String, String>(tokens[2], tokens[1]));
				}
			}
		}
	}
	
	public void readLanguageWikiSize (String filePath) {
		List<String> lines = IOManager.readLines(filePath);
		for (int i = 0; i < lines.size(); ++i) {
			String[] tokens = lines.get(i).split("\t");
			languageWikiSizeMap.put(tokens[0], Integer.parseInt(tokens[1]));
		}
	}
	
	public void readWikiLanguageLinks (String filePath) {
		List<String> lines = IOManager.readLines(filePath);
		
		String line = lines.get(0);
		String[] tokens = line.split(",");
		List<String> hrlList = new ArrayList<String>();
		for (int i = 1; i < tokens.length; ++i) {
			hrlList.add(tokens[i]);
		}
		List<String> lrlList = new ArrayList<String>();
		for (int i = 1; i < lines.size(); ++i) {
			tokens = lines.get(i).split(",");
			lrlList.add(tokens[0]);
			
			lgLinkNumMap.put(tokens[0], new HashMap<String, Integer>());
			for (int j = 1; j < tokens.length; ++j) {
				lgLinkNumMap.get(tokens[0]).put(hrlList.get(j - 1), Integer.parseInt(tokens[j]));	
			}
		}
		
	}
	
	public void readLanguageSummaryMap (String filePath) {
		List<String> lines = IOManager.readLines(filePath);
		String line = lines.get(0);
		String[] tokens = line.split(",");
		for (int i = 2; i < tokens.length; ++i) {
			hrlList.add(tokens[i]);
		}
		for (int i = 1; i < lines.size(); ++i) {
			tokens = lines.get(i).split(",");
			lrlList.add(tokens[0]);
			
			lgSummaryMap.put(tokens[0], new HashMap<String, Double>());
			for (int j = 2; j < tokens.length; ++j) {
				lgSummaryMap.get(tokens[0]).put(hrlList.get(j - 2), Double.parseDouble(tokens[j]));	
			}
		}
	}
	
	public void readBridgeLanguageCodes (String filePath) {
		List<String> lines = IOManager.readLines(filePath);
		for (int i = 0; i < lines.size(); ++i) {
			bridgeLangageSet.add(lines.get(i).trim());
		}
	}
	
	public void readLowResourceLanguageCodes (String filePath) {
		List<String> lines = IOManager.readLines(filePath);
		for (int i = 0; i < lines.size(); ++i) {
			lowResourceLanguageSet.add(lines.get(i).trim());
		}
	}
}
