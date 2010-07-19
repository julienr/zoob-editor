package net.fhtagn.zoobeditor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import net.fhtagn.zoobeditor.tools.EraseTool;
import net.fhtagn.zoobeditor.tools.PathTool;
import net.fhtagn.zoobeditor.tools.TankTool;
import net.fhtagn.zoobeditor.tools.WallTool;
import net.fhtagn.zoobeditor.utils.TankView;
import net.fhtagn.zoobeditor.utils.Types;
import net.fhtagn.zoobeditor.utils.WallView;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.AdapterView.OnItemClickListener;

public class Editor extends Activity {
	static final String TAG = "Editor";
	static final int DIALOG_WALL_ID = 0;
	static final int DIALOG_TANK_ID = 1;
	static final int DIALOG_SAVE_NAME = 2;
	static final int DIALOG_ERR_EXTERNAL = 3;
	static final int DIALOG_ERR_EXPORT = 4;
	static final int DIALOG_SAVE_SUCCESS = 5;
	
	static final String LEVELS_DIR_NAME = "levels";
	
	private LevelView levelView;
	
	private LinearLayout buttonsLayout;
	
	private PathTool pathTool = null;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		Intent i = getIntent();
		int xdim = i.getIntExtra("xdim", 12);
		int ydim = i.getIntExtra("ydim", 8);
		Log.i(TAG, "xdim="+xdim+", ydim="+ydim);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);    
		setContentView(R.layout.editor);
		
		buttonsLayout = (LinearLayout)findViewById(R.id.editor_btns);
		
		FrameLayout levelFrame = (FrameLayout)findViewById(R.id.levelframe);
		levelView = new LevelView(getApplicationContext(), xdim, ydim);
		levelFrame.addView(levelView);
		
		toNormalMode();
		levelView.requestFocus(); //force trackball focus on level view
	}
	
	private void toPathMode (PathTool tool) {
		pathTool = tool;
		buttonsLayout.removeAllViews();
		buttonsLayout.addView(getLayoutInflater().inflate(R.layout.editor_path_btns, null));
		levelView.postInvalidate();
		setupPathCallbacks();
	}
	
	private void toNormalMode () {
		pathTool = null;
		levelView.setEditorTool(null);
		buttonsLayout.removeAllViews();
		levelView.postInvalidate();
		buttonsLayout.addView(getLayoutInflater().inflate(R.layout.editor_normal_btns, null));
		setupNormalCallbacks();
	}
	
	//Path editing mode buttons
	private void setupPathCallbacks () {
		Button resetButton = (Button)findViewById(R.id.btn_path_reset);
		resetButton.setOnClickListener(new OnClickListener() {
			@Override
      public void onClick(View view) {
				pathTool.resetPath();
				levelView.postInvalidate();
      }
		});
		
		Button validateButton = (Button)findViewById(R.id.btn_path_validate);
		validateButton.setOnClickListener(new OnClickListener() {
			@Override
      public void onClick(View view) {
				pathTool.savePath();
				toNormalMode();
      }
		});
	}
	
	//Normal mode buttons
	private void setupNormalCallbacks () {
		Button wallButton = (Button)findViewById(R.id.btn_wall);
		wallButton.setOnClickListener(new OnClickListener() {
			@Override
      public void onClick(View view) {
				showDialog(DIALOG_WALL_ID);
      }
		});
		
		Button eraseButton = (Button)findViewById(R.id.btn_erase);
		eraseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				levelView.setEditorTool(new EraseTool());
			}
		});
		
		Button tankButton = (Button)findViewById(R.id.btn_tank);
		tankButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showDialog(DIALOG_TANK_ID);
			}
		});
		
		Button pathButton = (Button)findViewById(R.id.btn_path);
		pathButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				PathTool tool = new PathTool();
				levelView.setEditorTool(tool);
				toPathMode(tool);
			}
		});
		
		Button saveButton = (Button)findViewById(R.id.btn_save);
		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showDialog(DIALOG_SAVE_NAME);
			}
		});
	} 
	
	public void saveAs (String name) {
		String state = Environment.getExternalStorageState();
		File root = Environment.getExternalStorageDirectory();
		if (!Environment.MEDIA_MOUNTED.equals(state) || !root.canWrite()) {
			showDialog(DIALOG_ERR_EXTERNAL);
			return;
		}
		
		String jsonLevel = "";
		try {
	    jsonLevel = levelView.toJSON().toString();
    } catch (JSONException e) {
    	e.printStackTrace();
    	showDialog(DIALOG_ERR_EXPORT);
    	return;
    }
    
    File levelsDir = new File(root+File.separator+"zoob_levels");
    levelsDir.mkdirs();
    File levelFile = new File(levelsDir, name + ".json");
    FileWriter writer;
    try {
	    writer = new FileWriter(levelFile);
	    writer.write(jsonLevel);
	    writer.close();
    } catch (IOException e) {
	    e.printStackTrace();
	    showDialog(DIALOG_ERR_EXTERNAL);
	    return;
    }
    showDialog(DIALOG_SAVE_SUCCESS);
	}
	
	private class WallTypeAdapter extends BaseAdapter {
		private final Context context;
		public WallTypeAdapter (Context c) {
			context = c;
		}
		
		public int getCount () {
			return Types.WallType.values().length;
		}
		
		public Object getItem (int position) {
			return Types.WallType.values()[position];
		}
		
		@Override
    public long getItemId(int position) {
	    return position;
    }
		
		public View getView (int position, View convertView, ViewGroup parent) {
			Types.WallType type = Types.WallType.values()[position];
			WallView view;
			if (convertView != null) {
				view = (WallView)convertView;
				view.setType(type);
			} else {
				view = new WallView(context, type);
				view.setLayoutParams(new GridView.LayoutParams(50,50));
				view.setPadding(8,8,8,8);
			}
			return view;
		}
	}
	
	private class TankTypeAdapter extends BaseAdapter {
		private final Context context;
		public TankTypeAdapter (Context c) {
			context = c;
		}
		
		public int getCount () {
			return Types.TankType.values().length;
		}
		
		public Object getItem (int position) {
			return Types.TankType.values()[position];
		}
		
		@Override
		public long getItemId (int position) {
			return position;
		}
		
		public View getView (int position, View convertView, ViewGroup parent) {
			Types.TankType type = Types.TankType.values()[position];
			TankView view;
			if (convertView != null) {
				view = (TankView)convertView;
				view.setType(type); 
			} else {
				view = new TankView(context, type);
				view.setLayoutParams(new GridView.LayoutParams(50,50));
				view.setPadding(8,8,8,8);
			}
			return view;
		}
	}
	
	private Dialog createGridDialog (final int dialogId, int titleRes, BaseAdapter adapter, OnItemClickListener listener) {
		GridView gridView = (GridView)getLayoutInflater().inflate(R.layout.dlg_gridview, null).findViewById(R.id.gridview);
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(listener);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(titleRes)
		 			 .setView(gridView)
					 .setCancelable(true)
					 .setNegativeButton(android.R.string.cancel,
							 new DialogInterface.OnClickListener() {
						 	   public void onClick(DialogInterface dialog, int item) {
						 	  	 dismissDialog(dialogId);
						 	   }
					 		 });
		return builder.create();		
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
	
	protected Dialog onCreateDialog (int id) {
		switch (id) {
			case DIALOG_WALL_ID: 
				return createGridDialog(DIALOG_WALL_ID, R.string.editor_wall_dlg_title,
				    new WallTypeAdapter(getApplicationContext()),
				    new OnItemClickListener() {
					    @Override
					    public void onItemClick(AdapterView<?> parent, View view,
					        int position, long id) {
						    WallView cellView = (WallView) view;
						    levelView.setEditorTool(new WallTool(cellView.getType()));
						    dismissDialog(DIALOG_WALL_ID);
					    }
				    });
			case DIALOG_TANK_ID: {
				return createGridDialog(DIALOG_TANK_ID, R.string.editor_tank_dlg_title,
				    new TankTypeAdapter(getApplicationContext()),
				    new OnItemClickListener() {
					    @Override
					    public void onItemClick(AdapterView<?> parent, View view,
					        int position, long id) {
						    TankView cellView = (TankView) view;
						    levelView.setEditorTool(new TankTool(getApplicationContext(), cellView.getType()));
						    dismissDialog(DIALOG_TANK_ID);
					    }
				    });
			}
			case DIALOG_SAVE_NAME: {
				return new SaveDialog(this, DIALOG_SAVE_NAME, this);
			}
			case DIALOG_ERR_EXTERNAL:
				return createErrorDialog(DIALOG_ERR_EXTERNAL, R.string.save_err_external);
			case DIALOG_ERR_EXPORT:
				return createErrorDialog(DIALOG_ERR_EXPORT, R.string.save_err_export);
			case DIALOG_SAVE_SUCCESS: 
				return createErrorDialog(DIALOG_SAVE_SUCCESS, R.string.save_success);
			default:
				return null;
		}
	}
}
