package co.japo.fabric.ui.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.Image;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
            ((TextView) navBarHeader.findViewById(R.id.userPoints)).setText(userInfo.earnedPoints + "\n" + navigationView.getContext().getResources().getString(R.string.points));
            ((TextView) navBarHeader.findViewById(R.id.userChallengesCount)).setText(userInfo.challengesCompleted + "\n" + navigationView.getContext().getResources().getString(R.string.challenges));
        }else{
            Log.w("displayUserInfo","No user data to be displayed");
        }
    }

    public static void displayTextPopup(String title, String message, Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.create().show();
    }

}
