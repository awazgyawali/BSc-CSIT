package com.techies.bsccsit.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.devspark.robototextview.widget.RobotoTextView;
import com.squareup.picasso.Picasso;
import com.techies.bsccsit.R;
import com.techies.bsccsit.activities.FbEvent;
import com.techies.bsccsit.activities.FbPage;
import com.techies.bsccsit.activities.MainActivity;
import com.techies.bsccsit.advance.Singleton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.VH> {
    private final Context context;
    LayoutInflater inflater;
    private int noOfUpcomming;
    ArrayList<String> names, time, hoster, imageURL,eventsIds;
    int header=1,card=0;

    public EventAdapter(Context context,int noOfUpcomming, ArrayList<String> names, ArrayList<String> eventsIds, ArrayList<String> time
            , ArrayList<String> hoster, ArrayList<String> imageURL) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.names = names;
        this.time = time;
        this.noOfUpcomming=noOfUpcomming;
        this.eventsIds=eventsIds;
        this.hoster = hoster;
        this.imageURL = imageURL;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType==header)
            return new VH((inflater.inflate(R.layout.header_text_view,parent,false)));
        else
            return new VH(inflater.inflate(R.layout.events_each_post, parent, false));
    }

    @Override
    public void onBindViewHolder(VH holder, final int position) {
        if (position==0 && noOfUpcomming!=0)
            holder.headerText.setText("Upcoming events");
        else if(position==0 && noOfUpcomming==0)
            holder.headerText.setText("No upcoming events");
        else if (position==(noOfUpcomming+1) && names.size()>noOfUpcomming)
            holder.headerText.setText("Past events");
        else if (position==(noOfUpcomming+1) && names.size()==noOfUpcomming)
            holder.headerText.setText("No Past events");
        else if(position<=noOfUpcomming)
            fillCard(holder,position-1,true);
        else if (position>noOfUpcomming)
            fillCard(holder,position-2,false);
    }

    public void fillCard(final VH holder, final int position,boolean isUpcoming){

        holder.nameHolder.setText(names.get(position));

        holder.monthHolder.setText(getMonth(time.get(position)));
        holder.dayHolder.setText(getDay(time.get(position)));

        holder.hosterHolder.setText("Hosted by: " + hoster.get(position));

        if (imageURL.get(position).equals(""))
            holder.imageHolder.setVisibility(View.GONE);
        else {
            Picasso.with(context).load(imageURL.get(position)).into(holder.imageHolder);
            holder.imageHolder.setVisibility(View.VISIBLE);
        }

        if(isUpcoming)
            holder.addToSchedule.setVisibility(View.VISIBLE);
        else
            holder.addToSchedule.setVisibility(View.GONE);

        if(Singleton.isScheduledEvent(eventsIds.get(position))){
            holder.addToSchedule.setImageResource(R.drawable.calender_check);
            holder.addToSchedule.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Snackbar.make(MainActivity.drawerLayout,"Remainder removed.",Snackbar.LENGTH_SHORT).show();
                    Singleton.getInstance().getDatabase().execSQL("DELETE FROM remainder WHERE eventID = "+eventsIds.get(position));
                    notifyItemChanged(position+1);
                }
            });
        } else {
            holder.addToSchedule.setImageResource(R.drawable.calender_plus);
            holder.addToSchedule.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Snackbar.make(MainActivity.drawerLayout,"Remainder scheduled.",Snackbar.LENGTH_SHORT).show();
                    ContentValues values=new  ContentValues();
                    values.put("eventID",eventsIds.get(position));
                    values.put("created_time",time.get(position));
                    Singleton.getInstance().getDatabase().insert("remainder",null,values);
                    notifyItemChanged(position+1);
                }
            });
        }
        holder.eachEventCore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, FbEvent.class)
                        .putExtra("eventID",eventsIds.get(position))
                        .putExtra("eventName",names.get(position))
                        .putExtra("imageURL",imageURL.get(position))
                        .putExtra("eventTime",time.get(position))
                        .putExtra("eventHost","Hosted By: "+ hoster.get(position)));
            }
        });
    }

    public String getMonth(String created_time) {
        Date date = convertToSimpleDate(created_time);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int month = cal.get(Calendar.MONTH);
        switch (month) {
            case 0:
                return "JAN";
            case 1:
                return "FEB";
            case 2:
                return "MAR";
            case 3:
                return "APR";
            case 4:
                return "MAY";
            case 5:
                return "JUN";
            case 6:
                return "JUL";
            case 7:
                return "AUG";
            case 8:
                return "SEP";
            case 9:
                return "OCT";
            case 10:
                return "NOV";
            case 11:
                return "DEC";
            default:
                return "";
        }
    }

    public String getDay(String created_time) {
        Date date = convertToSimpleDate(created_time);

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_MONTH) + "";

    }

    private Date convertToSimpleDate(String created_time) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZZZZZ", Locale.US);
        try {
            return simpleDateFormat.parse(created_time);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public int getItemCount() {
        return names.size()+2;
    }

    @Override
    public int getItemViewType(int position) {
        if(position==0)
            return header;
        else if (position==(noOfUpcomming+1))
            return header;
        return card;
    }

    public class VH extends RecyclerView.ViewHolder {
        RobotoTextView nameHolder, monthHolder, dayHolder, hosterHolder,headerText;
        ImageView imageHolder, addToSchedule;
        LinearLayout eachEventCore;


        public VH(View itemView) {
            super(itemView);
            nameHolder = (RobotoTextView) itemView.findViewById(R.id.nameOfEvent);
            monthHolder = (RobotoTextView) itemView.findViewById(R.id.monthTxt);
            dayHolder = (RobotoTextView) itemView.findViewById(R.id.dayTxt);
            hosterHolder = (RobotoTextView) itemView.findViewById(R.id.eventHoster);
            addToSchedule = (ImageView) itemView.findViewById(R.id.addToCalender);
            imageHolder= (ImageView) itemView.findViewById(R.id.eventCoverImage);
            headerText= (RobotoTextView) itemView.findViewById(R.id.headerTextView);
            eachEventCore= (LinearLayout) itemView.findViewById(R.id.eachEventCore);
        }
    }
}
