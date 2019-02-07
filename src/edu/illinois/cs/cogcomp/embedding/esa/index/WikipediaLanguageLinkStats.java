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

import edu.illinois.cs.cogcomp.classification.lowresource.dictionary.HausaDictioinary;
import edu.illinois.cs.cogcomp.descartes.util.IOManager;

public class WikipediaLanguageLinkStats {

	public static String systemNewLine = System.getProperty("line.separator");

	public static void main (String[] args) throws IOException {
		List<String> lgSimMat = IOManager.readLines("matlab\\lrlSummary_new.txt"); 
		String line = lgSimMat.get(0);
		String[] tokens = line.split(",");
		List<String> hrlList = new ArrayList<String>();
		for (int i = 2; i < tokens.length; ++i) {
			hrlList.add(tokens[i]);
		}
		List<String> lrlList = new ArrayList<String>();
		for (int i = 1; i < lgSimMat.size(); ++i) {
			tokens = lgSimMat.get(i).split(",");
			lrlList.add(tokens[0]);
		}
		
		String wikiLangLinkFolder = "C:\\yqsong\\data\\cross-lingual\\wikiLanguageLinks\\";
		String fileName = "aawiki-20151123-langlinks.sql.gz";
		String enFileName = "enwiki-20150805-langlinks.sql.gz";
		Charset encoding = Charset.forName("UTF-8");
		HashMap<String, HashMap<String, Integer>> lgLinkNumMap = new 
				HashMap<String, HashMap<String, Integer>>();
		for (int i = 0; i < lrlList.size(); ++i) {
			for (int j = 0; j < hrlList.size(); ++j) {
				String lrl = lrlList.get(i);
				String hrl = hrlList.get(j);
				
				String lrlWikiLinks = fileName.replace("aawiki", lrl + "wiki");
				String hrlWikiLinks = fileName.replace("aawiki", hrl + "wiki");
				if (hrl.equals("en")) {
					hrlWikiLinks = enFileName;
				}
				
				System.out.println("[Processing] " + lrl);
				int selectedLrl = handleFile(new File(wikiLangLinkFolder + lrlWikiLinks), encoding, hrl);
//				System.out.println("[Processing] " + hrl);
//				int selectedHrl = handleFile(new File(wikiLangLinkFolder + hrlWikiLinks), encoding, lrl);
				
				if (lgLinkNumMap.containsKey(lrl) == false) {
					lgLinkNumMap.put(lrl, new HashMap<String, Integer>());
				}
				lgLinkNumMap.get(lrl).put(hrl, selectedLrl);
			}
		}
		FileWriter writer = new FileWriter("matlab\\lrl_wiki_Langlinks.txt");
		writer.write("source,");
		for (int j = 0; j < hrlList.size(); ++j) {
			writer.write(hrlList.get(j) + ",");
		}
		writer.write(HausaDictioinary.systemNewLine);
		for (int i = 0; i < lrlList.size(); ++i) {
			writer.write(lrlList.get(i) + ",");
			for (int j = 0; j < hrlList.size(); ++j) {
				int num = lgLinkNumMap.get(lrlList.get(i)).get(hrlList.get(j));
				writer.write(num + ",");
			}
			writer.write(HausaDictioinary.systemNewLine);
		}
		writer.close();
	}
	
	 private static int handleFile(File file, Charset encoding, String language)
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
		 int selectedNum = handleCharacters(buffer, language);
		 reader.close();
		 in.close();
		 return selectedNum;
	 }

	 private static int handleCharacters(Reader reader, String language) {
		 int selected = 0;
		 try {
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
					 boolean isSelected = prcessContent (bufferRemovingTags.toString(), language);
					 bufferRemovingTags = new StringBuffer();

					 if (count % 1000000 == 0) {
						 System.out.println("  [" + language + "] " + "Processed " + count + " items, selected " + selected);
					 }
					 count++;
					 if (isSelected) {
						 selected++;
					 }
				 }
			 }
			 
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
		 return selected;
	 }
	 
	 private static boolean prcessContent (String content, String language) {
		 boolean isSelected = false;
		 
		 try {
			 content = content.replace("(", "");
			 content = content.replace(")", "");
			 content = content.replace("'", "");
			 String[] tokens = content.split(",");
			 if (tokens.length == 3) {
				 if (tokens[1].equals(language)) {
					 isSelected = true;
				 }
			 }
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
		 
		 return isSelected;
	 }
}
