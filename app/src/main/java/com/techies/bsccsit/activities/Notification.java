package com.techies.bsccsit.activities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.devspark.robototextview.widget.RobotoTextView;
import com.techies.bsccsit.R;
import com.techies.bsccsit.adapters.NotificationAdapter;
import com.techies.bsccsit.advance.Singleton;
import com.techies.bsccsit.networking.NotificationDownloader;

import java.util.ArrayList;

public class Notification extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recy;
    private RobotoTextView noNotifications;

    private NotificationAdapter adapter;

    ArrayList<String> title = new ArrayList<>(),
            desc = new ArrayList<>(),
            link = new ArrayList<>();

    ArrayList<Integer> show = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        toolbar = (Toolbar) findViewById(R.id.notifToolbar);
        recy = (RecyclerView) findViewById(R.id.recyNotif);
        noNotifications = (RobotoTextView) findViewById(R.id.noNotifIcon);

        setSupportActionBar(toolbar);

        setTitle("Notifications");
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadFromDb();

    }

    private void loadFromDb() {
        SQLiteDatabase database = Singleton.getInstance().getDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM notifications", null);
        title.clear();
        desc.clear();
        show.clear();
        link.clear();
        int i = 0;
        while (cursor.moveToNext()) {
            i++;
            title.add(cursor.getString(cursor.getColumnIndex("title")));
            desc.add(cursor.getString(cursor.getColumnIndex("desc")));
            show.add(cursor.getInt(cursor.getColumnIndex("show")));
            link.add(cursor.getString(cursor.getColumnIndex("link")));
        }
        cursor.close();
        if (i == 0) {
            noNotifications.setVisibility(View.VISIBLE);
        } else
            fillRecy();
    }


    private void fillRecy() {
        adapter = new NotificationAdapter(this,title,desc,link);
        recy.setLayoutManager(new LinearLayoutManager(this));
        recy.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }
}