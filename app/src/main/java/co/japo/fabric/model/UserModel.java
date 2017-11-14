package co.japo.fabric.model;

/**
 * Created by japodeveloper on 11/12/17.
 */

public class UserModel {

    public String name;
    public String email;
    public String photoUrl;
    public String phoneNumber;
    public float earnedPoints;
    public int challengesCompleted;
    public int level;

    public UserModel(){

    }

    public UserModel(String name, String email){
        this.name =  name;
        this.email = email;
        this.photoUrl = null;
        this.phoneNumber = null;
        this.earnedPoints = 0f;
        this.challengesCompleted = 0;
        this.level = 1;
    }

    public UserModel(String name, String email, String photoUrl, String phoneNumber){
        this.name =  name;
        this.email = email;
        this.photoUrl = photoUrl;
        this.phoneNumber = phoneNumber;
        this.earnedPoints = 0f;
        this.challengesCompleted = 0;
        this.level = 1;
    }

    @Override
    public String toString() {
        return "UserModel{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", photoUrl='" + photoUrl + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", earnedPoints=" + earnedPoints +
                ", challengesCompleted=" + challengesCompleted +
                ", level=" + level +
                '}';
    }
}
