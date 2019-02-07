package edu.illinois.cs.cogcomp.embedding.esa.index;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.sr.SerbianNormalizationFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class LanguageTextNormalization {
	public static Analyzer analyzer = AnalyzerFactory.initialize("standard");

	public static String normalizeSerbian (String text) throws IOException {
		SerbianNormalizationFilter shFac = new SerbianNormalizationFilter(analyzer.tokenStream("text", text));
		CharTermAttribute cattr = shFac.addAttribute(CharTermAttribute.class);
		shFac.reset();
		String textNew = "";
		while (shFac.incrementToken()) {
			textNew += cattr.toString() + " ";
		}
		shFac.end();
		shFac.close();
		return textNew.trim();
	}
}
