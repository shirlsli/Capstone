package com.example.capstone;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private Context context;
    private List<String> friendsLines;
    private ArrayList<String> selectedLines;
    private boolean selectOn;
    EventListener onClickEventListener;
    EventListener onLongClickEventListener;

    public interface EventListener {
        void onEvent(ArrayList<String> data);
    }

    public boolean getSelectOn() {
        return selectOn;
    }

    public void setSelectOn(boolean selectOn) {
        this.selectOn = selectOn;
    }

    public SearchAdapter(Context context, List<String> friendsLines, EventListener eventListener, EventListener longClickEventListener) {
        this.context = context;
        this.friendsLines = friendsLines;
        this.onClickEventListener = eventListener;
        this.onLongClickEventListener = longClickEventListener;
        selectedLines = new ArrayList<>();
        selectOn = false;
    }

    public void clear() {
        friendsLines.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<String> list) {
        friendsLines.addAll(list);
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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private TextView tvFriendsLine;

        public ViewHolder(@NonNull View itemView) throws NullPointerException {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            tvFriendsLine = itemView.findViewById(R.id.tvPoemLine);
        }

        public void bind(String friendsLine) {
            tvFriendsLine.setText(friendsLine);
            tvFriendsLine.setTextColor(Color.parseColor("#404040"));
        }

        @Override
        public boolean onLongClick(View view) {
            int position = getAdapterPosition();

            if (position != RecyclerView.NO_POSITION) {
                selectOn = true;
                String text = friendsLines.get(position);
                TextView tvSelected = view.findViewById(R.id.tvPoemLine);
                tvSelected.setTextColor(Color.parseColor("#a6a6a6"));
                selectedLines.add(text);
                onLongClickEventListener.onEvent(selectedLines);
            }
            return true;
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();

            if (position != RecyclerView.NO_POSITION) {
                String text = friendsLines.get(position);
                TextView tvSelected = view.findViewById(R.id.tvPoemLine);
                tvSelected.setTextColor(Color.parseColor("#a6a6a6"));
                selectedLines.add(text);
                if (!selectOn) {
                    onClickEventListener.onEvent(selectedLines);
                    selectedLines.removeAll(selectedLines);
                }
            } // delete multiple: check for onLongPress, allow them to tap selected lines, then press checkmark button
        }
    }
}
