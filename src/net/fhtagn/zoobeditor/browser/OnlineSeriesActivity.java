package net.fhtagn.zoobeditor.browser;

import java.util.Date;

import net.fhtagn.zoobeditor.Common;
import net.fhtagn.zoobeditor.EditorConstants;
import net.fhtagn.zoobeditor.Series;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.fhtagn.zoobeditor.R;
import net.fhtagn.zoobeditor.editor.SerieEditActivity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class OnlineSeriesActivity extends URLFetchActivity implements OnItemClickListener {
	
	private JSONArray series = null;
	
	@Override
	public void onResume () {
		super.onResume();
		toLoadingState();
	}
	
	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.onlineserielist_menu, menu);
		Common.createCommonOptionsMenu(this, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected (MenuItem item) {
		switch (item.getItemId()) {
			case R.id.refresh:
				toLoadingState();
				return true;
		}
		return Common.commonOnOptionsItemSelected(this, item);
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
	}
	
	@Override
	protected String getURL() {
		return EditorConstants.getListUrl();
	}
	
	@Override
  public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
	  try {
	    long communityId = series.getJSONObject(position).getJSONObject("meta").getLong("id");
	    Cursor cur = managedQuery(Series.CONTENT_URI, new String[]{Series.ID, Series.IS_MINE}, Series.COMMUNITY_ID+"="+communityId, null, null);
	    Intent intent;
	    if (cur.moveToFirst()) {
	    	//exists in local DB, display the offline serie view
	    	int isMine = cur.getInt(cur.getColumnIndex(Series.IS_MINE));
	    	if (isMine == 1) { //we are the owner => start editor
	    		intent = new Intent(getApplicationContext(), SerieEditActivity.class);
	    		intent.setData(ContentUris.withAppendedId(Series.CONTENT_URI, cur.getLong(cur.getColumnIndex(Series.ID))));
	    	} else { //standard offline view
	    		intent = new Intent(getApplicationContext(), OfflineSerieViewActivity.class);
		    	intent.putExtra("serieid", cur.getLong(cur.getColumnIndex(Series.ID)));
	    	}
	    } else {
	    	//not found in local DB
		    intent = new Intent(getApplicationContext(), OnlineSerieViewActivity.class);
		    intent.putExtra("communityID", communityId);
	    }
	    startActivity(intent);
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
	        
	        //Look if this serie has been downloaded
	        int communityId = serieObj.getJSONObject("meta").getInt("id");
	        Cursor cur = getContentResolver().query(Series.CONTENT_URI, new String[]{Series.ID, Series.IS_MINE, Series.LAST_MODIFICATION}, Series.COMMUNITY_ID+"="+communityId, null, null);
	  			TextView downloadStatus = (TextView)view.findViewById(R.id.download_status);
	        if (!cur.moveToFirst()) {
	        	downloadStatus.setText(R.string.not_downloaded);
	        	downloadStatus.setTextColor(EditorConstants.COLOR_NOT_UPLOADED);
	        } else {
	        	if (cur.getInt(cur.getColumnIndex(Series.IS_MINE)) == 1) {
	        		downloadStatus.setText(R.string.mine);
	        		downloadStatus.setTextColor(EditorConstants.COLOR_UPLOADED);
	        	} else {
		        	Date localModification = Common.dateFromDB(cur.getString(cur.getColumnIndex(Series.LAST_MODIFICATION)));
		        	Date serverModification = Common.dateFromDB(serieObj.getJSONObject("meta").getString("updated"));
		        	if (Common.epsilonBefore(localModification, serverModification)) {
		        		//mark this serie as updated
		        		ContentValues values = new ContentValues();
		        		values.put(Series.UPDATE_AVAILABLE, true);
		        		getContentResolver().update(ContentUris.withAppendedId(Series.CONTENT_URI, cur.getLong(cur.getColumnIndex(Series.ID))), values, null, null);
		        		
		        		downloadStatus.setText(R.string.update_available);
		        		downloadStatus.setTextColor(EditorConstants.COLOR_NOT_UPLOADED);
		        	} else {
		        		downloadStatus.setText(R.string.downloaded);
		        		downloadStatus.setTextColor(EditorConstants.COLOR_UPLOADED);
		        	}
	        	}
	        }
	        cur.close();
	        
	        //Rating
	        RatingBar ratingBar = (RatingBar)view.findViewById(R.id.rating);
	        if (serieObj.getJSONObject("meta").has("rating")) {
	        	float rating = (float)serieObj.getJSONObject("meta").getDouble("rating");
	        	ratingBar.setRating(rating);
	        } else {
	        	ratingBar.setVisibility(View.INVISIBLE);
	        }
        } catch (JSONException e) {
	        e.printStackTrace();
	        textName.setText("Error loading serie");
        }
			}
			
			return view;
		}
	}
}
