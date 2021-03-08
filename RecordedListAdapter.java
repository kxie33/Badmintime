package com.example.notificationtest;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SimpleCursorAdapter;

/**
 *
 */
public class RecordedListAdapter extends SimpleCursorAdapter {
    private Activity ctx;

    /**
     *
     * @param context
     * @param layout
     * @param c
     * @param from
     * @param to
     * @param flags
     */
    public RecordedListAdapter(Activity context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        ctx = context;
        setViewBinder(new UpdateCheckBox());
    }

    /**
     *
     */
    private static class UpdateCheckBox implements SimpleCursorAdapter.ViewBinder{

        /**
         *
         * @param view
         * @param cursor
         * @param columnIndex
         * @return
         */
        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            if (view instanceof CheckBox){
                CheckBox cBox = (CheckBox)view;
                cBox.setChecked(Boolean.valueOf(cursor.getString(columnIndex)));
                return true;
            }
            if (view instanceof Button){
                return true;
            }
            return false;
        }
    }

    /**
     *
     */
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        if (convertView == null) {
//            convertView = ctx.getLayoutInflater().inflate(R.layout.recorded_list, parent, false);
//        }
//
//        CheckBox cBox = convertView.findViewById(R.id.checkRecord);
//        cBox.setChecked((Boolean) getItem(position));
//
//        return convertView;
//    }


}
