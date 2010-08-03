package net.fhtagn.zoobeditor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Intent;
import android.net.Uri;

public class Common {	
	static final String TAG = "Common";
	
	public static final String LEVELS_DIR_NAME = "zoob_levels";
	public static final String MY_LEVELS_SUBDIR = "mylevels";
	public static final String ONLINE_LEVELS_SUBDIR = "community";
	
	public static Intent playSerie (long id) {
 		Uri.Builder builder = new Uri.Builder();
		builder.scheme("content");
		builder.authority("net.fhtagn.zoobgame");
		builder.path("/series/"+id);
		Intent i = new Intent("net.fhtagn.zoobgame.PLAY", builder.build());
		return i;
	}
	
	public static String removeSpecialCharacters(String s) {
	  return s.replaceAll("\\W", "");
	}

	public static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

}
