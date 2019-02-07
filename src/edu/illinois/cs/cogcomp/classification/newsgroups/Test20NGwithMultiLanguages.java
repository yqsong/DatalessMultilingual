package edu.illinois.cs.cogcomp.classification.newsgroups;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.illinois.cs.cogcomp.MultiLingualResourcesConfig;
import edu.illinois.cs.cogcomp.classification.newsgroups.data.NGData;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.descartes.util.IOManager;
import edu.illinois.cs.cogcomp.embedding.esa.index.LanguageTextNormalization;

public class Test20NGwithMultiLanguages {
	
	public static void main (String[] args) {
		MultiLingualResourcesConfig.initialization();
		testAllLanguages (MultiLingualResourcesConfig.multiLingual20NGFolder);
	}

	public static void testAllLanguages (String folderName) {
		HashMap<String, Double> orgIndexMap = new HashMap<String, Double>();
		HashMap<String, Double> mergedIndexMap = new HashMap<String, Double>();
		int topK = 1;
		File folder = new File(folderName);
		File[] fileList = folder.listFiles();
		for (int i = 0; i < fileList.length; ++i) {
			File subFolder = fileList[i];
			//Lucenen 5.3.0 for English is worse than 3.0.3 version
//			if (subFolder.getName().equals("sh") == false)
//				continue;
			if (subFolder.getName().equals("en"))
				continue;
			if (subFolder.isDirectory()) {
				NGData data = NGData.readData(subFolder.getAbsolutePath());
				
				double precision = 0;
				
				precision = NG20ClassificationWithOriginalIndex.test(data, topK);
				orgIndexMap.put(data.lgName, precision);
				System.out.println("[Original Index]\t" + data.lgName + "\t" + precision);
				System.out.println();
				
				precision = NG20ClassificationWithMergedIndex.test(data, topK);
				mergedIndexMap.put(data.lgName, precision);
				System.out.println("[Merged Index]\t" + data.lgName + "\t" + precision);
				System.out.println();
			}
		}
		
		System.out.println("-------------------Original Index--------------------");
		for (String lgName : orgIndexMap.keySet()){
			System.out.println(lgName + "\t" + orgIndexMap.get(lgName));
		}
		System.out.println("-------------------Merged Index--------------------");
		for (String lgName : mergedIndexMap.keySet()){
			System.out.println(lgName + "\t" + mergedIndexMap.get(lgName));
		}
	}
	

}
