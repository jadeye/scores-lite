/*
 *  PanelContentProvider.java
 *
 *  Reference: BooksProvider class from Beginning Android 4 Application Development by Wei-Meng Lee
 *  http://www.wrox.com/WileyCDA/WroxTitle/Beginning-Android-4-Application-Development.productCd-1118199545,descCd-DOWNLOAD.html
 *  modified to suit
 *  
 *  Description: This class is the Content Provider for the panel table in the App database
 *  it facilitates CRUD operations on the database table and notifies ContentResolver whenever
 *  the database is changed
 *  

 *  Written on: Jan 2013
 *  
 * 
 */
package fm.gaa_scores.lite;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class TeamContentProvider extends DatabaseSetup {
	// set up uri for content provider
	public static final String PROVIDER_NAME = "fm.gaa_scores.lite.provider.team";
	public static final String BASE_PATH_2 = "stats";

	public static final Uri CONTENT_URI_2 = Uri.parse("content://"
			+ PROVIDER_NAME + "/" + BASE_PATH_2);
//
	public static final String _ID = "_id";
	private static final int STATS = 3;
	private static final int STATS_ID = 4;

	// use urimatcher to parse input uri from contentresolver
	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(PROVIDER_NAME, BASE_PATH_2, STATS);
		uriMatcher.addURI(PROVIDER_NAME, BASE_PATH_2 + "/#", STATS_ID);
	}

	// ---for database use---
	private SQLiteDatabase matchAppDB;
	public static final String DATABASE_TABLE_STATS = "stats";
	public static final String STATSID = "_id";
	public static final String STATSLINE = "line";
	

	@Override
	// open connection to team defined in BaseProvider Class
	public boolean onCreate() {
		Context context = getContext();
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		matchAppDB = dbHelper.getWritableDatabase();
		return (matchAppDB == null) ? false : true;
	}

	@Override
	// delete players from database
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count = 0;
		switch (uriMatcher.match(uri)) {
		// delete all players
		case STATS:
			count = matchAppDB.delete(DATABASE_TABLE_STATS, selection, selectionArgs);
			break;
		// delete single player
		case STATS_ID:
			String id1 = uri.getPathSegments().get(1);
			count = matchAppDB.delete(DATABASE_TABLE_STATS, _ID
					+ " = "
					+ id1
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
							+ ')' : ""), selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		// notify contentresolver of change
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		// ---get all players---
		case STATS:
			return "fm.gaa_scores.lite.cursor.dir/fm.gaa_scores.lite.players ";
			// ---get a particular player---
		case STATS_ID:
			return "fm.gaa_scores.lite.cursor.item/fm.gaa_scores.lite.players ";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	//
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Uri _uri = null;

		switch (uriMatcher.match(uri)) {
		case STATS:
			long rowID2 = matchAppDB.insert(DATABASE_TABLE_STATS, "", values);
			if (rowID2 > 0) {
				_uri = ContentUris.withAppendedId(CONTENT_URI_2, rowID2);
				// notify contentresolver of change
				getContext().getContentResolver().notifyChange(_uri, null);
			}
			break;
		default:

			throw new SQLException("Failed to insert row into " + uri);
		}
		return _uri;
	}

	@Override
	//N
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
		switch (uriMatcher.match(uri)) {

		case STATS:
			sqlBuilder.setTables(DATABASE_TABLE_STATS);
			if (uriMatcher.match(uri) == STATS_ID)
				// ---if getting a particular player---
				sqlBuilder.appendWhere(_ID + " = "
						+ uri.getPathSegments().get(1));
			if (sortOrder == null || sortOrder == "")
				sortOrder = STATSID;
			break;

		}
		Cursor c = sqlBuilder.query(matchAppDB, projection, selection,
				selectionArgs, null, null, sortOrder);
		// notify contentresolver of change
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int count = 0;
		switch (uriMatcher.match(uri)) {
		// update single player
		case STATS:
			count = matchAppDB.update(DATABASE_TABLE_STATS, values, selection,
					selectionArgs);
			break;
		// update all players
		case STATS_ID:
			count = matchAppDB.update(
					DATABASE_TABLE_STATS,
					values,
					_ID
							+ " = "
							+ uri.getPathSegments().get(1)
							+ (!TextUtils.isEmpty(selection) ? " AND ("
									+ selection + ')' : ""), selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		// notify contentresolver of change
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
}
