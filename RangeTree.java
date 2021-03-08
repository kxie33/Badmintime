package com.example.notificationtest;

import android.os.Build;
import android.util.Log;

import java.time.LocalDateTime;
import androidx.annotation.RequiresApi;

/**
 *
 */
public class RangeTree extends TreeNode {
    private LocalDateTime timeStamp;
    private TreeNode before;
    private TreeNode after;

    /**
     *
     * @param e
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public ActionNode performAction(Event e) {
        // TODO Auto-generated method stub
        if ( e.getTimeStamp().isBefore(timeStamp) ) {
            return before.performAction(e);
        }
        return after.performAction(e);
    }

    /**
     *
     * @param tag
     */
    @Override
    public void toLog(String tag) {
        Log.i(tag,"<" + timeStamp + " " + before + " " + after);
    }

    /**
     *
     * @param timeStamp
     * @param before
     * @param after
     */
    public RangeTree(LocalDateTime timeStamp, TreeNode before, TreeNode after) {
        super();
        this.timeStamp = timeStamp;
        this.before = before;
        this.after = after;
    }

    /**
     *
     * @return
     */
    public String toString() {
        return "<" + timeStamp + "\n" + before + "\n" + after;
    }
}
