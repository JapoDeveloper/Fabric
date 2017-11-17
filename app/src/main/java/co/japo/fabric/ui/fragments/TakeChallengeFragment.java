package co.japo.fabric.ui.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import co.japo.fabric.R;
import co.japo.fabric.database.ChallengeDatabaseService;
import co.japo.fabric.database.UserDatabaseService;
import co.japo.fabric.model.ChallengeModel;
import co.japo.fabric.model.ChallengeResponseModel;
import co.japo.fabric.ui.util.ViewRefactor;

import static android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE;

/**
 * A simple {@link Fragment} subclass.
 */
public class TakeChallengeFragment extends Fragment {

    private UserDatabaseService mUserDatabaseService;
    private ChallengeDatabaseService mChallengeDatabaseService;
    private FrameLayout mChallengeOptions;
    private View mFragment;

    private String mChallengeKey;
    private ChallengeModel mChallenge;

    private String mUserAnswers;

    public TakeChallengeFragment() {
        mUserDatabaseService = UserDatabaseService.getInstance();
        mChallengeDatabaseService = ChallengeDatabaseService.getInstance();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mFragment = inflater.inflate(R.layout.fragment_take_challenge, container, false);

        Bundle bundle = getArguments();
        mChallengeKey = bundle.getString("challengeKey");
        mChallenge = mChallengeDatabaseService.getChallenge(mChallengeKey);

        ((TextView) mFragment.findViewById(R.id.challengeLevel)).setText(
                mFragment.getResources().getString(R.string.level)+": "+mChallenge.level);
        ((TextView) mFragment.findViewById(R.id.challengePoints)).setText(
                mFragment.getResources().getString(R.string.points)+": "+mChallenge.points);

        ImageView challengePhoto = mFragment.findViewById(R.id.challengePhoto);
        TextView challengeDescription = mFragment.findViewById(R.id.challengeDescription);
        if(mChallenge.presentation.equals("image")){
            challengePhoto.setVisibility(View.VISIBLE);
            challengeDescription.setVisibility(View.GONE);
            Glide.with(mFragment.getContext())
                    .load(mChallenge.sourceUrl)
                    .into(challengePhoto);
        }if(mChallenge.presentation.equals("text")){
            challengePhoto.setVisibility(View.GONE);
            challengeDescription.setVisibility(View.VISIBLE);
            challengeDescription.setText(mChallenge.description);
        }

        mChallengeOptions = (FrameLayout) mFragment.findViewById(R.id.challengeOptions);
        mChallengeOptions.removeAllViews();

        TreeMap<String,String> optionsMap = mChallenge.getOptions();

        if(mChallenge.multipleChoice){
            LinearLayout checboxGroup = new LinearLayout(mFragment.getContext());
            checboxGroup.setLayoutParams(
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT)
            );
            checboxGroup.setOrientation(LinearLayout.VERTICAL);
            for(int i = 0; i < optionsMap.size();i++){
                CheckBox checkBox = new CheckBox(mFragment.getContext());
                checkBox.setTag(optionsMap.keySet().toArray()[i].toString());
                checkBox.setText(optionsMap.keySet().toArray()[i].toString()+")\t"+optionsMap.values().toArray()[i].toString());
                checboxGroup.addView(checkBox);
            }
            mChallengeOptions.addView(checboxGroup);
        }else{
            RadioGroup radioGroup = new RadioGroup(mFragment.getContext());
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT);

            for(int i = 0; i < optionsMap.size();i++){
                RadioButton radioButton = new RadioButton(radioGroup.getContext());
                radioButton.setTag(optionsMap.keySet().toArray()[i].toString());
                radioButton.setText(optionsMap.keySet().toArray()[i].toString()+")\t"+optionsMap.values().toArray()[i].toString());
                radioGroup.addView(radioButton,params);
            }
            mChallengeOptions.addView(radioGroup);
        }

        ImageButton sendChallengeResponse = mFragment.findViewById(R.id.sendChallengeResponse);
        sendChallengeResponse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendResponse();
            }
        });

        return mFragment;
    }

    private void sendResponse(){
        boolean valid = false;
        if(mChallenge.multipleChoice){
            valid = validateResponseMultiple();
        }else{
            valid = validateResponseSingle();
        }
        if(!valid){
            Toast.makeText(mFragment.getContext(),
                    R.string.msg_request_answer,
                    Toast.LENGTH_SHORT)
            .show();
        }else{
            ChallengeResponseModel response = new ChallengeResponseModel();
            response.submitDate = new Date();
            response.userResponse = mUserAnswers;
            if(mChallenge.correctAnswer.equals(mUserAnswers)){
                response.earnedPoints = mChallenge.points;
                response.isCorrect = true;
            }else{
                response.earnedPoints = 0;
                response.isCorrect = false;
            }
            mChallengeDatabaseService.saveUserChallengeResponse(mChallengeKey,
                    mUserDatabaseService.getLoggedInUserId(),
                    response);
            ViewRefactor.displayTextPopup(
                    response.isCorrect ? getString(R.string.congrats_title) : getString(R.string.apologize_title),
                    response.isCorrect ? String.format(getString(R.string.congrats_body),response.earnedPoints)
                            : getString(R.string.apologize_body),
                    mFragment.getContext()
            );
            getActivity().getSupportFragmentManager().popBackStack("ChallengesFragment",POP_BACK_STACK_INCLUSIVE);
        }
    }

    private boolean validateResponseSingle(){
        boolean valid = false;
        mUserAnswers = "";
        RadioGroup radioGroup = (RadioGroup) mChallengeOptions.getChildAt(0);
        for(int i = 0; i < radioGroup.getChildCount(); i++){
            RadioButton radio = ((RadioButton) radioGroup.getChildAt(i));
            if(radio.isChecked()){
                mUserAnswers = radio.getTag().toString()+",";
                valid = true;
                break;
            }
        }
        return valid;
    }

    private boolean validateResponseMultiple(){
        boolean valid = false;
        mUserAnswers = "";
        LinearLayout checboxGroup = (LinearLayout) mChallengeOptions.getChildAt(0);
        for(int i = 0; i < checboxGroup.getChildCount(); i++){
            CheckBox checkBox = ((CheckBox) checboxGroup.getChildAt(i));
            if(checkBox.isChecked()){
                mUserAnswers += checkBox.getTag().toString()+",";
                valid = true;
            }else{
                mUserAnswers.replace(checkBox.getTag().toString()+",","");
            }
        }

        return valid;
    }
}
