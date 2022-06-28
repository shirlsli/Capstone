//package com.example.capstone;
//
//import android.content.Context;
//import android.net.Uri;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.fragment.app.Fragment;
//import androidx.fragment.app.FragmentManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
//import com.example.capstone.fragments.PoemDetailsFragment;
//import com.example.capstone.models.Poem;
//import com.example.capstone.models.Post;
//import com.parse.ParseFile;
//
//import java.text.DateFormat;
//import java.util.Date;
//import java.util.List;
//
//public class ArchiveAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {
//
//    private Context context;
//    private List<Poem> poems;
//
//    public ArchiveAdapter(Context context, List<Poem> poems) {
//        this.context = context;
//        this.poems = poems;
//    }
//
//    @NonNull
//    @Override
//    public ArchiveAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(context).inflate(R.layout.item_poem, parent, false);
//        return new ArchiveAdapter.ViewHolder(view);
//    }
//    @Override
//    public void onBindViewHolder(@NonNull PostsAdapter.ViewHolder holder, int position) {
//        Poem poem = poems.get(position);
//        holder.bind(poem);
//    }
//
//    @Override
//    public int getItemCount() {
//        return poems.size();
//    }
//
//    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
//
//        private TextView tvDate;
//        private TextView tvPoemPreview;
//
//        public ViewHolder(@NonNull View itemView) throws NullPointerException {
//            super(itemView);
//            itemView.setOnClickListener(this);
//            tvDate = itemView.findViewById(R.id.tvDate);
//            tvPoemPreview = itemView.findViewById(R.id.tvPoemPreview);
//        }
//
//        public void bind(Poem poem) {
//            Date date = poem.getCreatedAt();
//            DateFormat df = DateFormat.getDateInstance();
//            String reportDate = df.format(date);
//            tvDate.setText(reportDate);
//            tvPoemPreview.setText(poem.getPoemLines().get(0));
//        }
//
//        @Override
//        public void onClick(View v) {
//            int position = getAdapterPosition();
//            FragmentManager fragmentManager = ((AppCompatActivity)context).getSupportFragmentManager();
//
//            if (position != RecyclerView.NO_POSITION) {
//                Poem poem = poems.get(position);
//                Fragment poemDetailsFragment = new PoemDetailsFragment();
//                Bundle bundle = new Bundle();
//                bundle.putParcelable("Poem", poem);
//                Log.i("bundle_post_poem", "Parcelled item: " + poem);
//                poemDetailsFragment.setArguments(bundle);
//                fragmentManager.beginTransaction().replace(R.id.flContainer, poemDetailsFragment).commit();
//            }
//        }
//    }
//}
