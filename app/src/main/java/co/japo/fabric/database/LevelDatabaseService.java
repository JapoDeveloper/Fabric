package co.japo.fabric.database;

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import co.japo.fabric.interfaces.DataSetUpdatable;

/**
 * Created by japodeveloper on 11/15/17.
 */

public class LevelDatabaseService {

    private static LevelDatabaseService instance;
    private DataSetUpdatable mDataSetUpdatable;
    private DatabaseReference mLevelsReference;
    private ValueEventListener mLevelValueEventListener;
    public Map<String,String> mLevels;

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
        mLevels =  new TreeMap<>();
        initializeListeners();
    }

    public void destroy(){
        detachListeners();
        instance = null;
    }

    private void initializeListeners(){
            if(mLevelValueEventListener == null) {
                mLevelValueEventListener =  new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mLevels.clear();
                        for(DataSnapshot levelDataSnapshot : dataSnapshot.getChildren()){
                            mLevels.put(levelDataSnapshot.getKey(),levelDataSnapshot.getValue(String.class));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };
                mLevelsReference.orderByKey().addValueEventListener(mLevelValueEventListener);
            }
    }

    public void detachListeners(){
        if(mLevelValueEventListener != null){
            mLevelsReference.removeEventListener(mLevelValueEventListener);
        }
    }

    public void setUpdateDataSetDelegate(DataSetUpdatable dataSetUpdatable){
        this.mDataSetUpdatable = dataSetUpdatable;
    }

    public List<String> getLevelList(){
        return Arrays.asList(mLevels.values().toArray(new String[mLevels.values().size()]));
    }

    public String getLevel(String levelKey){
        return this.mLevels.get(levelKey);
    }
}
