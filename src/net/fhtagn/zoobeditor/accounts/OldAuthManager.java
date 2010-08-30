package net.fhtagn.zoobeditor.accounts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.fhtagn.zoobeditor.EditorConstants;
import net.fhtagn.zoobeditor.accounts.AuthManager.AuthAccount;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.google.android.googlelogin.GoogleLoginServiceBlockingHelper;
import com.google.android.googlelogin.GoogleLoginServiceBlockingHelper.AuthenticationException;
import com.google.android.googlelogin.GoogleLoginServiceNotFoundException;

import android.app.Activity;
import android.content.Context;
import android.provider.ContactsContract.RawContacts.Entity;
import android.util.Log;

public class OldAuthManager extends AuthManager {
	static final String TAG = "OldAuthManager";
	
	private final Context context;
	
	public OldAuthManager(Context ctx, String account) {
		setAccount(new AuthAccount(account));
		context = ctx;
  }

	@Override
  public void authenticate(final Activity activity, final DefaultHttpClient httpClient, final OnAuthenticatedCallback callback) {
		final AuthAccount authAcc = getAccount();
		if (authAcc == null) {
			Log.i(TAG, "No accounts found");
			showNoAccountDialog(activity, httpClient, callback);
			return;
		}
		(new Thread() {
			public void run () {
			  try {
	        String authToken = GoogleLoginServiceBlockingHelper.getAuthToken(context, authAcc.getStrAccount(), EditorConstants.AUTH_TOKEN_TYPE);
	        //invalidate and get new, otherwise we might have an expired cached token, leading to authentication failure
	        GoogleLoginServiceBlockingHelper.invalidateAuthToken(context, authToken);
	        authToken = GoogleLoginServiceBlockingHelper.getAuthToken(context, authAcc.getStrAccount(), EditorConstants.AUTH_TOKEN_TYPE);
	        
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
        } catch (GoogleLoginServiceNotFoundException e) {
        	authError(activity, httpClient, callback, e.getMessage());
	        e.printStackTrace();
        } catch (AuthenticationException e) {
        	authError(activity, httpClient, callback, e.getMessage());
	        e.printStackTrace();
        } catch (ClientProtocolException e) {
        	authError(activity, httpClient, callback, e.getMessage());
	        e.printStackTrace();
        } catch (IOException e) {
        	authError(activity, httpClient, callback, e.getMessage());
	        e.printStackTrace();
        }
			}
		}).start();
  }
}
