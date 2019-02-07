package edu.illinois.cs.cogcomp.embedding.esa.index;

import info.bliki.wiki.dump.WikiArticle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.compress.bzip2.CBZip2InputStream;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.descartes.util.IOManager;
import edu.illinois.cs.cogcomp.descartes.util.PageParser;
import edu.illinois.cs.cogcomp.wiki.parsing.WikiDumpFilter;
import edu.illinois.cs.cogcomp.wiki.parsing.processors.PageMeta;

/**
 * @author Yangqiu Song
 * 
 * Modified based on Vivek Srikumar's edu.illinois.cs.cogcomp.descartes.indexer.WikiDocIndexer
 * Changed to Lucene 5.3.0
 * 
 */

public class WikipediaDocumentsIndex {

	private static final String PAGE_TAG = "page";
	private static final int BUFFER_SIZE = 100;
	private static final int DOC_COUNT = 1000;

	private int lowerBoundLength;
	private int numLinks;

	private ArrayList<PageParser> arrPages;

	
	public static class Stats {
		public int numPages;
		public int numIndexed;
	}

    protected IndexWriter indexer;

	public WikipediaDocumentsIndex(String language, String indexDir, String configFile)
			throws Exception {

		if (IOManager.isDirectoryExist(indexDir)) {
		    System.out.println("The directory " + indexDir  + " already exists in the system. It will be deleted now.");
		    IOManager.deleteDirectory(indexDir);
		}
		Analyzer analyzer = AnalyzerFactory.initialize(language);
	
		File file = new File(indexDir);
		Path path = file.toPath();
		Directory dir = FSDirectory.open(path);
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		indexer = new IndexWriter(dir, config);
		
		arrPages = new ArrayList<PageParser>();

		// Read the configuration file
		PropertiesConfiguration fileCconfig = new PropertiesConfiguration(configFile);
		lowerBoundLength = fileCconfig.getInt("cogcomp.parser.lowerboundlength");
		numLinks = fileCconfig.getInt("cogcomp.parser.numoflink");

		System.out.println("Configuration:");
		System.out.println("\tlowerBoundLength = " + lowerBoundLength);
		System.out.println("\tnumOfLink = " + numLinks);
	}

	public Stats index(String fname) throws Exception {
		File file = new File(fname);

		FileInputStream fileStream = new FileInputStream(file);

		// Ugly hack because the constructor of CBZip2InputStream does not
		// like seeing the two "magic" characters at the start of the
		// stream. This is documented here:
		// http://api.dpml.net/ant/1.7.0/org/apache/tools/bzip2/CBZip2InputStream.html#CBZip2InputStream(java.io.InputStream)
		fileStream.read();
		fileStream.read();

		CBZip2InputStream bz2Stream = new CBZip2InputStream(fileStream);

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				bz2Stream));

		String line;
		boolean startPage = false;
		ArrayList<String> pageContent = new ArrayList<String>();
		int count = 0;
		int validCount = 0;
		Date start = new Date();
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.length() == 0)
				continue;

			if (("<" + PAGE_TAG + ">").equals(line)) {
				startPage = true;
			}

			if (startPage)
				pageContent.add(line);

			if (("</" + PAGE_TAG + ">").equals(line)) {
				startPage = false;
				PageParser pageParser = new PageParser();
				boolean isValid = pageParser.parse(pageContent,
						lowerBoundLength, numLinks);

				if (isValid) {
					addToBuffer(pageParser);
					validCount++;
				}

				pageContent = new ArrayList<String>();
				count++;
				if (count % DOC_COUNT == 0) {
					
					Date end = new Date();
					System.out.println("Parsed " + validCount + " out of "
							+ count + " documents with " + 
							(end.getTime() - start.getTime()) / (float) 1000 + " seconds..");
					
				}
			}
		}
		IOManager.closeReader(reader);
		bz2Stream.close();
		fileStream.close();

		if (arrPages.size() > 0) {
			outputPages();
		}

		System.out.println("Finished optimizing");
		indexer.close();
		System.out.println("Done.");

		System.out.println("Total: " + validCount + " out of " + count
				+ " documents were parsed.");
		
		Stats stats = new Stats();
		stats.numPages = count;
		stats.numIndexed = validCount;
		return stats;
	}

	private void addToBuffer(PageParser pageParser) {
		arrPages.add(pageParser);
		if (arrPages.size() == BUFFER_SIZE) {
			outputPages();
			arrPages = null;
			arrPages = new ArrayList<PageParser>();
		}
	}

	private void outputPages() {
		try {
			int n = arrPages.size();
			for (int i = 0; i < n; i++) {
				PageParser page = arrPages.get(i);
				if (page == null)
					continue;

				Document doc = new Document();
				// Id
				Field idField = new Field("id", page.getId(), TextField.TYPE_STORED);
				doc.add(idField);
				// Title
				Field titleField = new Field("title", page.getTitle(), TextField.TYPE_STORED);
				doc.add(titleField);
				// Text
				Field textField = new Field("text", page.getText(), TextField.TYPE_STORED);
				doc.add(textField);

				indexer.addDocument(doc);

			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public Stats indexWikiAPI(String fname) throws Exception {
		final AtomicInteger count = new AtomicInteger(0);
		final AtomicInteger validCount = new AtomicInteger(0);
		String bz2Filename = fname;
		try {
			System.out.println("Started dump parsing");
			// indexer.addDocument(doc);
			WikiDumpFilter filter = new WikiDumpFilter() {
				@Override
				public void processAnnotation(WikiArticle page, PageMeta meta,
						TextAnnotation ta) {
					// Do anything you want to both annotations
					count.incrementAndGet();
					// System.out.println(page.getTitle() + " " + page.getId());
					if (page.getText().length() > lowerBoundLength
							&& meta.getLinks().size() > numLinks) {

						validCount.incrementAndGet();
						try {
							Document doc = new Document();
							// Id
							Field idField = new Field("id", page.getId(), TextField.TYPE_STORED);
							doc.add(idField);
							// Title
							Field titleField = new Field("title", page.getTitle(), TextField.TYPE_STORED);
							doc.add(titleField);
							// Text
							Field textField = new Field("text", page.getText(), TextField.TYPE_STORED);
							doc.add(textField);
							
							indexer.addDocument(doc);
						} catch (IOException e) {
							System.out
									.println("Something went wrong in parsing");
							e.printStackTrace();
						}
					}
				}
				// Suppress progress output if you prefer
			};
			filter.silence();
	        // Start parsing
	        WikiDumpFilter.parseDump(bz2Filename, filter);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Stats stats = new Stats();
		stats.numPages = count.get();
		stats.numIndexed = validCount.get();
		return stats;
	}
	
}
