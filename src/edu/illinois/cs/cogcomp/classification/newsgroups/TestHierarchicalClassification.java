package edu.illinois.cs.cogcomp.classification.newsgroups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.AbstractConceptTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ConceptTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml.ConceptTreeTopDownML;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.ConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;
import edu.illinois.cs.cogcomp.classification.main.CustomizedLabelDataTree;
import edu.illinois.cs.cogcomp.classification.main.DatalessResourcesConfig;
import edu.illinois.cs.cogcomp.classification.representation.esa.AbstractESA;
import edu.illinois.cs.cogcomp.classification.representation.esa.simple.SimpleESALocal;
import edu.illinois.cs.cogcomp.descartes.AnalyzerFactory;
import edu.illinois.cs.cogcomp.descartes.retrieval.simple.Searcher;
import edu.illinois.cs.cogcomp.descartes.util.IOManager;
import edu.illinois.cs.cogcomp.embedding.esa.index.CrossLanguageWikipediaDocumentIndex;
import edu.illinois.cs.cogcomp.embedding.esa.multiesa.MultiLingalESA;

public class TestHierarchicalClassification {
	
	public static String lgIndexFolder = "/shared/corpora/yqsong/data/wikipedia/crossLanguageIndexNew/merged/";
	public static String[] languageIndexFolders = new String[] {
			"dewiki-20150826-merged", 
			"eswiki-20150826-merged", 
			"frwiki-20150826-merged", 
			"hiwiki-20150826-merged", 
			"plwiki-20150826-merged", 
			"ruwiki-20150826-merged"};
	public static HashMap<String, Double> conceptWeights = new HashMap<String, Double>();
	
	public static String contentFolder = "/shared/shelley/yqsong/data/wikipedia_crossLanguage/testData/";
	public static String[] languageContentFiles = new String[] {
			"de_basketball.txt", 
			"es_basketball.txt", 
			"fr_basketball.txt", 
			"hi_basketball.txt", 
			"pl_basketball.txt", 
			"ru_basketball.txt"};
	
	public static void main (String[] args) {
		
		
		String content = "";
		int langID = 0;
//		//Deutsch
//		System.out.println("------------------------------------------------------------------------");
//		System.out.println("[Language] Deutsch");
//		langID = 0;
//		content = IOManager.readContent(contentFolder + languageContentFiles[langID]);
//		String deAbs = "Basketball ist eine meist in der Halle betriebene Ballsportart, bei der zwei Mannschaften versuchen, den Ball in den jeweils gegnerischen Korb zu werfen. Die Körbe sind 3,05 Meter hoch an den beiden Schmalseiten des Spielfelds angebracht. Eine Mannschaft besteht aus fünf Feldspielern und bis zu sieben Auswechselspielern, die beliebig oft wechseln können. Jeder Treffer in den Korb aus dem Spiel heraus zählt je nach Entfernung zwei oder drei Punkte. Ein getroffener Freiwurf zählt einen Punkt. Es gewinnt die Mannschaft mit der höheren Punktzahl.";
//		test (deAbs, content, langID);
//		
//		//Español
//		System.out.println("------------------------------------------------------------------------");
//		System.out.println("[Language] Español");
//		langID = 1;
//		content = IOManager.readContent(contentFolder + languageContentFiles[langID]);
//		String esAbs = "El baloncesto, basquetbol o básquetbol (del inglés basketball; de basket, 'canasta', y ball, 'pelota'),Nota 1 o simplemente básquet,1 es un deporte de equipo que se puede desarrollar tanto en pista cubierta como en descubierta, en el que dos conjuntos de cinco jugadores cada uno, intentan anotar puntos, también llamados canastas o dobles y/o triples introduciendo un balón en un aro colocado a 3,05 metros del suelo del que cuelga una red, lo que le da un aspecto de cesta o canasta.";
//		test (esAbs, content, langID);
//		
//		//Français
//		System.out.println("------------------------------------------------------------------------");
//		System.out.println("[Language] Français");
//		langID = 2;
//		content = IOManager.readContent(contentFolder + languageContentFiles[langID]);
//		String frAbs = "Le basket-ball ou basketball6, fréquemment désigné en français par son apocope basket, est un sport collectif opposant deux équipes de cinq joueurs sur un terrain rectangulaire. L'objectif de chaque équipe est de faire passer un ballon au sein d'un arceau de 46 cm de diamètre, fixé à un panneau et placé à 3,05 m du sol : le panier. Chaque panier inscrit rapporte deux points à son équipe, à l'exception des tirs effectués derrière la ligne des trois points et des lancers francs accordés à la suite d'une faute, qui ne rapportent qu'un seul point. L'équipe avec le nombre de points le plus important remporte la partie";
//		test (frAbs, content, langID);
		
		//हिन्दी
		System.out.println("------------------------------------------------------------------------");
		System.out.println("[Language] हिन्दी");
		langID = 3;
		content = IOManager.readContent(contentFolder + languageContentFiles[langID]);
		String hiAbs = "बास्केटबॉल एक टीम खेल है, जिसमें 5 सक्रिय खिलाड़ी वाली दो टीमें होती हैं, जो एक दूसरे के खिलाफ़ एक 10 फुट (3,048 मीटर) ऊंचे घेरे (गोल) में, संगठित नियमों के तहत एक गेंद डाल कर अंक अर्जित करने की कोशिश करती हैं। बास्केटबॉल, विश्व के सबसे लोकप्रिय और व्यापक रूप से देखे जाने वाले खेलों में से एक है।[1] गेंद को ऊपर से टोकरी के आर-पार फेंक कर (शूटिंग) अंक बनाए जाते हैं; खेल के अंत में अधिक अंकों वाली टीम जीत जाती है। गेंद को कोर्ट में उछालते हुए (ड्रिब्लिंग) या साथियों के बीच आदान-प्रदान करके आगे बढ़ाया जाता है। बाधित शारीरिक संपर्क (फाउल) को दंडित किया जाता है और गेंद को कैसे संभाला जाए इस पर पाबंदियां हैं (उल्लंघन). समय के साथ, बास्केटबॉल ने विकास करते हुए शूटिंग, पासिंग और ड्रिब्लिंग की आम तकनीकों के साथ-साथ खिलाड़ियों की स्थिति और आक्रामक और रक्षात्मक संरचनाओं को भी शामिल किया। आम तौर पर, टीम के सबसे लंबे सदस्य सेंटर या दो फॉरवर्ड पोज़ीशनों में से एक पर खेलते हैं, जबकि छोटे खिलाड़ी या वे जो गेंद को संभालने में सबसे दक्ष और तेज़ हैं, गार्ड पोज़ीशन पर खेलते हैं। जहां प्रतिस्पर्धी बास्केटबॉल को सावधानी से विनियमित किया गया है, यदा-कदा खेलने के लिए, बास्केटबॉल के कई परिवर्तित रूपों को विकसित किया गया है। कुछ देशों में, बास्केटबॉल एक लोकप्रिय दर्शक खेल भी है। जहां प्रतिस्पर्धी बास्केटबॉल मुख्य रूप से एक इनडोर खेल है, जिसे बास्केटबॉल कोर्ट पर खेला जाता है, वहीं आउटडोर खेले जाने वाले कम विनियमित भिन्न रूप, शहरों और ग्रामीण समूहों, दोनों के बीच तेजी से लोकप्रिय हो गए हैं।";
		test (hiAbs, content, langID);
		
//		//Polski
//		System.out.println("------------------------------------------------------------------------");
//		System.out.println("[Language] Polski");
//		langID = 4;
//		content = IOManager.readContent(contentFolder + languageContentFiles[langID]);
//		String plAbs = "Koszykówka powstała 21 grudnia 1891 roku w Springfield w stanie Massachusetts, gdy nauczyciel wychowania fizycznego w YMCA James Naismith opracował grę zespołową, którą mogliby uprawiać studenci college'u zimą w sali. Początkowo do gry w koszykówkę używano zwykłej piłki futbolowej[2]. Zasady koszykówki były bardzo proste i zostały spisane przez twórcę tego sportu. Gra ta stawała się coraz bardziej znana, a jej zasady zmieniały się. Wkrótce koszykówka była znana w całej Ameryce Północnej. 6 czerwca 1946 zostało założone BAA (Basketball Association of America), które jesienią 1949 przybrało nazwę National Basketball Association.";
//		test (plAbs, content, langID);
//		
//		//Русский
//		System.out.println("------------------------------------------------------------------------");
//		System.out.println("[Language] Русский");
//		langID = 5;
//		content = IOManager.readContent(contentFolder + languageContentFiles[langID]);
//		String ruAbs = "В баскетбол играют две команды, каждая из которых состоит из пяти полевых игроков (всего в каждой команде по 12 человек, замены не ограничены). Цель каждой команды — забросить руками мяч в кольцо с сеткой (корзину) соперника и помешать другой команде завладеть мячом и забросить его в свою корзину[1]. Корзина находится на высоте 3,05 м от пола (10 футов). За мяч, заброшенный с ближней и средней дистанций, засчитывается два очка, с дальней (из-за 3-х очковой линии) — три очка; штрафной бросок оценивается в одно очко. Стандартный размер баскетбольной площадки — 28 м в длину и 15 — в ширину. Баскетбол — один из самых популярных видов спорта в мире[2]. Баскетбол входит в программу Олимпийских игр с 1936 года (изобретатель игры Джеймс Нейсмит был там в качестве гостя). Регулярные чемпионаты мира по баскетболу среди мужчин проводятся с 1950 года, среди женщин — с 1953 года, а чемпионаты Европы — с 1935 года.";
//		test (ruAbs, content, langID);
	}

	public static void test (String abs, String content, int langID) {
		String lgName = languageIndexFolders[langID].substring(0, languageIndexFolders[langID].indexOf("wiki-2015"));
		
		content = abs;
		content = content.replaceAll("\\[", "");
		content = content.replaceAll("\\]", "");
		content = content.replaceAll("\\(", "");
		content = content.replaceAll("\\)", "");
		
		String indexFile = lgIndexFolder + languageIndexFolders[langID];
		

		int conceptNum = 500;
		
		String fileTopicHierarchyPath = "data/yahooDir.txt";
		HashMap<String, Double> conceptWeights = new HashMap<String, Double>();
		ConceptTreeTopDownML tree = new ConceptTreeTopDownML(DatalessResourcesConfig.CONST_DATA_YAHOO, "simple", conceptWeights, true);
		
//		DatalessResourcesConfig.level = 1;
//		String fileTopicHierarchyPath = "";
//		HashMap<String, Double> conceptWeights = new HashMap<String, Double>();
//		DatalessResourcesConfig.level = 1;
//		ConceptTreeTopDownML tree = new ConceptTreeTopDownML(DatalessResourcesConfig.CONST_DATA_CUSTOMIZED, "simple", conceptWeights, true);

		tree.setDebug(false);
		tree.esa = new MultiLingalESA(lgName, indexFile);
		System.out.println("process tree...");
		tree.treeLabelData.readTreeHierarchy(fileTopicHierarchyPath);
		ConceptTreeNode rootNode = tree.initializeTree("root", 0);
		tree.setRootNode(rootNode);
		tree.aggregateChildrenDescription(rootNode);
		tree.setConceptNum(conceptNum);
		tree.conceptualizeTreeLabels(rootNode, ClassifierConstant.isBreakConcepts);
		
		System.out.println("Perform ESA over test data");
		
		AbstractESA esa = tree.esa;
		List<ConceptData> concepts = null;
		List<String> conceptsList = new ArrayList<String>();
		List<Double> scores = new ArrayList<Double>();

		try {
			concepts = esa.retrieveConcepts(content, conceptNum);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (concepts != null) {
			for (int i = 0; i < concepts.size(); i++) {
				conceptsList.add(concepts.get(i).concept + "");
				scores.add(concepts.get(i).score);
			}
		}
		
		SparseVector document = new SparseVector(conceptsList, scores, ClassifierConstant.isBreakConcepts, conceptWeights);
		HashMap<Integer, List<LabelKeyValuePair>> labelResultsInDepth = tree.labelDocumentML(document);
		
		System.out.println("[Document] " + abs);
		for (Integer depth : labelResultsInDepth.keySet()) {
			System.out.print("[Depth] " + depth + ": ");
			List<LabelKeyValuePair> kvp = labelResultsInDepth.get(depth);
			for (int i = 0; i < kvp.size(); ++i) {
				System.out.print(kvp.get(i).getLabel().trim() + "," + String.format("%.4f", kvp.get(i).getScore()) + "; ");
			}
			System.out.println();
		}
	}
}
