package co.japo.fabric.database;

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.japo.fabric.interfaces.DataSetUpdatable;

/**
 * Created by japodeveloper on 11/13/17.
 */

public class TopicDatabaseService {

    private static TopicDatabaseService instance;
    private DataSetUpdatable mDataSetUpdatable;
    private DatabaseReference mTopicsReference;
    private ChildEventListener mTopicsChilEventListener;
    public List<String> mTopics;

    private TopicDatabaseService(){
        mTopicsReference = FirebaseDatabase.getInstance().getReference().child("topics");
        init();
    }

    public static TopicDatabaseService getInstance(){
        if(instance == null){
            instance =  new TopicDatabaseService();
        }
        return instance;
    }

    private void init(){
        mTopics =  new ArrayList<>();
        initializeListeners();
    }

    public void destroy(){
        detachListeners();
        instance = null;
    }

    private void initializeListeners(){
        if(mTopicsChilEventListener == null) {
            mTopicsChilEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    String topic = dataSnapshot.getKey();
                    mTopics.add(topic);
                    Collections.sort(mTopics);
                    if(mDataSetUpdatable != null) {
                        mDataSetUpdatable.updateDataSet();
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    String topic = dataSnapshot.getKey();
                    mTopics.remove(topic);
                    if(mDataSetUpdatable != null) {
                        mDataSetUpdatable.updateDataSet();
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            };
            mTopicsReference.addChildEventListener(mTopicsChilEventListener);
        }
    }

    public void addTopic(String topic){
        mTopicsReference.child(topic).setValue(true);
    }

    public void detachListeners(){
        if(mTopicsChilEventListener != null) {
            mTopicsReference.removeEventListener(mTopicsChilEventListener);
        }
    }

    public void setUpdateDataSetDelegate(DataSetUpdatable dataSetUpdatable){
        this.mDataSetUpdatable = dataSetUpdatable;
    }

}
