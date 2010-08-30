package net.fhtagn.zoobeditor.browser;

import net.fhtagn.zoobeditor.Common;
import net.fhtagn.zoobeditor.EditorConstants;
import net.fhtagn.zoobeditor.Series;
import net.fhtagn.zoobeditor.editor.MiniLevelView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.fhtagn.zoobeditor.R;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RatingBar;
import android.widget.TextView;

public class OnlineSerieViewActivity extends URLFetchActivity {
	static final String TAG = "OnlineSerieViewActivity";
	
	private JSONObject serieObj = null;
	
	private long communityID;
	
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent i = getIntent();
		if (i == null) {
			Log.e(TAG, "onCreate : null intent");
			finish();
		}
		
		communityID = i.getLongExtra("communityID", -1);
		if (communityID == -1) {
			Log.e(TAG, "onCreate : serieid = -1");
			finish();
		}
		
		toLoadingState();
	}
	
	@Override
	protected void onContentReady (String result) {
		if (result == null) {
			Log.e(TAG, "onContentReady : null result");
			finish();
		}
		
		setContentView(R.layout.serieview);
		
		TextView serieName = (TextView)findViewById(R.id.name);
		try {
			serieObj = new JSONObject(result);
	    serieName.setText(serieObj.getString("name"));
			/*GridView gridView = (GridView)findViewById(android.R.id.list);
			gridView.setAdapter(new LevelsAdapter(this, serieObj.getJSONArray("levels")));*/
	    SeriePreviewGrid previewGrid = (SeriePreviewGrid)findViewById(R.id.seriepreview);
	    previewGrid.setSerie(serieObj);
	    
	    TextView authorView = (TextView)findViewById(R.id.serie_author);
	    authorView.setText(serieObj.getJSONObject("meta").getString("author"));
	    
	    TextView numLvlView = (TextView)findViewById(R.id.serie_num_levels);
	    numLvlView.setText(""+serieObj.getJSONArray("levels").length());
	    
	    TextView progressView = (TextView)findViewById(R.id.progress);
	    progressView.setText(R.string.no_progress);
    } catch (JSONException e) {
    	serieName.setText("Error loading serie from JSON");
	    e.printStackTrace();
    }
    
    //Disable "my" rating, only available when viewing from downloaded series
    TextView sep = (TextView)findViewById(R.id.my_rating_separator);
    RatingBar myRating = (RatingBar)findViewById(R.id.my_rating);
  	sep.setVisibility(View.GONE);
  	myRating.setVisibility(View.GONE);
  	
  	//Display community rating
  	RatingBar communityRating = (RatingBar)findViewById(R.id.rating);
  	try {
    	JSONObject meta = serieObj.getJSONObject("meta");
	    if (!meta.has("rating"))
	    	communityRating.setVisibility(View.INVISIBLE);
	    else
	    	communityRating.setRating((float)meta.getDouble("rating"));
    } catch (JSONException e) {
    	e.printStackTrace();
    }
    
    Button updateBtn = (Button)findViewById(R.id.btn_update);
    updateBtn.setVisibility(View.GONE);
		
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
	  	//Look if this serie has been downloaded
	  	JSONObject meta = serie.getJSONObject("meta");
	    int communityId = meta.getInt("id");
	    Cursor cur = getContentResolver().query(Series.CONTENT_URI, new String[]{Series.ID}, Series.COMMUNITY_ID+"="+communityId, null, null);
	    ContentValues values = new ContentValues();
		  values.put(Series.JSON, serie.toString());
	    values.put(Series.COMMUNITY_ID, meta.getInt("id"));
	    if (meta.has("my_rating"))
	    	values.put(Series.MY_RATING, (float)meta.getDouble("my_rating"));
	    if (!cur.moveToFirst()) {
	    	values.put(Series.IS_MINE, false); 
	    	cur.close();
		    return Long.parseLong(getContentResolver().insert(Series.CONTENT_URI, values).getLastPathSegment());
	    } else {
	    	//In our DB, update
	    	long serieId = cur.getLong(cur.getColumnIndex(Series.ID));
	    	cur.close();
	    	Uri serieUri = ContentUris.withAppendedId(Series.CONTENT_URI, serieId);
	    	getContentResolver().update(serieUri, values, null, null);
	    	return serieId;
	    }
    } catch (JSONException e) {
	    e.printStackTrace();	   
	    throw new IllegalArgumentException("saveSerie. Serie deson't have a community id");
    }
	}
	
	@Override
	protected String getURL() {
		return EditorConstants.getDetailsUrl(communityID);
	}
}
