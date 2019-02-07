package edu.illinois.cs.cogcomp.embedding.esa.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.illinois.cs.cogcomp.MultiLingualResourcesConfig;
import edu.illinois.cs.cogcomp.descartes.util.IOManager;

public class CrossLanguageWikipediaDocumentIndex {
	public static String englishField = "englishtext";
	public static String languageField = "languagetext";
	public static String englishTitle = "title";
	public static String languageTitle = "languagetitle";
	
//	public static Analyzer enAnalyzer = AnalyzerFactory.initialize("en");
	public static Analyzer enAnalyzer = AnalyzerFactory.initialize("standard");
	public static Analyzer standardAnalyzer = AnalyzerFactory.initialize("standard");
	
	public static void main (String[] args) {
		MultiLingualResourcesConfig.initialization();
		
		String enIndex = MultiLingualResourcesConfig.enIndex;
		String enIdTitleMappingFile = MultiLingualResourcesConfig.enIdTitleMappingFile;
		String lgResourceFolder = MultiLingualResourcesConfig.lgResourceFolder;
		String lgIndexFolder = MultiLingualResourcesConfig.lgIndexFolder;
		String langLinkFile = MultiLingualResourcesConfig.langLinkFile;
		String wikiURLs = MultiLingualResourcesConfig.wikiURLs;
		
		List<String> urlList = IOManager.readLines(wikiURLs);
		
		List<String> languages = new ArrayList<String>();
		List<Integer> originalPages = new ArrayList<Integer>();
		List<Integer> indexedPages = new ArrayList<Integer>();
		List<Integer> mergedPages = new ArrayList<Integer>();
		
		int foldNum = 5;
		int selectedFold = Integer.parseInt(args[0]);
		File lgLinkFolder = new File(lgIndexFolder + "langLinks/");
		if (lgLinkFolder.exists() == false) {
			lgLinkFolder.mkdirs();
		}
		
		for (int i = 0; i < urlList.size(); ++i) {
			
			if (i % foldNum == selectedFold) {
				String url = urlList.get(i);
				String[] tokens = url.split("\\/");
				String fileName = tokens[5];
				String lg = fileName.substring(0, fileName.indexOf("wiki-2015"));
				
//				if (lg.equals("sh") == false)
//					continue;
				
				String lgLinkFilename = lgIndexFolder + "langLinks/" + lg + "_langlink.txt";
				File lgLinkFile = new File(lgLinkFilename);
				if (lgLinkFile.exists() == false) {
					ReadLangLinkingFile.readLangLinking (langLinkFile, lgIndexFolder + "langLinks/", lg);
				}
				
				String inputFile = lgResourceFolder + fileName;
				String lgIndex = lgIndexFolder + "original/" + tokens[3] + "-" + tokens[4] + "-original";
				WikipediaDocumentsIndex.Stats statsOrg = new WikipediaDocumentsIndex.Stats();
				try {
					String configFile = "conf/configurations.properties";
					WikipediaDocumentsIndex indexer = new WikipediaDocumentsIndex(lg, lgIndex, configFile);
					Date start = new Date();
					statsOrg = indexer.index(inputFile);
					Date end = new Date();
					System.out.println("Index " + lg + " Done. " + (end.getTime() - start.getTime())
							/ (float) 1000);
					System.out.println();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				String mergedIndex = lgIndexFolder + "merged_StandardAnalyzer/" + tokens[3] + "-" + tokens[4] + "-merged";
				CrossLanguageWikipediaDocumentIndex mergeIndex = new CrossLanguageWikipediaDocumentIndex();
				CrossLanguageWikipediaDocumentIndex.Stats statsMerge = mergeIndex.mergeIndex (enIndex, enIdTitleMappingFile, lg, lgIndex, lgLinkFilename, mergedIndex);
				
				languages.add(lg);
				originalPages.add(statsOrg.numPages);
				indexedPages.add(statsOrg.numIndexed);
				mergedPages.add(statsMerge.numMerged);
			}

		}
		
		for (int i = 0; i < languages.size(); ++i) {
			System.out.println(languages.get(i) + "\t" + originalPages.get(i) + "\t" + mergedPages.get(i));
		}
	
		
	}
	
	public class Stats {
		int numEnPages;
		int numLgPages;
		int numMerged;
	}

	public Stats mergeIndex (String englishIndex, String enIdTitleMappingFile, String languageName,
			String languageIndex, String langLinkFile, String outputLangIndex) {
		
		Stats stats = new Stats();
		
		try {
			if (IOManager.isDirectoryExist(outputLangIndex)) {
			    System.out.println("The directory " + outputLangIndex
				    + " already exists in the system. "
				    + "It will be deleted now.");
			    IOManager.deleteDirectory(outputLangIndex);
			}
			
			HashMap<String, String> enIdTitleMapping =  enWikiIdTitleMapping (enIdTitleMappingFile);
			
			File file = new File(outputLangIndex);
			Path path = file.toPath();
			Directory outputDir = FSDirectory.open(path);
			
			Map<String, Analyzer> analyzerMap = new HashMap<String, Analyzer>();
			Analyzer lgAnalyzer = AnalyzerFactory.initialize(languageName);
			
			
			analyzerMap.put(englishTitle, enAnalyzer);
			analyzerMap.put(languageTitle, lgAnalyzer);
			analyzerMap.put(englishField, enAnalyzer);
			analyzerMap.put(languageField, lgAnalyzer);
			PerFieldAnalyzerWrapper wrapper = new PerFieldAnalyzerWrapper(new WhitespaceAnalyzer(), analyzerMap);
			
			IndexWriterConfig config = new IndexWriterConfig(wrapper);
			IndexWriter indexWriter = new IndexWriter(outputDir, config);
			
			HashMap<String, String> langLink = loadLangLink (langLinkFile);
			HashMap<String, String> langInverseLink = new HashMap<String, String>();
			HashMap<String, String> langLinkNew = new HashMap<String, String>();

			Directory endir = FSDirectory.open(new File(englishIndex).toPath());
			DirectoryReader enReader = DirectoryReader.open(endir);
			
			Directory lgdir = FSDirectory.open(new File(languageIndex).toPath());
			DirectoryReader lgReader = DirectoryReader.open(lgdir);
			
		    Calendar cal = Calendar.getInstance();
		    long startTime = cal.getTimeInMillis();
		    
		    int maxLgNum = lgReader.maxDoc();
		    int lgCount = 0;
		    for(int i = 0; i < maxLgNum; i++){
		    	if (i % 100000 == 0) {
		    		System.out.println("[" + languageIndex + "] Processed: " + i + " out of " + maxLgNum + " documents; selected " + lgCount);
		    		
		    		Calendar cal1 = Calendar.getInstance();
		    		long endTime = cal1.getTimeInMillis();
		    		long second = (endTime - startTime)/1000;
		    		System.out.println("Elipsed time: " + second + " seconds");
		    	}

	    		try {
	    			String lgtitle = lgReader.document(i).getField("title").stringValue().trim(); 		
		    		if (langLink.containsKey(lgtitle)) {
		    			String id = lgReader.document(i).getField("id").stringValue().trim(); 
		    			String text = lgReader.document(i).getField("text").stringValue().trim(); 
		    			String entitle = enIdTitleMapping.get(langLink.get(lgtitle));
		    			
		    			if (entitle != null && entitle.trim().equals("") == false) {
			    			langInverseLink.put(enIdTitleMapping.get(langLink.get(lgtitle)), lgtitle);
			    			lgCount++;
		    			}
		    			
		    		} else {
//		    			System.out.println(title);
//		    			System.out.println();
		    		}
	    		}
	    		catch(Exception e){
	    			e.printStackTrace();
	    		}
		    		
		    }
			
		    
		    int maxEnNum = enReader.maxDoc();
		    int enCount = 0;
		    for(int i = 0; i < maxEnNum; i++){
		    	if (i % 100000 == 0) {
		    		System.out.println("[" + englishIndex + "] Processed: " + i + " out of " + maxEnNum + " documents; selected " + enCount);
		    		
		    		Calendar cal1 = Calendar.getInstance();
		    		long endTime = cal1.getTimeInMillis();
		    		long second = (endTime - startTime)/1000;
		    		System.out.println("Elipsed time: " + second + " seconds");
		    	}

	    		try {
	    			String id = enReader.document(i).getField("id").stringValue();
		    		String title = enReader.document(i).getField("title").stringValue().trim();
		    		String text = enReader.document(i).getField("text").stringValue().trim(); 
		    		if (langInverseLink.containsKey(title)) {
		    			langLinkNew.put(langInverseLink.get(title), title);
		    			
		    			Document doc = new Document();
						// Id
						Field idField = new Field("id", id, TextField.TYPE_STORED);
						doc.add(idField);
						// Id
						Field lgField = new Field("language", "english", TextField.TYPE_STORED);
						doc.add(lgField);
						// Title
						Field titleField = new Field(englishTitle, title, TextField.TYPE_STORED);
						doc.add(titleField);
						// Text
						Field textField = new Field(englishField, text, TextField.TYPE_STORED);
						doc.add(textField);
						
		    			indexWriter.addDocument(doc);
		    			enCount++;
		    		}
	    		}
	    		catch(Exception e){
	    			e.printStackTrace();
	    		}
		    }
		    
		    int lgCountNew = 0;
		    for(int i = 0; i < maxLgNum; i++){
		    	if (i % 100000 == 0) {
		    		System.out.println("[" + languageIndex + "] Processed: " + i + " out of " + maxLgNum + " documents; selected " + lgCountNew);
		    		
		    		Calendar cal1 = Calendar.getInstance();
		    		long endTime = cal1.getTimeInMillis();
		    		long second = (endTime - startTime)/1000;
		    		System.out.println("Elipsed time: " + second + " seconds");
		    	}

	    		try {
	    			String lgtitle = lgReader.document(i).getField("title").stringValue().trim(); 	
	    			
		    		if (langLinkNew.containsKey(lgtitle)) {
		    			if (languageName.equals("sh")) {
		    				lgtitle = LanguageTextNormalization.normalizeSerbian(lgtitle);
		    			}
		    			String id = lgReader.document(i).getField("id").stringValue().trim(); 
		    			String text = lgReader.document(i).getField("text").stringValue().trim(); 
		    			if (languageName.equals("sh")) {
		    				text = LanguageTextNormalization.normalizeSerbian(text);
		    			}
		    			String entitle = enIdTitleMapping.get(langLink.get(lgtitle));
		    			
		    			if (entitle != null && entitle.trim().equals("") == false) {
		    				Document doc = new Document();
							// Id
							Field idField = new Field("id", id, TextField.TYPE_STORED);
							doc.add(idField);
							// Id
							Field lgField = new Field("language", "other", TextField.TYPE_STORED);
							doc.add(lgField);
							// Title
							Field titleField = new Field(englishTitle, entitle, TextField.TYPE_STORED);
							doc.add(titleField);
							Field titleFieldOwn = new Field(languageTitle, lgtitle, TextField.TYPE_STORED);
							doc.add(titleFieldOwn);
							// Text
							Field textField = new Field(languageField, text, TextField.TYPE_STORED);
							doc.add(textField);
			    			
			    			indexWriter.addDocument(doc);
			    			langInverseLink.put(enIdTitleMapping.get(langLink.get(lgtitle)), lgtitle);
			    			lgCountNew++;
		    			}
		    			
		    		} else {
//		    			System.out.println(title);
//		    			System.out.println();
		    		}
	    		}
	    		catch(Exception e){
	    			e.printStackTrace();
	    		}
		    		
		    }
		    System.out.println("[Found " + languageName + " pages linked] " + lgCount + " out of " + maxLgNum + " pages");
		    System.out.println("[Found en pages linked] " + enCount + " out of " + maxEnNum + " pages");
		    System.out.println("[Found " + languageName + " new pages linked] " + lgCountNew + " out of " + maxLgNum + " pages");
		    
	    	indexWriter.close();
	    	System.out.println("Merge Index Finished");
	    	System.out.println();
	    	System.out.println();
	    	
			stats.numEnPages = maxEnNum;
			stats.numLgPages = maxLgNum;
			stats.numMerged = lgCountNew;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return stats;
	}
	

	
	public static HashMap<String, String> loadLangLink (String langLinkFile) {
		HashMap<String, String> langLink = new HashMap<String, String> ();
		
		List<String> lines = IOManager.readLines(langLinkFile);
		
		for (String line : lines) {
			String[] tokens = line.split(",");
			if (tokens.length == 3) {
				String enID = tokens[0];
				String lgTitle = tokens[2];
				langLink.put(lgTitle, enID);
			}
		}
		
		System.out.println("[LanglinkNum] " + lines.size());
		
		return langLink;
	}
	
	public static HashMap<String, String> enWikiIdTitleMapping (String fileName) {
		HashMap<String, String> pageIdTitleMapping = new HashMap<String, String>();
		try {
			File mappingFile = new File (fileName);
			System.out.println("Read mapping file: " + mappingFile.getAbsolutePath());
			FileReader mappReader = new FileReader(mappingFile);
			BufferedReader bf = new BufferedReader(mappReader);
			String line = "";
			while ((line = bf.readLine()) != null) {
				if (line.equals("") == true) 
					continue;
				String[] tokens = line.split("\t");
				if (tokens.length != 2)
					continue;
				if (pageIdTitleMapping.containsKey(tokens[0].trim()) == false) {
					pageIdTitleMapping.put(tokens[0], tokens[1]);
				}
			}
			System.out.println("Done.");
		} catch (Exception e) {
			
		}
		return pageIdTitleMapping;
	}
	
}
