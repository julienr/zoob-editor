package net.fhtagn.zoobeditor.browser;

import java.io.IOException;

import net.fhtagn.zoobeditor.EditorConstants;
import net.fhtagn.zoobeditor.Series;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

//Delete a serie on the server and in the local DB.
public class DeleteActivity extends ServerRequestActivity {
	static final String TAG = "DeleteActivity";
	
	private long communityID = -1;
	
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		if (!intent.hasExtra("community_id")) {
			Log.e(TAG, "no community_id in intent");
			setResult(EditorConstants.RESULT_ERROR);
			finish();
		}
		
		
		communityID = intent.getExtras().getLong("community_id");
	}
	
	@Override
  protected boolean doRequest(DefaultHttpClient httpClient) {
	  try {
	  	if (communityID == -1) {
	  		Log.e(TAG, "delete with communityID == -1");
	  		setResult(EditorConstants.RESULT_ERROR);
	  		return false;
	  	}
	  	
	  	HttpGet httpGet = new HttpGet(EditorConstants.getDeleteUrl(communityID));
	  	HttpResponse response = httpClient.execute(httpGet);
	  	if (response.getStatusLine().getStatusCode() != 200) {
	  		errorDialog("Error rating  : " + response.getStatusLine().getReasonPhrase());
	  		return false;
	  	}
	  	
	  	Uri deleteUri = ContentUris.withAppendedId(Series.CONTENT_URI, communityID);
			getContentResolver().delete(deleteUri, null, null);
			
	  	return true;
	  } catch (IOException e) {
	  	e.printStackTrace();
	  }
	  return false;
  }
	
	
}
