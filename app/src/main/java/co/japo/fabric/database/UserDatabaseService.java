package co.japo.fabric.database;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import co.japo.fabric.model.UserModel;

/**
 * Created by japodeveloper on 11/12/17.
 */

public class UserDatabaseService {

    private static UserDatabaseService instance;
    private DatabaseReference mUsersReference;
    private ChildEventListener mUsersChilEventListener;
    public Map<String,UserModel> mUsers;

    private UserDatabaseService(){
        mUsersReference = FirebaseDatabase.getInstance().getReference().child("users");
        init();
    }

    public static UserDatabaseService getInstance(){
        if(instance == null){
            instance =  new UserDatabaseService();
        }
        return instance;
    }

    private void init(){
        mUsers = new HashMap<>();
        initializeListeners();
    }

    public void destroy(){
        detachListeners();
        instance = null;
    }

    public UserModel registerUser(String uid, String name, String email, String photoUrl, String phoneNuumber){
        UserModel user = new UserModel(name,email, photoUrl, phoneNuumber);
        mUsersReference.child(uid).setValue(user);
        return user;
    }

    public void updateUser(String uid, UserModel user){
        //TODO: define user fields to be updated
    }

    public UserModel getLoggedInUser(){
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            return getUser(FirebaseAuth.getInstance().getCurrentUser().getUid());
        }
        return null;
    }

    public UserModel getUser(final String uid) {
        return mUsers.get(uid);
    }

    private void initializeListeners(){
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
    }

}
