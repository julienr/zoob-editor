package net.fhtagn.zoobeditor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.fhtagn.zoobeditor.browser.MySeriesActivity.SerieAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class Common {	
	static final String TAG = "Common";
	
	public static final String LEVELS_DIR_NAME = "zoob_levels";
	public static final String MY_LEVELS_SUBDIR = "mylevels";
	public static final String ONLINE_LEVELS_SUBDIR = "community";
	
	private static File getSeriesDir (String subdir) throws ExternalStorageException {
		String state = Environment.getExternalStorageState();
		File root = Environment.getExternalStorageDirectory();
		if (!Environment.MEDIA_MOUNTED.equals(state) || !root.canWrite())
			throw new ExternalStorageException("Cannot write to external storage");
    
    File levelsDir = new File(root+File.separator+LEVELS_DIR_NAME+File.separator+subdir);
    levelsDir.mkdirs();
    return levelsDir;
	}
	
	public static File getMySeriesDir () throws ExternalStorageException {
		return getSeriesDir(MY_LEVELS_SUBDIR);
	}
	
	public static File getDownloadedSeriesDir () throws ExternalStorageException {
		return getSeriesDir(ONLINE_LEVELS_SUBDIR);
	}
	
	
	public static void saveMySerie (JSONObject serieObj) throws JSONException, IOException {
		saveSerie(MY_LEVELS_SUBDIR, serieObj);
	}
	
	public static void saveCommunitySerie (JSONObject serieObj) throws JSONException, IOException {
		saveSerie(ONLINE_LEVELS_SUBDIR, serieObj);
	}
	
	private static void saveSerie (String subdir, JSONObject serieObj) throws JSONException, IOException {
		String name;
		File levelsDir;
		levelsDir = getSeriesDir(subdir);
		
		String jsonSerie = "";
		name = Common.removeSpecialCharacters(serieObj.getString("name"));
	  jsonSerie = serieObj.toString();
    
    File levelFile = new File(levelsDir, name + ".json");
    FileWriter writer;
    writer = new FileWriter(levelFile);
    writer.write(jsonSerie);
    writer.close();
    Log.i(TAG, "Saved serie to : " + name);
	}
	
	public static Intent playMySerie (JSONObject serieObj) throws JSONException {
		return playSerie(MY_LEVELS_SUBDIR, serieObj);
	}
	
	public static Intent playCommunitySerie(JSONObject serieObj) throws JSONException {
		return playSerie(ONLINE_LEVELS_SUBDIR, serieObj);
	}
	
	private static Intent playSerie (String subdir, JSONObject serieObj) throws JSONException {
 		Uri.Builder builder = new Uri.Builder();
		builder.scheme("content");
		builder.authority("net.fhtagn.zoobgame");
		
		final String name = Common.removeSpecialCharacters(serieObj.getString("name"));
		builder.path(subdir + File.separator + name + ".json");
		Intent i = new Intent("net.fhtagn.zoobgame.PLAY", builder.build());
		/*Log.e(TAG, "uri : " + i.getData().toString());
		Log.e(TAG, "type : " + i.getType());*/
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
