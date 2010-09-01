package net.fhtagn.zoobeditor.browser;

import java.util.Date;

import net.fhtagn.zoobeditor.Common;
import net.fhtagn.zoobeditor.EditorConstants;
import net.fhtagn.zoobeditor.SerieCursorAdapter;
import net.fhtagn.zoobeditor.Series;
import net.fhtagn.zoobeditor.editor.SerieEditActivity;

import org.json.JSONException;
import org.json.JSONObject;

import net.fhtagn.zoobeditor.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

public class MySeriesActivity extends ListActivity {
	static final String TAG = "MyLevelsActivity";
	
	static final int DIALOG_NEWSERIE_ID = 0;
	static final int DIALOG_CONFIRM_DELETE = 1;
	
	static final int MENU_ITEM_PLAY = 0;
	static final int MENU_ITEM_EDIT = 1;
	static final int MENU_ITEM_UPLOAD = 2;
	static final int MENU_ITEM_DELETE = 3;
	
	private long deleteID; //set when the confirm dialog for deletion is shown. Contains the item position 
	
	private String[] projection = new String[]{Series.ID, Series.NAME, Series.JSON, Series.COMMUNITY_ID, Series.LAST_MODIFICATION, Series.UPLOAD_DATE};
	
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		setContentView(R.layout.myseries_list);
		
		Cursor cur = managedQuery(Series.CONTENT_URI, projection, Series.IS_MINE+"=1", null, null);
		setListAdapter(new SerieAdapter(this, cur));
		getListView().setOnCreateContextMenuListener(this);
		
		Button newSerieBtn = (Button) findViewById(R.id.btn_newserie);
		newSerieBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_NEWSERIE_ID);
			}
		});
	}
	
	private void refreshList () {
		SerieAdapter adapter = (SerieAdapter)getListAdapter();
		adapter.getCursor().requery();
	}
	
	@Override
	public void onCreateContextMenu (ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    AdapterView.AdapterContextMenuInfo info;
    try {
      info = (AdapterView.AdapterContextMenuInfo) menuInfo;
    } catch (ClassCastException e) {
      Log.e(TAG, "bad menuInfo", e);
      return;
    }
    
    SerieAdapter adapter = (SerieAdapter)getListAdapter();
    menu.setHeaderTitle(adapter.getName(info.position));
    menu.add(0, MENU_ITEM_PLAY, 0, R.string.menu_play_serie);
    menu.add(1, MENU_ITEM_EDIT, 1, R.string.menu_edit);
    menu.add(2, MENU_ITEM_UPLOAD, 2, R.string.menu_upload);
    menu.add(3, MENU_ITEM_DELETE, 3, R.string.menu_delete);
	}
	
	@Override
	public boolean onContextItemSelected (MenuItem item) {
    AdapterView.AdapterContextMenuInfo info;
    try {
      info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
    } catch (ClassCastException e) {
      Log.e(TAG, "bad menuInfo", e);
      return false;
    }
    
    switch (item.getItemId()) {
    	case MENU_ITEM_PLAY: {
    		Common.Serie.play(this, info.id);
        return true;
    	}
    	case MENU_ITEM_EDIT: {
    		launchSerieEditor(ContentUris.withAppendedId(Series.CONTENT_URI, info.id));
    		return true;
    	}
    	case MENU_ITEM_UPLOAD: {
    		Common.Serie.upload(this, info.id);
    		return true;
    	}
    	case MENU_ITEM_DELETE: {
    		deleteID = info.id;
    		showDialog(DIALOG_CONFIRM_DELETE);
    		return true;
    	}
    }
    return false;
	}
	
	@Override
	protected void onResume () {
		super.onResume();
		refreshList();
	}
	
	@Override
	public void onListItemClick (ListView l, View v, int position, long id) {
		launchSerieEditor(ContentUris.withAppendedId(Series.CONTENT_URI, id));
	}
	
	protected void launchSerieEditor (Uri uri) {
		Intent i = new Intent(getParent(), SerieEditActivity.class);
		i.setData(uri);
		this.getParent().startActivity(i);
	}
	
	protected Uri newSerie (String name) throws JSONException {
		JSONObject obj = new JSONObject();
	  obj.put("name", name);
	  ContentValues values = new ContentValues();
	  values.put(Series.JSON, obj.toString());
	  values.put(Series.IS_MINE, true);
	  return getContentResolver().insert(Series.CONTENT_URI, values);
	}
	
	@Override
	protected Dialog onCreateDialog (int id) {
		switch (id) {
			case DIALOG_NEWSERIE_ID: {
				return new NameDialog(this, DIALOG_NEWSERIE_ID, this, new NameDialog.OnOkListener() {
					@Override
					public void onOK(String enteredText) {
						try {
	            launchSerieEditor(newSerie(enteredText));
            } catch (JSONException e) {
	            e.printStackTrace();
            }
					}
				});
			}
			case DIALOG_CONFIRM_DELETE: {
				return Common.createConfirmDeleteDialog(this, R.string.confirm_delete_serie, new DialogInterface.OnClickListener() {
	  			public void onClick (DialogInterface dialog, int id) {
	  				Common.Serie.deleteSerie(MySeriesActivity.this, deleteID);
	  				refreshList();
	  			}
				});
			}
			default:
				return null;
		}
	}
	
	public class SerieAdapter extends SerieCursorAdapter {	
		public SerieAdapter(Context context, Cursor c) {
	    super(context, c, R.layout.serielist_item);
    }
    
		@Override
		protected void fillView (View view, Cursor cursor) {
			TextView textName = (TextView) view.findViewById(R.id.name);
			textName.setText(cursor.getString(cursor.getColumnIndex(Series.NAME)));
			
			TextView uploadStatus = (TextView)view.findViewById(R.id.upload_status);
			boolean uploaded = !cursor.isNull(cursor.getColumnIndex(Series.COMMUNITY_ID));
			if (uploaded) {
				if (cursor.isNull(cursor.getColumnIndex(Series.UPLOAD_DATE))) {
					//shouldn't happen, but yeah :)
					Log.e(TAG, "Serie with community id : " + cursor.getLong(cursor.getColumnIndex(Series.COMMUNITY_ID)) + " has a NULL upload_date");
					uploadStatus.setText(R.string.uploaded);
					uploadStatus.setTextColor(EditorConstants.COLOR_UPLOADED);	
				} else {
					Date uploadDate = Common.dateFromDB(cursor.getString(cursor.getColumnIndex(Series.UPLOAD_DATE)));
					Date lastModification = Common.dateFromDB(cursor.getString(cursor.getColumnIndex(Series.LAST_MODIFICATION)));
					Log.i(TAG, "upload date : " + uploadDate + " lastModification : " + lastModification);
					if (Common.epsilonBefore(uploadDate, lastModification)) {
						uploadStatus.setText(R.string.modified);
						uploadStatus.setTextColor(EditorConstants.COLOR_NOT_UPLOADED);
					} else {
						uploadStatus.setText(R.string.uploaded);
						uploadStatus.setTextColor(EditorConstants.COLOR_UPLOADED);
					}
				}
			} else {
				uploadStatus.setText(R.string.not_uploaded);
				uploadStatus.setTextColor(EditorConstants.COLOR_NOT_UPLOADED);
			}
		}

	
	}

}
