package novaz.modules.reddit.pojo;

import com.google.gson.annotations.Expose;

/**
 * Created by Siddharth Verma on 24/4/16.
 */
public class Post {
    @Expose
    public PostData data;
    @Expose
    public String kind;
}
