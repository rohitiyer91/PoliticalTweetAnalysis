package tweet.miscellaneous;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class UnionFindTest {
	
	private static ArrayList<ArrayList<String>> tweetHashSetMap;
	private static String[] tagArray = {"#tag1","#tag4","#tag5","#tag10","#tag12","#tag6","#tag7","#tag8"};
	
	private static void populateHashTagMap(){
		
		ArrayList<String> list1 = new ArrayList<String>();
		ArrayList<String> list2 = new ArrayList<String>();
		ArrayList<String> list3 = new ArrayList<String>();
		
		list1.add("#tag1");list1.add("#tag4");list1.add("#tag5");
		list2.add("#tag5");list2.add("#tag10");list2.add("#tag12");
		list3.add("#tag6");list3.add("#tag7");list3.add("#tag8");
		
		tweetHashSetMap = new ArrayList<ArrayList<String>>();
		tweetHashSetMap.add(list1);
		tweetHashSetMap.add(list2);
		tweetHashSetMap.add(list3);
		
	}
	
	
	// A utility function to find the subset of an element i
    private static String find(HashMap<String,String> parent, String i)
    {
        if (parent.get(i).compareToIgnoreCase("") == 0)
            return i;
        
        return find(parent, parent.get(i).toLowerCase());
    }
 
    // A utility function to do union of two subsets
    private static  void Union(HashMap<String,String> parent, String x, String y)
    {
        String xset = find(parent, x);
        String yset = find(parent, y);
        
        if(xset.compareTo(yset) != 0){
        	parent.put(yset,xset );
        }
        
    }
	
	public static void main(String[] args) {

		populateHashTagMap();
		
		HashMap<String,String> parent = new HashMap<String,String>();
		
		for(String tagname : tagArray){
			parent.put(tagname, "");
		}
		
		for(ArrayList<String> list : tweetHashSetMap){
			
			String x = null;
			
			for(int i=0; i<list.size(); i++){
				if(i==0){
					x = list.get(i);
					continue;
				}
				
				Union(parent, x, list.get(i));
				x = list.get(i);
			}
			
		}
		
		//Now that merging is done, lets look at parents
		Iterator itr = parent.entrySet().iterator();
		while(itr.hasNext()){
			Map.Entry pair = (Map.Entry)itr.next();
			
			String tag = pair.getKey().toString();
			String parentOfTag = pair.getValue().toString();
			
			System.out.println(tag+"->"+parentOfTag);
			
		}
		

	}

}
