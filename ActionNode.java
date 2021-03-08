package com.example.notificationtest;

import android.util.Log;

/**
 * class: ActionNode. This class setup the sub type of tree node to after a decision is made.
 * Actions are expected to be capitalized one letter string N, R, D only
 * the entropy of the action is also recorded in this object
 */
public class ActionNode extends TreeNode {
    public String Action; // the action of this node
    public double entropy; // the entropy of this node

    /**
     * Constructor of ActionNode
     * @param val the action, only N, R, D are allowed
     * @param var entropy of the action
     */
    public ActionNode(String val, double var) {
        Action = val;
        entropy = var;
    }

    /**
     * This method creates an action node using the given event
     * @param e the given event
     * @return created action node
     */
    @Override
    public ActionNode performAction(Event e) {
        return this;
    }

    /**
     * This method dumps the action node and entropy to android studio logcat for debugging
     * @param tag tag attached to the log showing in the logcat
     */
    @Override
    public void toLog(String tag) {
        Log.i(tag,"Action Node " + Action + " " + entropy);
    }

    /**
     * This method converts the action node information including the action and entropy into string
     * @return the result string
     */
    public String toString() {
        return Action + "@" + entropy;
    }

    /**
     * This is the getter for the entropy field
     * @return value of the entropy field in double
     */
    public double getEntropy() {
        return entropy;
    }

    /**
     * This is the getter for the action field
     * @return value of the action field, should be N, R, or D
     */
    public String getAction() {
        return Action;
    }

}
