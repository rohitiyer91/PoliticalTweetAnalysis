package tweet.community.topic;

import tweet.constants.FileConstants;

public class CommunityTopicExtractor {

	public static void main(String[] args) {
		
		TopicExtractorRevised extractor = new TopicExtractorRevised(FileConstants.TWEET_COMMUNITY_TOPIC_2 ,FileConstants.TWEET_TOPIC_2_FILE_REVISED, FileConstants.TOKEN_SIMILARITY_SCORES_TOPIC_2_FILE, FileConstants.TWEET_SUB_TOPIC_2);
		extractor.extractTopicForCommunities();
	}

}
