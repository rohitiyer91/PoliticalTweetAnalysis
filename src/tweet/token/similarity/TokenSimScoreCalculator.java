package tweet.token.similarity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

import edu.cmu.lti.jawjaw.pobj.POS;
import tweet.constants.FileConstants;

public class TokenSimScoreCalculator {
	
	private static String TWEET_FILE = FileConstants.TWEET_TOPIC_2_FILE_REVISED;
	private static String TOKEN_SIM_OUTPUT_FILE = FileConstants.TOKEN_SIMILARITY_SCORES_TOPIC_2_FILE;
	
	private static WS4jTokenSimilarity tokenSimilarityObj;
	private static HashSet<String> stopWords;
	
	//Maintain a list of all tokens(Nouns/Verbs/Adj)
	private static HashSet<String> allTokensHashSet;
	
	private static ArrayList<String> allNounTokens;
	private static ArrayList<String> allVerbTokens;
	private static ArrayList<String> allAdjTokens;
	private static HashSet<String> allNounTokensHashSet;
	private static HashSet<String> allVerbTokensHashSet;
	private static HashSet<String> allAdjTokensHashSet;
	
	private static HashMap<String,Double> pairSimilarityScore;
	

	public static void main(String[] args) {

		allNounTokens = new ArrayList<String>();
		allVerbTokens = new ArrayList<String>();
		allAdjTokens = new ArrayList<String>();
		
		allTokensHashSet = new HashSet<String>();
		
		allNounTokensHashSet = new HashSet<String>();
		allVerbTokensHashSet = new HashSet<String>();
		allAdjTokensHashSet = new HashSet<String>();
		
		stopWords = new HashSet<String>();
		tokenSimilarityObj = new WS4jTokenSimilarity();
		pairSimilarityScore = new HashMap<String, Double>();
		
		populateStopWordList();
		populateUniversalTokenSet();
		//populateTokenSimMap();
		initializeTokenSimMap();
	}
	
	
	private static void populateStopWordList(){
		
		try{
			File stopWordFile = new File(FileConstants.STOP_WORD_FILE);
			
			 Scanner sc = new Scanner(new FileReader(stopWordFile)); 
	         while(sc.hasNextLine()){
	         
	        	 String[] lineTokens = sc.nextLine().split("[\\s\\n\\t]");
	        	 
	        	 for(String token : lineTokens){
	        		 
	        		 if(token.length() > 3){
	        			 stopWords.add(token.toLowerCase());
	        		 }
	        	 }
	         }
	         
	        //Also add numbers
	        stopWords.add("three");
	 		stopWords.add("four");
	 		stopWords.add("five");
	 		//stopWords.add("six");
	 		stopWords.add("seven");
	 		stopWords.add("eight");
	 		stopWords.add("nine");
	 		//stopWords.add("ten");
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		System.out.println("Populated Stop word DS");
	}
	
	
	
	private static void populateUniversalTokenSet(){
			
		try{
        	//File tweetFile = new File(TWEET_FILE);
            //Scanner sc = new Scanner(new FileReader(tweetFile));
        	
        	//Initialze the Stanford NLP Lemmatizer
        	NLPStanfordLemmatizer _nlpLemmatizer = new NLPStanfordLemmatizer();
            
            BufferedReader br = Files.newBufferedReader(Paths.get(TWEET_FILE));
            String line;
            
            while((line = br.readLine()) != null){
            	
            	HashSet<String> tokenList = new HashSet<String>();
            	
            	List<String> lemmatizedList = _nlpLemmatizer.lemmatize(line);
            	
            	//line = line.replace("#", " #");
            	//line = line.replaceAll("[.,:;!?+=/|{}()'<>*^\"%-]", " ").toLowerCase();
            	
            	//String[] lineTokens = line.split("[\\s\\n\\t]");
            	
            	ArrayList<String> lineTokens = new ArrayList<String>(lemmatizedList);
            	     	
            	for(String token : lineTokens){
            	
            		token = token.toLowerCase();
            		
            		if(stopWords.contains(token) || StringUtils.isNumeric(token) ){
            			continue;
            		}
            		
            		//All words of length less than 4 are to be are ignored
	            	if(token.length() > 3){
	            		tokenList.add(token);
	            		
	            		//Consider tokens which are not Hash Tags & Twitter Ids
	            		if(!token.startsWith("#") && !token.startsWith("@")){
	            		
		            		if(!allTokensHashSet.contains(token)){
		            			
		            			switch(tokenSimilarityObj.getPOSForWord(token)){
		            			
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
		            					
		            				case 4 : System.out.println("Synset R : "+ token);allTokensHashSet.add(token);break;
		            				
		            				default: System.out.println("ProperNoun :" + token);allTokensHashSet.add(token);break;
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
	
	
	private static void initializeTokenSimMap(){
		
		try{
		    PrintWriter writer = new PrintWriter(TOKEN_SIM_OUTPUT_FILE, "UTF-8");
		    
		    Collections.sort(allNounTokens);
		    Collections.sort(allVerbTokens);
		    Collections.sort(allAdjTokens);
		    
		    //Nouns
		    System.out.println("Starting writing sim scores for NOUNS");
		    for(int i=0; i<allNounTokens.size()-1; i++){
				String str1 = allNounTokens.get(i);
				for(int j=i+1; j<allNounTokens.size(); j++){
					
					String str2 = allNounTokens.get(j);
					String combo = str1+","+str2;;
					
					if(!pairSimilarityScore.containsKey(combo)){
						//calculate score and update
						double simScore = tokenSimilarityObj.wordSimilarity_revised(str1, str2 , POS.n.name());
						if(simScore > 0.8){
							writer.println(combo+","+simScore);
						}
					}
				}
				
				System.out.println("Noun[" + i +"]");
			}
		    
		    //Verbs
		    System.out.println("Starting writing sim scores for VERBS");
		    for(int i=0; i<allVerbTokens.size()-1; i++){
				String str1 = allVerbTokens.get(i);
				for(int j=i+1; j<allVerbTokens.size(); j++){
					
					String str2 = allVerbTokens.get(j);
					String combo = str1+","+str2;;
					
					if(!pairSimilarityScore.containsKey(combo)){
						//calculate score and update
						double simScore = tokenSimilarityObj.wordSimilarity_revised(str1, str2, POS.v.name());
						if(simScore > 0.8){
							writer.println(combo+","+simScore);
						}
					}
				}
				
				System.out.println("Verb[" + i +"]");
			}
		    
		    
		    //Adjectives
		    System.out.println("Starting writing sim scores for ADJECTIVES");
		    for(int i=0; i<allAdjTokens.size()-1; i++){
				String str1 = allAdjTokens.get(i);
				for(int j=i+1; j<allAdjTokens.size(); j++){
					
					String str2 = allAdjTokens.get(j);
					String combo = str1+","+str2;;
					
					if(!pairSimilarityScore.containsKey(combo)){
						//calculate score and update
						double simScore = tokenSimilarityObj.wordSimilarity_revised(str1, str2, POS.a.name());
						if(simScore > 0.8){
							writer.println(combo+","+simScore);
						}
					}
				}
				
				System.out.println("Adj[" + i +"]");
			}
		    
		    writer.close();
		    
		}catch (Exception e) {
			   e.printStackTrace();
		}

		System.out.println("Finished initializing Token Similarity Map");
			
	}

}
