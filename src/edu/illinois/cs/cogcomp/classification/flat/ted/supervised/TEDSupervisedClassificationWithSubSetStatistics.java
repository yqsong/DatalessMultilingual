package edu.illinois.cs.cogcomp.classification.flat.ted.supervised;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;
import edu.illinois.cs.cogcomp.MultiLingualResourcesConfig;
import edu.illinois.cs.cogcomp.classification.flat.ted.data.TEDData;
import edu.illinois.cs.cogcomp.classification.flat.ted.data.TEDDataSplitting;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.jlis.CorpusDataProcessing;
import edu.illinois.cs.cogcomp.classification.hierarchy.evaluation.StatUtils;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;

public class TEDSupervisedClassificationWithSubSetStatistics {
	public static Random r = new Random(0);
	
	public static void main (String args[]) {
		MultiLingualResourcesConfig.initialization();
		
		int fold = 1;
		int selected = 0;
		SolverType solver = SolverType.L2R_L2LOSS_SVC_DUAL;  //  ClassifierConstant.solver;

		try {
			for (int i = 0; i < TEDData.lgNameArray.length; ++i) {
				int lgID = i;
				
				if (lgID % fold == selected) {
					List<Double> f1List = new ArrayList<Double>();
					for (int j = 0; j < TEDData.labelNameArray.length; ++j) {
						double f1 = testOneLabelOneLanguage (lgID, j, 0.1);
						f1List.add(f1);
					}
					
					for (int j = 0; j < TEDData.labelNameArray.length; ++j) {
						System.out.println("[Summary] " + "\t" + TEDData.lgNameArray[lgID] + "\t" + TEDData.labelNameArray[j] + "\t" + f1List.get(j));
					}
					
					double mean = StatUtils.listAverage(f1List);
					double std = StatUtils.std(f1List, mean);
					System.out.println("[Summary] " + "\t" + "ALL" + "\t" + mean + "\t" + std);
				}
				
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static double testOneLabelOneLanguage (int lgID, int labelID, double sampleRate) throws Exception {
		String lgName = TEDData.lgNameArray[lgID];
		String label = TEDData.labelNameArray[labelID];
		
		System.out.println("---------------------------------------------------------------------");
		System.out.println("Language: " + lgName + ", label: " + label);
		
		String upFolder = MultiLingualResourcesConfig.tedcldcPath;
		String trainPathPos = upFolder + lgName + "-en" + "/train/" + label + "/positive/";
		String trainPathNeg = upFolder + lgName + "-en" + "/train/" + label + "/negative/";
		String testPathPos = upFolder + lgName + "-en" + "/test/" + label + "/positive/";
		String testPathNeg = upFolder + lgName + "-en" + "/test/" + label + "/negative/";
		
		if (lgName.equals("en")) {
			
			trainPathPos = upFolder + "en-es"  + "/train/" + label + "/positive/";
			trainPathNeg = upFolder + "en-es"  + "/train/" + label + "/negative/";
			testPathPos = upFolder + "en-es"  + "/test/" + label + "/positive/";
			testPathNeg = upFolder + "en-es"  + "/test/" + label + "/negative/";
			
		} 
		
		TEDDataSplitting dataSplit = new TEDDataSplitting();
		dataSplit.loadOneLanguageOneLabel(lgName, trainPathPos, trainPathNeg, testPathPos, testPathNeg, false, sampleRate, r);

		List<Double> yList = new ArrayList<Double>();;
		List<FeatureNode[]> xList = new ArrayList<FeatureNode[]>();
		int posCount = 0;
		int negCount = 0;
		for (int i = 0; i < dataSplit.labeledTrainIDs.size(); ++i) {
			String docID = dataSplit.labeledTrainIDs.get(i);
			boolean lab = dataSplit.labeledTrainData.get(i).getSecond();
			double docLabel = 2;
			if (lab == true)
				posCount++;
			else
				negCount++;
		}
		
		System.out.println("[Training ] " + "Language: " + lgName + ", pos: " + posCount + ", neg: " + negCount);

		return dataSplit.labeledTrainIDs.size();
	}
}
