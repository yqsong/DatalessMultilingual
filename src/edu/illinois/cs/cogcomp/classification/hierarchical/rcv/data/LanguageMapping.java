package edu.illinois.cs.cogcomp.classification.hierarchical.rcv.data;

import java.util.HashMap;

public class LanguageMapping {

	public static String[] lgNames = new String[] 		{
		"english",
		"chinese", 
		"danish",
		"dutch",
		"french",
		"german",
		"italian",
		"japanese",
		"norwegian", 
		"portuguese",
		"russian",
		"spanish",
		"spanish-latam", 
		"swedish", 
	};
	
	public static HashMap<String, String> lgMapping = new HashMap<String, String>() {
		{
			put("english", "en");
			put("chinese", "zh");
			put("danish", "da");
			put("dutch", "nl");
			put("french", "fr");
			put("german", "de");
			put("italian", "it");
			put("japanese", "ja");
			put("norwegian", "no");
			put("portuguese", "pt");
			put("russian", "ro");
			put("spanish", "es");
			put("spanish-latam", "es");
			put("swedish", "sv");
		}
	};
	
}
