package net.fhtagn.zoobeditor.editor;

import net.fhtagn.zoobeditor.Common;
import net.fhtagn.zoobeditor.EditorConstants;
import net.fhtagn.zoobeditor.Series;
import net.fhtagn.zoobeditor.browser.NameDialog;
import net.fhtagn.zoobeditor.browser.UploadActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.fhtagn.zoobeditor.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.apps.music.TouchInterceptor;

public class SerieEditActivity extends ListActivity {
	static final String TAG = "SerieEditActivity";
	static final int DIALOG_NEWLVL_ID = 0;
	static final int DIALOG_CONFIRM_LEVEL_DELETE = 1;
	static final int DIALOG_CONFIRM_SERIE_DELETE = 2;
	static final int DIALOG_RENAME = 3;
	
	static final int MENU_ITEM_PLAY = 0;
	static final int MENU_ITEM_EDIT = 1;
	static final int MENU_ITEM_DELETE = 2;
	
	private JSONObject serieObj;
	
	private int levelToDelete = -1;
	
	//When returning from a REQUEST_PLAY_RETURN_TO_EDITOR, will open the editor on this level
	private int levelToEdit = -1;
	
	private String serieName;
	
	private Uri serieUri;
	
	@Override
	public void onCreate (Bundle bundle) {
		super.onCreate(bundle);
		
		Intent i = getIntent();
		if (i == null)
			Log.e("SerieEditActivity", "NULL INTENT");
    try {
    	serieUri = i.getData();
    	Cursor cur = managedQuery(serieUri, new String[]{Series.JSON}, null, null, null);
    	if (cur.getCount() != 1 || !cur.moveToFirst())
    		throw new JSONException("Unable to get serie from content provider");
    	String serieString = cur.getString(cur.getColumnIndex(Series.JSON));
	    serieObj = new JSONObject(serieString);
   
	    setContentView(R.layout.serieedit); 
	    
	    serieName = serieObj.getString("name");
	    TextView titleView = (TextView)findViewById(R.id.title);
	    titleView.setText(serieName);
	    
			//Create levels array if not yet existing
			JSONArray arr = serieObj.optJSONArray("levels");
			if (arr == null) {
				arr = new JSONArray();
				serieObj.put("levels", arr);
			}
	    
	    setListAdapter(new SerieAdapter());
	    getListView().setOnCreateContextMenuListener(this);
	    
	    /** Drag'n'drop reordering */
	    TouchInterceptor listView = (TouchInterceptor)getListView();
	    
	    listView.setDropListener(new TouchInterceptor.DropListener() {
				@Override
        public void drop(int from, int to) {
					moveLevel(from, to);
        }
	    });
    } catch (JSONException e) {
    	TextView textView = new TextView(this);
    	e.printStackTrace();
    	textView.setText("Error loading serie : " + e.getMessage());
	    setContentView(textView);
    }
    
    Button newLvlButton = (Button)findViewById(R.id.new_level);
    newLvlButton.setOnClickListener(new OnClickListener() {
			@Override
      public void onClick(View arg0) {
				showDialog(DIALOG_NEWLVL_ID);
      }
    });
	}
	
	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.serieedit_menu, menu);
		Common.createCommonOptionsMenu(this, menu);
		return true;
	}
	
	@Override
	public void finish () {
		save();
		super.finish();
	}
	
	@Override
	public boolean onOptionsItemSelected (MenuItem item) {
		switch (item.getItemId()) {
			case R.id.play: {
				save();
				Common.Serie.play(this, Common.extractId(serieUri));
				return true;
			}
			case R.id.delete: {
				showDialog(DIALOG_CONFIRM_SERIE_DELETE);
				return true;
			}
			case R.id.upload: {
				Common.Serie.upload(this, Common.extractId(serieUri));
    		return true;
			}
			case R.id.rename:
				showDialog(DIALOG_RENAME);
				return true;
			case R.id.advanced: {
				Intent i = new Intent(getApplicationContext(), SerieOptionsActivity.class);
				i.putExtra("json", serieObj.toString());
				startActivityForResult(i, EditorConstants.REQUEST_SERIE_OPTIONS);
				return true;
			}
				
		}
		return Common.commonOnOptionsItemSelected(this, item);
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
    menu.setHeaderTitle(serieName + " #"+info.position);
    menu.add(0, MENU_ITEM_PLAY, 0, R.string.menu_play_level);
    menu.add(1, MENU_ITEM_EDIT, 1, R.string.menu_edit);
    menu.add(2, MENU_ITEM_DELETE, 2, R.string.menu_delete);
	}
	
	private void moveLevel (int from, int to) {
		if (from == to)
			return;
		try {
      JSONArray newArray = new JSONArray();
      //Copy all the objects between the beginning and min
      final JSONArray levelsArray = serieObj.getJSONArray("levels");
      for (int i=0; i<levelsArray.length(); i++) {
      	if (i == from) {
      		
      	} else if (i == to) {
      		if (from < to) {
      			newArray.put(levelsArray.getJSONObject(i));
      			newArray.put(levelsArray.getJSONObject(from));
      		} else {
      			newArray.put(levelsArray.getJSONObject(from));
      			newArray.put(levelsArray.getJSONObject(i));
      		}
      		
      	} else {
      		newArray.put(levelsArray.getJSONObject(i));
      	}
      }
      serieObj.put("levels", newArray);
      /*Log.i(TAG, "newArray : ");
      for (int i=0; i<newArray.length(); i++) {
      	Log.i(TAG, "" + i + ":"+newArray.getJSONObject(i).getInt("xdim")+"*"+newArray.getJSONObject(i).getInt("ydim"));
      }*/
      notifyAdapter();
    } catch (JSONException e) {
    	Log.e(TAG, "Error in moveLevel from " + from + " to " + to + " : " + e.getMessage());
      e.printStackTrace();
    }
	} 
	
	public void save () {
		ContentValues values = new ContentValues();
		values.put(Series.JSON, serieObj.toString());
		getContentResolver().update(serieUri, values, null, null);
	}
	
	@Override
	public void onListItemClick (ListView l, View v, int position, long id) {
		launchEditor(position);
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
    		save();
    		Common.Level.play(this, Common.extractId(serieUri), info.position, EditorConstants.REQUEST_PLAY_RETURN_TO_SERIE);
        return true;
    	}
    	case MENU_ITEM_EDIT: {
    		launchEditor(info.position);
    		return true;
    	}
    	case MENU_ITEM_DELETE: {
    		levelToDelete = info.position;
    		showDialog(DIALOG_CONFIRM_LEVEL_DELETE);
    		return true;
    	}
    }
    return false;
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case EditorConstants.REQUEST_LEVEL_EDITOR: {
				if (resultCode == RESULT_OK || resultCode == EditorConstants.RESULT_OK_PLAY) {
					String levelJSON = data.getStringExtra("json");
					int levelNumber = data.getIntExtra("number", -1);
					try {
			      JSONObject levelObj = new JSONObject(levelJSON);
			      final JSONArray levelsArray = serieObj.getJSONArray("levels");
			      if (levelNumber != -1)
			      	levelsArray.put(levelNumber, levelObj);
			      else {
			      	levelsArray.put(levelObj);
			      	levelNumber = levelsArray.length()-1;
			      }
			      serieObj.put("levels", levelsArray);
			      notifyAdapter();
			      if (resultCode == EditorConstants.RESULT_OK_PLAY) {
			      	levelToEdit = levelNumber;
			      	//FIXME: This is REALLY ugly, but this is because it looks like the save() called from notifyAdapter()
			      	//isn't blocking. And therefore, Zoob is launched and queries the content provider for the serie, but 
			      	//the old serie WITHOUT any new level is sent back
			      	try {
	              Thread.sleep(500);
              } catch (InterruptedException e) {
	              e.printStackTrace();
              }
			      	Common.Level.play(this, Common.extractId(serieUri), levelNumber, EditorConstants.REQUEST_PLAY_RETURN_TO_EDITOR);
			      }
		      } catch (JSONException e) {
			      e.printStackTrace();
			      return;
		      }
				} else if (resultCode == EditorActivity.RESULT_DELETE) {
					//deletion requested from editor
					deleteLevel();
				}
				break;
			}
			case EditorConstants.REQUEST_SERIE_OPTIONS: {
				if (resultCode != RESULT_OK) {
					Log.e(TAG, "REQUEST_LEVEL_OPTIONS: unhandled resultCode = " + resultCode);
					return;
				}
				if (!data.hasExtra("json")) {
					Log.e(TAG, "REQUEST_LEVEL_OPTIONS : got not json in response");
					return;
				}
				try {
					serieObj = new JSONObject(data.getStringExtra("json"));
					notifyAdapter();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			}
			case EditorConstants.REQUEST_PLAY_RETURN_TO_EDITOR: {
				if (levelToEdit != -1) {
					final int level = levelToEdit;
					levelToEdit = -1;
					launchEditor(level);
				}
			}
		}
	}
	
	public void notifyAdapter () {
		save();
    SerieAdapter adapter = (SerieAdapter)getListAdapter();
    adapter.notifyDataSetChanged();
	}
	
	private void doLaunch (int position, JSONObject obj) {
		Intent i = new Intent(this, EditorActivity.class);
		levelToDelete = position; //in case we receive a RESULT_DELETE
		i.putExtra("json", obj.toString());
		i.putExtra("number", position);
		i.setData(serieUri);
		startActivityForResult(i, EditorConstants.REQUEST_LEVEL_EDITOR);
	}
	
	protected void launchEditor (int position) {
		try {
	    JSONObject obj = serieObj.getJSONArray("levels").getJSONObject(position);
	    doLaunch(position, obj);
    } catch (JSONException e) {
	    e.printStackTrace();
	    return;
    }
	}
	
	protected void launchEditorNew (int xdim, int ydim) {
		JSONObject obj = new JSONObject();
		try {
	    obj.put("xdim", xdim);
	    obj.put("ydim", ydim);
    } catch (JSONException e) {
	    e.printStackTrace();
	    return;
    }
    doLaunch(-1, obj);
	}
	
	@Override
	protected Dialog onCreateDialog (int id) {
		switch (id) {
			case DIALOG_NEWLVL_ID: {
				return new LevelSizeDialog(this, DIALOG_NEWLVL_ID, this, new LevelSizeDialog.OnOkListener() {
					@Override
					public void onOK(int xdim, int ydim) {
						launchEditorNew(xdim, ydim);
					}
				});
			}
			case DIALOG_CONFIRM_SERIE_DELETE: {
				return Common.createConfirmDeleteDialog(this, R.string.confirm_delete_serie, new DialogInterface.OnClickListener() {
	  			public void onClick (DialogInterface dialog, int id) {
	  				Common.Serie.deleteSerie(SerieEditActivity.this, Common.extractId(serieUri));
	  				SerieEditActivity.super.finish(); //use super to avoid the implicite save by finish()
	  			}
				});
			}
			case DIALOG_CONFIRM_LEVEL_DELETE: {
				return Common.createConfirmDeleteDialog(this, R.string.confirm_delete_level, new DialogInterface.OnClickListener() {
	  			public void onClick (DialogInterface dialog, int id) {
	  				deleteLevel();
	  			}
				});
			}
			case DIALOG_RENAME: {
				return new NameDialog(this, DIALOG_RENAME, this, new NameDialog.OnOkListener() {
					@Override
					public void onOK(String enteredText) {
						try {
	            serieObj.put("name", enteredText);
	            serieName = enteredText;
	      	    TextView titleView = (TextView)findViewById(R.id.title);
	      	    titleView.setText(serieName);
	      	    save();
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
	
	private void deleteLevel () {
		//If we just created the level, we'll get a -1
		if (levelToDelete == -1) 
			return;
		
		try {
			JSONArray newArray = new JSONArray();
			// Copy all the objects between the beginning and min
			final JSONArray levelsArray = serieObj.getJSONArray("levels");
			for (int i = 0; i < levelsArray.length(); i++) {
				if (i != levelToDelete)
					newArray.put(levelsArray.getJSONObject(i));
			}
			serieObj.put("levels", newArray);
			notifyAdapter();
			levelToDelete = -1;
		} catch (JSONException e) {
      e.printStackTrace();
    }
	}
	
	
	class SerieAdapter extends BaseAdapter {				
		@Override
    public int getCount() {
			try {
	      return serieObj.getJSONArray("levels").length();
      } catch (JSONException e) {
	      e.printStackTrace();
	      return 0;
      }
    }

		@Override
    public Object getItem(int position) {
	    try {
	      return serieObj.getJSONArray("levels").get(position);
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
				view = getLayoutInflater().inflate(R.layout.serieedit_item, null);
			
			//TextView textName = (TextView)view.findViewById(R.id.name);
			MiniLevelView levelView = (MiniLevelView)view.findViewById(R.id.minilevel);
			TextView txtView = (TextView)view.findViewById(R.id.lvlnum);
			try {
	      JSONObject levelObj = serieObj.getJSONArray("levels").getJSONObject(position);
	      //textName.setText("Level " + position + " ["+levelObj.getInt("xdim")+","+levelObj.getInt("ydim")+"]");
	      levelView.setLevel(levelObj);
	      txtView.setText(""+position);
      } catch (JSONException e) {
      	TextView textView = new TextView(SerieEditActivity.this);
      	textView.setText("Error reading level");
	      e.printStackTrace();
	      return textView;	
      }
	    return view;
    }
		
	}
}
