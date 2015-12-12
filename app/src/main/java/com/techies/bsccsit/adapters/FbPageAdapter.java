package com.techies.bsccsit.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;
import com.squareup.picasso.Picasso;
import com.techies.bsccsit.R;
import com.techies.bsccsit.activities.FbPage;
import com.techies.bsccsit.activities.ImageViewActivity;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FbPageAdapter extends RecyclerView.Adapter<FbPageAdapter.VH> {
    private Context context;
    LayoutInflater inflater;
    ArrayList<String> names, time, ids, message, imageURL;

    public FbPageAdapter(Context context, ArrayList<String> names, ArrayList<String> time, ArrayList<String> ids, ArrayList<String> messages, ArrayList<String> imageURL) {
    inflater = LayoutInflater.from(context);
        this.names = names;
        this.time = time;
        this.ids = ids;
        this.message = messages;
        this.imageURL = imageURL;
    }

    @Override
    public FbPageAdapter.VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VH(inflater.inflate(R.layout.fbpage_each_post,parent,false));
    }

    @Override
    public void onBindViewHolder(FbPageAdapter.VH holder, final int position) {
        holder.nameHolder.setText(names.get(position));
        holder.timeHolder.setText(time.get(position));

        if (message.get(position).equals(""))
            holder.messageHolder.setVisibility(View.GONE);
        else {
            holder.messageHolder.setText(message.get(position));
            holder.messageHolder.setVisibility(View.VISIBLE);
        }

        if(imageURL.get(position).equals(""))
            holder.imageHolder.setVisibility(View.GONE);
        else {
            Picasso.with(context).load(imageURL.get(position)).into(holder.imageHolder);
            holder.imageHolder.setVisibility(View.VISIBLE);
            holder.imageHolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(context, ImageViewActivity.class)
                            .putExtra("ImageURL",imageURL.get(position))
                            .putExtra("des",message.get(position)));
                }
            });
        }
        holder.profilePicHolder.setProfileId(ids.get(position));

    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    public class VH extends RecyclerView.ViewHolder {
        TextView nameHolder, timeHolder, messageHolder;
        ImageView imageHolder;
        ProfilePictureView profilePicHolder;

        public VH(View itemView) {
            super(itemView);
            nameHolder= (TextView) itemView.findViewById(R.id.nameOfPoster);
            timeHolder= (TextView) itemView.findViewById(R.id.timeOfPost);
            messageHolder= (TextView) itemView.findViewById(R.id.messageOfPost);
            imageHolder= (ImageView) itemView.findViewById(R.id.imageOfPost);
            profilePicHolder= (ProfilePictureView) itemView.findViewById(R.id.imageOfPoster);
        }
    }
}