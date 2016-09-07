package novaz.modules.reddit.pojo;


import com.google.gson.annotations.Expose;

/**
 * Created by Siddharth Verma on 5/5/16.
 */
public class CommentData {

    @Expose
    public String author;

    @Expose
    public String body;

    @Expose
    public Long created;

    @Expose
    public Long created_utc;

    @Expose
    public String subreddit;

    @Expose
    public Integer score;

    @Expose
    public String id;

    @Expose
    public InitialDataComment replies;
	public boolean isOp;
}
