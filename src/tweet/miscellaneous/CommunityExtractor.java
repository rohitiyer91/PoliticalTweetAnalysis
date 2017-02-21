package tweet.miscellaneous;

import java.io.File;
import java.io.FileReader;
import java.util.Scanner;

import tweet.constants.FileConstants;

public class CommunityExtractor {

	private static final String COMMUNITY_FILE = FileConstants.TWEET_OUTPUT_BASE + "SimScores/community/topic-2-communities.txt";
	
	public static void main(String[] args) {
		System.out.println("Extracting Communities from File");
		
		try{
			File topicFile = new File(COMMUNITY_FILE);
			 Scanner sc = new Scanner(new FileReader(topicFile));
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

}
