package com.techies.bsccsit.advance;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.format.DateUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Singleton {
    private static Singleton sInstance = null;
    private RequestQueue mRequestQueue;
    private DatabaseHandler mDatabase;


    private Singleton() {
        mDatabase = new DatabaseHandler(MyApp.getContext());
        mRequestQueue = Volley.newRequestQueue(MyApp.getContext());
    }

    public static Singleton getInstance() {
        if (sInstance == null) {
            sInstance = new Singleton();
        }
        return sInstance;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public SQLiteDatabase getDatabase() {
        return mDatabase.getWritableDatabase();
    }

    public static void downloadElibrary(){
        JsonArrayRequest request=new JsonArrayRequest(Request.Method.POST, "https://slim-bloodskate.c9users.io/app/api/elibrary", new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                SQLiteDatabase database= Singleton.getInstance().getDatabase();
                database.delete("eLibrary",null,null);
                ContentValues values=new ContentValues();
                for(int i=0;i<response.length();i++){
                    try {
                        values.put("Title", response.getJSONObject(i).getString("title"));
                        values.put("Source", response.getJSONObject(i).getString("source"));
                        values.put("Tag", response.getJSONObject(i).getString("tag"));
                        values.put("Link", response.getJSONObject(i).getString("link"));
                        values.put("Link", response.getJSONObject(i).getString("filename"));
                        database.insert("eLibrary",null,values);
                        values.clear();
                    }catch (Exception e){}
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("semester", MyApp.getContext().getSharedPreferences("loginInfo", Context.MODE_PRIVATE).getInt("semester",0)+"");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        Singleton.getInstance().getRequestQueue().add(request);
    }

    public static boolean checkExistInFollowing(String id){
        Cursor cursor = Singleton.getInstance().getDatabase().rawQuery("SELECT Title FROM myCommunities WHERE FbID = "+id,null);
        if (cursor.moveToNext()){
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    public static boolean checkExistInPopular(String id){
        Cursor cursor = Singleton.getInstance().getDatabase().rawQuery("SELECT Title FROM popularCommunities WHERE FbID = "+id,null);
        if (cursor.moveToNext()){
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    public static String getFollowingList(){
        Cursor cursor = Singleton.getInstance().getDatabase().rawQuery("SELECT FbID FROM myCommunities",null);
        String string="";
        while(cursor.moveToNext()){
            string=string+cursor.getString(cursor.getColumnIndex("FbID"))+",";
        }
        string=string+"bsccsitapp";
        cursor.close();
        return string;
    }

    public static ArrayList<String> getFollowingArray(){
        Cursor cursor = Singleton.getInstance().getDatabase().rawQuery("SELECT FbID FROM myCommunities",null);
        ArrayList<String> names=new ArrayList<>();
        while(cursor.moveToNext()){
            names.add(cursor.getString(cursor.getColumnIndex("FbID")));
        }
        names.add("bsccsitapp");
        cursor.close();
        return names;
    }

    public static CharSequence convertToSimpleDate(String created_time) {

        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZZZZZ", Locale.US);
        try {
            Date date = simpleDateFormat.parse(created_time);
            return DateUtils.getRelativeTimeSpanString(date.getTime(),System.currentTimeMillis(),DateUtils.SECOND_IN_MILLIS,DateUtils.FORMAT_ABBREV_RELATIVE);
        } catch (Exception e) {
            return "Unknown Time";
        }
    }
}