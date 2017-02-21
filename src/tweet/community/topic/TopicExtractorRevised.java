package tweet.community.topic;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;

import tweet.constants.FileConstants;
import tweet.cosine.similarity.CosSimilarity;
import tweet.cosine.similarity.TweetSimilarityMeasure;

public class TopicExtractorRevised {
	private String _tweetFile;
	private String _communityFile;
	private String _tokenSimScoreFile;
	private String _subTopicFile;
	
	private HashMap<Integer, String> tweetIdTextMap;
	private HashMap<Integer, ArrayList<Integer>> communityTweetMap;
	
	public TopicExtractorRevised(String communityfile, String tweetFile, String tokenSimScoreFile, String subTopicFile){
		_tweetFile = tweetFile;
		_communityFile = communityfile;
		_tokenSimScoreFile = tokenSimScoreFile;
		_subTopicFile = subTopicFile;
		
		communityTweetMap = new HashMap<Integer, ArrayList<Integer>>();
		tweetIdTextMap = new HashMap<Integer, String>();
		populateCommunityTweetMap();
		constructTweetIdTextMap();
	}
	
	
	private void populateCommunityTweetMap(){
		
		try{
			//Go through each line in the file and then treat each tweet content as a separate entry.
            //Get the file name and start reading the file
            try{
            	File communityFile = new File(_communityFile);
            	
	            Scanner sc = new Scanner(new FileReader(communityFile));
	            int commId = -1;
	            
	            //Form the the [community,<topic-list>] structure.
	            while(sc.hasNextLine()){ 
	            	
	            	String line = sc.nextLine();
	            	
	            	if(line.length() <= 0)
	            		continue;
	            	
	            	//If the line contains community Id, get it
	            	if(line.contains("$")){
	            		line = line.replaceAll("[$`]", "");
	            		
	            		commId = Integer.parseInt(line);
	            	
	            	}else if(line.contains("[") && line.contains("]")){
	            		int pos = line.indexOf("\"");
	            		
	            		line = line.substring(pos);
	            		line = line.replaceAll("\"", "");
	            		line = line.replaceAll(" ", ",");
	            		line = line.replaceAll("\t", ",");
	            		
	            		//Get the tweetIds
	            		String[] tweetIds = line.split(",");
	            		
	            		for(String tId : tweetIds){
	            			
	            			if(tId == null || tId.length() <= 0)
	            				continue;
	            			
	            			ArrayList<Integer> relatedTweets = null;
	       		         
		            		if(communityTweetMap.containsKey(commId)){
			            		relatedTweets = communityTweetMap.get(commId);
			            	}else{
			            		relatedTweets = new ArrayList<Integer>();
			            	}
			            	
			            	relatedTweets.add(Integer.parseInt(tId));
			            	communityTweetMap.put(commId, relatedTweets);
	            		}
	            		
	            	}
	            	
	            }
	               
	         }catch(Exception ex){
	            	ex.printStackTrace();
	            	return;
	         }
            
		}catch(Exception ex){
        	ex.printStackTrace();
        	return;
        }
		
	
		System.out.println("Finished populating [Communtiy,Tweets] map");	
	}
	
	
	private void constructTweetIdTextMap(){
		
		HashSet<Integer> allCommunityTweetIds = new HashSet<Integer>();
		
		//Get tweetsIds for all tweetsIds that you can find in the community pair
		{
			Iterator relatedTweetsItr = communityTweetMap.values().iterator();
			while(relatedTweetsItr.hasNext()){
				ArrayList<Integer> relatedTweetIds = (ArrayList<Integer>) relatedTweetsItr.next();
				
				for(int tId : relatedTweetIds){
					allCommunityTweetIds.add(tId);
				}
			}
		}
		
		
		int tCnt = 1;
		//Get Tweet for corresponding Ids from the tweet File
		try{
			
			File tweetFile = new File(_tweetFile);

            Scanner sc = new Scanner(new FileReader(tweetFile));
            
            //Form the the [community,<topic-list>] structure.
            while(sc.hasNextLine()){ 
            	String line = sc.nextLine();
            	
            	if(allCommunityTweetIds.contains(tCnt)){
            		tweetIdTextMap.put(tCnt, line);
            	}
            	
            	tCnt++;
            }
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
	}
	
	
	public void extractTopicForCommunities(){
		
		CosSimilarity cosSim = new CosSimilarity(_tweetFile,_tokenSimScoreFile);
		
		//For each community, get the list of topic K topics
		Iterator communityTweetMapItr = communityTweetMap.entrySet().iterator();
		
		try{
			PrintWriter writer_subTopic_file = new PrintWriter(_subTopicFile, "UTF-8");
			
			while(communityTweetMapItr.hasNext()){
				
				HashMap<String, Double> topicFrequency = new HashMap<String, Double>();
				
				Map.Entry pair = (Map.Entry)communityTweetMapItr.next();
				
				int communityId = (int)pair.getKey();
				ArrayList<Integer> relatedTweets = (ArrayList<Integer>) pair.getValue();
				
				if(relatedTweets.size() <= 4){
					continue;
				}
				
				Collections.sort(relatedTweets);
				
				int ix = 0;
				for(int tweetId : relatedTweets){
					//int ix = relatedTweets.get(tweetId);
					
					for(int i = ix+1; i<relatedTweets.size(); i++){
						
						TweetSimilarityMeasure simMsr = cosSim.getCosineSim(tweetId, relatedTweets.get(i));
						
						HashMap<String,Double> termContribution = simMsr.simTokenContribution;
						
						Iterator termContributionItr = termContribution.entrySet().iterator();
						//Populate Topic Frequency
						while(termContributionItr.hasNext()){
							Map.Entry termContriPair = (Map.Entry)termContributionItr.next();
							
							String term = termContriPair.getKey().toString();
							double contribution = (double)termContriPair.getValue();
							
							double fq = 0;
							if(topicFrequency.containsKey(term.toLowerCase())){
								fq = topicFrequency.get(term.toLowerCase());
							}
							
							topicFrequency.put(term.toLowerCase(), fq+contribution);
						}
					}
					
					ix++;
				}
				
				
				//Done with all tweets for the given Community
				//Now that we have all common terms, we need to fetch the top K terms
				PriorityQueue<TopicNode> topicFrequencyRank = new PriorityQueue<TopicNode>(new TopicQComparator());
				
				Iterator topicFrequencyItr = topicFrequency.entrySet().iterator();
				while(topicFrequencyItr.hasNext()){
					Map.Entry topicFqPair = (Map.Entry)topicFrequencyItr.next();
					
					String topic = topicFqPair.getKey().toString();
					double fq = (double)topicFqPair.getValue();
					
					topicFrequencyRank.add(new TopicNode(topic,fq));
				}
				
				if(topicFrequencyRank.isEmpty())
					continue;
				
				writer_subTopic_file.print("\nCommunityId : ["+communityId+"], Size["+relatedTweets.size()+"]\nSub-Topics : [");
				
				for(int j=0; j<15;j++){
					TopicNode nextNode = topicFrequencyRank.poll();
					
					if(nextNode == null){
						break;
					}
					
					writer_subTopic_file.print(nextNode.topicName +",");
				}
				
				writer_subTopic_file.println();
				for(int k=0;k<relatedTweets.size();k++){
					int tId = relatedTweets.get(k);
					String tweetTxt = tweetIdTextMap.get(tId);
					writer_subTopic_file.println("[" + tId + "] :"+ tweetTxt);
				}
				
			}
			
			writer_subTopic_file.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}

		
		System.out.println("\nDone Extracting Topics for Communities ");
		
	}
		
}
