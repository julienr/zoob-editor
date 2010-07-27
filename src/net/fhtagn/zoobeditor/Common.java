package net.fhtagn.zoobeditor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.JSONException;

import android.os.Environment;

public class Common {	
	public static final String LEVELS_DIR_NAME = "zoob_levels";
	
	public static File getLevelsDir () throws ExternalStorageException {
		String state = Environment.getExternalStorageState();
		File root = Environment.getExternalStorageDirectory();
		if (!Environment.MEDIA_MOUNTED.equals(state) || !root.canWrite())
			throw new ExternalStorageException("Cannot write to external storage");
    
    File levelsDir = new File(root+File.separator+LEVELS_DIR_NAME);
    levelsDir.mkdirs();
    return levelsDir;
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
