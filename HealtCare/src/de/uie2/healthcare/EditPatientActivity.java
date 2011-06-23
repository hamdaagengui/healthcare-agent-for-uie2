package de.uie2.healthcare;

import java.util.Calendar;

import de.uie2.healthcare.patient.Patient;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;

public class EditPatientActivity extends Activity {

	private final static int DIALOG_BIRTHDAY_ID=0;
	private static final String TAG = "EditPatientActivity";
	private int position;
	
	private EditText firstname, lastname, address, city, id, birthdate;
	private RadioButton male, female;
	protected int mYear;
	protected int mMonth;
	protected int mDay;
	private boolean boolNewPatient;
	private Patient patient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.patientedit);
		firstname = (EditText) findViewById(R.id.ed_patient_firstname);
		lastname = (EditText) findViewById(R.id.ed_patient_lastname);
		address = (EditText) findViewById(R.id.ed_patient_address);
		id = (EditText) findViewById(R.id.ed_patient_id);
		city = (EditText) findViewById(R.id.ed_patient_city);
		birthdate = (EditText) findViewById(R.id.ed_patient_birthdate);
		male = (RadioButton) findViewById(R.id.opt_patient_male);
		female = (RadioButton) findViewById(R.id.opt_patient_female);
		

		// get the current date for birthday chooser
		Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);
		
		// hide the keyboard onCreate
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		position = getIntent().getIntExtra(PatientViewActivity.LISTPOSITION, -1);
		if(position<0)
			boolNewPatient = true;
		
		if(!boolNewPatient)
			fillEditText();
		
		super.onCreate(savedInstanceState);
	}
	
	public void openBirthdayDialog(View v){
		showDialog(DIALOG_BIRTHDAY_ID);
	}
	private void fillEditText(){
		patient = DataApplication.getData().getPatient(position);
		
		firstname.setText(patient.getFirstname());
		lastname.setText(patient.getLastname());
		address.setText(patient.getAddress());
		city.setText(patient.getCity());
		id.setText(""+patient.getId());
		birthdate.setText(patient.getDateofbirth());
		if (patient.getGender().equals("m"))
			male.setChecked(true);
		else
			female.setChecked(true);
	}
	

	// the callback received when the user "sets" the date in the dialog
	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;
			String zero = "";
			String zero2 = "";
			if (mDay<10)
				zero = "0";
			mMonth++;
			if (mMonth<10);
				zero2 ="0";
			birthdate.setText(
					new StringBuilder()
					.append(zero)
					.append(mDay).append(".")
					.append(zero2)
					.append(mMonth).append(".")
					.append(mYear).append(""));
		}
	};

	@Override

	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_BIRTHDAY_ID:
			if (!boolNewPatient){
				String birthdayString = birthdate.getText().toString();
				if(birthdayString.length()>9){
					String mDayString = birthdayString.substring(0, 2);
					if (mDayString.startsWith("0"))
						mDayString = mDayString.substring(1);
					String mMonthString = birthdayString.substring(3, 5);
					if (mMonthString.startsWith("0"))
						mMonthString = mMonthString.substring(1);
					mDay = Integer.valueOf(mDayString);
					mMonth = Integer.valueOf(mMonthString);
					mYear = Integer.valueOf(birthdayString.substring(6, 10));
					Log.d(TAG, mDay+" - "+ mMonth+" - "+ mYear);
				}
			}
			return new DatePickerDialog(this,
					mDateSetListener, mYear, mMonth-1, mDay);
		}
		return null;
	}

	public void saveChanges(View v){
		// edit patient
		if (!boolNewPatient){
			patient.setFirstname(firstname.getText().toString());
			patient.setLastname(lastname.getText().toString());
			patient.setDateofbirth(birthdate.getText().toString());
			patient.setCity(city.getText().toString());
			patient.setAddress(address.getText().toString());
			DataApplication.getData().editPatient(patient, position);
		}else{
			// create new patient
			String firstnameStr, lastnameStr, birthdayStr, cityStr, addressStr ="";
			long idLong = 0;
			Patient newPatient = new Patient();
			firstnameStr = firstname.getText().toString();
			lastnameStr = lastname.getText().toString();
			birthdayStr = birthdate.getText().toString();
			cityStr = city.getText().toString();
			addressStr = address.getText().toString();
			if (id.getText().toString().length()<1)
				idLong=1337;
			else
				idLong = Long.valueOf(id.getText().toString()).longValue();

			
			newPatient.setFirstname(firstnameStr);
			newPatient.setLastname(lastnameStr);
			newPatient.setDateofbirth(birthdayStr);
			newPatient.setCity(cityStr);
			newPatient.setAddress(addressStr);
			newPatient.setId(idLong);
			DataApplication.getData().addPatient(newPatient);
		}
		finish();
	}
	
	public void discardChanges(View v){
		finish();
	}
}
