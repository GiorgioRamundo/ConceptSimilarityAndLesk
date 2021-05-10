import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.data.list.PointerTargetNodeList;
import net.sf.extjwnl.data.list.PointerTargetTree;
import net.sf.extjwnl.data.relationship.AsymmetricRelationship;
import net.sf.extjwnl.data.relationship.RelationshipFinder;
import net.sf.extjwnl.data.relationship.RelationshipList;
import net.sf.extjwnl.dictionary.Dictionary;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ConceptSimilarity {

    static HashMap<Pair<String,String>,Float> annotatedSimilarity;
    static HashMap<Pair<String,String>,Float> wuAndPalmerSimilarity;
    static HashMap<Pair<String,String>,Float> shortestPathSimilarity;
    static HashMap<Pair<String,String>,Float> leakcockAndChodorowSimilarity;

    static Dictionary dictionary;

    static {
        try {
            dictionary = Dictionary.getDefaultResourceInstance();
        } catch (JWNLException e) {
            e.printStackTrace();
        }
    }

    static int depthMax;

    static {
        try {
            depthMax = depthMax();
        } catch (JWNLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Read SemCor353 similarities");
        setAnnotatedSimilarity();
        System.out.print("Calculate Wu and Palmer...");
        setWuAndPalmerSimilarity();
        System.out.println("OK");
        System.out.print("Calculate Shortest path...");
        System.out.println("OK");
        setShortestPathSimilarity();
        System.out.print("Calculate Leakcock and Chodorow...");
        setLeakcockAndChodorowSimilarity();
        System.out.println("OK");
        System.out.println("PEARSON COEFFICIENTS");
        System.out.println("SemCOR353 vs Wu and Palmer: " + pearsonCoefficient(annotatedSimilarity,wuAndPalmerSimilarity));
        System.out.println("SemCOR353 vs Shortest Path: " + pearsonCoefficient(annotatedSimilarity,shortestPathSimilarity));
        System.out.println("SemCOR353 vs Leakcock and Chodorow: " + pearsonCoefficient(annotatedSimilarity,leakcockAndChodorowSimilarity));


    }



    public static float pearsonCoefficient(HashMap<Pair<String,String>,Float> d1,
                                            HashMap<Pair<String,String>,Float> d2){
        List<Float> X = toList(d1);
        List<Float> Y = toList(d2);
        return covariance(X,Y) / (standardDeviation(X)*standardDeviation(Y));
    }

    private static List<Float> toList(HashMap<Pair<String, String>, Float> d) {
        List<Float> result = new ArrayList<>();
        for (Pair<String, String> x: d.keySet())
            result.add(d.get(x));
        return result;
    }

    public static float covariance(List<Float> X, List<Float> Y){
        float valoreAttesoX = valoreAtteso(X);
        float valoreAttesoY = valoreAtteso(Y);
        List<Float> result = new ArrayList<>();
        for (float x: X)
            for (float y: Y)
                result.add(Math.abs(x - valoreAttesoX)*Math.abs(y - valoreAttesoY));
        return valoreAtteso(result);
    }

    public static float standardDeviation(List<Float> l){
        float media = valoreAtteso(l);
        float somma = 0.0f;
        for (float x: l)
            somma += ((x - media)*(x-media));
        return (float) Math.sqrt(somma / l.size());
    }

    public static float valoreAtteso(List<Float> l) {
        float sum = 0.0f;
        for (float x: l)
            sum += x;
        return sum / l.size();
    }

    public static void setWuAndPalmerSimilarity() throws Exception {
        // set annotated similarity from WordSim353 corpus
        wuAndPalmerSimilarity = new HashMap<>();
       for (Pair<String,String> p : annotatedSimilarity.keySet()){
           String w1 = p.first;
           String w2 = p.second;
           wuAndPalmerSimilarity.put(p,
                   (float) WuAndPalmerSimilarity(w1,w2));
       }
    }
    public static void setShortestPathSimilarity() throws Exception {
        // set annotated similarity from WordSim353 corpus
        shortestPathSimilarity = new HashMap<>();
        for (Pair<String,String> p : annotatedSimilarity.keySet()){
            String w1 = p.first;
            String w2 = p.second;
            shortestPathSimilarity.put(p,
                    (float) ShortestPathSimilarity(w1,w2));
        }
    }
    public static void setLeakcockAndChodorowSimilarity() throws Exception {
        // set annotated similarity from WordSim353 corpus
        leakcockAndChodorowSimilarity = new HashMap<>();
        for (Pair<String,String> p : annotatedSimilarity.keySet()){
            String w1 = p.first;
            String w2 = p.second;
            leakcockAndChodorowSimilarity.put(p,
                    (float) LeakcockAndChodorowSimilarity(w1,w2));
        }
    }
    public static void setAnnotatedSimilarity() throws IOException {
        // set annotated similarity from WordSim353 corpus
        FileReader f = new FileReader("WordSim353.csv");
        BufferedReader b = new BufferedReader(f);
        b.readLine();
        String l;
        annotatedSimilarity = new HashMap<>();
        while ((l=b.readLine()) != null) {
            String[] data = l.split(",");
            Pair<String,String> p = new Pair<>(data[0],data[1]);
            annotatedSimilarity.put(p,((Float.parseFloat(data[2]))/10));
        }
    }

    public static double WuAndPalmerSimilarity(String w1, String w2) throws Exception {
        int n = 0;
        int m = 0;

        try{
            n = dictionary.lookupIndexWord(POS.NOUN, CoreNLP.lemmatization(w1).get(w1)).getSenses().size();}
        catch(Exception e){
            n = dictionary.lookupIndexWord(POS.VERB,CoreNLP.lemmatization(w1).get(w1)).getSenses().size();
        }

        try{
            m = dictionary.lookupIndexWord(POS.NOUN,CoreNLP.lemmatization(w2).get(w2)).getSenses().size();
        }catch(Exception e){
            m = dictionary.lookupIndexWord(POS.VERB,CoreNLP.lemmatization(w2).get(w2)).getSenses().size();
        }
        double max = Double.MIN_VALUE;
        Synset s1, s2;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                try {
                    s1 = dictionary.lookupIndexWord(POS.NOUN, CoreNLP.lemmatization(w1).get(w1)).getSenses().get(i);
                }catch(Exception e){
                    s1 = dictionary.lookupIndexWord(POS.VERB, CoreNLP.lemmatization(w1).get(w1)).getSenses().get(i);
                }
                try {
                    s2 = dictionary.lookupIndexWord(POS.NOUN, CoreNLP.lemmatization(w2).get(w2)).getSenses().get(j);
                }catch(Exception e){
                    s2 = dictionary.lookupIndexWord(POS.VERB, CoreNLP.lemmatization(w2).get(w2)).getSenses().get(j);
                }
                double s = WuAndPalmerSimilarityH(s1, s2);
                if (s > max)
                    max = s;
            }
        }
        return max;
    }

    public static double WuAndPalmerSimilarityH(Synset s1, Synset s2) throws JWNLException, CloneNotSupportedException {
        return ((double)2*depth(LCS(s1,s2))) / ((double)(depth(s1)+depth(s2)));
    }

    public static double ShortestPathSimilarity(String w1, String w2) throws Exception {
        int n = 0;
        int m = 0;

        try{
            n = dictionary.lookupIndexWord(POS.NOUN, CoreNLP.lemmatization(w1).get(w1)).getSenses().size();}
        catch(Exception e){
            n = dictionary.lookupIndexWord(POS.VERB,CoreNLP.lemmatization(w1).get(w1)).getSenses().size();
        }

        try{
            m = dictionary.lookupIndexWord(POS.NOUN,CoreNLP.lemmatization(w2).get(w2)).getSenses().size();
        }catch(Exception e){
            m = dictionary.lookupIndexWord(POS.VERB,CoreNLP.lemmatization(w2).get(w2)).getSenses().size();
        }
        double max = Double.MIN_VALUE;
        Synset s1, s2;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                try {
                    s1 = dictionary.lookupIndexWord(POS.NOUN, CoreNLP.lemmatization(w1).get(w1)).getSenses().get(i);
                }catch(Exception e){
                    s1 = dictionary.lookupIndexWord(POS.VERB, CoreNLP.lemmatization(w1).get(w1)).getSenses().get(i);
                }
                try {
                    s2 = dictionary.lookupIndexWord(POS.NOUN, CoreNLP.lemmatization(w2).get(w2)).getSenses().get(j);
                }catch(Exception e){
                    s2 = dictionary.lookupIndexWord(POS.VERB, CoreNLP.lemmatization(w2).get(w2)).getSenses().get(j);
                }
                double s = ShortestPathSimilarityH(s1, s2);
                if (s > max)
                    max = s;
            }
        }
        return max;
    }

    public static double ShortestPathSimilarityH(Synset s1, Synset s2) throws JWNLException, CloneNotSupportedException {
        return ((double)(2*depthMax - length(s1,s2))) / ((double)(2*depthMax));
    }

    public static double LeakcockAndChodorowSimilarity(String w1, String w2) throws Exception {
        int n = 0;
        int m = 0;

        try{
            n = dictionary.lookupIndexWord(POS.NOUN, CoreNLP.lemmatization(w1).get(w1)).getSenses().size();}
        catch(Exception e){
            n = dictionary.lookupIndexWord(POS.VERB,CoreNLP.lemmatization(w1).get(w1)).getSenses().size();
        }

        try{
            m = dictionary.lookupIndexWord(POS.NOUN,CoreNLP.lemmatization(w2).get(w2)).getSenses().size();
        }catch(Exception e){
            m = dictionary.lookupIndexWord(POS.VERB,CoreNLP.lemmatization(w2).get(w2)).getSenses().size();
        }
        double max = Double.MIN_VALUE;
        Synset s1, s2;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                try {
                    s1 = dictionary.lookupIndexWord(POS.NOUN, CoreNLP.lemmatization(w1).get(w1)).getSenses().get(i);
                }catch(Exception e){
                    s1 = dictionary.lookupIndexWord(POS.VERB, CoreNLP.lemmatization(w1).get(w1)).getSenses().get(i);
                }
                try {
                    s2 = dictionary.lookupIndexWord(POS.NOUN, CoreNLP.lemmatization(w2).get(w2)).getSenses().get(j);
                }catch(Exception e){
                    s2 = dictionary.lookupIndexWord(POS.VERB, CoreNLP.lemmatization(w2).get(w2)).getSenses().get(j);
                }
                double s = LeakcockAndChodorowSimilarityH(s1, s2);
                if (s > max)
                    max = s;
            }
        }
        return max;
    }

    public static double LeakcockAndChodorowSimilarityH(Synset s1, Synset s2) throws JWNLException, CloneNotSupportedException {
        return (-(Math.log((length(s1,s2)+1)/((2*depthMax)+1))))/(Math.log((2*depthMax)+1));
    }

    private static double length(Synset s1, Synset s2) throws JWNLException, CloneNotSupportedException {
        Synset lcs = LCS(s1,s2);
        int LCSHeight = depth(lcs.getSynset());
        int s1LCSPath = depth(s1) - LCSHeight;
        int s2LCSPath = depth(s2) - LCSHeight;
        return s1LCSPath + s2LCSPath;
    }

    public static int depthMax() throws JWNLException {
        int max = Integer.MIN_VALUE;
        List<POS> pos = POS.getAllPOS();
        for (int i = 0; i < pos.size(); i++) {
            Iterator<Synset> it = dictionary.getSynsetIterator(pos.get(i));
            while (it.hasNext()) {
                int d = depth(it.next().getSynset());
                if (d > max)
                    max = d;
            }
        }
        return max;
    }

    public static int depth(Synset s) throws JWNLException {
        PointerTargetTree hypernymTree = PointerUtils.getHypernymTree(s);
        return longestPath(hypernymTree);
    }

    public static Synset LCS(Synset s1, Synset s2) throws JWNLException, CloneNotSupportedException {
        RelationshipList rel = RelationshipFinder.findRelationships(
                s1.getSynset(),
                s2.getSynset(),
                PointerType.HYPERNYM);
        int lcs = ((AsymmetricRelationship) rel.get(0)).getCommonParentIndex();
        return rel.get(0).getNodeList().get(lcs).getSynset();
    }

    public static int longestPath(PointerTargetTree t){
        int max = Integer.MIN_VALUE;
        for (PointerTargetNodeList p: t.toList())
            if (p.size() > max)
                max = p.size();
        return max;
    }




}


