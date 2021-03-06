// Corner cases.  All methods and the constructor should throw a java.lang.NullPointerException if any argument is null. The constructor should throw a java.lang.IllegalArgumentException if the input does not correspond to a rooted DAG. The distance() and sap() methods should throw a java.lang.IllegalArgumentException unless both of the noun arguments are WordNet nouns.
// Performance requirements.  Your data type should use space linear in the input size (size of synsets and hypernyms files). The constructor should take time linearithmic (or better) in the input size. The method isNoun() should run in time logarithmic (or better) in the number of nouns. The methods distance() and sap() should run in time linear in the size of the WordNet digraph. For the analysis, assume that the number of nouns per synset is bounded by a constant.
// We define the semantic relatedness of two wordnet nouns A and B as follows:
// distance(A, B) = distance is the minimum length of any ancestral path between any synset v of A and any synset w of B.
// This is the notion of distance that you will use to implement the distance() and sap() methods in the WordNet data type.
// Outcast detection. Given a list of wordnet nouns A1, A2, ..., An, which noun is the least related to the others? To identify an outcast, compute the sum of the distances between each noun and every other one:
// di   =   dist(Ai, A1)   +   dist(Ai, A2)   +   ...   +   dist(Ai, An)
// and return a noun At for which dt is maximum.

// List of noun synsets. The file synsets.txt lists all the (noun) synsets in WordNet. The first field is the synset id (an integer), the second field is the synonym set (or synset), and the third field is its dictionary definition (or gloss). For example, the line
// 36,AND_circuit AND_gate,a circuit in a computer that fires only when all of its inputs fire
// means that the synset { AND_circuit, AND_gate } has an id number of 36 and it's gloss is a circuit in a computer that fires only when all of its inputs fire. The individual nouns that comprise a synset are separated by spaces (and a synset element is not permitted to contain a space). The S synset ids are numbered 0 through S − 1; the id numbers will appear consecutively in the synset file.

// List of hypernyms. The file hypernyms.txt contains the hypernym relationships: The first field is a synset id; subsequent fields are the id numbers of the synset's hypernyms. For example, the following line
// 164,21012,56099
// means that the the synset 164 ("Actifed") has two hypernyms: 21012 ("antihistamine") and 56099 ("nasal_decongestant"), representing that Actifed is both an antihistamine and a nasal decongestant. The synsets are obtained from the corresponding lines in the file synsets.txt.

import java.lang.NullPointerException;
import java.lang.IllegalArgumentException;
import java.util.ArrayList;
import java.util.Hashtable;

public class WordNet {

   private Hashtable<Integer, ArrayList<String>> int2strMap;
   private Hashtable<String, ArrayList<Integer>> str2intMap;
   private Digraph wordNet;
   private SAP sap;
   // constructor takes the name of the two input files
   public WordNet(String synsets, String hypernyms) {
      if (synsets == null || hypernyms == null) {
         throw new NullPointerException();
      }
      In syn = new In(synsets);
      In hyp = new In(hypernyms);
      int vNum = 0;
      int2strMap = new Hashtable<Integer, ArrayList<String>>();
      str2intMap = new Hashtable<String, ArrayList<Integer>>();

      while (syn.hasNextLine()) {
         vNum ++;
         String[] parts = syn.readLine().split(",");
         int id = Integer.parseInt(parts[0]);
         String[] words = parts[1].split(" ");

         for (String word: words) {
            ArrayList<Integer> arrInt = str2intMap.get(word);
            if (arrInt == null) {
               arrInt = new ArrayList<Integer>();
               arrInt.add(id);
               str2intMap.put(word, arrInt);
            }
            else {
               arrInt.add(id);
            }

            ArrayList<String> arrStr = int2strMap.get(id);
            if (arrStr == null) {
               arrStr = new ArrayList<String>();
               arrStr.add(word);
               int2strMap.put(id, arrStr);
            }
            else {
               arrStr.add(word);
            }

         }
      }

      wordNet = new Digraph(vNum);
      while (hyp.hasNextLine()) {
         String[] virticles = hyp.readLine().split(",");
         if (virticles.length < 2) {
            continue;
         }
         int child = Integer.parseInt(virticles[0]);
         for (int i = 1; i < virticles.length; i++) {
            int ancestor = Integer.parseInt(virticles[i]);
            wordNet.addEdge(child, ancestor);
         }
      }

      if (!isValidGraph(wordNet)) {
         throw new IllegalArgumentException();
      }

      sap = new SAP(wordNet);
   }

   // dag && has root
   private boolean isValidGraph(Digraph g) {
      DirectedCycle dc = new DirectedCycle(g);
      if (dc.hasCycle()) {
         return false;
      }
      int rootNum = 0;
      for (int i = 0; i < g.V(); i++) {
         if (g.outdegree(i) == 0) {
            rootNum++;
         }
      }
      if (rootNum == 1) {
         return true;
      }
      return false;
   }

   // returns all WordNet nouns
   public Iterable<String> nouns() {
      return str2intMap.keySet();
   }

   // is the word a WordNet noun?
   public boolean isNoun(String word) {
      if (word == null) {
         throw new NullPointerException();
      }
      return str2intMap.containsKey(word);
   }

   // distance between nounA and nounB (defined below)
   public int distance(String nounA, String nounB) {
      if (nounA == null || nounB == null) {
         throw new NullPointerException();
      }
      if (!isNoun(nounA) || !isNoun(nounB)) {
         throw new IllegalArgumentException();
      }
      ArrayList<Integer> idA = str2intMap.get(nounA);
      ArrayList<Integer> idB = str2intMap.get(nounB);
      return sap.length(idA, idB);
   }

   // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
   // in a shortest ancestral path (defined below)
   public String sap(String nounA, String nounB) {
      if (nounA == null || nounB == null) {
         throw new NullPointerException();
      }
      if (!isNoun(nounA) || !isNoun(nounB)) {
         throw new IllegalArgumentException();
      }
      ArrayList<Integer> idA = str2intMap.get(nounA);
      ArrayList<Integer> idB = str2intMap.get(nounB);
      int ancestor = sap.ancestor(idA, idB);
      if (ancestor == -1) {
         return null;
      }
      else {
         String result = "";
         for (String s: int2strMap.get(ancestor)) {
            result += s + " ";
         }
         return result.trim();
      }
   }

   // do unit testing of this class
   public static void main(String[] args) {
      String syn = args[0];
      String hyp = args[1];
      StdOut.println(hyp);
      WordNet w = new WordNet(syn, hyp);
      // for (String word: w.nouns()) {
      //    StdOut.println(word);
      // }
      StdOut.println(w.distance("simple_sugar", "closed_chain"));
      StdOut.println(w.sap("simple_sugar", "closed_chain"));
   }
}
