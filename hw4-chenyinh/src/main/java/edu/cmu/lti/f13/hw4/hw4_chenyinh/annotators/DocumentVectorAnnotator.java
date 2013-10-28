package edu.cmu.lti.f13.hw4.hw4_chenyinh.annotators;

import java.awt.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.html.HTMLDocument.Iterator;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.IntegerArray;
import org.apache.uima.jcas.cas.NonEmptyFSList;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;

import edu.cmu.lti.f13.hw4.hw4_chenyinh.typesystems.Document;
import edu.cmu.lti.f13.hw4.hw4_chenyinh.typesystems.Token;
import edu.cmu.lti.f13.hw4.hw4_chenyinh.utils.Utils;

public class DocumentVectorAnnotator extends JCasAnnotator_ImplBase {

  private Pattern tokenPattern = Pattern.compile("\\w+[']?\\w+");

  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {

    FSIterator<Annotation> iter = jcas.getAnnotationIndex().iterator();
    if (iter.isValid()) {
      iter.moveToNext();
      Document doc = (Document) iter.get();
      try {
        createTermFreqVector(jcas, doc);
      } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

  }

  /**
   * 
   * @param jcas
   * @param doc
   * @throws FileNotFoundException
   */

  private void createTermFreqVector(JCas jcas, Document doc) throws FileNotFoundException {

    String docText = doc.getText();
    /* using regex expression to find tokens */
    Matcher matcherq = tokenPattern.matcher(docText);
    FSList fsTokenList = new NonEmptyFSList(jcas);
    ArrayList<Token> alTokenList = new ArrayList<Token>();
    HashMap<String, Integer> tftable = new HashMap<String, Integer>();

    int pos = 0;
    while (matcherq.find(pos)) {
      // found one - create token annotation
      String token = docText.substring(matcherq.start(), matcherq.end());
      if (tftable.containsKey(token)) {
        int oldFreq = tftable.get(token);
        tftable.put(token, (oldFreq + 1));
      } else {
        tftable.put(token, 1);
      }
      pos = matcherq.end();
    }

    /* generate the stopwords hashlist in order to remove useless information in the document */
    Scanner scan = new Scanner(new File("src/main/resources/stopwords.txt"));
    HashMap<String, Integer> stopList = new HashMap<String, Integer>();
    String originLine = null;
    do {
      originLine = scan.nextLine().trim();
      if (!originLine.startsWith("#")) {
        stopList.put(originLine, 1);
      }
    } while (scan.hasNext());

    /* construct a vector of tokens and update the tokenList in CAS */
    for (Entry<String, Integer> entry : tftable.entrySet()) {
      // transfer each term to lowercase
      String term = entry.getKey().toLowerCase();
      // String term = entry.getKey();
      if (!stopList.containsKey(term)) {
        int tf = entry.getValue();
        Token token = new Token(jcas);
        token.setText(term);
        token.setFrequency(tf);
        token.addToIndexes();
        alTokenList.add(token);
      }
    }

    fsTokenList = Utils.fromCollectionToFSList(jcas, alTokenList);
    doc.setTokenList(fsTokenList);
    fsTokenList.addToIndexes();

  }

}
