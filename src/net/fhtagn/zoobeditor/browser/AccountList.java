package net.fhtagn.zoobeditor.browser;

import java.io.IOException;

import com.google.android.accounts.Account;
import com.google.android.accounts.AccountManager;
import com.google.android.accounts.AccountManagerCallback;
import com.google.android.accounts.AccountManagerFuture;
import com.google.android.accounts.AuthenticatorException;
import com.google.android.accounts.OperationCanceledException;
import com.google.android.apps.mytracks.io.AccountChooser;
import com.google.android.apps.mytracks.io.AuthManager;
import com.google.android.apps.mytracks.io.AuthManagerFactory;
import com.google.android.apps.mytracks.io.AccountChooser.AccountHandler;
import com.google.android.googlelogin.GoogleLoginServiceHelper;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import net.fhtagn.zoobeditor.EditorConstants;
import net.fhtagn.zoobeditor.R;

public class AccountList extends Activity {
	static final int DIALOG_PROGRESS = 1;
	
	private AuthManager auth = null;
	private AccountChooser accountChooser = new AccountChooser();
	
	private ProgressDialog progressDialog = null;
	
	private static AccountList instance = null;
	
	public static AccountList getInstance () {
		return instance;
	}
	
	public AccountChooser getAccountChooser () {
		return accountChooser;
	}
	
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;
		
		/*AccountChooser chooser = new AccountChooser();
		chooser.chooseAccount(this, new AccountHandler() {
			@Override
      public void handleAccountSelected(Account account) {
				System.out.println("Selected account : " + account.name);
				text.setText("Selected : " + account.name);
				choosedAccount = account;
				getAuthToken();
      }
		});*/
		
		/*text = new TextView(this);
		setContentView(text);*/
		
		Button authButton = new Button(this);
		authButton.setText("authenticate");
		authButton.setOnClickListener(new OnClickListener() {
			@Override
      public void onClick(View arg0) {
				showDialog(DIALOG_PROGRESS);
				authenticate(new Intent(), EditorConstants.SEND_TO_ZOOB_WEB);
      }
		});
		setContentView(authButton);
	}
	
	private void authenticate (final Intent results, final int requestCode) {
		final String service = "ah";
		if (auth == null)
			auth = AuthManagerFactory.getAuthManager(this, requestCode, null, true, service);
		if (AuthManagerFactory.useModernAuthManager()) {
			runOnUiThread(new Runnable() {
				@Override
        public void run() {
					accountChooser.chooseAccount(AccountList.this, new AccountChooser.AccountHandler() {
						@Override
						public void handleAccountSelected(Account account) {
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
      default:
      	return null;
		}
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
					dismissDialogSafely(DIALOG_PROGRESS);
					Log.i(EditorConstants.TAG, "Auth token : " + auth.getAuthToken());
				} else {
					dismissDialogSafely(DIALOG_PROGRESS);
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
	
	private void doLogin (final Intent results, final int requestCode, final String service, final Account account) {
		auth.doLogin(new Runnable() {
			@Override
      public void run() {
				onActivityResult(requestCode, RESULT_OK, results);
      }
		}, account);
	}
		
	//This function might need to be called twice
	//1. On the first call, it will launch the intent to request user's approval of the account's use
	//2. On the second call, the actual auth token will be returned
	/*private void getAuthToken () {
		AccountManager accountManager = AccountManager.get(getApplicationContext());
		accountManager.getAuthToken(choosedAccount, "ah", false, new AccountManagerCallback<Bundle>() {
			@Override
      public void run(AccountManagerFuture<Bundle> result) {
				try {
          Bundle bundle = result.getResult();
          //An intent is returned if we should request user approval of his account usage
          Intent intent = (Intent)bundle.get(AccountManager.KEY_INTENT);
          if (intent != null) {
          	System.out.println("got intent");
          	startActivity(intent);
          } else {
          	String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
            System.out.println("obtained token : " + authToken);
            text.setText("obtained token : " + authToken);
          }
        } catch (OperationCanceledException e) {
          e.printStackTrace();
        } catch (AuthenticatorException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
        
      }
		}, null);
	}
	
	@Override
	protected void onResume () {
		super.onResume();
		if (choosedAccount != null)
			getAuthToken();
	}*/
}
