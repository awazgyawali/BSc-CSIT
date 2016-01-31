package com.brainants.bsccsit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.brainants.bsccsit.R;
import com.brainants.bsccsit.advance.Singleton;
import com.brainants.bsccsit.fragments.IntroFragment;
import com.brainants.bsccsit.networking.MyCommunitiesDownloader;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.flaviofaria.kenburnsview.Transition;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import mehdi.sakout.fancybuttons.FancyButton;

public class LoginActivity extends AppCompatActivity {

    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;
    private MaterialDialog dialog;
    ViewPager viewPager;
    KenBurnsView mKenBurns;
    int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editor = getSharedPreferences("loginInfo", MODE_PRIVATE).edit();
        preferences = getSharedPreferences("loginInfo", MODE_PRIVATE);

        final FancyButton button = (FancyButton) findViewById(R.id.loginButton);
        //viewPager = (ViewPager) findViewById(R.id.introViewPager);
        mKenBurns = (KenBurnsView) findViewById(R.id.kenBurns);
        mKenBurns.setTransitionListener(new KenBurnsView.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {

            }

            @Override
            public void onTransitionEnd(Transition transition) {
//                viewPager.setCurrentItem(i % 5, true);
                i++;
            }
        });

        // viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AccessToken.getCurrentAccessToken() == null)
                    loginButton.callOnClick();
                else
                    postFbLoginWork();
            }
        });

        loginButton = new LoginButton(this);

        loginButton.setReadPermissions("public_profile", "email", "user_hometown");

        callbackManager = CallbackManager.Factory.create();

        //login button ko kaam
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                postFbLoginWork();
            }

            @Override
            public void onCancel() {
                Snackbar.make(findViewById(R.id.LoginCore), "Login process aborted.", Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException e) {
                e.printStackTrace();
                Snackbar.make(findViewById(R.id.LoginCore), "Unable to connect.", Snackbar.LENGTH_SHORT).setAction("Retry", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        button.callOnClick();
                    }
                }).show();
            }
        });
        handleViewPager();
    }

    private void handleViewPager() {
        ViewPager pager = new ViewPager(this);
        pager.setCurrentItem(1, true);
    }

    private void postFbLoginWork() {
        if (dialog == null)
            dialog = new MaterialDialog.Builder(this)
                    .content("Logging in...")
                    .progress(true, 0)
                    .cancelable(false)
                    .build();

        dialog.show();
        Bundle bundle = new Bundle();
        bundle.putString("fields", "id,name,email,hometown,gender,first_name,last_name");
        if (preferences.getString("FirstName", "").equals("")) {
            new GraphRequest(AccessToken.getCurrentAccessToken(), "me", bundle, HttpMethod.GET, new GraphRequest.Callback() {
                @Override
                public void onCompleted(GraphResponse response) {
                    if (response.getError() != null) {
                        Snackbar.make(findViewById(R.id.LoginCore), "Unable to connect.", Snackbar.LENGTH_SHORT).setAction("Retry", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                postFbLoginWork();
                            }
                        }).show();
                        dialog.dismiss();
                        return;
                    }
                    try {
                        JSONObject object = response.getJSONObject();
                        //email save garera complete login activity ma name ani email pass gareko
                        editor.putString("email", object.getString("email"));
                        editor.putString("FirstName", object.getString("first_name"));
                        editor.putString("LastName", object.getString("last_name"));
                        editor.putString("FullName", object.getString("name"));
                        editor.putString("Gender", object.getString("gender"));
                        editor.putString("UserID", object.getString("id"));
                        editor.putString("HomeTown", object.getJSONObject("hometown").getString("name"));
                        editor.apply();
                        postFbLoginWork();
                    } catch (JSONException ignored) {
                    }
                }
            }).executeAsync();
            return;
        }
        if (!preferences.getBoolean("checked", false)) {
            StringRequest request = new StringRequest(Request.Method.POST, "http://bsccsit.brainants.com/getuser", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject object = new JSONObject(response);
                        if (object.getBoolean("exists")) {
                            addEveryThingToSp(object.getJSONObject("data"));
                            if (object.getJSONObject("data").getString("communities").equals("")) {
                                editor.putBoolean("loggedFirstIn", true);
                                editor.putBoolean("formFilled", true).apply();
                                dialog.dismiss();
                                finish();
                                startActivity(new Intent(LoginActivity.this, CompleteLogin.class));
                            }
                            editor.putBoolean("checked", true);
                            editor.apply();
                            postFbLoginWork();
                        } else {
                            editor.putBoolean("loggedFirstIn", true);
                            editor.apply();
                            dialog.dismiss();
                            startActivity(new Intent(LoginActivity.this, CompleteLogin.class));
                            finish();
                        }
                    } catch (Exception ignored) {
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Snackbar.make(findViewById(R.id.LoginCore), "Unable to connect.", Snackbar.LENGTH_SHORT).setAction("Retry", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            postFbLoginWork();
                        }
                    }).show();
                    dialog.dismiss();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("user_id", preferences.getString("UserID", ""));
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("Content-Type", "application/x-www-form-urlencoded");
                    return params;
                }
            };
            request.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            Singleton.getInstance().getRequestQueue().add(request);
            return;
        }

        if (Singleton.getFollowingArray().size() - 1 == 0) {
            MyCommunitiesDownloader downloader = new MyCommunitiesDownloader();
            downloader.doInBackground();
            downloader.setTaskCompleteListener(new MyCommunitiesDownloader.OnTaskCompleted() {
                @Override
                public void onTaskCompleted(boolean success) {
                    if (success) {
                        editor.putBoolean("loggedIn", true);
                        editor.apply();
                        Toast.makeText(LoginActivity.this, "Welcome back " + preferences.getString("FirstName", ""), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Snackbar.make(findViewById(R.id.LoginCore), "Unable to connect.", Snackbar.LENGTH_SHORT).setAction("Retry", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                postFbLoginWork();
                            }
                        }).show();
                        dialog.dismiss();
                    }
                }
            });
        }

    }

    private void addEveryThingToSp(JSONObject response) {
        try {

            editor.putInt("semester", Integer.parseInt(response.getString("semester")));
            editor.putString("college", response.getString("college"));
            editor.putString("phone_number", response.getString("phone_number"));
            editor.putBoolean("admin", response.getInt("admin") == 1);
            editor.apply();

        } catch (Exception ignored) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    class PagerAdapter extends FragmentStatePagerAdapter {

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return IntroFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 5;
        }
    }
}