package net.fhtagn.zoobeditor.browser;

import net.fhtagn.zoobeditor.R;
import net.fhtagn.zoobeditor.editor.MiniLevelView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class LevelsAdapter extends BaseAdapter {
	private final JSONArray levelsArray;
	private final LayoutInflater inflater;
	private final Context context;

	public LevelsAdapter(Context context, JSONArray levelsArray) {
		this.levelsArray = levelsArray;
		this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.context = context;
	}

	@Override
	public int getCount() {
		return levelsArray.length();
	}

	@Override
	public Object getItem(int position) {
		try {
			return levelsArray.get(position);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		if (convertView != null)
			view = convertView;
		else
			view = inflater.inflate(R.layout.serieview_item, null);

		MiniLevelView levelView = (MiniLevelView) view.findViewById(R.id.minilevel);
		try {
			JSONObject levelObj = levelsArray.getJSONObject(position);
			levelView.setLevel(levelObj);
		} catch (JSONException e) {
			TextView textView = new TextView(context);
			textView.setText("Error reading level");
			e.printStackTrace();
			return textView;
		}
		return view;
	}
}
