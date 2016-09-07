
package novaz.modules.reddit.gson;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class SearchResultData {

    @SerializedName("facets")
    @Expose
    private Facets facets;
    @SerializedName("modhash")
    @Expose
    private String modhash;
    @SerializedName("children")
    @Expose
    private List<Child> children = new ArrayList<Child>();
    @SerializedName("after")
    @Expose
    private Object after;
    @SerializedName("before")
    @Expose
    private Object before;

    /**
     * 
     * @return
     *     The facets
     */
    public Facets getFacets() {
        return facets;
    }

    /**
     * 
     * @param facets
     *     The facets
     */
    public void setFacets(Facets facets) {
        this.facets = facets;
    }

    /**
     * 
     * @return
     *     The modhash
     */
    public String getModhash() {
        return modhash;
    }

    /**
     * 
     * @param modhash
     *     The modhash
     */
    public void setModhash(String modhash) {
        this.modhash = modhash;
    }

    /**
     * 
     * @return
     *     The children
     */
    public List<Child> getChildren() {
        return children;
    }

    /**
     * 
     * @param children
     *     The children
     */
    public void setChildren(List<Child> children) {
        this.children = children;
    }

    /**
     * 
     * @return
     *     The after
     */
    public Object getAfter() {
        return after;
    }

    /**
     * 
     * @param after
     *     The after
     */
    public void setAfter(Object after) {
        this.after = after;
    }

    /**
     * 
     * @return
     *     The before
     */
    public Object getBefore() {
        return before;
    }

    /**
     * 
     * @param before
     *     The before
     */
    public void setBefore(Object before) {
        this.before = before;
    }

}
