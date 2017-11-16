package co.japo.fabric.database;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.japo.fabric.interfaces.DataSetUpdatable;

/**
 * Created by japodeveloper on 11/15/17.
 */

public class LevelDatabaseService {

    private static LevelDatabaseService instance;
    private DataSetUpdatable mDataSetUpdatable;
    private DatabaseReference mLevelsReference;
    private ChildEventListener mLevelsChilEventListener;
    public Map<String,String> mLevels;
    public List<String> mLevelsAsList;

    private LevelDatabaseService(){
        mLevelsReference = FirebaseDatabase.getInstance().getReference().child("levels");
        init();
    }

    public static LevelDatabaseService getInstance(){
        if(instance == null){
            instance =  new LevelDatabaseService();
        }
        return instance;
    }

    private void init(){
        mLevels =  new HashMap<>();
        mLevelsAsList = new ArrayList<>();
        initializeListeners();
    }

    public void destroy(){
        detachListeners();
        instance = null;
    }

    private void initializeListeners(){
        if(mLevelsChilEventListener == null) {
            mLevelsChilEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    mLevels.put(dataSnapshot.getKey(),dataSnapshot.getValue(String.class));
                    mLevelsAsList.add(dataSnapshot.getValue(String.class));
                    if(mDataSetUpdatable != null) {
                        mDataSetUpdatable.updateDataSet();
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    String level = dataSnapshot.getKey();
                    mLevels.remove(level);
                    mLevels.remove(dataSnapshot.getValue(String.class));
                    if(mDataSetUpdatable != null) {
                        mDataSetUpdatable.updateDataSet();
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            };
            mLevelsReference.addChildEventListener(mLevelsChilEventListener);
        }
    }

    public void detachListeners(){
        if(mLevelsChilEventListener != null) {
            mLevelsReference.removeEventListener(mLevelsChilEventListener);
        }
    }

    public void setUpdateDataSetDelegate(DataSetUpdatable dataSetUpdatable){
        this.mDataSetUpdatable = dataSetUpdatable;
    }

    public List<String> getLevelList(){
        return Arrays.asList(mLevels.values().toArray(new String[mLevels.values().size()]));
    }
}
