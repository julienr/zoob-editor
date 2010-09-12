package net.fhtagn.zoobeditor.browser;

import org.apache.http.impl.client.DefaultHttpClient;

import net.fhtagn.zoobeditor.EditorApplication;
import net.fhtagn.zoobeditor.EditorConstants;
import net.fhtagn.zoobeditor.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import net.fhtagn.zoobeditor.accounts.AuthManager;

//Base classe for activities that involve a blocking server request with authentification
public abstract class ServerRequestActivity extends Activity implements AuthManager.OnAuthenticatedCallback {
	private static final String TAG = "ServerRequestActivity";
	
	static final int DIALOG_PROGRESS = 1;
	static final int DIALOG_ERROR = 2;
	static final int DIALOG_SUCCESS = 3;
	
	private ProgressDialog progressDialog = null;
	private DefaultHttpClient httpClient = new DefaultHttpClient();
	private EditorApplication app;
	
	//This message will be shown in the error dialog
	private String errorDialogMsg = "";
	
	private int successMessage = -1;
	
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		showDialog(DIALOG_PROGRESS);
		app = (EditorApplication)getApplication();
		app.getAuthManager().authenticate(this, httpClient, this);
	}
	
	//successMessage is the message that will be displayed if the request succeed
	public void setSuccessMessage (int resId) {
		successMessage = resId;
	}
	
	protected void errorDialog (String errorMsg) {
		Log.e(TAG, "errorDialog : " + errorMsg);
		errorDialogMsg = errorMsg;
		dismissDialogSafely(DIALOG_PROGRESS);
		showDialog(DIALOG_ERROR);
	}
	
	@Override
	protected void onPrepareDialog (int id, Dialog dialog) {
		switch (id) {
			case DIALOG_ERROR: {
				AlertDialog alertDialog = (AlertDialog)dialog;
				if (errorDialogMsg != null) {
					alertDialog.setMessage(getResources().getString(R.string.upload_error)+errorDialogMsg);
					errorDialogMsg = null;
				}
			}
		}
	}
	
	@Override
	protected Dialog onCreateDialog (int id) {
		switch (id) {
			case DIALOG_PROGRESS:
				progressDialog = new ProgressDialog(this);
        progressDialog.setIcon(android.R.drawable.ic_dialog_info);
        progressDialog.setTitle(getString(R.string.progress_title));
        progressDialog.setIndeterminate(true);
        return progressDialog;
			case DIALOG_ERROR: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("")
							 .setCancelable(true)
							 .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
								 @Override
								 public void onClick(DialogInterface dialog, int id) {
									 dialog.cancel();
									 setResult(EditorConstants.RESULT_ERROR);
									 ServerRequestActivity.this.finish();
								 }
							 	});
				return builder.create();
			}
			case DIALOG_SUCCESS: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				if (successMessage != -1)
					builder.setMessage(successMessage);
				builder.setCancelable(true)
							 .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
								 @Override
								 public void onClick(DialogInterface dialog, int id) {
									 setResult(RESULT_OK);
									 ServerRequestActivity.this.finish();
									 dialog.cancel();
								 }
							 	});
				return builder.create();
			}
      default:
      	return null;
		}
	}
	
	//Should perform the request (the httpClient having being authenticated)
	//returns false if the request failed 
	protected abstract boolean doRequest (DefaultHttpClient httpClient);
	
	public void dismissDialogSafely(final int id) {
    try {
      dismissDialog(id);
    } catch (IllegalArgumentException e) {
      // This will be thrown if this dialog was not shown before.
    }
	}
	
	@Override
  public void authenticated(DefaultHttpClient httpClient) {
		dismissDialog(DIALOG_PROGRESS);
		if (doRequest(httpClient)) {
			showDialog(DIALOG_SUCCESS);
		} //Otherwise, error dialog handled by doSerieUpload
  }

	@Override
  public void authenticationCanceled(DefaultHttpClient httpClient) {
	  dismissDialog(DIALOG_PROGRESS);
	  finish();  
  }

	@Override
  public void authenticationError(DefaultHttpClient httpClient, String error) {
		dismissDialog(DIALOG_PROGRESS);
		errorDialog(error); 
  }

}
