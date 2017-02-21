package tweet.community.topic;

import java.io.File;
import java.io.FileReader;
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

public class TopicExtractor {	
	
	private String _tweetFile;
	private String _communityFile;
	private String _tokenSimScoreFile;
	
	private HashMap<String,Double> pairSimilarityScore;
	
	private HashMap<Integer, ArrayList<Integer>> communityTweetMap;
	
	public TopicExtractor(String communityfile, String tweetFile, String tokenSimScoreFile){
		_tweetFile = tweetFile;
		_communityFile = communityfile;
		_tokenSimScoreFile = tokenSimScoreFile;
		
		communityTweetMap = new HashMap<Integer, ArrayList<Integer>>();
	
		populateCommunityTweetMap();
	}
	
	
	private void populateCommunityTweetMap(){
		
		try{
			//Go through each line in the file and then treat each tweet content as a separate entry.
            //Get the file name and start reading the file
            try{
            	File communityFile = new File(_communityFile);
            	
	            Scanner sc = new Scanner(new FileReader(communityFile));
	            
	            String dummyLine = sc.nextLine();
	            
	            //Form the the [community,<topic-list>] structure.
	            while(sc.hasNextLine()){ 
	            	
	            	String line = sc.nextLine();
	            	String[] lineParts = line.split(",");
	            	
	            	int tweetId = Integer.parseInt(lineParts[0]);
	            	int communityId =  Integer.parseInt(lineParts[1]);
	            	
	            	ArrayList<Integer> relatedTweets = null;
	            	
	            	if(communityTweetMap.containsKey(communityId)){
	            		relatedTweets = communityTweetMap.get(communityId);
	            	}else{
	            		relatedTweets = new ArrayList<Integer>();
	            	}
	            	
	            	relatedTweets.add(tweetId);
	            	communityTweetMap.put(communityId, relatedTweets);
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
	
	public void extractTopicForCommunities(){
		
		CosSimilarity cosSim = new CosSimilarity(_tweetFile,_tokenSimScoreFile);
		
		//For each community, get the list of topic K topics
		Iterator communityTweetMapItr = communityTweetMap.entrySet().iterator();
		
		while(communityTweetMapItr.hasNext()){
			
			HashMap<String, Double> topicFrequency = new HashMap<String, Double>();
			
			Map.Entry pair = (Map.Entry)communityTweetMapItr.next();
			
			int communityId = (int)pair.getKey();
			ArrayList<Integer> relatedTweets = (ArrayList<Integer>) pair.getValue();
			
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
						
						if(term.startsWith("#")){
							fq+=1;
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
			
			System.out.print("\n["+communityId+"] ["+relatedTweets.size()+"] : ");
			
			for(int j=0; j<15;j++){
				TopicNode nextNode = topicFrequencyRank.poll();
				
				if(nextNode == null){
					break;
				}
				
				System.out.print(nextNode.topicName +",");
			}
			System.out.println("");
			for(int k=0;k<relatedTweets.size();k++){
				System.out.print(relatedTweets.get(k)+",");
			}
			
		}
		
		System.out.println("\nDone Extracting Topics for Communities ");
		
	}
		
}


class TopicNode{
	
	final String topicName;
	double priority;
	
	public TopicNode(String name, double pr){
		topicName = name;
		priority = pr;
	}
	
	public String getTopicName(){
		return topicName;
	}
}


class TopicQComparator implements Comparator<TopicNode>{
	
	public int compare(TopicNode node1, TopicNode node2){
		
		if(node1.priority < node2.priority){
			return 1;
		}
		
		if(node1.priority > node2.priority){
			return -1;
		}
		
		return 0;
	}
}


