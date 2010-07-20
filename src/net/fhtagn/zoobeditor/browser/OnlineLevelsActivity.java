package net.fhtagn.zoobeditor.browser;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class OnlineLevelsActivity extends Activity {
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		TextView textView = new TextView(this);
		textView.setText("Online levels");
		setContentView(textView);
	}
}
