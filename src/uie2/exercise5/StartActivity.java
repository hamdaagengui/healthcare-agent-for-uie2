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

import uie2.exercise5.PatientListFragment.OnPatientSelectedListener;
import android.app.Activity;
import android.app.ListFragment;
import android.content.ContentValues;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;

public class StartActivity extends Activity implements
		OnPatientSelectedListener {
	/** Called when the activity is first created. */
	public static SimpleDateFormat sdf = new SimpleDateFormat(
			"dd.MM.yyyy HH:mm");
	private List<SimpleXYSeries> series;
	private List<String> activeMeasurementsInGraph;
	private long id;
	private Button temperature_button;
	private Button blood_pressure_button;
	private Button heart_rate_button;
	private Button time_of_medication_button;
	private XYPlot mySimpleXYPlot;

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

		Cursor c = getContentResolver().query(MyContentProvider.PATIENT_URI,
				new String[] { "_id" }, null, null, null);
		c.moveToFirst();
		id = c.getLong(0);

		temperature_button = (Button) findViewById(R.id.temperature);
		blood_pressure_button = (Button) findViewById(R.id.bloodpressure);
		heart_rate_button = (Button) findViewById(R.id.heartrate);
		time_of_medication_button = (Button) findViewById(R.id.timeofmedication);

		temperature_button.setOnTouchListener(myOnDragListener);
		blood_pressure_button.setOnTouchListener(myOnDragListener);
		heart_rate_button.setOnTouchListener(myOnDragListener);
		time_of_medication_button.setOnTouchListener(myOnDragListener);

		rootLayout = (LinearLayout) findViewById(R.id.rootlayout);

		activeMeasurementsInGraph = new ArrayList<String>();
		chooseMeasurementType("heart rate");
		paint();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	public void onPatientSelected(long patientId) {
		this.id = patientId;
		setPatient(patientId);
	}

	private void paint() {
		mySimpleXYPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
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
			if (activeMeasurementsInGraph.contains(type) ||( type.startsWith("blood pressure") && activeMeasurementsInGraph.contains("blood pressure"))) {
				LineAndPointFormatter formatter = new LineAndPointFormatter(
						getColorByType(type), // line color
						getColorByType(type), // point color
						null); // fill color (optional)
				Paint lineP = new Paint();
				lineP.setStyle(Paint.Style.STROKE);
				lineP.setStrokeWidth(8);
				lineP.setColor(getColorByType(type));
				formatter.setLinePaint(lineP);
				Cursor values = getContentResolver().query(
						MyContentProvider.MEASUREMENT_URI,
						new String[] { "date", "time", "value" },
						"patientID==" + id + " AND type=\"" + type + "\"",
						null, null);
				if (values.getCount() > 0) {
					Log.d("1337", "" + values.getCount());
					ArrayList<Number> x = new ArrayList<Number>(
							values.getCount());
					ArrayList<Number> y = new ArrayList<Number>(
							values.getCount());
					while (values.moveToNext()) {
						x.add(toTimestamp(values.getString(0),
								values.getString(1)));
						y.add(normalizeValue((values.getFloat(2)), type));
					}
					SimpleXYSeries serie = new SimpleXYSeries(x, y, type);
					series.add(serie);
					mySimpleXYPlot.addSeries(serie, formatter);
					mySimpleXYPlot
							.setRangeBoundaries(0, 100, BoundaryMode.AUTO);
					mySimpleXYPlot.setRangeLabel("");
					mySimpleXYPlot
							.setRangeStepValue(22);
					mySimpleXYPlot.getGraphWidget().getRangeLabelPaint()
							.setAlpha(0);
					mySimpleXYPlot.getGraphWidget().getRangeOriginLabelPaint()
							.setAlpha(0);
					mySimpleXYPlot.setDomainLabel("Date");
					// mySimpleXYPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL,
					// 1);
					mySimpleXYPlot.setDomainStep(XYStepMode.SUBDIVIDE,
							values.getCount());
					values.close();
					mySimpleXYPlot.setDomainValueFormat(new MyDateFormat());
					mySimpleXYPlot.disableAllMarkup();
				}
				mySimpleXYPlot.redraw();
			}
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.add_patient) {
			Intent i = new Intent(this, AddPatientActivity.class);
			startActivity(i);
		} else if (item.getItemId() == R.id.add_measurement) {
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
					cal.get(Calendar.HOUR_OF_DAY) + ":"
							+ cal.get(Calendar.MINUTE));
			Random rand = new Random();
			cv.put("value",
					Math.round((36f + 2f * rand.nextFloat()) * 10f) / 10f);
			cv.put("patientID", id);
			getContentResolver().insert(MyContentProvider.MEASUREMENT_URI, cv);
			getContentResolver().notifyChange(
					MyContentProvider.MEASUREMENT_URI, null);
			paint();
		}
		return super.onOptionsItemSelected(item);
	}

	private float normalizeValue(float value, String type) {
		//Log.d("1337", " normalize type: " + type + " value: " + value);
		if (type.equals("temperature"))
			return ((100 / 9) * value - 389);
		else if (type.equals("blood pressure diastole"))
			return (value / 2);
		else if (type.equals("blood pressure systole"))
			return (value / 2);
		else if (type.equals("heart rate"))
			return (value / (2.1f));
		return -1;
	}

	private Integer getColorByType(String type) {
		if (type.equals("temperature"))
			return Color.rgb(0, 200, 0);
		else if (type.equals("blood pressure diastole"))
			return Color.rgb(127, 219, 255);
		else if (type.equals("blood pressure systole"))
			return Color.rgb(98, 12, 67);
		else if (type.equals("hearth rate"))
			return Color.rgb(255, 0, 0);
		return Color.rgb(0, 200, 0);
	}

	private void chooseMeasurementType(String type) {
		if (activeMeasurementsInGraph.isEmpty())
		{
			activeMeasurementsInGraph.add(type);
			ImageView image2 = (ImageView)findViewById(R.id.imageView2);
			if(type.equals("temperature"))image2.setImageResource(R.drawable.temperature);
			else if(type.equals("blood pressure"))image2.setImageResource(R.drawable.bloodpressure);
			else if(type.equals("heart rate"))image2.setImageResource(R.drawable.heartrate);
		}
		else if (activeMeasurementsInGraph.size() == 1)
		{
			activeMeasurementsInGraph.add(type);
			ImageView image1 = (ImageView)findViewById(R.id.imageView1);
			if(type.equals("temperature"))image1.setImageResource(R.drawable.temperature);
			else if(type.equals("blood pressure"))image1.setImageResource(R.drawable.bloodpressure);
			else if(type.equals("heart rate"))image1.setImageResource(R.drawable.heartrate);
		}
		else {
			activeMeasurementsInGraph.remove(0);
			activeMeasurementsInGraph.add(type);
			ImageView image1 = (ImageView)findViewById(R.id.imageView1);
			if(activeMeasurementsInGraph.get(0).equals("temperature"))image1.setImageResource(R.drawable.temperature);
			else if(activeMeasurementsInGraph.get(0).equals("blood pressure"))image1.setImageResource(R.drawable.bloodpressure);
			else if(activeMeasurementsInGraph.get(0).equals("heart rate"))image1.setImageResource(R.drawable.heartrate);
			ImageView image2 = (ImageView)findViewById(R.id.imageView2);
			if(activeMeasurementsInGraph.get(1).equals("temperature"))image1.setImageResource(R.drawable.temperature);
			else if(activeMeasurementsInGraph.get(1).equals("blood pressure"))image1.setImageResource(R.drawable.bloodpressure);
			else if(activeMeasurementsInGraph.get(1).equals("heart rate"))image1.setImageResource(R.drawable.heartrate);
		}
		for(int i=0; i<activeMeasurementsInGraph.size(); i++)
		{
			Log.d("1338", i+ "im graphen: "+activeMeasurementsInGraph.get(i));
		}
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

	private final static int START_DRAGGING = 0;
	private final static int STOP_DRAGGING = 1;
	private boolean draggedOverView = false;

	private LinearLayout rootLayout;
	private int status;
	private LayoutParams params;
	private ImageView image;

	private OnTouchListener myOnDragListener = new OnTouchListener() {

		public boolean onTouch(View view, MotionEvent me) {
			if (me.getAction() == MotionEvent.ACTION_DOWN) {
				status = START_DRAGGING;
				image = new ImageView(StartActivity.this);
				Bitmap bm = view.getDrawingCache();
				image.setImageBitmap(bm);
				rootLayout.addView(image, params);
			}
			if (me.getAction() == MotionEvent.ACTION_UP) {
				status = STOP_DRAGGING;
				Log.i("Drag", "Stopped Dragging");
				// dragged view is moved in the view
				if (me.getX() > mySimpleXYPlot.getLeft()
						& me.getX() < mySimpleXYPlot.getRight()
						& me.getY() < mySimpleXYPlot.getBottom()
						& me.getY() > mySimpleXYPlot.getTop()) {
					rootLayout.removeView(image);
				}
				draggedOverView = false;
			} else if (me.getAction() == MotionEvent.ACTION_MOVE) {
				if (status == START_DRAGGING) {
					System.out.println("Dragging");
					int actualX = (int) me.getX() - (view.getWidth() / 2);
					int actualY = (int) me.getY() - (view.getHeight() / 2);
					image.setPadding(actualX, actualY, 0, 0);
					image.invalidate();
					if (me.getX() > mySimpleXYPlot.getLeft()
							& me.getX() < mySimpleXYPlot.getRight()
							& me.getY() < mySimpleXYPlot.getBottom()
							& me.getY() > mySimpleXYPlot.getTop()) {
						if (!draggedOverView)
							Toast.makeText(StartActivity.this, "IN DER VIEW",
									Toast.LENGTH_SHORT).show();
						draggedOverView = true;
					} else {
						if (draggedOverView) {
							Toast.makeText(StartActivity.this,
									"UND WIEDER RAUS", Toast.LENGTH_SHORT)
									.show();
							draggedOverView = false;
						}
					}
				}
			}
			return false;
		}
	};
}