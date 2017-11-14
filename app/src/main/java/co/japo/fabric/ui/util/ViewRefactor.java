package co.japo.fabric.ui.util;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import co.japo.fabric.R;
import co.japo.fabric.model.UserModel;

/**
 * Created by japodeveloper on 11/12/17.
 */

public class ViewRefactor {

    private ViewRefactor(){}

    public static void displayUserInfo(UserModel userInfo, Activity from){
        if(userInfo != null && from != null) {
            final ViewGroup navigationView = from.findViewById(R.id.nav_view);

            View navBarHeader = from.getLayoutInflater().inflate(R.layout.nav_header_main, navigationView);
            if (userInfo.photoUrl != null
                    && userInfo.photoUrl != "") {
                ImageView userPhoto = navBarHeader.findViewById(R.id.userPhoto);
                Glide.with(navigationView.getContext())
                        .load(userInfo.photoUrl)
                        .apply(RequestOptions.circleCropTransform())
                        .into(userPhoto);
            }
            ((TextView) navBarHeader.findViewById(R.id.userName)).setText(userInfo.name);
            ((TextView) navBarHeader.findViewById(R.id.userEmailAddress)).setText(userInfo.email);
            ((TextView) navBarHeader.findViewById(R.id.userLevel)).setText(userInfo.level + "\n" + navigationView.getContext().getResources().getString(R.string.level));
            ((TextView) navBarHeader.findViewById(R.id.userPoints)).setText(userInfo.earnedPoints + "\n" + navigationView.getContext().getResources().getString(R.string.points));
            ((TextView) navBarHeader.findViewById(R.id.userChallengesCount)).setText(userInfo.challengesCompleted + "\n" + navigationView.getContext().getResources().getString(R.string.challenges));
        }else{
            Log.w("displayUserInfo","No user data to be displayed");
        }
    }

}
