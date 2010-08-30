package net.fhtagn.zoobeditor.editor;

import java.util.ArrayList;
import java.util.List;

import net.fhtagn.zoobeditor.Common;
import net.fhtagn.zoobeditor.EditorApplication;
import net.fhtagn.zoobeditor.EditorConstants;
import net.fhtagn.zoobeditor.editor.LevelView.UndoListener;
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
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class EditorActivity extends Activity {
	//returned if the user requested the level's deletion. The calling activity
	//should proceed with the deleteion
	public static final int RESULT_DELETE = Activity.RESULT_FIRST_USER;
	
	static final String TAG = "Editor";
	static final int DIALOG_TOOL_TYPE = 1;
	static final int DIALOG_CONFIRM_DELETE = 2;
	
	private LevelView levelView;
	
	private LinearLayout buttonsLayout;
	
	private PathTool pathTool = null;
	
	private int levelNumber;
	
	private Uri serieUri;
	
	private Button undoButton = null;
	
	private UndoListener undoListener = new UndoListener() {
		@Override
		public void enableUndo(boolean enabled) {
			if (undoButton != null)
				undoButton.setEnabled(enabled);
		}
	};
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		if (!((EditorApplication)getApplication()).checkZoob(this)) {
			finish();
			return;
		}
		
		Intent i = getIntent();
		if (!i.hasExtra("json")) { 
			//FIXME: ONLY FOR DEBUG
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);    
			setContentView(R.layout.editor);
			
			buttonsLayout = (LinearLayout)findViewById(R.id.editor_btns);
			
			FrameLayout levelFrame = (FrameLayout)findViewById(R.id.levelframe);
			try {
				JSONObject levelObj = new JSONObject();
				levelObj.put("xdim", 12);
				levelObj.put("ydim", 8);
				levelView = new LevelView(getApplicationContext(), levelObj, undoListener);
				levelFrame.addView(levelView);
				toNormalMode();
				levelView.requestFocus(); //force trackball focus on level view
			} catch (JSONException e) {
				TextView textView = new TextView(this);
				textView.setText("Error loading level from JSON : " + e.getMessage());
				levelFrame.addView(textView);
			}
		} else {
			levelNumber = i.getIntExtra("number", -1);
			serieUri = i.getData();
			
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);    
			setContentView(R.layout.editor);
			
			buttonsLayout = (LinearLayout)findViewById(R.id.editor_btns);
			
			FrameLayout levelFrame = (FrameLayout)findViewById(R.id.levelframe);
			try {
				JSONObject levelObj = new JSONObject(i.getStringExtra("json"));
				levelView = new LevelView(getApplicationContext(), levelObj, undoListener);
				levelFrame.addView(levelView);
				toNormalMode();
				levelView.requestFocus(); //force trackball focus on level view
			} catch (JSONException e) {
				TextView textView = new TextView(this);
				textView.setText("Error loading level from JSON : " + e.getMessage());
				levelFrame.addView(textView);
			}
		}
	}
	
	@Override
	public void finish () {
		returnToSerie(RESULT_OK);
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
			case R.id.advanced: {
				Intent i = new Intent(getApplicationContext(), LevelOptionsActivity.class);
				try {
	        i.putExtra("json", levelView.toJSON().toString());
        } catch (JSONException e) {
	        e.printStackTrace();
	        return false;
        }
        startActivityForResult(i, EditorConstants.REQUEST_LEVEL_OPTIONS);
				return true;
			}
			case R.id.play: {
				returnToSerie(EditorConstants.RESULT_OK_PLAY);
				return true;
			}
			case R.id.delete: {
				showDialog(DIALOG_CONFIRM_DELETE);
				return true;
			}
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
		toWallMode();
	}
	
	//Path editing mode buttons
	private void setupPathCallbacks () {
		undoButton = null;
		
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
		Button toolButton = (Button)findViewById(R.id.changetool);
		toolButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showDialog(DIALOG_TOOL_TYPE);
			}
		});
		
		undoButton = (Button)findViewById(R.id.undo);
		undoButton.setEnabled(levelView.canUndo());
		undoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick (View view) {
				levelView.undo();
			}
		});
	}
	
	public void returnToSerie (int resultCode) {
		Intent i = new Intent(this, SerieEditActivity.class);
		try {
			i.putExtra("json", levelView.toJSON().toString());
			i.putExtra("number", levelNumber);
			setResult(resultCode, i);
		} catch (JSONException e) {
			e.printStackTrace();
			//FIXME: display error to user ?
			setResult(RESULT_CANCELED);
		}
		super.finish();
	}
	
	private void toWallMode () {
		RadioGroup toolsLayout = (RadioGroup)findViewById(R.id.tools);
		toolsLayout.removeAllViews();
		toolsLayout.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
      public void onCheckedChanged(RadioGroup group, int btnId) {
				if (btnId == -1)
					return;
				Types.WallType type = Types.WallType.values()[btnId];
				levelView.setEditorTool(new WallTool(type));
      }
		});
		final int len = Types.WallType.values().length;
		for (int i=0; i<len; i++) {
			Types.WallType type = Types.WallType.values()[i];
			WallView view = new WallView(EditorActivity.this, type);
			view.setId(i); //ids are used in the onchecklistener to identify the clicked type
			view.setLayoutParams(new RadioGroup.LayoutParams(50, 50));
			view.setPadding(8,8,8,8);
			toolsLayout.addView(view);
		}
	}
	
	private void toTankMode () {
		RadioGroup toolsLayout = (RadioGroup)findViewById(R.id.tools);
		toolsLayout.removeAllViews();
		toolsLayout.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
      public void onCheckedChanged(RadioGroup group, int btnId) {
				if (btnId == -1)
					return;
				Types.TankType type = Types.TankType.values()[btnId];
				levelView.setEditorTool(new TankTool(EditorActivity.this, type));
      }
		});
		final int len = Types.TankType.values().length;
		for (int i=0; i<len; i++) {
			Types.TankType type = Types.TankType.values()[i];
			TankView view = new TankView(EditorActivity.this, type);
			view.setId(i); //ids are used in the onchecklistener to identify the clicked type
			view.setLayoutParams(new RadioGroup.LayoutParams(50, 50));
			view.setPadding(8,8,8,8);
			toolsLayout.addView(view);
		}
	}
	
	private void toPathMode () {
		PathTool tool = new PathTool();
		levelView.setEditorTool(tool);
		toPathMode(tool);
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case EditorConstants.REQUEST_LEVEL_OPTIONS: {
				if (resultCode != RESULT_OK) {
					Log.e(TAG, "REQUEST_LEVEL_OPTIONS: unhandled resultCode = " + resultCode);
					return;
				}
				if (!data.hasExtra("json")) {
					Log.e(TAG, "REQUEST_LEVEL_OPTIONS : got not json in response");
					return;
				}
				try {
	        JSONObject obj = new JSONObject(data.getStringExtra("json"));
	        levelView = new LevelView(getApplicationContext(), obj, undoListener);
        } catch (JSONException e) {
	        e.printStackTrace();
        }
			}
		}
	}
	
	@Override
	protected Dialog onCreateDialog (int id) {
		switch (id) {
			case DIALOG_TOOL_TYPE: {
				final Resources r = this.getResources();
				final CharSequence[] items = {r.getString(R.string.btn_wall), 
																			r.getString(R.string.btn_tank),
																			r.getString(R.string.btn_path)};

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Select a tool");
				builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						dialog.dismiss();
						if (item == 0) { //Wall
							toWallMode();
						} else if (item == 1) { //Tank
							toTankMode();
						} else { //Path
							toPathMode();
						}
				  }
				});
				AlertDialog alert = builder.create();
				return alert;
			}
			case DIALOG_CONFIRM_DELETE: {
				return Common.createConfirmDeleteDialog(this, R.string.confirm_delete_level, new DialogInterface.OnClickListener() {
	  			public void onClick (DialogInterface dialog, int id) {
	  				setResult(RESULT_DELETE);
	  				EditorActivity.super.finish(); //use super to avoid the implicit save by finish()
	  			}
				});
			}
			default:
				return null;
		}
	}
}
