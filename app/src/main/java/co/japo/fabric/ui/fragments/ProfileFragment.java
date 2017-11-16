package co.japo.fabric.ui.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import co.japo.fabric.R;
import co.japo.fabric.database.UserDatabaseService;
import co.japo.fabric.model.UserModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private UserModel mUserInfo;

    public ProfileFragment() {
        mUserInfo = UserDatabaseService.getInstance().getLoggedInUser();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        Glide.with(view)
                .load(mUserInfo.photoUrl != null && mUserInfo.photoUrl != ""
                        ? mUserInfo.photoUrl : R.drawable.default_avatar)
                .apply(RequestOptions.circleCropTransform())
                .into((ImageView) view.findViewById(R.id.profile_userPhoto));

        ((TextView) view.findViewById(R.id.profile_userName)).setText(mUserInfo.name);
        ((TextView) view.findViewById(R.id.profile_userEmail)).setText(mUserInfo.email);
        ((TextView) view.findViewById(R.id.profile_userPhoneNumber)).setText(mUserInfo.phoneNumber);
        ((TextView) view.findViewById(R.id.profile_userLevel)).setText(mUserInfo.level);
        ((TextView) view.findViewById(R.id.profile_userPoints)).setText(mUserInfo.earnedPoints+"");
        ((TextView) view.findViewById(R.id.profile_userChallengesCount)).setText(mUserInfo.challengesCompleted+"");

        return view;
    }

}
