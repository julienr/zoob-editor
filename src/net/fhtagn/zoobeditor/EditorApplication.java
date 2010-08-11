package net.fhtagn.zoobeditor;

import java.io.IOException;
import java.io.InputStream;
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

import net.fhtagn.zoobeditor.R;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
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

public class EditorApplication extends Application {
	static final String TAG = "EditorApplication";

	private SharedPreferences prefs;
	private AccountManager accountManager;
	
	@Override
	public void onCreate () {
		super.onCreate();
		prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		accountManager = AccountManager.get(getApplicationContext());
		
		if (Common.hasInternet(getApplicationContext())) {
			syncDownloadedSeries();
			syncMySeries();
		}
	}
	
	/**
	 * This background task will synchronize the "my series list" with the list of series we have authored as stored on the server
	 */
	public void syncMySeries () {
		final Account currentAccount = findAccount();
		if (currentAccount == null || !Common.hasInternet(getApplicationContext()))
			return;
		
		(new Thread() {
			@Override
			public void run () {
				Log.i(TAG, "Launching background syncMySeries() for account : " + EditorConstants.getAuthorIdentification(currentAccount));
				HttpClient httpClient = new DefaultHttpClient();
				String result = Common.urlQuery(httpClient, EditorConstants.getByAuthorListUrl(currentAccount));
				if (result != null) {
					try {
	          insertAsMineFromJSON(httpClient, new JSONArray(result), currentAccount);
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
	 * This background task synchronize the downloaded series list (download updates)
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
									getContentResolver().insert(ContentUris.withAppendedId(Series.CONTENT_URI, serieID), values);
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
	private void insertAsMineFromJSON (HttpClient httpClient, JSONArray arr, Account myAccount) {
		try {
			int len = arr.length();
			HashSet<Long> remoteIDs = new HashSet<Long>();
			//First step, insert series authored by the user but that aren't in the local DB
			for (int i=0; i<len; i++) {
				JSONObject serie = arr.getJSONObject(i);
				JSONObject meta = serie.getJSONObject("meta");
				String author = meta.getString("author");
				if (!author.equals(EditorConstants.getAuthorIdentification(myAccount)))
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
	
	//Retrieve account from preferences
	private Account findAccount () {
		String accountName = prefs.getString(getResources().getString(R.string.pref_key_account), "");
		if (accountName == "")
			return null;
		
		final Account[] accounts = AccountManager.get(this).getAccountsByType(EditorConstants.ACCOUNT_TYPE);
		for (Account a: accounts) {
			if (a.name.equals(accountName)) {
				return a;
			}
		}
		return null;
	}
	
	public interface OnAuthenticatedCallback {
		public void authenticated (DefaultHttpClient httpClient);
		public void authenticationError (DefaultHttpClient httpClient, String error);
		public void authenticationCanceled (DefaultHttpClient httpClient);
	}
	
	
	private void authError (final Activity activity, final DefaultHttpClient httpClient, final OnAuthenticatedCallback callback, final String msg) {
		activity.runOnUiThread(new Runnable() {
			public void run () {
				callback.authenticationError(httpClient, msg);
			}
		});
	}
	
	private void authSuccess (final Activity activity, final DefaultHttpClient httpClient, final OnAuthenticatedCallback callback) {
		activity.runOnUiThread(new Runnable() {
			public void run () {
				callback.authenticated(httpClient);
			}
		});
	}
	
	private void authCancel (final Activity activity, final DefaultHttpClient httpClient, final OnAuthenticatedCallback callback) {
		activity.runOnUiThread(new Runnable() {
			public void run () {
				callback.authenticationCanceled(httpClient);
			}
		});
	}
	
	private void showNoAccountDialog (final Activity activity, final DefaultHttpClient httpClient, final OnAuthenticatedCallback callback) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(R.string.dlg_no_account_title)
					 .setMessage(R.string.dlg_no_account_msg)
					 .setCancelable(false)
					 .setPositiveButton(R.string.goto_prefs, new DialogInterface.OnClickListener() {
						 @Override
						 public void onClick (DialogInterface dialog, int id) {
							 dialog.dismiss();
							 authCancel(activity, httpClient, callback);
							 Intent i = new Intent(getApplicationContext(), Preferences.class);
							 activity.startActivity(i);
						 }
					 })
					 .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
								authCancel(activity, httpClient, callback);
							}
					});
		AlertDialog dialog = builder.create();
		dialog.setOwnerActivity(activity);
		dialog.show();
	}
	
	//Fetch the auth cookie for the given httpClient by using the user-defined account stored in preferences
	//Will redirect the user to the preferences panel if no account has been defined
	//This is a non-blocking method, the given callback is called when authentication is done (or on error)
	public void authenticate (final Activity activity, final DefaultHttpClient httpClient, final OnAuthenticatedCallback callback) {
		final Account account = findAccount();
		if (account == null) {
			Log.i(TAG, "No accounts found");
			showNoAccountDialog(activity, httpClient, callback);
			return;
		}
		
		(new Thread(){
			public void run () {
				try {
					if (EditorConstants.isProd()) {
						String authToken = accountManager.blockingGetAuthToken(account, EditorConstants.AUTH_TOKEN_TYPE, true);
						if (authToken == null) {
							//Indicate an error logging in,
							authError(activity, httpClient, callback, "Received null auth token");
							return;
						}
						//invalidate and get new, otherwise we might have an expired cached token, leading to authentication failure
						accountManager.invalidateAuthToken(account.type, authToken);
						authToken = accountManager.blockingGetAuthToken(account, EditorConstants.AUTH_TOKEN_TYPE, true);
						
						final String continueURL = EditorConstants.getServerUrl();
						httpClient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);		
						HttpGet httpGet = new HttpGet(EditorConstants.getLoginUrl()+"/_ah/login?auth="
								+ authToken + "&continue="+continueURL);
						Log.i(TAG, "GET : " + httpGet.getURI().toString());
						
						HttpResponse response;
				    response = httpClient.execute(httpGet);
						if (response.getStatusLine().getStatusCode() != 302) {
							//Response should be a redirect
							authError(activity, httpClient, callback, "Response not a redirect. Status code : " 
																												+ response.getStatusLine().getStatusCode() 
																												+ ", error message : " 
																												+ response.getStatusLine().getReasonPhrase());
							return;
						}
						
						for (Cookie cookie: httpClient.getCookieStore().getCookies()) {
							if (cookie.getName().equals("ACSID")) {
								//Good, we found our cookie
								authSuccess(activity, httpClient, callback);
								return;
							}
						}
					} else {
						httpClient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);		
						HttpGet httpGet = new HttpGet(EditorConstants.getLoginUrl()+"/_ah/login?email="
								+account.name + "&action=Login&continue="+EditorConstants.getServerUrl());
						HttpResponse response;
				    response = httpClient.execute(httpGet);
						if (response.getStatusLine().getStatusCode() != 302) {
							//Response should be a redirect
							authError(activity, httpClient, callback, "Response not a redirect. Status code : " 
																												+ response.getStatusLine().getStatusCode() 
																												+ ", error message : " 
																												+ response.getStatusLine().getReasonPhrase());
							return;
						}
						
						for (Cookie cookie: httpClient.getCookieStore().getCookies()) {
							if (cookie.getName().equals("dev_appserver_login")) {
								//Good, we found our cookie
								authSuccess(activity, httpClient, callback);
							}
						}
					}
		    } catch (ClientProtocolException e) {
			    e.printStackTrace();
		    } catch (IOException e) {
			    e.printStackTrace();
		    } catch (OperationCanceledException e) {
			    e.printStackTrace();
		    } catch (AuthenticatorException e) {
			    e.printStackTrace();
		    }
			}
		}).start();
	}
}
