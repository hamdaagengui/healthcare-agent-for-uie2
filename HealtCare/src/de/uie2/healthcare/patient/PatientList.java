package de.uie2.healthcare.patient;

import java.util.ArrayList;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "ehr")
public class PatientList {

	@ElementList(inline = true, entry = "patient", required=false)
	private ArrayList<Patient> patient;
	
	public PatientList(){
		this.patient = new ArrayList<Patient>(); 
	}

	public PatientList(ArrayList<Patient> patiens) {
		super();
		this.patient = patiens;
	}

	public ArrayList<Patient> getPatiens() {
		return patient;
	}
	

	public void setPatiens(ArrayList<Patient> patiens) {
		this.patient = patiens;
	}
	
	
}
