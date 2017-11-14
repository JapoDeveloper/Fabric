package co.japo.fabric.ui.fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.regex.Pattern;

import co.japo.fabric.R;
import co.japo.fabric.database.TopicDatabaseService;
import co.japo.fabric.interfaces.DataSetUpdatable;

/**
 * A simple {@link Fragment} subclass.
 */
public class TopicsFragment extends Fragment implements DataSetUpdatable {

    private TopicDatabaseService mTopicDatabaseService;
    private TopicsAdapter mTopicsAdapter;

    public TopicsFragment() {
        // Required empty public constructor
        mTopicDatabaseService = TopicDatabaseService.getInstance();
        mTopicDatabaseService.setUpdateDataSetDelegate(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       final View fragment = inflater.inflate(R.layout.fragment_topics, container, false);

        mTopicsAdapter = new TopicsAdapter(mTopicDatabaseService.mTopics);

        RecyclerView topicsList = (RecyclerView) fragment.findViewById(R.id.topicsList);
        topicsList.setLayoutManager(
                new LinearLayoutManager(fragment.getContext(),LinearLayoutManager.VERTICAL,false)
        );
        topicsList.setAdapter(
            mTopicsAdapter
        );

        FloatingActionButton addTopic = (FloatingActionButton) fragment.findViewById(R.id.addTopic);
        addTopic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText editText = new EditText(view.getContext());
                final Pattern sPattern
                        = Pattern.compile("^(?=.*[A-Za-z0-9])[A-Za-z0-9 _]*$");

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Topic name");
                builder.setView(editText);
                builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(sPattern.matcher(editText.getText().toString()).matches()) {
                            String userInput = editText.getText().toString();
                            mTopicDatabaseService.addTopic(userInput);
                            Toast.makeText(fragment.getContext(),"Topic saved!",Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(fragment.getContext(),"Invalid input",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.show();
            }
        });
        return fragment;
    }

    @Override
    public void updateDataSet() {
        mTopicsAdapter.notifyDataSetChanged();
    }

    private class TopicsAdapter extends RecyclerView.Adapter<TopicViewHolder>{

        private List<String> topics;

        public TopicsAdapter(List<String> items) {
            this.topics = items;
        }

        @Override
        public TopicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new TopicViewHolder(
                    LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.single_line_item, null)
            );
        }

        @Override
        public void onBindViewHolder(TopicViewHolder holder, int position) {
            String item = this.topics.get(position);
            holder.topicName.setText(item);
        }

        @Override
        public int getItemCount() {
            return topics.size();
        }
    }

    private class TopicViewHolder extends RecyclerView.ViewHolder{
        public TextView topicName;

        public TopicViewHolder(View itemView) {
            super(itemView);
            this.topicName = (TextView) itemView.findViewById(R.id.itemTitle);
        }
    }
}
