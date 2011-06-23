package de.uie2.healthcare.patient;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Order;
import org.simpleframework.xml.Text;

@Order(attributes={"type","metric","date","time"})
public class Measurement {

	@Attribute(required=false)
	private String type;
	@Attribute(required=false)
	private String metric;
	@Attribute(required=false)
	private String date;
	@Attribute(required=false)
	private String time;
	
	@Text(required=false)
	private double value;
	
	public Measurement(){
		
	}
	
	public Measurement(String type, String metric, String date, String time,
			double value) {
		super();
		this.type = type;
		this.metric = metric;
		this.date = date;
		this.time = time;
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public String getMetric() {
		return metric;
	}

	public String getDate() {
		return date;
	}

	public String getTime() {
		return time;
	}

	public double getValue() {
		return value;
	}
	
	
	
}
