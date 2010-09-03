package net.fhtagn.zoobeditor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import net.fhtagn.zoobeditor.browser.DeleteActivity;
import net.fhtagn.zoobeditor.browser.RateActivity;
import net.fhtagn.zoobeditor.browser.UploadActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.RatingBar;

public class Common {	
	static final String TAG = "Common";
	
	public static final String LEVELS_DIR_NAME = "zoob_levels";
	public static final String MY_LEVELS_SUBDIR = "mylevels";
	public static final String ONLINE_LEVELS_SUBDIR = "community";
	
	public static final String ZOOB_PACKAGE = "net.fhtagn.zoob_demo";
	public static final String ZOOB_MAINCLASS = "net.fhtagn.zoobgame.Zoob";
	
	public static final int dp2pixels (Context ctx, float dp) { 
		final float scale = ctx.getResources().getDisplayMetrics().density;
		return (int)(scale*dp);
	}
	
	public static boolean isZoobInstalled (Context ctx) {
		try {
	    ctx.getPackageManager().getPackageInfo(ZOOB_PACKAGE, 0);
	    Log.i(TAG, "zoob found");
	    return true;
    } catch (NameNotFoundException e) {
    	Log.i(TAG, "zoob not found");
    	return false;
    } 
	}

	
	public static Intent playSerie (long id) {
		Intent i = new Intent("net.fhtagn.zoobgame.PLAY", ContentUris.withAppendedId(Series.CONTENT_URI, id));
		i.setClassName(ZOOB_PACKAGE, ZOOB_MAINCLASS);
		return i;
	}
	
	public static long extractId (Uri serieUri) {
		return Long.parseLong(serieUri.getLastPathSegment());
	}
	
	public static Intent playSerie (long serieId, int level) {
		Uri.Builder builder = new Uri.Builder();
		builder.scheme("content");
		builder.authority("net.fhtagn.zoobgame");
		builder.path("/series/"+serieId);
		builder.appendQueryParameter("startlevel", ""+level);
		Intent i = new Intent("net.fhtagn.zoobgame.PLAY", builder.build());
		i.setClassName(ZOOB_PACKAGE, ZOOB_MAINCLASS);
		return i;
	}
	
	public static Intent playLevel (String json) {
		Intent i = new Intent("net.fhtagn.zoobgame.PLAY", Uri.parse("content://net.fhtagn.zoobgame.SerieContentProvider/level"));
		i.putExtra("json", json);
		i.setClassName(ZOOB_PACKAGE, ZOOB_MAINCLASS);
		return i;
	}
	
	public static String removeSpecialCharacters(String s) {
	  return s.replaceAll("\\W", "");
	}
	
	public static String readFromAssets(Activity activity, String fileName) {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(activity.getAssets().open(fileName)));
			String line;
			StringBuilder buffer = new StringBuilder();
			while ((line = in.readLine()) != null)
				buffer.append(line).append('\n');
			return buffer.toString();
		} catch (IOException e) {
			return "Error loading " + fileName + "from assets.";
		} finally {
			//Close stream
			if (in != null) {
	      try {
	        in.close();
        } catch (IOException e) {
        	//ignore
	        e.printStackTrace();
        }
			}
		}
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
	
	public static String dateToDB (Date date) {
		return iso8601Format.format(date);
	}
	
	public static Date getUTCTime () {
		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		return cal.getTime();
	}
	
	//Returns true if d1 is at least MILLIS_EPSILON before d2
	public static long MILLIS_EPSILON = 1000*60; //1 minute
	public static boolean epsilonBefore (Date d1, Date d2) {
		final long diff = d2.getTime() - d1.getTime();
		return diff > MILLIS_EPSILON;
	}
	
	/*
	* @return boolean return true if the application can access the internet
	*/
	public static boolean hasInternet(Context ctx){
		NetworkInfo info=((ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		if(info==null || !info.isConnected()){
			return false;
		}
		if(info.isRoaming()){
			//here is the roaming option you can change it if you want to disable internet while roaming, just return false
			//FIXME: add a preference for that
			return true;
		}
		return true;
	}
	
	//Run a GET query at the specified url and returns the content. returns null if status code != 200 or on error
	public static String urlQuery (HttpClient httpClient, String url) {
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse response = httpClient.execute(httpGet);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity entity = response.getEntity();
				if (entity == null) {
					Log.e(TAG, "urlQuery() : Entity = null");
					return null; 
				}
				InputStream instream = entity.getContent();
				return Common.convertStreamToString(instream);
			} else {
				Log.e(TAG, "Error during urlQuery : " + response.getStatusLine().getStatusCode());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	
	//These two functions can be used to create and handle the common options
	//menu that should be available on all menu
	public static void createCommonOptionsMenu (Activity activity, Menu menu) {
		MenuInflater inflater = activity.getMenuInflater();
		inflater.inflate(R.menu.base_menu, menu);
	}
	
	public static boolean commonOnOptionsItemSelected (Activity activity, MenuItem item) {
		switch (item.getItemId()) {
			case R.id.prefs:
				Intent i = new Intent(activity, Preferences.class);
				activity.startActivity(i);
				return true;
			case R.id.help:
				showHelp(activity);
				return true;
		}
		return false;
	}
	
	public static void showHelp (Activity activity) {
		Dialog dialog = createHtmlDialog(activity, R.string.help_title, R.layout.html_dialog, Common.readFromAssets(activity, "help.html"));
		dialog.setOwnerActivity(activity);
		dialog.show();
	}
	
	public static Dialog createHtmlDialog (Activity activity, int titleRes, int layoutRes, String html) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(titleRes);
		LayoutInflater inflater = (LayoutInflater)activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(layoutRes, null);
		
		WebView webView = (WebView)view.findViewById(R.id.webview);
		webView.setBackgroundColor(Color.TRANSPARENT);
		webView.loadData(html, "text/html", "utf-8");
		
		builder.setView(view);
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
      public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
      }
		});
		return builder.create();
	}
	
	public static Dialog createConfirmDeleteDialog (Activity activity, int msgID, DialogInterface.OnClickListener listener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(R.string.delete_dlg_title)
					 .setMessage(msgID)
					  .setCancelable(true)
					  .setPositiveButton(R.string.ok, listener)
					  .setNegativeButton(R.string.cancel,
					    new DialogInterface.OnClickListener() {
						    public void onClick(DialogInterface dialog, int id) {
							    dialog.dismiss();
						    }
						  });
		return builder.create();
	}
	
	public static Dialog createRateDialog (final Activity activity, final long serieID) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		final RatingBar ratingBar = new RatingBar(activity);
		ratingBar.setNumStars(5);
		builder.setTitle(R.string.dlg_rate_title)
					 .setCancelable(true)
					 .setView(ratingBar)
					 .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						 public void onClick (DialogInterface dialog, int id) {
							 dialog.dismiss();
							 Intent i = new Intent(activity.getApplicationContext(), RateActivity.class);
							 i.putExtra("community_id", serieID);
							 i.putExtra("rating", ratingBar.getRating());
							 activity.startActivityForResult(i, EditorConstants.REQUEST_RATE);
						 }
					 });
		return builder.create();
	}
	
	public static final class Level {
		public static final void play (Activity sourceActivity, long serieID, int level, int requestCode) {
			Intent i = Common.playSerie(serieID, level);
	    sourceActivity.startActivityForResult(i, requestCode);
		}
	}
	
	//Regroup common actions on series
	public static final class Serie {
		public static final void upload (Activity sourceActivity, long serieID) {
			Intent i = new Intent(sourceActivity.getApplicationContext(), UploadActivity.class);
			i.putExtra("id", serieID);
			sourceActivity.startActivityForResult(i, EditorConstants.REQUEST_UPLOAD);
		}
		
		public static final void play (Activity sourceActivity, long serieID) {
			Intent i = Common.playSerie(serieID);
	    sourceActivity.startActivity(i);
		}
		
		public static final void deleteSerie (Activity sourceActivity, long serieID) {
			Intent i = new Intent(sourceActivity.getApplicationContext(), DeleteActivity.class);
			i.putExtra("id", serieID);
			sourceActivity.startActivityForResult(i, EditorConstants.REQUEST_DELETE);
		}
		
		public static final void rateSerie (Activity sourceActivity, long serieID, int rating) {
			
			Intent i = new Intent(sourceActivity.getApplicationContext(), RateActivity.class);
			i.putExtra("community_id", serieID);
			i.putExtra("rating", rating);
			sourceActivity.startActivityForResult(i, EditorConstants.REQUEST_RATE);
		}
	}
}
