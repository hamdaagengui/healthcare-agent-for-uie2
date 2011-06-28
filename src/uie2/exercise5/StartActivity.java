package uie2.exercise5;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class StartActivity extends Activity implements OnItemClickListener {
	/** Called when the activity is first created. */

	private void fillDatabase() {
		InputStream input = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			input = getResources().openRawResource(R.raw.ehr);
			Document doc = builder.parse(input);
			doc.getDocumentElement().normalize();
			NodeList patientList = doc.getElementsByTagName("patient");
			for (int i = 0; i < patientList.getLength(); i++) {
				Patient patient = new Patient(patientList.item(i));
				getContentResolver().insert(MyContentProvider.PATIENT_URI,
						patient.getValues());

				NodeList m_nodes = patientList.item(i).getChildNodes().item(1)
						.getChildNodes();
				for (int j = 1; j < m_nodes.getLength(); j += 2) {
					Measurement measurement = new Measurement(m_nodes.item(j),
							patient.getId());
					getContentResolver().insert(
							MyContentProvider.MEASUREMENT_URI,
							measurement.getValues());
				}
			}
			getContentResolver().notifyChange(
					MyContentProvider.MEASUREMENT_URI, null);
			getContentResolver().notifyChange(MyContentProvider.PATIENT_URI,
					null);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		fillDatabase();

		ListView patients = (ListView) findViewById(R.id.listViewPatients);
		Cursor cursor = managedQuery(MyContentProvider.PATIENT_URI,
				new String[] { "_id", "firstname", "lastname", "dateofbirth" },
				null, null, " lastname ASC, firstname ASC");
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.patient_item, cursor, new String[] { "lastname",
						"firstname", "dateofbirth" }, new int[] {
						R.id.textViewName, R.id.textViewFirstname,
						R.id.textViewDateofbirth });
		patients.setAdapter(adapter);
		patients.setOnItemClickListener(this);
	}

	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent i = new Intent(this, PatientActivity.class);
		i.putExtra("patient", id);
		startActivity(i);
	}

	public void addPatient(View view) {
		Intent i = new Intent(this, AddPatientActivity.class);
		startActivity(i);
	}
}