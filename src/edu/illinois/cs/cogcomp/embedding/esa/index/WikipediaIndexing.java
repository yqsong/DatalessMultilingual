package edu.illinois.cs.cogcomp.embedding.esa.index;

import java.util.Date;

import cern.jet.math.Mult;
import edu.illinois.cs.cogcomp.MultiLingualResourcesConfig;


/**
 * yqsong@illinois.edu
 */

public class WikipediaIndexing {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		MultiLingualResourcesConfig.initialization();
		
		String fname = MultiLingualResourcesConfig.lgResourceFolder;//"/shared/corpora/yqsong/data/wikipedia/crossLanguage/";
		String indexDir = MultiLingualResourcesConfig.lgIndexFolder;//"/shared/corpora/yqsong/data/wikipedia/crossLanguageIndex.5.3.0/";
		String configFile = "conf/configurations.properties";
		
		String[] fileNames = {
				"enwiki-20150805-pages-articles.xml.bz2",
		};
		
		String[] indexNames = {
				"enwiki-20150805-orginal",
		};
		
		int langId = 0;
		try {
			WikipediaDocumentsIndex indexer = new WikipediaDocumentsIndex("standard", indexDir + indexNames[langId], configFile);
			Date start = new Date();
			indexer.index(fname + fileNames[langId]);
//			indexer.indexWikiAPI(fname + fileNames[langId]);
			Date end = new Date();
			System.out.println("Done. " + (end.getTime() - start.getTime()) / (float) 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		

	}

}
