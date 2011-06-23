package de.uie2.healthcare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import de.uie2.healthcare.patient.Patient;



public class PatientViewActivity extends Activity {

	public static final String LISTPOSITION = "listposition";
	private static final String TAG = "PatientViewActivity";
	private TextView patientName, patientId, patientAddress,
			patientDateOfBirth;
	private Patient tempPatient;
	private int patientPosition;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.patientview);
		super.onCreate(savedInstanceState);

		patientName = (TextView) findViewById(R.id.tx_patient_fullname);
		patientAddress = (TextView) findViewById(R.id.tx_patient_address);
		patientId = (TextView) findViewById(R.id.tx_patient_id);
		patientDateOfBirth = (TextView) findViewById(R.id.tx_patient_dateofbirth);
		
		patientPosition = getIntent().getIntExtra(LISTPOSITION, 0);
		tempPatient = DataApplication.getData().getPatient(patientPosition);
		
	}
	@Override
	protected void onResume() {
		fillTextViews(tempPatient);
		super.onResume();
	}

	private void fillTextViews(Patient p) {
		patientName.setText(p.getLastname() + ", " + p.getFirstname());
		patientAddress.setText(p.getAddress() + ", " + p.getCity());
		patientId.setText(p.getId() + "");
		patientDateOfBirth.setText(p.getDateofbirth());
	}

	public void startGraphActivity(View v){
		Intent i = new Intent(this,MeasurementActivity.class);
		//		Put the actual position as extra
		i.putExtra(LISTPOSITION, patientPosition);
		startActivity(i);
	}
	
	public void startEditActivity(View v){
		Intent i = new Intent(this,EditPatientActivity.class);
		//		Put the actual position as extra
		i.putExtra(LISTPOSITION, patientPosition);
		startActivity(i);
	}
}
