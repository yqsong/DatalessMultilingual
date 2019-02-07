package edu.illinois.cs.cogcomp.classification.flat.ted.data;

import java.util.List;

import edu.illinois.cs.cogcomp.descartes.util.IOManager;

public class TEDData {
	public static String systemNewLine = System.getProperty("line.separator");
	
	public static String[] lgNameArray = new String[] {
			"ar", "de", "es", "fr", "it", "nl", "pb", "pl", "ro", "ru", "tr", "zh", "en"
	};
	
	public static String[] labelNameArray = new String[] {
			"art",
			"arts",
			"biology",
			"business",
			"creativity",
			"culture",
			"design",
			"economics",
			"education",
			"entertainment",
			"global",
			"health",
			"politics",
			"science",
			"technology",
	};
	
	public static double computePositiveF1 (List<Boolean> goldList, List<Boolean> predList) throws Exception {
		double tp = 0; 
		double fp = 0;
		double fn = 0;
		if (goldList.size() != predList.size()) {
			throw new Exception();
		}
		for (int i = 0; i < goldList.size(); ++i) {
			if (goldList.get(i) == true && goldList.get(i) == predList.get(i)) {
				tp += 1;
			}
			if (goldList.get(i) == true &&  predList.get(i) == false) {
				fn += 1;
			}
			if (goldList.get(i) == false &&  predList.get(i) == true) {
				fp += 1;
			}
		}
		double p = tp / (tp + fp + Double.MIN_NORMAL);
		double r = tp / (tp + fn + Double.MIN_NORMAL);
		double f = 2 * p * r / (p + r + Double.MIN_NORMAL);
//		System.out.println("  [Eval] p=" 
//				+ String.format("%.4f", p)
//				+ ",r=" + String.format("%.4f", r)
//				+ ",f1=" + String.format("%.4f", f)
//				);
		return f;
	}
	

}
