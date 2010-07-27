package net.fhtagn.zoobeditor.browser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.fhtagn.zoobeditor.Common;
import net.fhtagn.zoobeditor.EditorConstants;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

public class OnlineSeriesActivity extends Activity {
	static final String LEVEL_LIST_GET_URL = "http://zoobweb.appspot.com/level";
	
	private JSONArray series = null;
	
	private ProgressBar progress;
	
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		progress = new ProgressBar(this);
		setContentView(progress);
		progress.setIndeterminate(true);
		
		fetchSeriesJSON();
	}
	
	private void onSeriesUpdated () {
		runOnUiThread(new Runnable() {
			@Override
			public void run () {
				if (series != null) {
					System.out.println("num series : " + series.length());
					for (int i=0; i<series.length(); i++) {
						try {
	            JSONObject serieObj = series.getJSONObject(i);
	            TextView textView = new TextView(OnlineSeriesActivity.this);
	            String message = "serie name : " + serieObj.get("name") + ", by : " + serieObj.getJSONObject("meta").get("author");
	            textView.setText(message);
	            setContentView(textView);
	            System.out.println(message);
            } catch (JSONException e) {
	            e.printStackTrace();
            }
					}
				} else {
					TextView textView = new TextView(OnlineSeriesActivity.this);
					textView.setText("Error retrieving series");
					setContentView(textView);
					System.out.println("Error retrieving series");
				}
			}
		});
	}
	
	private void fetchSeriesJSON () {
		(new Thread() {
			@Override
			public void run () {
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(LEVEL_LIST_GET_URL);
				HttpResponse response;
				
				try {
					response = httpClient.execute(httpGet);
					if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						HttpEntity entity = response.getEntity();
						if (entity == null) {
							Log.e(EditorConstants.TAG, "Entity = null");
							onSeriesUpdated();
							return;
						}
						
						InputStream instream = entity.getContent();
						String result = Common.convertStreamToString(instream);
						series = new JSONArray(result);
					} else {
						Log.e(EditorConstants.TAG, "Error : status code : " + response.getStatusLine().getStatusCode());
						series = null;
						//TODO: error
					}
				} catch (Exception e) {
					series = null;
					e.printStackTrace();
				}
				onSeriesUpdated();
			}
		}).start();
	}
}
