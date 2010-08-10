package net.fhtagn.zoobeditor.browser;

import java.io.IOException;

import net.fhtagn.zoobeditor.EditorConstants;
import net.fhtagn.zoobeditor.R;
import net.fhtagn.zoobeditor.Series;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

//Delete a serie on the server and in the local DB.
public class DeleteActivity extends ServerRequestActivity {
	static final String TAG = "DeleteActivity";
	
	private long communityID = -1;
	private long serieID;
	
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setSuccessMessage(R.string.delete_success);
		
		Intent intent = getIntent();
		if (!intent.hasExtra("id")) {
			Log.e(TAG, "no id in intent");
			setResult(EditorConstants.RESULT_ERROR);
			finish();
		}
		
		serieID = intent.getExtras().getLong("id");
		Cursor cursor = managedQuery(ContentUris.withAppendedId(Series.CONTENT_URI, serieID), new String[]{Series.COMMUNITY_ID}, null, null, null);
		if (!cursor.moveToFirst()) {
			Log.e(TAG, "serie not found in database");
			setResult(EditorConstants.RESULT_ERROR);
			finish();
		}
		
		if (!cursor.isNull(cursor.getColumnIndex(Series.COMMUNITY_ID)))
			communityID = cursor.getLong(cursor.getColumnIndex(Series.COMMUNITY_ID)); 
	}
	
	@Override
  protected boolean doRequest(DefaultHttpClient httpClient) {
	  try {
	  	//If the serie has been uploaded, send a request to delete it to the server.
	  	//Otherwise, just proceed with local deletion
	  	if (communityID != -1) {
		  	HttpGet httpGet = new HttpGet(EditorConstants.getDeleteUrl(communityID));
		  	HttpResponse response = httpClient.execute(httpGet);
		  	if (response.getStatusLine().getStatusCode() != 200) {
		  		errorDialog("Error rating  : " + response.getStatusLine().getReasonPhrase());
		  		return false;
		  	}
	  	}
	  	
	  	//delete from local db
	  	Uri deleteUri = ContentUris.withAppendedId(Series.CONTENT_URI, serieID);
			getContentResolver().delete(deleteUri, null, null);
			
	  	return true;
	  } catch (IOException e) {
	  	e.printStackTrace();
	  }
	  return false;
  }
	
	
}
