package edu.cmu.lti.f13.hw4.hw4_chenyinh.casconsumers;

import java.util.Map;
import java.util.Map.Entry;

public abstract class Similarity {

  /*
   * calculate the number of intersection elements in two vectors, convenient for JaccardSimilarity
   * and DiceSimilarity
   */
  public double countUinion(Map<String, Integer> queryVector, Map<String, Integer> docVector) {
    double sum = 0.0;
    for (Entry<String, Integer> entry : queryVector.entrySet()) {
      String term = entry.getKey();
      double qtf = (double) entry.getValue();
      String term1 = term + "s";
      String term2 = term + "es";
      if (docVector.containsKey(term)) {
        sum = sum + Math.min(qtf, docVector.get(term));
      } else if (docVector.containsKey(term1)) {
        sum = sum + qtf * docVector.get(term1);
      } else if (docVector.containsKey(term2)) {
        sum = sum + qtf * docVector.get(term2);
      }
    }
    return sum;
  }

  /* compute the length of a vector */
  abstract double length(Map<String, Integer> Vector);

  /* compute the similarity score */
  public abstract double computeScore(Map<String, Integer> queryVector,
          Map<String, Integer> docVector);

}
