/*
 *  BaseProvider.java
 *
 *  Written by: Fintan Mahon 12101524
 *  
 *  Description: Create all tables in SQLite databasen team
 *  
 *  Written on: Jan 2013
 *  
 * 
 */
package fm.gaa_scores.lite;

import android.content.ContentProvider;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class DatabaseSetup extends ContentProvider {

	DatabaseHelper dbHelper;

	public static final String DATABASE_NAME = "teams";
	public static final int DATABASE_VERSION = 1;

	// setup table to store panel player details
	
	private static final String CREATE_TABLE_STATS = "create table "
			+ TeamContentProvider.DATABASE_TABLE_STATS
			+ " (_id integer primary key autoincrement, " 
			+ TeamContentProvider.STATSLINE + " text);";

	// inner class to create database
	static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		// method to create database tables defined above
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_TABLE_STATS); 
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// code to upgrade your db here (all of your tables, indexes,
			// triggers...)
			// upgrade to add panelname to DB
		}
	}

}
