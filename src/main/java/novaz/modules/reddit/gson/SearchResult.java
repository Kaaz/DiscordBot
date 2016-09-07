
package novaz.modules.reddit.gson;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class SearchResult {

    @SerializedName("kind")
    @Expose
    private String kind;
    @SerializedName("data")
    @Expose
    private SearchResultData searchResultData;

    /**
     * 
     * @return
     *     The kind
     */
    public String getKind() {
        return kind;
    }

    /**
     * 
     * @param kind
     *     The kind
     */
    public void setKind(String kind) {
        this.kind = kind;
    }

    /**
     * 
     * @return
     *     The data
     */
    public SearchResultData getSearchResultData() {
        return searchResultData;
    }

    /**
     * 
     * @param searchResultData
     *     The data
     */
    public void setSearchResultData(SearchResultData searchResultData) {
        this.searchResultData = searchResultData;
    }

}
