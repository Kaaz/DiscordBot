/*
 * Copyright 2017 github.com/kaaz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package discordbot.modules.github.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.List;

@Generated("org.jsonschema2pojo")
public class RepositoryCommit {

    @SerializedName("sha")
    @Expose
    private String sha;
    @SerializedName("commit")
    @Expose
    private Commit commit;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("html_url")
    @Expose
    private String htmlUrl;
    @SerializedName("comments_url")
    @Expose
    private String commentsUrl;
    @SerializedName("author")
    @Expose
    private Author author;
    @SerializedName("committer")
    @Expose
    private Committer committer;
    @SerializedName("parents")
    @Expose
    private List<Parent> parents = new ArrayList<Parent>();

    /**
     * @return The sha
     */
    public String getSha() {
        return sha;
    }

    /**
     * @param sha The sha
     */
    public void setSha(String sha) {
        this.sha = sha;
    }

    /**
     * @return The commit
     */
    public Commit getCommit() {
        return commit;
    }

    /**
     * @param commit The commit
     */
    public void setCommit(Commit commit) {
        this.commit = commit;
    }

    /**
     * @return The url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url The url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return The htmlUrl
     */
    public String getHtmlUrl() {
        return htmlUrl;
    }

    /**
     * @param htmlUrl The html_url
     */
    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    /**
     * @return The commentsUrl
     */
    public String getCommentsUrl() {
        return commentsUrl;
    }

    /**
     * @param commentsUrl The comments_url
     */
    public void setCommentsUrl(String commentsUrl) {
        this.commentsUrl = commentsUrl;
    }

    /**
     * @return The author
     */
    public Author getAuthor() {
        return author;
    }

    /**
     * @param author The author
     */
    public void setAuthor(Author author) {
        this.author = author;
    }

    /**
     * @return The committer
     */
    public Committer getCommitter() {
        return committer;
    }

    /**
     * @param committer The committer
     */
    public void setCommitter(Committer committer) {
        this.committer = committer;
    }

    /**
     * @return The parents
     */
    public List<Parent> getParents() {
        return parents;
    }

    /**
     * @param parents The parents
     */
    public void setParents(List<Parent> parents) {
        this.parents = parents;
    }

}
