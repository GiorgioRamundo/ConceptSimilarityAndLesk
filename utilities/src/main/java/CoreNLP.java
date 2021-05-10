import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class CoreNLP {
    public static HashMap<String,String> POStagging(String text) {

        //returns pos tagging for sentence in a <word, pos> map
        HashMap<String,String> result = new HashMap<>();
        // set up pipeline properties
        Properties props = new Properties();

        // set the list of annotators to run
        props.setProperty("annotators", "tokenize,ssplit,pos");
        // build pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        // create a document object
        CoreDocument document = pipeline.processToCoreDocument(text);
        // save tokens
        for (CoreLabel tok : document.tokens()) {
            result.put(tok.word(), tok.tag());
        }

        return result;

    }

    public static HashMap<String,String> lemmatization(String text) {

        //returns lemmatization for sentence in a <word, lemma> map
        HashMap<String,String> result = new HashMap<>();

        // set up pipeline properties
        Properties props = new Properties();
        // set the list of annotators to run
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma");
        // build pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        // create a document object
        CoreDocument document = pipeline.processToCoreDocument(text);
        // save tokens
        for (CoreLabel tok : document.tokens()) {
            result.put(tok.word(), tok.lemma());
        }

        return result;

    }

    public static List<String> tokenization(String text) {

        //returns lemmatization for sentence in a <word, lemma> map
        List<String> result = new ArrayList<>();

        // set up pipeline properties
        Properties props = new Properties();
        // set the list of annotators to run
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma");
        // build pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        // create a document object
        CoreDocument document = pipeline.processToCoreDocument(text);
        // save tokens
        for (CoreLabel tok : document.tokens()) {
            result.add(tok.word());
        }

        return result;

    }
}
