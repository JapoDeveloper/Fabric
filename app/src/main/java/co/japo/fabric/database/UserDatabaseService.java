package co.japo.fabric.database;

import android.util.Log;

import com.firebase.ui.auth.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import co.japo.fabric.interfaces.UserDataUpdatable;
import co.japo.fabric.model.UserModel;

/**
 * Created by japodeveloper on 11/12/17.
 */

public class UserDatabaseService {

    private static UserDatabaseService instance;

    private LevelDatabaseService mLevelDatabaseService;

    private UserDataUpdatable mUserDataUpdatable;

    private DatabaseReference mUsersReference;

    private ChildEventListener mUsersChilEventListener;
    private ValueEventListener mUsersValueEventListener;
    private ValueEventListener mCurrentUserValueEventListener;

    public Map<String,UserModel> mUsers;

    private UserDatabaseService(){
        mLevelDatabaseService = LevelDatabaseService.getInstance();
        mUsersReference = FirebaseDatabase.getInstance().getReference().child("users");

        init();
    }

    public static UserDatabaseService getInstance(){
        if(instance == null){
            instance =  new UserDatabaseService();
        }
        return instance;
    }

    public void setUserDataUpdatable(UserDataUpdatable userDataUpdatable){
        this.mUserDataUpdatable = userDataUpdatable;
    }

    private void init(){
        mUsers = new HashMap<>();
        initializeListeners();
    }

    public void destroy(){
        detachListeners();
        instance = null;
    }

    public void registerUserIfNotExists(String uid, final String name, final String email, final String photoUrl, final String phoneNumber){

        mUsersReference.child(uid).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                UserModel user = mutableData.getValue(UserModel.class);
                if(user == null){
                    final String firstLevel = mLevelDatabaseService.getLevel("1");

                    user = new UserModel(name,email, photoUrl, phoneNumber);
                    user.level = firstLevel;
                    mutableData.setValue(user);

                    return Transaction.success(mutableData);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

            }
        });

    }

    public UserModel registerUser(String uid, String name, String email, String photoUrl, String phoneNumber){
        UserModel user = new UserModel(name,email, photoUrl, phoneNumber);
        mUsersReference.child(uid).setValue(user);
        return user;
    }

    public void updateUser(String uid, UserModel user){
        //TODO: define user fields to be updated
    }

    public void updateUserMetaAfterChallenge(String userKey, final int points){
        mUsersReference.child(userKey).child("earnedPoints").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                int earnedPoints = mutableData.getValue(Integer.class);
                earnedPoints += points;
                mutableData.setValue(earnedPoints);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {}
        });
        mUsersReference.child(userKey).child("challengesCompleted").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                int challengesCompleted = mutableData.getValue(Integer.class);
                challengesCompleted++;
                mutableData.setValue(challengesCompleted);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {}
        });
    }

    public void changeUserProfileImage(String userKey, String imageLocation){
        mUsersReference.child(userKey).child("photoUrl").setValue(imageLocation);
    }

    public String getLoggedInUserId(){
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        return null;
    }

    public UserModel getLoggedInUser(){
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            UserModel user = getUser(FirebaseAuth.getInstance().getCurrentUser().getUid());
            return user;
        }
        return null;
    }

    public UserModel getUser(final String uid) {
        return mUsers.get(uid);
    }

    private void initializeListeners(){
        if(mUsersValueEventListener == null){
            mUsersValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mUsers.clear();
                    for(DataSnapshot userDataSnapshot : dataSnapshot.getChildren()){
                        mUsers.put(userDataSnapshot.getKey(),userDataSnapshot.getValue(UserModel.class));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            mUsersReference.addValueEventListener(mUsersValueEventListener);
        }
        if(mUsersChilEventListener == null) {
            mUsersChilEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    UserModel user = dataSnapshot.getValue(UserModel.class);
                    mUsers.put(dataSnapshot.getKey(), user);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    UserModel user = dataSnapshot.getValue(UserModel.class);
                    mUsers.put(dataSnapshot.getKey(), user);
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    mUsers.remove(dataSnapshot.getKey());
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            mUsersReference.addChildEventListener(mUsersChilEventListener);
        }
    }

    public void detachListeners(){
        if(mUsersChilEventListener != null) {
            mUsersReference.removeEventListener(mUsersChilEventListener);
        }

        if(mUsersValueEventListener != null){
            mUsersReference.removeEventListener(mUsersValueEventListener);
        }
    }

    public void attachCurrentUserEventListener(){
        if(mCurrentUserValueEventListener == null){
            mCurrentUserValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mUsers.put(dataSnapshot.getKey(),dataSnapshot.getValue(UserModel.class));
                    mUserDataUpdatable.displayUserChanges();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            };
            Log.d("AttachListenerToUser",FirebaseAuth.getInstance().getCurrentUser().getUid());
            mUsersReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .addValueEventListener(mCurrentUserValueEventListener);
        }
    }

}
