package com.example.notificationtest;

import android.util.Log;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class DecisionNode extends TreeNode {

    /**
     *
     */
    private String attribute;
    //private TreeNode defaultAction;
    Map<String,TreeNode> children = new HashMap<String,TreeNode>();

    /**
     *
     * @param attribute
     */
    public DecisionNode(String attribute) {
        this.attribute = attribute;
    }

    /**
     *
     * @param e
     * @return
     */
    @Override
    public ActionNode performAction(Event e) {
        // TODO Auto-generated method stub

        String value = e.getValue(attribute);
        TreeNode child = children.get(value);
        if(child == null) {
            return defaultAction.performAction(e);
        }
        else {
            return child.performAction(e);
        }
    }

    /**
     *
     * @param tag
     */
    @Override
    public void toLog(String tag) {
        Log.i(tag,"Decision Node " + attribute);
        for (String k : children.keySet())
        {
            Log.i(tag,"Key = " + k);
            children.get(k).toLog(tag);
        }
    }

    /**
     *
     * @param key
     * @param Action
     */
    public void addChild(String key, TreeNode Action) {
        children.put(key, Action);
    }

    /**
     *
     * @return
     */
    public String toString() {

        StringBuffer buffer = new StringBuffer();
        buffer.append(attribute);
        buffer.append("=");

        for (String k : children.keySet())
        {
            buffer.append(k);
            buffer.append("\n  ");
            buffer.append(children.get(k));
            buffer.append("\n");
        }

        return new String(buffer);
    }
}
