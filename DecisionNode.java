package com.example.notificationtest;

import java.util.HashMap;
import java.util.Map;

public class DecisionNode extends TreeNode {
    String nodeValue;
    DecisionNode left, right;
    private String attribute;
    //private TreeNode defaultAction;
    Map<String,TreeNode> children = new HashMap<String,TreeNode>();

    public DecisionNode(String attribute) {
        this.attribute = attribute;
    }

    private ActionNode defaultAction = new ActionNode("Notify",10);

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

    public void addChild(String key, TreeNode Action) {
        children.put(key, Action);
    }

    /*
     *
     *
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
