package edu.illinois.cs.cogcomp.embedding.esa.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.zip.GZIPInputStream;

import edu.illinois.cs.cogcomp.MultiLingualResourcesConfig;
import edu.illinois.cs.cogcomp.descartes.util.IOManager;

public class ReadLangLinkingFile {

	public static String systemNewLine = System.getProperty("line.separator");

	public static void main (String[] args) {
		String langLinkFile = MultiLingualResourcesConfig.langLinkFile; //"/shared/corpora/yqsong/data/wikipedia/crossLanguage/enwiki-20150805-langlinks.sql";
		String outputFolder = "/shared/shelley/yqsong/data/wikipedia_crossLanguage/";
		String[] languages = new String[] {"es", "fr", "de", "hi", "pl", "ru"};
		for (String lg : languages) {
			readLangLinking (langLinkFile, outputFolder, lg);
		}
		
	}
	
	public static void readLangLinking (String fileName, String outputFolder, String language) {
		Reader reader;
		try {
			File file = new File (fileName);
			Charset encoding = Charset.forName("UTF-8");
			handleFile(file, outputFolder, encoding, language);
		} catch (Exception e) {
			e.printStackTrace();
		}
		   
	}
	
	 private static void handleFile(File file, String outputFolder, Charset encoding, String language)
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

	 private static void handleCharacters(Reader reader, String outputFolder, String language) {
		 try {
			 FileWriter writer = new FileWriter(outputFolder + language + "_langlink.txt");
			 int r;
			 int count = 0;
			 int selected = 0;
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
					 boolean isSelected = prcessContent (bufferRemovingTags.toString(), writer, language);
					 bufferRemovingTags = new StringBuffer();
					 count++;
					 
					 if (isSelected) {
						 selected++;
					 }
					 if (count % 1000000 == 0) {
						 System.out.println("[" + language + "] " + "Processed " + count + " items, selected " + selected);
					 }
				 }
			 }
			 
			 writer.flush();
			 writer.close();
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
	
	 }
	 
	 private static boolean prcessContent (String content, FileWriter writer, String language) {
		 boolean isSelected = false;
		 
		 try {
			 content = content.replace("(", "");
			 content = content.replace(")", "");
			 content = content.replace("'", "");
			 String[] tokens = content.split(",");
			 if (tokens.length == 3) {
				 if (tokens[1].equals(language)) {
					 isSelected = true;
					 writer.write(content + systemNewLine);
				 }
			 }
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
		 
		 return isSelected;
	 }
	
}
