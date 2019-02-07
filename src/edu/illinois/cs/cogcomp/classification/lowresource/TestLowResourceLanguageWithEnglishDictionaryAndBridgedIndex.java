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

public class TestLowResourceLanguageWithEnglishDictionaryAndBridgedIndex {

	// translate the low resource language (LRL), e.g., Hausa to English
	// use all HRL-english index to find English titles based on English queries 
	
	public static void main (String[] args) throws IOException {
		MultiLingualResourcesConfig.initialization();
		
		String lowResourceLanguageListFile = MultiLingualResourcesConfig.lowResourceLanguageList;
		List<String> sourceLanguageList = IOManager.readLines(lowResourceLanguageListFile);
		HashSet<String> sourceLanguageHashSet = new HashSet<String>(sourceLanguageList);
		int lgCount = 0;
		for (String sLg : sourceLanguageList)
		{
			
			NGData data = NGData.readData(MultiLingualResourcesConfig.multiLingual20NGFolder + sLg);
			String translateDictFolder = MultiLingualResourcesConfig.multiLingualDictionaryFolderFromTranslation + sLg + "/";
			String outFile = "/shared/shelley/yqsong/data/crossLingual/output-bridge-english/output_" + sLg + ".txt";

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
			
			String languageListFile = MultiLingualResourcesConfig.multiLingual20NGFolder + "languagelist_with_en.txt";
			List<String> languageList = IOManager.readLines(languageListFile);

			int tLgCount = 0;
			for (String tLg : languageList) 
			{
				if (sourceLanguageHashSet.contains(tLg) == true) 
					continue;
				
				System.out.println("---------------------------" + lgCount + " Source Language " + sLg + "---------------------------");
				System.out.println("---------------------------" + tLgCount + " Target Language " + tLg + "---------------------------");
				String tDictFile = translateDictFolder + sLg + "-" + "en" + ".txt";
				try {
					TranslationDictionary tDict = new TranslationDictionary(tDictFile);
					data.lgName = tLg;
					if (tLg.equals("en")) {
						
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
