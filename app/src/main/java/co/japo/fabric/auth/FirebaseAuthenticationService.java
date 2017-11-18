package co.japo.fabric.auth;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

import co.japo.fabric.Constants;
import co.japo.fabric.R;
import co.japo.fabric.database.UserDatabaseService;
import co.japo.fabric.model.UserModel;

/**
 * Created by japodeveloper on 11/12/17.
 */

public class FirebaseAuthenticationService {

    private static FirebaseAuthenticationService instance;

    private FirebaseAuthenticationService(){}

    public static FirebaseAuthenticationService getInstance(){
        if(instance == null){
            instance = new FirebaseAuthenticationService();
        }
        return instance;
    }

    public FirebaseAuth.AuthStateListener getFirebaseAuthStateListener(final Activity from){
        return new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    UserDatabaseService userDatabaseService = UserDatabaseService.getInstance();
                    UserModel userInfo = userDatabaseService.getUser(user.getUid());

                    //user is signed in
                        if (userInfo == null) {
                          userDatabaseService.registerUserIfNotExists(user.getUid(),
                                    user.getDisplayName(), user.getEmail(),
                                    user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null, user.getPhoneNumber());
                        }
                        Toast.makeText(from,
                                from.getResources().getString(R.string.successful_login_message), Toast.LENGTH_LONG)
                                .show();
                    userDatabaseService.attachCurrentUserEventListener();
                }else{
                    //user is signed out
                    from.startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(
                                            Arrays.asList(
                                                    new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()
                                            )
                                    )
                                    .build(),
                            Constants.RC_SIGN_IN);
                }
            }
        };
    }
}
