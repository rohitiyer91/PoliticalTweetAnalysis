package tweet.cosine.similarity;

import java.util.HashMap;

public class TweetSimilarityMeasure {
	
	public int forTweetId;
	public int simTweetId;
	public double simScore;
	public HashMap<String,Double> simTokenContribution;
	
	public TweetSimilarityMeasure(int forTweetId, int simTweetId){
		this.forTweetId = forTweetId;
		this.simTweetId = simTweetId;
		simScore = 0;
		this.simTokenContribution = new HashMap<String,Double>();
	}
	
	public void setSimScore(double sc){
		this.simScore = sc;
	}
	
	public void addTokenContribution(String token, double score){
		double ct = 0;
		
		if(this.simTokenContribution.containsKey(token.toLowerCase())){
			ct = simTokenContribution.get(token.toLowerCase());
		}
		
		this.simTokenContribution.put(token.toLowerCase(), ct + score);
	}

}
