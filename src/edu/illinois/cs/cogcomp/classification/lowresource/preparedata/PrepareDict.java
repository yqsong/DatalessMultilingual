package edu.illinois.cs.cogcomp.classification.lowresource.preparedata;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import edu.illinois.cs.cogcomp.descartes.util.IOManager;

public class PrepareDict {
	
	public static void main (String[] args) throws IOException {
		String inputFile = "python\\nut\\data\\dict\\uz\\uz-ar.txt";
		String outputFile = "python\\nut\\data\\dict\\ar-uz.txt";
		processDict (inputFile, outputFile);
	}
	
	public static void processDict (String inputFile, String outputFile) throws IOException {
		FileWriter writer = new FileWriter(outputFile);
		List<String> lines = IOManager.readLines(inputFile);
		for (String line : lines) {
			String[] tokens = line.split("\t");
			String newLine = tokens[1] + "\t" + tokens[0];
			writer.write(newLine + PrepareDataForCrossLingualClassification.systemNewLine);
		}
		writer.close();
	}

}
