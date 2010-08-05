package net.fhtagn.zoobeditor.browser;

import org.json.JSONException;
import org.json.JSONObject;

import net.fhtagn.zoobeditor.R;
import net.fhtagn.zoobeditor.SerieCursorAdapter;
import net.fhtagn.zoobeditor.Series;
import net.fhtagn.zoobeditor.browser.MySeriesActivity.SerieAdapter;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

public class DownloadedActivity extends ListActivity {
	private String[] projection = new String[]{Series.ID, Series.JSON, Series.LAST_MODIFICATION, Series.PROGRESS};
	
	@Override
	public void onCreate (Bundle savedInstancestate) {
		super.onCreate(savedInstancestate);
		
		setContentView(R.layout.downloaded_list);
		
		Cursor cur = managedQuery(Series.CONTENT_URI, projection, Series.IS_MINE+"=0", null, null);
		setListAdapter(new SerieAdapter(this, cur));
	}
	
	private void refreshList () {
		SerieAdapter adapter = (SerieAdapter)getListAdapter();
		adapter.getCursor().requery();
	}
	
	@Override
	protected void onResume () {
		super.onResume();
		refreshList();
	}
	
	@Override
  public void onListItemClick (ListView listView, View view, int position, long id) {
    Intent i = new Intent(getApplicationContext(), OfflineSerieViewActivity.class);
    i.putExtra("serieid", id);
    startActivity(i);
  }
	
	public class SerieAdapter extends SerieCursorAdapter {
		public SerieAdapter(Context context, Cursor c) {
			super(context, c, R.layout.downloaded_list_item);
		}
		
		@Override
		protected void fillView (View view, Cursor cursor) {
			String json = cursor.getString(cursor.getColumnIndex(Series.JSON));
			TextView textName = (TextView)view.findViewById(R.id.name);
			try {
				JSONObject serieObj = new JSONObject(json);
				textName.setText(serieObj.getString("name"));
				
				//Progress
				TextView progressView = (TextView)view.findViewById(R.id.progress);
				progressView.setText(cursor.getInt(cursor.getColumnIndex(Series.PROGRESS)) + " / " + serieObj.getJSONArray("levels").length());
				
				//Rating
        RatingBar ratingBar = (RatingBar)view.findViewById(R.id.rating);
        if (serieObj.has("meta") && serieObj.getJSONObject("meta").has("rating")) {
        	float rating = (float)serieObj.getJSONObject("meta").getDouble("rating");
        	ratingBar.setRating(rating);
        } else {
        	ratingBar.setVisibility(View.INVISIBLE);
        }
			} catch (JSONException e) {
				e.printStackTrace();
				textName.setText("JSON error : " + e.getMessage());
			}
		}
	}
}
