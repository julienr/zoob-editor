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

public class UploadActivity extends ServerRequestActivity {
	static final String TAG = "UploadActivity";
	
	private String toUploadContent = null;
	//community id if the upload is an update
	private long toUploadId = -1;
	
	private Uri serieUri;
	
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setSuccessMessage(R.string.upload_success);
		
		Intent i = getIntent();
		if (i == null || !i.hasExtra("id")) {
			Log.e(TAG, "no id in intent");
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
	}
	
	@Override
	protected boolean doRequest (DefaultHttpClient httpClient) {
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
	    values.put(Series.UPLOAD_DATE, Common.dateToDB(Common.getUTCTime()));
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
}
