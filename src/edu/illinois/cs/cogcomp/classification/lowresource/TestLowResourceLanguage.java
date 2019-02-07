package edu.illinois.cs.cogcomp.classification.lowresource;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import edu.illinois.cs.cogcomp.MultiLingualResourcesConfig;
import edu.illinois.cs.cogcomp.classification.lowresource.dictionary.HausaDictioinary;
import edu.illinois.cs.cogcomp.classification.lowresource.dictionary.TranslationDictionary;
import edu.illinois.cs.cogcomp.classification.newsgroups.NG20ClassificationWithMergedIndex;
import edu.illinois.cs.cogcomp.classification.newsgroups.NG20ClassificationWithOriginalIndex;
import edu.illinois.cs.cogcomp.classification.newsgroups.Test20NGwithMultiLanguages;
import edu.illinois.cs.cogcomp.classification.newsgroups.data.NGData;
import edu.illinois.cs.cogcomp.descartes.util.IOManager;

public class TestLowResourceLanguage {
	
	// translate the low resource language (LRL), e.g., Hausa to HRL, e.g., Arabic
	// use all HRL-english index to find English titles based on HRL queries 

	public static void main (String[] args) throws IOException {
		MultiLingualResourcesConfig.initialization();

		int foldNum = Integer.parseInt(args[0]);
//		String outputFolder = "/shared/shelley/yqsong/data/crossLingual/output-detailed-labels/";
//		outputDetailedResults(foldNum, outputFolder);
		
		String outputFolder = "/home/data/corpora/20-newsgroups/output-panlex-org/";
		String dictFolder = "/home/data/corpora/20-newsgroups/panlex-dict-20ng/"; 
//				MultiLingualResourcesConfig.multiLingualDictionaryFolderFromTranslation;
		outputAggregatedResults(foldNum, outputFolder, dictFolder);
	}

	public static void outputDetailedResults (int foldNum, String outputFolder) throws IOException {
		
		String lowResourceLanguageListFile = MultiLingualResourcesConfig.lowResourceLanguageList;
		List<String> sourceLanguageList = IOManager.readLines(lowResourceLanguageListFile);
		HashSet<String> sourceLanguageHashSet = new HashSet<String>(sourceLanguageList);
		int lgCount = 0;
		for (int i = 0; i < sourceLanguageList.size(); ++i) {

			String sLg = sourceLanguageList.get(i);
			if (i % 5 == foldNum) {
				
				if (sLg.equals("sh"))
					continue;
				
				NGData data = NGData.readData(MultiLingualResourcesConfig.multiLingual20NGFolder + sLg);
				String translateDictFolder = MultiLingualResourcesConfig.multiLingualDictionaryFolderFromTranslation + sLg + "/";

				int topK = 1;
				
				String languageListFile = MultiLingualResourcesConfig.multiLingual20NGFolder + "languagelist_with_en.txt";
				List<String> languageList = IOManager.readLines(languageListFile);

				int tLgCount = 0;
				for (String tLg : languageList) 
				{
					if (sourceLanguageHashSet.contains(tLg) == true) 
						continue;
					
					String outFile = outputFolder + "output_" + sLg + "_" + tLg + ".txt";
					FileWriter writer = new FileWriter(outFile);

					System.out.println("---------------------------" + lgCount + " Source Language " + sLg + "---------------------------");
					System.out.println("---------------------------" + tLgCount + " Target Language " + tLg + "---------------------------");
					String tDictFile = translateDictFolder + sLg + "-" + tLg + ".txt";
					try {
						TranslationDictionary tDict = new TranslationDictionary(tDictFile);
						data.lgName = tLg;
						List<String[]> results = null;
						if (tLg.equals("en")) {
							results = TranslatedNG20ClassificationWithEnglishIndex.annotate(data, topK, tDict.dictionary);
						} else {
							results = TranslatedNG20ClassificationWithMergedIndex.annotate(data, topK, tDict.dictionary);
						}
						for (String[] result : results) {
							writer.write(result[0] + "\t" + result[1] + "\t" + result[2] + HausaDictioinary.systemNewLine);
						}
						
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					writer.flush();
					writer.close();
					tLgCount++;
				}
				
				lgCount++;
			}
			
		}
	}

	public static void outputAggregatedResults (int foldNum, String outputFolder, String dictFolder) throws IOException {
		
		String lowResourceLanguageListFile = MultiLingualResourcesConfig.lowResourceLanguageList;
		List<String> sourceLanguageList = IOManager.readLines(lowResourceLanguageListFile);
		HashSet<String> sourceLanguageHashSet = new HashSet<String>(sourceLanguageList);
		int lgCount = 0;
		for (int i = 0; i < sourceLanguageList.size(); ++i) {

			String sLg = sourceLanguageList.get(i);
			if (i % 5 == foldNum) {
				if (sLg.equals("sh"))
					continue;
				NGData data = NGData.readData(MultiLingualResourcesConfig.multiLingual20NGFolder + sLg);
				String translateDictFolder = dictFolder + sLg + "/";
				String outFile = outputFolder + "output_" + sLg + ".txt";

				double precision = 0;
				int topK = 1;
				
				FileWriter writer = new FileWriter(outFile);
				
				System.out.println("---------------------------" + lgCount + " Source Language " + sLg + "---------------------------");
				System.out.println("---------------------------" + "original Index" + "---------------------------");
				precision = NG20ClassificationWithOriginalIndex.test(data, topK);
				writer.write("[Original Index]\t" + data.lgName + "\t" + precision + HausaDictioinary.systemNewLine);

				System.out.println("---------------------------" + lgCount + " Source Language " + sLg + "---------------------------");
				System.out.println("---------------------------" + "merged Index" + "---------------------------");
				precision = NG20ClassificationWithMergedIndex.test(data, topK);
				writer.write("[Merged Index]\t" + data.lgName + "\t" + precision + HausaDictioinary.systemNewLine);
				
				if (sLg.equals("ha")) {
					String dictFile = MultiLingualResourcesConfig.hausaDictionary;
					HausaDictioinary dict = new HausaDictioinary(dictFile);
					System.out.println("---------------------------" + lgCount + " Source Language " + sLg + "---------------------------");
					System.out.println("---------------------------" + " English Index with Public Dictionary" + "---------------------------");
					precision = TranslatedNG20ClassificationWithEnglishIndex.test(data, topK, dict.dictionary);
					writer.write("[English Index]\t" + "English" + "\t" + precision + HausaDictioinary.systemNewLine);
				}
				
				String languageListFile = MultiLingualResourcesConfig.multiLingual20NGFolder + "languagelist_with_en.txt";
				List<String> languageList = IOManager.readLines(languageListFile);

				int tLgCount = 0;
				for (String tLg : languageList) 
//				String tLg = "en";
				{
					if (sourceLanguageHashSet.contains(tLg) == true) 
						continue;
					
					System.out.println("---------------------------" + lgCount + " Source Language " + sLg + "---------------------------");
					System.out.println("---------------------------" + tLgCount + " Target Language " + tLg + "---------------------------");
					String tDictFile = translateDictFolder + sLg + "-" + tLg + ".txt";
					try {
						TranslationDictionary tDict = new TranslationDictionary(tDictFile);
						data.lgName = tLg;
						if (tLg.equals("en")) {
							precision = TranslatedNG20ClassificationWithEnglishIndex.test(data, topK, tDict.dictionary);
						} else {
							precision = TranslatedNG20ClassificationWithMergedIndex.test(data, topK, tDict.dictionary);
						}
						
						writer.write("[Merged Index]\t" + tLg + "\t" + precision + HausaDictioinary.systemNewLine);
					} catch (Exception e) {
						e.printStackTrace();
					}
					writer.flush();
					
					tLgCount++;
				}
				writer.close();
				
				lgCount++;
			}
			
		}
	}
	
}
