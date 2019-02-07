package edu.illinois.cs.cogcomp.embedding.esa.multiesa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;

import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.ConceptData;
import edu.illinois.cs.cogcomp.classification.main.DatalessResourcesConfig;
import edu.illinois.cs.cogcomp.classification.representation.QueryPreProcessor;
import edu.illinois.cs.cogcomp.classification.representation.esa.AbstractESA;
import edu.illinois.cs.cogcomp.descartes.retrieval.IResult;
import edu.illinois.cs.cogcomp.embedding.esa.index.AnalyzerFactory;
import edu.illinois.cs.cogcomp.embedding.esa.index.CrossLanguageWikipediaDocumentIndex;
import edu.illinois.cs.cogcomp.embedding.esa.search.SearcherNew;

public class MultiLingalESA extends AbstractESA {

	SearcherNew searcher;

	public MultiLingalESA (String languageName, String[] fieldNames, String indexFile) {
		
		Set<String> stopwordSet = DatalessResourcesConfig.stopwordSet;
		
		searcher = new SearcherNew(fieldNames, false, stopwordSet, CrossLanguageWikipediaDocumentIndex.standardAnalyzer);
		
		try {
			searcher.open(indexFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Done.");
	}
	
	public MultiLingalESA (String languageName, String indexFile) {
		
		String[] fieldNames = new String[] {
				CrossLanguageWikipediaDocumentIndex.englishField,
				CrossLanguageWikipediaDocumentIndex.languageField,
				CrossLanguageWikipediaDocumentIndex.englishTitle,
				CrossLanguageWikipediaDocumentIndex.languageTitle};
		
		Set<String> stopwordSet = DatalessResourcesConfig.stopwordSet;
		
		Map<String, Analyzer> analyzerMap = new HashMap<String, Analyzer>();
		Analyzer lgAnalyzer = AnalyzerFactory.initialize(languageName);
		Analyzer enAnalyzer = CrossLanguageWikipediaDocumentIndex.enAnalyzer;
		
		analyzerMap.put(CrossLanguageWikipediaDocumentIndex.englishField, enAnalyzer);
		analyzerMap.put(CrossLanguageWikipediaDocumentIndex.languageField, lgAnalyzer);
		
		analyzerMap.put(CrossLanguageWikipediaDocumentIndex.englishTitle, enAnalyzer);
		analyzerMap.put(CrossLanguageWikipediaDocumentIndex.languageTitle, lgAnalyzer);
		
		PerFieldAnalyzerWrapper wrapper = new PerFieldAnalyzerWrapper(CrossLanguageWikipediaDocumentIndex.standardAnalyzer, analyzerMap);
		
		searcher = new SearcherNew(fieldNames, false, stopwordSet, wrapper);
		
		try {
			searcher.open(indexFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Done.");
	}
	
	@Override
	public List<ConceptData> retrieveConcepts(String document, int numConcepts,
			String complexVectorType) throws Exception {
		document = QueryPreProcessor.process(document);

		List<ConceptData> concepts = new ArrayList<ConceptData>();
		
		int count = 0;
		StringBuffer sb = new StringBuffer();
		for (String s : document.split("\\s")) {
			sb.append(s + " ");
			count++;
		}
		
		ArrayList<IResult> search = null;
		try {
			search = searcher.search(document, numConcepts);
		} catch (Exception e) {
			throw e;
		}
		if (search != null) {
			for (IResult res : search) {
				concepts.add(new ConceptData(res.getTitle().replaceAll(",", "").replaceAll(";", "").replaceAll("\t", ""), res.getScore()));
			}
		}

		return concepts;

	}

	@Override
	public List<ConceptData> retrieveConcepts(String document, int numConcepts)
			throws Exception {
		return retrieveConcepts(document, numConcepts, "");
	}

}
