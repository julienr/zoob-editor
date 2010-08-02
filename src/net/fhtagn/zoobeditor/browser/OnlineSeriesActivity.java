package net.fhtagn.zoobeditor.browser;

import net.fhtagn.zoobeditor.EditorConstants;
import net.fhtagn.zoobeditor.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class OnlineSeriesActivity extends URLFetchActivity implements OnItemClickListener {
	
	private JSONArray series = null;
	
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		toLoadingState();
	}
	
	@Override
	protected void onContentReady (String result) {
    try {
    	if (result != null)
    		series = new JSONArray(result);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    
		setContentView(R.layout.onlineseries_list);
		ListView listView = (ListView)findViewById(android.R.id.list);
		listView.setAdapter(new SeriesAdapter());
		listView.setOnItemClickListener(OnlineSeriesActivity.this);
		
		Button refreshBtn = (Button)findViewById(R.id.btn_refresh);
		refreshBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick (View v) {
				toLoadingState();
			}
		});
	}
	
	@Override
	protected String getURL() {
		return EditorConstants.getListUrl();
	}
	
	@Override
  public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
	  try {
	    int serieId = series.getJSONObject(position).getJSONObject("meta").getInt("id");
	    Intent i = new Intent(getApplicationContext(), OnlineSerieViewActivity.class);
	    i.putExtra("serieid", serieId);
	    startActivity(i);
    } catch (JSONException e) {
	    e.printStackTrace();
    }
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
				view = getLayoutInflater().inflate(R.layout.onlineserielist_item, null);
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
