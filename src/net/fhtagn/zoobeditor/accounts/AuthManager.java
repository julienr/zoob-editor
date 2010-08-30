package net.fhtagn.zoobeditor.accounts;

import net.fhtagn.zoobeditor.Preferences;
import net.fhtagn.zoobeditor.R;

import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.google.android.accounts.Account;

public abstract class AuthManager {
	//FACTORY methods
	public static boolean hasAccountManager() {
    return Integer.parseInt(Build.VERSION.SDK) >= 5;
  }
	
	public static AuthManager getAuthManager (Context ctx) {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		if (hasAccountManager())
			return new ModernAuthManager(ctx, prefs.getString(ctx.getResources().getString(R.string.pref_key_account), ""));
		else
			return new OldAuthManager(ctx, prefs.getString(ctx.getResources().getString(R.string.pref_key_account), ""));
	}
	
	public static class AuthAccount {
		private String strAccount = null;
		private Account account = null;
		
		//<2.0
		public AuthAccount (String account) {
			assert(!hasAccountManager());
			strAccount = account;
		}
		
		//>=2.0
		public AuthAccount (Account account) {
			assert(hasAccountManager());
			this.account = account;
		}
		
		String getStrAccount () {
			return strAccount;
		}
		
		Account getAccount () {
			return account;
		}
	}
	
	public static interface OnAuthenticatedCallback {
		public void authenticated (DefaultHttpClient httpClient);
		public void authenticationError (DefaultHttpClient httpClient, String error);
		public void authenticationCanceled (DefaultHttpClient httpClient);
	}
	
	//Instance
	private AuthAccount account;
	
	//MUST be called just after construction of child class
	protected void setAccount (AuthAccount account) {
		this.account = account;
	}
	
	protected AuthAccount getAccount () {
		return account;
	}
	
	public String getAuthorIdentification () {
		if (account == null)
			return null;
		
		if (hasAccountManager())
			return account.getAccount().name;
		else
			return account.getStrAccount();
	}
	
	void authError (final Activity activity, final DefaultHttpClient httpClient, final OnAuthenticatedCallback callback, final String msg) {
		activity.runOnUiThread(new Runnable() {
			public void run () {
				callback.authenticationError(httpClient, msg);
			}
		});
	}
	
	void authSuccess (final Activity activity, final DefaultHttpClient httpClient, final OnAuthenticatedCallback callback) {
		activity.runOnUiThread(new Runnable() {
			public void run () {
				callback.authenticated(httpClient);
			}
		});
	}
	
	void authCancel (final Activity activity, final DefaultHttpClient httpClient, final OnAuthenticatedCallback callback) {
		activity.runOnUiThread(new Runnable() {
			public void run () {
				callback.authenticationCanceled(httpClient);
			}
		});
	}
	
	//Fetch the auth cookie for the given httpClient by using the user-defined account stored in preferences
	//Will redirect the user to the preferences panel if no account has been defined
	//This is a non-blocking method, the given callback is called when authentication is done (or on error)
	public abstract void authenticate (final Activity activity, final DefaultHttpClient httpClient, final OnAuthenticatedCallback callback);
	
	void showNoAccountDialog (final Activity activity, final DefaultHttpClient httpClient, final OnAuthenticatedCallback callback) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(R.string.dlg_no_account_title)
					 .setMessage(R.string.dlg_no_account_msg)
					 .setCancelable(false)
					 .setPositiveButton(R.string.goto_prefs, new DialogInterface.OnClickListener() {
						 @Override
						 public void onClick (DialogInterface dialog, int id) {
							 dialog.dismiss();
							 authCancel(activity, httpClient, callback);
							 Intent i = new Intent(activity.getApplicationContext(), Preferences.class);
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
}
