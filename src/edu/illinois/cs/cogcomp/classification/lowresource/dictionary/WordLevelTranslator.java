package edu.illinois.cs.cogcomp.classification.lowresource.dictionary;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;

public class WordLevelTranslator {
  static final String dictionaryPath = "/home/data/corpora/panlex/panlex-20160801-csv/";
  //static final String dictionaryPath = "data/";

  private String sourceLanguageCode = "";
  private String targetLanguageCode = "";
  HashMap<String, String> sourceTextIdMap = new HashMap<String, String>();
  HashMap<String, String> targetIdTextMap = new HashMap<String, String>();
  HashMap<String, String> meaningMap = new HashMap<String, String>();

  public static void main(String[] args) throws IOException {
	    WordLevelTranslator translator = new WordLevelTranslator("1627", "187");
	    Scanner sc = new Scanner(System.in);
	    for (;;) {
	      String sourceString = sc.nextLine();
	      if (!sourceString.equals("q"))
	        translator.translateText(sourceString);
	      else
	        break;
	    }
  }
  
  private void addMeaning(HashMap<String, HashSet<String>> meaningMap,
      ArrayList<String> sourceExpressionList, HashSet<String> targetExpressionSet) {
    if (sourceExpressionList.isEmpty() || targetExpressionSet.isEmpty())
      return;
    for (String sourceExpression : sourceExpressionList) {
      if (!meaningMap.containsKey(sourceExpression)) {
        meaningMap.put(sourceExpression, targetExpressionSet);
      }
      else {
        meaningMap.get(sourceExpression).addAll(targetExpressionSet);
      }
    }
  }

  private void addMeaningCount(HashMap<String, HashMap<String, Integer>> meaningMap,
      ArrayList<String> sourceExpressionList, HashSet<String> targetExpressionSet) {
    if (sourceExpressionList.isEmpty() || targetExpressionSet.isEmpty())
      return;
    for (String sourceExpression : sourceExpressionList) {
      HashMap<String, Integer> tempTargetMap;
      boolean isNewMap = false;
      if (!meaningMap.containsKey(sourceExpression)) {
        tempTargetMap = new HashMap<String, Integer>();
        isNewMap = true;
      }
      else
        tempTargetMap = meaningMap.get(sourceExpression);

      for (String targetExpression : targetExpressionSet) {
        if (!tempTargetMap.containsKey(targetExpression))
          tempTargetMap.put(targetExpression, 1);
        else {
          int tmp = tempTargetMap.get(targetExpression);
          tmp += 1;
          tempTargetMap.remove(targetExpression);
          tempTargetMap.put(targetExpression, new Integer(tmp));
        }
      }

      if (isNewMap)
        meaningMap.put(sourceExpression, tempTargetMap);
    }
  }

  public WordLevelTranslator(String sourceLanguageCode, String targetLanguageCode) {
    System.out.println("WordLevelTranslator initialized with source language "
      + sourceLanguageCode + " and " + targetLanguageCode);
    FileInputStream inputStream = null;
    Scanner sc = null;
    HashSet<String> sourceIdSet = new HashSet<String>();
    HashSet<String> targetIdSet = new HashSet<String>();
    HashMap<String, HashMap<String, Integer>> tempMeaningMap = new HashMap<String, HashMap<String, Integer>>();

    try {
      inputStream = new FileInputStream(dictionaryPath + "ex.csv");
      sc = new Scanner(inputStream, "UTF-8");
      System.out.println("Reading expressions..");
      sc.nextLine();
      while (sc.hasNextLine()) {
        String line = sc.nextLine();
        String[] fields = line.split(",");
        if (fields.length != 4) continue;
        if (fields[1].equals(sourceLanguageCode)) {
          sourceTextIdMap.put(fields[2], fields[0]);
          sourceIdSet.add(fields[0]);
        }
        else if (fields[1].equals(targetLanguageCode)) {
          targetIdTextMap.put(fields[0], fields[2]);
          targetIdSet.add(fields[0]);
        }
      }
      
      inputStream = new FileInputStream(dictionaryPath + "dn.csv");
      sc = new Scanner(inputStream, "UTF-8");
      System.out.println("Reading denotations..");
      sc.nextLine();
      String currentMeaning = "";
      // a meaning may correspond to multiple expressions
      ArrayList<String> sourceExpressionList = new ArrayList<String>();
      HashSet<String> targetExpressionSet = new HashSet<String>();
      while (sc.hasNextLine()) {
        String line = sc.nextLine();
        String[] fields = line.split(",");
        if (fields.length != 3) continue;

        // process new meaning
        if (!fields[1].equals(currentMeaning)) {
          addMeaningCount(tempMeaningMap, sourceExpressionList, targetExpressionSet);
          currentMeaning = fields[1];
          sourceExpressionList = new ArrayList<String>();
          targetExpressionSet = new HashSet<String>();
        }

        // add denotation item
        if (sourceIdSet.contains(fields[2])) {
          sourceExpressionList.add(fields[2]);
        }
        if (targetIdSet.contains(fields[2])) {
          targetExpressionSet.add(fields[2]);
        }
      }
      addMeaningCount(tempMeaningMap, sourceExpressionList, targetExpressionSet);

      // choose the meaning with max match
      for (String sourceExpression : tempMeaningMap.keySet()) {
        int maxCount = 0;
        String maxTarget = "";
        HashMap<String, Integer> targetMap = tempMeaningMap.get(sourceExpression);
        for (String targetExpression : targetMap.keySet()) {
          int currentCount = targetMap.get(targetExpression);
          if (currentCount > maxCount) {
            maxCount = currentCount;
            maxTarget = targetExpression;
          }
        }
        meaningMap.put(sourceExpression, maxTarget);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      if (sc != null) {
        sc.close();
      }
    }
    System.out.println("Total source: " + targetIdSet.size());
    System.out.println("Total target: " + sourceIdSet.size());
    System.out.println("Total translations: " + meaningMap.size());
  }

  public void translateText(String sourceText) {
    String sourceId = sourceTextIdMap.get(sourceText);
    if (sourceId == null) {
      System.out.println("Source text not found!");
      return;
    }
    if (!meaningMap.containsKey(sourceId)) {
      System.out.println("No translated items found!");
      return;
    }
    System.out.println(targetIdTextMap.get(meaningMap.get(sourceId)));
  }

  // return the translated string
  // null if error occurs
  public String translate(String sourceText) {
    String sourceId = sourceTextIdMap.get(sourceText);
    if (sourceId == null) return null;
    if (!meaningMap.containsKey(sourceId)) return null;
    return targetIdTextMap.get(meaningMap.get(sourceId));
  }

 
}
 
