package edu.illinois.cs.cogcomp;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class MultiLingualResourcesConfig {
	
	public static boolean isInitialized = false;
	
	public static String europarlPath;
	public static String tedcldcPath;
	public static String bicvmTEDPath;
	public static String bicvmEuroparlPath;	
	public static String musePath;
	public static String babylonPath;
	public static String word2vecPath;
	public static int word2vecDim;
	public static String rcv1v2Training;
	public static String rcv1ZipData1;
	public static String rcv1ZipData2;
	public static String rcv1DataProcessed;
	public static String rcv1Hierarchy;
	public static String rcv1Topics;
	public static String rcv2DataFolder;
	public static String multiLingualOrgESAFolder;
	public static String multiLingualOrgESASuffix;
	public static String multiLingualESAFolder;
	public static String multiLingualESASuffix;
	public static String englishESA530IndexFolder;
	public static String multiLingual20NGFolder;
	public static String multiLingual20NGEnBackFolder;
	public static String multiLingual20NGAllFolder;
	public static String multiLingual20NGEnBackAllFolder;
	public static String ngOriginalFolder;
	
	public static String enIndex;
	public static String enIdTitleMappingFile;
	public static String lgResourceFolder;
	public static String lgIndexFolder;
	public static String langLinkFile;
	public static String wikiURLs;
	
	public static String hausaDictionary;
	public static String hausaDictionaryFolderFromTranslation;
	public static String turkishDictionaryFolderFromTranslation;
	public static String multiLingualDictionaryFolderFromTranslation;
	public static String multiLingualDictionaryFolderFromTranslationAll;

	public static String languageFeatures;
	public static String languageCodes;
	public static String languageWikiSize;
	public static String lowResourceLanguageList;
	public static String allLanguageList;
	public static String languageGoolgeTranslateSupportList;
	
	public static String bridgedLanguageResults;

	public static void initialization () {
		isInitialized = true;
		
		// Read the configuration file
		String configFile = "conf/multilingual-config-hkust.properties";
		PropertiesConfiguration config = null;
		try {
			config = new PropertiesConfiguration(configFile);
		} catch (ConfigurationException e1) {
			e1.printStackTrace();
		}
		
		europarlPath = config.getString("europarl_Path", "/shared/shelley/yqsong/representation_original_tool/multilingual/europarl/extracted/europarl-v7.");
		
		tedcldcPath = config.getString("tedcldc_Path", "/shared/shelley/yqsong/data/tedcldc/ted-cldc/");

		bicvmTEDPath = config.getString("bicvm_ted_Path", "/shared/shelley/yqsong/representation_original_tool/multilingual/bicvm_ted/embedding/");
		
		bicvmEuroparlPath = config.getString("bicvm_europarl_Path", "/shared/shelley/yqsong/representation_original_tool/multilingual/bicvm_europarl/embedding/");
		
		babylonPath  = config.getString("babylon_Path", "/home/data/corpora/bi-embedding-babylon78/"); //babylon_Path
		musePath  = config.getString("muse_Path", "/home/data/corpora/bi-embedding-muse/"); //muse_Path
		
		word2vecPath = config.getString("word2vec_Path", "/shared/shelley/yqsong/representation_original_tool/word2vector-convertor/Skipgram-wiki/vectors-enwikitext_vivek128_skipgram.txt");
		
		word2vecDim = Integer.parseInt(config.getString("word2vec_Dim", "128"));
		
		rcv1v2Training = config.getString("rcv1v2_Training", "data/rcvTest/lyrl2004_tokens_train.dat");
		
		rcv1ZipData1 = config.getString("rcv1_ZipData1", "/shared/shelley/yqsong/benchmark/ReutersCorpusVolume1/Data/ReutersCorpusVolume1_Original/CD1");
		rcv1ZipData2 = config.getString("rcv1_ZipData2", "/shared/shelley/yqsong/benchmark/ReutersCorpusVolume1/Data/ReutersCorpusVolume1_Original/CD2");
		
		rcv1DataProcessed = config.getString("rcv1_Data_Processed", "/home/data/corpora/rcv/ReutersCorpusVolume1/rcvOrgTrainContent.txt");
		
		rcv1Hierarchy = config.getString("rcv1_Hierarchy", "data/rcvTest/rcv1.topics.hier.orig");
		
		rcv1Topics = config.getString("rcv1_Topics", "data/rcvTest/topics.rbb");
		
		rcv2DataFolder = config.getString("rcv2_Data_Folder", "/home/data/corpora/rcv2/RCV2_Multilingual_Corpus/");
		
		multiLingualOrgESAFolder = config.getString("multi_Lingual_OrgESA_Folder", "/shared/corpora/yqsong/data/wikipedia/crossLanguageIndex.5.3.0/original/");
		
		multiLingualOrgESASuffix = config.getString("multi_Lingual_OrgESA_Suffix", "wiki-20150826-original");

		multiLingualESAFolder = config.getString("multi_Lingual_ESA_Folder", "/shared/corpora/yqsong/data/wikipedia/crossLanguageIndex.5.3.0/merged_StandardAnalyzer/");
		
		multiLingualESASuffix = config.getString("multi_Lingual_ESA_Suffix", "wiki-20150826-merged");
		
		englishESA530IndexFolder = config.getString("english_5_3_0_IndexFolder", "/shared/corpora/yqsong/data/wikipedia/crossLanguageIndex.5.3.0/enwiki-20150805-orginal");
		
		multiLingual20NGFolder = config.getString("multi_Lingual_20NG_Folder", "/home/data/corpora/20-newsgroups/multilingual-20ng/");
		multiLingual20NGEnBackFolder = config.getString("multi_Lingual_20NG_EnBack_Folder", "/shared/shelley/yqsong/data/crossLingual/20NG-translate-back/");

		multiLingual20NGAllFolder = config.getString("multi_Lingual_20NG_Folder_All", "/shared/shelley/yqsong/data/crossLingual/20NG-translate-All/");
		multiLingual20NGEnBackAllFolder = config.getString("multi_Lingual_20NG_EnBack_Folder_all", "/shared/shelley/yqsong/data/crossLingual/20NG-translate-back-All/");
		
		ngOriginalFolder = config.getString("ng_Originl_Folder", "/shared/bronte/hpeng7/20news-18828/");
		
		enIndex = config.getString("enIndex", "/shared/corpora/yqsong/data/wikipedia/crossLanguageIndex.5.3.0/enwiki-20150805-orginal");
		enIdTitleMappingFile = config.getString("enIdTitleMappingFile", "/shared/shelley/yqsong/data/wikipedia/wiki_structured/wikiPageIDMapping.txt");
		lgResourceFolder = config.getString("lgResourceFolder", "/shared/corpora/yqsong/data/wikipedia/crossLanguage/");
		lgIndexFolder = config.getString("lgIndexFolder", "/shared/corpora/yqsong/data/wikipedia/crossLanguageIndex.5.3.0/");
		langLinkFile = config.getString("langLinkFile", "/shared/corpora/yqsong/data/wikipedia/crossLanguage/enwiki-20150805-langlinks.sql");
		wikiURLs = config.getString("wikiURLs", "/shared/corpora/yqsong/data/wikipedia/crossLanguage/wikiurls.txt");
		
		hausaDictionary = config.getString("hausa_dictionary", "data/dicts/hausa_dictionaryofhaus01robiuoft_manual.txt");
		
		hausaDictionaryFolderFromTranslation = config.getString("hausa_dictionary_folder_from_translation", "/shared/shelley/yqsong/data/crossLingual/dict-20ng/hausa/");
		turkishDictionaryFolderFromTranslation = config.getString("turkish_dictionary_folder_from_translation", "/shared/shelley/yqsong/data/crossLingual/dict-20ng/turkish/");
		
		multiLingualDictionaryFolderFromTranslation = config.getString("multi_lingual_dictionary_folder_from_translation", "/shared/shelley/yqsong/data/crossLingual/dict-20ng/");
		multiLingualDictionaryFolderFromTranslationAll = config.getString("multi_lingual_dictionary_folder_from_translation_all", "/shared/shelley/yqsong/data/crossLingual/dict-20ng-all/");
		
		languageFeatures = config.getString("language_features", "data/wals/language.csv");
		languageCodes = config.getString("language_codes", "data/wals/language-codes.csv");
		languageWikiSize = config.getString("language_wiki_size", "data/wals/language-wiki-size.txt");
		
		lowResourceLanguageList = config.getString("low_resource_language_list", "data/low-resource-languages.txt");
		allLanguageList = config.getString("all_language_list", "data/languagelist_with_en.txt");
		
		bridgedLanguageResults = config.getString("bridged_language_list", "matlab/lrlSummary_new.txt");
					
		languageGoolgeTranslateSupportList = config.getString("all_language_google_translate_support_list", "/shared/shelley/yqsong/data/crossLingual/google-translate-support-languages.txt");
		System.out.println("Multilingual Configuration Done.");
	}
	
	

}
