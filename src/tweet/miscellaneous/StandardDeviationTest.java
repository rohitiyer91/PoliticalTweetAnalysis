package tweet.miscellaneous;

import java.util.DoubleSummaryStatistics;


public class StandardDeviationTest {

	public static void main(String[] args) {
		
		double[] arr = {1.0,2.0,3.0,4.0,5.0};
		
		DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
	    stats.accept(1.0);
	    stats.accept(2.0);
	    stats.accept(3.0);
	    stats.accept(4.0);
	    stats.accept(5.0);
	    
	    double avg = stats.getAverage();
	    int n = (int)stats.getCount();
	    
	    double sd = 0.0;
	    for (int i=0; i<n;i++){
	        sd += Math.pow(arr[i] - avg, 2);
	    }
	    
	    double finalRes = Math.sqrt(sd/(double)(n));
	    
	    System.out.println(finalRes);
		
	}

}
