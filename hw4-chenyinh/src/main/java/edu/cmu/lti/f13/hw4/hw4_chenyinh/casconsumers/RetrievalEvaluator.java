package edu.cmu.lti.f13.hw4.hw4_chenyinh.casconsumers;

import java.awt.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.NonEmptyFSList;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import edu.cmu.lti.f13.hw4.hw4_chenyinh.typesystems.Document;
import edu.cmu.lti.f13.hw4.hw4_chenyinh.typesystems.Token;
import edu.cmu.lti.f13.hw4.hw4_chenyinh.utils.Utils;

public class RetrievalEvaluator extends CasConsumer_ImplBase {

  /** query id number **/
  public ArrayList<Integer> qIdList;

  /** query and text relevant values **/
  public ArrayList<Integer> relList;

  public ArrayList<HashMap> tokenList;

  public ArrayList<Double> similarity;

  public ArrayList<Integer> querylocList;

  public ArrayList<String> textList;

  public void initialize() throws ResourceInitializationException {

    qIdList = new ArrayList<Integer>();

    relList = new ArrayList<Integer>();

    tokenList = new ArrayList<HashMap>();

    querylocList = new ArrayList<Integer>();

    similarity = new ArrayList<Double>();

    textList = new ArrayList<String>();

  }

  /**
   * TODO :: 1. construct the global word dictionary 2. keep the word frequency for each sentence
   */
  @Override
  public void processCas(CAS aCas) throws ResourceProcessException {

    JCas jcas;
    try {
      jcas = aCas.getJCas();
    } catch (CASException e) {
      throw new ResourceProcessException(e);
    }

    FSIterator it = jcas.getAnnotationIndex(Document.type).iterator();

    if (it.hasNext()) {
      Document doc = (Document) it.next();

      // Make sure that your previous annotators have populated this in CAS
      FSList fsTokenList = doc.getTokenList();
      // ArrayList<Token>tokenList=Utils.fromFSListToCollection(fsTokenList, Token.class);

      qIdList.add(doc.getQueryID());
      relList.add(doc.getRelevanceValue());
      textList.add(doc.getText());

      FSList fstList = doc.getTokenList();
      ArrayList<Token> alTokenList = Utils.fromFSListToCollection(fstList, Token.class);

      HashMap<String, Integer> tf = new HashMap<String, Integer>();
      for (int i = 0; i < alTokenList.size(); i++) {
        String term = alTokenList.get(i).getText();
        int frequency = alTokenList.get(i).getFrequency();
        tf.put(term, frequency);
      }
      tokenList.add(tf);

    }

  }

  /**
   * TODO 1. Compute Cosine Similarity and rank the retrieved sentences 2. Compute the MRR metric
   */
  @Override
  public void collectionProcessComplete(ProcessTrace arg0) throws ResourceProcessException,
          IOException {

    super.collectionProcessComplete(arg0);

    // TODO :: compute the cosine similarity measure
    int prevqid = -1;
    int nextqid = qIdList.get(0);
    HashMap<String, Integer> qmap = tokenList.get(0);
    for (int i = 0; i < qIdList.size(); i++) {
      nextqid = qIdList.get(i);
      if (nextqid == prevqid) {
        /* compute the similarity function */
        HashMap<String, Integer> amap = tokenList.get(i);
        CosineSimilarity cs = new CosineSimilarity();
        similarity.add(i, cs.computeScore(amap, qmap));
        // similarity.add(i, computeCosineSimilarity(amap, qmap));

      } /* jump to another question */
      else {
        qmap = tokenList.get(i);
        querylocList.add(i);
        similarity.add(i, 0.0);
      }
      prevqid = nextqid;

    }

    // TODO :: compute the rank of retrieved sentences

    // TODO :: compute the metric:: mean reciprocal rank
    double metric_mrr = compute_mrr();
    System.out.println(" (MRR) Mean Reciprocal Rank ::" + metric_mrr);
  }

  /**
   * 
   * @return cosine_similarity
   * @throws FileNotFoundException
   */
  private double computeCosineSimilarity(Map<String, Integer> queryVector,
          Map<String, Integer> docVector) throws FileNotFoundException {
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
      /*
       * else if(docVector.containsKey(term1)) { sum = sum + qtf * docVector.get(term1); }else
       * if(docVector.containsKey(term2)) { sum = sum + qtf * docVector.get(term2); }
       */
      querylength = querylength + (qtf * qtf);
    }

    for (Entry<String, Integer> entry : docVector.entrySet()) {
      double qtf = (double) entry.getValue();
      doclength = doclength + (qtf * qtf);
    }
    cosine_similarity = sum / (Math.sqrt(querylength) * Math.sqrt(doclength));
    return cosine_similarity;
  }

  /**
   * 
   * @return mrr
   */
  private double compute_mrr() {
    double metric_mrr = 0.0;
    ArrayList<Integer> rank = new ArrayList<Integer>();

    // TODO :: compute Mean Reciprocal Rank (MRR) of the text collection
    for (int i = 0; i < querylocList.size(); i++) {
      HashMap<Integer, Double> map = new HashMap<Integer, Double>();

      int end = 0;
      if (i == (querylocList.size() - 1)) {
        end = relList.size();
      } else {
        end = querylocList.get(i + 1);
      }
      for (int j = querylocList.get(i); j < end; j++) {
        int rel = relList.get(j);
        double score = similarity.get(j);
        // map.put(rel, score);
        map.put(j, score);
      }

      ArrayList<Entry<Integer, Double>> list = new ArrayList<Entry<Integer, Double>>(map.entrySet());

      Collections.sort(list, new Comparator<Entry<Integer, Double>>() {

        @Override
        public int compare(Entry<Integer, Double> o1, Entry<Integer, Double> o2) {
          // TODO Auto-generated method stub
          if (o1.getValue() > o2.getValue()) {
            return -1;
          } else if (o1.getValue() < o2.getValue()) {
            return 1;
          }
          return 0;
        }

      });

      int m = 0;
      while (m < list.size()) {
        if (relList.get(list.get(m).getKey()) == 1) {
          rank.add((m + 1));
          metric_mrr = metric_mrr + 1 / (float) (m + 1);
          // java.text.DecimalFormat df=new java.text.DecimalFormat("#.##");

          System.out.println("Score: " + list.get(m).getValue() + "\trank = " + (m + 1)
                  + "\trel = " + relList.get(list.get(m).getKey()) + "\tqid = "
                  + qIdList.get(list.get(m).getKey()) + "\t" + textList.get(list.get(m).getKey()));
          break;
        }
        m++;
      }

    }

    metric_mrr = metric_mrr / querylocList.size();
    return metric_mrr;
  }

}
