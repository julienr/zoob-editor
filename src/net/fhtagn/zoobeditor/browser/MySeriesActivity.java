package net.fhtagn.zoobeditor.browser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import net.fhtagn.zoobeditor.Common;
import net.fhtagn.zoobeditor.ExternalStorageException;
import net.fhtagn.zoobeditor.R;
import net.fhtagn.zoobeditor.editor.EditorActivity;
import net.fhtagn.zoobeditor.editor.SerieEditActivity;
import net.fhtagn.zoobeditor.editor.utils.Types;
import net.fhtagn.zoobeditor.editor.utils.WallView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
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
	
	private int deletePosition; //set when the confirm dialog for deletion is shown. Contains the item position 
	
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
    try {
    	Common.getLevelsDir(); //this is just to test if external storage is available. It will raise an exception if not
			setContentView(R.layout.myseries_list);
			
  		setListAdapter(new SerieAdapter(this));
  		getListView().setOnCreateContextMenuListener(this);
			
			Button newSerieBtn = (Button) findViewById(R.id.btn_newserie);
			newSerieBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					showDialog(DIALOG_NEWSERIE_ID);
				}
			});
    } catch (ExternalStorageException e) {
    	//If the external storage is not available, display simple error message
  		e.printStackTrace();
  		TextView textView = new TextView(this);
  		textView.setText(R.string.save_err_external);
  		setContentView(textView);
    }
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
    
    File file = (File)getListAdapter().getItem(info.position);
    if (file == null) {
    	//for some reason, requested item not available, do nothing
    	return;
    }
    
    menu.setHeaderTitle(file.getName());
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
    		Uri.Builder builder = new Uri.Builder();
    		builder.scheme("content");
    		builder.authority("net.fhtagn.zoobgame");
    		
    		SerieAdapter adapter = (SerieAdapter)getListAdapter();
    		File serie = adapter.getSerie(info.position);
    		builder.path(serie.getName());
    		Intent i = new Intent("net.fhtagn.zoobgame.PLAY", builder.build());
    		Log.e(TAG, "uri : " + i.getData().toString());
    		Log.e(TAG, "type : " + i.getType());
    		startActivity(i);
    		return true;
    	}
    	case MENU_ITEM_EDIT: {
    		SerieAdapter adapter = (SerieAdapter)getListAdapter();
    		JSONObject serieObj = adapter.loadSerie(info.position);
    		launchSerieEditor(serieObj);
    		return true;
    	}
    	case MENU_ITEM_UPLOAD:
    		return true;
    	case MENU_ITEM_DELETE: {
    		deletePosition = info.position;
    		showDialog(DIALOG_CONFIRM_DELETE);
    		return true;
    	}
    }
    return false;
	}
	
	@Override
	protected void onResume () {
		super.onResume();
		SerieAdapter adapter = (SerieAdapter)getListAdapter();
		adapter.loadFiles();
		adapter.notifyDataSetChanged();
	}
	
	@Override
	public void onListItemClick (ListView l, View v, int position, long id) {
		l.showContextMenuForChild(v);
	}
	
	protected void launchSerieEditor (JSONObject serieObj) {
		Intent i = new Intent(getParent(), SerieEditActivity.class);
		i.putExtra("serie", serieObj.toString());
		this.getParent().startActivity(i);
	}
	
	protected JSONObject newSerie (String name) throws JSONException {
		JSONObject obj = new JSONObject();
	  obj.put("name", name);
		return obj;
	}
	
	@Override
	protected Dialog onCreateDialog (int id) {
		switch (id) {
			case DIALOG_NEWSERIE_ID: {
				return new SaveDialog(this, DIALOG_NEWSERIE_ID, this, new SaveDialog.OnOkListener() {
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
		adapter.deleteSerie(deletePosition);
	}
	
	
	
	public class SerieAdapter extends BaseAdapter {
		private File[] files;
		private final Context context;
		public SerieAdapter (Context context) {
			super();
			this.context = context;
			loadFiles();
		}
		
		public void loadFiles () {
			try {
	    	//Normal case => display a list of levels
		    File root = Common.getLevelsDir();
	  		files = root.listFiles(new FilenameFilter() {
	  			@Override
	        public boolean accept(File file, String name) {
	  				return name.endsWith(".json");
	        }
	  		});
			} catch (ExternalStorageException e) {
				e.printStackTrace();
				files = null;
			}
		}

		@Override
    public int getCount() {
			if (files == null)
				return 0;
			return files.length;
    }

		@Override
    public Object getItem(int position) {
			if (files == null)
				return null;
			return files[position];
    }

		@Override
    public long getItemId(int position) {
			return position;
    }
		
		public void deleteSerie (int position) {
			files[position].delete();
			loadFiles();
			this.notifyDataSetChanged();
		}
		
		public JSONObject loadSerie (int position) {
		  try {
	      BufferedReader reader = new BufferedReader(new FileReader(files[position]));
	      String content = "";
	      String line;
	      while ((line = reader.readLine()) != null)
	      	content += line;
	      return new JSONObject(content);
      } catch (FileNotFoundException e) {
	      e.printStackTrace();
	      return null;
      } catch (JSONException e) {
	      e.printStackTrace();
	      return null;
      } catch (IOException e) {
	      e.printStackTrace();
	      return null;
      }
		}
		
		public File getSerie (int position) {
			return files[position];
		}

		@Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View view;
      if (convertView != null)
      	view = convertView;
      else
      	view = getLayoutInflater().inflate(R.layout.serielist_item, null);
      
      //JSONObject levelObj = loadLevel(files[position]);
      //FIXME: currently, "name" is not stored in level => do that
      TextView textName = (TextView)view.findViewById(R.id.name);
      if (files == null)
      	textName.setText(R.string.save_err_external);
      else
      	textName.setText(files[position].getName());
    	/*try {
    		String name = levelObj.getString("name");
        textName.setText(name);
      } catch (JSONException e) {
        e.printStackTrace();
        textName.setText("Error loading : " + files[position].getName());
      }*/
      return view;
    }
	}

}
