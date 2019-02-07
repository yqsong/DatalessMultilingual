package edu.illinois.cs.cogcomp.translation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.*;
import java.util.*;

import edu.illinois.cs.cogcomp.MultiLingualResourcesConfig;
import edu.illinois.cs.cogcomp.descartes.util.IOManager;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;

public class Translate {
	
	public static void main(String[] args) throws Exception {
		int index = Integer.parseInt(args[0]);
		int fold = 5;
		translateAll (index, fold);
	}
	   
	// translate all documents for 91 languages
	public static void translateAll (int index, int fold)  throws Exception {
		MultiLingualResourcesConfig.initialization();
		ArrayList<String> languages = IOManager.readLines(MultiLingualResourcesConfig.languageGoolgeTranslateSupportList);
		String inputFolder = MultiLingualResourcesConfig.ngOriginalFolder;
	    String outputFolderTranslate = MultiLingualResourcesConfig.multiLingual20NGAllFolder;
	    String outputFolderTranslateBack = MultiLingualResourcesConfig.multiLingual20NGEnBackAllFolder;
	    
	    File folder = new File (inputFolder);
	    File[] folderList = folder.listFiles();
	    int count = 0;
	    for (int i = 0; i < folderList.length; ++i) {
	    	File[] fileList = folderList[i].listFiles();
	    	String label = folderList[i].getName();
	    	for (int j = 0; j < fileList.length; ++j) {
	    		String file = fileList[j].getName();
	    		System.out.println ("[" + count + "] " + fileList[j].getAbsolutePath());
	    		
	    		String text = IOManager.readContent(fileList[j].getAbsolutePath());
	    		TextAnnotation ta = CuratorTokenizer.createDoc(text);
	    		
	    		count++;
	    		
	    		for (int l = 0; l < languages.size(); ++l) {
	    			String lg = languages.get(l);
	    			
	    			if (l % fold != index)
	    				continue;
	    			
	    			System.out.println (lg + " ");
	    			
		    		String outputTranslateFile = outputFolderTranslate + lg + "/" + label + "_" + file;
		    		File outputFolder1 = new File(outputFolderTranslate + lg);
		    		if (outputFolder1.exists() == false) {
		    			outputFolder1.mkdir();
		    		}
		    		
		    		File writeFile = new File (outputTranslateFile);
		    		if (writeFile.exists()) {
		    			String content = IOManager.readContent(outputTranslateFile);
		    			if (content.trim().equals("")) {
		    				
		    				FileWriter writer = new FileWriter(outputTranslateFile);
				    		String outputTranslateBackFile = outputFolderTranslateBack + lg + "/" + label + "_" + file;
				    		File outputFolder2 = new File(outputFolderTranslateBack + lg);
				    		if (outputFolder2.exists() == false) {
				    			outputFolder2.mkdir();
				    		}
				    		FileWriter writerBack = new FileWriter(outputTranslateBackFile);
				    		
				    		for (int k = 0; k < ta.getNumberOfSentences(); k++) {
				    			String sen = ta.getSentence(k).getText();
				    			sen = sen.replace('\n', ' ');
				    			
				    			System.out.println (" en->"+lg);
				    			String f_sen = googleTranslate(sen, lg, "en");
				    			writer.write(f_sen + "\n");
				    			
				    			System.out.println (" " +lg + "->en");
				    			String f_sen_back = googleTranslate(f_sen, "en", lg);
				    			writerBack.write(f_sen_back + "\n");
				    		}
				    		
				    		writer.flush();
				    		writer.close();
				    		
				    		writerBack.flush();
				    		writerBack.close();
		    			}
		    		} else {
		    			FileWriter writer = new FileWriter(outputTranslateFile);
			    		String outputTranslateBackFile = outputFolderTranslateBack + lg + "/" + label + "_" + file;
			    		File outputFolder2 = new File(outputFolderTranslateBack + lg);
			    		if (outputFolder2.exists() == false) {
			    			outputFolder2.mkdir();
			    		}
			    		FileWriter writerBack = new FileWriter(outputTranslateBackFile);
			    		
			    		for (int k = 0; k < ta.getNumberOfSentences(); k++) {
			    			String sen = ta.getSentence(k).getText();
			    			sen = sen.replace('\n', ' ');
			    			
			    			System.out.println (" en->"+lg);
			    			String f_sen = googleTranslate(sen, lg, "en");
			    			writer.write(f_sen + "\n");
			    			
			    			System.out.println (" " +lg + "->en");
			    			String f_sen_back = googleTranslate(f_sen, "en", lg);
			    			writerBack.write(f_sen_back + "\n");
			    		}
			    		
			    		writer.flush();
			    		writer.close();
			    		
			    		writerBack.flush();
			    		writerBack.close();
		    		}
		    		
		    		
	    		}
	    		
	    		System.out.println();
	    	}
	    }
	    

	}
	
	// translate 100 perfect documents for 88 languages
	public static void translatePartially () throws Exception {
		MultiLingualResourcesConfig.initialization();
		ArrayList<String> files = IOManager.readLines(MultiLingualResourcesConfig.multiLingual20NGFolder + "filelist_new.txt");
		//files.clear();
		files.add("label.en");
		ArrayList<String> languages = IOManager.readLines(MultiLingualResourcesConfig.multiLingual20NGFolder + "languagelist.txt");
	        
		for (String file : files) {
			fileTranslate(MultiLingualResourcesConfig.multiLingual20NGFolder, file, languages, 1);
		}

	}
	
    public static String googleTranslate(String to_translate, String to_langage, String from_langage) {
        String page, result, hl, sl, q;
        
        String before_trans = "class=\"t0\">";
        
        String charset = "UTF-8";
        
        try{
            hl = URLEncoder.encode(to_langage, charset);
            sl = URLEncoder.encode(from_langage, charset);
            q = URLEncoder.encode(to_translate, charset);
        }catch(Exception e){
//            e.printStackTrace();
        	System.err.println("  [encode error]");
            return "";
        }
        
        String query = String.format("https://translate.google.com/m?hl=%s&sl=%s&q=%s", hl,sl,q);
        
        try {
            page = URLConnectionReader.getText(query);
        } catch (Exception e) {
//            e.printStackTrace();
        	System.err.println("  [translate error]");
            return "";
        }
        result = page.substring(page.indexOf(before_trans)+before_trans.length());
        result = result.split("<")[0];
        return result;
    }
    
    public static void fileTranslate(String folder, String file, ArrayList<String> languages, int opt) throws Exception {
    	System.out.print(file);
    	String input = "";
    	if (!file.equals("label.en")) {
    		input = folder + "selected_original/" + file;
    	}
    	else {
    		input = folder + file;
    	}
		String text = IOManager.readContent(input);
		ArrayList<String> lines = IOManager.readLines(input);
		TextAnnotation ta = CuratorTokenizer.createDoc(text);
		for (String lang : languages) {
			System.out.print(" " + lang);
			String dir = lang;
			if (opt == 2) {
				dir = lang + "_word";
			}
			String filename = folder + dir + "/" + file;
			if (file.equals("label.en")) {
				filename = folder + dir + "/" + "label." + lang;
			}
			BufferedWriter bw = IOManager.openWriter(filename);
			if (!file.equals("label.en")) {
				for (int i = 0; i < ta.getNumberOfSentences(); i++) {
	    			if (opt == 1) {
		    			String sen = ta.getSentence(i).getText();
		    			sen = sen.replace('\n', ' ');
		    			String f_sen = googleTranslate(sen, lang, "en");
		    			bw.write(f_sen + "\n");
	    			}
	    			if (opt == 2) {
	    				String[] tokens = ta.getSentence(i).getTokens();
	    				for (String token : tokens) {
	    					String f_sen = googleTranslate(token, lang, "en");
	    	    			bw.write(f_sen + " ");
	    				}
	    				bw.write("\n");
	    			}
	    		}
			}
			else {
				for (String line : lines) {
	    			if (opt == 1) {
		    			String f_sen = googleTranslate(line, lang, "en");
		    			bw.write(f_sen + "\n");
	    			}
	    			if (opt == 2) {
	    				String[] tokens = line.split(" ");
	    				for (String token : tokens) {
	    					String f_sen = googleTranslate(token, lang, "en");
	    	    			bw.write(f_sen + " ");
	    				}
	    				bw.write("\n");
	    			}
	    		}
			}
    		bw.close();
		}
		System.out.println();
    }
    
}
