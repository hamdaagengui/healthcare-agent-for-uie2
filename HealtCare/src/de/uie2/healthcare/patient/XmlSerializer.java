package de.uie2.healthcare.patient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.simpleframework.xml.core.Persister;

import android.util.Log;

public class XmlSerializer extends Persister{

	private static final String TAG = "SimpleXmlSerializer";
	private mOutputStream output;
	
	class mOutputStream extends OutputStream{
		StringBuffer sb = new StringBuffer();

		@Override
		public void write(int oneByte) throws IOException {
			sb.append((char)oneByte);
		}

		@Override
		public String toString() {
			return sb.toString();
		}
		
	}
	
	public String serializeToXmlString(Object inputClass){
		this.output = new mOutputStream();
		try {
			this.write(inputClass, output);
			Log.d(TAG, "serialize from "+inputClass.getClass().getSimpleName()+ " Class to XML String: SUCCESS");
			return output.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d(TAG, "serialize from "+inputClass.getClass().getSimpleName()+ " Class to XML String: FAIL");
			return null;
		}
		
	}

	public File serializeToXmlFile(Object inputClass){
		File output = new File("output.xml");
		try {
			this.write(inputClass, output);
			return output;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	public Object deserializeToClass(String inputXmlString, Object inputClass){
		
		try {
			Object newClass = this.read(inputClass, inputXmlString);
			Log.d(TAG, "deserialize from XML String to "+inputClass.getClass().getSimpleName()+ " Class : SUCCESS");
			return newClass;
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "deserialize from XML String to "+inputClass.getClass().getSimpleName()+ " Class : FAIL");
			return null;
		}
	}
	
	public Object deserializeToClass(InputStream inputXmlStream, Object inputClass){
		
		try {
			Object newClass = this.read(inputClass, inputXmlStream);
			Log.d(TAG, "deserialize from XML String to "+inputClass.getClass().getSimpleName()+ " Class : SUCCESS");
			return newClass;
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "deserialize from XML String to "+inputClass.getClass().getSimpleName()+ " Class : FAIL");
			return null;
		}
	}
	
	
}
