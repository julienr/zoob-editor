package net.fhtagn.zoobeditor.editor;

import com.quietlycoding.widget.NumberPicker;

import net.fhtagn.zoobeditor.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;

public class LevelSizeDialog extends AlertDialog {
	public interface OnOkListener {
		public void onOK (int xdim, int ydim);
	}
	
	private final OnOkListener listener;
	
	public LevelSizeDialog(Context context, final int thisID, final Activity sourceActivity, final OnOkListener listener) {
	  super(context);
	  this.listener = listener;
	  
	  View view = this.getLayoutInflater().inflate(R.layout.dlg_lvlsize, null);
	  this.setView(view);
	  
	  this.setTitle(R.string.lvlsize_dlg_title);
	  this.setButton(AlertDialog.BUTTON_NEGATIVE, context.getResources().getString(android.R.string.cancel), new OnClickListener() {
	 	   public void onClick(DialogInterface dialog, int item) {
	 	  	 sourceActivity.dismissDialog(thisID);
	 	   }
		 });
	  
	  final NumberPicker widthPicker = (NumberPicker)view.findViewById(R.id.width);
	  widthPicker.setRange(4, 12);
	  widthPicker.setWrap(false);
	  
	  final NumberPicker heightPicker = (NumberPicker)view.findViewById(R.id.height);
	  heightPicker.setRange(4, 8);
	  heightPicker.setWrap(false);
	  
	  
	  this.setButton(AlertDialog.BUTTON_POSITIVE, context.getResources().getString(android.R.string.ok), new OnClickListener() {
	 	   public void onClick(DialogInterface dialog, int item) {
	 	  	 sourceActivity.dismissDialog(thisID);
	 	  	 listener.onOK(widthPicker.getCurrent(), heightPicker.getCurrent());
	 	   }
		 });
  }
}
