package edu.illinois.cs.cogcomp.translation;

import org.apache.thrift.TException;

import edu.illinois.cs.cogcomp.edison.data.curator.CuratorClient;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.thrift.base.AnnotationFailedException;
import edu.illinois.cs.cogcomp.thrift.base.ServiceUnavailableException;

public class CuratorTokenizer {
	
	private static String curatorHost = "trollope.cs.illinois.edu";
	private static String curatePort = "9010";
	private static CuratorClient curator = new CuratorClient(curatorHost, Integer.parseInt(curatePort));
	
	public static TextAnnotation createDoc(String text) throws ServiceUnavailableException, AnnotationFailedException, TException {
		TextAnnotation ta = curator.getTextAnnotation("", "", text, false);
		return ta;
	}
}
