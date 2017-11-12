package co.japo.fabric;

import com.firebase.ui.auth.User;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by japodeveloper on 11/12/17.
 */

public class UserManagement {

    private static UserManagement instance;
    private DatabaseReference mUsersReference;
    public Map<String,UserModel> users;

    private UserManagement(FirebaseDatabase firebaseDatabase){
        mUsersReference = firebaseDatabase.getReference().child("users");
    }

    public static UserManagement getInstance(FirebaseDatabase firebaseDatabase){
        if(instance == null){
            instance =  new UserManagement(firebaseDatabase);
        }
        return instance;
    }

    public UserModel registerUser(String uid, String name, String email){
        UserModel user = new UserModel(name,email);
        mUsersReference.child(uid).setValue(user);
        return user;
    }

    public UserModel getUser(final String uid) {
        if (users == null){
            users = new HashMap<>();
        }
        mUsersReference.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserModel currentUser = dataSnapshot.getValue(UserModel.class);
                users.put(uid,currentUser);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return users.get(uid);
    }

}
