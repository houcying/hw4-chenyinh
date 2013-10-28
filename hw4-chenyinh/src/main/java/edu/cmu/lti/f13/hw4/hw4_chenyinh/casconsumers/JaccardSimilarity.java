package edu.cmu.lti.f13.hw4.hw4_chenyinh.casconsumers;

import java.util.Map;
import java.util.Map.Entry;

public class JaccardSimilarity extends Similarity{


  @Override
  public double computeScore(Map<String, Integer> queryVector, Map<String, Integer> docVector) {
    // TODO Auto-generated method stub

    double jaccard_similarity=0.0;
    
    // TODO :: compute Jaccardsimilarity between two sentences
    double unionnumber = countUinion(queryVector, docVector);
    jaccard_similarity = unionnumber/ (length(docVector) + length(queryVector)-unionnumber);
    return jaccard_similarity;
  
  }
  
  /* Jaccardsimilarity evalute the length of a vector as the total number of tokens */
  @Override
  double length(Map<String, Integer> Vector) {
    double length = 0.0;
    for(Entry<String, Integer> entry: Vector.entrySet())
    {
      double qtf = (double)entry.getValue();
      length = length + qtf;
    }
    return length;
  }



}
