package com.example.notificationtest;

public class ActionNode extends TreeNode {
    public String Action;
    public double entropy;


    public ActionNode(String val, double var) {
        Action = val;
        entropy = var;
    }

    @Override
    public ActionNode performAction(Event e) {

        //System.out.println(Action + " " + e.getValue("Date"));
        return this;
    }

    public String toString() {
        return Action + "@" + entropy;
    }

    public double getEntropy() {
        return entropy;
    }

    public String getAction() {
        return Action;
    }

}
