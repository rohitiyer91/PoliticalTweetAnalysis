package tweet.token.similarity;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.HirstStOnge;
import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.impl.LeacockChodorow;
import edu.cmu.lti.ws4j.impl.Lesk;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.impl.Resnik;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.PorterStemmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;


public class WS4jTokenSimilarity {

	private static ILexicalDatabase db = new NictWordNet();
	private static RelatednessCalculator rc = new WuPalmer(db);
	
	private static ArrayList<String> posTypes;
	
	private static PorterStemmer stemmer;
	
	public WS4jTokenSimilarity(){
		stemmer = new PorterStemmer();
	}
	
	public int getPOSForWord(String word){
		
		/*
		 	POS Types
			0. Invalid
			1. Noun
			2. Verb
			3. Adj
			4. Adverb
		*/
		
		try{
			WS4JConfiguration.getInstance().setMFS(true);
			
			//Check for NOUN
			if(!db.getAllConcepts(word, POS.n.name()).isEmpty())
				return 1;
			
			//Check for verb
			if(!db.getAllConcepts(word, POS.v.name()).isEmpty())
				return 2;
			
			//Check for adjective
			if(!db.getAllConcepts(word, POS.a.name()).isEmpty())
				return 3;
			
			//Check for NOUN
			if(!db.getAllConcepts(word, POS.r.name()).isEmpty())
				return 4;
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return 0;
	}
	
	
	
	public double wordSimilarity_revised(String word1, String word2, String wordPosType ) {
	    double maxScore = 0D;
	    
	    //word1 = stemmer.stemWord(word1);
	    //word2 = stemmer.stemWord(word2);
	    
		try{
			WS4JConfiguration.getInstance().setMFS(true);
			
			//Get all the related concepts and see which one gives the highest similarity for the given two words.
			List<Concept> synsets1 = (List<Concept>) db.getAllConcepts(word1, wordPosType);
			List<Concept> synsets2 = (List<Concept>) db.getAllConcepts(word2, wordPosType);
			
			if(synsets1.size() > 0 && synsets2.size() > 0){
				for (Concept synset1 : synsets1) {
					for (Concept synset2 : synsets2) {
						Relatedness relatedness = rc.calcRelatednessOfSynset(synset1, synset2);
						double score = relatedness.getScore();
						if (score > maxScore) {
							maxScore = score;
						}
					}
				}
			}
			
			
			//System.out.println(word1 + " & " + word2 + " : " + maxScore);
			
		}catch (Exception e) {
			e.printStackTrace();
		}
			
		return maxScore;
	  }
	
	
}
