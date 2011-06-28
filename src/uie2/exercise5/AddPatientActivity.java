package uie2.exercise5;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RadioGroup;
import android.widget.EditText;

public class AddPatientActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_patient);
	}

	public void done(View view) {
		EditText firstname = (EditText) findViewById(R.id.first_name);
		EditText lastname = (EditText) findViewById(R.id.last_name);
		EditText address = (EditText) findViewById(R.id.address);
		EditText city = (EditText) findViewById(R.id.city);
		RadioGroup gender = (RadioGroup) findViewById(R.id.gender);
		DatePicker dateofbirth = (DatePicker) findViewById(R.id.dateofbirth);
		ContentValues cv = new ContentValues();
		cv.put("firstname", firstname.getText().toString());
		cv.put("lastname", lastname.getText().toString());
		cv.put("dateofbirth",
				dateofbirth.getDayOfMonth() + "." + dateofbirth.getMonth()
						+ "." + dateofbirth.getYear());
		cv.put("address", address.getText().toString());
		cv.put("city", city.getText().toString());
		if (gender.getCheckedRadioButtonId() == R.id.male) {
			cv.put("gender", "m");
		} else {
			cv.put("gender", "f");
		}
		getContentResolver().insert(MyContentProvider.PATIENT_URI, cv);
		getContentResolver().notifyChange(MyContentProvider.PATIENT_URI, null);
		finish();
	}
}
