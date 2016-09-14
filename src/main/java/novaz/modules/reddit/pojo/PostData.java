package novaz.modules.reddit.pojo;

import com.google.gson.annotations.Expose;

/**
 * Created by Siddharth Verma on 24/4/16.
 */
public class PostData {

	@Expose
	public String domain;
	@Expose
	public String selftext;
	@Expose
	public String id;
	@Expose
	public String name;
	@Expose
	public Integer score;
	@Expose
	public Integer downs;
	@Expose
	public String permalink;
	@Expose
	public String url;
	@Expose
	public String title;
	@Expose
	public String num_comments;
	@Expose
	public Integer ups;
	@Expose
	public String post_hint;
	@Expose
	public ImagePreview preview;
	@Expose
	public boolean is_self;

	@Expose
	public String subreddit;

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getSelftext() {
		return selftext;
	}

	public void setSelftext(String selftext) {
		this.selftext = selftext;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	public Integer getDowns() {
		return downs;
	}

	public void setDowns(Integer downs) {
		this.downs = downs;
	}

	public String getPermalink() {
		return permalink;
	}

	public void setPermalink(String permalink) {
		this.permalink = permalink;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getNum_comments() {
		return num_comments;
	}

	public void setNum_comments(String num_comments) {
		this.num_comments = num_comments;
	}

	public Integer getUps() {
		return ups;
	}

	public void setUps(Integer ups) {
		this.ups = ups;
	}

	public String getPost_hint() {
		return post_hint;
	}

	public void setPost_hint(String post_hint) {
		this.post_hint = post_hint;
	}

	public ImagePreview getPreview() {
		return preview;
	}

	public void setPreview(ImagePreview preview) {
		this.preview = preview;
	}


}
