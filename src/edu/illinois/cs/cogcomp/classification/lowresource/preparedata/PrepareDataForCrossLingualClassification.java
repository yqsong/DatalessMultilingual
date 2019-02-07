package edu.illinois.cs.cogcomp.classification.lowresource.preparedata;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.illinois.cs.cogcomp.descartes.util.IOManager;

public class PrepareDataForCrossLingualClassification {
	
	public static String systemNewLine = System.getProperty("line.separator");

	public static void main (String[] args) throws IOException {
		String inputEnFolderStr = "python\\nut\\data\\ar";
		String inputLgFolderStr = "python\\nut\\data\\uz";
		
		String outputFolderStr = "python\\nut\\data\\ar-uz";
		File outputFolder = new File (outputFolderStr);
		if (outputFolder.exists() == false) {
			outputFolder.mkdir();
		}
		
		File inputEnFolder = new File(inputEnFolderStr);
		File inputLgFolder = new File(inputLgFolderStr);
		
		File[] lgList = inputLgFolder.listFiles();
		HashMap<String, File> lgFileMap = new HashMap<String, File>();
		for (int i = 0; i < lgList.length; ++i) {
			lgFileMap.put(lgList[i].getName(), lgList[i]);
		}
		
		File[] enList = inputEnFolder.listFiles();
		List<String> enContentlist = new ArrayList<String>();
		List<String> lgContentlist = new ArrayList<String>();
		for (int i = 0; i < enList.length; ++i) {
			String[] tokens = enList[i].getName().split("_");
			String label = tokens[0];
			String enContent = IOManager.readContent(enList[i].getAbsolutePath());
			String lgContent = IOManager.readContent(lgFileMap.get(enList[i].getName()).getAbsolutePath());
			
			String enFormated = formatedString(enContent, label);
			String lgFormated = formatedString(lgContent, label);
			enContentlist.add(enFormated);
			lgContentlist.add(lgFormated);
		}
		
		int fold = 5;
		for (int f = 0; f < fold; ++f) {
			String enTraining = "";
			String enTesting = "";
			String lgTraining = "";
			String lgTesting = "";
			for (int i = 0; i < enContentlist.size(); ++i) {
				if (i % fold == f) {
					enTesting += enContentlist.get(i) + systemNewLine;
					lgTesting += lgContentlist.get(i) + systemNewLine;
				} else {
					enTraining += enContentlist.get(i) + systemNewLine;
					lgTraining += lgContentlist.get(i) + systemNewLine;
				}
			}
			FileWriter writerEnTraining = new FileWriter(outputFolder + "\\" + "ar_" + f + "_training.txt");
			writerEnTraining.write(enTraining);
			writerEnTraining.close();
			FileWriter writerEnTesting = new FileWriter(outputFolder + "\\" + "ar_" + f + "_testing.txt");
			writerEnTesting.write(enTesting);
			writerEnTesting.close();
			FileWriter writerLgTraining = new FileWriter(outputFolder + "\\" + "lg_" + f + "_training.txt");
			writerLgTraining.write(lgTraining);
			writerLgTraining.close();
			FileWriter writerLgTesting = new FileWriter(outputFolder + "\\" + "lg_" + f + "_testing.txt");
			writerLgTesting.write(lgTesting);
			writerLgTesting.close();

		}
		
 	}
	
	public static String formatedString (String content, String label) {
		String formatedContent = "";
		
		String[] tokens = content.split("\\s+");
		HashMap<String, Integer> dict = new HashMap<String, Integer>();
		for (String token : tokens) {
			if (dict.containsKey(token)) {
				dict.put(token, dict.get(token)+ 1);
			} else {
				dict.put(token, 1);
			}
		}
		
		for (String key : dict.keySet()) {
			formatedContent += key + ":" + dict.get(key) + " ";
		}
		formatedContent += "#label#:" + label;
		
		return formatedContent;
	}

}
