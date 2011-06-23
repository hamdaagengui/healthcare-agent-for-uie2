package de.uie2.healthcare;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.widget.TextView;

import com.androidplot.Plot;
import com.androidplot.series.XYSeries;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;

import de.uie2.healthcare.patient.Patient;

public class MeasurementActivity extends Activity{

	private XYPlot mySimpleXYPlot;
	private TextView patientName;
	private Patient tempPatient;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.measurementsview);
		super.onCreate(savedInstanceState);
		patientName = (TextView) findViewById(R.id.tx_measurement_patient);
		mySimpleXYPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
		int position = getIntent().getIntExtra(PatientViewActivity.LISTPOSITION, 0);
		tempPatient = DataApplication.getData().getPatient(position);
		
		if (tempPatient.getMeasurements().size()>0){
			patientName.setText("Measurements for: "+tempPatient.getFirstname()+" "+tempPatient.getLastname());
			fillGraph();
		}else{
			patientName.setTextColor(Color.RED);
			patientName.setText("NO Measurements for: "+tempPatient.getFirstname()+" "+tempPatient.getLastname());
			mySimpleXYPlot.setWillNotDraw(true);
		}
	}
	
	private void fillGraph() {

		// x-axis gets the date and Time --- y-axis gehts the measured
		// temperatures
		XYSeries series2 = new SimpleXYSeries(
				// x-axis DATE
				DataApplication.getData().getMeasurementLongDate(tempPatient), 
				// y-axis Temperature
				DataApplication.getData().getMeasurementsTemp(tempPatient),
				"Measurements");

		mySimpleXYPlot.getGraphWidget().getGridBackgroundPaint()
				.setColor(Color.WHITE);
		mySimpleXYPlot.getGraphWidget().getGridLinePaint()
				.setColor(Color.BLACK);
		mySimpleXYPlot.getGraphWidget().getGridLinePaint()
				.setPathEffect(new DashPathEffect(new float[] { 1, 1 }, 1));
		mySimpleXYPlot.getGraphWidget().getDomainOriginLinePaint()
				.setColor(Color.BLACK);
		mySimpleXYPlot.getGraphWidget().getRangeOriginLinePaint()
				.setColor(Color.BLACK);

		mySimpleXYPlot.setBorderStyle(Plot.BorderStyle.SQUARE, null, null);
		mySimpleXYPlot.getBorderPaint().setStrokeWidth(1);
		mySimpleXYPlot.getBorderPaint().setAntiAlias(false);
		mySimpleXYPlot.getBorderPaint().setColor(Color.WHITE);

		// setup our line fill paint to be a slightly transparent gradient:
		Paint lineFill = new Paint();
		lineFill.setAlpha(255);
		lineFill.setShader(new LinearGradient(0, 0, 0, 250, Color.RED,
				Color.WHITE, Shader.TileMode.MIRROR));

		LineAndPointFormatter formatter = new LineAndPointFormatter(Color.DKGRAY, Color.RED, Color.GREEN);
		formatter.setFillPaint(lineFill);
		mySimpleXYPlot.getGraphWidget().setPaddingRight(2);
		mySimpleXYPlot.addSeries(series2, formatter);


		// get Temperatures from 36 to 42°C
		mySimpleXYPlot.setRangeBoundaries(35.0, 42.0, BoundaryMode.FIXED);
		mySimpleXYPlot.setDomainStep(XYStepMode.SUBDIVIDE, DataApplication.getData().getMeasurementLongDate(tempPatient).size());

		// customize our domain/range labels
		mySimpleXYPlot.setDomainLabel("Date");
		mySimpleXYPlot.setRangeLabel("Temperature in C°");

		//set the specia date format 
		mySimpleXYPlot.setDomainValueFormat(new MyDateFormat());
		
		//chance textsize of dates
		mySimpleXYPlot.getGraphWidget().getDomainLabelPaint().setTextSize(6);
		// remove legend widget
		mySimpleXYPlot.getLayoutManager().remove(mySimpleXYPlot.getLegendWidget());

		mySimpleXYPlot.disableAllMarkup();

	}

	private class MyDateFormat extends Format {

		private SimpleDateFormat dateFormat = new SimpleDateFormat(
				"dd.MM.yyyy - HH:mm");

		@Override
		public StringBuffer format(Object obj, StringBuffer toAppendTo,
				FieldPosition pos) {

			long timestamp = ((Number) obj).longValue();
			Date date = new Date(timestamp);
			return dateFormat.format(date, toAppendTo, pos);
		}

		@Override
		public Object parseObject(String source, ParsePosition pos) {
			return null;

		}

	}
	
}
