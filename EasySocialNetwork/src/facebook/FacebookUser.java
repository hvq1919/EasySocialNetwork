package facebook;

import java.io.Serializable;

/**
 * The object contains all info of fb user
 * 
 * @author quanhv
 *
 */

public class FacebookUser implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1494771677311260270L;
	private String id;
	private String email;
	private String name;
	private String last_name;
	private String first_name;
	private String birthday;
	private String gender;
	private String avatarURL;

	public FacebookUser(String id, String email, String name, String last_name, String first_name, String birthday,
			String gender) {
		super();
		this.id = id;
		this.email = email;
		this.name = name;
		this.last_name = last_name;
		this.first_name = first_name;
		this.birthday = birthday;
		this.gender = gender;
	}

	public FacebookUser(String id, String email, String name, String last_name, String first_name, String birthday,
			String gender, String avatarURL) {
		super();
		this.id = id;
		this.email = email;
		this.name = name;
		this.last_name = last_name;
		this.first_name = first_name;
		this.birthday = birthday;
		this.gender = gender;
		this.avatarURL = avatarURL;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLast_name() {
		return last_name;
	}

	public void setLast_name(String last_name) {
		this.last_name = last_name;
	}

	public String getFirst_name() {
		return first_name;
	}

	public void setFirst_name(String first_name) {
		this.first_name = first_name;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getAvatarURL() {
		return avatarURL;
	}

	public void setAvatarURL(String avatarURL) {
		this.avatarURL = avatarURL;
	}

}
