package co.japo.fabric.ui.activities;

import android.content.Intent;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;

import co.japo.fabric.Constants;
import co.japo.fabric.R;
import co.japo.fabric.auth.FirebaseAuthenticationService;
import co.japo.fabric.database.UserDatabaseService;
import co.japo.fabric.interfaces.Displayable;
import co.japo.fabric.interfaces.UserDataUpdatable;
import co.japo.fabric.model.UserModel;
import co.japo.fabric.ui.fragments.ChallengesFragment;
import co.japo.fabric.ui.fragments.ProfileFragment;
import co.japo.fabric.ui.fragments.TopicsFragment;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener, Displayable, UserDataUpdatable{

    private Toolbar mToolbar;
    private NavigationView mNavigationView;
    private CollapsingToolbarLayout mCollapsingToolbar;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private UserDatabaseService mUserDatabaseService;

    private Fragment mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUserDatabaseService = UserDatabaseService.getInstance();
        mUserDatabaseService.setUserDataUpdatable(this);

        mToolbar  = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCollapsingToolbar = findViewById(R.id.collapsing_toolbar);
        mCollapsingToolbar.setTitle(getString(R.string.challenges));

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        //Initialize Firebase components
        mFirebaseAuth = FirebaseAuth.getInstance();

        //Initialize Firebase listeners
        mAuthStateListener = FirebaseAuthenticationService.getInstance().getFirebaseAuthStateListener(this);

        openChallengesFragment();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Constants.RC_SIGN_IN){
            if(resultCode == RESULT_CANCELED){
                finish();
            }else if(resultCode == RESULT_OK){}
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
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
            case R.id.nav_account:
                openProfileFragment();
                break;
            case R.id.nav_topics:
                openTopicsFragment();
                break;
            case R.id.nav_challenges:
                openChallengesFragment();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void openTopicsFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.contentFrame, new TopicsFragment(),"TopicsFragment")
                .addToBackStack(null)
                .commit();

        mCollapsingToolbar.setTitle(getString(R.string.topics));

        final ImageView backToolbar = findViewById(R.id.back_toolbar);
        Glide.with(this).load(R.drawable.topics_header)
                .apply(RequestOptions.centerCropTransform())
                .into(backToolbar);
    }

    private void openProfileFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.contentFrame,new ProfileFragment(),"ProfileFragment")
                .commit();

        mCollapsingToolbar.setTitle(getString(R.string.account));

        final ImageView backToolbar = findViewById(R.id.back_toolbar);
        Glide.with(this).load(R.drawable.profile_header)
                .apply(RequestOptions.centerCropTransform())
                .into(backToolbar);
    }

    private void openChallengesFragment() {
        ChallengesFragment fragment = new ChallengesFragment();
        fragment.setDisplayable(this);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.contentFrame,fragment,"ChallengesFragment")
                .addToBackStack(null)
                .commit();

        mCollapsingToolbar.setTitle(getString(R.string.challenges));

        final ImageView backToolbar = findViewById(R.id.back_toolbar);
        Glide.with(this).load(R.drawable.challenges_header)
                .apply(RequestOptions.centerCropTransform())
                .into(backToolbar);
    }

    @Override
    public void display(Class classType, String from, Bundle bundle, String title, Integer resourceId) {

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

                mCollapsingToolbar.setTitle(title);

                int drawable = R.drawable.default_header;
                if(resourceId != null){
                    drawable = resourceId.intValue();
                }

                final ImageView backToolbar = findViewById(R.id.back_toolbar);
                Glide.with(this).load(drawable)
                        .apply(RequestOptions.centerCropTransform())
                        .into(backToolbar);

            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void displayUserChanges() {
        updateUserInfoInNav();
    }

    public void updateUserInfoInNav(){
        UserModel currentUser = mUserDatabaseService.getLoggedInUser();
        if(currentUser != null){
            View navBarHeader = getLayoutInflater().inflate(R.layout.nav_header_main, null);
            if (currentUser.photoUrl != null
                    && currentUser.photoUrl != "") {
                ImageView userPhoto = navBarHeader.findViewById(R.id.userPhoto);
                Glide.with(mNavigationView.getContext())
                        .load(currentUser.photoUrl)
                        .apply(RequestOptions.circleCropTransform())
                        .into(userPhoto);
            }
            ((TextView) navBarHeader.findViewById(R.id.userName)).setText(currentUser.name);
            ((TextView) navBarHeader.findViewById(R.id.userEmailAddress)).setText(currentUser.email);
            ((TextView) navBarHeader.findViewById(R.id.userPoints)).setText(currentUser.earnedPoints + "\n" + getString(R.string.points));
            ((TextView) navBarHeader.findViewById(R.id.userChallengesCount)).setText(currentUser.challengesCompleted + "\n" + getString(R.string.challenges));

            mNavigationView.removeHeaderView(mNavigationView.getHeaderView(0));
            mNavigationView.addHeaderView(navBarHeader);
        }
    }


}
