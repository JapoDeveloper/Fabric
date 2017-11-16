package co.japo.fabric.ui.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
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

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import co.japo.fabric.Constants;
import co.japo.fabric.R;
import co.japo.fabric.database.LevelDatabaseService;
import co.japo.fabric.database.TopicDatabaseService;
import co.japo.fabric.interfaces.DataSetUpdatable;


/**
 * A simple {@link Fragment} subclass.
 */
public class CreateChallengeFragment extends Fragment implements DataSetUpdatable {

    private LevelDatabaseService mLevelDatabaseService;
    private TopicDatabaseService mTopicsDatabaseService;

    private ArrayAdapter<String> mLevelAdapter;

    private ImageView mPhoto;
    private FrameLayout mChallengeOptionsFrame;
    private TableLayout mOptionsItemsLayout;
    private View mFragment;
    private ToggleButton mChoiceType;

    private String[] mTopicsAvailable;
    private boolean[] mTopicsUserSelection;

    public CreateChallengeFragment() {
        // Required empty public constructor
        mLevelDatabaseService = LevelDatabaseService.getInstance();
        mLevelDatabaseService.setUpdateDataSetDelegate(this);

        mTopicsDatabaseService = TopicDatabaseService.getInstance();
        mTopicsDatabaseService.setUpdateDataSetDelegate(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mFragment = inflater.inflate(R.layout.fragment_create_challenge, container, false);

        Spinner level = mFragment.findViewById(R.id.challengeLevelInput);

        mLevelAdapter = new ArrayAdapter<>(mFragment.getContext(),android.R.layout.simple_spinner_item,mLevelDatabaseService.mLevelsAsList);
        mLevelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        level.setAdapter(mLevelAdapter);
        level.setSelection(0);

        initTopics();

        ImageButton topics = mFragment.findViewById(R.id.challengeTopicsPicker);
        topics.setOnClickListener(new View.OnClickListener() {
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

        final EditText description = mFragment.findViewById(R.id.challengeDescriptionInput);
        final Button photoPickerCamera = mFragment.findViewById(R.id.challengePickPhotoCamera);
        final Button photoPickerGallery = mFragment.findViewById(R.id.challengePickPhotoGallery);
        mPhoto = mFragment.findViewById(R.id.challengePhotoPreview);

        description.setVisibility(View.VISIBLE);
        photoPickerCamera.setVisibility(View.GONE);
        photoPickerGallery.setVisibility(View.GONE);
        mPhoto.setVisibility(View.GONE);

        mChoiceType = mFragment.findViewById(R.id.challengeChoiceInput);
        mChoiceType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                reRenderOptionsItemsLayouts(b);
            }
        });

        ToggleButton presentationType = mFragment.findViewById(R.id.challengePresentationTypeInput);
        presentationType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){//toggle button on > image option
                    photoPickerCamera.setVisibility(View.VISIBLE);
                    photoPickerGallery.setVisibility(View.VISIBLE);
                    mPhoto.setVisibility(View.GONE);
                    description.setVisibility(View.GONE);
                }else{//toggle button off > text option
                    photoPickerCamera.setVisibility(View.GONE);
                    photoPickerGallery.setVisibility(View.GONE);
                    mPhoto.setVisibility(View.GONE);
                    description.setVisibility(View.VISIBLE);
                }
            }
        });

        renderChallengeOptionsFrame();

        photoPickerCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(openCamera, Constants.RC_IMAGE_CAPTURE_FROM_CAMERA);
            }
        });

        photoPickerGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openGallery = new Intent(Intent.ACTION_PICK);
                openGallery.setType("image/*");
                startActivityForResult(openGallery,Constants.RC_IMAGE_CAPTURE_FROM_GALLERY);
            }
        });

        return mFragment;
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
        Button addOption = new Button(mChallengeOptionsFrame.getContext());
        addOption.setText(mFragment.getResources().getString(R.string.addOption));
        addOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                renderNewChallengeOption();
            }
        });
        LinearLayout linearLayout = new LinearLayout(mChallengeOptionsFrame.getContext());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        linearLayout.addView(addOption);
        linearLayout.addView(mOptionsItemsLayout);

        mChallengeOptionsFrame.addView(linearLayout);
    }

    private void renderNewChallengeOption(){
        TableRow option = new TableRow(mOptionsItemsLayout.getContext());
        option.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));

        View selectableOption;
        if(mChoiceType.isChecked()) {
            selectableOption = new CheckBox(option.getContext());
        }else{
            selectableOption = generateRadioButton();
        }
        TextView optionKey = new TextView(option.getContext());
        optionKey.setText(mOptionsItemsLayout.getChildCount()+1+"");
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
            displayPhotoResolve(imageBitmap);
        }else if(requestCode == Constants.RC_IMAGE_CAPTURE_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri imageLocation = data.getData();
            displayPhotoResolve(imageLocation);
        }
    }

    private void displayPhotoResolve(Bitmap bitmap){
        mPhoto.setVisibility(View.VISIBLE);
        mPhoto.setImageBitmap(bitmap);
    }

    private void displayPhotoResolve(Uri uri){
        mPhoto.setVisibility(View.VISIBLE);
        mPhoto.setImageURI(uri);
    }
}
