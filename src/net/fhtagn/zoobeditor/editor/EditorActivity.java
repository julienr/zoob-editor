package net.fhtagn.zoobeditor.editor;

import net.fhtagn.zoobeditor.Common;
import net.fhtagn.zoobeditor.editor.tools.EraseTool;
import net.fhtagn.zoobeditor.editor.tools.PathTool;
import net.fhtagn.zoobeditor.editor.tools.TankTool;
import net.fhtagn.zoobeditor.editor.tools.WallTool;
import net.fhtagn.zoobeditor.editor.utils.TankView;
import net.fhtagn.zoobeditor.editor.utils.Types;
import net.fhtagn.zoobeditor.editor.utils.WallView;

import org.json.JSONException;
import org.json.JSONObject;

import net.fhtagn.zoobeditor.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class EditorActivity extends Activity {
	static final String TAG = "Editor";
	static final int DIALOG_WALL_ID = 0;
	static final int DIALOG_TANK_ID = 1;
	
	private LevelView levelView;
	
	private LinearLayout buttonsLayout;
	
	private PathTool pathTool = null;
	
	private int levelNumber;
	
	private Uri serieUri;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		Intent i = getIntent();
		levelNumber = i.getIntExtra("number", -1);
		serieUri = i.getData();
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);    
		setContentView(R.layout.editor);
		
		buttonsLayout = (LinearLayout)findViewById(R.id.editor_btns);
		
		FrameLayout levelFrame = (FrameLayout)findViewById(R.id.levelframe);
		try {
			JSONObject levelObj = new JSONObject(i.getStringExtra("json"));
			levelView = new LevelView(getApplicationContext(), levelObj);
			levelFrame.addView(levelView);
			toNormalMode();
			levelView.requestFocus(); //force trackball focus on level view
		} catch (JSONException e) {
			TextView textView = new TextView(this);
			textView.setText("Error loading level from JSON : " + e.getMessage());
			levelFrame.addView(textView);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.editor_menu, menu);
		Common.createCommonOptionsMenu(this, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected (MenuItem item) {
		switch (item.getItemId()) {
			case R.id.save:
				saveToSerie();
				return true;
			case R.id.play:
				Intent i = Common.playeSerie(Common.extractId(serieUri), levelNumber);
        startActivity(i);
				return true;
			case R.id.help:
				return true;
		}
		return Common.commonOnOptionsItemSelected(this, item);
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
	}
	
	public void saveToSerie () {
		Intent i = new Intent(this, SerieEditActivity.class);
		try {
			i.putExtra("json", levelView.toJSON().toString());
			i.putExtra("number", levelNumber);
			setResult(RESULT_OK, i);
		} catch (JSONException e) {
			e.printStackTrace();
			//FIXME: display error to user ?
			setResult(RESULT_CANCELED);
		}
		finish();
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
	
	@Override
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
			default:
				return null;
		}
	}
}
