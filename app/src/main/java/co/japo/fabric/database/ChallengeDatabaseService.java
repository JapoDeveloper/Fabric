package co.japo.fabric.database;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import co.japo.fabric.interfaces.DataSetUpdatable;
import co.japo.fabric.model.ChallengeModel;

/**
 * Created by japodeveloper on 11/14/17.
 */

public class ChallengeDatabaseService {

    private static ChallengeDatabaseService instance;
    private DataSetUpdatable mDataSetUpdatable;
    private DatabaseReference mChallengesReference;
    private DatabaseReference mChallengesTopicsReference;
    private DatabaseReference mChallengesResponsesReference;
    private ValueEventListener mChallengesValueEventListener;
    public Map<String,ChallengeModel> mChallenges;

    private ChallengeDatabaseService(){
        mChallengesReference = FirebaseDatabase.getInstance().getReference("challenges");
        mChallengesTopicsReference = FirebaseDatabase.getInstance().getReference("challenges_topics");
        mChallengesResponsesReference = FirebaseDatabase.getInstance().getReference("challenges_responses");
        init();
    }

    public static ChallengeDatabaseService getInstance(){
        if (instance == null){
            instance =  new ChallengeDatabaseService();
        }
        return instance;
    }

    private void init(){
        mChallenges = new HashMap<>();
        initializeListeners();
    }

    public void destroy(){
        detachListeners();
        instance = null;
    }

    private void initializeListeners(){
        if(mChallengesValueEventListener == null) {
            mChallengesValueEventListener = new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Iterator<DataSnapshot> challenges = dataSnapshot.getChildren().iterator();
                    while(challenges.hasNext()){
                        DataSnapshot challenge = challenges.next();
                        mChallenges.put(challenge.getKey(),challenge.getValue(ChallengeModel.class));
                    }
                    if(mDataSetUpdatable != null){
                        mDataSetUpdatable.updateDataSet();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            mChallengesReference.addValueEventListener(mChallengesValueEventListener);
        }
    }

    public void detachListeners(){
        if(mChallengesValueEventListener != null) {
            mChallengesReference.removeEventListener(mChallengesValueEventListener);
        }
    }

    public void addNewChallenge(ChallengeModel challenge, List<String> topics){
        DatabaseReference chaRef = mChallengesReference.push();
        chaRef.setValue(challenge);

        DatabaseReference chaTopicRef = mChallengesTopicsReference.child(chaRef.getKey());
        for(String topic: topics){
            chaTopicRef.child(topic).setValue(true);
        }
    }

    public ChallengeModel getChallenge(String key){
        return mChallenges.get(key);
    }


    public void setUpdateDataSetDelegate(DataSetUpdatable dataSetUpdatable){
        this.mDataSetUpdatable = dataSetUpdatable;
    }
}
