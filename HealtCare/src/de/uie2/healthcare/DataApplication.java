package de.uie2.healthcare;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Application;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;
import de.uie2.healthcare.patient.Measurement;
import de.uie2.healthcare.patient.Patient;
import de.uie2.healthcare.patient.PatientList;
import de.uie2.healthcare.patient.XmlSerializer;

public class DataApplication extends Application {
	private static final String TAG = "DataApplication";
	private static DataApplication dataProvider;
	private static PatientList patientList;
	private static XmlSerializer myXmlParser;

	private static final String XML_FILENAME = "PATIENTS_DATA.xml";
	private static final String URL_XMLFILE = "http://dl.dropbox.com/u/13973104/ehr.xml";

	public DataApplication() {
		super();
		dataProvider = this;
	}

	@Override
	public void onCreate() {
		myXmlParser = new XmlSerializer();
		super.onCreate();
	}

	public static DataApplication getData() {
		return dataProvider;
	}

	public ArrayList<Patient> getPatients() {
		if (patientList == null)
			initializePatientList();
		return patientList.getPatiens();
	}

	public ArrayList<String> getPatientFullNames() {
		if (patientList == null)
			initializePatientList();
		ArrayList<String> tempList = new ArrayList<String>();
		for (Patient entry : patientList.getPatiens()) {
			tempList.add(entry.getLastname() + ", " + entry.getFirstname());
		}
		return tempList;
	}

	public Patient getPatient(int position) {
		if (patientList == null)
			initializePatientList();
		return patientList.getPatiens().get(position);
	}

	public Patient getPatientFromId(int id) {
		if (patientList == null)
			initializePatientList();
		for (Patient entry : patientList.getPatiens()) {
			if (entry.getId() == id) {
				return entry;
			}
		}
		return null;
	}
	
	public ArrayList<Long> getMeasurementLongDate(Patient p){
		ArrayList<Long> tempList = new ArrayList<Long>();
		SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy / HH:mm");
		for (Measurement entry : p.getMeasurements()){
		    Date date;
			try {
				date = formatter.parse(entry.getDate()+" / "+entry.getTime());
				long dateInLong = date.getTime();
				tempList.add(dateInLong);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return tempList;
	}
	
	public ArrayList<Double> getMeasurementsTemp(Patient p){
		if (patientList == null)
			initializePatientList();
		ArrayList<Double> tempList = new ArrayList<Double>();
		for (Measurement entry : p.getMeasurements()){
			tempList.add(entry.getValue());
		}
		return tempList;
	}
	
	public int getPosition(Patient p){
		int i=0;
		for(Patient iterator : patientList.getPatiens()){
			if (p == iterator)
				return i;
			i++;
		}
		return -1;
	}
	public void editPatient(Patient editedPatient, int position){
		if (patientList == null)
			initializePatientList();
		patientList.getPatiens().set(position, editedPatient);
		savePatientListToXML(patientList);
	}
	
	public void addPatient(Patient newPatient) {
		if (patientList == null)
			initializePatientList();
		patientList.getPatiens().add(newPatient);
		savePatientListToXML(patientList);
		Toast.makeText(this, "Patient: "+newPatient.getFirstname()+" "+newPatient.getLastname()+"\nadded to database!", Toast.LENGTH_LONG).show();
	}

	public void deletePatient(int position){
		if (patientList == null)
			initializePatientList();
		patientList.getPatiens().remove(position);
		savePatientListToXML(patientList);
		Toast.makeText(this, "Patient removed from database!", Toast.LENGTH_LONG).show();
	}
	
	
	private void downloadXmlToData() {
		Log.d(TAG, "downloadXmlToData.....");
		PatientList tempList;
			URL url;
			try {
				url = new URL(URL_XMLFILE);
			} catch (MalformedURLException e) {
				Log.d(TAG, "WRONG URL FORMAT : " + e.toString());
				return;
			}
				try {
					InputStream inpStream = url.openStream();
					tempList = myXmlParser.read(new PatientList(), inpStream);
					savePatientListToXML(tempList);
				} catch (IOException e) {
					Toast.makeText(this, "Download Error....Connection?", Toast.LENGTH_LONG).show();
					tempList = new PatientList();
					e.printStackTrace();
					return;
				} catch (Exception e) {
					Toast.makeText(this, "Download Error....Connection?", Toast.LENGTH_LONG).show();
					tempList = new PatientList();
					e.printStackTrace();
					return;
				}
				Toast.makeText(this, "XMl File download: successful", Toast.LENGTH_LONG).show();
	}

	private void savePatientListToXML(PatientList list) {
		Log.d(TAG, "savePatientListToXML.....");
		list = convertStrings(list);
		String actualContent = myXmlParser.serializeToXmlString(list);
		writeFileToData(XML_FILENAME, actualContent);
	}
	

	private void initializePatientList() {
		Log.d(TAG, "initializePatientList.....");
		if (!checkForFileFromData(XML_FILENAME)) {
			downloadXmlToData();
		}
		try {
			patientList = myXmlParser.read(new PatientList(),
					readFileFromData(XML_FILENAME));
		} catch (Exception e) {
			Log.d(TAG, "Error while initializing : " + e.toString());
			e.printStackTrace();
		}
	}
	
	public void getNewDownloadedList(){
		downloadXmlToData();
		initializePatientList();
	}

	private PatientList convertStrings(PatientList list) {
		for (Patient entry : list.getPatiens()) {
			String firstName = entry.getFirstname();
			String lastName = entry.getLastname();
			String city = entry.getCity();
			String address = entry.getAddress();
			entry.setFirstname(clearMutation(firstName));
			entry.setLastname(clearMutation(lastName));
			entry.setAddress(clearMutation(address));
			entry.setCity(clearMutation(city));
		}
		return list;
	}
	
	private String clearMutation(String input){
		input = input.replaceAll("Ü", "Ue");
		input = input.replaceAll("ü", "ue");
		input = input.replaceAll("Ö", "Oe");
		input = input.replaceAll("ö", "oe");
		input = input.replaceAll("Ä", "Ae");
		input = input.replaceAll("ä", "ae");
		input = input.replaceAll("ß", "ss");
		return input;
	}

	public void writeFileToData(String filename, String source) {
		try {
			FileOutputStream fOut = openFileOutput(filename,
					MODE_WORLD_WRITEABLE);
			OutputStreamWriter osw = new OutputStreamWriter(fOut);
			osw.write(source);
			osw.flush();
			osw.close();
		} catch (Exception e) {
			Log.d(TAG, "Error while writing File: " + e.toString());
			e.printStackTrace();
		}
	}

	private boolean checkForFileFromData(String filename) {
		try {
			new BufferedReader(new InputStreamReader(openFileInput(filename)));
		} catch (IOException e) {
			Log.d(TAG, "CheckForFileFromData: " + filename + " NOT found!");
			return false;
		}
		Log.d(TAG, "CheckForFileFromData: " + filename + " found!");
		return true;
	}
	
	private String readFileFromData(String filename) {
		StringBuilder text = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					openFileInput(filename)));
			String line;
			while ((line = br.readLine()) != null) {
				text.append(line);
				text.append('\n');
			}
		} catch (IOException e) {
			Log.d(TAG, "FILE NOT FOUND :" + e.toString());
			return "FAIL";
		}
		return text.toString();
	}
}
