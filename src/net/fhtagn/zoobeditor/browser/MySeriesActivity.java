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
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
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
	
	static final int MENU_ITEM_PLAY = 0;
	static final int MENU_ITEM_EDIT = 1;
	static final int MENU_ITEM_UPLOAD = 2;
	static final int MENU_ITEM_DELETE = 3;
	
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		File root;
    try {
    	//Normal case => display a list of levels
	    root = Common.getLevelsDir();
  		File[] levels = root.listFiles(new FilenameFilter() {
  			@Override
        public boolean accept(File file, String name) {
  				return name.endsWith(".json");
        }
  		});
			setContentView(R.layout.myseries_list);
			
  		setListAdapter(new LevelAdapter(this, levels));
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
    	case MENU_ITEM_EDIT:
    		return true;
    	case MENU_ITEM_UPLOAD:
    		return true;
    	case MENU_ITEM_DELETE:
    		return true;
    }
    return false;
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
			default:
				return null;
		}
	}
	
	
	
	public class LevelAdapter extends BaseAdapter {
		private final File[] files;
		private final Context context;
		public LevelAdapter (Context context, File[] files) {
			super();
			this.context = context;
			this.files = files;
		}

		@Override
    public int getCount() {
			return files.length;
    }

		@Override
    public Object getItem(int position) {
			return files[position];
    }

		@Override
    public long getItemId(int position) {
			return position;
    }
		
		private JSONObject loadLevel (File f) {
		  try {
	      BufferedReader reader = new BufferedReader(new FileReader(f));
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
