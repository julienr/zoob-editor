package net.fhtagn.zoobeditor;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

public abstract class SerieCursorAdapter extends CursorAdapter {
	private final LayoutInflater inflater;
	private final int layoutID;
	public SerieCursorAdapter(Context context, Cursor c, int layoutID) {
    super(context, c);
    this.layoutID = layoutID;
    inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }
	
	public String getName(int position) {
		Cursor cur = (Cursor)this.getItem(position);
		return cur.getString(cur.getColumnIndex(Series.NAME));
	}
	
	protected abstract void fillView (View view, Cursor cursor);
	
	@Override
  public void bindView(View view, Context context, Cursor cursor) {
		fillView(view, cursor);
  }

	@Override
  public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(layoutID, null);
		fillView(view, cursor);
    return view;
  }
}
