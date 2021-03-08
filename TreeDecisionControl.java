package com.example.notificationtest;

import android.os.Build;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import androidx.annotation.RequiresApi;

/**
 * Class: TreeDecisionControl. TreeDecisionControl is decision tree class that handles tree construction by entropy calculation for club events and calendar events.
 * Club events are handled by a tree called club Tree.
 * Calendar events are handled by a tree called calendar tree.
 * Creating and restructuring the
 */
public class TreeDecisionControl {
    private TreeNode rootC; // root for the calendar events decision tree
    private TreeNode root; // root for the club events decision tree

    /**
     * This method resets all trees to default state.
     * Default roots for Club Tree and Calendar Tree have action N for notifying and entropy 10
     */
    public void reset() {
        root = new ActionNode("N",10);
        rootC = new ActionNode("N",10);
    }

    /**
     * This is the class constructor
     * Two roots are initialized in the constructor.
     */
    public TreeDecisionControl() {
        root = new ActionNode("N",10);
        rootC = new ActionNode("N",10);
    }

    /**
     * This method creates a sub list from the input ClubEvent list based on the input attribute and value
     * @param attribute the target attribute of the club event. This is hardcoded in ClubEvent class in the getAllAttrs method
     * @param value the value that need to be matched from club event such as Monday, 18:00 and club name
     * @param pastAction the list that contains all club events for learning
     * @return the list of all club events that matched the input attribute value
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static List<ClubEvent> splitOnAttr(String attribute, String value, List<ClubEvent> pastAction){

        List<ClubEvent> subTree = new ArrayList<>();
        for (ClubEvent e: pastAction) {
            if ( e.getValue(attribute) .equals(value) ) {
                subTree.add(e);
            }
        }
        return subTree;
    }

    /**
     * This method finds all values that appeared for the target attribute. For example, Monday, Tuesday, ect for attribute dayofweek
     * @param attribute the target attribute to find values
     * @return a string set contains all values appeared in the all events for the target attribute
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static Set<String> findAllVals(String attribute, List<ClubEvent> events){

        Set<String> attrs = new HashSet<>();

        for ( ClubEvent e: events ) {
            attrs.add(e.getValue(attribute));
        }
        return attrs;
    }

    /**
     * This method calculates the entropy of the input events list based on the distribution of actions including notify, record and delete.
     * @return the result entropy
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static double entropy(List<ClubEvent> events) {

        double result = 0;
        double size = events.size();
        double N = 0;
        double R = 0;
        double D = 0;

        for (ClubEvent e: events) {
            String action = e.getAction();
            switch ( action ) {
                case "N": N++; break;
                case "R": R++; break;
                case "D": D++; break;
            }
        }
        result = 0;
        if (N > 0) {
            result += (N/size) * Math.log( N/size );
        }
        else if (D > 0) {
            result += ( D/size ) * Math.log( D/size );
        }
        else if (R > 0) {
            result += ( R/size ) * Math.log( R/size );
        }
        return -result;
    }


    /**
     * This method calculates the entropy of each attribute value and returns the attribute with smallest entropy as result
     * @return the attribute value that has the smallest entropy
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static String chooseAttr(List<ClubEvent> events) {

        String bestAttr = "";
        String[] basicAttrs = {"Start","End","clubName","dayOfWeek"};
        List<ClubEvent> subTree;
        double oldEntropy = entropy(events);
        double bestEntropy = 10;

        for (int i = 0; i < basicAttrs.length; i++) {
            String attr = basicAttrs[i];
            int size = events.size();
            double attrEntropy = 0;
            Set<String> Values = findAllVals(attr,events);

            for (String v: Values) {
                subTree = splitOnAttr(attr,v,events);
                attrEntropy += entropy(subTree)*subTree.size()/size;
            }

            if (bestEntropy > attrEntropy) {
                bestEntropy = attrEntropy;
                bestAttr = attr;
            }

        }
        if (oldEntropy <= bestEntropy) {
            return null;
        }
        return bestAttr;
    }

    /**
     * This method creates and retrains the club event tree based on the existing club event list if there are more than three events in the list, and returns the root
     * @param pastAction the list that contains all club events or the sub list of the club event
     * @return root of the result tree
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static TreeNode learnTree(List<ClubEvent> pastAction) {
        if (pastAction.size() < 2){
            return TreeNode.defaultAction;
        }
        double entropy = entropy(pastAction);
        if (entropy <= 0) {
            for (ClubEvent e: pastAction) {
                return new ActionNode(e.getAction(),0);
            }
            return new ActionNode("N",10);
        }
        else {
            String attr = chooseAttr(pastAction);
            if (attr == null) {
                return computeDefaultTree(entropy, buildRangeTree(pastAction), mostCommonAct(pastAction));
            }
            DecisionNode deciNode = new DecisionNode(attr);

            for (String v: findAllVals(attr,pastAction)) {
                List<ClubEvent> subTree;
                subTree = splitOnAttr(attr,v,pastAction);
                if ( subTree.size() == pastAction.size()){
                    return computeDefaultTree(entropy, buildRangeTree(pastAction), mostCommonAct(pastAction));
                }
                deciNode.children.put(v, learnTree(subTree));
            }
            return deciNode;
        }
    }

    /**
     * This method creates the default action node when there are less than three events to learn from or when user action is null
     * @param entropy entropy of the club event list
     * @param treeNode the tree node for default use
     * @param s the action of the default tree
     * @return the result tree node
     */
    private static TreeNode computeDefaultTree(double entropy, TreeNode treeNode, String s) {
        TreeNode range = treeNode;
        if (range != null) {
            return range;
        }

        String Action = s;
        return new ActionNode(Action, entropy);
    }

    /**
     * This method compares the last two actions from the user to determine whether there is a continuous action from the user.
     * If a continuous action such as two events with similar attributes had same user action, this action will be set as recommended
     * to club event tree with entropy 0.2
     * @param pastAction the club events list
     * @return the recommended decision node for club event tree
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static TreeNode buildRangeTree(List<ClubEvent> pastAction) {

        int size = pastAction.size();
        if ( size < 3 ) {
            return null;
        }

        List<ClubEvent> Events = new ArrayList<>(pastAction);
        Collections.sort(Events, (a,b)->  a.getTimeStamp().compareTo(b.getTimeStamp()) );
        String Action = Events.get(size - 1).getAction();
        if ( ! Action .equals( Events.get(size - 2).getAction() ) ) {
            return null;
        }
        LocalDateTime timeStamp = Events.get(size - 2).getTimeStamp();
        for(int i = size - 3; i > 0; i--) {
            ClubEvent e = Events.get(i);
            if ( ! e.getAction() .equals (Action) ) {
                break;
            }

            timeStamp = e.getTimeStamp();
        }

        return new RangeTree(timeStamp,null,new ActionNode(Action,0.2));
    }


    /**
     * This method counts the total number of all kinds of actions from user, and return the most common action
     * @param pastAction the club event list
     * @return the most common action
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static String mostCommonAct(List<ClubEvent> pastAction) {
        int nCount = 0;
        int rCount = 0;
        int dCount = 0;
        String a;
        for (ClubEvent e: pastAction) {
            a = e.getAction();
            switch (a) {
                case "N" : nCount++; break;
                case "R" : rCount++; break;
                case "D" : dCount++; break;
            }
        }
        String Action;
        if(nCount >= rCount && nCount >= dCount) {
            Action = "N";
        }
        else if(rCount > nCount && rCount >= dCount) {
            Action = "R";
        }
        else {
            Action = "D";
        }
        return Action;
    }

    /**
     * This method creates an action node for the target club event
     * @param event the club event
     * @param root the club event decision tree root node
     * @return the result action node
     */
    public static ActionNode process(ClubEvent event, TreeNode root) {
        ActionNode action = root.performAction(event);
        return action;
    }


    //************************************************************//

    /*
     *
     * Secondary Tree process learning from calendar events associate with Club events.
     *
     */

    /**
     * This method creates a sub list from the input CalendarEvent list based on the input attribute and value
     * @param value the word that need to be matched from calendar event such as location, event type grabbed from description
     * @param pastAction the list that contains all calendar events for learning
     * @return the list of all calendar events that matched the input word
     */
    private static List<CalendarEvent> splitOnAttrCal(String value, List<CalendarEvent> pastAction){

        List<CalendarEvent> subTree = new ArrayList<>();

        for (CalendarEvent e: pastAction) {
            if ( e.getWords().contains(value) ) {
                subTree.add(e);
            }
        }

        return subTree;
    }

    /**
     * This method finds all words that appeared in the calendar event description
     * @param actions the calendar event list
     * @return a set of string that contains all different words
     */
    private static Set<String> findAllWords(List<CalendarEvent> actions){

        Set<String> words = new HashSet<>();

        for ( CalendarEvent e: actions ) {
            words.addAll(e.getWords());
        }

        return words;
    }


    /**
     *
     * @param actions
     * @return
     */
    private static double entropyCal(List<CalendarEvent> actions) {

        double result = 0;
        double size = actions.size();
        double n = 0;
        double r = 0;
        double d = 0;

        for (CalendarEvent e: actions) {
            String action = e.getFlags();
            switch ( action ) {
                case "N": n++; break;
                case "R": r++; break;
                case "D": d++; break;
            }

        }
        result = 0;
        if (n > 0) {
            result += (n/size) * Math.log( n/size );
        }
        else if (d > 0) {
            result += ( d/size ) * Math.log( d/size );
        }
        else if (r > 0) {
            result += ( r/size ) * Math.log( r/size );
        }
        return -result;
    }


    /**
     *
     * @param actions
     * @return
     */
    private static String chooseAttrCal(List<CalendarEvent> actions) {

        String bestAttr = "";
        List<CalendarEvent> subTree;
        List<CalendarEvent> missingTree;
        double oldEntropy = entropyCal(actions);
        double bestEntropy = 10;

        int size = actions.size();
        double attrEntropy = 0;
        Set<String> Values = findAllWords(actions);

        for (String v: Values) {
            subTree = splitOnAttrCal(v,actions);
            missingTree = new ArrayList<>(actions);
            missingTree.removeAll(subTree);
            attrEntropy = entropyCal(subTree)*subTree.size()/size + entropyCal(missingTree)*missingTree.size()/size;

            if (bestEntropy > attrEntropy) {
                bestEntropy = attrEntropy;
                bestAttr = v;
            }
        }

        if (oldEntropy <= bestEntropy) {
            return null;
        }

        return bestAttr;
    }


    /**
     *
     * @param pastAction
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static TreeNode learnTreeCal(List<CalendarEvent> pastAction) {
        if (pastAction.size() < 3){
            return TreeNode.defaultAction;
        }
        return learnSubTreeCal(pastAction);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static TreeNode learnSubTreeCal(List<CalendarEvent> pastAction) {
        double entropy = entropyCal(pastAction);
        if (entropy <= 0) {
            for (CalendarEvent e: pastAction) {
                return new ActionNode(e.getFlags(), entropyCal(pastAction));
            }
            return new ActionNode("N",10);
        }
        else {
            String attr = chooseAttrCal(pastAction);
            if (attr == null) {
                return computeDefaultTree(entropyCal(pastAction), buildRangeTreeCal(pastAction), mostCommonActCal(pastAction));
            }

            for (String v: findAllWords(pastAction)) {
                List<CalendarEvent> subTree;
                subTree = splitOnAttrCal(v,pastAction);
                if ( subTree.size() == pastAction.size() || subTree.isEmpty()){
                    continue;
                }
                DecisionNode deciNode = new DecisionNode(v);
                deciNode.children.put("true", learnSubTreeCal(subTree));
                List<CalendarEvent> missingTree = new ArrayList<>(pastAction);
                missingTree.removeAll(subTree);
                deciNode.children.put("false", learnSubTreeCal(missingTree));
                return deciNode;
            }

            return computeDefaultTree(entropyCal(pastAction), buildRangeTreeCal(pastAction), mostCommonActCal(pastAction));
        }
    }

    /**
     *
     * @param pastAction
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static TreeNode buildRangeTreeCal(List<CalendarEvent> pastAction) {

//        int size = pastAction.size();
//        if ( size < 2 ) {
//            return null;
//        }
//
//        List<CalendarEvent> events = new ArrayList<>(pastAction);
//        Collections.sort(events, (a, b)->  a.getTimeStamp().compareTo(b.getTimeStamp()) );
//        String Action = pastAction.get(events.size()-1).getFlags();
//        if ( ! Action .equals(pastAction.get(events.size() - 2).getFlags() ) ) {
//            return null;
//        }
//        LocalDateTime timeStamp = events.get(size - 2).getTimeStamp();
//        for(int i = size - 3; i > 0; i--) {
//            CalendarEvent e = events.get(i);
//            if ( ! e.getFlags() .equals (Action) ) {
//                break;
//            }
//
//            timeStamp = e.getTimeStamp();
//        }
//
//        return new RangeTree(timeStamp,null,new ActionNode(Action, entropyCal(pastAction)));
        return null;
    }


    /**
     *
     * @param pastAction
     * @return
     */
    private static String mostCommonActCal(List<CalendarEvent> pastAction) {
        int nCount = 0;
        int rCount = 0;
        int dCount = 0;
        String a;
        for (CalendarEvent e: pastAction) {
            a = e.getFlags();
            switch (a) {
                case "N" : nCount++; break;
                case "R" : rCount++; break;
                case "D" : dCount++; break;
            }
        }
        String action;
        if(nCount >= rCount && nCount >= dCount) {
            action = "N";
        }
        else if(rCount > nCount && rCount >= dCount) {
            action = "R";
        }
        else {
            action = "D";
        }
        return action;
    }

    /**
     *
     * @param event
     * @param root
     * @return
     */
    public static ActionNode processCal(CalendarEvent event, TreeNode root) {
        ActionNode action = root.performAction(event);
        return action;
    }

}
