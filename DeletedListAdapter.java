package com.example.notificationtest;

import android.app.Activity;
import android.database.Cursor;
import android.view.View;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;

/**
 * Class: DeletedListAdapter. This class setup the restore button on the deleted events list
 * There is an inner class UpdateButton extends the viewBinder of SimpleCursorAdapter to setup text on the button
 */
public class DeletedListAdapter extends SimpleCursorAdapter {
    private Activity ctx;

    /**
     * This is the constructor of this class.
     * All parameters are passed in from the DeletedEventList class
     * @param context context of the activity, from DeletedEventsList
     * @param layout the list layout
     * @param c cursor
     * @param from string array
     * @param to integer array
     * @param flags flags
     */
    public DeletedListAdapter(Activity context, int layout, Cursor c, String[] from, int[] to,int flags) {
        super(context, layout, c, from, to,flags);
        ctx = context;
        setViewBinder(new UpdateButton());
    }

    /**
     * This is the inner class which extends the ViewBinder to setup the button text
     */
    private static class UpdateButton implements SimpleCursorAdapter.ViewBinder{

        /**
         * This is the method in the ViewBinder that setup the button text
         * @param view view
         * @param cursor cursor
         * @param columnIndex column index of the button
         * @return true for success, false if failed
         */
        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            if (view instanceof Button){
                Button b = (Button)view;
                b.setText("Restore");
                return true;
            }
            return false;
        }
    }
}
