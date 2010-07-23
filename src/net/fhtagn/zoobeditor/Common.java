package net.fhtagn.zoobeditor;

import java.io.File;
import java.io.IOException;

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


}
