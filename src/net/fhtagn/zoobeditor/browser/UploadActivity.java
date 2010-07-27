package net.fhtagn.zoobeditor.browser;

import net.fhtagn.zoobeditor.EditorConstants;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class UploadActivity extends Activity {
	static final int REQUEST_GOOGLE_LOGIN = 1;
	
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Button loginButton = new Button(this);
		loginButton.setText("Login");
		loginButton.setOnClickListener(new OnClickListener() {
			@Override
      public void onClick(View arg0) {
				Intent i = new Intent(getApplicationContext(), GoogleLoginActivity.class);
				startActivityForResult(i, REQUEST_GOOGLE_LOGIN); 
      }
		});
		setContentView(loginButton);
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, final Intent results) {
		switch (requestCode) {
			case REQUEST_GOOGLE_LOGIN: {
				if (resultCode == RESULT_OK) {
					//TODO: login OK, get results.getExtra("token") for auth token
				} else {
					//TODO: login failed
				}
			}
			default:
				Log.w(EditorConstants.TAG, "Warning unhandled request code : " + requestCode);
		}
	}
	
}
