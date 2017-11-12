package co.japo.fabric;

/**
 * Created by japodeveloper on 11/12/17.
 */

public class UserModel {

    public String name;
    public String email;
    public float earnedPoints;
    public int challengesCompleted;
    public int level;

    public UserModel(){

    }

    public UserModel(String name, String email){
        this.name =  name;
        this.email = email;
        this.earnedPoints = 0f;
        this.challengesCompleted = 0;
        this.level = 1;
    }

    @Override
    public String toString() {
        return "UserModel{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", earnedPoints=" + earnedPoints +
                ", challengesCompleted=" + challengesCompleted +
                ", level=" + level +
                '}';
    }
}
