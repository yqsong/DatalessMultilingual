package edu.illinois.cs.cogcomp.embedding.esa.index;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class OptimizeIndex {

	
	public static void main (String[] args) throws IOException {
//		englishIndex ();
		languageIndex () ;
	}
	
	public static void englishIndex () throws IOException {
		String outputLangIndex = "/shared/corpora/yqsong/data/wikipedia/crossLanguageIndex.5.3.0/enwiki-20150805-orginal";
		int index = outputLangIndex.indexOf("wiki-20150805");
		
		File file = new File(outputLangIndex);
		Path path = file.toPath();
		Directory outputDir = FSDirectory.open(path);
		
		String languageName = outputLangIndex.substring(index - 2, index);
		
		IndexWriterConfig config = new IndexWriterConfig(CrossLanguageWikipediaDocumentIndex.standardAnalyzer);
		IndexWriter indexWriter = new IndexWriter(outputDir, config);
		
		System.out.println("Merge index for: " + languageName);
		
		indexWriter.forceMerge(1);
		
		System.out.println("Done." );
	}
	
	public static void languageIndex () throws IOException {
		String indexDir = "/shared/corpora/yqsong/data/wikipedia/crossLanguageIndex.5.3.0/original/";
		File dir = new File (indexDir);
		File[] fileList = dir.listFiles();
		for (int i = 0; i < fileList.length; ++i) {
			String outputLangIndex = fileList[i].toString();
			int index = outputLangIndex.indexOf("wiki-20150826");
			
			File file = new File(outputLangIndex);
			Path path = file.toPath();
			Directory outputDir = FSDirectory.open(path);
			
			String languageName = outputLangIndex.substring(index - 2, index);
			
			Map<String, Analyzer> analyzerMap = new HashMap<String, Analyzer>();
			Analyzer lgAnalyzer = AnalyzerFactory.initialize(languageName);
			
			
			analyzerMap.put(CrossLanguageWikipediaDocumentIndex.englishTitle, CrossLanguageWikipediaDocumentIndex.enAnalyzer);
			analyzerMap.put(CrossLanguageWikipediaDocumentIndex.languageTitle, lgAnalyzer);
			analyzerMap.put(CrossLanguageWikipediaDocumentIndex.englishField, CrossLanguageWikipediaDocumentIndex.enAnalyzer);
			analyzerMap.put(CrossLanguageWikipediaDocumentIndex.languageField, lgAnalyzer);
			PerFieldAnalyzerWrapper wrapper = new PerFieldAnalyzerWrapper(new WhitespaceAnalyzer(), analyzerMap);
			
			IndexWriterConfig config = new IndexWriterConfig(wrapper);
			IndexWriter indexWriter = new IndexWriter(outputDir, config);
			
			System.out.println("Merge index for: " + languageName);
			
			indexWriter.forceMerge(1);
			
			System.out.println("Done." );
		}
	}
	
}
