package net.fhtagn.zoobeditor.browser;

import net.fhtagn.zoobeditor.Common;
import net.fhtagn.zoobeditor.EditorConstants;
import net.fhtagn.zoobeditor.R;
import net.fhtagn.zoobeditor.Series;
import net.fhtagn.zoobeditor.editor.MiniLevelView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

public class OnlineSerieViewActivity extends URLFetchActivity {
	static final String TAG = "OnlineSerieViewActivity";
	
	private JSONObject serieObj = null;
	private JSONArray levelsArray = null;
	
	private int serieID;
	
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent i = getIntent();
		if (i == null) {
			Log.e(TAG, "onCreate : null intent");
			finish();
		}
		
		serieID = i.getIntExtra("serieid", -1);
		if (serieID == -1) {
			Log.e(TAG, "onCreate : serieid = -1");
			finish();
		}
		
		toLoadingState();
	}
	
	@Override
	protected void onContentReady (String result) {
		try {
			if (result != null) {
				serieObj = new JSONObject(result);
				levelsArray = serieObj.getJSONArray("levels");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		setContentView(R.layout.serieview);
		
		TextView serieName = (TextView)findViewById(R.id.name);
		try {
	    serieName.setText(serieObj.getString("name"));
			GridView gridView = (GridView)findViewById(android.R.id.list);
			gridView.setAdapter(new LevelsAdapter());
    } catch (JSONException e) {
    	serieName.setText("Error loading serie from JSON");
	    e.printStackTrace();
    }
		
		Button playBtn = (Button)findViewById(R.id.btn_play);
		playBtn.setOnClickListener(new OnClickListener() {
			@Override
      public void onClick(View view) {
				if (serieObj == null) {
					Log.e(TAG, "Trying to play with serieObj = null");
					return;
				}
				Intent i = Common.playSerie(saveSerie(serieObj));
				startActivity(i);
      }
		});
	}
	
	private long saveSerie (JSONObject serie) {
	  try {
		  ContentValues values = new ContentValues();
		  values.put(Series.JSON, serie.toString());
		  values.put(Series.IS_MINE, false); //FIXME: should check if author is owner of phone, in case of reinstall
	    values.put(Series.COMMUNITY_ID, serie.getJSONObject("meta").getInt("id"));
	    return Long.parseLong(getContentResolver().insert(Series.CONTENT_URI, values).getLastPathSegment());
    } catch (JSONException e) {
	    e.printStackTrace();	   
	    throw new IllegalArgumentException("saveSerie. Serie deson't have a community id");
    }
	  
	}
	
	@Override
	protected String getURL() {
		return EditorConstants.getDetailsUrl(serieID);
	}
	
	class LevelsAdapter extends BaseAdapter {				
		@Override
    public int getCount() {
			return levelsArray.length();
    }

		@Override
    public Object getItem(int position) {
	    try {
	      return levelsArray.get(position);
      } catch (JSONException e) {
	      e.printStackTrace();
	      return null;
      }
    }

		@Override
    public long getItemId(int position) {
			return position;
    }
		
		@Override
    public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (convertView != null)
				view = convertView;
			else
				view = getLayoutInflater().inflate(R.layout.serieview_item, null);
			
			MiniLevelView levelView = (MiniLevelView)view.findViewById(R.id.minilevel);
			try {
	      JSONObject levelObj = levelsArray.getJSONObject(position);
	      levelView.setLevel(levelObj);
      } catch (JSONException e) {
      	TextView textView = new TextView(OnlineSerieViewActivity.this);
      	textView.setText("Error reading level");
	      e.printStackTrace();
	      return textView;	
      }
	    return view;
    }
		
	}
}
