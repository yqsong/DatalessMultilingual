/**
 * 
 */
package edu.illinois.cs.cogcomp.embedding.esa.search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.cs.cogcomp.descartes.retrieval.IResult;
import edu.illinois.cs.cogcomp.descartes.retrieval.simple.Result;

/**
 * @author Vivek Srikumar Modified by Yangqiu Song
 * 
 */
public class SearcherNew {
	protected IndexSearcher searcher;
	protected Directory memoryDirectory;
	
	protected Analyzer analyzer;
	protected Version analyzerVersion = Version.LUCENE_5_3_0;

	protected String[] fields;
	protected boolean addNGrams;

	private Logger log = LoggerFactory.getLogger(SearcherNew.class);

	public SearcherNew(String[] fields, boolean ngrams, Set<String> stopWords, Analyzer specifiedAnalyzer) {

		this.fields = fields;

		this.addNGrams = ngrams;

		this.analyzer = specifiedAnalyzer;
		
	}
	
	/**
	 * Opens the index created by the Lucene indexer
	 */
	public void open(String indexDirectory) throws IOException {

		File indexDir = new File(indexDirectory);
		

		
		if (!indexDir.exists() || !indexDir.isDirectory()) {
			throw new IOException(indexDir
					+ "does not exist or is not a directory");
		}
		Directory fileDirectory = FSDirectory.open(indexDir.toPath());
		DirectoryReader ireader = DirectoryReader.open(fileDirectory);
		
		searcher = new IndexSearcher(ireader);
		
		Similarity sim = new DefaultSimilarity();
//		Similarity sim = new UnNormalizedLuceneSimilarity();
		searcher.setSimilarity(sim);

		log.info("Opened index located at " + indexDirectory);

	}

	/**
	 * Searches the index for the query and returns an ArrayList of results
	 */
	public ArrayList<IResult> search(String queryText, int numResults)
			throws Exception {

		ArrayList<IResult> results = new ArrayList<IResult>();

		if (queryText.replaceAll("\\s+", "").length() == 0)
			return results;

		Query query = makeQuery(queryText);

		TopDocs searchResults = searcher.search(query, numResults);

		for (int i = 0; i < searchResults.scoreDocs.length; i++) {
			Document doc = searcher.doc(searchResults.scoreDocs[i].doc);

			results.add(new Result(Integer
					.toString(searchResults.scoreDocs[i].doc),
					doc.get("title"), doc.get("text"),
					searchResults.scoreDocs[i].score));

		}

		return results;
	}
	
	/**
	 * @param queryText
	 * @return
	 * @throws ParseException
	 */
	protected Query makeQuery(String queryText) throws Exception {
		queryText = queryText.replaceAll("-", " ");
		queryText = queryText.replaceAll("\"", "");
		queryText = queryText.replaceAll("\'", "");

		String finalQuery = queryText;

		Query query = null;
		BooleanQuery.setMaxClauseCount(50000);
		if (fields.length == 1) {
			QueryParser parser = new QueryParser(fields[0], analyzer);
			query = parser.parse(QueryParser.escape(finalQuery));
		} else {
			MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer);
			query = parser.parse(QueryParser.escape(finalQuery));
		}

//		BooleanQuery.setMaxClauseCount(30000);
//		BooleanQuery bq = new BooleanQuery();
//		bq.add(query, BooleanClause.Occur.SHOULD);
//		return bq;
		return query;
	}

	public void close() throws IOException {
		searcher.getIndexReader().close();
	}

}
