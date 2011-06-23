package de.uie2.healthcare;

import de.uie2.healthcare.patient.Patient;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private ProgressDialog statusDialog;
	private ListView mainList;

	private String[] items = { "Delete", "Edit", "View temperature" };

	private static final String TAG = "HealthCare";
	private static final String XmlFileName = "PATIENTS_DATA.xml";
	private static final String mURL = "http://dl.dropbox.com/u/13973104/ehr.xml";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mainList = (ListView) findViewById(R.id.list_patients);
		mainList.setOnItemClickListener(myOnItemClickListener);
		mainList.setOnItemLongClickListener(myOnItemLongClickListener);
	}

	@Override
	public void onResume() {
		super.onResume();
		new PrepareListTask().execute();
		
	};
	
	OnItemClickListener myOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> a, View v, final int position,
				long id) {
			Intent patientViewIntent = new Intent(MainActivity.this,
					PatientViewActivity.class);
			patientViewIntent.putExtra(PatientViewActivity.LISTPOSITION, position);
			startActivity(patientViewIntent);
		}
	};

	OnItemLongClickListener myOnItemLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View v,
				final int position, long id) {
			Patient tempPatient = DataApplication.getData().getPatient(position);
			new AlertDialog.Builder(MainActivity.this)
					.setTitle("Options for: "+tempPatient.getLastname()+", "+tempPatient.getFirstname())
					.setItems(items, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
							case 0:
								DataApplication.getData().deletePatient(
										position);
								new PrepareListTask().execute();
								break;
							case 1:
								Intent i = new Intent(MainActivity.this, EditPatientActivity.class);
								i.putExtra(PatientViewActivity.LISTPOSITION, position);
								startActivity(i);
								break;
							case 2:
								Intent j = new Intent(MainActivity.this, MeasurementActivity.class);
								j.putExtra(PatientViewActivity.LISTPOSITION, position);
								startActivity(j);
							}
						}
					}).show();
			return true;
		}
	};

	private class PrepareListTask extends
			AsyncTask<Void, Void, ArrayAdapter<String>> {

		@Override
		protected void onPreExecute() {
			statusDialog = ProgressDialog.show(MainActivity.this,
					"Prepare patients", "loading....", true, true);
			super.onPreExecute();
		}

		@Override
		protected ArrayAdapter<String> doInBackground(Void... input) {
			return new ArrayAdapter<String>(MainActivity.this,
					android.R.layout.simple_list_item_1, DataApplication
							.getData().getPatientFullNames());
		}

		@Override
		protected void onPostExecute(ArrayAdapter<String> adapt) {
			mainList.setAdapter(adapt);
			statusDialog.dismiss();
			super.onPostExecute(null);
		}
	}

	public void getOnlineData(View v) {
		DataApplication.getData().getNewDownloadedList();
		new PrepareListTask().execute();
		Toast.makeText(this, "Online Database received.\nOld file overwriten!", Toast.LENGTH_LONG).show();
	}

	public void addPatient(View v){
		Intent i = new Intent(MainActivity.this, EditPatientActivity.class);
		startActivity(i);
	}
}