package net.fhtagn.zoobeditor.editor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import net.fhtagn.zoobeditor.Common;
import net.fhtagn.zoobeditor.ExternalStorageException;
import net.fhtagn.zoobeditor.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.commonsware.cwac.tlv.TouchListView;
import com.commonsware.cwac.tlv.TouchListView.DragListener;
import com.commonsware.cwac.tlv.TouchListView.DropListener;
import com.commonsware.cwac.tlv.TouchListView.RemoveListener;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SerieEditActivity extends ListActivity {
	static final String TAG = "SerieEditActivity";
	static final int DIALOG_NEWLVL_ID = 0;
	static final int DIALOG_ERR_EXTERNAL = 1;
	static final int DIALOG_ERR_EXPORT = 2;
	
	static final int REQUEST_LEVEL_EDITOR = 1;
	
	private JSONObject serieObj;
	private JSONArray levelsArray;
	
	@Override
	public void onCreate (Bundle bundle) {
		super.onCreate(bundle);
		Intent i = getIntent();
		if (i == null)
			Log.e("SerieEditActivity", "NULL INTENT");
    try {
    	String serieString = i.getStringExtra("serie");
    	if (serieString == null)
    		throw new JSONException("null serie string");
	    serieObj = new JSONObject(serieString);
   
	    setContentView(R.layout.serieedit);
	    
			//Create levels array if not yet existing
			JSONArray arr = serieObj.optJSONArray("levels");
			if (arr == null) {
				arr = new JSONArray();
				serieObj.put("levels", arr);
			}
			levelsArray  = arr;
	    
	    setListAdapter(new SerieAdapter());
	    
	    /** Drag'n'drop reordering */
	    TouchListView listView = (TouchListView)getListView();
	    listView.setDragListener(new DragListener() {
				@Override
        public void drag(int from, int to) {
					//FIXME: highlight the place where it would end ?
        }
	    });
	    
	    //Remove is disallowed
	    /*listView.setRemoveListener(new RemoveListener() {
				@Override
        public void remove(int which) {
	        Log.i(TAG, "remove " + which);
        }
	    });*/
	    
	    listView.setDropListener(new DropListener() {
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
	
	private void moveLevel (int from, int to) {
		if (from == to)
			return;
		try {
      JSONArray newArray = new JSONArray();
      //Copy all the objects between the beginning and min
      for (int i=0; i<levelsArray.length(); i++) {
      	if (i == from) {
      		
      	} else if (i == to) {
      		newArray.put(levelsArray.getJSONObject(from));
      		newArray.put(levelsArray.getJSONObject(i));
      	} else {
      		newArray.put(levelsArray.getJSONObject(i));
      	}
      }
      serieObj.put("levels", newArray);
      levelsArray = newArray;
      notifyAdapter();
    } catch (JSONException e) {
    	Log.e(TAG, "Error in moveLevel from " + from + " to " + to + " : " + e.getMessage());
      e.printStackTrace();
    }
	} 
	
	public void save () {
		String name;
		File levelsDir;
		try {
			levelsDir = Common.getLevelsDir();
		} catch (ExternalStorageException e) {
			showDialog(DIALOG_ERR_EXTERNAL);
			return;
		}
		
		String jsonSerie = "";
		try {
			name = Common.removeSpecialCharacters(serieObj.getString("name"));
	    jsonSerie = serieObj.toString();
    } catch (JSONException e) {
    	e.printStackTrace();
    	showDialog(DIALOG_ERR_EXPORT);
    	return;
    }
    
    File levelFile = new File(levelsDir, name + ".json");
    FileWriter writer;
    try {
	    writer = new FileWriter(levelFile);
	    writer.write(jsonSerie);
	    writer.close();
	    Log.i(TAG, "Saved serie to : " + name);
    } catch (IOException e) {
	    e.printStackTrace();
	    showDialog(DIALOG_ERR_EXTERNAL);
	    return;
    }
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
	
	private Dialog createErrorDialog (final int dialogId, int messageRes) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(messageRes)
					 .setCancelable(false)
					 .setNeutralButton(android.R.string.ok,  new DialogInterface.OnClickListener() {
						 	   public void onClick(DialogInterface dialog, int item) {
						 	  	 dismissDialog(dialogId);
						 	   }
					 		 });
		return builder.create();
	}
	
	@Override
	protected Dialog onCreateDialog (int id) {
		switch (id) {
			case DIALOG_NEWLVL_ID: {
				final CharSequence[] items = {"8x8", "9x8"};
				final int[][] itemsDim = {{8,8},{9,8}};
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.newlvl_dlg_title)
							 .setItems(items,
							    new DialogInterface.OnClickListener() {
								    @Override
								    public void onClick(DialogInterface dialog, int item) {
								    	launchEditorNew(itemsDim[item][0], itemsDim[item][1]);
								    }
							    })
							  .setCancelable(true)
							  .setNegativeButton(android.R.string.cancel,
							    new DialogInterface.OnClickListener() {
								    public void onClick(DialogInterface dialog, int id) {
									    dialog.cancel();
								    }
								  });
				return builder.create();
			}
			case DIALOG_ERR_EXTERNAL: {
				return createErrorDialog(DIALOG_ERR_EXTERNAL, R.string.save_err_external);
			}
			case DIALOG_ERR_EXPORT: {
				return createErrorDialog(DIALOG_ERR_EXPORT, R.string.save_err_export);
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
