package net.fhtagn.zoobeditor.browser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class OnlineSeriesActivity extends Activity {
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Button loginButton = new Button(this);
		loginButton.setText("Login");
		loginButton.setOnClickListener(new OnClickListener() {
			@Override
      public void onClick(View arg0) {
				Intent i = new Intent(getApplicationContext(), AccountList.class);
				startActivity(i); 
      }
			
		});
		setContentView(loginButton);
	}
}
