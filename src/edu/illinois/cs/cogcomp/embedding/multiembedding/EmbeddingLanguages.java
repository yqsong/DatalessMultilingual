package edu.illinois.cs.cogcomp.embedding.multiembedding;

import java.util.HashMap;

public class EmbeddingLanguages {

	
	public static HashMap<String, String> ted_availability = new HashMap<String, String>() {
		{
			put("ar", "ar");
			put("de", "de");
			put("es", "es");
			put("fr", "fr");
			put("it", "it");
			put("nl", "nl");
			put("pt", "pt"); put("pb", "pt");
			put("pl", "pl");
			put("ro", "ro");
			put("ru", "ru");
			put("tr", "tr");
			put("zh", "zh");
			put("en", "en");
		}
	};
	
	public static HashMap<String, String> europarl_availability = new HashMap<String, String>() {
		{
			put("da", "da");
			put("de", "de");
			put("es", "es");
			put("fr", "fr");
			put("it", "it");
			put("nl", "nl");
			put("pl", "pl");
			put("pt", "pt"); put("pb", "pt");
			put("ro", "ro");
			put("sv", "sv");
		}
	};
	
	public static HashMap<String, String> muse_availability = new HashMap<String, String>() {
		{
			put("en", "en");
			put("ar", "ar");
			put("da", "da");
			put("de", "de");
			put("es", "es");
			put("fr", "fr");
			put("it", "it");
			put("nl", "nl");
			put("no", "no");
			put("pl", "pl");
			put("pt", "pt"); 
			put("ro", "ro");
			put("ru", "ru");
			put("sv", "sv");
			put("tr", "tr");
			
		}
	};
}
