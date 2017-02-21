package tweet.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class UnionFind {
	
	private ArrayList<ArrayList<String>> tweetHashSetMap;
	private HashSet<String> tagHashSet;
	
	public UnionFind(HashMap<Integer,HashSet<String>> tweetHashTagAssoc){
		
		tweetHashSetMap = new ArrayList<ArrayList<String>>();
		tagHashSet = new  HashSet<String>();
		
		Iterator hashSetItr = tweetHashTagAssoc.values().iterator();
		while(hashSetItr.hasNext()){
			
			HashSet<String> tagList = (HashSet<String>) hashSetItr.next();
			ArrayList<String> tagListArr = new ArrayList<String>();
			
			Iterator tagListItr = tagList.iterator();
			while(tagListItr.hasNext()){
				String tagName = tagListItr.next().toString();
				
				if(!tagHashSet.contains(tagName)){
					tagHashSet.add(tagName);
				}
				tagListArr.add(tagName);
			}
			
			tweetHashSetMap.add(tagListArr);
		}
	}
	
	
	public HashMap<String,String> getTagParents(){
		
		//Assign parents as self
		HashMap<String,String> parent = new HashMap<String,String>();
		Iterator hashSetItr = tagHashSet.iterator();
		while(hashSetItr.hasNext()){
			String tagName = hashSetItr.next().toString();
			parent.put(tagName, "");
		}
		
		//Combine hashtags occuring together into a single set
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
		
		//Return the output
		return parent;
	}
	
	
	// A utility function to find the subset of an element i
    private String find(HashMap<String,String> parent, String i){
        if (parent.get(i).compareToIgnoreCase("") == 0)
            return i;
        
        return find(parent, parent.get(i).toLowerCase());
    }
 
    // A utility function to do union of two subsets
    private void Union(HashMap<String,String> parent, String x, String y){
        String xset = find(parent, x);
        String yset = find(parent, y);
        
        if(xset.compareTo(yset) != 0){
        	parent.put(yset,xset );
        }
    }

}
