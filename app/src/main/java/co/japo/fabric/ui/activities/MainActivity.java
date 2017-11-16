package co.japo.fabric.ui.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import co.japo.fabric.Constants;
import co.japo.fabric.R;
import co.japo.fabric.auth.FirebaseAuthenticationService;
import co.japo.fabric.database.UserDatabaseService;
import co.japo.fabric.interfaces.Displayable;
import co.japo.fabric.ui.fragments.ChallengesFragment;
import co.japo.fabric.ui.fragments.CreateChallengeFragment;
import co.japo.fabric.ui.fragments.ProfileFragment;
import co.japo.fabric.ui.fragments.TopicsFragment;
import co.japo.fabric.ui.util.ViewRefactor;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener, Displayable{

    private Toolbar mToolbar;
    private NavigationView mNavigationView;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private Fragment mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar  = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        //Initialize Firebase components
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        //Initialize Firebase listeners
        mAuthStateListener = FirebaseAuthenticationService.getInstance().getFirebaseAuthStateListener(this);

        ChallengesFragment fragment = new ChallengesFragment();
        fragment.setDisplayable(this);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.contentFrame, fragment,"ChallengesFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Constants.RC_SIGN_IN){
            if(resultCode == RESULT_CANCELED){
                finish();
            }else if(resultCode == RESULT_OK){
                ViewRefactor.displayUserInfo(
                        UserDatabaseService.getInstance().getLoggedInUser(),this
                );
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.nav_sign_out:
                AuthUI.getInstance().signOut(this);
                break;
            case R.id.nav_home:
                break;
            case R.id.nav_account:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.contentFrame,new ProfileFragment(),"ProfileFragment")
                        .commit();

                mToolbar.setTitle(R.string.account);
                break;
            case R.id.nav_topics:
                getSupportFragmentManager().beginTransaction()
                .replace(R.id.contentFrame, new TopicsFragment(),"TopicsFragment")
                .addToBackStack(null)
                .commit();

                mToolbar.setTitle(R.string.topics);
                break;
            case R.id.nav_challenges:
                ChallengesFragment fragment = new ChallengesFragment();
                fragment.setDisplayable(this);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.contentFrame,fragment,"ChallengesFragment")
                        .addToBackStack(null)
                        .commit();

                mToolbar.setTitle(R.string.challenges);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void display(Class classType, String from, Bundle bundle) {

        try {
            Object instance = classType.newInstance();
            if(instance instanceof Fragment){
                mCurrentFragment = (Fragment) instance;
                if(bundle != null) {
                    mCurrentFragment.setArguments(bundle);
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.contentFrame,mCurrentFragment)
                        .addToBackStack(from)
                        .commit();
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
