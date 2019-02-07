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

public class TEDSupervisedClassification {
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
						double f1 = testOneLabelOneLanguage (lgID, j, 10000, 0.01, solver);
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
//			int lgID = 12;
//			for (double C = 100000; C < 100000000; C = C * 10) {
//				List<Double> f1List = new ArrayList<Double>();
//				for (int j = 0; j < TEDData.labelNameArray.length; ++j) {
//					double f1 = testOneLabelOneLanguage (lgID, j, C, 0.0001);
//					f1List.add(f1);
//				}
//				for (int j = 0; j < TEDData.labelNameArray.length; ++j) {
//					System.out.println("[Summary] " + "\t" + TEDData.lgNameArray[lgID] + "\t" + TEDData.labelNameArray[j] + "\t" + f1List.get(j));
//				}
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static double testOneLabelOneLanguage (int lgID, int labelID, double C, double eps, SolverType solver) throws Exception {
		String lgName = TEDData.lgNameArray[lgID];
		String label = TEDData.labelNameArray[labelID];
		
		System.out.println("-----------------------------------C=" + C + "----------------------------------");
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
		dataSplit.loadOneLanguageOneLabel(lgName, trainPathPos, trainPathNeg, testPathPos, testPathNeg, false);

		HashMap<String, String> corpusStringMap = new HashMap<String, String>();
		for (int i = 0; i < dataSplit.labeledTrainIDs.size(); ++i) {
			String id = dataSplit.labeledTrainIDs.get(i);
			String content = dataSplit.labeledTrainData.get(i).getFirst();
			corpusStringMap.put(id, content);
		}
		
		CorpusDataProcessing corpus = new CorpusDataProcessing();
		corpus.startFromZero = false;
		HashMap<String, String> trainingDataLibSVMFormat = corpus.initializeTrainingDocumentFeatures (corpusStringMap, true, true);

		List<Double> yList = new ArrayList<Double>();;
		List<FeatureNode[]> xList = new ArrayList<FeatureNode[]>();
		for (int i = 0; i < dataSplit.labeledTrainIDs.size(); ++i) {
			String docID = dataSplit.labeledTrainIDs.get(i);
			boolean lab = dataSplit.labeledTrainData.get(i).getSecond();
			double docLabel = 2;
			if (lab == true)
				docLabel = 1;
			
			// initialize labal for libLinear
			yList.add(docLabel);
			
			// initialize data for libLinear
			String[] tokens = trainingDataLibSVMFormat.get(docID).split(" ");
			FeatureNode[] feature = new FeatureNode[tokens.length];
			for (int j = 0; j < tokens.length; ++j) {
				String[] subTokens = tokens[j].trim().split(":");
				int index = Integer.parseInt(subTokens[0].trim());
				double value = Double.parseDouble(subTokens[1].trim());
				feature[j] = new FeatureNode(index, value);
			}
			xList.add(feature);
		}
		
		int maxIter = 10;
		int thresholdNum = 1000;
		double[] thresholdArray = new double[thresholdNum];
		for (int i = 0; i < thresholdNum; i++) {
			thresholdArray[i] = i * 0.001;
		}
		List<List<Double>> f1ScoreListAll = new ArrayList<List<Double>>();
		for (int iter = 0; iter < maxIter; ++ iter) {
			List<Double> yListTrain = new ArrayList<Double>();;
			List<FeatureNode[]> xListTrain = new ArrayList<FeatureNode[]>();
			List<Double> yListDev = new ArrayList<Double>();;
			List<FeatureNode[]> xListDev = new ArrayList<FeatureNode[]>();
			for (int i = 0; i < yList.size(); ++i) {
				double v = r.nextDouble();
				if (v < 0.7) {
					yListTrain.add(yList.get(i));
					xListTrain.add(xList.get(i));
				} else {
					yListDev.add(yList.get(i));
					xListDev.add(xList.get(i));
				}
			}
			
			Feature[][] xArray = new Feature[xListTrain.size()][];
			double[] yArray = new double[xListTrain.size()];
			for (int i = 0; i < xListTrain.size(); ++i) {
				yArray[i] = yListTrain.get(i);
				xArray[i] = xListTrain.get(i);
			}
			Problem problem = new Problem();
			problem.l = xListTrain.size(); // number of training examples
			problem.n = corpus.getDictSize(); // number of features
			problem.x = xArray; // feature nodes
			problem.y = yArray; // target values
			
			Model model = null;
			
			try {
				if (problem != null) {
				    Calendar cal = Calendar.getInstance();
				    long startTime = cal.getTimeInMillis();
					System.out.println("  [Dev:] svm for: " + label + "; data num: " + problem.l + "; feature num: " + problem.n);
					
					Parameter parameter = new Parameter(solver, C, eps);
					
					if (problem.l > 0) {
						Linear.disableDebugOutput();
						model = Linear.train(problem, parameter);
					}
					Calendar cal1 = Calendar.getInstance();
		    		long endTime = cal1.getTimeInMillis();
		    		long second = (endTime - startTime)/1000;
					System.out.println("  [Dev:] finished," + " time: " + second + " seconds");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			List<Double> trainScoreList = new ArrayList<Double>();
			for (int i = 0; i < xListDev.size(); ++i) {
				Feature[] xTest = xListDev.get(i);
		        double[] decValues = new double[2];
		        double yTest = Linear.predictValues(model, xTest, decValues);
		        int outputLabel = (int) yTest;

		        double[] transformedValues = new double[decValues.length];
		        int nr_class = decValues.length;
		        int nr_w;
		        if (nr_class == 2)
		            nr_w = 1;
		        else
		            nr_w = nr_class;

		        //a key problem of original svm prob output	Output a lot of zeroes when lack of training data.        
		        for (int j = 0; j < nr_w; j++) {
		        	transformedValues[j] = 1 / (1 + Math.exp(-decValues[j] + Math.random() * 1e-10));
		        }
		        
		        for (int j = 0; j < nr_w; j++) {
		        	if (decValues[j] == 0) {
		        		transformedValues[j] = 0;
		        	}
		        }

		        if (nr_class == 2) // for binary classification
		        	transformedValues[1] = 1. - transformedValues[0];
		        else {
		            double sum = 0;
		            for (int j = 0; j < nr_class; j++)
		                sum += transformedValues[j];

		            for (int j = 0; j < nr_class; j++)
		            	transformedValues[j] = transformedValues[j] / sum;
		        }
		        trainScoreList.add(transformedValues[0]);
			}
			List<Double> f1ScoreList = new ArrayList<Double>();
			for (int t = 0; t < thresholdNum; t++) {
				double threshold = thresholdArray[t];
				List<Boolean> goldList = new ArrayList<Boolean>();
				for (int i = 0; i < yListDev.size(); ++i) {
					if (yListDev.get(i) == 1)
						goldList.add(true);
					else 
						goldList.add(false);
				}
				List<Boolean> predList = new ArrayList<Boolean>();
				for (int i = 0; i < xListDev.size(); ++i) {
					if (trainScoreList.get(i) > threshold) {
						predList.add(true);
					} else {
						predList.add(false);
					}
				}
				double f1 = TEDData.computePositiveF1 (goldList, predList);
				f1ScoreList.add(f1);
			}
			f1ScoreListAll.add(f1ScoreList);
		}
		double[] f1ScoreList = new double[thresholdNum];
		for (int i = 0; i < f1ScoreListAll.size(); ++i) {
			for (int j = 0; j < f1ScoreListAll.get(i).size(); ++j) {
				f1ScoreList[j] += f1ScoreListAll.get(i).get(j);
			}
		}
		
		double bestScore = 0;
		double bestThreshold = 0;
		for (int t = 0; t < thresholdNum; t++) {
			double threshold = thresholdArray[t];
			double f1 = f1ScoreList[t];
			if (bestScore < f1) {
				bestScore = f1;
				bestThreshold = threshold;
			}
		}
		System.out.println("[Best Training Threshold]  " + bestThreshold + ", F1: " + bestScore);
		
		
		Feature[][] xArray = new Feature[xList.size()][];
		double[] yArray = new double[xList.size()];
		for (int i = 0; i < xList.size(); ++i) {
			yArray[i] = yList.get(i);
			xArray[i] = xList.get(i);
		}
		Problem problem = new Problem();
		problem.l = xList.size(); // number of training examples
		problem.n = corpus.getDictSize(); // number of features
		problem.x = xArray; // feature nodes
		problem.y = yArray; // target values
		
		Model model = null;
		
		try {
			if (problem != null) {
			    Calendar cal = Calendar.getInstance();
			    long startTime = cal.getTimeInMillis();
				System.out.println("  [Training:] svm for: " + label + "; data num: " + problem.l + "; feature num: " + problem.n);
				
				Parameter parameter = new Parameter(solver, C, eps);
				
				if (problem.l > 0) {
					Linear.disableDebugOutput();
					model = Linear.train(problem, parameter);
				}
				Calendar cal1 = Calendar.getInstance();
	    		long endTime = cal1.getTimeInMillis();
	    		long second = (endTime - startTime)/1000;
				System.out.println("  [Training:] finished," + " time: " + second + " seconds");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<Boolean> goldList = new ArrayList<Boolean>();
		List<Boolean> predList = new ArrayList<Boolean>();
		for (int i = 0; i < dataSplit.labeledTestIDs.size(); ++i) {
			String docID = dataSplit.labeledTestIDs.get(i);
			String docContent = dataSplit.labeledTestData.get(i).getFirst();
			boolean docLabel = dataSplit.labeledTestData.get(i).getSecond();
			goldList.add(docLabel);
			String docLibSVMFormat = corpus.convertTestDocContentToTFIDF (docContent, true, true);
			
			try {
				String[] tokens = docLibSVMFormat.trim().split(" ");
				// initialize data for libLinear
				FeatureNode[] xTest = new FeatureNode[tokens.length];
				for (int j = 0; j < tokens.length; ++j) {
					String[] subTokens = tokens[j].trim().split(":");
					if (subTokens.length < 2) {
						xTest[j] = new FeatureNode(1, 0);
						continue;
					}
					int index = Integer.parseInt(subTokens[0].trim());
					double value = Double.parseDouble(subTokens[1].trim());
					xTest[j] = new FeatureNode(index, value);
				}
		        
				
		        double[] decValues = new double[2];
		        double yTest = Linear.predictValues(model, xTest, decValues);
		        int outputLabel = (int) yTest;

		        double[] transformedValues = new double[decValues.length];
		        int nr_class = decValues.length;
		        int nr_w;
		        if (nr_class == 2)
		            nr_w = 1;
		        else
		            nr_w = nr_class;

		        //a key problem of original svm prob output	Output a lot of zeroes when lack of training data.        
		        for (int j = 0; j < nr_w; j++) {
		        	transformedValues[j] = 1 / (1 + Math.exp(-decValues[j] + Math.random() * 1e-10));
		        }
		        
		        for (int j = 0; j < nr_w; j++) {
		        	if (decValues[j] == 0) {
		        		transformedValues[j] = 0;
		        	}
		        }

		        if (nr_class == 2) // for binary classification
		        	transformedValues[1] = 1. - transformedValues[0];
		        else {
		            double sum = 0;
		            for (int j = 0; j < nr_class; j++)
		                sum += transformedValues[j];

		            for (int j = 0; j < nr_class; j++)
		            	transformedValues[j] = transformedValues[j] / sum;
		        }
				
		        boolean booleanPredLabel = false;
//		        if (outputLabel == 1) 
		        if (transformedValues[0] > bestThreshold)
		        	booleanPredLabel = true;
		        
		        predList.add(booleanPredLabel);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		double f1 = 0;
		f1 = TEDData.computePositiveF1 (goldList, predList);
		System.out.println("[Testing ] " + "Language: " + lgName + ", label: " + label + ", F1: " + f1);
		return f1;
	}
}
