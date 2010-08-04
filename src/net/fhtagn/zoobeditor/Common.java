package net.fhtagn.zoobeditor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Intent;
import android.net.Uri;
import android.text.format.DateFormat;

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
	
	public static long extractId (Uri serieUri) {
		return Long.parseLong(serieUri.getLastPathSegment());
	}
	
	public static Intent playeSerie (long serieId, int level) {
		Uri.Builder builder = new Uri.Builder();
		builder.scheme("content");
		builder.authority("net.fhtagn.zoobgame");
		builder.path("/series/"+serieId);
		builder.appendQueryParameter("startlevel", ""+level);
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
	
	private static final SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static Date dateFromDB(String date) {
		try {
	    return iso8601Format.parse(date);
    } catch (ParseException e) {
	    e.printStackTrace();
	    return new Date();
    }
	}

}
