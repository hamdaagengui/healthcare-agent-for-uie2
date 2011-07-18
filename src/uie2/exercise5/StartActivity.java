package uie2.exercise5;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

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
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.series.XYSeries;
import com.androidplot.ui.Formatter;
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
	private TextView popUp;

	private final static int START_DRAGGING = 0;
	private final static int STOP_DRAGGING = 1;

	private final static String TYPE_TEMPERATURE = "temperature";
	private final static String TYPE_HEARTRATE = "heart rate";
	private final static String TYPE_BLOODPRESSURE = "blood pressure";
	private final static String TYPE_MEDICATION = "medication";

	private boolean draggedOverView = false;

	private FrameLayout rootLayout;
	private int status;
	private LayoutParams params;
	private ImageView image;

	private XYPlot mySimpleXYPlot;
	private boolean added;
	private FrameLayout graphFramelayout;
	private long x_min;
	private long x_max;
	private long lowerBoundary;
	private long upperBoundary;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		series = new ArrayList<SimpleXYSeries>();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

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

		temperature_button.setDrawingCacheEnabled(true);
		blood_pressure_button.setDrawingCacheEnabled(true);
		heart_rate_button.setDrawingCacheEnabled(true);
		time_of_medication_button.setDrawingCacheEnabled(true);

		temperature_button.setOnTouchListener(myOnDragListener);
		blood_pressure_button.setOnTouchListener(myOnDragListener);
		heart_rate_button.setOnTouchListener(myOnDragListener);
		time_of_medication_button.setOnTouchListener(myOnDragListener);

		graphFramelayout = (FrameLayout) findViewById(R.id.graphFrameLayout);
		rootLayout = (FrameLayout) findViewById(R.id.rootlayoutRoot);
		params = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		activeMeasurementsInGraph = new ArrayList<String>();
		updateButtons();

		setPatient(id);
		chooseMeasurementType(TYPE_HEARTRATE);
		updateButtons();
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
		mySimpleXYPlot.setOnTouchListener(myGraphTouchListener);
		for (int i = 0; i < series.size(); i++) {
			mySimpleXYPlot.removeSeries(series.get(i));
		}
		series.clear();
		// Create a formatter to use for drawing a series using
		// LineAndPointRenderer:
		x_min = Long.MAX_VALUE;
		x_max = Long.MIN_VALUE;

		Cursor types = getContentResolver().query(
				MyContentProvider.MEASUREMENT_URI,
				new String[] { "DISTINCT type" }, "patientId=" + id, null,
				"type ASC");
		if (types.getCount() > 0) {
			while (types.moveToNext()) {
				String type = types.getString(0);
				if (activeMeasurementsInGraph.contains(type)
						|| (type.startsWith(TYPE_BLOODPRESSURE) && activeMeasurementsInGraph
								.contains(TYPE_BLOODPRESSURE))) {
					LineAndPointFormatter formatter = new LineAndPointFormatter(
							getColorByType(type), // line color
							getColorByType(type), // point color
							null); // fill color (optional)
					Paint lineP = new Paint();
					lineP.setStyle(Paint.Style.STROKE);
					lineP.setStrokeWidth(8);
					lineP.setColor(getColorByType(type));
					formatter.setLinePaint(lineP);
					Paint vertexP = new Paint();
					vertexP.setStyle(Paint.Style.STROKE);
					vertexP.setStrokeWidth(14);
					vertexP.setColor(getColorByType(type));
					formatter.setVertexPaint(vertexP);
					Cursor values = getContentResolver().query(
							MyContentProvider.MEASUREMENT_URI,
							new String[] { "date", "time", "value" },
							"patientID==" + id + " AND type=\"" + type + "\"",
							null, null);
					if (values.getCount() > 0) {
						ArrayList<Number> x = new ArrayList<Number>(
								values.getCount());
						ArrayList<Number> y = new ArrayList<Number>(
								values.getCount());
						while (values.moveToNext()) {
							long x_val = toTimestamp(values.getString(0),
									values.getString(1));
							if (x_val < x_min) {
								x_min = x_val;
							}
							if (x_val > x_max) {
								x_max = x_val;
							}
							x.add(x_val);
							y.add(normalizeValue((values.getFloat(2)), type));

						}
						SimpleXYSeries serie = new SimpleXYSeries(x, y, type);
						series.add(serie);
						mySimpleXYPlot.addSeries(serie, formatter);
						mySimpleXYPlot.setRangeBoundaries(0, 100,
								BoundaryMode.AUTO);
						mySimpleXYPlot.setRangeLabel("");
						mySimpleXYPlot.setRangeStepValue(21);
						mySimpleXYPlot.getGraphWidget().getRangeLabelPaint()
								.setAlpha(0);
						mySimpleXYPlot.getGraphWidget()
								.getRangeOriginLabelPaint().setAlpha(0);
						mySimpleXYPlot.setDomainLabel("Date");
						// mySimpleXYPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL,
						// 1);
						mySimpleXYPlot.setDomainStep(
								XYStepMode.INCREMENT_BY_VAL,
								1000 * 60 * 60 * 24);
						lowerBoundary = x_max - 1000 * 60 * 60 * 24 * 7;
						upperBoundary = x_max;
						mySimpleXYPlot.setDomainBoundaries(lowerBoundary,
								upperBoundary, BoundaryMode.AUTO);
						values.close();
						mySimpleXYPlot.setDomainValueFormat(new MyDateFormat());
						mySimpleXYPlot.disableAllMarkup();
					}
					mySimpleXYPlot.redraw();
					if (added) {
						removePopUp();
						mySimpleXYPlot.setCursorPosition(0, 0);
					}
				}
			}
		}
		try {
			mySimpleXYPlot.getSeriesSet();
			mySimpleXYPlot.getSeriesSet().isEmpty();
		} catch (Exception e) {
			ArrayList<Number> x = new ArrayList<Number>();
			ArrayList<Number> y = new ArrayList<Number>();
			SimpleXYSeries serie = new SimpleXYSeries(x, y, "");
			series.add(serie);
			LineAndPointFormatter formatter = new LineAndPointFormatter(
					Color.BLACK, Color.BLACK, Color.BLACK);
			mySimpleXYPlot.addSeries(serie, formatter);
			mySimpleXYPlot.setRangeBoundaries(0, 100, BoundaryMode.AUTO);
			mySimpleXYPlot.setRangeLabel("");
			mySimpleXYPlot.setRangeStepValue(21);
			mySimpleXYPlot.getGraphWidget().getRangeLabelPaint().setAlpha(0);
			mySimpleXYPlot.getGraphWidget().getRangeOriginLabelPaint()
					.setAlpha(0);
			mySimpleXYPlot.setDomainLabel("Date");
			mySimpleXYPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL,
					1000 * 60 * 60 * 24);
			lowerBoundary = System.currentTimeMillis() - 1000 * 60 * 60 * 24
					* 7;
			upperBoundary = System.currentTimeMillis();
			mySimpleXYPlot.setDomainBoundaries(lowerBoundary, upperBoundary,
					BoundaryMode.AUTO);
			mySimpleXYPlot.setDomainValueFormat(new MyDateFormat());
			mySimpleXYPlot.disableAllMarkup();
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.add_patient) {
			Intent i = new Intent(this, AddPatientActivity.class);
			startActivity(i);
		} else if (item.getItemId() == R.id.add_measurement) {
			ContentValues cv = new ContentValues();
			cv.put("type", TYPE_TEMPERATURE);
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
		// Log.d("1337", " normalize type: " + type + " value: " + value);
		if (type.equals(TYPE_TEMPERATURE))
			return ((100 / 9) * value - 389);
		else if (type.equals(TYPE_BLOODPRESSURE + " diastole"))
			return (value / 2);
		else if (type.equals(TYPE_BLOODPRESSURE + " systole"))
			return (value / 2);
		else if (type.equals(TYPE_HEARTRATE))
			return (value / (2.1f));
		else if (type.equals(TYPE_MEDICATION))
			return 50f;
		return -1;
	}

	private float denormalizeValue(float normalizedValue, String type) {
		if (type.equals(TYPE_TEMPERATURE))
			return ((normalizedValue * 9) + (389 * 9)) / 100;
		else if (type.equals(TYPE_BLOODPRESSURE + " diastole"))
			return normalizedValue * 2;
		else if (type.equals(TYPE_BLOODPRESSURE + " systole"))
			return normalizedValue * 2;
		else if (type.equals(TYPE_HEARTRATE))
			return normalizedValue * 2.1f;
		return -1;
	}

	private Integer getColorByType(String type) {
		if (type.equals(TYPE_TEMPERATURE))
			return Color.rgb(0, 200, 0);
		else if (type.equals(TYPE_BLOODPRESSURE + " diastole"))
			return Color.rgb(127, 219, 255);
		else if (type.equals(TYPE_BLOODPRESSURE + " systole"))
			return Color.rgb(98, 12, 67);
		else if (type.equals(TYPE_HEARTRATE))
			return Color.rgb(255, 0, 0);
		else if (type.equals(TYPE_MEDICATION))
			return Color.rgb(255, 255, 255);
		return Color.rgb(0, 200, 0);
	}

	private void chooseMeasurement(int buttonID) {
		switch (buttonID) {
		case (R.id.bloodpressure):
			chooseMeasurementType(TYPE_BLOODPRESSURE);
			paint();
			break;
		case (R.id.heartrate):
			chooseMeasurementType(TYPE_HEARTRATE);
			paint();
			break;
		case (R.id.temperature):
			chooseMeasurementType(TYPE_TEMPERATURE);
			paint();
			break;
		case (R.id.timeofmedication):
			chooseMeasurementType(TYPE_MEDICATION);
			paint();
			break;
		}
	}

	// TYPE for timeofmedication = TYPE_MEDICATION
	private void chooseMeasurementType(String type) {
		if (activeMeasurementsInGraph.isEmpty()) {
			activeMeasurementsInGraph.add(type);
			ImageView image2 = (ImageView) findViewById(R.id.imageView2);
			if (type.equals(TYPE_TEMPERATURE))
				image2.setImageResource(R.drawable.temperature);
			else if (type.equals(TYPE_BLOODPRESSURE))
				image2.setImageResource(R.drawable.bloodpressure);
			else if (type.equals(TYPE_HEARTRATE))
				image2.setImageResource(R.drawable.heartrate);
			else if (type.equals(TYPE_MEDICATION))
				image2.setImageResource(R.drawable.black);
		} else if (activeMeasurementsInGraph.size() == 1) {
			activeMeasurementsInGraph.add(type);
			ImageView image1 = (ImageView) findViewById(R.id.imageView1);
			if (type.equals(TYPE_TEMPERATURE))
				image1.setImageResource(R.drawable.temperature);
			else if (type.equals(TYPE_BLOODPRESSURE))
				image1.setImageResource(R.drawable.bloodpressure);
			else if (type.equals(TYPE_HEARTRATE))
				image1.setImageResource(R.drawable.heartrate);
			else if (type.equals(TYPE_MEDICATION))
				image1.setImageResource(R.drawable.black);
		} else {
			activeMeasurementsInGraph.remove(0);
			activeMeasurementsInGraph.add(type);
			ImageView image1 = (ImageView) findViewById(R.id.imageView1);
			if (activeMeasurementsInGraph.get(0).equals(TYPE_TEMPERATURE))
				image1.setImageResource(R.drawable.temperature);
			else if (activeMeasurementsInGraph.get(0)
					.equals(TYPE_BLOODPRESSURE))
				image1.setImageResource(R.drawable.bloodpressure);
			else if (activeMeasurementsInGraph.get(0).equals(TYPE_HEARTRATE))
				image1.setImageResource(R.drawable.heartrate);
			else if (activeMeasurementsInGraph.get(0).equals(TYPE_MEDICATION))
				image1.setImageResource(R.drawable.black);
			ImageView image2 = (ImageView) findViewById(R.id.imageView2);
			if (activeMeasurementsInGraph.get(1).equals(TYPE_TEMPERATURE))
				image2.setImageResource(R.drawable.temperature);
			else if (activeMeasurementsInGraph.get(1)
					.equals(TYPE_BLOODPRESSURE))
				image2.setImageResource(R.drawable.bloodpressure);
			else if (activeMeasurementsInGraph.get(1).equals(TYPE_HEARTRATE))
				image2.setImageResource(R.drawable.heartrate);
			else if (activeMeasurementsInGraph.get(1).equals(TYPE_MEDICATION))
				image2.setImageResource(R.drawable.black);
		}
		for (int i = 0; i < activeMeasurementsInGraph.size(); i++) {
			Log.d("1338", i + "im graphen: " + activeMeasurementsInGraph.get(i));
		}
	}

	public void setPatient(long patientId) {
		Log.d("1339", "patientid: " + patientId);
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
	}

	private OnTouchListener myGraphTouchListener = new OnTouchListener() {
		private int touches = 0;

		public boolean onTouch(View v, MotionEvent event) {
			Log.i("OnGraphTouch", "touches=" + touches);
			if (event.getAction() == MotionEvent.ACTION_DOWN
					&& mySimpleXYPlot.containsPoint(event.getX(), event.getY())) {
				touches++;
				if (touches<=1) {
					mySimpleXYPlot
							.setCursorPosition(event.getX(), event.getY());
					long xAxisValue = mySimpleXYPlot.getGraphWidget()
							.getXVal(event.getX()).longValue();
					double yAxisValue = mySimpleXYPlot.getGraphWidget()
							.getRangeCursorVal();
					mySimpleXYPlot.getGraphWidget().setCursorLabelPaint(null);
					String xAxisDate = new Date(xAxisValue).toLocaleString();
					Log.d("OnGraphTouch", "X-Value: " + xAxisDate
							+ "\nY-Value: " + yAxisValue);
					getInfoTextFromLongDate(xAxisValue);
					if (!added)
						createPopUp(getInfoTextFromLongDate(xAxisValue),
								(int) event.getX(), (int) event.getY());
					else {
						removePopUp();
						createPopUp(getInfoTextFromLongDate(xAxisValue),
								(int) event.getX(), (int) event.getY());

					}
				}
			}
			if (event.getAction() == MotionEvent.ACTION_UP) {
				touches--;
			}
			if (event.getAction() == MotionEvent.ACTION_MOVE
					&& mySimpleXYPlot.containsPoint(event.getX(), event.getY())) {
				if (touches <= 1) {
					if (event.getHistorySize() > 0) {
						mySimpleXYPlot.setCursorPosition(event.getX(),
								event.getY());
						long xAxisValue = mySimpleXYPlot.getGraphWidget()
								.getXVal(event.getX()).longValue();
						double yAxisValue = mySimpleXYPlot.getGraphWidget()
								.getRangeCursorVal();
						String xAxisDate = new Date(xAxisValue)
								.toLocaleString();
						Log.d("OnGraphTouch", "X-Value: " + xAxisDate
								+ "\nY-Value: " + yAxisValue);
						popUp.setText(getInfoTextFromLongDate(xAxisValue));
						popUp.setPadding((int) event.getX(),
								(int) event.getY(), 0, 0);
					}
				} else {
					removePopUp();
					mySimpleXYPlot.setCursorPosition(0, 0);
					if (event.getHistorySize() > 0) {
						mySimpleXYPlot.setCursorPosition(event.getX(),
								event.getY());
						long present = mySimpleXYPlot.getGraphWidget()
								.getXVal(event.getX()).longValue();
						mySimpleXYPlot.setCursorPosition(event.getX(),
								event.getY());
						long last = mySimpleXYPlot.getGraphWidget()
								.getXVal(event.getHistoricalX(0)).longValue();
						long diff = present - last;
						lowerBoundary -= diff;
						upperBoundary -= diff;

						mySimpleXYPlot.setDomainBoundaries(lowerBoundary,
								upperBoundary, BoundaryMode.AUTO);
					}
				}
				return true;
			}

			return true;
		}
	};

	private void removePopUp() {
		graphFramelayout.removeView(popUp);
		added = false;
	}

	private void createPopUp(String text, int x, int y) {
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		popUp = new TextView(this);
		popUp.setPadding(x, y, 0, 0);
		popUp.setText(text);
		popUp.setTextSize(20);
		popUp.setTextColor(Color.BLACK);
		graphFramelayout.addView(popUp, params);
		popUp.setOnTouchListener(myGraphTouchListener);
		added = true;
	}

	private String getInfoTextFromLongDate(long dateValue) {
		String dateString = "";
		String temperatur = "";
		String heartRate = "";
		String bloodpressure = "";
		String medication = "";
		Set<XYSeries> seriesset = mySimpleXYPlot.getSeriesSet();
		Iterator<XYSeries> seriesiterator = seriesset.iterator();
		while (seriesiterator.hasNext()) {
			ArrayList<Long> dateValues = new ArrayList<Long>();
			XYSeries serie = seriesiterator.next();
			if (series.size() <= 0) {
				continue;
			}
			for (int i = 0; i < serie.size(); i++) {
				dateValues.add((Long) serie.getX(i));
			}

			dateString = new Date(getNearestDate(dateValues, dateValue))
					.toLocaleString();
			if (serie.getTitle().contains(TYPE_TEMPERATURE)) {
				temperatur = "\nTemperatur: "
						+ cutNumber(denormalizeValue(
								(Float) serie.getY(getNearestAT(dateValues,
										dateValue)), serie.getTitle())) + " °C";
			}
			if (serie.getTitle().contains(TYPE_BLOODPRESSURE + " diastole")) {
				bloodpressure = "\nBloodpressure: "
						+ denormalizeValue((Float) serie.getY(getNearestAT(
								dateValues, dateValue)), serie.getTitle());

			}
			if (serie.getTitle().contains(TYPE_BLOODPRESSURE + " systole")) {
				bloodpressure += " - "
						+ denormalizeValue((Float) serie.getY(getNearestAT(
								dateValues, dateValue)), serie.getTitle())
						+ " bpm";

			}
			if (serie.getTitle().contains(TYPE_MEDICATION)) {
				medication = "\nMedication: xyz";

			}
			if (serie.getTitle().contains(TYPE_HEARTRATE)) {
				heartRate = "\nHeart-Rate: "
						+ denormalizeValue((Float) serie.getY(getNearestAT(
								dateValues, dateValue)), serie.getTitle())
						+ " mmHg";
			}
		}
		// walk through displayed measurements and get Values from the given
		// long dateValue
		// walk through arrays to find values which is nearest to the
		// cursorpoint in graph

		return dateString + temperatur + heartRate + bloodpressure + medication;
	}

	private int getNearestAT(ArrayList<Long> seriesDates, long dateValue) {
		int minimumAt = 0;
		long minimumDifference = Math.abs(seriesDates.get(0) - dateValue);
		int i = 0;
		for (long entry : seriesDates) {
			if (Math.abs(seriesDates.get(i) - dateValue) < minimumDifference) {
				minimumAt = i;
				minimumDifference = Math.abs(seriesDates.get(i) - dateValue);
			}
			i++;
		}
		return minimumAt;
	}

	private long getNearestDate(ArrayList<Long> seriesDates, long dateValue) {
		int minimumAt = 0;
		if (!seriesDates.isEmpty()) {
			long minimumDifference = Math.abs(seriesDates.get(0) - dateValue);
			long nextDate = seriesDates.get(0);
			int i = 0;
			for (long entry : seriesDates) {
				if (Math.abs(seriesDates.get(i) - dateValue) < minimumDifference) {
					minimumAt = i;
					minimumDifference = Math
							.abs(seriesDates.get(i) - dateValue);
					nextDate = seriesDates.get(i);
				}
				i++;
			}
			return nextDate;
		} else {
			return 0;
		}
	}

	private double cutNumber(double input) {
		input = input * 100;
		int bla = (int) input;
		double bla2 = bla / 100d;
		return bla2;
	}

	private OnTouchListener myOnDragListener = new OnTouchListener() {

		public boolean onTouch(View view, MotionEvent me) {
			if (me.getAction() == MotionEvent.ACTION_DOWN) {
				status = START_DRAGGING;
				image = new ImageView(StartActivity.this);
				Bitmap bm = view.getDrawingCache();
				image.setImageBitmap(bm);
				int actualX = (int) me.getRawX() - (view.getWidth() / 2);
				int actualY = (int) me.getRawY() - (view.getHeight() / 2);
				image.setPadding(actualX, actualY, 0, 0);
				rootLayout.addView(image, params);
			}
			if (me.getAction() == MotionEvent.ACTION_UP) {
				status = STOP_DRAGGING;
				Log.i("Drag", "Stopped Dragging");
				// dragged view is moved in the view

				if (me.getRawX() > graphFramelayout.getX()
						+ rootLayout.getWidth() / 4
						& me.getRawX() < graphFramelayout.getX()
								+ graphFramelayout.getWidth()
								+ rootLayout.getWidth() / 4
						& me.getRawY() < graphFramelayout.getY()
								+ graphFramelayout.getHeight()
								+ rootLayout.getHeight() / 6
						& me.getRawY() > graphFramelayout.getY()
								+ rootLayout.getHeight() / 6) {
					chooseMeasurement(view.getId());
					updateButtons();
				}
				rootLayout.removeView(image);
				draggedOverView = false;
			} else if (me.getAction() == MotionEvent.ACTION_MOVE) {
				if (status == START_DRAGGING) {
					int actualX = (int) me.getRawX() - (view.getWidth() / 2);
					int actualY = (int) me.getRawY() - (view.getHeight() / 2);
					image.setPadding(actualX, actualY, 0, 0);
					image.invalidate();
				}
			}
			return true;
		}
	};

	private OnClickListener myDismissButtonListener = new OnClickListener() {

		public void onClick(View v) {
			if (activeMeasurementsInGraph.size() > 1) {
				switch (v.getId()) {
				case (R.id.bloodpressure):
					activeMeasurementsInGraph.remove(TYPE_BLOODPRESSURE);
					break;
				case (R.id.heartrate):
					activeMeasurementsInGraph.remove(TYPE_HEARTRATE);
					break;
				case (R.id.temperature):
					activeMeasurementsInGraph.remove(TYPE_TEMPERATURE);
					break;
				case (R.id.timeofmedication):
					activeMeasurementsInGraph.remove(TYPE_MEDICATION);
					break;
				}
				updateButtons();
				paint();
			}
		}
	};

	private void updateButtons() {
		if (activeMeasurementsInGraph.contains(TYPE_TEMPERATURE)) {
			temperature_button.setBackgroundResource(R.drawable.buttonbggreen);
			temperature_button.setOnClickListener(myDismissButtonListener);
			temperature_button.setOnTouchListener(null);
		} else {
			temperature_button.setBackgroundResource(R.drawable.buttonbgred);
			temperature_button.setOnClickListener(null);
			temperature_button.setOnTouchListener(myOnDragListener);
		}
		if (activeMeasurementsInGraph.contains(TYPE_BLOODPRESSURE)) {
			blood_pressure_button
					.setBackgroundResource(R.drawable.buttonbggreen);
			blood_pressure_button.setOnClickListener(myDismissButtonListener);
			blood_pressure_button.setOnTouchListener(null);
		} else {
			blood_pressure_button.setBackgroundResource(R.drawable.buttonbgred);
			blood_pressure_button.setOnClickListener(null);
			blood_pressure_button.setOnTouchListener(myOnDragListener);
		}

		if (activeMeasurementsInGraph.contains(TYPE_HEARTRATE)) {
			heart_rate_button.setBackgroundResource(R.drawable.buttonbggreen);
			heart_rate_button.setOnClickListener(myDismissButtonListener);
			heart_rate_button.setOnTouchListener(null);
		} else {
			heart_rate_button.setBackgroundResource(R.drawable.buttonbgred);
			heart_rate_button.setOnClickListener(null);
			heart_rate_button.setOnTouchListener(myOnDragListener);
		}

		if (activeMeasurementsInGraph.contains(TYPE_MEDICATION)) {
			time_of_medication_button
					.setBackgroundResource(R.drawable.buttonbggreen);
			time_of_medication_button
					.setOnClickListener(myDismissButtonListener);
			time_of_medication_button.setOnTouchListener(null);
		} else {
			time_of_medication_button
					.setBackgroundResource(R.drawable.buttonbgred);
			time_of_medication_button.setOnClickListener(null);
			time_of_medication_button.setOnTouchListener(myOnDragListener);
		}
	}
}