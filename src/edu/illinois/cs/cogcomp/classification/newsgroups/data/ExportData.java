package edu.illinois.cs.cogcomp.classification.newsgroups.data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import edu.illinois.cs.cogcomp.MultiLingualResourcesConfig;
import edu.illinois.cs.cogcomp.classification.hierarchical.rcv.data.LanguageMapping;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.jlis.CorpusDataProcessing;
import edu.illinois.cs.cogcomp.classification.lowresource.TranslatedNG20ClassificationWithEnglishIndex;
import edu.illinois.cs.cogcomp.classification.lowresource.dictionary.TranslationDictionary;
import edu.illinois.cs.cogcomp.descartes.util.IOManager;
import edu.illinois.cs.cogcomp.embedding.esa.index.AnalyzerFactory;

public class ExportData {
	public static String systemNewLine = System.getProperty("line.separator");

	public static void main (String[] args) throws IOException {
		MultiLingualResourcesConfig.initialization();
//		exportDocTranslated ();
		exportWordTranslated ();
	}
	
	public static void exportWordTranslated () throws IOException {
		String inputFolder = MultiLingualResourcesConfig.multiLingual20NGFolder + "en";
		String outputFolder = "/shared/shelley/yqsong/data/crossLingual/libSVMFormatData/wordLevel/";
		String languageListFile = MultiLingualResourcesConfig.multiLingual20NGFolder + "languagelist_with_en.txt";
		String translateDictFolder = MultiLingualResourcesConfig.multiLingualDictionaryFolderFromTranslation + "en" + "/";
		List<String> lgList = IOManager.readLines(languageListFile);
		HashMap<String, String> contentMap = readAll (inputFolder);
		for (int i = 0; i < lgList.size(); ++i) {
			System.out.println(outputFolder + lgList.get(i) + ".txt");
			HashMap<String, String> libSVMFormat = null;
			CorpusDataProcessing corpus = new CorpusDataProcessing();
			if (lgList.get(i).equals("en")) {
				libSVMFormat = corpus.initializeTrainingDocumentFeatures(contentMap, true, true);	
			} else {
				String tDictFile = translateDictFolder + "en" + "-" + lgList.get(i) + ".txt";
				TranslationDictionary tDict = new TranslationDictionary(tDictFile);
				HashMap<String, String> newContentMap = new HashMap<String, String>();
				for (String key : contentMap.keySet()) {
					String content = contentMap.get(key);
					String newContent = NGData.extendContent(content, tDict.dictionary);
					newContentMap.put(key, newContent);
				}
				libSVMFormat = corpus.initializeTrainingDocumentFeatures(newContentMap, true, true);
			}
			FileWriter writer = new FileWriter (outputFolder + lgList.get(i) + ".txt");
			for (String key : libSVMFormat.keySet()) {
				String[] tokens = key.split("_");
				if (tokens.length == 2)
					writer.write(tokens[1] + " " + tokens[0] + " " + libSVMFormat.get(key) + systemNewLine);
			}
			writer.close();
		}
	}
	
	public static void exportDocTranslated () throws IOException {
		String inputFolder = MultiLingualResourcesConfig.multiLingual20NGFolder;
		File folder = new File (inputFolder);
		File[] folderList = folder.listFiles();
		String outputFolder = "/shared/shelley/yqsong/data/crossLingual/libSVMFormatData/docLevel/";
		for (int i = 0; i < folderList.length; ++i) {
			if (folderList[i].isDirectory() == false)
				continue;
			
			System.out.println(outputFolder + folderList[i].getName() + ".txt");
			FileWriter writer = new FileWriter (outputFolder + folderList[i].getName() + ".txt");
			HashMap<String, String> contentMap = readAll (folderList[i].getAbsolutePath());
			CorpusDataProcessing corpus = new CorpusDataProcessing();
			HashMap<String, String> libSVMFormat = corpus.initializeTrainingDocumentFeatures(contentMap, true, true);
			for (String key : libSVMFormat.keySet()) {
				String[] tokens = key.split("_");
				if (tokens.length == 2)
					writer.write(tokens[1] + " " + tokens[0] + " " + libSVMFormat.get(key) + systemNewLine);
			}
			writer.close();
		}
	}
	
	public static HashMap<String, String> readAll (String folderString) {
		HashMap<String, String> contentMap = new HashMap<String, String>();
		File folder = new File (folderString);
		File[] listFile = folder.listFiles();
		Analyzer analyzer = AnalyzerFactory.initialize(LanguageMapping.lgMapping.get(folder.getName()));
		for (int i = 0; i < listFile.length; ++i) {
			try {
				if (listFile[i].getName().startsWith("label") == false)  {
					System.out.println(listFile[i].getAbsolutePath());
					
					String content = IOManager.readContent(listFile[i].getAbsolutePath());
					String contentNew = "";
					TokenStream stream = analyzer.tokenStream(null, content);
					CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
					stream.reset();
					while (stream.incrementToken()) {
					  contentNew += cattr.toString() + " ";
					}
					stream.end();
					stream.close();
					
					contentMap.put(listFile[i].getName(), contentNew);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return contentMap;
	}
}
