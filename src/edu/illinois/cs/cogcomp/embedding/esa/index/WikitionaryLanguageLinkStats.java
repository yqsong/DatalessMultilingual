package edu.illinois.cs.cogcomp.embedding.esa.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

import edu.illinois.cs.cogcomp.descartes.util.IOManager;

public class WikitionaryLanguageLinkStats {

	public static String systemNewLine = System.getProperty("line.separator");

	public static void main (String[] args) {
		test3();
	}
	
	public static void test3 () {
		String lgListFile = "D:\\yqsong\\data\\wiktionary\\wiktionarytoplanguages.txt";
		List<String> lgList = IOManager.readLines(lgListFile);
		String inputFolder = "D:\\yqsong\\data\\wiktionary\\output\\";
		HashMap<String, HashMap<String, Integer>> allMap = new HashMap<String, HashMap<String, Integer>>();
		for (String lg : lgList) {
			String lgLingFile = inputFolder + lg + "_langlink.txt";
			List<String> lgLines = IOManager.readLines(lgLingFile);
			HashMap<String, Integer> lgMap = new HashMap<String, Integer>();
			for (String line : lgLines) {
				String[] tokens = line.split("\t");
				if (tokens.length == 2)
					lgMap.put(tokens[0], Integer.parseInt(tokens[1]));
			}
			allMap.put(lg, lgMap);
		}
		for (String lg : allMap.keySet()) {
			HashMap<String, Integer> lgMap = allMap.get(lg);
			int count = 0;
			for (String lgNew : lgMap.keySet()) {
				if (lgMap.get(lgNew) > 10000) 
					count++;
			}
			System.out.println(lg + "\t" + count);
		}
		
	}
	
	public static void test2 () {
		String lgListFile = "D:\\yqsong\\data\\wiktionary\\wiktionarytoplanguages.txt";
		List<String> lgList = IOManager.readLines(lgListFile);
		String inputFolder = "D:\\yqsong\\data\\wiktionary\\output\\";
		HashMap<String, HashMap<String, Integer>> allMap = new HashMap<String, HashMap<String, Integer>>();
		for (String lg : lgList) {
			String lgLingFile = inputFolder + lg + "_langlink.txt";
			List<String> lgLines = IOManager.readLines(lgLingFile);
			HashMap<String, Integer> lgMap = new HashMap<String, Integer>();
			for (String line : lgLines) {
				String[] tokens = line.split("\t");
				if (tokens.length == 2)
					lgMap.put(tokens[0], Integer.parseInt(tokens[1]));
			}
			allMap.put(lg, lgMap);
		}
		HashMap<String, Integer> enMap = allMap.get("en");
		List<Integer> listLgNew = new ArrayList<Integer>();
		for (String lg : enMap.keySet()) {
			if (enMap.get(lg) < 10000) {
				int count = 0;
				HashMap<String, Integer> lgMap = allMap.get(lg);
				for (String lgNew : lgMap.keySet()) {
					if (lgMap.get(lgNew) > 10000) 
						count++;
				}
				listLgNew.add(count);
			}
		}
		for (int c : listLgNew) {
			System.out.println(c);
		}
		
	}
	
	public static void test1 () {
		String lgListFile = "D:\\yqsong\\data\\wiktionary\\wiktionarytoplanguages.txt";
		List<String> lgList = IOManager.readLines(lgListFile);
		String outputFolder = "D:\\yqsong\\data\\wiktionary\\output\\";
		for (String lg : lgList) {
			String lgLingFile = "D:\\yqsong\\data\\wiktionary\\"+lg+"wiktionary-20160305-langlinks.sql.gz";
			System.out.println("Processing " + lg );
			readLangLinking(lgLingFile, outputFolder + lg, lgList);
		}
	}
	
	public static void readLangLinking (String fileName, String outputFolder, List<String> language) {
		Reader reader;
		try {
			File file = new File (fileName);
			Charset encoding = Charset.forName("UTF-8");
			handleFile(file, outputFolder, encoding, language);
		} catch (Exception e) {
			e.printStackTrace();
		}
		   
	}
	
	 private static void handleFile(File file, String outputFolder, Charset encoding, List<String> language)
			 throws IOException {
		 Reader reader = null;
		 InputStream in = null;
		 if (file.getName().endsWith("gz")) {
			 in = new GZIPInputStream(new FileInputStream(file));
			 reader = new InputStreamReader(in, encoding);
		 } else {
			 in = new FileInputStream(file);
			 reader = new InputStreamReader(in, encoding);
		 }
		 Reader buffer = new BufferedReader(reader);
		 handleCharacters(buffer, outputFolder, language);
		 reader.close();
		 in.close();
	 }

	 private static void handleCharacters(Reader reader, String outputFolder, List<String> language) {
		 try {
			 HashMap<String, Integer> lgMap = new HashMap<String, Integer>();
			 FileWriter writer = new FileWriter(outputFolder + "_langlink.txt");
			 int r;
			 int count = 0;
			 boolean isContent = false;
			 StringBuffer bufferRemovingTags = new StringBuffer();
			 while ((r = reader.read()) != -1) {
				 char ch = (char) r;
				 if(ch == '(') 
					 isContent = true;
				 if (isContent) 
					 bufferRemovingTags.append(ch);
				 if(ch == ')') {
					 isContent = false;
					 String selectedLG = prcessContent (bufferRemovingTags.toString(), language);
					 bufferRemovingTags = new StringBuffer();
					 count++;
					 
					 if (selectedLG.equals("null") == false) {
						 if (lgMap.containsKey(selectedLG) == true) {
							 lgMap.put(selectedLG, lgMap.get(selectedLG) + 1);
						 } else {
							 lgMap.put(selectedLG, 1);
						 }
					 }
					 if (count % 1000000 == 0) {
						 System.out.println("[" + outputFolder + "_langlink.txt" + "] " + "Processed " + count);
					 }
				 }
			 }
			 
			 for (String lg : lgMap.keySet()) {
				 writer.write(lg + "\t" + lgMap.get(lg) + systemNewLine);
			 }
			 
			 writer.flush();
			 writer.close();
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
	
	 }
	 
	 private static String prcessContent (String content, List<String> language) {
		 String selectedLG = "null";
		 try {
			
			 content = content.replace("(", "");
			 content = content.replace(")", "");
			 content = content.replace("'", "");
			 String[] tokens = content.split(",");
			 if (tokens.length == 3) {
				 for (String lg : language) {
					 if (tokens[1].equals(lg)) {
						 selectedLG = lg;
					 }
				 }
			 }
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
		 
		 return selectedLG;
	 }
}
