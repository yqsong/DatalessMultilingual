package edu.illinois.cs.cogcomp.classification.lowresource.dictionary;

import static java.util.stream.Collectors.toSet;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.oscii.lex.Lexicon;
import org.oscii.lex.Meaning;
import org.oscii.lex.Translation;
import org.oscii.panlex.PanLexDir;
import org.oscii.panlex.PanLexJSONParser;
import org.oscii.panlex.PanLexJSONParser3Digits;

import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.HashSort;
import edu.illinois.cs.cogcomp.classification.newsgroups.data.NGData;
import edu.illinois.cs.cogcomp.reader.util.IOManager;

public class TestPanLexDict {

	public static void main (String[] args) throws Exception {
//		testEnglish20NG3Digits();
		panlexStats ();
	}
	
	public static void panlexStats () throws Exception {
		String lgListFile = "C:\\yqsong\\data\\panlex\\panlex-20160801-csv\\ex.csv";
		List<String> lines = IOManager.readLines(lgListFile);
		HashMap<String, Integer> lgIDMap = new HashMap<String, Integer>();
		for (int i = 1; i < lines.size(); ++i) {
			String line = lines.get(i);
			String[] tokens = line.split(",");
			String id = tokens[1];
			if (lgIDMap.containsKey(id)) {
				lgIDMap.put(id, lgIDMap.get(id) + 1);
			} else {
				lgIDMap.put(id, 1);
			}
		}
		
		lgListFile = "C:\\yqsong\\data\\panlex\\panlex-20160801-csv\\lv.csv";
		lines = IOManager.readLines(lgListFile);
		HashMap<String, Set<String>> lgCodeIDMap = new HashMap<String, Set<String>>();
		for (int i = 1; i < lines.size(); ++i) {
			String line = lines.get(i);
			String[] tokens = line.split(",");
			String id = tokens[0];
			String code = tokens[1];
			if (lgCodeIDMap.containsKey(code)) {
				
			} else {
				HashSet<String> idSet = new HashSet<String>();
				lgCodeIDMap.put(code, idSet);
			}
			lgCodeIDMap.get(code).add(id);
		}
		
		String statFile = "C:\\yqsong\\data\\cross-lingual\\output.txt";
		lines = IOManager.readLines(statFile);

		String outFile = "C:\\yqsong\\data\\cross-lingual\\outputStats.txt";
		FileWriter writer = new FileWriter(outFile);

		for (int i = 0; i < lines.size(); ++i) {
			String[] tokens = lines.get(i).split("\t");
			
			Set<String> idSet = lgCodeIDMap.get(tokens[0]);
			int totalFreq = 0;
			int count = 0;
			for (String id : idSet) {
				if (lgIDMap.containsKey(id)) {
					int freq = lgIDMap.get(id);
					totalFreq += freq;
					count++;
				}
			}
			double avgFreq = totalFreq / (count + Double.MIN_VALUE);
			
			String line = lines.get(i) + "\t" + avgFreq;
			writer.write(line + HausaDictioinary.systemNewLine);
		}
		
		writer.close();
	}
	
	public static void testEnglish20NG3Digits () throws IOException {
		String lgListFile = "C:\\yqsong\\data\\panlex\\panlex-20160801-csv\\lv.csv";
		List<String> lines = IOManager.readLines(lgListFile);
		HashSet<String> lgSet = new HashSet<String>();
		HashMap<Integer, String> lgIDMap = new HashMap<Integer, String>();
		for (int i = 1; i < lines.size(); ++i) {
			String line = lines.get(i);
			String[] tokens = line.split(",");
			lgSet.add(tokens[1]);
			int id = Integer.parseInt(tokens[0]);
			lgIDMap.put(id, tokens[1]);
		}
		String lgString = "";
		for (String lg : lgSet) {
			lgString += lg + ",";
		}
		
		String path = "C:\\yqsong\\data\\penlex\\panlex-20160801-json";
		PanLexJSONParser3Digits panLex = new PanLexJSONParser3Digits(new PanLexDir(path));
		List<String> languages = Arrays.asList(lgString.split(","));
		panLex.addLanguages(languages);
		System.out.println("Three Digits: " + languages.size());
		System.out.println("Two Digits: " + panLex.languageTags.size());
		Pattern pattern = Pattern.compile(BuildPanLexDictionary.DEFAULT_PATTERN);
		panLex.read(pattern);
		Lexicon lexicon = new Lexicon();
		panLex.forEachMeaning(lexicon::add);
		
		String inputFolder = "C:\\yqsong\\data\\cross-lingual\\multilingual-20ng\\en\\";
		Set<String> vocabulary = NGData.getVocabularyFromFiles(inputFolder);
		
		int count = 0;
		HashMap<String, Integer> lgMap = new HashMap<String, Integer>();
		for (String tLg3Digit : lgSet) {
			if (tLg3Digit.equals(""))
				continue;
			
			System.out.println(tLg3Digit);
			count = 0;
			for (String word : vocabulary) {
				
				List<Meaning> results = lexicon.lookup(word, "eng");
				if (results.isEmpty() == true) {
//					word = word.replaceAll("[][(){},.;!?<>%]", "");
					word = word.replaceAll("\\p{Punct}+", "");
					results = lexicon.lookup(word, "eng");
				}
				
				if (results.isEmpty() == true) 
					continue;
				
				count++;
				
				boolean getTranslation = false;
				for (int i = 0; i < results.size(); ++i) {
					List<Translation> tranlations = results.get(i).translations;
					for (int j = 0; j < tranlations.size(); ++j) {
						if (tranlations.get(j).translation.language.equals(tLg3Digit) == true) { 
							getTranslation = true;
						}
					}
				}
				if (getTranslation == true) {
					if (lgMap.containsKey(tLg3Digit) == false) {
						lgMap.put(tLg3Digit, 1);
					} else {
						lgMap.put(tLg3Digit, lgMap.get(tLg3Digit) + 1);
					}
				}
			}
		}
		
		String outFile = "C:\\yqsong\\data\\cross-lingual\\output.txt";
		FileWriter writer = new FileWriter(outFile);
		for (String lg : lgMap.keySet()) {
			int lgCount = lgMap.get(lg);
			double perc = lgCount / (count + Double.MIN_VALUE);
			writer.write(lg + "\t" + perc + HausaDictioinary.systemNewLine);
		}
		writer.close();
	}
	
	public static void testPenLex () {
		String path = "C:\\yqsong\\data\\penlex\\panlex-20160801-json";
		PanLexJSONParser panLex = new PanLexJSONParser(new PanLexDir(path));
		String DEFAULT_LANGUAGES = "en,st";
		List<String> languages = Arrays.asList(DEFAULT_LANGUAGES.split(","));
		panLex.addLanguages(languages);
		Pattern pattern = Pattern.compile(BuildPanLexDictionary.DEFAULT_PATTERN);
		panLex.read(pattern);
		Lexicon lexicon = new Lexicon();
		panLex.forEachMeaning(lexicon::add);
		
		List<Meaning> results = lexicon.lookup("drive", "en");
		HashMap<String, Integer> translateMap = new HashMap<String, Integer>();
		HashSet<String> allWords = new HashSet<String>();
		for (int i = 0; i < results.size(); ++i) {
			List<Translation> tranlations = results.get(i).translations;
			for (int j = 0; j < tranlations.size(); ++j) {
				if (tranlations.get(j).translation.language.equals("es") == true) { 
					String transStr = tranlations.get(j).translation.text;
					allWords.add(transStr);
					if (translateMap.containsKey(transStr)) {
						translateMap.put(transStr, translateMap.get(transStr) + 1);
					} else {
						translateMap.put(transStr, 1);
					}
				}
			}
		}
//		for (String word : allWords) {
//			System.out.println(word);
//		}
		TreeMap<String, Integer> sortedMap = HashSort.sortByValues(translateMap);
		for (String word : sortedMap.keySet()) {
			System.out.println(word + "\t" + translateMap.get(word));
		}
	}
	
}
