package de.uie2.healthcare.patient;

import java.util.ArrayList;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Order;

@Order(attributes={"id","lastname","firstname","dateofbirth","gender","address","city"})
public class Patient {

	@Attribute(required=false, empty="")
	private long id;
	@Attribute(required=false)
	private String lastname;
	@Attribute(required=false)
	private String firstname;
	@Attribute(required=false)
	private String dateofbirth;
	@Attribute(required=false)
	private String gender;
	@Attribute(required=false)
	private String address;
	@Attribute(required=false)
	private String city;
	
	@ElementList(entry="measurement", name="measurements", required=false)
	private ArrayList<Measurement> measurements;

	public Patient(){
		this.measurements = new ArrayList<Measurement>();
		this.id = 1337;
		this.lastname = "";
		this.firstname = "";
		this.dateofbirth = "";
		this.gender = "";
		this.address = "";
		this.city = "";
	}
	
	public Patient(long id, String lastname, String firstname,
			String dateofbirth, String gender, String address, String city,
			ArrayList<Measurement> measurements) {
		super();
		this.id = id;
		this.lastname = lastname;
		this.firstname = firstname;
		this.dateofbirth = dateofbirth;
		this.gender = gender;
		this.address = address;
		this.city = city;
		this.measurements = measurements;
	}
	
	public long getId() {
		return id;
	}

	public String getLastname() {
		return lastname;
	}

	public String getFirstname() {
		return firstname;
	}

	public String getDateofbirth() {
		return dateofbirth;
	}

	public String getGender() {
		return gender;
	}

	public String getAddress() {
		return address;
	}

	public String getCity() {
		return city;
	}

	public ArrayList<Measurement> getMeasurements() {
		return measurements;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public void setDateofbirth(String dateofbirth) {
		this.dateofbirth = dateofbirth;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setMeasurements(ArrayList<Measurement> measurements) {
		this.measurements = measurements;
	}

}