package edu.illinois.cs.cogcomp.classification.newsgroups.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.descartes.util.IOManager;
import edu.illinois.cs.cogcomp.embedding.esa.index.AnalyzerFactory;
import edu.illinois.cs.cogcomp.embedding.esa.index.LanguageTextNormalization;

public class NGData {
	
	public String lgName;
	public String lgLabelPath;
	public List<String> labels = null;
	public List<String> ids = new ArrayList<String>();
	public List<Pair<String, String>> labeledContent = new ArrayList<Pair<String, String>>(); 
	
	public static NGData readData(String folderName) {
		File folder = new File(folderName);
		String lgName = folder.getName();
		File[] fileList = folder.listFiles();
		
		if (lgName.equals("sh")) {
			lgName = "sr";
		}
		NGData data = new NGData();
		data.lgName = lgName;
		for (int i = 0; i < fileList.length; ++i) {
			File file = fileList[i];
			String fileFullName = file.getAbsolutePath();
			String fileName = file.getName();
			if (fileName.startsWith("label")) {
				data.lgLabelPath = fileFullName;
				data.labels = IOManager.readLines(fileFullName);
			} else {
				String[] tokens = fileName.split("_");
				String label = tokens[0];
				String content = IOManager.readContent(fileFullName);
				data.labeledContent.add(new Pair<String, String>(label, content));
				data.ids.add(fileName);
			}
		}
		return data;
	}
	
	public static NGData readData_sh (String folderName) {
		File folder = new File(folderName);
		String lgName = folder.getName();
		File[] fileList = folder.listFiles();
		
		NGData data = new NGData();
		data.lgName = lgName;
		for (int i = 0; i < fileList.length; ++i) {
			File file = fileList[i];
			String fileFullName = file.getAbsolutePath();
			String fileName = file.getName();
			if (fileName.startsWith("label")) {
				data.lgLabelPath = fileFullName;
				List<String> labels = IOManager.readLines(fileFullName);
				if (lgName.equals("sh")) {
					data.labels = new ArrayList<String>();
					for (String l : labels) {
						try {
							l = LanguageTextNormalization.normalizeSerbian(l);
						} catch (IOException e) {
							e.printStackTrace();
						}
						data.labels.add(l);
					}
				} else {
					data.labels = labels;
				}
			} else {
				String[] tokens = fileName.split("_");
				String label = tokens[0];
				String content = IOManager.readContent(fileFullName);
				if (lgName.equals("sh")) {
					try {
						content = LanguageTextNormalization.normalizeSerbian(content);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} 
				data.labeledContent.add(new Pair<String, String>(label, content));
			}
		}
		return data;
	}
	
	public static String extendContent (String content, HashMap<String, HashSet<String>> dictionary) {
		String contentNew = "";
//		content = content.replaceAll("[^A-Za-z ]", " "); 
		content = content.replaceAll("\\s+", " ");
		String[] tokens = content.split("\\s+");
		for (String token : tokens) {
			if (token.length() > 2 && dictionary.containsKey(token)) {
				HashSet<String> explanation = dictionary.get(token);
				for (String text : explanation) {
					String[] toks = text.split("\\s+");
					for (int i = 0; i < Math.min(toks.length, 10); ++i) {
						if (dictionary.containsKey(toks[i].trim()) == false && 
								toks[i].trim().equals("see") == false &&
								toks[i].trim().equals("cf.") == false) {
							contentNew += toks[i] + " ";
						}
					}
				}
			} else {
				contentNew += token + " ";
			}
		}
		
		return contentNew;
	}
	
	public static String extendContentRemovingSuffixes (String content, HashMap<String, HashSet<String>> dictionary, String[] suffixes) {
		String contentNew = "";
		content = content.replaceAll("\\s+", " ");
		String[] tokens = content.split("\\s+");
		for (String token : tokens) {
			boolean flag = false;
			for (int i = 0; i < suffixes.length; ++i) {
				if (token.endsWith(suffixes[i])) {
					token = token.replace(suffixes[i], "");
				}
			}
			if (flag == true) {
				flag = false;
				for (int i = 0; i < suffixes.length; ++i) {
					if (token.endsWith(suffixes[i])) {
						token = token.replace(suffixes[i], "");
						flag = true;
					}
				}
			}
			if (flag == true) {
				flag = false;
				for (int i = 0; i < suffixes.length; ++i) {
					if (token.endsWith(suffixes[i])) {
						token = token.replace(suffixes[i], "");
						flag = true;
					}
				}
			}
			if (flag == true) {
				flag = false;
				for (int i = 0; i < suffixes.length; ++i) {
					if (token.endsWith(suffixes[i])) {
						token = token.replace(suffixes[i], "");
						flag = true;
					}
				}
			}
			if (flag == true) {
				flag = false;
				for (int i = 0; i < suffixes.length; ++i) {
					if (token.endsWith(suffixes[i])) {
						token = token.replace(suffixes[i], "");
						flag = true;
					}
				}
			}
			if (token.length() > 2 && dictionary.containsKey(token)) {
				HashSet<String> explanation = dictionary.get(token);
				for (String text : explanation) {
					String[] toks = text.split("\\s+");
					for (int i = 0; i < Math.min(toks.length, 10); ++i) {
						if (dictionary.containsKey(toks[i].trim()) == false && 
								toks[i].trim().equals("see") == false &&
								toks[i].trim().equals("cf.") == false) {
							contentNew += toks[i] + " ";
						}
					}
				}
			} else {
				contentNew += token + " ";
			}
		}
		
		return contentNew;
	}
	
	public static String extendCJKContent (String content, HashMap<String, HashSet<String>> dictionary) {
		String contentNew = "";
//		content = content.replaceAll("[^A-Za-z ]", " "); 
		content = content.replaceAll("\\s+", " ");
		String[] tokens = content.split("\\s+");
		for (String token : tokens) {
			if (dictionary.containsKey(token)) {
				HashSet<String> explanation = dictionary.get(token);
				for (String text : explanation) {
					String[] toks = text.split("\\s+");
					for (int i = 0; i < Math.min(toks.length, 10); ++i) {
						if (dictionary.containsKey(toks[i].trim()) == false && 
								toks[i].trim().equals("see") == false &&
								toks[i].trim().equals("cf.") == false) {
							contentNew += toks[i] + " ";
						}
					}
				}
			} else {
				contentNew += token + " ";
			}
		}
		
		return contentNew;
	}
	
	public static Set<String> getVocabularyFromFiles (String inputFolder) {
		HashSet<String> vocabulary = new HashSet<String>();
		
		File folder = new File(inputFolder);
		File[] files = folder.listFiles();
		for (File file : files) {
			String content = IOManager.readContent(file.getAbsolutePath());
			String[] tokens = content.split("\\s+");
			for (String token : tokens) {
				vocabulary.add(token);
			}
		}
		
		return vocabulary;
	}
	
	public static Set<String> getCJKVocabularyFromFiles (String inputFolder, String lg) {
		HashSet<String> vocabulary = new HashSet<String>();
		
		Analyzer analyzer = AnalyzerFactory.initialize(lg);

		File folder = new File(inputFolder);
		File[] files = folder.listFiles();
		for (File file : files) {
			String content = IOManager.readContent(file.getAbsolutePath());
			
			try {
				TokenStream stream = analyzer.tokenStream(null, content);
				CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
				stream.reset();
				while (stream.incrementToken()) {
				  String token = cattr.toString().trim();
				  vocabulary.add(token);
				}
				stream.end();
				stream.close();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		return vocabulary;
	}
	
	public static String[] NG20Labels = new String[] {
		"comp.graphics",
		"comp.os.ms.windows.misc",
		"comp.sys.ibm.pc.hardware",
		"comp.sys.mac.hardware",
		"comp.windows.x",
		"rec.autos",
		"rec.motorcycles",
		"rec.sport.baseball",
		"rec.sport.hockey",
		"sci.crypt",
		"sci.electronics",
		"sci.med",
		"sci.space",
		"talk.politics.misc",
		"talk.politics.guns",
		"talk.politics.mideast",
		"talk.religion.misc",
		"alt.atheism",
		"soc.religion.christian",
		"misc.forsale",  
	};
}
