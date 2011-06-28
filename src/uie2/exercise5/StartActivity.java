package uie2.exercise5;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;
import uie2.exercise5.PatientListFragment.OnPatientSelectedListener;
import android.app.Activity;
import android.app.ListFragment;
import android.content.ContentValues;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class StartActivity extends Activity implements
		OnPatientSelectedListener {
	/** Called when the activity is first created. */
	public static SimpleDateFormat sdf = new SimpleDateFormat(
			"dd.MM.yyyy HH:mm");
	private List<SimpleXYSeries> series;
	private long id;

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
				try {
					getContentResolver().insert(MyContentProvider.PATIENT_URI,
							patient.getValues());
				} catch (Exception e) {
					e.printStackTrace();
				}

				NodeList m_nodes = patientList.item(i).getChildNodes().item(1)
						.getChildNodes();
				for (int j = 1; j < m_nodes.getLength(); j += 2) {
					Measurement measurement = new Measurement(m_nodes.item(j),
							patient.getId());
					try {
						getContentResolver().insert(
								MyContentProvider.MEASUREMENT_URI,
								measurement.getValues());
					} catch (Exception e) {
						e.printStackTrace();
					}
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
		series = new ArrayList<SimpleXYSeries>();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		id = -1;

		fillDatabase();

		ListFragment patients = (ListFragment) getFragmentManager()
				.findFragmentById(R.id.PatientListFragment);
		Cursor cursor = managedQuery(MyContentProvider.PATIENT_URI,
				new String[] { "_id", "firstname", "lastname", "dateofbirth" },
				null, null, " lastname ASC, firstname ASC");
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.patient_item, cursor, new String[] { "lastname",
						"firstname", "dateofbirth" }, new int[] {
						R.id.textViewName, R.id.textViewFirstname,
						R.id.textViewDateofbirth });
		patients.setListAdapter(adapter);
	}

	public void addPatient(View view) {
		Intent i = new Intent(this, AddPatientActivity.class);
		startActivity(i);
	}

	public void onPatientSelected(long patientId) {
		this.id = patientId;
		setPatient(patientId);
	}

	private void paint() {
		XYPlot mySimpleXYPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
		for (int i = 0; i < series.size(); i++) {
			mySimpleXYPlot.removeSeries(series.get(i));
		}
		series.clear();
		// Create a formatter to use for drawing a series using
		// LineAndPointRenderer:

		Cursor types = getContentResolver().query(
				MyContentProvider.MEASUREMENT_URI,
				new String[] { "DISTINCT type" }, "patientId=" + id, null,
				"type ASC");
		while (types.moveToNext()) {
			String type = types.getString(0);
			LineAndPointFormatter formatter = new LineAndPointFormatter(
					getColorByType(type), // line color
					getColorByType(type), // point color
					null); // fill color (optional)
			Cursor values = getContentResolver().query(
					MyContentProvider.MEASUREMENT_URI,
					new String[] { "date", "time", "value" },
					"patientID==" + id + " AND type=\"" + type + "\"", null,
					null);
			if (values.getCount() > 0) {
				Log.d("1337", "" + values.getCount());
				ArrayList<Number> x = new ArrayList<Number>(values.getCount());
				ArrayList<Number> y = new ArrayList<Number>(values.getCount());
				while (values.moveToNext()) {
					x.add(toTimestamp(values.getString(0), values.getString(1)));
					y.add(normalizeValue((values.getFloat(2)), type));
				}
				SimpleXYSeries serie = new SimpleXYSeries(x, y, type);
				series.add(serie);
				mySimpleXYPlot.addSeries(serie, formatter);
				mySimpleXYPlot.setRangeBoundaries(0, 100, BoundaryMode.AUTO);
				mySimpleXYPlot.setRangeLabel("");
				mySimpleXYPlot.setRangeStepMode(XYStepMode.INCREMENT_BY_VAL);
				mySimpleXYPlot.getGraphWidget().getRangeLabelPaint()
						.setAlpha(0);
				mySimpleXYPlot.setDomainLabel("Date");
				// mySimpleXYPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
				mySimpleXYPlot.setDomainStep(XYStepMode.SUBDIVIDE,
						values.getCount());
				values.close();
				mySimpleXYPlot.setDomainValueFormat(new MyDateFormat());
				mySimpleXYPlot.disableAllMarkup();
			}
			mySimpleXYPlot.redraw();
		}
	}

	public long toTimestamp(String date, String time) {
		try {
			Date d = sdf.parse(date + " " + time);
			return d.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public void addMeasurement(View view) {
		ContentValues cv = new ContentValues();
		cv.put("type", "temperature");
		cv.put("metric", "celsius");
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(System.currentTimeMillis());
		cv.put("date",
				cal.get(Calendar.DAY_OF_MONTH) + "."
						+ (cal.get(Calendar.MONTH) + 1) + "."
						+ cal.get(Calendar.YEAR));
		cv.put("time",
				cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE));
		Random rand = new Random();
		cv.put("value", Math.round((36f + 2f * rand.nextFloat()) * 10f) / 10f);
		cv.put("patientID", id);
		getContentResolver().insert(MyContentProvider.MEASUREMENT_URI, cv);
		getContentResolver().notifyChange(MyContentProvider.MEASUREMENT_URI,
				null);
		paint();
	}

	private float normalizeValue(float value, String type) {
		Log.d("1337", " normalize type: " + type + " value: " + value);
		if (type.equals("temperature"))
			return ((100 / 9) * value - 389);
		else if (type.equals("blood pressure diastole"))
			return (value / 2);
		else if (type.equals("blood pressure systole"))
			return (value / 2);
		else if (type.equals("hearth rate"))
			return (value / (2.1f));
		return -1;
	}

	private Integer getColorByType(String type) {
		if (type.equals("temperature"))
			return Color.rgb(0, 200, 0);
		else if (type.equals("blood pressure diastole"))
			return Color.rgb(0, 200, 0);
		else if (type.equals("blood pressure systole"))
			return Color.rgb(0, 200, 0);
		else if (type.equals("hearth rate"))
			return Color.rgb(255, 0, 0);
		return Color.rgb(0, 200, 0);
	}

	public void setPatient(long patientId) {
		Cursor c = getContentResolver().query(
				MyContentProvider.PATIENT_URI,
				new String[] { "lastname", "firstname", "dateofbirth",
						"gender", "address", "city", "pictureURI" },
				"_id==" + patientId, null, null);
		if (c.getCount() > 0) {
			c.moveToFirst();
			TextView lastname = (TextView) findViewById(R.id.textViewLastname);
			lastname.setText(c.getString(0));
			TextView firstname = (TextView) findViewById(R.id.textViewDetailsFirstname);
			firstname.setText(c.getString(1));
			TextView dateofbirth = (TextView) findViewById(R.id.textViewDetailsDateofbirth);
			dateofbirth.setText(c.getString(2));
			TextView gender = (TextView) findViewById(R.id.textViewGender);
			gender.setText(c.getString(3));
			TextView address = (TextView) findViewById(R.id.textViewAddress);
			address.setText(c.getString(4));
			TextView city = (TextView) findViewById(R.id.textViewCity);
			city.setText(c.getString(5));
			c.close();
		} else {
			Toast.makeText(this, "Datensatz wurde nicht gefunden",
					Toast.LENGTH_LONG).show();
			c.close();
			return;
		}

		paint();

		Handler handler = new Handler();

		getContentResolver().registerContentObserver(
				MyContentProvider.MEASUREMENT_URI, false,
				new ContentObserver(handler) {
					@Override
					public void onChange(boolean selfChange) {
						super.onChange(selfChange);
						paint();
					}
				});
	}
}