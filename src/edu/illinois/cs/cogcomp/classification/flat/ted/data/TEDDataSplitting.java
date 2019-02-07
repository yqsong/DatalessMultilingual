package edu.illinois.cs.cogcomp.classification.flat.ted.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.descartes.util.IOManager;

public class TEDDataSplitting {
	
	public List<String> labeledTrainIDs = new ArrayList<String>();
	public List<Pair<String, Boolean>> labeledTrainData = new ArrayList<Pair<String, Boolean>>();

	public List<String> labeledTestIDs = new ArrayList<String>();
	public List<Pair<String, Boolean>> labeledTestData = new ArrayList<Pair<String, Boolean>>();

	public void loadOneLanguageOneLabel (String lgName, String trainPathPos, String trainPathNeg, String testPathPos, String testPathNeg, boolean isOriginal) {
		Random r = new Random(0);
		loadOneLanguageOneLabel(lgName, trainPathPos, trainPathNeg, testPathPos, testPathNeg, isOriginal, 1, r);
	}
	
	public void loadOneLanguageOneLabel (String lgName, String trainPathPos, String trainPathNeg, String testPathPos, String testPathNeg, 
			boolean isOriginal, double sampleRate, Random r) {
		File trainFolderPos = new File (trainPathPos);
		File[] trainPosList = trainFolderPos.listFiles();
		List<String> allLabeledTrainIDs = new ArrayList<String>();
		List<Pair<String, Boolean>> allLabeledTrainData = new ArrayList<Pair<String, Boolean>>();
		for (int i = 0; i < trainPosList.length; ++i) {
			String fileName = trainPosList[i].getAbsolutePath();
			String docContent = "";
			if (isOriginal) {
				docContent = readDocument(fileName, lgName);
			} else {
				docContent = readDocumentForClassifiers(fileName, lgName);
			}
			Pair<String, Boolean> docLabelPair = new Pair<String, Boolean>(docContent, true);
			allLabeledTrainData.add(docLabelPair);
			allLabeledTrainIDs.add(fileName);
		}
		File trainFolderNeg = new File (trainPathNeg);
		File[] trainNegList = trainFolderNeg.listFiles();
		for (int i = 0; i < trainNegList.length; ++i) {
			String fileName = trainNegList[i].getAbsolutePath();
			String docContent = "";
			if (isOriginal) {
				docContent = readDocument(fileName, lgName);
			} else {
				docContent = readDocumentForClassifiers(fileName, lgName);
			}
			Pair<String, Boolean> docLabelPair = new Pair<String, Boolean>(docContent, false);
			allLabeledTrainData.add(docLabelPair);
			allLabeledTrainIDs.add(fileName);
		}
		
		for (int i = 0; i < allLabeledTrainIDs.size(); ++i) {
			if (r.nextDouble() <= sampleRate) {
				labeledTrainIDs.add(allLabeledTrainIDs.get(i));
				labeledTrainData.add(allLabeledTrainData.get(i));
			}
		}
		
		File testFolderPos = new File (testPathPos);
		File[] testPosList = testFolderPos.listFiles();
		for (int i = 0; i < testPosList.length; ++i) {
			String fileName = testPosList[i].getAbsolutePath();
			String docContent = "";
			if (isOriginal) {
				docContent = readDocument(fileName, lgName);
			} else {
				docContent = readDocumentForClassifiers(fileName, lgName);
			}
			Pair<String, Boolean> docLabelPair = new Pair<String, Boolean>(docContent, true);
			labeledTestData.add(docLabelPair);
			labeledTestIDs.add(fileName);
		}
		File testFolderNeg = new File (testPathNeg);
		File[] testNegList = testFolderNeg.listFiles();
		for (int i = 0; i < testNegList.length; ++i) {
			String fileName = testNegList[i].getAbsolutePath();
			String docContent = "";
			if (isOriginal) {
				docContent = readDocument(fileName, lgName);
			} else {
				docContent = readDocumentForClassifiers(fileName, lgName);
			}
			Pair<String, Boolean> docLabelPair = new Pair<String, Boolean>(docContent, false);
			labeledTestData.add(docLabelPair);
			labeledTestIDs.add(fileName);
		}
		
		int posTrainCount = trainPosList.length;
		int negTrainCount = trainNegList.length;
		int posTestCount = testPosList.length;
		int negTestCount = testNegList.length;
		System.out.println(
				"posTrainCount: " + posTrainCount +
				", negTrainCount: " + negTrainCount +
				", posTestCount: " + posTestCount +
				", negTestCount: " + negTestCount);
	}
	
	public static String readDocument (String fileName, String lgName) {
		String docContent = IOManager.readContent(fileName);
		String lgTag = "_" + lgName;
		docContent = docContent.replaceAll(lgTag, "");
		return docContent;
	}
	
	public static String readDocumentForClassifiers (String fileName, String lgName) {
		String docContent = IOManager.readContent(fileName);
		String lgTag = "_" + lgName;
		docContent = docContent.replaceAll(lgTag, "");
		String[] tokens = docContent.split("\\s+");
		String docStringNew = "";
		for (int i = 0; i < tokens.length; ++i) {
			tokens[i] = tokens[i].replaceAll("[\\p{P}\\p{Digit}]", "");
			docStringNew += tokens[i] + " ";
		}
		return docStringNew;
	}
	
}
