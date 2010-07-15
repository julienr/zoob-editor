package net.fhtagn.zoobeditor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainMenu extends Activity {
	static final String TAG = "ZoobEditor";
	static final int DIALOG_NEWLVL_ID = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Button newLvlBtn = (Button) findViewById(R.id.btn_newlevel);
		newLvlBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i(TAG, "new level");
				showDialog(DIALOG_NEWLVL_ID);
			}
		});

	}
	
	protected void launchEditor (int xdim, int ydim) {
		Intent i = new Intent(getApplicationContext(), Editor.class);
  	i.putExtra("xdim", xdim);
  	i.putExtra("ydim", ydim);
  	this.startActivity(i);
	}
	
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
								    	launchEditor(itemsDim[item][0], itemsDim[item][1]);
								    }
							    })
							  .setCancelable(true)
							  .setNegativeButton(R.string.cancel,
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
}