package tweet.cosine.similarity;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import tweet.constants.FileConstants;

public class TweetCosSimilarityMain {
	
	public static void main(String[] args) {
		
		CosSimilarity cosSim = new CosSimilarity(FileConstants.TWEET_TOPIC_2_FILE_REVISED, FileConstants.TOKEN_SIMILARITY_SCORES_TOPIC_2_FILE);
		
		ArrayList<Integer> nodeList = new ArrayList<Integer>();
		
		try{
		    PrintWriter writer_edge_file = new PrintWriter(FileConstants.TWEET_EDGE_GRAPH_TOPIC_2_FILE, "UTF-8");
		    
		    HashSet<String> tweetEdgeSet = new HashSet<String>();
		    
		    writer_edge_file.println("node1,node2,Weight");
		    
		    for(int i=1; i<=cosSim.getTweetCount(); i++){
		    	ArrayList<TweetSimilarityMeasure> simTweets = cosSim.getNearDuplicates(i);
		    
		    	if(!nodeList.contains(i))
		    		nodeList.add(i);
		    	
		    	for(TweetSimilarityMeasure simMsr : simTweets){
		    		String edge = "";
		    		
		    		Iterator contriTokenItr = simMsr.simTokenContribution.entrySet().iterator();
		    		//influentialTokenFile.println();
		    		while(contriTokenItr.hasNext()){
		    			Map.Entry contriTokenPair = (Map.Entry)contriTokenItr.next();
		    			
		    			String tokenName = contriTokenPair.getKey().toString();
		    			double contriVal = (double)contriTokenPair.getValue();
		    		}
		    		
		    		int tweetNodeId = simMsr.simTweetId;
		    		double simScore = simMsr.simScore;
		    		
		    		if(!nodeList.contains(tweetNodeId))
			    		nodeList.add(tweetNodeId);
		    		
		    		if(i<tweetNodeId){
		    			edge = i+","+tweetNodeId;
		    		}else{
		    			edge = tweetNodeId+","+i;
		    		}
		    		
		    		if(!tweetEdgeSet.contains(edge)){
		    			writer_edge_file.println(edge+","+(simScore*10));
		    		}
		    		
		    	}
		    }
		    
		    writer_edge_file.close();
		    
		} catch (Exception e) {
		   e.printStackTrace();
		}
		
		System.exit(0);
		
	}

}
