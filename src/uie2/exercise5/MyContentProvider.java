package uie2.exercise5;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;

public class MyContentProvider extends ContentProvider {
	private static final int MEASUREMENT = 0;
	private static final int PATIENT = 1;
	private static final String AUTHORITY = "uie2.exercise5";
	private static final String PATIENT_TABLE_NAME = "Patient";
	private static final String MEASUREMENT_TABLE_NAME = "Measurement";
	public static final Uri PATIENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/Patient");
	public static final Uri MEASUREMENT_URI = Uri.parse("content://"
			+ AUTHORITY + "/Measurement");
	private Database database;
	private UriMatcher uriMatcher;

	@Override
	public boolean onCreate() {
		database = new Database(getContext());
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, PATIENT_TABLE_NAME, PATIENT);
		uriMatcher.addURI(AUTHORITY, MEASUREMENT_TABLE_NAME, MEASUREMENT);
		return true;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count;
		switch (uriMatcher.match(uri)) {
		case PATIENT:
			count = database.getWritableDatabase().delete(PATIENT_TABLE_NAME,
					selection, selectionArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			return count;
		case MEASUREMENT:
			count = database.getWritableDatabase().delete(
					MEASUREMENT_TABLE_NAME, selection, selectionArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			return count;
		default:
			throw new IllegalArgumentException("Unknown URI" + uri);
		}
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case PATIENT:
			return Patient.class.getName();
		case MEASUREMENT:
			return Measurement.class.getName();
		default:
			throw new IllegalArgumentException("Unknown URI" + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long rowID;
		switch (uriMatcher.match(uri)) {
		case PATIENT:
			rowID = database.getWritableDatabase().insert(PATIENT_TABLE_NAME,
					null, values);
			if (rowID >= 0) {
				getContext().getContentResolver().notifyChange(uri, null);
				return ContentUris.withAppendedId(PATIENT_URI, rowID);
			}
			break;
		case MEASUREMENT:
			rowID = database.getWritableDatabase().insert(
					MEASUREMENT_TABLE_NAME, null, values);
			if (rowID >= 0) {
				getContext().getContentResolver().notifyChange(uri, null);
				return ContentUris.withAppendedId(MEASUREMENT_URI, rowID);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI" + uri);
		}

		throw new SQLiteException("Failed to insert row into " + uri);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		switch (uriMatcher.match(uri)) {
		case PATIENT:
			return database.getReadableDatabase()
					.query(PATIENT_TABLE_NAME, projection, selection,
							selectionArgs, null, null, sortOrder);
		case MEASUREMENT:
			return database.getReadableDatabase()
					.query(MEASUREMENT_TABLE_NAME, projection, selection,
							selectionArgs, null, null, sortOrder);
		default:
			throw new IllegalArgumentException("Unknown URI" + uri);
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int count;
		switch (uriMatcher.match(uri)) {
		case PATIENT:
			count = database.getWritableDatabase().update(PATIENT_TABLE_NAME,
					values, selection, selectionArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			return count;
		case MEASUREMENT:
			count = database.getWritableDatabase().update(
					MEASUREMENT_TABLE_NAME, values, selection, selectionArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			return count;
		default:
			throw new IllegalArgumentException("Unknown URI" + uri);
		}
	}
}
