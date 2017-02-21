package tweet.file.merge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Scanner;

import tweet.constants.FileConstants;

public class TweetFilesMerger {
	
	private static final String TOPIC_PROBABILITY_FILE = FileConstants.TWEET_TOPIC_PROBABILITY_CLASS_FILE;
	
	private static HashMap<Integer,HashMap<Integer,Double>> tweetTopicProbMap;
	
	private static HashMap<Integer,Integer> origTweetTopic;

	public static void main(String[] args) {
		
		tweetTopicProbMap = new HashMap<Integer,HashMap<Integer,Double>>();
		origTweetTopic = new HashMap<Integer,Integer>();
		
		//mergeTweetFile();
		
		buildOrigTopicMap();
		buildTopicProbMap();
		createRevTweetTopicFiles();
	}
	
	private static void buildTopicProbMap(){
		
		try{
			 int tweetId = 1;
			 BufferedReader br = Files.newBufferedReader(Paths.get(TOPIC_PROBABILITY_FILE));
			 String line = "";
			 
			 while((line = br.readLine()) != null){
				 
				 line = line.replace("[", "");
				 line = line.replace("]", "");
				 
				 //Done with all tweets for the given Community
				 //Now that we have all common terms, we need to fetch the top K terms
				 PriorityQueue<TopicProbabilityNode> topicProbRank = new PriorityQueue<TopicProbabilityNode>(new TopicProbQComparator());
				 
				//We need to calculate the top [Mean+SD] of this bell curve and then
				//take only those values which are actually bigger than this value
				DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
				ArrayList<Double> probVals = new ArrayList<Double>();
				 
				 String[] probs = line.split(",");
				 for(int i=0;i<=20;i++){
					 double probVal = Double.parseDouble(probs[i]);
					 int topicId = (i<=10)?i:(i+1);
					 
					 probVals.add(probVal);
					 stats.accept(probVal);
					 topicProbRank.add(new TopicProbabilityNode(topicId,probVal));
				 }
				 
				 double avg = stats.getAverage();
			     int n = (int)stats.getCount();
			    
 			     double sd = 0.0;
			     for (int i=0; i<n;i++){
			         sd += Math.pow(probVals.get(i) - avg, 2);
			     }
			    
			     double thresholdProbVal = avg + Math.sqrt(sd/(double)(n));
				 System.out.println(thresholdProbVal);
			     
				 //Check if we actually did put any thing into the map for current tweetId
				 if(!tweetTopicProbMap.containsKey(tweetId)){
					 HashMap<Integer,Double> probMapReserved = new HashMap<Integer,Double>();
					 
					 for(int c=1; c<=10;c++){
						 TopicProbabilityNode nextNode = topicProbRank.poll();
						 
						 if(nextNode.probability > thresholdProbVal){
							probMapReserved.put(nextNode.getTopicId(), nextNode.getTopicProb());
						 }else{
							 break;
						 }
					 }
					 
					 tweetTopicProbMap.put(tweetId, probMapReserved);
				 }
				
				 tweetId++;
			 }
			 
		 }catch(Exception ex){
			 ex.printStackTrace();
		 }
		
		System.out.println("Finished Building Topic Probability Map");
	}
	
	private static void mergeTweetFile(){
		
		ArrayList<String> fileList = FileConstants.returnTopicFileList();
		
		 try{
			 
			 PrintWriter writer = new PrintWriter(FileConstants.TWEET_ALL_TWEET_FILE);
			 for(String fileName : fileList){
				 File topicFile = new File(fileName);
				 Scanner sc = new Scanner(new FileReader(topicFile));
				 
				 while(sc.hasNext()){
					 String line = sc.nextLine();
					 writer.println(line);
				 }
			 }
			 
			 writer.close();
			 
		 }catch(Exception ex){
			 ex.printStackTrace();
		 }
		
		System.out.println("Created Merged Tweet File");
	}
	
	private static void buildOrigTopicMap(){
		
		ArrayList<String> fileList = FileConstants.returnTopicFileList();
		
		 try{
			 int topicId = 0, tweetId =1;
			 
			 for(String fileName : fileList){
				 File topicFile = new File(fileName);
				 Scanner sc = new Scanner(new FileReader(topicFile));
				 
				 while(sc.hasNext()){
					 String line = sc.nextLine();
					 origTweetTopic.put(tweetId, topicId);
					 
					 /*if(!tweetTopicProbMap.containsKey(tweetId)){
						 HashMap<Integer,Double> probMapReseved = new HashMap<Integer,Double>();
						 probMapReseved.put(topicId, 1.0);
						 tweetTopicProbMap.put(tweetId, probMapReseved);
					 }*/
					 
					 tweetId++;
				 }
				 
				 if(topicId == 10)
					 topicId=11;
				 
				 topicId++;
				 sc.close();
			 }
			 
		 }catch(Exception ex){
			 ex.printStackTrace();
		 }
		
		System.out.println("Finished building Orig Topic Based assignment of tweets");
	}
	
	
	
	private static void createRevTweetTopicFiles(){
		
		final String TWEET_REV_TOPIC_FOLDER = FileConstants.TWEET_TOPIC_BASE + "/RevisedTweets/";
		
		try{
			
			 PrintWriter writer0 = new PrintWriter(TWEET_REV_TOPIC_FOLDER + "Rev_topic0.txt", "UTF-8");
			 PrintWriter writer1 = new PrintWriter(TWEET_REV_TOPIC_FOLDER + "Rev_topic1.txt", "UTF-8");
			 PrintWriter writer2 = new PrintWriter(TWEET_REV_TOPIC_FOLDER + "Rev_topic2.txt", "UTF-8");
			 PrintWriter writer3 = new PrintWriter(TWEET_REV_TOPIC_FOLDER + "Rev_topic3.txt", "UTF-8");
			 PrintWriter writer4 = new PrintWriter(TWEET_REV_TOPIC_FOLDER + "Rev_topic4.txt", "UTF-8");
			 PrintWriter writer5 = new PrintWriter(TWEET_REV_TOPIC_FOLDER + "Rev_topic5.txt", "UTF-8");
			 PrintWriter writer6 = new PrintWriter(TWEET_REV_TOPIC_FOLDER + "Rev_topic6.txt", "UTF-8");
			 PrintWriter writer7 = new PrintWriter(TWEET_REV_TOPIC_FOLDER + "Rev_topic7.txt", "UTF-8");
			 PrintWriter writer8 = new PrintWriter(TWEET_REV_TOPIC_FOLDER + "Rev_topic8.txt", "UTF-8");
			 PrintWriter writer9 = new PrintWriter(TWEET_REV_TOPIC_FOLDER + "Rev_topic9.txt", "UTF-8");
			 PrintWriter writer10 = new PrintWriter(TWEET_REV_TOPIC_FOLDER + "Rev_topic10.txt", "UTF-8");
			 PrintWriter writer12 = new PrintWriter(TWEET_REV_TOPIC_FOLDER + "Rev_topic12.txt", "UTF-8");
			 PrintWriter writer13 = new PrintWriter(TWEET_REV_TOPIC_FOLDER + "Rev_topic13.txt", "UTF-8");
			 PrintWriter writer14 = new PrintWriter(TWEET_REV_TOPIC_FOLDER + "Rev_topic14.txt", "UTF-8");
			 PrintWriter writer15 = new PrintWriter(TWEET_REV_TOPIC_FOLDER + "Rev_topic15.txt", "UTF-8");
			 PrintWriter writer16 = new PrintWriter(TWEET_REV_TOPIC_FOLDER + "Rev_topic16.txt", "UTF-8");
			 PrintWriter writer17 = new PrintWriter(TWEET_REV_TOPIC_FOLDER + "Rev_topic17.txt", "UTF-8");
			 PrintWriter writer18 = new PrintWriter(TWEET_REV_TOPIC_FOLDER + "Rev_topic18.txt", "UTF-8");
			 PrintWriter writer19 = new PrintWriter(TWEET_REV_TOPIC_FOLDER + "Rev_topic19.txt", "UTF-8");
			 PrintWriter writer20 = new PrintWriter(TWEET_REV_TOPIC_FOLDER + "Rev_topic20.txt", "UTF-8");
			 PrintWriter writer21 = new PrintWriter(TWEET_REV_TOPIC_FOLDER + "Rev_topic21.txt", "UTF-8");
			 
			 PrintWriter incositentTweetFile = new PrintWriter(TWEET_REV_TOPIC_FOLDER + "InconsitentTweet.txt", "UTF-8");
			  
			 //BufferedReader br = Files.newBufferedReader(Paths.get(FileConstants.TWEET_ALL_TWEET_FILE));
			 int tweetId = 1;
			 
			 incositentTweetFile.println("TweetId,OrigTopicId");
			 
			 File topicFile = new File(FileConstants.TWEET_ALL_TWEET_FILE);
			 Scanner sc = new Scanner(new FileReader(topicFile));
			 
			 while(sc.hasNext()){
				 String line = sc.nextLine();
				 
				 if(!tweetTopicProbMap.containsKey(tweetId))
					 System.out.println("No Prob map found for " + tweetId);
				 
				 HashMap<Integer,Double> majorTopicProbMap = tweetTopicProbMap.get(tweetId);
				 
				 boolean isTopicConsistent = false;
				 int origTopicId = (int)origTweetTopic.get(tweetId);
				 
				 Iterator tweetTopicProbMapItr = majorTopicProbMap.keySet().iterator();
				 while(tweetTopicProbMapItr.hasNext()){
					 
					 int topicId = (int)tweetTopicProbMapItr.next();
					 
					 if(topicId == origTopicId){
						 isTopicConsistent = true;
					 }
					 
					 switch(topicId){
						 case 0 : writer0.println(line);break;
						 case 1 : writer1.println(line);break;
						 case 2 : writer2.println(line);break;
						 case 3 : writer3.println(line);break;
						 case 4 : writer4.println(line);break;
						 case 5 : writer5.println(line);break;
						 case 6 : writer6.println(line);break;
						 case 7 : writer7.println(line);break;
						 case 8 : writer8.println(line);break;
						 case 9 : writer9.println(line);break;
						 case 10 : writer10.println(line);break;
						 case 12 : writer12.println(line);break;
						 case 13 : writer13.println(line);break;
						 case 14 : writer14.println(line);break;
						 case 15 : writer15.println(line);break;
						 case 16 : writer16.println(line);break;
						 case 17 : writer17.println(line);break;
						 case 18 : writer18.println(line);break;
						 case 19 : writer19.println(line);break;
						 case 20 : writer20.println(line);break;
						 case 21 : writer21.println(line);break;
						 
						 default: System.out.println("Invalid Topic Id " + topicId);break;
					 } 
				 }	 
				 
				 if(!isTopicConsistent){
					 incositentTweetFile.println(tweetId+","+origTopicId);
				 }
				 
				 tweetId++;
			 }
			 
			 //Close all writers
			 writer0.close();
			 writer1.close();
			 writer2.close();
			 writer3.close();
			 writer4.close();
			 writer5.close();
			 writer6.close();
			 writer7.close();
			 writer8.close();
			 writer9.close();
			 writer10.close();
			 writer12.close();
			 writer13.close();
			 writer14.close();
			 writer15.close();
			 writer16.close();
			 writer17.close();
			 writer18.close();
			 writer19.close();
			 writer20.close();
			 writer21.close();
			 
			 incositentTweetFile.close();
			 
			 sc.close();
		 }catch(Exception ex){
			 ex.printStackTrace();
		 }
		
		System.out.println("Finished Writing to Files");
		
	}

}

class TopicProbabilityNode{
	
	final int topicId;
	double probability;
	
	public TopicProbabilityNode(int id, double pr){
		topicId = id;
		probability = pr;
	}
	
	public int getTopicId(){
		return topicId;
	}
	
	public double getTopicProb(){
		return probability;
	}
}


class TopicProbQComparator implements Comparator<TopicProbabilityNode>{
	
	public int compare(TopicProbabilityNode node1, TopicProbabilityNode node2){
		
		if(node1.probability < node2.probability){
			return 1;
		}
		
		if(node1.probability > node2.probability){
			return -1;
		}
		
		return 0;
	}
}
