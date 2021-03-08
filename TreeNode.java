package com.example.notificationtest;

/**
 *
 */
public abstract class TreeNode {
    protected static ActionNode defaultAction = new ActionNode("N",10);

    /**
     *
     * @param e
     * @return
     */
    public abstract ActionNode performAction(Event e);

    /**
     *
     * @param tag
     */
    public abstract void toLog(String tag);
}
