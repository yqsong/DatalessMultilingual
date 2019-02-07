package edu.illinois.cs.cogcomp.embedding.multiembedding;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;
import edu.illinois.cs.cogcomp.MultiLingualResourcesConfig;
import edu.illinois.cs.cogcomp.classification.flat.ted.data.TEDData;
import edu.illinois.cs.cogcomp.classification.flat.ted.data.TEDDataSplitting;
import edu.illinois.cs.cogcomp.classification.hierarchical.rcv.data.LanguageMapping;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.jlis.CorpusDataProcessing;
import edu.illinois.cs.cogcomp.classification.hierarchy.evaluation.StatUtils;
import edu.illinois.cs.cogcomp.descartes.util.IOManager;
import edu.illinois.cs.cogcomp.embedding.esa.index.AnalyzerFactory;
import edu.illinois.cs.cogcomp.embedding.esa.index.CrossLanguageWikipediaDocumentIndex;
import edu.illinois.cs.cogcomp.embedding.esa.multiesa.MultiLingalESA;

public class DataStats {

	public static void main (String args[]) throws IOException {
		
		MultiLingualResourcesConfig.initialization();

//		testTED ();
		
//		testEuroparl () ;
		
		testWiki () ;
	}
	
	public static void testWiki () throws IOException {
		
		String lgIndexFolder = MultiLingualResourcesConfig.multiLingualESAFolder;
		
		HashSet<String> allInterested = new HashSet<String>();
		for (String lg : TEDData.lgNameArray) {
			allInterested.add(lg);
		}
		for (String lgString : LanguageMapping.lgMapping.keySet()) {
			allInterested.add(LanguageMapping.lgMapping.get(lgString));
		}
		
		
		for (String lgName : allInterested) {
			lgName = "sh";
			
			if (lgName.equals("pb") == true ||
					lgName.equals("en") == true) {
				continue;
			}
			
			int lengthLg = 0;
			int lengthEn = 0;
			HashSet<String> vocabularyLg = new HashSet<String>();
			HashSet<String> vocabularyEn = new HashSet<String>();
			
			String indexFolderName = lgIndexFolder + lgName + MultiLingualResourcesConfig.multiLingualESASuffix;
			File indexDir = new File(indexFolderName);
			Directory fileDirectory = FSDirectory.open(indexDir.toPath());
			DirectoryReader ireader = DirectoryReader.open(fileDirectory);
			
			Analyzer analyzerEn = AnalyzerFactory.initialize(AnalyzerFactory.defaultAnalyzerName);
			Analyzer analyzerLg = AnalyzerFactory.initialize(lgName);

			
			int maxDoc = ireader.maxDoc();
			for (int i = 0; i < maxDoc; ++i) {
				
				if (i % 10000 == 0)
					System.out.println("Processed " + i + " documents..");
				
				Document doc = ireader.document(i);
				if (doc.getField("language").stringValue().equals("english")) {
					
					
					TokenStream stream = doc.getField(CrossLanguageWikipediaDocumentIndex.englishField).tokenStream(analyzerEn, null);
					CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
					stream.reset();
					while (stream.incrementToken()) {
						vocabularyEn.add(cattr.toString());
						lengthEn++;
					}
					stream.end();
					stream.close();
					
//					String content = doc.get(CrossLanguageWikipediaDocumentIndex.englishField);
//					String[] tokens = content.split("\\s+");
//					for (String token : tokens) {
//						vocabularyEn.add(token);
//					}
//					lengthEn += tokens.length;
				} else {
					
					TokenStream stream = doc.getField(CrossLanguageWikipediaDocumentIndex.languageField).tokenStream(analyzerLg, null);
					CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
					stream.reset();
					while (stream.incrementToken()) {
						vocabularyLg.add(cattr.toString());
						lengthLg++;
					}
					stream.end();
					stream.close();
					
					String content = doc.get(CrossLanguageWikipediaDocumentIndex.languageField);
					String[] tokens = content.split("\\s+");
//					for (String token : tokens) {
//						vocabularyEn.add(token);
//					}
//					lengthEn += tokens.length;
				}
			}
			System.out.println(lgName + "\t" + lengthLg + "\t" + vocabularyLg.size() + "\t" + lengthEn + "\t" + vocabularyEn.size());
		}
	}
	
	public static void testEuroparl () {
		try {
			for (String lgName : EmbeddingLanguages.europarl_availability.keySet()) {
				if (lgName.equals("pb") == false) {
					testEuroparlOneLg (lgName);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testEuroparlOneLg (String lgName) throws Exception {
		String upFolder = MultiLingualResourcesConfig.europarlPath;
		
		int lengthLg = 0;
		int lengthEn = 0;
		HashSet<String> vocabularyLg = new HashSet<String>();
		HashSet<String> vocabularyEn = new HashSet<String>();
		
		String lgFile = upFolder + lgName + "-en" + "." + lgName;
		String enFile = upFolder + lgName + "-en" + ".en";

		List<String> linesLg = IOManager.readLines(lgFile);
		
		for (int d = 0; d < linesLg.size(); ++d) {
			String content = linesLg.get(d);
			String[] tokens = content.split("\\s+");
			for (String token : tokens) {
				vocabularyLg.add(token);
			}
			lengthLg += tokens.length;
		}

		List<String> linesEn = IOManager.readLines(enFile);
		
		for (int d = 0; d < linesEn.size(); ++d) {
			String content = linesEn.get(d);
			String[] tokens = content.split("\\s+");
			for (String token : tokens) {
				vocabularyEn.add(token);
			}
			lengthEn += tokens.length;
		}
		
		System.out.println(lgName + "\t" + lengthLg + "\t" + vocabularyLg.size() + "\t" + lengthEn + "\t" + vocabularyEn.size());
	

	}
	
	public static void testTED () {
		try {
			for (int i = 0; i < TEDData.lgNameArray.length; ++i) {
				int lgID = i;
				testTEDOneLg (lgID);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testTEDOneLg (int lgID) throws Exception {
		String lgName = TEDData.lgNameArray[lgID];
		String upFolder = MultiLingualResourcesConfig.tedcldcPath;
		
		int lengthLg = 0;
		int lengthEn = 0;
		HashSet<String> vocabularyLg = new HashSet<String>();
		HashSet<String> vocabularyEn = new HashSet<String>();
		for (int j = 0; j < 1; ++j) {
			
			String label = TEDData.labelNameArray[j];

			String trainPathPos1 = upFolder + lgName + "-en" + "/train/" + label + "/positive/";
			String trainPathNeg1 = upFolder + lgName + "-en" + "/train/" + label + "/negative/";
			String testPathPos1 = upFolder + lgName + "-en" + "/test/" + label + "/positive/";
			String testPathNeg1 = upFolder + lgName + "-en" + "/test/" + label + "/negative/";
			

			String trainPathPos2 = upFolder + "en-" + lgName + "/train/" + label + "/positive/";
			String trainPathNeg2 = upFolder + "en-" + lgName + "/train/" + label + "/negative/";
			String testPathPos2 = upFolder + "en-" + lgName + "/test/" + label + "/positive/";
			String testPathNeg2 = upFolder + "en-" + lgName + "/test/" + label + "/negative/";

			TEDDataSplitting dataSplit1 = new TEDDataSplitting();
			dataSplit1.loadOneLanguageOneLabel(lgName, trainPathPos1, trainPathNeg1, testPathPos1, testPathNeg1, true);
			
			for (int d = 0; d < dataSplit1.labeledTrainData.size(); ++d) {
				String content = dataSplit1.labeledTrainData.get(d).getFirst();
				String[] tokens = content.split("\\s+");
				for (String token : tokens) {
					vocabularyLg.add(token);
				}
				lengthLg += tokens.length;
			}

			for (int d = 0; d < dataSplit1.labeledTestData.size(); ++d) {
				String content = dataSplit1.labeledTestData.get(d).getFirst();
				String[] tokens = content.split("\\s+");
				for (String token : tokens) {
					vocabularyLg.add(token);
				}
				lengthLg += tokens.length;
			}
			
			TEDDataSplitting dataSplit2 = new TEDDataSplitting();
			dataSplit2.loadOneLanguageOneLabel("en", trainPathPos2, trainPathNeg2, testPathPos2, testPathNeg2, true);
			
			for (int d = 0; d < dataSplit2.labeledTrainData.size(); ++d) {
				String content = dataSplit2.labeledTrainData.get(d).getFirst();
				String[] tokens = content.split("\\s+");
				for (String token : tokens) {
					vocabularyEn.add(token);
				}
				lengthEn += tokens.length;
			}

			for (int d = 0; d < dataSplit2.labeledTestData.size(); ++d) {
				String content = dataSplit2.labeledTestData.get(d).getFirst();
				String[] tokens = content.split("\\s+");
				for (String token : tokens) {
					vocabularyEn.add(token);
				}
				lengthEn += tokens.length;
			}
		}
		
		System.out.println(lgName + "\t" + lengthLg + "\t" + vocabularyLg.size() + "\t" + lengthEn + "\t" + vocabularyEn.size());
	

	}
}
