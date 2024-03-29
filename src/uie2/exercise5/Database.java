package uie2.exercise5;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 1;
	private static final String db_name = "uie2.exercise5.Database";

	public Database(Context context) {
		super(context, db_name, null, DATABASE_VERSION);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS Patient ("
				+ "_id INTEGER PRIMARY KEY NOT NULL, "
				+ "lastname VARCHAR, firstname VARCHAR, "
				+ " dateofbirth VARCHAR, gender VARCHAR(1), "
				+ "address VARCHAR, city VARCHAR, pictureURI String)");
		db.execSQL("CREATE TABLE IF NOT EXISTS Measurement ("
				+ "_id INTEGER PRIMARY KEY NOT NULL, "
				+ "patientID INTEGER, type STRING, "
				+ "metric STRING, date STRING, time String, value FLOAT)");
	}
}
