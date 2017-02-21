package tweet.cosine.similarity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

import tweet.constants.FileConstants;
import tweet.token.similarity.NLPStanfordLemmatizer;
import tweet.token.similarity.WS4jTokenSimilarity;

public class CosSimilarity {
	
	
	private String _tweetFile;
	private String _tokenSimScoreFile;
	private static int TWEET_COUNT; 
	
	private static HashSet<String> stopWords;
	
	private HashMap<Integer,HashSet<String>> tweetNounTokenAssoc;
	private HashMap<Integer,HashSet<String>> tweetVerbTokenAssoc;
	private HashMap<Integer,HashSet<String>> tweetAdjTokenAssoc;
	private HashMap<Integer,HashSet<String>> tweetProperNounTokenAssoc;
	
	
	
	private HashMap<Integer,HashSet<String>> tweetHashTagAssoc;
	private HashMap<Integer,HashMap<String,Integer>> tweetTokenFrequencyAssoc;
	
	private HashMap<String,Double> pairSimilarityScore;
	
	private ArrayList<String> allTokens;
	//Maintain a list of all tokens(Nouns/Verbs/Adj)
	private static HashSet<String> allTokensHashSet;
	
	private static ArrayList<String> allNounTokens;
	private static ArrayList<String> allVerbTokens;
	private static ArrayList<String> allAdjTokens;
	private static HashSet<String> allNounTokensHashSet;
	private static HashSet<String> allVerbTokensHashSet;
	private static HashSet<String> allAdjTokensHashSet;
		
	private HashMap<Integer,Double> docDistanceSqr;
	
	private WS4jTokenSimilarity tokenSimilarityObj;
	
	private NearDuplicates nearDupObj;
	
	
	
	public CosSimilarity(String file, String tokenSimFile){
		_tweetFile = file;
		_tokenSimScoreFile = tokenSimFile;
		
		TWEET_COUNT = 0;
		
		stopWords = new HashSet<String>();
		
		allTokens = new ArrayList<String>();
		
		allNounTokens = new ArrayList<String>();
		allVerbTokens = new ArrayList<String>();
		allAdjTokens = new ArrayList<String>();
		
		allTokensHashSet = new HashSet<String>();
		
		allNounTokensHashSet = new HashSet<String>();
		allVerbTokensHashSet = new HashSet<String>();
		allAdjTokensHashSet = new HashSet<String>();
		
		tweetNounTokenAssoc = new HashMap<Integer,HashSet<String>>();
		tweetVerbTokenAssoc = new HashMap<Integer,HashSet<String>>();
		tweetAdjTokenAssoc = new HashMap<Integer,HashSet<String>>();
		tweetProperNounTokenAssoc = new HashMap<Integer,HashSet<String>>();
		
		tweetHashTagAssoc = new HashMap<Integer,HashSet<String>>();
		tweetTokenFrequencyAssoc = new HashMap<Integer,HashMap<String,Integer>>();
		
		docDistanceSqr = new HashMap<Integer,Double>();
	
		//Scan all files and construct token list per file and the Universal token list.
		populateStopWordList();
		
		//Now that we need to calculate the cosine similarity, we initialize an instance of the class WS4jTokenSimilarity
		//and call it for all similarity operations.
		tokenSimilarityObj = new WS4jTokenSimilarity();
		
		pairSimilarityScore = new HashMap<String,Double>(); 
		//initializeTokenSimMap();
		
		populateUniversalTokenSet();
		populateTokenSimMap();
		
		
		//Now that we have all the maps, lets process the tweet file
		preProcessTweetFile();
		
		nearDupObj = new NearDuplicates(getTweetCount(),this.pairSimilarityScore, this.docDistanceSqr, this.tweetTokenFrequencyAssoc, this.tweetNounTokenAssoc, this.tweetVerbTokenAssoc, this.tweetAdjTokenAssoc, this.tweetProperNounTokenAssoc, this.tweetHashTagAssoc);
	}
	
	public int getTweetCount(){
		return TWEET_COUNT;
	}
	
	private void populateStopWordList(){
		
		try{
			 BufferedReader br = new BufferedReader(new FileReader(FileConstants.STOP_WORD_FILE)); 
			 
			 String line = null;
			 while((line = br.readLine()) != null) {
	         
	        	 String[] lineTokens = line.split("[\\s\\n\\t]");
	        	 
	        	 for(String token : lineTokens){
	        		 
	        		 token = token.toLowerCase();
	        		 
	        		 if(token.length() >= 3){
	        			 stopWords.add(token.toLowerCase());
	        		 }
	        	 }
	         }
			
			 br.close();
			 
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		System.out.println("Populated Stop word DS");
	}
	
	
	private void populateTokenSimMap(){
	
		 try{
			 
			BufferedReader br = new BufferedReader(new FileReader(_tokenSimScoreFile));
            
			String line = null;
		    while((line = br.readLine()) != null) {
				 
            	String[] lineParts = line.split(",");
            	
            	String first = lineParts[0].toLowerCase();
            	String second = lineParts[1].toLowerCase();
            	double simScore = Double.parseDouble(lineParts[2]);
            	
            	pairSimilarityScore.put(first+","+second, simScore);
            }
         
		    br.close();
		    
		 } catch (Exception e) {
			e.printStackTrace();
		 }
		 
		 System.out.println("Finished populating Token Pair Similarity Score. Size : " + pairSimilarityScore.size());
	}
	
	
	private void populateUniversalTokenSet(){
		
		try{
        	//Initialze the Stanford NLP Lemmatizer
        	NLPStanfordLemmatizer _nlpLemmatizer = new NLPStanfordLemmatizer();
            
            BufferedReader br = Files.newBufferedReader(Paths.get(_tweetFile));
            String line;
            
            while((line = br.readLine()) != null){
            	
            	HashSet<String> tokenList = new HashSet<String>();
            	
            	List<String> lemmatizedList = _nlpLemmatizer.lemmatize(line);   	
            	ArrayList<String> lineTokens = new ArrayList<String>(lemmatizedList);
            	
            	for(String token : lineTokens){
            	
            		token = token.toLowerCase();
            		
            		//All words of length less than 4 (and the word "the") are to be treated as STOP words
	            	if(token.length() > 3){
	            		tokenList.add(token);
	            		
	            		if(stopWords.contains(token) || StringUtils.isNumeric(token) ){
	            			continue;
	            		}
	            		
	            		if(!token.startsWith("#") && !token.startsWith("@")){//normal token
	            		
		            		if(!allTokensHashSet.contains(token)){
		            			int posType = tokenSimilarityObj.getPOSForWord(token);
		            			
		            			switch(posType){
		            				case 1: if(!allNounTokensHashSet.contains(token)){
		            							allNounTokensHashSet.add(token);allNounTokens.add(token); allTokensHashSet.add(token);
		            						}
		            						break;
		            				case 2: if(!allVerbTokensHashSet.contains(token)){
		            							allVerbTokensHashSet.add(token);allVerbTokens.add(token); allTokensHashSet.add(token);
            								}
            								break; 
		            				case 3: if(!allAdjTokensHashSet.contains(token)){
		            							allAdjTokensHashSet.add(token);allAdjTokens.add(token); allTokensHashSet.add(token);
		    								}
		    								break; 
		            					
		            				case 4 : System.out.println("Synset R : "+ token);break;
		            				
		            				default: System.out.println("Invalid POS =" + token);allTokensHashSet.add(token);break;
		            			}
	            			}
	            			
	            		}
	            		
	            	}	
            	}
            	
            }

    		br.close();
            
        }catch(Exception ex){
        	ex.printStackTrace();
        	return;
        }
		
		System.out.println("\nUniversal Noun Token Set Size : " + allNounTokens.size() );
		System.out.println("Universal Verb Token Set Size : " + allVerbTokens.size() );
		System.out.println("Universal Adjective Token Set Size : " + allAdjTokens.size() );
	}
	
	
	
	private void preProcessTweetFile(){
		System.out.println("\nScanning through all tweets in ["+_tweetFile+"].");
		
		int tweetId = 1;
		
		//Initialze the Stanford NLP Lemmatizer
    	NLPStanfordLemmatizer _nlpLemmatizer = new NLPStanfordLemmatizer();
		
		try{
			//Go through each line in the file and then treat each tweet content as a separate entry.
            //Get the file name and start reading the file
            try{
            	
                BufferedReader br = new BufferedReader(new FileReader(_tweetFile));
	            
                String line = null;
                while((line = br.readLine()) != null){
	            	HashSet<String> nounTokenList = new HashSet<String>();
	            	HashSet<String> verbTokenList = new HashSet<String>();
	            	HashSet<String> adjTokenList = new HashSet<String>();
	            	HashSet<String> properNounTokenList = new HashSet<String>();
	            	
	            	HashSet<String> hashTagList = new HashSet<String>();
	            	
	            	HashMap<String,Integer> tokenFrequencyMap = new HashMap<String,Integer>();
	            	
	            	List<String> lemmatizedList = _nlpLemmatizer.lemmatize(line);
	            	ArrayList<String> lineTokens = new ArrayList<String>(lemmatizedList);
	            	
	            	for(String token : lineTokens){
	            		token = token.toLowerCase();
	            		
	            		//All words of length less than 3 (and the word "the") are to be treated as STOP words
		            	if(token.length() > 3){
		            		
		            		if(token.startsWith("#")){//#tag	
		            			if(!hashTagList.contains(token)){
		            				hashTagList.add(token);
		            			}
		            		}else{//normal token
		            			
		            			if(stopWords.contains(token) || StringUtils.isNumeric(token) ){
			            			continue;
			            		}
		            			
		            			if(!allTokensHashSet.contains(token)){
		            				continue;
		            			}
		            			
		            			//Continue based on token type
		            			if(allNounTokensHashSet.contains(token)){
		            				nounTokenList.add(token);
		            			}else if(allVerbTokensHashSet.contains(token)){
		            				verbTokenList.add(token);
		            			}else if(allAdjTokensHashSet.contains(token)){
		            				adjTokenList.add(token);
		            			}else{
		            				properNounTokenList.add(token);
		            			}
		            			
		            			int fq= 0;
		            			//Record frequency of item for given tweet.
			            		if(tokenFrequencyMap.containsKey(token)){
			            			fq = tokenFrequencyMap.get(token);
			            		}
			            		tokenFrequencyMap.put(token, fq+1);
		            		}
		            	}
	            	}
	            	
	            	//Now that file parsing is done, create Tweet and token associations
	            	tweetNounTokenAssoc.put(tweetId, nounTokenList);
	            	tweetVerbTokenAssoc.put(tweetId, verbTokenList);
	            	tweetAdjTokenAssoc.put(tweetId, adjTokenList);
	            	tweetProperNounTokenAssoc.put(tweetId, properNounTokenList);
		            
		            //CALCUATING L2 NORM FOR GIVEN TWEET
		            {
		            	 //Update the Eucledian Distance of the file
			            double L2_Dist = 0;
			            Iterator tokenFqItr = tokenFrequencyMap.values().iterator();
			            while(tokenFqItr.hasNext()){
			            	int tokenFq = (int)tokenFqItr.next();
			            	L2_Dist += Math.pow((double)tokenFq,2);
			            }
			            
			            //Handle for HashTags
			            L2_Dist += 4* hashTagList.size();
			            
			            docDistanceSqr.put(tweetId,Math.sqrt(L2_Dist));
		            }
		           
		            
		            tweetTokenFrequencyAssoc.put(tweetId, tokenFrequencyMap);
		            tweetHashTagAssoc.put(tweetId, hashTagList);
		            
		            tweetId++;
		            TWEET_COUNT++;
	            }
	            
	            br.close();
	            
            }catch(Exception ex){
            	ex.printStackTrace();
            	return;
            }
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/////////////////////////////////////////////////////////////////Interface Calls to NearDuplicates classs/////////////////////////////////////////////////
	
	public ArrayList<TweetSimilarityMeasure> getNearDuplicates(int inputTweetId){
		return nearDupObj.getNearDuplicates(inputTweetId);
	}
	
	public TweetSimilarityMeasure getCosineSim(int tweet1 , int tweet2){
		return nearDupObj.getCosSimilarity_revised(tweet1, tweet2);
	}
	
}
