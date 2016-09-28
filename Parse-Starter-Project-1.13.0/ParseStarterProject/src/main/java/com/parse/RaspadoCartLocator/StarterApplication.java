/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.RaspadoCartLocator;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;


public class StarterApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    // Enable Local Datastore.
    Parse.enableLocalDatastore(this);

    // Add your initialization code here
    /*
    *
up vote
10
down vote
accepted
You have to add an "/" to the end of your server-url. After /parse*/
    Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
            .applicationId("jfkdjfkdiureirmclslseoioef")
            .clientKey("")
            .server("http://raspadocartlocator.herokuapp.com/parse/")
            .build()
    );
   ParseUser.enableAutomaticUser();
   ParseACL defaultACL = new ParseACL();
    // Optionally enable public read access.
    defaultACL.setPublicReadAccess(true);
    ParseACL.setDefaultACL(defaultACL, true);



//      ParseObject testObject = new ParseObject("sellers");
//    testObject.put("userObjectId", "fdailerueiojf");
//      testObject.put("sellerLocation", new ParseGeoPoint(30, -108.54));
//    try {
//
//      testObject.save();
//    } catch (ParseException e) {
//      e.printStackTrace();
//    }
    //Need these for faceboook login,
    //crashes on log in button press
    FacebookSdk.sdkInitialize(getApplicationContext());
    AppEventsLogger.activateApp(this);
    ParseFacebookUtils.initialize(getApplicationContext());
  }

}
