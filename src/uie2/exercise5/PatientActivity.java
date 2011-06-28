package uie2.exercise5;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;
import android.app.Activity;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class PatientActivity extends Activity {
	public static SimpleDateFormat sdf = new SimpleDateFormat(
			"dd.MM.yyyy HH:mm");
	private SimpleXYSeries series;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.patient_view);

		long id = getIntent().getLongExtra("patient", -1);
		if (id >= 0) {
			Cursor c = getContentResolver().query(
					MyContentProvider.PATIENT_URI,
					new String[] { "lastname", "firstname", "dateofbirth",
							"gender", "address", "city" }, "_id==" + id, null,
					null);
			if (c.getCount() > 0) {
				c.moveToFirst();
				TextView lastname = (TextView) findViewById(R.id.textViewLastname);
				lastname.setText(c.getString(0));
				TextView firstname = (TextView) findViewById(R.id.textViewFirstname);
				firstname.setText(c.getString(1));
				TextView dateofbirth = (TextView) findViewById(R.id.textViewDateofbirth);
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
				finish();
				return;
			}
		} else {
			Toast.makeText(this, "Fehler bei der Weitergabe des Befehls",
					Toast.LENGTH_LONG).show();
			finish();
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

	private void paint() {
		long id = getIntent().getLongExtra("patient", -1);
		XYPlot mySimpleXYPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
		mySimpleXYPlot.removeSeries(series);
		// Create a formatter to use for drawing a series using
		// LineAndPointRenderer:
		LineAndPointFormatter formatter = new LineAndPointFormatter(Color.rgb(
				0, 200, 0), // line color
				Color.rgb(0, 100, 0), // point color
				Color.rgb(150, 190, 150)); // fill color (optional)

		Cursor values = getContentResolver().query(
				MyContentProvider.MEASUREMENT_URI,
				new String[] { "date", "time", "value" }, "patientID==" + id,
				null, null);
		if (values.getCount()>0) {
			Log.d("1337", "" + values.getCount());
			ArrayList<Number> x = new ArrayList<Number>(values.getCount());
			ArrayList<Number> y = new ArrayList<Number>(values.getCount());
			while (values.moveToNext()) {
				x.add(toTimestamp(values.getString(0), values.getString(1)));
				y.add(values.getFloat(2));
			}
			series = new SimpleXYSeries(x, y, "Temperature");
			mySimpleXYPlot.addSeries(series, formatter);
			mySimpleXYPlot.setDomainLabel("Date");
			mySimpleXYPlot.setRangeLabel("Degrees Celsius");
			mySimpleXYPlot.setDomainStep(XYStepMode.SUBDIVIDE,
					values.getCount() / 2);
			values.close();
			mySimpleXYPlot.setDomainValueFormat(new MyDateFormat());
			mySimpleXYPlot.disableAllMarkup();
		}
		mySimpleXYPlot.redraw();
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
		long id = getIntent().getLongExtra("patient", -1);
		ContentValues cv = new ContentValues();
		cv.put("type", "temperature");
		cv.put("metric", "celsius");
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(System.currentTimeMillis());
		cv.put("date", cal.get(Calendar.DAY_OF_MONTH)+
				 "." + (cal.get(Calendar.MONTH)+1)+ "." + cal.get(Calendar.YEAR));
		cv.put("time", cal.get(Calendar.HOUR_OF_DAY)+ ":" + cal.get(Calendar.MINUTE));
		Random rand = new Random();
		cv.put("value", Math.round((36f + 2f * rand.nextFloat()) * 10f) / 10f);
		cv.put("patientID", id);
		getContentResolver().insert(MyContentProvider.MEASUREMENT_URI, cv);
		getContentResolver().notifyChange(MyContentProvider.MEASUREMENT_URI, null);
		paint();
	}
}
