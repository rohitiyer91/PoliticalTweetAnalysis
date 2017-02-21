package tweet.token.similarity;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

public class TweetSimScorePruner {

	private static final String INPUT_FILE_NAME1 = "C:/Users/riyer/EclipseWorkspace/CreativeComponent/TweetCosineSimilarity/output/TokenSimScores.txt";
	private static final String OUTPUT_FILE = "C:/Users/riyer/EclipseWorkspace/CreativeComponent/TweetCosineSimilarity/output/RevTokenSimScores.txt";
	
	private static HashSet<String> badWords;
	
	public static void main(String[] args) {
		
		populateBadWordList();
		
		try{
		    PrintWriter writer = new PrintWriter(OUTPUT_FILE, "UTF-8");
		    
		    File tweetSimScoreFile = new File(INPUT_FILE_NAME1);
        	
            Scanner sc = new Scanner(new FileReader(tweetSimScoreFile));
            
            while(sc.hasNextLine()){ 
            	
            	String line = sc.nextLine();
            	String[] lineParts = line.split(",");
            	
            	String first = lineParts[0];
            	String second = lineParts[1];
            	double simScore = Double.parseDouble(lineParts[2]);
            	
            	if(badWords.contains(first.toLowerCase()) || badWords.contains(second.toLowerCase()) || simScore <= 0.7){
            		continue;
            	}
            	
            	writer.println(line);	
            }
		    
		    writer.close();
		} catch (Exception e) {
		   e.printStackTrace();
		}
		
		System.out.println("Finished with populating file");
		
	}
	
	private static void populateBadWordList(){
		
		badWords = new HashSet<String>();
		badWords.add("one");
		badWords.add("two");
		badWords.add("three");
		badWords.add("four");
		badWords.add("five");
		badWords.add("six");
		badWords.add("seven");
		badWords.add("eight");
		badWords.add("nine");
		badWords.add("ten");
		
		System.out.println("Populated Bad words");
		
	}

}
