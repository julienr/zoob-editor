package net.fhtagn.zoobeditor.browser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.android.accounts.Account;
import com.google.android.apps.mytracks.io.AccountChooser;
import com.google.android.apps.mytracks.io.AuthManager;
import com.google.android.apps.mytracks.io.AuthManagerFactory;

import net.fhtagn.zoobeditor.EditorConstants;
import net.fhtagn.zoobeditor.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class UploadActivity extends Activity {
	static final String TAG = "UploadActivity";
	
	static final int DIALOG_PROGRESS = 1;
	static final int DIALOG_LOGIN_ERROR = 2;
	static final int DIALOG_SUCCESS = 3;
	
	private AuthManager auth = null;
	private AccountChooser accountChooser = new AccountChooser();
	
	private ProgressDialog progressDialog = null;
	
	private static UploadActivity instance = null;
	
	private DefaultHttpClient httpClient = new DefaultHttpClient();
	
	private String toUploadContent = null;
	
	private Account account = null;
	
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;
		
		Intent i = getIntent();
		if (i == null) {
			Log.e(TAG, "null intent");
			setResult(EditorConstants.RESULT_ERROR);
			finish();
		}
		
		toUploadContent = i.getExtras().getString("data");
		showDialog(DIALOG_PROGRESS);
		authenticate(new Intent(), EditorConstants.SEND_TO_ZOOB_WEB);
		
		/*Button loginButton = new Button(this);
		loginButton.setText("Login");
		loginButton.setOnClickListener(new OnClickListener() {
			@Override
      public void onClick(View arg0) {
				Intent i = new Intent(getApplicationContext(), Browser.class);
				startActivityForResult(i, REQUEST_GOOGLE_LOGIN); 
      }
		});
		setContentView(loginButton);*/
	}

	public static UploadActivity getInstance () {
		return instance;
	}
	
	public AccountChooser getAccountChooser () {
		return accountChooser;
	}
	
	public void sendToWeb (String content) {
		toUploadContent = content;
		showDialog(DIALOG_PROGRESS);
		authenticate(new Intent(), EditorConstants.SEND_TO_ZOOB_WEB);
	}
	
	public void authenticate (final Intent results, final int requestCode) {
		final String service = "ah";
		if (auth == null)
			auth = AuthManagerFactory.getAuthManager(this, requestCode, null, true, service);
		
		if (AuthManagerFactory.useModernAuthManager()) {
			runOnUiThread(new Runnable() {
				@Override
        public void run() {
					accountChooser.chooseAccount(UploadActivity.this, new AccountChooser.AccountHandler() {
						@Override
						public void handleAccountSelected(Account account) {
							UploadActivity.this.account = account;
							if (account == null) {
								dismissDialogSafely(DIALOG_PROGRESS);
								return;
							}
							doLogin(results, requestCode, service, account);
						}
					});
        }
			});
		} else {
			doLogin(results, requestCode, service, null);
		}
	}
	
	@Override
	protected Dialog onCreateDialog (int id) {
		switch (id) {
			case DIALOG_PROGRESS:
				progressDialog = new ProgressDialog(this);
        progressDialog.setIcon(android.R.drawable.ic_dialog_info);
        progressDialog.setTitle(getString(R.string.progress_title));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage("");
        progressDialog.setMax(100);
        progressDialog.setProgress(10);
        return progressDialog;
			case DIALOG_LOGIN_ERROR: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(R.string.login_error)
							 .setCancelable(true)
							 .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								 @Override
								 public void onClick(DialogInterface dialog, int id) {
									 dialog.cancel();
									 setResult(EditorConstants.RESULT_ERROR);
									 UploadActivity.this.finish();
								 }
							 	});
				return builder.create();
			}
			case DIALOG_SUCCESS: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(R.string.upload_success)
							 .setCancelable(true)
							 .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								 @Override
								 public void onClick(DialogInterface dialog, int id) {
									 setResult(RESULT_OK);
									 UploadActivity.this.finish();
									 dialog.cancel();
								 }
							 	});
				return builder.create();
			}
      default:
      	return null;
		}
	}
	
	//Try to exchange the auth token obtained using google's authentication
	//against a cookie to access the zoobweb app
	//Returns true on success, false on failure
	protected boolean getCookieFromAuthToken () {
		if (account == null)
			return false;
		
		try {
			if (EditorConstants.isProd()) {
				final String token = auth.getAuthToken();
				
				final String continueURL = EditorConstants.getServerUrl();
				httpClient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);		
				HttpGet httpGet = new HttpGet(EditorConstants.getServerUrl()+"/_ah/login?auth="
						+ token + "&continue="+continueURL);
				Log.i(TAG, "GET : " + httpGet.getURI().toString());
				
				HttpResponse response;
		    response = httpClient.execute(httpGet);
				if (response.getStatusLine().getStatusCode() != 302) {
					//Response should be a redirect
					Log.e(TAG, "Response not a redirect. Status code : " 
										+ response.getStatusLine().getStatusCode() 
										+ ", error message : " 
										+ response.getStatusLine().getReasonPhrase());
					return false;
				}
				
				for (Cookie cookie: httpClient.getCookieStore().getCookies()) {
					if (cookie.getName().equals("ACSID")) {
						//Good, we found our cookie
						return true;
					}
				}
			} else {
				httpClient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);		
				HttpGet httpGet = new HttpGet(EditorConstants.getServerUrl()+"/_ah/login?email="
						+account.name + "&action=Login&continue="+EditorConstants.getServerUrl());
				HttpResponse response;
		    response = httpClient.execute(httpGet);
				if (response.getStatusLine().getStatusCode() != 302) {
					//Response should be a redirect
					Log.e(TAG, "Response not a redirect. Status code : " 
										+ response.getStatusLine().getStatusCode() 
										+ ", error message : " 
										+ response.getStatusLine().getReasonPhrase());
					return false;
				}
				
				for (Cookie cookie: httpClient.getCookieStore().getCookies()) {
					if (cookie.getName().equals("dev_appserver_login")) {
						//Good, we found our cookie
						return true;
					}
				}
			}
    } catch (ClientProtocolException e) {
	    e.printStackTrace();
    } catch (IOException e) {
	    e.printStackTrace();
    }
    return false;
	}
	
	protected boolean doSerieUpload () {
		try {
			HttpPost httpPost = new HttpPost(EditorConstants.getCreateUrl());
	    httpPost.setEntity(new StringEntity(toUploadContent));
	    httpClient.execute(httpPost);
	    return true;
    } catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
    } catch (ClientProtocolException e) {
	    e.printStackTrace();
    } catch (IOException e) {
	    e.printStackTrace();
    }
    return false;
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, final Intent results) {
		switch (requestCode) {
			case EditorConstants.GET_LOGIN: {
				if (resultCode == RESULT_OK && auth != null) {
					if (!auth.authResult(resultCode, results)) {
						dismissDialogSafely(DIALOG_PROGRESS);
					} 
				} else {
					dismissDialogSafely(DIALOG_PROGRESS);
				}
				break;
			}
			case EditorConstants.SEND_TO_ZOOB_WEB: {
				if (resultCode == RESULT_OK && auth != null) {
					Log.i(EditorConstants.TAG, "Auth token : " + auth.getAuthToken());
					(new Thread() {
						public void run() {
							if (getCookieFromAuthToken()) {
								Log.i(TAG, "Got cookie for zoobweb");
								if (doSerieUpload()) {
									dismissDialogSafely(DIALOG_PROGRESS);
									showDialogSafely(DIALOG_SUCCESS);
								} else { 
									Log.e(TAG, "Serie upload failed");
									dismissDialogSafely(DIALOG_PROGRESS);
									showDialogSafely(DIALOG_LOGIN_ERROR);
								}
							} else {
								Log.e(TAG, "Couldn't get cookie");
								dismissDialogSafely(DIALOG_PROGRESS);
								showDialogSafely(DIALOG_LOGIN_ERROR);
							}
						}
					}).start();
				} else {
					dismissDialogSafely(DIALOG_PROGRESS);
					auth = null;
					showDialogSafely(DIALOG_LOGIN_ERROR);
				}
				break;
			}
			default:
				Log.w(EditorConstants.TAG, "Warning unhandled request code : " + requestCode);
		}
	}
	
	public void dismissDialogSafely(final int id) {
		runOnUiThread(new Runnable() {
			@Override
			public void run () {
        try {
          dismissDialog(id);
        } catch (IllegalArgumentException e) {
          // This will be thrown if this dialog was not shown before.
        }
			}
		});
	}
	
	public void showDialogSafely (final int id) {
		runOnUiThread(new Runnable() {
			@Override
			public void run () {
				showDialog(id);
			}
		});
	}
	
	private void doLogin (final Intent results, final int requestCode, final String service, final Account account) {
		auth.doLogin(new Runnable() {
			@Override
      public void run() {
				onActivityResult(requestCode, RESULT_OK, results);
      }
		}, account);
	}
	
}
