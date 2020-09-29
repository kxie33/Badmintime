package com.example.notificationtest;

public class RangeTree extends TreeNode {
    @Override
    public ActionNode performAction(Event e) {
        // TODO Auto-generated method stub
        if ( e.getTimeStamp() < timeStamp ) {
            return before.performAction(e);
        }

        return after.performAction(e);
    }

    public RangeTree(int timeStamp, TreeNode before, TreeNode after) {
        super();
        this.timeStamp = timeStamp;
        this.before = before;
        this.after = after;
    }

    int timeStamp;

    TreeNode before;
    TreeNode after;

    public String toString() {
        return "<" + timeStamp + "\n" + before + "\n" + after;
    }
}
