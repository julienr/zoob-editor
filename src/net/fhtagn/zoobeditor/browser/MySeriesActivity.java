package net.fhtagn.zoobeditor.browser;

import java.io.File;

import net.fhtagn.zoobeditor.Common;
import net.fhtagn.zoobeditor.EditorConstants;
import net.fhtagn.zoobeditor.R;
import net.fhtagn.zoobeditor.Series;
import net.fhtagn.zoobeditor.editor.SerieEditActivity;

import org.json.JSONException;
import org.json.JSONObject;

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
	
	private String[] projection = new String[]{Series.ID, Series.JSON};
	
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
		Cursor cur = managedQuery(Series.CONTENT_URI, projection, Series.IS_MINE+"=1", null, null);
		SerieAdapter adapter = (SerieAdapter)getListAdapter();
		adapter.changeCursor(cur);
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
    menu.add(0, MENU_ITEM_PLAY, 0, R.string.menu_play);
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
    		SerieAdapter adapter = (SerieAdapter)getListAdapter();
        Intent i = Common.playSerie(info.id);
        startActivity(i);
        return true;
    	}
    	case MENU_ITEM_EDIT: {
    		SerieAdapter adapter = (SerieAdapter)getListAdapter();
    		launchSerieEditor(ContentUris.withAppendedId(Series.CONTENT_URI, info.id));
    		return true;
    	}
    	case MENU_ITEM_UPLOAD: {
    		SerieAdapter adapter = (SerieAdapter)getListAdapter();
    		Browser parent = (Browser)getParent();
    		Intent i = new Intent(getApplicationContext(), UploadActivity.class);
    		i.putExtra("id", info.id);
    		startActivityForResult(i, EditorConstants.SEND_TO_ZOOB_WEB);
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
		l.showContextMenuForChild(v);
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
	  values.put(Series.IS_MINE, 1);
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
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.delete_dlg_title)
							 .setMessage(R.string.confirm_delete)
							  .setCancelable(true)
							  .setPositiveButton(android.R.string.ok,
							  		new DialogInterface.OnClickListener() {
							  			public void onClick (DialogInterface dialog, int id) {
							  				deleteSerie();
							  			}
							  	})
							  .setNegativeButton(android.R.string.cancel,
							    new DialogInterface.OnClickListener() {
								    public void onClick(DialogInterface dialog, int id) {
									    dialog.cancel();
								    }
								  });
				return builder.create();
			}
			default:
				return null;
		}
	}
	
	private void deleteSerie () {
		SerieAdapter adapter = (SerieAdapter)getListAdapter();
		Uri deleteUri = ContentUris.withAppendedId(Series.CONTENT_URI, deleteID);
		getContentResolver().delete(deleteUri, null, null);
		refreshList();
	}
	
	public class SerieAdapter extends CursorAdapter {	
		public SerieAdapter(Context context, Cursor c) {
	    super(context, c);
    }
		
		public String getName(int position) {
			//FIXME: could use some caching
			Cursor cur = (Cursor)this.getItem(position);
			String json = cur.getString(cur.getColumnIndex(Series.JSON));
			JSONObject levelObj;
      try {
	      levelObj = new JSONObject(json);
	      return levelObj.getString("name");
      } catch (JSONException e) {
	      e.printStackTrace();
	      return "";
      }
		}
    
		private void fillView (View view, Cursor cursor) {
			String json = cursor.getString(cursor.getColumnIndex(Series.JSON));
			TextView textName = (TextView) view.findViewById(R.id.name);
			try {
				JSONObject levelObj = new JSONObject(json);
				String name = levelObj.getString("name");
				textName.setText(name);
			} catch (JSONException e) {
				e.printStackTrace();
				textName.setText("JSON error");
			}		
		}

		@Override
    public void bindView(View view, Context context, Cursor cursor) {
			fillView(view, cursor);
    }

		@Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = getLayoutInflater().inflate(R.layout.serielist_item, null);
			fillView(view, cursor);
	    return view;
    }
	}

}
