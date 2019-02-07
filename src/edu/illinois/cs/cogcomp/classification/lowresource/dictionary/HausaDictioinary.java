package edu.illinois.cs.cogcomp.classification.lowresource.dictionary;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.illinois.cs.cogcomp.MultiLingualResourcesConfig;
import edu.illinois.cs.cogcomp.descartes.util.IOManager;

public class HausaDictioinary {
	public static String systemNewLine = System.getProperty("line.separator");
	
	public HashMap<String, HashSet<String>> dictionary = new HashMap<String, HashSet<String>>();

	public static void main (String[] args) throws IOException {
		MultiLingualResourcesConfig.initialization();
		String dictFile = MultiLingualResourcesConfig.hausaDictionary;
		HausaDictioinary dict = new HausaDictioinary(dictFile);
		System.out.println(dict.dictionary.size());
	}
	
	public HausaDictioinary (String inputFile) {
		try {
			processManualData(inputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void processManualData (String inputFile) throws IOException {
		if (MultiLingualResourcesConfig.isInitialized == false)
			MultiLingualResourcesConfig.initialization();
		
		List<String> content = IOManager.readLines(inputFile);
		
		dictionary = new HashMap<String, HashSet<String>>();
		
		String paragraph = "";
		for (int i = 0; i < content.size(); ++i) {
			String line = content.get(i);
			if (line.trim().equals("") == true) {
				processParagraph (paragraph);
//				System.out.println(paragraph);
				paragraph = "";
			} else {
				if (paragraph.endsWith("-")) {
					paragraph = paragraph.substring(0, paragraph.length() - 1) + line.trim();
				} else {
					paragraph = paragraph + " " + line.trim();
				}
			}
		}
	}
	
	public void processParagraph (String paragraph) {
		String[] tokens1 = paragraph.trim().split(" ");
		String[] tokens2 = paragraph.trim().split(",");
		String word = "";
		if (tokens2[0].contains(tokens1[0]) == true) {
			word = tokens1[0];
		} else {
			word = tokens2[0];
		}
		
		if (word.trim().equals("") == false) {
			String subStr = paragraph.substring(word.length() + 1);
			
			if (dictionary.containsKey(word) == false) {
				dictionary.put(word, new HashSet<String>());
			}
			dictionary.get(word).add(subStr);
//			System.out.println(word);
//			System.out.println(subStr);
		}
		
	}
}
