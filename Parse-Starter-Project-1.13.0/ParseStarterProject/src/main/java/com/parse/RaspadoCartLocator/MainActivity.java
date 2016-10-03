/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.RaspadoCartLocator;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
//
//import com.facebook.AccessToken;
//import com.facebook.GraphRequest;
//import com.facebook.GraphResponse;
//import com.facebook.HttpMethod;
//import com.facebook.appevents.AppEventsLogger;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseAnalytics;

//You need to initialize Facebook SDK before you can use it. Add a call to FacebookSdk.sdkInitialize from onCreate in your Application class:
// Add this to the header of your file:
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
//import com.facebook.FacebookSdk;

public class MainActivity extends AppCompatActivity {
  Button facebookLoginButton;
  Switch sellerOrUserSwitch;
  public static final List<String> mPermissions = new ArrayList<String>() {{
    add("public_profile");
    add("email");
  }};
  private ParseUser parseUser;
  private String email;
  private String name;
  private Bitmap bitmap;
  private ParseFile parseFile;
  private int MAPACTIVITYCODE = 32;
  private String userOrSellerString = " " ;
  ParseACL defaultAcl;
  private Intent mapActivityIntent;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    defaultAcl = new ParseACL();
    defaultAcl.setPublicReadAccess(true);
    defaultAcl.setPublicWriteAccess(true);
    // Initialize the SDK before executing any other operations,
    ParseAnalytics.trackAppOpenedInBackground(getIntent());

    if(ParseUser.getCurrentUser()!= null){
      Log.i("UserTest",ParseUser.getCurrentUser().getEmail()+ " is currently logged in");
      // getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MainPageFragment()).commit();
      ParseUser.logOut();
    }
    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
    // Add the buttons
    builder.setPositiveButton("User", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        // User clicked OK button
        userOrSellerString = "user";
      }
    });
    builder.setNegativeButton("Seller", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        // User cancelled the dialog
        userOrSellerString = "seller";

      }
    });
    // Set other dialog properties
    // Create the AlertDialog
    AlertDialog dialog = builder.create();
    dialog.show();
    facebookLoginButton =(Button) findViewById(R.id.facebookLoginButton);
    facebookLoginButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        ParseFacebookUtils.logInWithReadPermissionsInBackground(MainActivity.this, mPermissions, new LogInCallback() {
          @Override
          public void done(ParseUser user, ParseException err) {
            mapActivityIntent = new Intent(getApplicationContext(),RaspadoMapsActivity.class);
           if (user == null) {
              Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
            } else if (user.isNew()) {
              Log.d("MyApp", "User signed up and logged in through Facebook!");
             Log.d("MyApp", "Logged in user info " + user.getUsername());
             //Get info from facebook and create new parse user
              getUserDetailsFromFB();

            } else {
              Log.d("MyApp", "User logged in through Facebook!");
              //User already registered, getting info from parse
              getUserDetailsFromParse();
             startActivity(mapActivityIntent);

           }
          }
        });

      }
    });

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    //Check if map intent was successfully launched
    if (requestCode == MAPACTIVITYCODE){
      Log.i("mapActivity", "Map Acqtivity has started");
    }
  }

  private void getUserDetailsFromFB() {
    // Suggested by https://disqus.com/by/dominiquecanlas/
    Log.i("isFacebookMethod", "Begginging of getUserDetails from FB method");
    Bundle parameters = new Bundle();
    parameters.putString("fields", "email,name,picture");
    //Method that grabs data from facebook
    new GraphRequest(
            AccessToken.getCurrentAccessToken(), //Current Token parameter
            "/me",                               //?
            parameters,                          //data requested parameters
            HttpMethod.GET,                     //?
            new GraphRequest.Callback() {       //?
              public void onCompleted(GraphResponse response) {
         /* handle the result */
                try {
                  Log.i("facebookTest", response.toString());
                  //Get users facebook email
                  email = response.getJSONObject().getString("email");
                  //Get users facebook name
                  name = response.getJSONObject().getString("name");
                  Log.i("nameTest","Check the value of name " + name);
                  JSONObject picture = response.getJSONObject().getJSONObject("picture");
                  JSONObject data = picture.getJSONObject("data");
                  //  Returns a 50x50 profile picture
                  String pictureUrl = data.getString("url");
                  new ProfilePhotoAsync(pictureUrl).execute();
                } catch (JSONException e) {
                  e.printStackTrace();
                  Log.e("facebookGetDataErr", e.getMessage());
                }
              }
            }
    ).executeAsync();
  }
  class ProfilePhotoAsync extends AsyncTask<String, String, String> {
    String url;
    public ProfilePhotoAsync(String url) {
      this.url = url;
    }
    @Override
    protected String doInBackground(String... params) {
      // Fetching data from URI and storing in bitmap
      bitmap = DownloadImageBitmap(url);
      return null;
    }
    @Override
    protected void onPostExecute(String s) {
      super.onPostExecute(s);
      //Saves the new User after getting details from FB
      saveNewUser();
    }
  }

  //Method to save a new user
  private void saveNewUser() {
    parseUser = ParseUser.getCurrentUser();
    //Set the username and email for current user
    parseUser.setUsername(name);
    parseUser.setEmail(email);
    // Saving profile photo as a ParseFile
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
    byte[] data = stream.toByteArray();
    String thumbName = parseUser.getUsername().replaceAll("\\s+", "");
    parseFile = new ParseFile(thumbName + "_thumb.jpg", data);

    if(userOrSellerString.equals("seller")){
      //Create the seller object
      ParseObject sellerObject = new ParseObject("sellers");
      sellerObject.put("userObjectId", parseUser.getObjectId() );   //Id to keep track of the parseuser
      sellerObject.put("sellerLocation", new ParseGeoPoint());        //Keep track of where seller is
      // Menu List?
      sellerObject.put("sellerMenuList", new ArrayList<String>() );
      //Save the seller object
      try {
        sellerObject.save();
      } catch (ParseException e) {
        e.printStackTrace();
      }
    }

    //Add the data into the parse server
    parseUser.put("sellerOrUser", userOrSellerString);
    parseUser.put("profileThumb", parseFile);
    parseUser.put("userLocation", new ParseGeoPoint());
    //Save the Data
    parseUser.saveInBackground(new SaveCallback() {
      @Override
      public void done(ParseException e) {

        if (e == null) {
          Toast.makeText(MainActivity.this, "New user:" + name + " Signed up", Toast.LENGTH_LONG).show();

          startActivity(mapActivityIntent);

        } else {
          Log.i("parseUserSave", e.toString());
        }
      }
    });



  }


  //Method to download image from web
  public static Bitmap DownloadImageBitmap(String url) {
    Bitmap bm = null;
    try {
      //Create Url
      URL aURL = new URL(url);
      //Create connectionf or the URl
      URLConnection conn = aURL.openConnection();
      conn.connect();
      //Get the data stream from connection
      InputStream is = conn.getInputStream();
      //Buffer the inputstream
      BufferedInputStream bis = new BufferedInputStream(is);
      //Decode the stream and store in bitmap
      bm = BitmapFactory.decodeStream(bis);
      bis.close();
      is.close();
    } catch (IOException e) {
      Log.e("IMAGE", "Error getting bitmap", e);
    }
    return bm;
  }

  //Gets the info from users or sellers already registered
  private void getUserDetailsFromParse() {
    parseUser = ParseUser.getCurrentUser();
    //Fetch profile photo
        try {
            ParseFile parseFile = parseUser.getParseFile("profileThumb");
            byte[] data = parseFile.getData();
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
          //Update whether user is in user or seller mode
          parseUser.put("sellerOrUser", userOrSellerString);
          parseUser.save();
//            mProfileImage.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
//    mEmailID.setText(parseUser.getEmail());
//    mUsername.setText(parseUser.getUsername());
    Toast.makeText(MainActivity.this, "Welcome back " + parseUser.getUsername(), Toast.LENGTH_SHORT).show();
  }
}
