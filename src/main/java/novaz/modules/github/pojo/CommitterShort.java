package novaz.modules.github.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class CommitterShort {

	@SerializedName("name")
	@Expose
	private String name;
	@SerializedName("email")
	@Expose
	private String email;
	@SerializedName("date")
	@Expose
	private String date;

	/**
	 * @return The name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return The email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email The email
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return The date
	 */
	public String getDate() {
		return date;
	}

	/**
	 * @param date The date
	 */
	public void setDate(String date) {
		this.date = date;
	}

}
