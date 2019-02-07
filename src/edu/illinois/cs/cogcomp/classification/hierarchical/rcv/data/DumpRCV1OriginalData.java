package edu.illinois.cs.cogcomp.classification.hierarchical.rcv.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import edu.illinois.cs.cogcomp.MultiLingualResourcesConfig;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.rcv.RCVCorpusConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;

public class DumpRCV1OriginalData {

	public static void main (String[] args) {
		MultiLingualResourcesConfig.initialization();
		process();
	}
	
	public static void process () {
		int seed = 0;
		Random random = new Random(seed);
		double trainingRate = 0.5;
		
		String inputData = MultiLingualResourcesConfig.rcv1v2Training;
		
		RCVCorpusConceptData rcvData = new RCVCorpusConceptData();
		rcvData.readCorpusContentOnly(inputData, random, trainingRate);
		
		HashSet<String> dataIDList = new HashSet<String>(rcvData.getCorpusContentMap().keySet());
		
		for (String id : dataIDList) {
			System.out.println(id);
		}
		
		List<String> tempString = new ArrayList<String>();
        StringBuffer sbf = new StringBuffer();
        
		String rcvZipData1 = MultiLingualResourcesConfig.rcv1ZipData1;
		String rcvZipData2 = MultiLingualResourcesConfig.rcv1ZipData2;
		
		File zipDir1 = new File (rcvZipData1);
		File zipDir2 = new File (rcvZipData2);
		File[] fileList1 = zipDir1.listFiles();
		File[] fileList2 = zipDir2.listFiles();
		
		List<File> fileList = new ArrayList<File>();
		for (int i = 0; i < fileList1.length; ++i) {
			fileList.add(fileList1[i]);
		}
		for (int i = 0; i < fileList2.length; ++i) {
			fileList.add(fileList2[i]);
		}

		
		String outputContent = MultiLingualResourcesConfig.rcv1DataProcessed;
		
		FileWriter writer = null;
		try {
			writer = new FileWriter(outputContent);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for (int i = 0; i < fileList.size(); ++i) {
			File file = fileList.get(i);
			
			InputStream input;
	        try {
	        	System.out.println("Processing " + file);
	        	
	        	if (file.getName().endsWith("zip") == false) 
	        		continue;
	        	
	        	ZipFile zipFile = new ZipFile(file);

	        	Enumeration<? extends ZipEntry> entries = zipFile.entries();

	        	while(entries.hasMoreElements()){
	        		ZipEntry entry = entries.nextElement();
	        		
	        		if (!entry.isDirectory()) {
	                    final String fileName = entry.getName();
	                    if (fileName.endsWith(".xml")) {
	                    	int index = fileName.indexOf("news");
	                    	String subStr = fileName.substring(0, index);
	                    	if (dataIDList.contains(subStr)) {
	                    		InputStream zipInput = zipFile.getInputStream(entry);
	                    		BufferedReader br = new BufferedReader(new InputStreamReader(zipInput, "UTF-8"));
		                        String line;
		                        String document = "";
		                        while((line = br.readLine()) != null) {
		                        	document += line + "";
		                        }
		                        br.close();
		                        zipInput.close();
		                        
		                        Pattern pattern = Pattern.compile("<p>(.*?)</p>");
		                        Matcher matcher = pattern.matcher(document);
		                        String docClean = "";
		                        while (matcher.find()) {
		                        	docClean += matcher.group(1).replace("&quot;", "") + " ";
		                        }	
		                        System.out.println("Write document " + subStr);
		                        
		                        docClean = docClean.replaceAll("[^a-zA-Z\\s]", "");
		                    	
		                        docClean = docClean.replaceAll("\\s+", " ");
		                    	
		                        writer.write(subStr + "\t" + docClean + "\n\r");
	                    	}
	                    }
	                }
	        	}
	        	writer.flush();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		}
		
      
		try {
			writer.flush();
			writer.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
}