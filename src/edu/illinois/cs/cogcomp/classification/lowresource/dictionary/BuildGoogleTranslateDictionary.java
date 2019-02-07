package edu.illinois.cs.cogcomp.classification.lowresource.dictionary;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.illinois.cs.cogcomp.MultiLingualResourcesConfig;
import edu.illinois.cs.cogcomp.classification.newsgroups.data.NGData;
import edu.illinois.cs.cogcomp.descartes.util.IOManager;
import edu.illinois.cs.cogcomp.translation.Translate;

public class BuildGoogleTranslateDictionary {
	
	public static void main (String[] args) {
//		buildOneLg ();
		buildLRL(args);
	}
	
	public static void buildOneLg () {
		MultiLingualResourcesConfig.initialization();
		
		String sLg = "zh";
		String outputFolder = MultiLingualResourcesConfig.multiLingualDictionaryFolderFromTranslation + sLg + "/";

		File output = new File(outputFolder);
		if (output.exists() == false) {
			output.mkdirs();
		}
		
		String inputFolder = MultiLingualResourcesConfig.multiLingual20NGFolder + sLg;
		Set<String> vocabulary = null;
		if (sLg.equals("zh") || sLg.equals("ja") || sLg.equals("ko")) {
			vocabulary = NGData.getCJKVocabularyFromFiles(inputFolder, sLg);
		} else {
			vocabulary = NGData.getVocabularyFromFiles(inputFolder);
		}
		
		String languageListFile = MultiLingualResourcesConfig.multiLingual20NGFolder + "languagelist_with_en.txt";
		List<String> intermediateLanguageList = IOManager.readLines(languageListFile);
		for (String tLg : intermediateLanguageList) {
			if (tLg.equals(sLg) == false) {
				
				System.out.println("Translating [" + sLg + "] to [" + tLg + "]");
				
				String outputFile = outputFolder + sLg + "-" + tLg + ".txt";
				try {
					getTranslationSet(vocabulary, sLg, tLg, outputFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
	
	public static void buildLRL (String[] args) {
		MultiLingualResourcesConfig.initialization();
		
		String lowResourceLanguageListFile = MultiLingualResourcesConfig.lowResourceLanguageList;
		List<String> sourceLanguageList = IOManager.readLines(lowResourceLanguageListFile);
	
		int num = Integer.parseInt(args[0]);
		int index = 0;
		int fold = 5;
		for (String sLg : sourceLanguageList) {
			if (sLg.equals("sh")) {
				index++;
				continue;
			}
			
			if (index % fold == num) {
				String outputFolder = MultiLingualResourcesConfig.multiLingualDictionaryFolderFromTranslationAll + sLg + "/";
//				String sLg = "ha";
//				String outputFolder = MultiLingualResourcesConfig.hausaDictionaryFolderFromTranslation;

				File output = new File(outputFolder);
				if (output.exists() == false) {
					output.mkdirs();
				}
				
				String inputFolder = MultiLingualResourcesConfig.multiLingual20NGAllFolder + sLg;
				Set<String> vocabulary = NGData.getVocabularyFromFiles(inputFolder);
				
				String languageListFile = MultiLingualResourcesConfig.multiLingual20NGFolder + "languagelist_with_en.txt";
				List<String> intermediateLanguageList = IOManager.readLines(languageListFile);
				for (String tLg : intermediateLanguageList) {
					if (tLg.equals(sLg) == false) {
						
						System.out.println("Translating [" + sLg + "] to [" + tLg + "]");
						
						String outputFile = outputFolder + sLg + "-" + tLg + ".txt";
						try {
							getTranslationSet(vocabulary, sLg, tLg, outputFile);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
			index++;
		}

	}

	public static void getTranslationSet (Set<String> vocabulary, String sLg, String tLg, String outputFile) throws IOException {
		FileWriter writer = new FileWriter(outputFile);
		int index = 0;
		for (String word : vocabulary) {
			if (index % 1000 == 0)
				System.out.println("  " + index);
			index++;
			String targetWord = Translate.googleTranslate(word, tLg, sLg);
//			System.out.println("Translated [" + word + "] from " + sLg + " to " + tLg + ": [" + targetWord + "]");
			writer.write(word + "\t" + targetWord + HausaDictioinary.systemNewLine);
		}
		writer.close();
	}
	

}
