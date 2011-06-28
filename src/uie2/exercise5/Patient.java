package uie2.exercise5;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import android.content.ContentValues;

public class Patient {
	private int id;
	private String lastname, firstname;
	private String dateofbirth;
	private String gender;
	private String address;
	private String city;

	public Patient(Node item) {
		NamedNodeMap map = item.getAttributes();
		id = Integer.parseInt(map.getNamedItem("id").getNodeValue());
		lastname = map.getNamedItem("lastname").getNodeValue();
		firstname = map.getNamedItem("firstname").getNodeValue();
		dateofbirth = map.getNamedItem("dateofbirth").getNodeValue();
		gender = map.getNamedItem("gender").getNodeValue();
		address = map.getNamedItem("address").getNodeValue();
		city = map.getNamedItem("city").getNodeValue();
	}

	public Patient(int id, String lastname, String firstname,
			String dateofbirth, String gender, String address, String city) {
		super();
		this.id = id;
		this.lastname = lastname;
		this.firstname = firstname;
		this.dateofbirth = dateofbirth;
		this.gender = gender;
		this.address = address;
		this.city = city;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getDateofbirth() {
		return dateofbirth;
	}

	public void setDateofbirth(String dateofbirth) {
		this.dateofbirth = dateofbirth;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public ContentValues getValues() {
		ContentValues cv = new ContentValues();
		cv.put("city", city);
		cv.put("address", address);
		cv.put("gender", gender);
		cv.put("dateofbirth", dateofbirth);
		cv.put("firstname", firstname);
		cv.put("lastname", lastname);
		cv.put("_id", id);
		return cv;
	}
}
