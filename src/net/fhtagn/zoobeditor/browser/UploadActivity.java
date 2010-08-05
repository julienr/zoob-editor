package net.fhtagn.zoobeditor.browser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import net.fhtagn.zoobeditor.Common;
import net.fhtagn.zoobeditor.EditorApplication;
import net.fhtagn.zoobeditor.EditorConstants;
import net.fhtagn.zoobeditor.Series;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import net.fhtagn.zoobeditor.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class UploadActivity extends Activity implements EditorApplication.OnAuthenticatedCallback {
	static final String TAG = "UploadActivity";
	
	static final int DIALOG_PROGRESS = 1;
	static final int DIALOG_ERROR = 2;
	static final int DIALOG_SUCCESS = 3;
	
	private ProgressDialog progressDialog = null;
	
	private DefaultHttpClient httpClient = new DefaultHttpClient();
	
	private String toUploadContent = null;
	//community id if the upload is an update
	private long toUploadId = -1;
	
	private Uri serieUri;
	
	private EditorApplication app;
	
	//This message will be shown in the error dialog
	private String errorDialogMsg = ""; 
	
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent i = getIntent();
		if (i == null || !i.hasExtra("id")) {
			Log.e(TAG, "null intent");
			setResult(EditorConstants.RESULT_ERROR);
			finish();
		}
		
		serieUri = ContentUris.withAppendedId(Series.CONTENT_URI, i.getExtras().getLong("id"));
		
		Cursor cur = this.managedQuery(serieUri, new String[]{Series.JSON, Series.COMMUNITY_ID}, null, null, null);
		if (!cur.moveToFirst()) {
			Log.e(TAG, "Couldn't find serie : " + i.getExtras().getLong("id"));
			setResult(EditorConstants.RESULT_ERROR);
			finish();
		}
		
		toUploadContent = cur.getString(cur.getColumnIndex(Series.JSON));
		if (!cur.isNull(cur.getColumnIndex(Series.COMMUNITY_ID)))
			toUploadId = cur.getLong(cur.getColumnIndex(Series.COMMUNITY_ID));
		
		
		showDialog(DIALOG_PROGRESS);
		app = (EditorApplication)getApplication();
		app.authenticate(this, httpClient, this);
	}
	
	private void errorDialog (String errorMsg) {
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
									 UploadActivity.this.finish();
								 }
							 	});
				return builder.create();
			}
			case DIALOG_SUCCESS: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(R.string.upload_success)
							 .setCancelable(true)
							 .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
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
	
	protected boolean doSerieUpload () {
		try {
			HttpPost httpPost;
			if (toUploadId != -1) {
				Log.i(TAG, "Updating existing serie, communityID=" + toUploadId);
				httpPost = new HttpPost(EditorConstants.getPutUrl(toUploadId));
			} else {
				Log.i(TAG, "Creating new serie");
				httpPost = new HttpPost(EditorConstants.getPutUrl());
			}
			
	    httpPost.setEntity(new StringEntity(toUploadContent));
	    HttpResponse response = httpClient.execute(httpPost);
	    if (response.getStatusLine().getStatusCode() != 200) {
	    	errorDialog("Error uploading : " + response.getStatusLine().getReasonPhrase());
	    	return false;
	    }
	    
	    String json = Common.convertStreamToString(response.getEntity().getContent());
	    JSONObject serieObj = new JSONObject(json);
	    int communityID = serieObj.getJSONObject("meta").getInt("id");
	    ContentValues values = new ContentValues();
	    values.put(Series.COMMUNITY_ID, communityID);
	    getContentResolver().update(serieUri, values, null, null);
	    return true;
    } catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
    } catch (ClientProtocolException e) {
	    e.printStackTrace();
    } catch (IOException e) {
	    e.printStackTrace();
    } catch (JSONException e) {
	    e.printStackTrace();
    }
    return false;
	}
	
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
		if (doSerieUpload()) {
			showDialog(DIALOG_SUCCESS);
		} //Otherwise, error dialog handled by doSerieUpload
  }

	@Override
  public void authenticationError(DefaultHttpClient httpClient, String error) {
		dismissDialog(DIALOG_PROGRESS);
		errorDialog(error);
  }

	@Override
  public void authenticationCanceled(DefaultHttpClient httpClient) {
	  dismissDialog(DIALOG_PROGRESS);
	  finish();
  }
}
