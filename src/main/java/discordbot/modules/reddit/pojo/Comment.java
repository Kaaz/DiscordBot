package discordbot.modules.reddit.pojo;

import com.google.gson.annotations.Expose;

/**
 * Created by Siddharth Verma on 24/4/16.
 */
public class Comment {
	@Expose
	public CommentData data;
	@Expose
	public String kind;
}
