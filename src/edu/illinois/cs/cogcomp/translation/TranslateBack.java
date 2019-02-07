package edu.illinois.cs.cogcomp.translation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URLEncoder;
import java.util.ArrayList;

import edu.illinois.cs.cogcomp.MultiLingualResourcesConfig;
import edu.illinois.cs.cogcomp.descartes.util.IOManager;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;

public class TranslateBack {

	public static void main(String[] args) throws Exception {
		MultiLingualResourcesConfig.initialization();
		File inputFolder = new File(MultiLingualResourcesConfig.multiLingual20NGFolder);
		String outputStr = MultiLingualResourcesConfig.multiLingual20NGEnBackFolder;
		
		File[] folderList = inputFolder.listFiles();
		for (File file : folderList) {
			if (file.isDirectory()) {
				File[] listFile = file.listFiles();
				String lgName = "";
				for (int i = 0; i < listFile.length; ++i) {
					String fileName = listFile[i].getName();
					if (fileName.startsWith("label.") == true) {
						lgName = fileName.substring("label.".length());
					}
				}
				
				String outputLgFolderStr = outputStr + "/" + lgName;
				File outputLgFolder = new File(outputLgFolderStr);
				if (outputLgFolder.exists() == false) {
					outputLgFolder.mkdirs();
				}
				System.out.println("------------------------------" + lgName + "------------------------------");
				for (int i = 0; i < listFile.length; ++i) {
					String fileName = listFile[i].getName();
					String outputFile = outputLgFolderStr + "/" + fileName;
					
					if (fileName.startsWith("label.") == false) {
						System.out.println(fileName);
						FileWriter writer = new FileWriter(outputFile);
						ArrayList<String> lines = IOManager.readLines(listFile[i].getAbsolutePath());
						for (int j = 0; j < lines.size(); ++j) {
							String newLine = Translate.googleTranslate(lines.get(j), "en", lgName);
							writer.write(newLine + "\n");
						}
						writer.close();
					}
				}
			}
		}

	}
	   
}
