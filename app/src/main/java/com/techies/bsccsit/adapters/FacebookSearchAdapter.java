package com.techies.bsccsit.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.devspark.robototextview.widget.RobotoTextView;
import com.squareup.picasso.Picasso;
import com.techies.bsccsit.R;
import com.techies.bsccsit.activities.FbPage;
import com.techies.bsccsit.advance.Singleton;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import mehdi.sakout.fancybuttons.FancyButton;

public class FacebookSearchAdapter extends RecyclerView.Adapter<FacebookSearchAdapter.ViewHolder>{

    private final Context context;
    private final String allOrmy;
    private LayoutInflater inflater;

    ArrayList<String> names=new ArrayList<>(),
            ids=new ArrayList<>(),
            extra=new ArrayList<>();
    ArrayList<Boolean> verified=new ArrayList<>();
    ClickListener clickListener;


    public FacebookSearchAdapter(Context context,String appOrmy ,ArrayList<String> names,ArrayList<String> extra,ArrayList<String> ids,ArrayList<Boolean> verified){
        this.context=context;
        this.names=names;
        this.ids=ids;
        this.extra=extra;
        this.verified=verified;
        this.allOrmy=appOrmy;
        inflater=LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.fb_search_each_item,parent,false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Picasso.with(context).load("https://graph.facebook.com/"+ids.get(position)+"/picture?type=large").into(holder.profilePictureView);
        holder.nameView.setText(names.get(position));

        if(verified.get(position))
            holder.isVerified.setVisibility(View.VISIBLE);
        else
            holder.isVerified.setVisibility(View.GONE);

            holder.extraDetail.setText(extra.get(position));
        if ((allOrmy.equals("my") && Singleton.checkExistInFollowing(ids.get(position))) ||
                (allOrmy.equals("all") && Singleton.checkExistInPopular(ids.get(position))) ){
            holder.addToDB.setIconResource(R.drawable.cross);
            holder.addToDB.setText("Unfollow");
            holder.addToDB.setBackgroundColor(ContextCompat.getColor(context,R.color.unfollowColor));
            holder.addToDB.setFocusBackgroundColor(ContextCompat.getColor(context,R.color.unfollowColorTrans));
        }else {
            holder.addToDB.setIconResource(R.drawable.plus);
            holder.addToDB.setText("Follow");
            holder.addToDB.setBackgroundColor(ContextCompat.getColor(context,R.color.colorPrimary));
            holder.addToDB.setFocusBackgroundColor(ContextCompat.getColor(context,R.color.colorPrimaryTrans));
        }
    }

    public void setOnClickListener(ClickListener clickListener){
        this.clickListener=clickListener;
    }

    public void removeItem(int position) {
        names.remove(position);
        ids.remove(position);
        extra.remove(position);
        verified.remove(position);
        notifyItemRemoved(position);
    }

    public void addItem(String name,String id,String extr,boolean verify) {
        names.add(name);
        ids.add(id);
        extra.add(extr);
        verified.add(verify);
        notifyItemInserted(extra.size()-1);
    }
    public void removeBySearch(String id){
        for (int i=0;i<ids.size();i++){
            if (ids.get(i).equals(id))
                removeItem(i);
        }

    }

    public interface ClickListener{
        void onClick(FancyButton view,int position);
    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profilePictureView;
        RobotoTextView nameView,extraDetail;
        ImageView isVerified;
        FancyButton addToDB;
        public ViewHolder(View itemView) {
            super(itemView);
            profilePictureView= (CircleImageView) itemView.findViewById(R.id.profileImage);
            nameView= (RobotoTextView) itemView.findViewById(R.id.nameSearch);
            isVerified= (ImageView) itemView.findViewById(R.id.isVerified);
            addToDB= (FancyButton) itemView.findViewById(R.id.viewProfile);
            extraDetail= (RobotoTextView) itemView.findViewById(R.id.extraDetail);
            addToDB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onClick(addToDB,getAdapterPosition());
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(context, FbPage.class)
                            .putExtra("id",ids.get(getAdapterPosition()))
                            .putExtra("name",names.get(getAdapterPosition())));
                }
            });
        }
    }
}