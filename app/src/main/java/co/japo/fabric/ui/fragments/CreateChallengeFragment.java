package co.japo.fabric.ui.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.japo.fabric.Constants;
import co.japo.fabric.R;
import co.japo.fabric.database.ChallengeDatabaseService;
import co.japo.fabric.database.LevelDatabaseService;
import co.japo.fabric.database.TopicDatabaseService;
import co.japo.fabric.database.UserDatabaseService;
import co.japo.fabric.interfaces.DataSetUpdatable;
import co.japo.fabric.model.ChallengeModel;
import co.japo.fabric.storage.CloudStorageService;
import co.japo.fabric.storage.InternalStorageUtil;
import co.japo.fabric.ui.util.ViewUtility;

import static android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE;


/**
 * A simple {@link Fragment} subclass.
 */
public class CreateChallengeFragment extends Fragment implements DataSetUpdatable {

    private LevelDatabaseService mLevelDatabaseService;
    private TopicDatabaseService mTopicsDatabaseService;
    private ChallengeDatabaseService mChallengeDatabaseService;
    private UserDatabaseService mUserDatabaseService;

    private CloudStorageService mCloudStorageService;
    private ArrayAdapter<String> mLevelAdapter;

    private View mFragment;

    private Spinner mLevel;
    private ImageView mPhoto;
    private EditText mPoints;
    private EditText mDescription;
    private ToggleButton mChoiceType;
    private ToggleButton mPresentationType;
    private EditText mSolutionExplanation;

    private LinearLayout mChallengeOptionsFrame;
    private TableLayout mOptionsItemsLayout;

    private Uri mImageResource;

    private String[] mTopicsAvailable;
    private boolean[] mTopicsUserSelection;
    private List<String> mTopicsForNewChallenge;

    private ChallengeModel mChallengeData;

    public CreateChallengeFragment() {
        // Required empty public constructor
        mLevelDatabaseService = LevelDatabaseService.getInstance();
        mLevelDatabaseService.setUpdateDataSetDelegate(this);

        mTopicsDatabaseService = TopicDatabaseService.getInstance();
        mTopicsDatabaseService.setUpdateDataSetDelegate(this);

        mChallengeDatabaseService = ChallengeDatabaseService.getInstance();
        mChallengeDatabaseService.setUpdateDataSetDelegate(this);

        mUserDatabaseService = UserDatabaseService.getInstance();

        mCloudStorageService = CloudStorageService.getInstance();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mFragment = inflater.inflate(R.layout.fragment_create_challenge, container, false);

        mLevel = mFragment.findViewById(R.id.challengeLevelInput);

        mLevelAdapter = new ArrayAdapter<>(mFragment.getContext(),android.R.layout.simple_spinner_item,
                new ArrayList<String>( mLevelDatabaseService.mLevels.values()));
        mLevelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mLevel.setAdapter(mLevelAdapter);
        mLevel.setSelection(0);

        mPoints = mFragment.findViewById(R.id.challengePointsInput);

        initTopics();

        FloatingActionButton topicsPicker = mFragment.findViewById(R.id.challengeTopicsPicker);
        topicsPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mTopicsAvailable != null && mTopicsAvailable.length == 0){
                    initTopics();
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(mFragment.getContext());
                builder.setTitle(mFragment.getResources().getString(R.string.topics));
                builder.setMultiChoiceItems(mTopicsAvailable, mTopicsUserSelection
                        , new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        mTopicsUserSelection[i] = b;
                    }

                });
                builder.create().show();
            }
        });

        final FloatingActionButton photoPicker = mFragment.findViewById(R.id.challengePhotoPicker);
        photoPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mFragment.getContext());

                LinearLayout linearLayout = new LinearLayout(mFragment.getContext());
                linearLayout.setLayoutParams(
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT)
                );

                Button camera = new Button(mFragment.getContext());
                camera.setLayoutParams(
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT,1)
                );
                camera.setText(getString(R.string.camera));
                camera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent openCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(openCamera, Constants.RC_IMAGE_CAPTURE_FROM_CAMERA);
                    }
                });

                Button gallery = new Button(mFragment.getContext());
                gallery.setLayoutParams(
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT,1)
                );
                gallery.setText(getString(R.string.gallery));
                gallery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent openGallery = new Intent(Intent.ACTION_PICK);
                        openGallery.setType("image/*");
                        startActivityForResult(openGallery,Constants.RC_IMAGE_CAPTURE_FROM_GALLERY);
                    }
                });

                linearLayout.addView(camera);
                linearLayout.addView(gallery);

                builder.setView(linearLayout);
                builder.create().show();

            }
        });

        mDescription = mFragment.findViewById(R.id.challengeDescriptionInput);
        final TextInputLayout mDescriptionLayout = mFragment.findViewById(R.id.challengeDescriptionInputLayout);
        mSolutionExplanation = mFragment.findViewById(R.id.challengeSolutionExplanationInput);

        mPhoto = mFragment.findViewById(R.id.challengePhotoPreview);

        mDescriptionLayout.setVisibility(View.VISIBLE);
        photoPicker.setVisibility(View.GONE);
        mPhoto.setVisibility(View.GONE);

        mChoiceType = mFragment.findViewById(R.id.challengeChoiceInput);
        mChoiceType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                reRenderOptionsItemsLayouts(b);
            }
        });

        mPresentationType = mFragment.findViewById(R.id.challengePresentationTypeInput);
        mPresentationType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){//toggle button on > image option
                    photoPicker.setVisibility(View.VISIBLE);
                    mPhoto.setVisibility(View.VISIBLE);
                    mDescriptionLayout.setVisibility(View.GONE);
                }else{//toggle button off > text option
                    photoPicker.setVisibility(View.GONE);
                    mPhoto.setVisibility(View.GONE);
                    mDescriptionLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        renderChallengeOptionsFrame();

        return mFragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.create_challenge_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.saveChallenge:
                if(validateFormBeforeSubmit()){
                    if(mChallengeData.presentation.equals("image")){
                        UploadTask uploadTask = null;
                        try {
                             uploadTask = mCloudStorageService.uploadChallengeImage(mImageResource);
                             uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                 @Override
                                 public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    mChallengeData.sourceUrl = taskSnapshot.getDownloadUrl().toString();
                                     submitForm();
                                 }
                             });
                        }catch(IOException ex){

                        }
                    }else{
                        submitForm();
                    }

                    getActivity().getSupportFragmentManager().popBackStack("ChallengesFragment",POP_BACK_STACK_INCLUSIVE);
                }
                break;
        }
        return true;
    }

    @Override
    public void updateDataSet() {
        mLevelAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Constants.RC_IMAGE_CAPTURE_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            File imageFile = InternalStorageUtil.saveImage(imageBitmap,true, mFragment.getContext());
            mImageResource = Uri.parse(imageFile.getAbsolutePath());
            displayPhotoResolve(mImageResource);
        }else if(requestCode == Constants.RC_IMAGE_CAPTURE_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            mImageResource = data.getData();
            displayPhotoResolve(mImageResource);
        }
    }

    private void initTopics(){
        mTopicsUserSelection = new boolean[mTopicsDatabaseService.mTopics.size()];
        mTopicsAvailable = new String[mTopicsDatabaseService.mTopics.size()];
        for(int i = 0; i < mTopicsDatabaseService.mTopics.size(); i++){
            mTopicsAvailable[i] = mTopicsDatabaseService.mTopics.get(i);
            mTopicsUserSelection[i] = false;
        }
    }

    private void renderChallengeOptionsFrame() {
        mChallengeOptionsFrame = mFragment.findViewById(R.id.challengeOptionsContent);
        mOptionsItemsLayout = new TableLayout(mChallengeOptionsFrame.getContext());
//        Button addOption = new Button(mChallengeOptionsFrame.getContext());
//        addOption.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT
//                ,FrameLayout.LayoutParams.WRAP_CONTENT));
//        addOption.setText(mFragment.getResources().getString(R.string.addOption));
        Button addOption = mFragment.findViewById(R.id.addOption);
        addOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mOptionsItemsLayout.getChildCount() <= Constants.MAX_CHALLENGE_ANSWER_OPTONS) {
                    renderNewChallengeOption();
                }else{
                    Toast.makeText(mFragment.getContext(),
                            R.string.max_options_achieve,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        LinearLayout linearLayout = new LinearLayout(mChallengeOptionsFrame.getContext());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);

//        linearLayout.addView(addOption);
        linearLayout.addView(mOptionsItemsLayout);

        mChallengeOptionsFrame.addView(linearLayout);
    }

    private void renderNewChallengeOption(){
        TableRow option = new TableRow(mOptionsItemsLayout.getContext());
        option.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

        View selectableOption;
        if(mChoiceType.isChecked()) {
            selectableOption = new CheckBox(option.getContext());
        }else{
            selectableOption = generateRadioButton();
        }
        TextView optionKey = new TextView(option.getContext());
        optionKey.setText(Constants.ALPHABET[mOptionsItemsLayout.getChildCount()]);
        EditText optionValue = new EditText(option.getContext());
        optionValue.setHint(mFragment.getResources().getString(R.string.challenge_option_hint));

        option.addView(selectableOption);
        option.addView(optionKey);
        option.addView(optionValue);

        mOptionsItemsLayout.addView(option);
    }

    private void reRenderOptionsItemsLayouts(boolean multipleChoice){
        final int rowCount = mOptionsItemsLayout.getChildCount();
        for(int i = 0; i < rowCount; i++){
            TableRow row = (TableRow) mOptionsItemsLayout.getChildAt(i);//Object of TableRow type
            row.removeViewAt(0);
            View selectableOption;
            if(multipleChoice) {
                selectableOption = new CheckBox(row.getContext());
            }else{
                selectableOption = generateRadioButton();
            }
            row.addView(selectableOption,0);
        }
    }

    private RadioButton generateRadioButton(){
        RadioButton rb =new RadioButton(mOptionsItemsLayout.getContext());

        rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                for(int i = 0; i < mOptionsItemsLayout.getChildCount(); i++) {
                    TableRow row = (TableRow) mOptionsItemsLayout.getChildAt(i);//Object of TableRow type
                    ((RadioButton) row.getChildAt(0)).setChecked(false);
                }
                compoundButton.setChecked(b);
            }
        });

        return rb;
    }

    private boolean validateFormBeforeSubmit(){
        StringBuilder validationMessage = new StringBuilder("The following fields are required: \n");
        boolean valid = true;
        mChallengeData = new ChallengeModel();
        mChallengeData.creator = mUserDatabaseService.getLoggedInUserId();

        mChallengeData.level = mLevel.getSelectedItem().toString();

        mTopicsForNewChallenge = new ArrayList<>();
        for(int i = 0; i < mTopicsUserSelection.length; i++){
            if(mTopicsUserSelection[i]){
                mTopicsForNewChallenge.add(mTopicsAvailable[i]);
            }
        }
        if(mTopicsForNewChallenge.isEmpty()){
            validationMessage.append("\t").append(mFragment.getResources().getString(R.string.topics))
                    .append("\n");
            valid = false;
        }

        String points = mPoints.getText().toString();
        if(!points.equals("")) {
            mChallengeData.points = Integer.parseInt(points);
        }else{
            validationMessage.append("\t").append(mFragment.getResources().getString(R.string.points))
                    .append("\n");
            valid = false;
        }

        mChallengeData.multipleChoice = mChoiceType.isChecked();

        mChallengeData.presentation = mPresentationType.isChecked() ? "image" : "text";
        if(mPresentationType.isChecked()){
            if(mImageResource == null){
                validationMessage.append("\t").append(mFragment.getResources().getString(R.string.image_resource))
                        .append("\n");
                valid = false;
            }
        }else{
            String description = mDescription.getText().toString();
            if(!description.equals("")) {
                mChallengeData.description = description;
            }else{
                validationMessage.append("\t").append(mFragment.getResources().getString(R.string.description))
                        .append("\n");
                valid = false;
            }
        }

        int countAnswers = mOptionsItemsLayout.getChildCount();
        if(countAnswers > 0){
            String indexesCorrectAnswer = "";
            Map<String,String> answers = new HashMap<>();
            boolean correctAnswerSelected = false;
            boolean optionTextValid = true;
            for(int i = 0; i < countAnswers; i++){
                TableRow row = (TableRow) mOptionsItemsLayout.getChildAt(i);
                View selectionView = row.getChildAt(0);
                if(selectionView instanceof CheckBox){
                    if(((CheckBox) selectionView).isChecked()){
                        indexesCorrectAnswer += ((TextView) row.getChildAt(1)).getText().toString()+",";
                        correctAnswerSelected = true;
                    }
                }else if(selectionView instanceof RadioButton){
                    if(((RadioButton) selectionView).isChecked()){
                        indexesCorrectAnswer += ((TextView) row.getChildAt(1)).getText().toString()+",";
                        correctAnswerSelected = true;
                    }
                }
                String optionText = ((EditText) row.getChildAt(2)).getText().toString();
                if(optionText.equals("")){
                    optionTextValid = false;
                }else {
                    answers.put(
                            ((TextView) row.getChildAt(1)).getText().toString(),
                            optionText
                    );
                }
            }
            if(!optionTextValid){
                validationMessage.append("\t").append(mFragment.getResources().getString(R.string.answer_text))
                        .append("\n");
                valid = false;
            }else if(!correctAnswerSelected){
                validationMessage.append("\t").append(mFragment.getResources().getString(R.string.correct_answer))
                        .append("\n");
                valid = false;
            }else {
                mChallengeData.options = answers;
                mChallengeData.correctAnswer = indexesCorrectAnswer;
            }
        }else{
            validationMessage.append("\t").append(mFragment.getResources().getString(R.string.answers))
                    .append("\n");
            valid = false;
        }

        String solution = mSolutionExplanation.getText().toString();
        if(!solution.equals("")) {
            mChallengeData.solutionExplanation = solution;
        }else{
            validationMessage.append("\t").append(mFragment.getResources().getString(R.string.solution_explanation))
                    .append("\n");
            valid = false;
        }

        if(!valid){
            ViewUtility.displayTextPopup(mFragment.getResources().getString(R.string.alert),
                    validationMessage.append("\t").toString(),mFragment.getContext());
        }
        return valid;
    }

    private void submitForm(){
        mChallengeDatabaseService.addNewChallenge(mChallengeData,mTopicsForNewChallenge);
        Toast.makeText(mFragment.getContext(), R.string.create_challenge_success,Toast.LENGTH_LONG).show();
    }

    private void displayPhotoResolve(Uri uri){
        mPhoto.setVisibility(View.VISIBLE);
        mPhoto.setImageURI(uri);
    }
}
