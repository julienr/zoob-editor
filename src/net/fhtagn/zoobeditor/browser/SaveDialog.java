package net.fhtagn.zoobeditor.browser;

import net.fhtagn.zoobeditor.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SaveDialog extends AlertDialog {
	
	public interface OnOkListener {
		public void onOK (String enteredText);
	}
	
	private final OnOkListener listener;

	protected SaveDialog(Context context, final int thisID, final Activity sourceActivity, final OnOkListener listener) {
	  super(context);
	  
	  this.listener = listener;
	  
	  View view = this.getLayoutInflater().inflate(R.layout.dlg_save, null);
	  this.setView(view);
	  
	  this.setTitle(R.string.dlg_serie_name_title);
	  
	  this.setButton(AlertDialog.BUTTON_NEGATIVE, context.getResources().getString(android.R.string.cancel), new OnClickListener() {
	 	   public void onClick(DialogInterface dialog, int item) {
	 	  	 sourceActivity.dismissDialog(thisID);
	 	   }
 		 });
	  
	  final EditText editText = (EditText)view.findViewById(R.id.name);
	  
	  this.setButton(AlertDialog.BUTTON_POSITIVE, context.getResources().getString(android.R.string.ok), new OnClickListener() {
	 	   public void onClick(DialogInterface dialog, int item) {
	 	  	 sourceActivity.dismissDialog(thisID);
	 	  	 listener.onOK(editText.getText().toString());
	 	   }
 		 });
	  
	  
	  editText.addTextChangedListener(new TextWatcher() {
			@Override
      public void afterTextChanged(Editable editable) {
				Button okButton = SaveDialog.this.getButton(AlertDialog.BUTTON_POSITIVE);
				if (okButton == null)
					return;
				
				if (editText.getText().length() == 0)
					okButton.setEnabled(false);
				else
					okButton.setEnabled(true);
			}
			
			@Override
      public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			
			@Override
      public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
	  	
	  });
  }
	
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Button okButton = SaveDialog.this.getButton(AlertDialog.BUTTON_POSITIVE);
		if (okButton != null)
			okButton.setEnabled(false);
	}

}
