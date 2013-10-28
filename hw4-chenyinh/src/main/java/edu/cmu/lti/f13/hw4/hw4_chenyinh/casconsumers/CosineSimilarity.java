package edu.cmu.lti.f13.hw4.hw4_chenyinh.casconsumers;

import java.util.Map;
import java.util.Map.Entry;

public class CosineSimilarity extends Similarity {

  @Override
  public double computeScore(Map<String, Integer> queryVector, Map<String, Integer> docVector) {
    // TODO Auto-generated method stub

    double cosine_similarity = 0.0;

    // TODO :: compute cosine similarity between two sentences
    double sum = 0.0;
    double querylength = 0.0;
    double doclength = 0.0;
    for (Entry<String, Integer> entry : queryVector.entrySet()) {
      String term = entry.getKey();
      double qtf = (double) entry.getValue();
     
      String term1 = term + "s";
      String term2 = term + "es";
      if (docVector.containsKey(term)) {
        sum = sum + qtf * docVector.get(term);
      } 
      else if (docVector.containsKey(term1)) {
        sum = sum + qtf * docVector.get(term1);
      } else if (docVector.containsKey(term2)) {
        sum = sum + qtf * docVector.get(term2);
      }
      querylength = querylength + (qtf * qtf);
    }

    cosine_similarity = sum / (Math.sqrt(querylength) * Math.sqrt(length(docVector)));
    return cosine_similarity;

  }

  /* Jaccardsimilarity evalute the length of a vector as the geometric length of a vector */
  @Override
  double length(Map<String, Integer> Vector) {
    double length = 0.0;
    for (Entry<String, Integer> entry : Vector.entrySet()) {
      double qtf = (double) entry.getValue();
      length = length + (qtf * qtf);
    }
    return length;
  }

}
