package edu.illinois.cs.cogcomp.classification.flat.ted.data;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.jxpath.ri.model.beans.LangAttributePointer;

import edu.illinois.cs.cogcomp.classification.hierarchical.rcv.data.LanguageMapping;
import edu.illinois.cs.cogcomp.classification.main.DatalessResourcesConfig;
import edu.illinois.cs.cogcomp.descartes.util.IOManager;
import edu.illinois.cs.cogcomp.embedding.esa.index.ReadLangLinkingFile;

public class GetAllWords {

	public static void main (String[] args) {
	
		try {
			String folderStr = "D:\\yqsong\\projects\\multilingual\\bicvm\\sample\\";
			File folder = new File (folderStr);
			File[] listFile = folder.listFiles();
			for (int i = 0; i < listFile.length; ++i) {
				String vocName = listFile[i].getName();
				String outputFile = folderStr + vocName + ".txt";
				
				if (listFile[i].isDirectory() == true) {
					HashSet<String> voc = getAll (listFile[i].getAbsolutePath(), outputFile);
					
					FileWriter writer = new FileWriter(outputFile);
					for (String token : voc) {
						writer.write(token + ReadLangLinkingFile.systemNewLine);
					}
					writer.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static HashSet<String> getAll (String folderStr, String outputFile) {
		HashSet<String> voc = new HashSet<String>();
		
		File folder = new File (folderStr);
		File[] listFile = folder.listFiles();
		for (int i = 0; i < listFile.length; ++i) {
			List<String> lines = IOManager.readLines(listFile[i].getAbsolutePath());
			for (String line : lines) {
				String[] tokens = line.split("\\s+");
				for (String token : tokens) {
					voc.add(token);
				}
			}
		}
		return voc;
	}
	
}
