package net.fhtagn.zoobeditor.browser;

import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import net.fhtagn.zoobeditor.Common;
import net.fhtagn.zoobeditor.EditorConstants;
import net.fhtagn.zoobeditor.R;
import net.fhtagn.zoobeditor.browser.OnlineSeriesActivity.SeriesAdapter;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

/** 
 * Abstract base class for an activity that will fetch its content from an URL 
 */
public abstract class URLFetchActivity extends Activity {
	private ProgressBar progress;
	
	protected void toLoadingState () {
		runOnUiThread(new Runnable() {
			@Override
			public void run () {
				setContentView(R.layout.loading);
				progress = (ProgressBar)findViewById(R.id.progressbar);
				progress.setIndeterminate(true);
				fetchURL();
			}
		});
	}
	
	//Called when the content has been fetched (contained in result, null on error).
	//This is called on the UI thread automatically. It should set the content view for the 
	//loaded state
	protected abstract void onContentReady (String result);
	
	//Should return the URL from where to fetch the content
	protected abstract String getURL ();
	
	protected void toListState (final String result) {
		Log.e("TAG", "toListState");
		runOnUiThread(new Runnable() {
			@Override
			public void run () {
				progress = null;
				onContentReady(result);
			}
		});
	}
	
	private void fetchURL () {
		(new Thread() {
			@Override
			public void run () {
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(getURL());
				HttpResponse response;
				
				try {
					response = httpClient.execute(httpGet);
					if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						HttpEntity entity = response.getEntity();
						if (entity == null) {
							Log.e(EditorConstants.TAG, "Entity = null");
							toListState(null);
							return;
						}
						
						InputStream instream = entity.getContent();
						String result = Common.convertStreamToString(instream);
						toListState(result);
						return;
					} else {
						Log.e(EditorConstants.TAG, "Error : status code : " + response.getStatusLine().getStatusCode());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				toListState(null);
			}
		}).start();
	}
}
