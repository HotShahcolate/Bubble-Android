package com.bubbleinc.bubble;

import android.Manifest;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.PendingIntent;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bubbleinc.bubble.Adapters.RecyclerViewAdapter;
import com.bubbleinc.bubble.Models.bubbleMessage;
import com.bubbleinc.bubble.Models.bubbleMessageNotification;
import com.bubbleinc.bubble.Models.bubbleMessageToDatabase;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;

import static com.bubbleinc.bubble.App.CHANNEL_1_ID;
import static com.bubbleinc.bubble.App.CHANNEL_2_ID;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, NotificationsFragment.NotificationsListener {


    private static final String TAG = "MainActivity";

    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final float DEFAULT_ZOOM = 19f;
    private static final long UPDATE_INTERVAL = 30 * 60 * 1000;  // 30 mins
    private static final long FASTEST_INTERVAL = 20 * 60 * 1000; // 20 mins

    private boolean mLocationsPermissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Circle bubble;
    private bubbleMessageToDatabase bubbleMessageToDatabase;
    private FirebaseAuth auth;
    private double bubbleNotificationLat;
    private double bubbleNotificationLong;
    private String bubbleNotificationKey;
    private String bubbleMessageKeyToDatabase;
    private String bubbleMessageKeyFromDatabase;
    private String bubbleMessageName;
    private String bubbleMessageMessage;
    private String bubbleMessageNotificationName;
    private String bubbleMessageNotificationMessage;
    private String username;
    private Location currentLocation;
    private LatLng bubbleCenter;
    private ArrayList<bubbleMessage> mBubbleMessages;
    private NotificationsFragment notificationsFragment;
    private long bubbleMessageTimestampLong;
    private boolean paused;
    private NotificationManagerCompat notificationManager;

    private ImageButton sendMessageButton;
    private EditText userBubbleMessage;
    private TextView privacyPolicy;
    private RecyclerView bubbleMessagesList;
    private RecyclerView.Adapter bubbleMessageListAdapter;
    private RecyclerView.LayoutManager bubbleMessageListLayoutManager;
    private Bundle bubbleMessageNotificationBundle;

    private DatabaseReference ref;
    //private DatabaseReference refToBubbleMessages;
    //private DatabaseReference refToGeoFire;
    private GeoFire geoFire;
    private GeoQuery geoQuery;
    private GeoQuery vicinity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        notificationsFragment = new NotificationsFragment();
        bubbleMessageNotificationBundle = new Bundle();
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            auth.signOut();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (isServicesOK()) {
            getLocationPermission();
        }
        //assign username
        username = "Anon-";
        Random rnd = new Random();
        int n = 10000 + rnd.nextInt(90000);
        username += n;

        ref = FirebaseDatabase.getInstance().getReference();
        geoFire = new GeoFire(ref.child("GeoFire"));
        sendMessageButton = (ImageButton) findViewById(R.id.send_message_to_bubble);
        userBubbleMessage = (EditText) findViewById(R.id.bubble_message);
        bubbleMessagesList = (RecyclerView) findViewById(R.id.list_of_bubble_messages);
        bubbleMessagesList.setHasFixedSize(true);
        //bubbleMessageNotificationsListLayoutManager.setReverseLayout(true);
        bubbleMessageListLayoutManager = new LinearLayoutManager(this);
        mBubbleMessages = new ArrayList<>();
        //mBubbleMessages.add(new bubbleMessage("name", "message"));
        privacyPolicy = (TextView) findViewById(R.id.privacy_policy);
        privacyPolicy.setMovementMethod(LinkMovementMethod.getInstance());

        bubbleMessageListAdapter = new RecyclerViewAdapter(mBubbleMessages);

        bubbleMessagesList.setLayoutManager(bubbleMessageListLayoutManager);
        bubbleMessagesList.setAdapter(bubbleMessageListAdapter);

        notificationManager = NotificationManagerCompat.from(this);
        bubbleMessageToDatabase = new bubbleMessageToDatabase();
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        userBubbleMessage.setOnEditorActionListener(editorListener);

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.ic_home:
                        getSupportFragmentManager().beginTransaction().hide(notificationsFragment).commit();
                        break;

                    case R.id.ic_notifications:
                        getSupportFragmentManager().beginTransaction().show(notificationsFragment).commit();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, notificationsFragment).commit();
                        break;

                }
                return true;
            }
        });
    }

    //hide fragment, move map, make bubble, delete notification
    @Override
    public void onDataPass(Double lat, Double lng) {
        getSupportFragmentManager().beginTransaction().hide(notificationsFragment).commit();
        moveCamera(new LatLng(lat, lng), DEFAULT_ZOOM);
        drawBubble(new LatLng(lat, lng));
    }

    private TextView.OnEditorActionListener editorListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
            }
            return true;
        }
    };

    private void sendMessage() {
        if (TextUtils.isEmpty(userBubbleMessage.getText().toString())) {
            Toast.makeText(MainActivity.this, "Please enter a message.", Toast.LENGTH_SHORT).show();
        } else if (bubble == null) {
            Toast.makeText(MainActivity.this, "Please create a Bubble by tapping on the map.", Toast.LENGTH_SHORT).show();
        } else {
            bubbleMessageToDatabase.setMessage(userBubbleMessage.getText().toString());
            userBubbleMessage.getText().clear();
            bubbleMessageToDatabase.setName(username);
            bubbleMessageToDatabase.setTimestamp(ServerValue.TIMESTAMP);
            Log.d(TAG, "sendMessage: ");
            ref.child("Bubble_Messages").push().setValue(bubbleMessageToDatabase, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError,
                                       DatabaseReference databaseReference) {
                    bubbleMessageKeyToDatabase = databaseReference.getKey();
                    geoFire.setLocation(bubbleMessageKeyToDatabase, new GeoLocation(bubbleCenter.latitude, bubbleCenter.longitude), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                        }
                    });
                }
            });

        }
    }
    //you send message to database, bubble triggers,
    //you send message to database, vicinity triggers,
    //someone else sends message to database, bubble triggers, vicinity triggers, -- handled
    //someone else sends message to database, vicinity triggers, bubble triggers, (delete notification after detected in bubble)
    private void queryVicinity() {
        vicinity = geoFire.queryAtLocation(new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()), 8.04672);
        vicinity.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.d(TAG, "onKeyEntered: key detected");
                if(key.equals(bubbleMessageKeyToDatabase))
                {
                    return;
                }
                bubbleNotificationKey = key;
                bubbleNotificationLat = location.latitude;
                bubbleNotificationLong = location.longitude;
                ref.child("Bubble_Messages").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        bubbleMessageNotificationName = dataSnapshot.getValue(bubbleMessageNotification.class).getName();
                        bubbleMessageNotificationMessage = dataSnapshot.getValue(bubbleMessageNotification.class).getMessage();
                        bundleBubbleMessageNotification();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onKeyExited(String key) {
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
            }

            @Override
            public void onGeoQueryReady() {
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
            }
        });
    }

    /*
    private boolean messageIsInBubble(String key)
    {
        if(bubble != null)
        {
            for(bubbleMessage i : mBubbleMessages)
            {
                if(i.getKey().equals(key))
                    return true;
            }
            return false;
        }
        return false;
    }*/

    private void bundleBubbleMessageNotification()
    {
        bubbleMessageNotificationBundle.putString("name", bubbleMessageNotificationName);
        bubbleMessageNotificationBundle.putString("key", bubbleNotificationKey);
        bubbleMessageNotificationBundle.putString("message", bubbleMessageNotificationMessage);
        bubbleMessageNotificationBundle.putDouble("lat", bubbleNotificationLat);
        bubbleMessageNotificationBundle.putDouble("long", bubbleNotificationLong);
        if(notificationsFragment.getArguments() == null)
            notificationsFragment.setArguments(bubbleMessageNotificationBundle);
        else
            notificationsFragment.getArguments().putAll(bubbleMessageNotificationBundle);
        notificationsFragment.updateBubbleMessageNotificationsList();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, notificationsFragment).commitAllowingStateLoss();
    }



    @Override
    protected void onPause() {
        paused = true;
        Log.d(TAG, "onPause: activity paused");
        super.onPause();
    }

    @Override
    protected void onResume() {
        paused = false;
        Log.d(TAG, "onResume: activity resumed");
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        geoQuery.removeAllListeners();
        vicinity.removeAllListeners();
        super.onDestroy();
        Log.d(TAG, "onDestroy: activity destroyed");
    }

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    private void sendToChannel1() {
        Intent openApp = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, openApp, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setContentTitle(bubbleMessageName)
                .setContentText(bubbleMessageMessage)
                .setSmallIcon(R.drawable.ic_bubble_chart_black_24dp)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(1, notification);
    }

    private void drawBubble(LatLng latLng) {
        if (bubble != null) {
            bubble.remove();
            bubble = null;
            geoQuery.removeAllListeners();
            bubbleMessageListAdapter.notifyItemRangeRemoved(0, mBubbleMessages.size());
            mBubbleMessages.clear();
        }
        bubble = mMap.addCircle(new CircleOptions()
                .strokeWidth(10)
                .strokeColor(Color.argb(200, 0, 255, 0))
                .fillColor(Color.argb(75, 0, 255, 0))
                .radius(25)
                .center(latLng));
        bubbleCenter = new LatLng(latLng.latitude, latLng.longitude);
        queryBubble();
    }

    //Create the query for the bubble that is made. then load all of the key's associated data into a recyclerview and display the results on the screen.
    private void queryBubble() {
        geoQuery = geoFire.queryAtLocation(new GeoLocation(bubbleCenter.latitude, bubbleCenter.longitude), 0.025);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                bubbleMessageKeyFromDatabase = key;
                ref.child("Bubble_Messages").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        addBubbleMessageToList(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onKeyExited(String key) {
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
            }

            @Override
            public void onGeoQueryReady() {
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
            }
        });
    }


    private void addBubbleMessageToList(DataSnapshot dataSnapshot) {
        bubbleMessageName = dataSnapshot.getValue(bubbleMessage.class).getName() + " ";
        bubbleMessageMessage = dataSnapshot.getValue(bubbleMessage.class).getMessage();
        bubbleMessageTimestampLong = dataSnapshot.getValue(bubbleMessage.class).getTimestamp();
        mBubbleMessages.add(new bubbleMessage(bubbleMessageKeyFromDatabase, bubbleMessageName, bubbleMessageMessage, bubbleMessageTimestampLong));
        bubbleMessageListAdapter.notifyItemInserted(mBubbleMessages.size());
        bubbleMessagesList.scrollToPosition(mBubbleMessages.size() - 1);
        Log.d(TAG, "addBubbleMessageToList: bubble added to list ");
        if(paused) {
            Log.d(TAG, "addBubbleMessageToList: bubble sent to notifications");
            sendToChannel1();
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: Map is Ready");
        mMap = googleMap;

        MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle);
        mMap.setMapStyle(style);

        if (mLocationsPermissionGranted) {
            getLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                drawBubble(latLng);
            }
        });
    }

    private void getLocation() {
        Log.d(TAG, "getLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationsPermissionGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: location found");
                            currentLocation = (Location) task.getResult();
                            if (currentLocation == null) {
                                AlertDialog.Builder locationDisabled = new AlertDialog.Builder(MainActivity.this);
                                locationDisabled.setMessage("Your location is disabled. Location is required to use this app.")
                                        .setCancelable(false)
                                        .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                finish();
                                            }
                                        });
                                AlertDialog alert = locationDisabled.create();
                                alert.setTitle("Error");
                                alert.show();
                            } else {
                                moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                                queryVicinity();
                                startLocationUpdates();
                            }
                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MainActivity.this, "Unable to get Current Location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getLocation: Security Exception:" + e.getMessage());
        }
    }

    private void startLocationUpdates() {
        LocationRequest mLocationRequestHighAccuracy = new LocationRequest();
        mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequestHighAccuracy.setInterval(UPDATE_INTERVAL);
        mLocationRequestHighAccuracy.setFastestInterval(FASTEST_INTERVAL);

        Log.d(TAG, "startLocationUpdates: getting location information.");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequestHighAccuracy, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {

                        Log.d(TAG, "onLocationResult: got location result.");

                        Location location = locationResult.getLastLocation();

                        if (location != null) {
                            GeoLocation center = new GeoLocation(location.getLatitude(), location.getLongitude());
                            vicinity.setCenter(center);
                            //currentLocation = location;
                            Log.d(TAG, "onLocationResult: updated vicinity center. ");
                        }
                    }
                },
                Looper.myLooper()); // Looper.myLooper tells this to repeat forever until thread is destroyed
    }

    private void moveCamera(LatLng latLng, float zoom)
    {
        Log.d(TAG, "moveCamera: moving the camera to: lat:" + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void initMap()
    {
        Log.d(TAG, "initMap: Initializing Map...");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MainActivity.this);
    }

    private void getLocationPermission()
    {
        Log.d(TAG, "getLocationPermission: Getting Location Permissions...");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            mLocationsPermissionGranted = true;
            initMap();
        }
        else
        {
            ActivityCompat.requestPermissions(this, permissions,LOCATION_PERMISSION_REQUEST_CODE );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        Log.d(TAG, "onRequestPermissionsResult: Called");
        mLocationsPermissionGranted = false;

        switch(requestCode)
        {
            case LOCATION_PERMISSION_REQUEST_CODE:
            {
                if(grantResults.length > 0)
                {
                    for(int i = 0; i < grantResults.length; i++)
                    {
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED)
                        {
                            mLocationsPermissionGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: Permission Denied");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: Permission Granted");
                    mLocationsPermissionGranted = true;
                    //initialize map
                    initMap();
                }
            }
        }
    }

    public boolean isServicesOK()
    {
        Log.d(TAG, "isServicesOK: Checking Google Services Version...");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS)
        {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is Working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available))
        {
            //a resolvable error occurred
            Log.d(TAG, "isServicesOK: a resolvable error occurred");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else
        {
            Toast.makeText(this, "Cannot make map requests.",Toast.LENGTH_SHORT).show();
        }
        return false;
    }


}
