package net.fhtagn.zoobeditor.browser;

import java.io.InputStream;

import net.fhtagn.zoobeditor.Common;
import net.fhtagn.zoobeditor.EditorConstants;
import net.fhtagn.zoobeditor.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class OnlineSeriesActivity extends Activity {
	
	private JSONArray series = null;
	
	private ProgressBar progress;
	
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		toLoadingState();
	}
	
	private void toLoadingState () {
		runOnUiThread(new Runnable() {
			@Override
			public void run () {
				setContentView(R.layout.loading);
				progress = (ProgressBar)findViewById(R.id.progressbar);
				progress.setIndeterminate(true);
				fetchSeriesJSON();
			}
		});
	}
	
	private void toListState () {
		runOnUiThread(new Runnable() {
			@Override
			public void run () {
				progress = null;
				setContentView(R.layout.onlineseries_list);
				ListView listView = (ListView)findViewById(android.R.id.list);
				listView.setAdapter(new SeriesAdapter());
				
				Button refreshBtn = (Button)findViewById(R.id.btn_refresh);
				refreshBtn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick (View v) {
						toLoadingState();
					}
				});
			}
		});
	}
	
	protected void onSeriesUpdated () {
		toListState();
	}
	
	protected void fetchSeriesJSON () {
		(new Thread() {
			@Override
			public void run () {
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(EditorConstants.getListUrl());
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
	
	class SeriesAdapter extends BaseAdapter {
		@Override
		public int getCount () {
			if (series == null)
				return 0;
			return series.length();
		}
		
		@Override 
		public Object getItem (int position) {
			if (series == null)
				return null;
			try {
	      return series.get(position);
      } catch (JSONException e) {
	      e.printStackTrace();
	      return null;
      }
		}
		
		@Override
		public long getItemId (int position) {
			return position;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (convertView != null)
				view = convertView;
			else
				view = getLayoutInflater().inflate(R.layout.serielist_item, null);
			TextView textName = (TextView)view.findViewById(R.id.name);
			if (series == null)
				textName.setText(R.string.save_err_external);
			else {
				JSONObject serieObj;
        try {
	        serieObj = series.getJSONObject(position);
	        textName.setText(serieObj.getString("name"));
        } catch (JSONException e) {
	        e.printStackTrace();
	        textName.setText("Error loading serie");
        }
			}
			return view;
		}
	}
}
