package edu.illinois.cs.cogcomp.translation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import edu.illinois.cs.cogcomp.MultiLingualResourcesConfig;
import edu.illinois.cs.cogcomp.descartes.util.IOManager;

public class GetFiles {
	
	static int top_n = 10;
	
	public static void main (String[] args) throws IOException {
		MultiLingualResourcesConfig.initialization();
		String path = MultiLingualResourcesConfig.multiLingual20NGFolder; 
		String ngFile = MultiLingualResourcesConfig.ngOriginalFolder;
//		get20NG(path);
//		getLanguages(path);
//		getFileScript(path, ngFile);
		String[] strs = new File(path + "selected_original/").list();
		for (String s : strs) {
			System.out.println(s);
		}
	}
	
	public static void get20NG(String inputPath) throws IOException {
		ArrayList<String> lines = edu.illinois.cs.cogcomp.descartes.util.IOManager.readLines(inputPath + "output20NGSim.txt");
		BufferedWriter bw = IOManager.openWriter(inputPath + "filelist_new.txt");
		HashMap<String, HashMap<String, Double>> scores = new HashMap<String, HashMap<String, Double>>();
		
		for (String line : lines) {
			String[] strs = line.split("\t");
			String[] names = strs[0].split("\\\\");
			String category = names[4];
			String file = names[5];
			if (!scores.keySet().contains(category)) {
				scores.put(category, new HashMap<String, Double>());
			}
			
			String[] items = strs[1].split(" ");
			for (String item : items) {
				String label = item.substring(0, item.indexOf(','));
				if (label.equals(category)) {
					Double score = Double.parseDouble(item.substring(item.indexOf(',')+1, item.indexOf(';')));
					scores.get(category).put(file, score);
				}
			}
		}
		
		for (String cat : scores.keySet()) {
			HashMap<String, Double> map = scores.get(cat);
			bw.write(cat+"\t");
			for (int i = 0; i < top_n; i++) {
				Double max = 0.0;
				String file_key = "";
				for (String file_k : map.keySet()) {
					if (map.get(file_k) > max) {
						max = map.get(file_k);
						file_key = file_k;
					}
				}
				map.remove(file_key);
				bw.write(file_key + " ");
			}
			bw.write("\n");
		}
		IOManager.closeWriter(bw);
	}
	
	public static void getLanguages(String inputPath) throws IOException {
		ArrayList<String> lines = IOManager.readLines(inputPath + "wikiurls.txt");
		BufferedWriter bw = IOManager.openWriter(inputPath + "languagelist.txt");
		ArrayList<String> languages = new ArrayList<String>();
		for (String line : lines) {
			String lang = line.substring(line.lastIndexOf('/')+1, line.indexOf('-')-4);
			if (!languages.contains(lang)) {
				languages.add(lang);
			}
		}
		for (String str : languages) {
			bw.write(str + "\n");
		}
		IOManager.closeWriter(bw);
	}
	
	public static void getFileScript(String inputPath, String ngFile) throws IOException {
		ArrayList<String> lines = IOManager.readLines(inputPath + "filelist_new.txt");
		BufferedWriter bw = IOManager.openWriter(inputPath + "filelist_new.sh");
		for (String line : lines) {
			String category = line.split("\t")[0];
			String[] names = line.split("\t")[1].split(" ");
			int count = 0;
			for (String name : names) {
				if (count == 5) break;
				File file = new File(ngFile + category + "/" + name);
				if (file.exists()) {
					bw.write("cp ../20news-18828/" + category + "/" + name + " " + "selected_original/" + category + "_" + name + "\n");
					count++;
				}
			}
		}
		bw.close();
	}
	

}
