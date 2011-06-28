package uie2.exercise5;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import android.content.ContentValues;

public class Measurement {
	private String type;
	private String metric;
	private String date;
	private String time;
	private float value;
	private long patientId;

	public Measurement(Node item, long patientId) {
		NamedNodeMap map = item.getAttributes();
		type = map.getNamedItem("type").getNodeValue();
		metric = map.getNamedItem("metric").getNodeValue();
		date = map.getNamedItem("date").getNodeValue();
		time = map.getNamedItem("time").getNodeValue();
		value = Float.parseFloat(item.getChildNodes().item(0).getNodeValue());
		this.patientId = patientId;
	}

	public Measurement(String type, String metric, String date, String time,
			float value, long patientId) {
		super();
		this.type = type;
		this.metric = metric;
		this.date = date;
		this.time = time;
		this.value = value;
		this.patientId = patientId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMetric() {
		return metric;
	}

	public void setMetric(String metric) {
		this.metric = metric;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
	}

	public long getPatientId() {
		return patientId;
	}

	public void setPatientId(long patientId) {
		this.patientId = patientId;
	}

	public ContentValues getValues() {
		ContentValues cv = new ContentValues();
		cv.put("time", time);
		cv.put("date", date);
		cv.put("patientID", patientId);
		cv.put("metric", metric);
		cv.put("type", type);
		cv.put("value", value);
		return cv;
	}
}
