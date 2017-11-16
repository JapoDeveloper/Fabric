package co.japo.fabric.ui.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.TreeMap;

import co.japo.fabric.R;
import co.japo.fabric.database.ChallengeDatabaseService;
import co.japo.fabric.model.ChallengeModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class TakeChallengeFragment extends Fragment {

    private ChallengeDatabaseService mChallengeDatabaseService;

    public TakeChallengeFragment() {
        // Required empty public constructor
        mChallengeDatabaseService = ChallengeDatabaseService.getInstance();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View fragment = inflater.inflate(R.layout.fragment_take_challenge, container, false);

        Bundle bundle = getArguments();
        final ChallengeModel challenge = mChallengeDatabaseService.getChallenge(bundle.getString("challengeKey"));

        ((TextView) fragment.findViewById(R.id.challengeLevel)).setText(
                fragment.getResources().getString(R.string.level)+": "+challenge.level);
        ((TextView) fragment.findViewById(R.id.challengePoints)).setText(
                fragment.getResources().getString(R.string.points)+": "+challenge.points);

        ImageView challengePhoto = fragment.findViewById(R.id.challengePhoto);
        TextView challengeDescription = fragment.findViewById(R.id.challengeDescription);
        if(challenge.presentation.equals("image")){
            challengePhoto.setVisibility(View.VISIBLE);
            challengeDescription.setVisibility(View.GONE);
            Glide.with(fragment.getContext())
                    .load(challenge.sourceUrl)
                    .into(challengePhoto);
        }if(challenge.presentation.equals("text")){
            challengePhoto.setVisibility(View.GONE);
            challengeDescription.setVisibility(View.VISIBLE);
            challengeDescription.setText(challenge.description);
        }

       FrameLayout challengeOptions = (FrameLayout) fragment.findViewById(R.id.challengeOptions);
        challengeOptions.removeAllViews();

        TreeMap<String,String> optionsMap = challenge.getOptionsAsTreeMap();

        if(challenge.multipleChoice){
            LinearLayout checboxGroup = new LinearLayout(fragment.getContext());
            checboxGroup.setLayoutParams(
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT)
            );
            checboxGroup.setOrientation(LinearLayout.VERTICAL);
            for(int i = 0; i < optionsMap.size();i++){
                CheckBox checkBox = new CheckBox(fragment.getContext());
                checkBox.setText(optionsMap.values().toArray()[i].toString());
                checboxGroup.addView(checkBox);
            }
            challengeOptions.addView(checboxGroup);
        }else{
            RadioGroup radioGroup = new RadioGroup(fragment.getContext());
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT);

            for(int i = 0; i < optionsMap.size();i++){
                RadioButton radioButton = new RadioButton(radioGroup.getContext());
                radioButton.setText(optionsMap.values().toArray()[i].toString());
                radioGroup.addView(radioButton,params);
            }
            challengeOptions.addView(radioGroup);
        }

        return fragment;
    }

}
