package edu.illinois.cs.cogcomp.classification.lowresource.dictionary;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.oscii.lex.Lexicon;
import org.oscii.lex.Meaning;
import org.oscii.lex.Translation;
import org.oscii.panlex.PanLexDir;
import org.oscii.panlex.PanLexJSONParser;

import edu.illinois.cs.cogcomp.MultiLingualResourcesConfig;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.HashSort;
import edu.illinois.cs.cogcomp.classification.newsgroups.data.NGData;
import edu.illinois.cs.cogcomp.descartes.util.IOManager;
import edu.illinois.cs.cogcomp.translation.Translate;

public class BuildPanLexDictionary {
	static final String DEFAULT_PATTERN = "(?U)\\p{Lower}*";
	
    private final static Logger log = LogManager.getLogger(BuildPanLexDictionary.class);
	
	public static void main (String[] args) {
		buildAllDictionaryFor20NG();
	}
	
	public static void buildAllDictionaryFor20NG () {
		MultiLingualResourcesConfig.initialization();
		
		String lowResourceLanguageListFile = MultiLingualResourcesConfig.lowResourceLanguageList;
		List<String> sourceLanguageList = IOManager.readLines(lowResourceLanguageListFile);

		String languageListFile = "C:\\yqsong\\data\\cross-lingual\\languagelist_with_en.txt"; 
		//MultiLingualResourcesConfig.multiLingual20NGFolder + "languagelist_with_en.txt";
		List<String> intermediateLanguageList = IOManager.readLines(languageListFile);


		String path = "C:\\yqsong\\data\\panlex\\panlex-20160801-json";
		PanLexJSONParser panLex = new PanLexJSONParser(new PanLexDir(path));
		String DEFAULT_LANGUAGES = "";
		for (String lg : intermediateLanguageList) {
			if (lg.equals("sh"))
				continue;
			DEFAULT_LANGUAGES += lg + ",";
		}
		List<String> languages = Arrays.asList(DEFAULT_LANGUAGES.split(","));
		panLex.addLanguages(languages);
		Pattern pattern = Pattern.compile(DEFAULT_PATTERN);
		panLex.read(pattern);
		Lexicon lexicon = new Lexicon();
		panLex.forEachMeaning(lexicon::add);
		
		int index = 0;
		for (String sLg : sourceLanguageList) {
			if (sLg.equals("sh")) {
				index++;
				continue;
			}

			String outputFolder = "C:\\yqsong\\data\\cross-lingual\\panlex-dict-20ng\\" + sLg + "/";
					//MultiLingualResourcesConfig.multiLingualDictionaryFolderFromTranslationAll + sLg + "/";

			File output = new File(outputFolder);
			if (output.exists() == false) {
				output.mkdirs();
			}
			
			String inputFolder = "C:\\yqsong\\data\\cross-lingual\\multilingual-20ng\\" + sLg;
					//MultiLingualResourcesConfig.multiLingual20NGAllFolder + sLg;
			Set<String> vocabulary = NGData.getVocabularyFromFiles(inputFolder);
			
			for (String tLg : intermediateLanguageList) {
				if (tLg.equals("sh")) {
					continue;
				}
				if (tLg.equals(sLg) == false) {
					
					System.out.println("Translating [" + sLg + "] to [" + tLg + "]");
					
					String outputFile = outputFolder + sLg + "-" + tLg + ".txt";
					try {
						getTranslationSet(lexicon, vocabulary, sLg, tLg, outputFile);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			index++;
		}
		
		
	}
	
	public static void getTranslationSet (Lexicon lexicon, Set<String> vocabulary, String sLg, String tLg, String outputFile) throws IOException {
		
		FileWriter writer = new FileWriter(outputFile);
		int index = 0;
		for (String word : vocabulary) {
			if (index % 1000 == 0)
				System.out.println("  " + index);
			index++;
			String targetWord = getPenLexTranslate(lexicon, word, sLg, tLg);
			if (targetWord.trim().equals("")) {
				targetWord = word;
			}
//			System.out.println("Translated [" + word + "] from " + sLg + " to " + tLg + ": [" + targetWord + "]");
			writer.write(word + "\t" + targetWord + HausaDictioinary.systemNewLine);
		}
		writer.close();
	}
	
	public static String getPenLexTranslate (Lexicon lexicon, String word, String sLg, String tLg) {
		List<Meaning> results = lexicon.lookup(word, sLg);
		if (results.isEmpty() == true) {
//			word = word.replaceAll("[][(){},.;!?<>%]", "");
			word = word.replaceAll("\\p{Punct}+", "");
			results = lexicon.lookup(word, sLg);
		}
//		if (results.isEmpty() == true) {
//			Pattern pt = Pattern.compile("[^a-zA-Z0-9]");
//	        Matcher match= pt.matcher(word);
//	        while(match.find())
//	        {
//	            String s= match.group();
//	            word=word.replaceAll("\\"+s, "");
//	        }
//			results = lexicon.lookup(word, sLg);
//		}
		HashMap<String, Integer> translateMap = new HashMap<String, Integer>();
		HashSet<String> allWords = new HashSet<String>();
		for (int i = 0; i < results.size(); ++i) {
			List<Translation> tranlations = results.get(i).translations;
			for (int j = 0; j < tranlations.size(); ++j) {
				if (tranlations.get(j).translation.language.equals(tLg) == true) { 
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
		String transStr = "";
		for (String trans : allWords) {
			transStr += trans + " ";
		}
		return transStr;
	}
	

}
