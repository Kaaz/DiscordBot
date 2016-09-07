
package novaz.modules.reddit.gson;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Image {

    @SerializedName("source")
    @Expose
    private Source source;
    @SerializedName("resolutions")
    @Expose
    private List<Resolution> resolutions = new ArrayList<Resolution>();
    @SerializedName("variants")
    @Expose
    private Variants variants;
    @SerializedName("id")
    @Expose
    private String id;

    /**
     * 
     * @return
     *     The source
     */
    public Source getSource() {
        return source;
    }

    /**
     * 
     * @param source
     *     The source
     */
    public void setSource(Source source) {
        this.source = source;
    }

    /**
     * 
     * @return
     *     The resolutions
     */
    public List<Resolution> getResolutions() {
        return resolutions;
    }

    /**
     * 
     * @param resolutions
     *     The resolutions
     */
    public void setResolutions(List<Resolution> resolutions) {
        this.resolutions = resolutions;
    }

    /**
     * 
     * @return
     *     The variants
     */
    public Variants getVariants() {
        return variants;
    }

    /**
     * 
     * @param variants
     *     The variants
     */
    public void setVariants(Variants variants) {
        this.variants = variants;
    }

    /**
     * 
     * @return
     *     The id
     */
    public String getId() {
        return id;
    }

    /**
     * 
     * @param id
     *     The id
     */
    public void setId(String id) {
        this.id = id;
    }

}
