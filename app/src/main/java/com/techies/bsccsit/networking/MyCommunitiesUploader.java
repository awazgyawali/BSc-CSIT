package com.techies.bsccsit.networking;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.techies.bsccsit.advance.MyApp;
import com.techies.bsccsit.advance.Singleton;

import java.util.HashMap;
import java.util.Map;

public class MyCommunitiesUploader {

    public void doInBackground() {
        String url = "http://bsccsit.brainants.com/updateusercommunities";
        final StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                MyApp.getContext().getSharedPreferences("community", Context.MODE_PRIVATE).edit().putBoolean("changedComm", false).apply();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("fbid", MyApp.getContext().getSharedPreferences("loginInfo", Context.MODE_PRIVATE).getString("UserID", ""));
                params.put("communities", Singleton.getFollowingList().replace(",bsccsitapp", ""));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        if (MyApp.getContext().getSharedPreferences("community", Context.MODE_PRIVATE).getBoolean("changedComm", false))
            Singleton.getInstance().getRequestQueue().add(request);
    }
}
