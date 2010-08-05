package net.fhtagn.zoobeditor;

import java.io.IOException;

import net.fhtagn.zoobeditor.browser.UploadActivity;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class EditorApplication extends Application {
	static final String TAG = "EditorApplication";

	private SharedPreferences prefs;
	private AccountManager accountManager;
	private Account account = null;
	
	@Override
	public void onCreate () {
		super.onCreate();
		prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		accountManager = AccountManager.get(getApplicationContext());
		findAccount();
	}
	
	//Retrieve account from preferences
	private void findAccount () {
		String accountName = prefs.getString(getResources().getString(R.string.pref_key_account), "");
		if (accountName == "")
			return;
		
		final Account[] accounts = AccountManager.get(this).getAccountsByType(EditorConstants.ACCOUNT_TYPE);
		for (Account a: accounts) {
			if (a.name.equals(accountName)) {
				account = a;
				return;
			}
		}
	}
	
	public interface OnAuthenticatedCallback {
		public void authenticated (DefaultHttpClient httpClient);
		public void authenticationError (DefaultHttpClient httpClient, String error);
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
	
	//Fetch the auth cookie for the given httpClient by using the user-defined account stored in preferences
	//Will redirect the user to the preferences panel if no account has been defined
	//This is a non-blocking method, the given callback is called when authentication is done (or on error)
	public void authenticate (final Activity activity, final DefaultHttpClient httpClient, final OnAuthenticatedCallback callback) {
		(new Thread(){
			public void run () {
				if (account == null) {
					Log.i(TAG, "account = null");
					return;
					//TODO: redirect user to preferences
				}
				
				try {
					if (EditorConstants.isProd()) {
						String authToken = accountManager.blockingGetAuthToken(account, "ah", true);
						if (authToken == null) {
							//Indicate an error logging in,
							authError(activity, httpClient, callback, "Received null auth token");
							return;
						}
						//invalidate and get new, otherwise we might have an expired cached token, leading to authentication failure
						accountManager.invalidateAuthToken(account.type, authToken);
						authToken = accountManager.blockingGetAuthToken(account, "ah", true);
						
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
