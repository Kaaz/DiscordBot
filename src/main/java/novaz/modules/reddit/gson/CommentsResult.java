package novaz.modules.reddit.gson;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class CommentsResult {

	@SerializedName("kind")
	@Expose
	private String kind;
	@SerializedName("data")
	@Expose
	private SearchResultData data;

	/**
	 * @return The kind
	 */
	public String getKind() {
		return kind;
	}

	/**
	 * @param kind The kind
	 */
	public void setKind(String kind) {
		this.kind = kind;
	}

	/**
	 * @return The data
	 */
	public SearchResultData getData() {
		return data;
	}

	/**
	 * @param data The data
	 */
	public void setData(SearchResultData data) {
		this.data = data;
	}

}