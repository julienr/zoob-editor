package net.fhtagn.zoobeditor.browser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.fhtagn.zoobeditor.Common;
import net.fhtagn.zoobeditor.R;
import net.fhtagn.zoobeditor.Series;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

public class OfflineSerieViewActivity extends Activity {
	static final String TAG = "OfflineSerieViewActivity";
	static final int DIALOG_CONFIRM_DELETE = 0;
	private long serieID;
	
	private JSONObject serieObj = null;
	private JSONArray levelsArray = null;
	
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent i = getIntent();
		if (i == null) {
			Log.e(TAG, "onCreate : null intent");
			finish();
		}
		serieID = i.getLongExtra("serieid", -1);
		if (serieID == -1) {
			Log.e(TAG, "onCreate : serieID = -1");
			finish();
		}
		
		
		Cursor cur = managedQuery(ContentUris.withAppendedId(Series.CONTENT_URI, serieID), new String[]{Series.JSON, Series.NAME, Series.PROGRESS}, null, null, null);
		if (!cur.moveToFirst()) {
			Log.e(TAG, "onCreate: !cur.moveToFirst");
			finish();
		}
		
		setContentView(R.layout.serieview);
		
		try {
			serieObj = new JSONObject(cur.getString(cur.getColumnIndex(Series.JSON)));
			levelsArray = serieObj.getJSONArray("levels");
			
			TextView serieName = (TextView)findViewById(R.id.name);
			serieName.setText(cur.getString(cur.getColumnIndex(Series.NAME)));
			/*GridView gridView = (GridView)findViewById(android.R.id.list);
			gridView.setAdapter(new LevelsAdapter(this, serieObj.getJSONArray("levels")));*/
			SeriePreviewGrid previewGrid = (SeriePreviewGrid)findViewById(R.id.seriepreview);
	    previewGrid.setSerie(serieObj);
			
			Button playBtn = (Button)findViewById(R.id.btn_play);
			playBtn.setText(R.string.btn_play_serie);
			playBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					if (serieObj == null) {
						Log.e(TAG, "serieObj = null");
						return;
					}
					Intent i = Common.playSerie(serieID);
					startActivity(i);
				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
			finish();
		}
	}
	
	@Override
	protected Dialog onCreateDialog (int id) {
		switch (id) {
			case DIALOG_CONFIRM_DELETE: {
				return Common.createConfirmDeleteDialog(this, R.string.confirm_delete_serie, new DialogInterface.OnClickListener() {
	  			public void onClick (DialogInterface dialog, int id) {
	  				Uri deleteUri = ContentUris.withAppendedId(Series.CONTENT_URI, serieID);
	  				getContentResolver().delete(deleteUri, null, null);
	  				OfflineSerieViewActivity.this.finish();
	  			}
				});
			}
			default:
				return null;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.offlineserieview_menu, menu);
		Common.createCommonOptionsMenu(this, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected (MenuItem item) {
		switch (item.getItemId()) {
			case R.id.delete:
				showDialog(DIALOG_CONFIRM_DELETE);
				return true;
		}
		return Common.commonOnOptionsItemSelected(this, item);
	}
}
