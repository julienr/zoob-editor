package net.fhtagn.zoobeditor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashSet;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.accounts.Account;
import com.google.android.accounts.AccountManager;
import com.google.android.accounts.AuthenticatorException;
import com.google.android.accounts.OperationCanceledException;

import net.fhtagn.zoobeditor.R;
import net.fhtagn.zoobeditor.accounts.AuthManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;

public class EditorApplication extends Application {
	static final String TAG = "EditorApplication";

	private SharedPreferences prefs;

	private AuthManager authManager;
	
	@Override
	public void onCreate () {
		super.onCreate();
		prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		authManager = AuthManager.getAuthManager(getApplicationContext());
		
		if (Common.hasInternet(getApplicationContext())) {
			syncDownloadedSeries();
			syncMySeries();
		}
		
		//FIXME: check if zoob is installed and display an error if not
	}
	
	public AuthManager getAuthManager () {
		return authManager;
	}
	
	/**
	 * This background task will synchronize the "my series list" with the list of series we have authored as stored on the server
	 */
	public void syncMySeries () {
		final String account = authManager.getAuthorIdentification();
		if (account == null || !Common.hasInternet(getApplicationContext()))
			return;
		
		(new Thread() {
			@Override
			public void run () {
				Log.i(TAG, "Launching background syncMySeries() for account : " + account);
				HttpClient httpClient = new DefaultHttpClient();
				String result = Common.urlQuery(httpClient, EditorConstants.getByAuthorListUrl(account));
				if (result != null) {
					try {
	          insertAsMineFromJSON(httpClient, new JSONArray(result), account);
						Log.i(TAG, "syncMySeries done");
          } catch (JSONException e) {
	          e.printStackTrace();
          }
				} else 
					Log.e(TAG, "Error during background syncMySeries()");
			}
		}).start();
	}
	
	/**
	 * This background task synchronize the downloaded series list (mark updatabale series)
	 */
	public void syncDownloadedSeries () {
		(new Thread() {
			@Override
			public void run () {
				HttpClient httpClient = new DefaultHttpClient();
				Cursor cur = getContentResolver().query(Series.CONTENT_URI, new String[]{Series.ID, Series.COMMUNITY_ID, Series.LAST_MODIFICATION}, Series.IS_MINE+"=0 AND "+Series.COMMUNITY_ID+" NOT NULL", null, null);
				if (cur.moveToFirst()) {
					do {
						long communityID = cur.getLong(cur.getColumnIndex(Series.COMMUNITY_ID));
						long serieID = cur.getLong(cur.getColumnIndex(Series.ID));
						Log.i(TAG, "checking for update for serie with communityID="+communityID);
						String result = Common.urlQuery(httpClient, EditorConstants.getSummaryUrl(communityID));
						if (result != null) {
							try {
								JSONObject summary = new JSONObject(result);
								Date updated = Common.dateFromDB(summary.getJSONObject("meta").getString("updated"));
								Date lastLocalModif = Common.dateFromDB(cur.getString(cur.getColumnIndex(Series.LAST_MODIFICATION)));
								if (lastLocalModif.before(updated)) {
									ContentValues values = new ContentValues();
									values.put(Series.UPDATE_AVAILABLE, true);
									getContentResolver().update(ContentUris.withAppendedId(Series.CONTENT_URI, serieID), values, null, null);
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					} while(cur.moveToNext());
					Log.i(TAG, "syncDownloadedSeries done");
				}
				cur.close();
			}
		}).start();
	}
	
	//Will insert in the local database all the series in arr that aren't yet in the DB
	private void insertAsMineFromJSON (HttpClient httpClient, JSONArray arr, String account) {
		try {
			int len = arr.length();
			HashSet<Long> remoteIDs = new HashSet<Long>();
			//First step, insert series authored by the user but that aren't in the local DB
			for (int i=0; i<len; i++) {
				JSONObject serie = arr.getJSONObject(i);
				JSONObject meta = serie.getJSONObject("meta");
				String author = meta.getString("author");
				if (!author.equals(account))
					continue; //wrong author
				long communityID = meta.getLong("id");
				remoteIDs.add(communityID);
				
				Cursor cur = getContentResolver().query(Series.CONTENT_URI, new String[]{Series.ID}, Series.COMMUNITY_ID+"="+communityID, null, null);
				if (cur.getCount() > 0) {
					Log.i("ServerSync", "serie with community id " + communityID + " already in local DB");
					cur.close();
					continue; //already in db, don't update because we might overwrite local uncommited changes
				}
				cur.close();
				Log.i("ServerSync", "new serie with community id : " + communityID);
				//prepare for insert
				//have to fetch the whole json using the details view to get levels array
				String result = Common.urlQuery(httpClient, EditorConstants.getDetailsUrl(communityID));
				if (result == null) {
					Log.e("ServerSync", "Error fetching details for community id : " + communityID);
					continue;
				}
				ContentValues values = new ContentValues();
				values.put(Series.JSON, new JSONObject(result).toString());
				values.put(Series.IS_MINE, true);
				values.put(Series.COMMUNITY_ID, communityID);
				getContentResolver().insert(Series.CONTENT_URI, values);
			}
			
			//Second step, for series in the local DB but that weren't found on the server, remove the uploaded status (if set)
			Cursor cur = getContentResolver().query(Series.CONTENT_URI, new String[]{Series.ID, Series.COMMUNITY_ID}, Series.IS_MINE+"=1", null, null);
			if (cur.moveToFirst()) {
				do {
					long id = cur.getLong(cur.getColumnIndex(Series.COMMUNITY_ID));
					if (!remoteIDs.contains(id)) {
						long dbId = cur.getLong(cur.getColumnIndex(Series.ID));
						Log.i("ServerSync", "serie " + dbId + " not found on remote server, removing uploaded status");
						ContentValues values = new ContentValues();
						values.putNull(Series.UPLOAD_DATE);
						values.putNull(Series.COMMUNITY_ID);
						getContentResolver().update(ContentUris.withAppendedId(Series.CONTENT_URI, dbId), values, null, null);
					}
				} while (cur.moveToNext());
			}
			cur.close();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
