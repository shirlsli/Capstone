package com.example.capstone;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.capstone.fragments.CreatePoemFragment;
import com.example.capstone.fragments.PoemDetailsFragment;
import com.example.capstone.models.Poem;
import com.example.capstone.models.Post;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.EventListener;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private Context context;
    private List<String> friendsLines;
    EventListener eventListener;
    private static final String TAG = "SearchAdapter";

    public interface EventListener {
        void onEvent(String data);
    }

    public SearchAdapter(Context context, List<String> friendsLines, EventListener eventListener) {
        this.context = context;
        this.friendsLines = friendsLines;
        this.eventListener = eventListener;
    }

    public void clear() {
        friendsLines.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SearchAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_line, parent, false);
        return new SearchAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchAdapter.ViewHolder holder, int position) {
        String friendsLine = friendsLines.get(position);
        holder.bind(friendsLine);
    }

    @Override
    public int getItemCount() {
        return friendsLines.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tvFriendsLine;

        public ViewHolder(@NonNull View itemView) throws NullPointerException {
            super(itemView);
            itemView.setOnClickListener(this);
            tvFriendsLine = itemView.findViewById(R.id.tvPoemLine);
        }

        public void bind(String friendsLine) {
            tvFriendsLine.setText(friendsLine);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            FragmentManager fragmentManager = ((AppCompatActivity)context).getSupportFragmentManager();

            if (position != RecyclerView.NO_POSITION) {
                String text = friendsLines.get(position);
                eventListener.onEvent(text);
            }
        }
    }
}
