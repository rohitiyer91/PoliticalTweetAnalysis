package tweet.cosine.similarity;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import tweet.Utilities.UnionFind;
import tweet.constants.FileConstants;

public class NearDuplicates {

	private int TWEET_COUNT; 
	
	private HashMap<String,Double> pairSimilarityScore;
	private HashMap<Integer,Double> docDistanceSqr;
	
	private HashMap<Integer,HashMap<String,Integer>> tweetTokenFrequencyAssoc;
	
	private HashMap<Integer,HashSet<String>> tweetNounTokenAssoc;
	private HashMap<Integer,HashSet<String>> tweetVerbTokenAssoc;
	private HashMap<Integer,HashSet<String>> tweetAdjTokenAssoc;
	private HashMap<Integer,HashSet<String>> tweetProperNounTokenAssoc;
	
	private HashMap<Integer,HashSet<String>> tweetHashTagAssoc;
	
	private HashMap<String,String> tagParents;
	
	
	
	public NearDuplicates(int tc, HashMap<String,Double> _psc, HashMap<Integer,Double> ddSr, HashMap<Integer,HashMap<String,Integer>> ttFAsc, HashMap<Integer,HashSet<String>> tNounTokenAssoc, HashMap<Integer,HashSet<String>> tVerbTokenAssoc, HashMap<Integer,HashSet<String>> tAdjTokenAssoc, HashMap<Integer,HashSet<String>> tProperNounTokenAssoc, HashMap<Integer,HashSet<String>> tHashTagAsc){
		
		this.TWEET_COUNT = tc;
		this.pairSimilarityScore = _psc;
		this.docDistanceSqr = ddSr;
		this.tweetTokenFrequencyAssoc = ttFAsc;
		this.tweetNounTokenAssoc = tNounTokenAssoc;
		this.tweetVerbTokenAssoc = tVerbTokenAssoc;
		this.tweetAdjTokenAssoc =  tAdjTokenAssoc;
		this.tweetProperNounTokenAssoc = tProperNounTokenAssoc;
		this.tweetHashTagAssoc = tHashTagAsc;
		
		UnionFind tagAggregator = new UnionFind(tHashTagAsc);
		tagParents = tagAggregator.getTagParents();
	}
	
	
	public ArrayList<TweetSimilarityMeasure> getNearDuplicates(int inputTweetId){
		
		System.out.println("Near Duplicates for Tweet ["+inputTweetId+"]");
		
		HashMap<Integer,Double> similarTweets = new HashMap<Integer,Double>();
		
		ArrayList<TweetSimilarityMeasure> simTweetMeasure = new ArrayList<TweetSimilarityMeasure>();
		
		try{
		 
		    for(int i=inputTweetId+1; i<TWEET_COUNT; i++){
		    	 TweetSimilarityMeasure simMsr = this.getCosSimilarity_revised(inputTweetId, i);
				 
				 if(simMsr.simScore > 0.45){
					 simTweetMeasure.add(simMsr);
				 }
		    }
		    
		//	writer.close();
		}catch (Exception e) {
			   e.printStackTrace();
		}
		
		return simTweetMeasure;
	}
	
	
	
	public TweetSimilarityMeasure getCosSimilarityss(int tweet1_Id, int tweet2_Id){

		int commonTerms =0;
		double jaccVal, denominator; 
		
		HashMap<String,Integer> tweet1TokenFqList = tweetTokenFrequencyAssoc.get(tweet1_Id);
		HashMap<String,Integer> tweet2TokenFqList = tweetTokenFrequencyAssoc.get(tweet2_Id);
		
		TweetSimilarityMeasure simMeasure = new TweetSimilarityMeasure(tweet1_Id, tweet2_Id);
		
		//Calculate the number of common terms(excluding HashTags)
		Iterator t1TokenItr = tweet1TokenFqList.keySet().iterator();
		while(t1TokenItr.hasNext()){
			String token = t1TokenItr.next().toString().toLowerCase();
			String t2MaxToken = "";
			
			//If the T1 token is found in the T2 token list, then just add frequency as it is
			if(tweet2TokenFqList.keySet().contains(token)){
				
				int fqT1 = tweet1TokenFqList.get(token);
				int fqT2 = tweet2TokenFqList.get(token);

				commonTerms += (fqT1*fqT2);
				
				simMeasure.addTokenContribution(token.toLowerCase(), 1);
				
			}else{

				double maxSim = 0.0;
				Iterator t2TokenItr = tweet2TokenFqList.keySet().iterator();
				while(t2TokenItr.hasNext()){
					String t2Token = t2TokenItr.next().toString().toLowerCase();
					String combo = "";
					
					if(token.compareTo(t2Token) == 0){
						continue;
					}else if(token.compareTo(t2Token) < 0){
						combo = token+","+t2Token;
					}else{
						combo = t2Token+","+token;
					}
					
					double simScore  = 0;
					
					if(pairSimilarityScore.containsKey(combo)){
						simScore = pairSimilarityScore.get(combo);
					}
					
					if(simScore > maxSim){
						maxSim = simScore;
						t2MaxToken = t2Token;
					}
				}
				
				if(maxSim > 0){
					int fqT1 = tweet1TokenFqList.get(token);
					int fqT2 = tweet2TokenFqList.get(t2MaxToken);

					commonTerms += ((maxSim/2) * (fqT1 * fqT2));
					
					simMeasure.addTokenContribution(token.toLowerCase(), (maxSim/2));
					simMeasure.addTokenContribution(t2MaxToken.toLowerCase(), (maxSim/2));					
				}
			}
		}
		
		//Count the number of common hash tags.
		HashSet<String> t1HashTagList = tweetHashTagAssoc.get(tweet1_Id);
		HashSet<String> t2HashTagList = tweetHashTagAssoc.get(tweet2_Id);
		
		Iterator t1HashTagItr = t1HashTagList.iterator();
		while(t1HashTagItr.hasNext()){
			String t1HashTag = t1HashTagItr.next().toString();
			if(t2HashTagList.contains(t1HashTag)){
				simMeasure.addTokenContribution(t1HashTag.toLowerCase(), 2);
				commonTerms += 3;
			}
		}
		
		denominator = docDistanceSqr.get(tweet1_Id) * docDistanceSqr.get(tweet2_Id);
		jaccVal = (double)commonTerms /denominator;
		
		simMeasure.setSimScore(jaccVal);
		
		return simMeasure;
	}
	
	
	public TweetSimilarityMeasure getCosSimilarity_revised(int tweet1_Id, int tweet2_Id){

		double commonNounTerms, commonVerbTerms, commonAdjTerms, commonProperNounTerms, commonHashTerms;
		double jaccVal, denominator, numerator; 
		
		double w_N = 1 ,w_V = 0.5 ,w_A =0.5 ,w_P = 1, w_H = 2;
		
		commonNounTerms = commonVerbTerms = commonAdjTerms = commonProperNounTerms = commonHashTerms = numerator = 0;
		
		TweetSimilarityMeasure simMeasure = new TweetSimilarityMeasure(tweet1_Id, tweet2_Id);
		
		HashMap<String,Integer> tweet1TokenFqList = tweetTokenFrequencyAssoc.get(tweet1_Id);
		HashMap<String,Integer> tweet2TokenFqList = tweetTokenFrequencyAssoc.get(tweet2_Id);
		
		//NOUN List for both Tweets
		HashSet<String> tweet1NounTokenList = tweetNounTokenAssoc.get(tweet1_Id);
		HashSet<String> tweet2NounTokenList = tweetNounTokenAssoc.get(tweet2_Id);
		
		//VERB List for both Tweets
		HashSet<String> tweet1VerbTokenList = tweetVerbTokenAssoc.get(tweet1_Id);
		HashSet<String> tweet2VerbTokenList = tweetVerbTokenAssoc.get(tweet2_Id);
		
		//ADJECTIVE List for both Tweets
		HashSet<String> tweet1AdjTokenList = tweetAdjTokenAssoc.get(tweet1_Id);
		HashSet<String> tweet2AdjTokenList = tweetAdjTokenAssoc.get(tweet2_Id);
		
		//PROPER NOUN List for both Tweets
		HashSet<String> tweet1ProperNounTokenList = tweetProperNounTokenAssoc.get(tweet1_Id);
		HashSet<String> tweet2ProperNounTokenList = tweetProperNounTokenAssoc.get(tweet2_Id);
		
		
		//CALCULATE COMMON TERMS FOR TWEETS
		//1. Calculate Common Terms from among Nouns
		Iterator t1NounListItr = tweet1NounTokenList.iterator();
		while(t1NounListItr.hasNext()){
			String t1Noun = t1NounListItr.next().toString().toLowerCase();
			
			
			if(tweet2NounTokenList.contains(t1Noun)){ //If the T1 token is found in the T2 token list, then just add frequency as it is
				
				int fqT1 = tweet1TokenFqList.get(t1Noun);
				int fqT2 = tweet2TokenFqList.get(t1Noun);

				commonNounTerms += (fqT1*fqT2);
				
				simMeasure.addTokenContribution(t1Noun, 1);
				
			}else{	//If you don't find an exact match ,then just search for the closest matching pair of words.
				
				double maxSim = 0.0;
				String t2MaxNoun = "";
						
				//Iterate through the nouns from tweet 2 and find the most matching pair score
				Iterator t2NounListItr = tweet2NounTokenList.iterator();
				while(t2NounListItr.hasNext()){
					String t2Noun = t2NounListItr.next().toString().toLowerCase();
					String combo = "";
					
					if(t1Noun.compareTo(t2Noun) < 0){
						combo = t1Noun+","+t2Noun;
					}else{
						combo = t2Noun+","+t1Noun;
					}
					
					double simScore  = 0;
					
					if(pairSimilarityScore.containsKey(combo)){
						simScore = pairSimilarityScore.get(combo);
					}
					
					if(simScore > maxSim){
						maxSim = simScore;
						t2MaxNoun = t2Noun;
					}
				}
				
				if(maxSim > 0){
					int fqT1 = tweet1TokenFqList.get(t1Noun);
					int fqT2 = tweet2TokenFqList.get(t2MaxNoun);

					commonNounTerms += ((maxSim/2) * (fqT1 * fqT2));
					
					simMeasure.addTokenContribution(t1Noun, w_N *(maxSim/2));
					simMeasure.addTokenContribution(t2MaxNoun, w_N * (maxSim/2));					
				}
			}
			
		}
		
		
		//2. Calculate Common Terms from among Verbs
		Iterator t1VerbListItr = tweet1VerbTokenList.iterator();
		while(t1VerbListItr.hasNext()){
			String t1Verb = t1VerbListItr.next().toString().toLowerCase();
			
			if(tweet2VerbTokenList.contains(t1Verb)){ //If the T1 token is found in the T2 token list, then just add frequency as it is
				
				int fqT1 = tweet1TokenFqList.get(t1Verb);
				int fqT2 = tweet2TokenFqList.get(t1Verb);

				commonVerbTerms += (fqT1*fqT2);
				
				simMeasure.addTokenContribution(t1Verb, 1);
				
			}else{	//If you don't find an exact match ,then just search for the closest matching pair of words.
				
				double maxSim = 0.0;
				String t2MaxVerb = "";
				
				//Iterate through the nouns from tweet 2 and find the most matching pair score
				Iterator t2VerbListItr = tweet2VerbTokenList.iterator();
				while(t2VerbListItr.hasNext()){
					String t2Verb = t2VerbListItr.next().toString().toLowerCase();
					String combo = "";
					
					if(t1Verb.compareTo(t2Verb) < 0){
						combo = t1Verb+","+t2Verb;
					}else{
						combo = t2Verb+","+t1Verb;
					}
					
					double simScore  = 0;
					
					if(pairSimilarityScore.containsKey(combo)){
						simScore = pairSimilarityScore.get(combo);
					}
					
					if(simScore > maxSim){
						maxSim = simScore;
						t2MaxVerb = t2Verb;
					}
				}
				
				if(maxSim > 0){
					int fqT1 = tweet1TokenFqList.get(t1Verb);
					int fqT2 = tweet2TokenFqList.get(t2MaxVerb);

					commonVerbTerms += ((maxSim/2) * (fqT1 * fqT2));
					
					simMeasure.addTokenContribution(t1Verb, w_V *(maxSim/2));
					simMeasure.addTokenContribution(t2MaxVerb, w_V *(maxSim/2));					
				}
			}
			
		}
		
		
		
		//3. Calculate Common Terms from among Adjectives
		Iterator t1AdjListItr = tweet1AdjTokenList.iterator();
		while(t1AdjListItr.hasNext()){
			String t1Adj = t1AdjListItr.next().toString().toLowerCase();
			
			if(tweet2AdjTokenList.contains(t1Adj)){ //If the T1 token is found in the T2 token list, then just add frequency as it is
				
				int fqT1 = tweet1TokenFqList.get(t1Adj);
				int fqT2 = tweet2TokenFqList.get(t1Adj);

				commonAdjTerms += (fqT1*fqT2);
				
				simMeasure.addTokenContribution(t1Adj, 1);
				
			}else{	//If you don't find an exact match ,then just search for the closest matching pair of words.
				
				double maxSim = 0.0;
				String t2MaxAdj = "";
						
				//Iterate through the nouns from tweet 2 and find the most matching pair score
				Iterator t2AdjListItr = tweet2AdjTokenList.iterator();
				while(t2AdjListItr.hasNext()){
					String t2Adj = t2AdjListItr.next().toString().toLowerCase();
					String combo = "";
					
					if(t1Adj.compareTo(t2Adj) < 0){
						combo = t1Adj+","+t2Adj;
					}else{
						combo = t2Adj+","+t1Adj;
					}
					
					double simScore  = 0;
					
					if(pairSimilarityScore.containsKey(combo)){
						simScore = pairSimilarityScore.get(combo);
					}
					
					if(simScore > maxSim){
						maxSim = simScore;
						t2MaxAdj = t2Adj;
					}
				}
				
				if(maxSim > 0){
					int fqT1 = tweet1TokenFqList.get(t1Adj);
					int fqT2 = tweet2TokenFqList.get(t2MaxAdj);

					commonAdjTerms += ((maxSim/2) * (fqT1 * fqT2));
					
					simMeasure.addTokenContribution(t1Adj, w_A *(maxSim/2));
					simMeasure.addTokenContribution(t2MaxAdj, w_A *(maxSim/2));					
				}
			}
			
		}
		
		
		//4. Calculate Common Terms from among Proper Noun
		Iterator t1ProperNounListItr = tweet1ProperNounTokenList.iterator();
		while(t1ProperNounListItr.hasNext()){
			String t1ProperNoun = t1ProperNounListItr.next().toString().toLowerCase();
			
			if(tweet2ProperNounTokenList.contains(t1ProperNoun)){ //If the T1 token is found in the T2 token list, then just add frequency as it is
				
				int fqT1 = tweet1TokenFqList.get(t1ProperNoun);
				int fqT2 = tweet2TokenFqList.get(t1ProperNoun);

				commonProperNounTerms += (fqT1*fqT2);
				
				simMeasure.addTokenContribution(t1ProperNoun, w_P *1);
			}
		}
		
		
		//5. ount the number of common hash tags.
		HashSet<String> t1HashTagList = tweetHashTagAssoc.get(tweet1_Id);
		HashSet<String> t2HashTagList = tweetHashTagAssoc.get(tweet2_Id);
		HashSet<String> t1HashTagList_copy = new HashSet<String>(tweetHashTagAssoc.get(tweet1_Id));
		HashSet<String> t2HashTagList_copy = new HashSet<String>(tweetHashTagAssoc.get(tweet2_Id));
		
		//Trying the new approach to look for similar hash tags*************************************************
		Iterator t1HashTagItr = t1HashTagList.iterator();
		while(t1HashTagItr.hasNext()){
			String t1HashTag = t1HashTagItr.next().toString();
			if(t2HashTagList.contains(t1HashTag)){
				simMeasure.addTokenContribution(t1HashTag.toLowerCase(), w_H *1);
				commonHashTerms += 1;
				
				t1HashTagList_copy.remove(t1HashTag);
				t2HashTagList_copy.remove(t1HashTag);
			}
		}
		
		//Find the similarity between hash tags in both tweets
		if(!t1HashTagList_copy.isEmpty() && !t2HashTagList_copy.isEmpty()){
			
			Iterator t1Itr = t1HashTagList_copy.iterator();
			Iterator t2Itr = t2HashTagList_copy.iterator();
			
			String t1First = t1Itr.next().toString();
			String t2First = t2Itr.next().toString();
			
			if(tagParents.get(t1First).compareToIgnoreCase(tagParents.get(t2First).toString()) == 0 && tagParents.get(t1First).compareToIgnoreCase("") != 0){
					
				HashSet<String> allTags = new HashSet<String>();
				allTags.addAll(t1HashTagList_copy);
				allTags.addAll(t2HashTagList_copy);
				
				Iterator allTagsItr = allTags.iterator();
				while(allTagsItr.hasNext()){
					simMeasure.addTokenContribution(allTagsItr.next().toString().toLowerCase(), w_H *((double)1/allTags.size()));
					commonHashTerms += allTags.size()/2;
				}
			}	
		}
		
		
		//Calualte numberator by summation of weighted common terms.
		//Decide the value of the weights very carefully
		numerator = w_N * commonNounTerms + w_V * commonVerbTerms + w_A * commonAdjTerms + w_P * commonProperNounTerms + w_H * commonHashTerms;
		
		denominator = docDistanceSqr.get(tweet1_Id) * docDistanceSqr.get(tweet2_Id);
		jaccVal = (double)numerator /denominator;
		
		simMeasure.setSimScore(jaccVal);
		
		return simMeasure;
	}
	
}
