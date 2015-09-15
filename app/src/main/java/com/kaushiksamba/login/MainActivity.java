package com.kaushiksamba.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.Sharer;
import com.facebook.share.internal.ShareContentValidation;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


public class MainActivity extends AppCompatActivity {

    private CallbackManager callbackManager;
    private ShareButton shareButton;
    private SharedPreferences prefs;
    private SharePhotoContent sharePhotoContent;
    private ProfileTracker tracker;
    private ArrayList<String> couponList;
    private ArrayAdapter<String> adapter;

    private void updateUI() {
        boolean enableTheWorld = AccessToken.getCurrentAccessToken() != null;
        Profile profile = Profile.getCurrentProfile();
        if (profile == null)    //Detects log out.
        {
            Log.e("Profile", "null");
            Toast.makeText(getApplicationContext(), "You've just logged out, m8", Toast.LENGTH_SHORT).show();
            shareButton.setVisibility(View.INVISIBLE);
            shareButton.setClickable(false);
            if(Utilities.status==2) Utilities.status = 4;
                else if(Utilities.status==3) Utilities.status = 5;
            prefs.edit().putInt("Logged_in", Utilities.status).apply();
        }
        if (enableTheWorld && profile != null)  //Detects log in.
        {
            Utilities.fb_name = profile.getName();
            Utilities.fb_id=profile.getId();
            if(Utilities.status==1) Utilities.status = 2;
            Toast.makeText(getApplicationContext(), "Welcome, " + Utilities.fb_name, Toast.LENGTH_LONG).show();
            prefs.edit().putInt("Logged_in", Utilities.status).putString("Name", Utilities.fb_name).putString("FBID", Utilities.fb_id).apply();
            if(Utilities.status==2 && Utilities.coupons[0].equals("No coupon yet")) new myApiCaller().execute(1);
        }
        System.out.println("UpdateUI called");
        System.out.println("Utilities.status = " + Utilities.status);
    }

    private SharePhotoContent getMyShareContent() {
        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.festember_kaushik_edited_two);
        SharePhoto photo = new SharePhoto.Builder().setBitmap(image).build();
        sharePhotoContent = new SharePhotoContent.Builder().addPhoto(photo).build();
        return sharePhotoContent;
    }

    private void updateUtils() {
        couponList = new ArrayList<>();
        prefs = getSharedPreferences("LogInPrefs", Context.MODE_PRIVATE);
        Utilities.status = prefs.getInt("Logged_in", 1);
        Utilities.f_id = prefs.getString("f_id", "");
        Utilities.fb_name = prefs.getString("Name", "");
        Utilities.fb_id = prefs.getString("FBID", "");
        for(int i=0;i<11;i++)
        {
            Utilities.coupons[i] = prefs.getString("Coupon"+i,"No coupon yet");
        }
    }

    private void startShareButton() {
        shareButton.setVisibility(View.VISIBLE);
        shareButton.setClickable(true);
        shareButton.setShareContent(getMyShareContent());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button button = (Button) findViewById(R.id.profilePageButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),ProfilePage.class);
                startActivity(intent);
            }
        });
//Facebook initializations
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        tracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile profile, Profile profile1) {
                updateUI();
            }
        };
        tracker.startTracking();
        final ShareDialog shareDialog = new ShareDialog(this);
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {

                try {
                    ShareContentValidation.validateForApiShare(sharePhotoContent);
                    System.out.println("Data shared :)");
                } catch (FacebookException e) {
                    e.printStackTrace();
                }
                if (Utilities.status == 2 || Utilities.status == 4) {
                    Utilities.status = 3;
                    prefs.edit().putInt("Logged_in", Utilities.status).apply();
                    //API CALL TO REQUEST FIVE EXTRA COUPONS IF NOT ALREADY OBTAINED
                    new myApiCaller().execute(2);
                }
            }

            @Override
            public void onCancel() {
                System.out.println("Canceled. :(");
            }

            @Override
            public void onError(FacebookException e) {
                Toast.makeText(getApplicationContext(), "There was a problem with sharing, please try again.", Toast.LENGTH_SHORT).show();
            }
        });

//Calling Shared Preferences and updating utilities
        updateUtils();
        adapter = new ArrayAdapter<>(getApplicationContext(),R.layout.single_list_item);
        setArrayAdapter();

//Layout initialisations
        setContentView(R.layout.activity_main);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

//Setting up FB buttons
        LoginButton loginButton = (LoginButton) findViewById(R.id.loginButton);
        shareButton = (ShareButton) findViewById(R.id.shareButton);
        if (Utilities.status == 2 || Utilities.status==4 || Utilities.status == 5 || Utilities.status==3) {
            startShareButton();
        }

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                Set<String> set = accessToken.getDeclinedPermissions();
                if (set.contains("publish_actions")) {
                    LoginManager.getInstance().logInWithPublishPermissions(MainActivity.this, Collections.singletonList("publish_actions"));
                }

                startShareButton();
            }

            @Override
            public void onCancel() {
                System.out.println("Login attempt canceled. :(");
            }

            @Override
            public void onError(FacebookException e) {
                System.out.println(e.toString());
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    class myApiCaller extends AsyncTask<Integer, Void, Integer> {
        ProgressDialog myPd_ring = null;

        @Override
        protected void onPreExecute() {
            myPd_ring = new ProgressDialog(MainActivity.this);
            myPd_ring.setMessage("Increasing your chances of winning the raffle...");
            myPd_ring.setCancelable(false);
            myPd_ring.setCanceledOnTouchOutside(false);
            myPd_ring.show();
            System.out.println("Api called");
        }

        @Override
        protected Integer doInBackground(Integer... type) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpEntity httpEntity;
            HttpPost httppost = new HttpPost(Utilities.url_raffle);
            JSONObject jsonObject;
            int status = 0;

            try {
                List<BasicNameValuePair> nameValuePairs = new ArrayList<>();
                nameValuePairs.add(new BasicNameValuePair("fbid", Utilities.fb_name));
                nameValuePairs.add(new BasicNameValuePair("fname", Utilities.f_id));
                nameValuePairs.add(new BasicNameValuePair("fpsw", Utilities.f_pass));
                nameValuePairs.add(new BasicNameValuePair("type", type[0].toString()));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
                HttpResponse response = httpclient.execute(httppost);
                httpEntity = response.getEntity();
                String s = EntityUtils.toString(httpEntity);
                System.out.println(s);
                try {
                    jsonObject = new JSONObject(s);
                    status = jsonObject.getInt("status");
                    System.out.println("Status of API call: " + status);
                    switch(status)
                    {
                        case 0: //Festember name and Festember password don't match
                        case 4: //Unexpected failure
                            if(type[0]==1) {
                                Utilities.status=1;
                                prefs.edit().putInt("Logged_in", Utilities.status).apply();
                            }
                            else if (type[0]==2)
                            {
                                Utilities.status=2;
                                prefs.edit().putInt("Logged_in", Utilities.status).apply();
                            }
                            break;
                        case 1:
                            break;
                        case 2:
                            if (type[0] == 1) {
                                Utilities.coupons[0] = jsonObject.getString("data");
                            } else if (type[0] == 2) {
                                JSONArray array = jsonObject.getJSONArray("data");
                                for (int i = 1; i < 6; i++) {
                                    Utilities.coupons[i] = array.getJSONObject(i - 1).getString("coupon_code");
                                }
                            }
                            break;
                        case 3:
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return status;
        }

        @Override
        protected void onPostExecute(Integer status) {
            super.onPostExecute(status);
            System.out.println("Utilities.status onPostExecute()" + Utilities.status);
            myPd_ring.dismiss();
            for(int i=0;i<11;i++)
                prefs.edit().putString("Coupon" + i, Utilities.coupons[i]).apply();
            setArrayAdapter();
            switch (status) {
                case 0: //Authorization error
                case 4: //Failure
                    Toast.makeText(getApplicationContext(), "There was an error logging in. Please check your credentials.", Toast.LENGTH_LONG).show();
                    break;
                case 1:
                    //F_id authorized
                    //Nothing to do as of yet
                    break;
                case 2:
                    //F_id authorized and raffle ticket(s) generated
                    break;
                case 3:
                    //F_id authorized but raffle ticket(s) already allotted
                    break;
            }
        }
    }

    private void setArrayAdapter()
    {
        couponList.clear();
        for(int i=0;i<11;i++)
        {
            if(!Utilities.coupons[i].equals("No coupon yet")) couponList.add(i,Utilities.coupons[i]);
        }
        if(couponList.isEmpty()) couponList.add(0,"Log in through Facebook to win a place in the raffle draw!");
        else if(couponList.size()==1) couponList.add(1,"Share #festember to win 5 extra tickets!");
        adapter.clear();
        adapter.addAll(couponList);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        tracker.stopTracking();
        super.onDestroy();
    }
}
