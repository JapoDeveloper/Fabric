package co.japo.fabric.ui.fragments;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Map;

import co.japo.fabric.R;
import co.japo.fabric.database.ChallengeDatabaseService;
import co.japo.fabric.interfaces.DataSetUpdatable;
import co.japo.fabric.interfaces.Displayable;
import co.japo.fabric.model.ChallengeModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChallengesFragment extends Fragment implements DataSetUpdatable {

    private ChallengeDatabaseService mChallengeDatabaseService;
    private ChallengesAdapter mChallengesAdapter;
    private Displayable mDisplayable;


    public ChallengesFragment() {
        // Required empty public constructor
        mChallengeDatabaseService = ChallengeDatabaseService.getInstance();
        mChallengeDatabaseService.setUpdateDataSetDelegate(this);
    }

    public void setDisplayable(Displayable displayable){
        this.mDisplayable = displayable;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.fragment_challenges, container, false);

        mChallengesAdapter = new ChallengesAdapter(mChallengeDatabaseService.mChallenges);

        RecyclerView challengesList = (RecyclerView) fragment.findViewById(R.id.challengesList);
        challengesList.setLayoutManager(
                new LinearLayoutManager(fragment.getContext(),LinearLayoutManager.VERTICAL,false)
        );
        challengesList.setAdapter(mChallengesAdapter);

        FloatingActionButton addChallenge = (FloatingActionButton) fragment.findViewById(R.id.addChallenge);
        addChallenge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDisplayable.display(CreateChallengeFragment.class,"ChallengesFragment", null);
            }
        });
        return fragment;
    }

    @Override
    public void updateDataSet() {
        mChallengesAdapter.notifyDataSetChanged();
    }

    private class ChallengesAdapter extends RecyclerView.Adapter<ChallengesViewHolder>{
        private Map<String,ChallengeModel> mChallenges;

        public ChallengesAdapter(Map<String,ChallengeModel> challenges){
            this.mChallenges = challenges;
        }

        @Override
        public ChallengesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ChallengesViewHolder(
                    LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.challenge_item,null)
            );
        }

        @Override
        public void onBindViewHolder(final ChallengesViewHolder holder, final int position) {
            final ChallengeModel challenge = (ChallengeModel) mChallenges.values()
                    .toArray()[position];
            if(challenge.presentation.equals("text")){
                holder.challengeDescription.setText(challenge.description);
                holder.challengeDescription.setVisibility(View.VISIBLE);
                holder.challengePhoto.setVisibility(View.GONE);
            }else if (challenge.presentation.equals("image")){
                holder.challengeDescription.setVisibility(View.GONE);
                holder.challengePhoto.setVisibility(View.VISIBLE);
                Glide.with(holder.challengePhoto)
                        .load(challenge.sourceUrl)
                        .into(holder.challengePhoto);
            }
            holder.challengeLevel.setText(holder.itemView.getResources().getString(R.string.level)+"\n"+challenge.level);
            holder.getChallengePoints.setText(holder.itemView.getResources().getString(R.string.points)+"\n"+challenge.points);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(mDisplayable != null) {
                        Bundle bundle = new Bundle();
                        bundle.putString("challengeKey",mChallenges.keySet().toArray()[position].toString());
                        mDisplayable.display(TakeChallengeFragment.class,"ChallengesFragment",bundle);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mChallenges.size();
        }

    }

    private class ChallengesViewHolder extends RecyclerView.ViewHolder{
        public ImageView challengePhoto;
        public TextView challengeDescription;
        public TextView challengeLevel;
        public TextView getChallengePoints;

        public ChallengesViewHolder(View itemView){
            super(itemView);
            this.challengePhoto = (ImageView) itemView.findViewById(R.id.challengePhoto);
            this.challengeDescription = (TextView) itemView.findViewById(R.id.challengeDescription);
            this.challengeLevel = (TextView) itemView.findViewById(R.id.challengeLevel);
            this.getChallengePoints = (TextView) itemView.findViewById(R.id.challengePoints);
        }
    }
}
