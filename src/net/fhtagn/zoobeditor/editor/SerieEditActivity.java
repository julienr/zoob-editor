package net.fhtagn.zoobeditor.editor;

import net.fhtagn.zoobeditor.R;
import net.fhtagn.zoobeditor.Series;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.apps.music.TouchInterceptor;

public class SerieEditActivity extends ListActivity {
	static final String TAG = "SerieEditActivity";
	static final int DIALOG_NEWLVL_ID = 0;
	
	static final int REQUEST_LEVEL_EDITOR = 1;
	
	private JSONObject serieObj;
	private JSONArray levelsArray;
	
	private TextView debugText;
	
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
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);    
	    
	    debugText = (TextView)findViewById(R.id.debugmsg);
	    
			//Create levels array if not yet existing
			JSONArray arr = serieObj.optJSONArray("levels");
			if (arr == null) {
				arr = new JSONArray();
				serieObj.put("levels", arr);
			}
			levelsArray  = arr;
	    
	    setListAdapter(new SerieAdapter());
	    
	    /** Drag'n'drop reordering */
	    TouchInterceptor listView = (TouchInterceptor)getListView();
	    /*listView.setDragListener(new DragListener() {
				@Override
        public void drag(int from, int to) {
					//FIXME: highlight the place where it would end ?
        }
	    });*/
	    
	    //Remove is disallowed
	    /*listView.setRemoveListener(new RemoveListener() {
				@Override
        public void remove(int which) {
	        Log.i(TAG, "remove " + which);
        }
	    });*/
	    
	    listView.setDropListener(new TouchInterceptor.DropListener() {
				@Override
        public void drop(int from, int to) {
					debugText.setText("Drop from  " + from + " to " + to);
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
	
	private void moveLevel (int from, int to) {
		if (from == to)
			return;
		try {
      JSONArray newArray = new JSONArray();
      //Copy all the objects between the beginning and min
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
      levelsArray = newArray;
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
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		if (requestCode != REQUEST_LEVEL_EDITOR)
			return;
		
		if (resultCode == RESULT_OK) {
			String levelJSON = data.getStringExtra("json");
			int levelNumber = data.getIntExtra("number", -1);
			try {
	      JSONObject levelObj = new JSONObject(levelJSON);
	      if (levelNumber != -1)
	      	levelsArray.put(levelNumber, levelObj);
	      else
	      	levelsArray.put(levelObj);
	      notifyAdapter();
      } catch (JSONException e) {
	      e.printStackTrace();
	      return;
      }
		} else { //RESULT_CANCELED
			
		}
	}
	
	public void notifyAdapter () {
		save();
    SerieAdapter adapter = (SerieAdapter)getListAdapter();
    adapter.notifyDataSetChanged();
	}
	
	private void doLaunch (int position, JSONObject obj) {
		Intent i = new Intent(this, EditorActivity.class);
		i.putExtra("json", obj.toString());
		i.putExtra("number", position);
		startActivityForResult(i, REQUEST_LEVEL_EDITOR);
	}
	
	protected void launchEditor (int position) {
		try {
	    JSONObject obj = levelsArray.getJSONObject(position);
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
			default:
				return null;
		}
	}
	
	
	class SerieAdapter extends BaseAdapter {				
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
				view = getLayoutInflater().inflate(R.layout.serieedit_item, null);
			
			//TextView textName = (TextView)view.findViewById(R.id.name);
			MiniLevelView levelView = (MiniLevelView)view.findViewById(R.id.minilevel);
			try {
	      JSONObject levelObj = levelsArray.getJSONObject(position);
	      //textName.setText("Level " + position + " ["+levelObj.getInt("xdim")+","+levelObj.getInt("ydim")+"]");
	      levelView.setLevel(levelObj);
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
