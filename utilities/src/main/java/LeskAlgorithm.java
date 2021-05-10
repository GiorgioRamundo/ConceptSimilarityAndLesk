import edu.stanford.nlp.ling.tokensregex.PhraseTable;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class LeskAlgorithm {

    static Dictionary dictionary;

    static {
        try {
            dictionary = Dictionary.getDefaultResourceInstance();
        } catch (JWNLException e) {
            e.printStackTrace();
        }
    }

    public static void main2(String[] args) throws JWNLException {
        System.out.println(
                    semplifiedLesk("arms",
                            "Arms bend at the elbow"));
    }

    public static void main(String[] args) throws IOException, JWNLException {
        FileReader f = new FileReader("sentences.txt");
        BufferedReader b = new BufferedReader(f);
        while (true) {
            String s = b.readLine();
            if (s == null)
                break;
            String temp = s.substring(2);
            String polysemic = polysemic(temp);
            String sentence = removeAsterisks(temp);
            System.out.println("Disambiguating <<" + polysemic + ">> in sentence: <<" + sentence + ">>");
            semplifiedLesk(polysemic,sentence);
        }
    }

    private static String removeAsterisks(String sentence) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sentence.length(); i++) {
            char c = sentence.charAt(i);
            if (sentence.charAt(i) == '*')
                continue;
            sb.append(c);
        }
        return sb.toString();

    }

    private static String polysemic(String sentence) {
        String temp = sentence.split("\\*\\*")[1];
        return temp.split("\\*\\*")[0];
    }

    public static long semplifiedLesk(String word, String sentence) throws JWNLException {
        // returns lemma of word
        String lemma = CoreNLP.lemmatization(word).get(word);
        IndexWord indexedWord = dictionary.getIndexWord(POS.NOUN,lemma);
        Synset bestSense = indexedWord.getSenses().get(0);
        int maxOverlap = 0;
        Set<String> context = getNounsLemmas(sentence);
        for (Synset sens: indexedWord.getSenses()){
            //System.out.println(sens.getGloss());
            Set<String> signature = getNounsLemmas(sens.getGloss());
            //System.out.println(signature);
            Set<String> overlap = new HashSet<>(signature);
            overlap.retainAll(context); // intersection between context and signature
            int overlapSize = overlap.size();
            if (overlapSize > maxOverlap) {
                maxOverlap = overlapSize;
                bestSense = sens;
            }
        }
        String newSentence = rewrite(sentence, word, bestSense);
        System.out.println("Best sense is " + bestSense + "(offset " + bestSense.getOffset() + ")");
        System.out.println("New sentence is <<" + newSentence + ">>");
        return bestSense.getOffset();
    }

    private static String rewrite(String sentence, String word, Synset bestSense) {
        List<String> words = new ArrayList<>();
        for (Word w: bestSense.getWords()) {
            String lemma = w.getLemma().toString();
            words.add(lemma);
        }
        return sentence.replaceAll(word,words.toString());
    }

    private static Set<String> getNounsLemmas(String sentence) {
        // pre-processing with CoreNLP
        HashMap<String,String> pos = CoreNLP.POStagging(sentence);
        HashMap<String,String> lemmas = CoreNLP.lemmatization(sentence);
        return getPOS(pos, lemmas);
    }

    /**
     * getPOS
     * @param pos result of pos tagging
     * @param lemmas result of lemmatization
     * @return set of lemmas iff rispettive words are nouns or verbs or ...
     */
    private static Set<String> getPOS(HashMap<String, String> pos, HashMap<String, String> lemmas) {
        Set<String> result = new HashSet<>();
        for (String w: pos.keySet())
            if (pos.get(w).equals("NN")
                    || pos.get(w).equals("NNS")
                    || pos.get(w).equals("NNP")
                    || pos.get(w).equals("NNPS")
                    || pos.get(w).equals("VB")
                    || pos.get(w).equals("VBD")
                    || pos.get(w).equals("VBG")
                    || pos.get(w).equals("VBN")
                    || pos.get(w).equals("VBP")
                    || pos.get(w).equals("VBZ")
            )
                result.add(lemmas.get(w));
        return result;
    }

}

