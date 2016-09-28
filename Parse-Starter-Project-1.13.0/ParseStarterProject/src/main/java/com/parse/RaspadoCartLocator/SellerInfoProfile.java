package com.parse.RaspadoCartLocator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.w3c.dom.Text;

import java.util.List;

//import com.parse.starter.R;

public class SellerInfoProfile extends AppCompatActivity {

    private String markerId;
    private ImageView sellerProfileImageView;
    private ListView sellerMenuListView;
    private List<String> sellerMenuDataList;
    private TextView sellerNameTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_info_profile);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        //Inflate the User Interface widgets
        sellerMenuListView = (ListView) findViewById(R.id.sellerMenuListView);
        sellerProfileImageView = (ImageView) findViewById(R.id.sellerProfileImageView);
        sellerNameTextView = (TextView) findViewById(R.id.sellerNameTextView);

        //Get the markerId from the marker selected
        markerId = getIntent().getExtras().getString("markerId");
        //Check if markerId has been set up
        if (markerId==null){
            Log.i("passedMarkerIdCheck", "Marker is null");
        }
        else{
            //Query the current seller from the marker id
            ParseQuery<ParseObject> sellerQuery = ParseQuery.getQuery("sellers");
            sellerQuery.whereEqualTo("markerId",markerId);
            sellerQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e==null){
                        if(objects.size()>0){

                            //Get the menu list
                            ParseQuery<ParseUser> query = ParseUser.getQuery();
                            query.whereEqualTo("objectId", objects.get(0).getString("user" +
                                    "ObjectId"));
                            query.findInBackground(new FindCallback<ParseUser>() {
                                public void done(List<ParseUser> objects, ParseException e) {
                                    if (e == null) {
                                        if (objects.size()>0){
                                            Log.i("sellerProfileUserCheck", "Query was succesful");
                                            //Get the users name
                                            sellerNameTextView.setText(objects.get(0).getUsername());
                                            //Get the users profile image
                                            ParseFile parseFile = objects.get(0).getParseFile("profileThumb");

                                            byte[] data = new byte[0];
                                            try {
                                                data = parseFile.getData();
                                                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                                                sellerProfileImageView.setImageBitmap(bitmap);
                                            } catch (ParseException e1) {
                                                e1.printStackTrace();
                                            }

                                        }
                                        else{
                                            Log.i("sellerProfileUserCheck", "Query was succesfull returned zero results " + objects.size());

                                        }
                                    } else {
                                        // Something went wrong.
                                        Log.i("sellerProfileUserCheck", "Query was not succesfull" );
                                    }
                                }
                            });
                        }
                        else{
                            Log.i("sellerMarkerId","Seller query returned 0 results");
                        }
                    }
                    else {

                    }
                }
            });

        }



    }

}
