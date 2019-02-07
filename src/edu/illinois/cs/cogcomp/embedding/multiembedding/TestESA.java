package edu.illinois.cs.cogcomp.embedding.multiembedding;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.sr.SerbianNormalizationFilter;
import org.apache.lucene.analysis.sr.SerbianNormalizationFilterFactory;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import edu.illinois.cs.cogcomp.MultiLingualResourcesConfig;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.ConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.HashSort;
import edu.illinois.cs.cogcomp.classification.main.DatalessResourcesConfig;
import edu.illinois.cs.cogcomp.classification.newsgroups.TestHierarchicalClassification;
import edu.illinois.cs.cogcomp.classification.representation.esa.simple.SimpleESALocal;
import edu.illinois.cs.cogcomp.embedding.esa.index.AnalyzerFactory;
import edu.illinois.cs.cogcomp.embedding.esa.index.CrossLanguageWikipediaDocumentIndex;
import edu.illinois.cs.cogcomp.embedding.esa.multiesa.MultiLingalESA;

public class TestESA {
	
	public static void main (String[] args) throws Exception {
//		String text = "кошарка";
//		Analyzer analyzer = AnalyzerFactory.initialize("sh");
//		SerbianNormalizationFilter shFac = new SerbianNormalizationFilter(analyzer.tokenStream("text", text));
//		CharTermAttribute cattr = shFac.addAttribute(CharTermAttribute.class);
//		shFac.reset();
//		String textNew = "";
//		while (shFac.incrementToken()) {
//			System.out.println(cattr.toString());
//			textNew += cattr.toString() + " ";
//		}
//		shFac.end();
//		shFac.close();
//		test ( text, "sr");//serbian
		
//		String enText = "earthquake";
//		test ( "girgizar kasa", enText, "ha");//hausa
//		test ( "газар хөдлөлт", enText, "mn");//mongolian
//		test ( "地震", enText, "ja");
//		test ( "地震", enText, "zh");
//		test ( "tremblement de terre", enText, "fr");
//		test ( "زلزال", enText, "ar");
//		test ( "terremoto", enText, "es");
//		test ( "jordskælv", enText, "da");
//		test ( "רעידת אדמה",  enText, "he");//greek
//		test ( "deprem", enText, "tr");
//		test ( "землетрясение", enText, "ru");
//		test ( "भूकंप",  enText, "hi");
		
		String enText = "machine learning";
		test ( "машин сургалтын", enText, "mn");//mongolian

		
	}
	public static void test (String text, String enText, String lgName) throws Exception {
		MultiLingualResourcesConfig.initialization();
		String lgIndexFolder = MultiLingualResourcesConfig.multiLingualESAFolder;//"/shared/corpora/yqsong/data/wikipedia/crossLanguageIndex.5.3.0/merged_StandardAnalyzer/";
		String indexFolderName = lgIndexFolder + lgName + MultiLingualResourcesConfig.multiLingualESASuffix;
		MultiLingalESA esa = new MultiLingalESA(lgName, indexFolderName);
		
		String testEn = enText;
		List<ConceptData> conceptsEn = esa.retrieveConcepts(testEn, 500);
		String testLg = text;
		List<ConceptData> conceptsLg = esa.retrieveConcepts(testLg, 500);
		
		HashMap<String, Double> concept1 = new HashMap<String, Double>();  
		HashMap<String, Double> concept2 = new HashMap<String, Double>();  
//		System.out.println("------------English------------");
		for (int i = 0; i < conceptsEn.size(); ++i) {
//			System.out.println(conceptsEn.get(i).concept + "\t" + conceptsEn.get(i).score);
			concept1.put(conceptsEn.get(i).concept, conceptsEn.get(i).score);
		}
		System.out.println("------------" + lgName + "------------");
		for (int i = 0; i < conceptsLg.size(); ++i) {
			System.out.println(conceptsLg.get(i).concept + "\t" + conceptsLg.get(i).score);
			concept2.put(conceptsLg.get(i).concept, conceptsLg.get(i).score);
		}
		HashSet<String> concept = new HashSet<String>();
		concept.addAll(concept1.keySet());
		concept.retainAll(concept2.keySet());
		HashMap<String, Double> conceptMap = new HashMap<String, Double>();
		for (String c : concept) {
			double s1 = concept1.get(c);
			double s2 = concept2.get(c);
			double s = s1 * s2;
			conceptMap.put(c, s);
		}
		
		TreeMap<String, Double> sortedMap = HashSort.sortByValues(conceptMap);
		int count = 0;
		for (String c : sortedMap.keySet()) {
			System.out.println(c);
			if (count++ > 5) 
				break;
		}
	}
}
