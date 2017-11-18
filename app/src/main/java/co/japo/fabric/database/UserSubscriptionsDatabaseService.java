package co.japo.fabric.database;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by japodeveloper on 11/17/17.
 */

public class UserSubscriptionsDatabaseService {

    private static UserSubscriptionsDatabaseService mInstance;
    private DatabaseReference mUsersSubscriptionReference;

    public Set<String> mTopics;

    private UserSubscriptionsDatabaseService(String userKey){
        mUsersSubscriptionReference = FirebaseDatabase.getInstance()
                .getReference()
                .child("user_subscriptions")
            .child(userKey);
        init();
    }

    public static UserSubscriptionsDatabaseService getInstance(String userKey){
        if(mInstance == null){
            mInstance = new UserSubscriptionsDatabaseService(userKey);
        }
        return mInstance;
    }

    private void init() {
        mTopics = new HashSet<>();
        initializeListeners();
    }

    public void initializeListeners() {
        ValueEventListener mTopicsValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child : dataSnapshot.getChildren()){
                    if(child.getValue(Boolean.class)) {
                        mTopics.add(child.getKey());
                    }else{
                        mTopics.remove(child.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        mUsersSubscriptionReference.child("topics")
                .addValueEventListener(mTopicsValueEventListener);
    }

    public boolean subscribeToTopic(String topic){
        mUsersSubscriptionReference.child("topics")
                .child(topic).setValue(true);
        return true;
    }

    public boolean unSubscribeToTopic(String topic){
        mUsersSubscriptionReference.child("topics")
                .child(topic).setValue(false);
        return true;
    }
}
