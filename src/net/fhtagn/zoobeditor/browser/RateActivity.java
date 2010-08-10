package net.fhtagn.zoobeditor.browser;

import java.io.IOException;

import net.fhtagn.zoobeditor.EditorConstants;
import net.fhtagn.zoobeditor.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class RateActivity extends ServerRequestActivity {
	static final String TAG = "RateActivity";
	
	private long communityID = -1;
	private int rating = -1;

	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		if (!intent.hasExtra("community_id") || !intent.hasExtra("rating")) {
			Log.e(TAG, "no community_id or rating in intent");
			setResult(EditorConstants.RESULT_ERROR);
			finish();
		}
		communityID = intent.getExtras().getLong("community_id");
		rating = intent.getExtras().getInt("rating");
	}
	
	@Override
  protected boolean doRequest(DefaultHttpClient httpClient) {
	  try {
	  	if (communityID == -1 || rating == -1) {
	  		Log.e(TAG, "rate with communityID == -1 || rating == -1");
	  		setResult(EditorConstants.RESULT_ERROR);
	  		return false;
	  	}
	  	
	  	HttpGet httpGet = new HttpGet(EditorConstants.getRateUrl(communityID, rating));
	  	HttpResponse response = httpClient.execute(httpGet);
	  	if (response.getStatusLine().getStatusCode() != 200) {
	  		if (response.getStatusLine().getStatusCode() == 461)
	  			errorDialog(getResources().getString(R.string.err_already_rated));
	  		else
	  			errorDialog("Error deleting  : " + response.getStatusLine().getReasonPhrase());
	  		return false;
	  	}
	  	return true;
	  } catch (IOException e) {
	  	e.printStackTrace();
	  }
	  return false;
  }

}
