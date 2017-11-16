package co.japo.fabric.model;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by japodeveloper on 11/14/17.
 */

public class ChallengeModel {

    public String description;
    public String sourceUrl;
    public String presentation;
    public boolean multipleChoice;
    public int limitedTime;// count of seconds
    public Map<String,String> options;
    public String correctAnswer;
    public String solutionExplanation;
    public int points;
    public String level;

    public TreeMap<String,String> getOptionsAsTreeMap(){
        return new TreeMap<String,String>(options);
    }

    @Override
    public String toString() {
        return "ChallengeModel{" +
                "description='" + description + '\'' +
                ", sourceUrl='" + sourceUrl + '\'' +
                ", presentation='" + presentation + '\'' +
                ", multipleChoice=" + multipleChoice +
                ", limitedTime=" + limitedTime +
                ", options=" + options +
                ", correctAnswer='" + correctAnswer + '\'' +
                ", solutionExplanation='" + solutionExplanation + '\'' +
                ", points=" + points +
                ", level=" + level +
                '}';
    }
}
