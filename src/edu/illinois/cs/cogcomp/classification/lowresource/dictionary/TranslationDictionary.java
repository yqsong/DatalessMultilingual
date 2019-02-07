package edu.illinois.cs.cogcomp.classification.lowresource.dictionary;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.illinois.cs.cogcomp.MultiLingualResourcesConfig;
import edu.illinois.cs.cogcomp.descartes.util.IOManager;

public class TranslationDictionary {
	
	public HashMap<String, HashSet<String>> dictionary = new HashMap<String, HashSet<String>>();

	public static void main (String[] args) throws IOException {
//		processManualData ();
	}
	
	public TranslationDictionary (String inputFile) {
		try {
			processTranlationData(inputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void processTranlationData (String inputFile) throws IOException {
		if (MultiLingualResourcesConfig.isInitialized == false)
			MultiLingualResourcesConfig.initialization();
		
		List<String> content = IOManager.readLines(inputFile);
		
		dictionary = new HashMap<String, HashSet<String>>();
		
		for (int i = 0; i < content.size(); ++i) {
			String line = content.get(i).trim();
			String[] tokens = line.split("\t");
			if (tokens.length == 2) {
				dictionary.put(tokens[0], new HashSet<String>());
				dictionary.get(tokens[0]).add(tokens[1]);
			}
		}
	}
	
}
