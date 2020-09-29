package com.example.notificationtest;

import android.os.Build;

import com.example.notificationtest.ActionNode;
import com.example.notificationtest.CalendarEvent;
import com.example.notificationtest.ClubEvent;
import com.example.notificationtest.TreeNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.RequiresApi;

public class TreeDecisionControl {
    private Map<CalendarEvent,String> calendarHistory;
    private ArrayList<ClubEvent> history;
    private TreeNode rootC;
    private TreeNode root;

    /**
     *
     */
    public void reset() {
        root = new ActionNode("N",10);
        rootC = new ActionNode("N",10);
        history = new ArrayList<>();
        calendarHistory = new HashMap<>();
    }

    /**
     *
     */
    public TreeDecisionControl() {
        root = new ActionNode("N",10);
        rootC = new ActionNode("N",10);
        history = new ArrayList<>();
        calendarHistory = new HashMap<>();
    }

    /**
     *
     * @param attribute
     * @param value
     * @param pastAction
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static ArrayList<ClubEvent> splitOnAttr(String attribute, String value, ArrayList<ClubEvent> pastAction){

        ArrayList<ClubEvent> subTree = new ArrayList<>();

        for (ClubEvent e: pastAction) {
            if ( e.getValue(attribute) .equals(value) ) {
                subTree.add(e);
            }
        }

        return subTree;
    }

    /**
     *
     * @param attribute
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static Set<String> findAllVals(String attribute, ArrayList<ClubEvent> events){

        Set<String> attrs = new HashSet<>();

        for ( ClubEvent e: events ) {
            attrs.add(e.getValue(attribute));
        }

        return attrs;
    }


    /**
     *
     * @return
     */
    private static double entropy(ArrayList<ClubEvent> events) {

        double result = 0;
        double size = events.size();
        double N = 0;
        double R = 0;
        double D = 0;

        for (ClubEvent e: events) {
            String action = e.getFlags();
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
     *
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static String chooseAttr(ArrayList<ClubEvent> events) {

        String bestAttr = "";
        String[] basicAttrs = {"Date","Time","Conflict"};
        ArrayList<ClubEvent> subTree = new ArrayList<>();
        double oldEntropy = entropy(events);
        double bestEntropy = 10;

        for (int i = 0; i < 3; i++) {
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
     *
     * @param pastAction
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static TreeNode learnTree(ArrayList<ClubEvent> pastAction) {
        double entropy = entropy(pastAction);
        if (entropy <= 0) {
            for (ClubEvent e: pastAction) {
                return new ActionNode(e.getFlags(),0);
            }
            System.out.println("No Actions");
            return new ActionNode("N",10);
        }
        else {
            String attr = chooseAttr(pastAction);
            if (attr == null) {

                TreeNode range = buildRangeTree(pastAction);
                if ( range != null ) {
                    return range;
                }

                String Action = mostCommonAct(pastAction);
                return new ActionNode(Action,entropy);
            }
            DecisionNode deciNode = new DecisionNode(attr);

            for (String v: findAllVals(attr,pastAction)) {
                ArrayList<ClubEvent> subTree = new ArrayList<>();
                subTree = splitOnAttr(attr,v,pastAction);

                deciNode.children.put(v, learnTree(subTree));
            }
            return deciNode;
        }

    }

    /**
     *
     * @param pastAction
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static TreeNode buildRangeTree(ArrayList<ClubEvent> pastAction) {

        int size = pastAction.size();
        if ( size < 3 ) {
            return null;
        }

        ArrayList<ClubEvent> Events = new ArrayList<>(pastAction);
        Collections.sort(Events, (a,b)-> a.getTimeStamp() - b.getTimeStamp() );
        String Action = Events.get(size - 1).getFlags();
        if ( ! Action .equals( Events.get(size - 2).getFlags() ) ) {
            return null;
        }
        int timeStamp = Events.get(size - 2).getTimeStamp();
        for(int i = size - 3; i > 0; i--) {
            ClubEvent e = Events.get(i);
            if ( ! e.getFlags() .equals (Action) ) {
                break;
            }

            timeStamp = e.getTimeStamp();
        }

        return new RangeTree(timeStamp,null,new ActionNode(Action,0));
    }


    /**
     *
     * @param pastAction
     * @return
     */
    private static String mostCommonAct(ArrayList<ClubEvent> pastAction) {
        int nCount = 0;
        int rCount = 0;
        int dCount = 0;
        String a;
        for (ClubEvent e: pastAction) {
            a = e.getFlags();
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
     *
     * @param event
     * @param root
     * @param pastActions
     * @return
     */
    public static String process(ClubEvent event, TreeNode root, ArrayList<ClubEvent> pastActions) {
        ActionNode action = root.performAction(event);

        pastActions.add(event);
        return action.getAction();
    }


    //************************************************************//

    /*
     *
     * Secondary Tree for private events off from Club events.
     *
     */

    /**
     *
     * @param attribute
     * @param value
     * @param pastAction
     * @return
     */
    private static Map<CalendarEvent,String> splitOnAttr2(String attribute, String value, Map<CalendarEvent,String> pastAction){

        Map<CalendarEvent,String> subTree = new HashMap<>();

        for (CalendarEvent e: pastAction.keySet()) {
            if ( e.getValue(attribute) .equals(value) ) {
                subTree.put(e, pastAction.get(e));
            }
        }

        return subTree;
    }

    /**
     *
     * @param word
     * @param actions
     * @return
     */
    private static Set<String> findAllWords(String word, Map<CalendarEvent,String> actions){

        Set<String> words = new HashSet<>();

        for ( CalendarEvent e: actions.keySet() ) {
            words.add(e.getValue(word));
        }

        return words;
    }


    /**
     *
     * @param actions
     * @return
     */
    private static double entropy2(Map<CalendarEvent,String> actions) {

        double result = 0;
        double size = actions.size();
        double n = 0;
        double r = 0;
        double d = 0;

        for (CalendarEvent e: actions.keySet()) {
            String action = actions.get(e);
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
    private static String chooseAttr2(Map<CalendarEvent,String> actions) {

        String bestAttr = "";
        String[] basicAttrs = {"attendee","eventTitle"};
        Map<CalendarEvent,String> subTree = new HashMap<>();
        double oldEntropy = entropy2(actions);
        double bestEntropy = 10;

        for (int i = 0; i < 2; i++) {
            String attr = basicAttrs[i];
            int size = actions.size();
            double attrEntropy = 0;
            Set<String> Values = findAllWords(attr,actions);

            for (String v: Values) {
                subTree = splitOnAttr2(attr,v,actions);
                attrEntropy = entropy2(subTree)*subTree.size()/size;

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
     *
     * @param pastAction
     * @return
     */
    private static TreeNode learnTree2(Map<CalendarEvent,String> pastAction) {
        if (entropy2(pastAction) <= 0) {
            for (CalendarEvent e: pastAction.keySet()) {
                return new ActionNode(pastAction.get(e),entropy2(pastAction));
            }
            System.out.println("No Actions");
            return new ActionNode("N",10);
        }
        else {
            String attr = chooseAttr2(pastAction);
            if (attr == null) {

                TreeNode range = buildRangeTree2(pastAction);
                if ( range != null ) {
                    return range;
                }

                String action = mostCommonAct2(pastAction);
                return new ActionNode(action,entropy2(pastAction));
            }
            DecisionNode deciNode = new DecisionNode(attr);

            for (String v: findAllWords(attr,pastAction)) {
                Map<CalendarEvent,String> subTree = new HashMap<>();
                subTree = splitOnAttr2(attr,v,pastAction);

                deciNode.children.put(v, learnTree2(subTree));
            }
            return deciNode;
        }

    }

    /**
     *
     * @param pastAction
     * @return
     */
    private static TreeNode buildRangeTree2(Map<CalendarEvent, String> pastAction) {

        int size = pastAction.size();
        if ( size < 3 ) {
            return null;
        }

        ArrayList<CalendarEvent> events = new ArrayList<>(pastAction.keySet());
        Collections.sort(events, (a, b)-> a.getTimeStamp() - b.getTimeStamp() );
        String Action = pastAction.get(events.get(size - 1));
        if ( ! Action .equals(pastAction.get(events.get(size - 2)))) {
            return null;
        }
        int timeStamp = events.get(size - 2).getTimeStamp();
        for(int i = size - 3; i > 0; i--) {
            CalendarEvent e = events.get(i);
            if ( ! pastAction.get(e) .equals (Action) ) {
                break;
            }

            timeStamp = e.getTimeStamp();
        }

        return new RangeTree(timeStamp,null,new ActionNode(Action,entropy2(pastAction)));
    }


    /**
     *
     * @param pastAction
     * @return
     */
    private static String mostCommonAct2(Map<CalendarEvent, String> pastAction) {
        int nCount = 0;
        int rCount = 0;
        int dCount = 0;
        for (String a: pastAction.values()) {
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
     * @param userAction
     * @param pastActions
     * @return
     */
    private static ActionNode process2(CalendarEvent event, TreeNode root, String userAction, Map<CalendarEvent, String> pastActions) {
        ActionNode action = root.performAction(event);
        //System.out.println(Action+" "+Event);
        pastActions.put(event,userAction);

        return action;
    }

}
