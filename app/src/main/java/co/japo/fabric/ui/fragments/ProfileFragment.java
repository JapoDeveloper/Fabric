package co.japo.fabric.ui.fragments;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

import co.japo.fabric.Constants;
import co.japo.fabric.R;
import co.japo.fabric.database.UserDatabaseService;
import co.japo.fabric.model.UserModel;
import co.japo.fabric.storage.CloudStorageService;
import co.japo.fabric.storage.InternalStorageUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private UserModel mUserInfo;
    private View mFragment;
    private ImageView mPhoto;
    private CloudStorageService mCloudStorageService;
    private UserDatabaseService mUserDatabaseService;

    public ProfileFragment() {
        mUserDatabaseService = UserDatabaseService.getInstance();
        mCloudStorageService = CloudStorageService.getInstance();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mUserInfo = mUserDatabaseService.getLoggedInUser();

        mFragment = inflater.inflate(R.layout.fragment_profile, container, false);
        mPhoto = (ImageView) mFragment.findViewById(R.id.profile_userPhoto);

        Glide.with(mFragment)
                .load(mUserInfo.photoUrl != null && mUserInfo.photoUrl != ""
                        ? mUserInfo.photoUrl : R.drawable.default_avatar)
                .apply(RequestOptions.circleCropTransform())
                .into(mPhoto);

        ((TextView) mFragment.findViewById(R.id.profile_userName))
                .setText(mUserInfo.name);
        ((TextView) mFragment.findViewById(R.id.profile_userEmail))
                .setText(mUserInfo.email);
        if(mUserInfo.phoneNumber != null && mUserInfo.phoneNumber != "") {
            ((TextView) mFragment.findViewById(R.id.profile_userPhoneNumber))
                    .setText(mUserInfo.phoneNumber);
        }else{
            ((TextView) mFragment.findViewById(R.id.profile_userPhoneNumber))
                    .setVisibility(View.GONE);
        }
        ((TextView) mFragment.findViewById(R.id.profile_userLevel))
                .setText(mUserInfo.level);
        ((TextView) mFragment.findViewById(R.id.profile_userPoints))
                .setText(mUserInfo.earnedPoints+"");
        ((TextView) mFragment.findViewById(R.id.profile_userChallengesCount))
                .setText(mUserInfo.challengesCompleted+"");

        FloatingActionButton changeUserPhoto = mFragment.findViewById(R.id.changeUserPhoto);
        changeUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(openCamera, Constants.RC_IMAGE_CAPTURE_FROM_CAMERA);
            }
        });
        return mFragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == Constants.RC_IMAGE_CAPTURE_FROM_CAMERA
                && resultCode == Activity.RESULT_OK){
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            File imageFile = InternalStorageUtil.saveImage(imageBitmap,false, mFragment.getContext());
            UploadTask uploadTask = null;
            try {
                uploadTask = mCloudStorageService.uploadChallengeImage(Uri.parse(imageFile.getAbsolutePath()));
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        String imageUrl = taskSnapshot.getDownloadUrl().toString();

                        mUserDatabaseService.changeUserProfileImage(mUserDatabaseService.getLoggedInUserId(),
                            imageUrl
                        );
                        Glide.with(mFragment)
                                .load(imageUrl)
                            .apply(RequestOptions.circleCropTransform())
                                .into(mPhoto);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
