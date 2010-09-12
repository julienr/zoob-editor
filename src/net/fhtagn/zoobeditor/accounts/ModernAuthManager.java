package net.fhtagn.zoobeditor.accounts;

import java.io.IOException;

import net.fhtagn.zoobeditor.EditorConstants;
import net.fhtagn.zoobeditor.R;
import net.fhtagn.zoobeditor.accounts.AuthManager.OnAuthenticatedCallback;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.android.accounts.Account;
import com.google.android.accounts.AccountManager;
import com.google.android.accounts.AuthenticatorException;
import com.google.android.accounts.OperationCanceledException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class ModernAuthManager extends AuthManager {
	static final String TAG = "ModernAuthManager";
	
	private final AccountManager accountManager;
	
	public ModernAuthManager (Context ctx, String acc) {
		Account account = findAccount(ctx, acc);
		if (account == null)
			setAccount(null);
		else
			setAccount(new AuthAccount(account));
		accountManager = AccountManager.get(ctx);
	}
	
	private Account findAccount (Context ctx, String accountName) {
		final Account[] accounts = AccountManager.get(ctx).getAccountsByType(EditorConstants.ACCOUNT_TYPE);
		if (accountName == "") {
			//This will happen at first app launch. If we have only one account for the phone, use it by default without asking
			//(user will be asked later anyway)
			if (accounts.length == 1) {
				final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
				SharedPreferences.Editor edit = prefs.edit();
				edit.putString(ctx.getResources().getString(R.string.pref_key_account), accounts[0].name);
				edit.commit();
				Log.i(TAG, "Found unique Google account : " + accounts[0].name);
				return accounts[0];
			} else {
				//User has multiple account, an dialog asking him to choose which account to use will be shown on first login
				return null;
			}
		}
		
		for (Account a: accounts) {
			if (a.name.equals(accountName)) {
				return a;
			}
		}
		return null;
	}

	@Override
  public void authenticate(final Activity activity, final DefaultHttpClient httpClient, final OnAuthenticatedCallback callback) {
		final AuthAccount authAcc = getAccount();
		if (authAcc == null) {
			Log.i(TAG, "No accounts found");
			showNoAccountDialog(activity, httpClient, callback);
			return;
		}
		final Account account = authAcc.getAccount();
		
		(new Thread(){
			public void run () {
				try {
					if (EditorConstants.isProd()) {
						String authToken = accountManager.blockingGetAuthToken(account, EditorConstants.AUTH_TOKEN_TYPE, true);
						if (authToken == null) {
							//An access request notification is shown to the user here, so just show a dialog telling him to look at the notification area
							//FIXME: can we programatically trigger this access request when the user selects an account ?
							activity.runOnUiThread(new Runnable () {
								public void run () {
									showAccessRequestDialog(activity, httpClient, callback);
								}
							});
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
		    	authError(activity, httpClient, callback, e.getMessage());
			    e.printStackTrace();
		    } catch (IOException e) {
		    	authError(activity, httpClient, callback, e.getMessage());
			    e.printStackTrace();
		    } catch (OperationCanceledException e) {
		    	authError(activity, httpClient, callback, e.getMessage());
			    e.printStackTrace();
		    } catch (AuthenticatorException e) {
		    	authError(activity, httpClient, callback, e.getMessage());
			    e.printStackTrace();
		    }
			}
		}).start();
  }
	
	void showAccessRequestDialog (final Activity activity, final DefaultHttpClient httpClient, final OnAuthenticatedCallback callback) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(R.string.dlg_access_request_title)
					 .setMessage(R.string.dlg_access_request_msg)
					 .setCancelable(false)
					 .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						 @Override
						 public void onClick (DialogInterface dialog, int id) {
							 dialog.dismiss();
							 authCancel(activity, httpClient, callback);
						 }
					 });
		AlertDialog dialog = builder.create();
		dialog.setOwnerActivity(activity);
		dialog.show();
	}

}
