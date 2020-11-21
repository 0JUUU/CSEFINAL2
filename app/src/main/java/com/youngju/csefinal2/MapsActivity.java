package com.youngju.csefinal2;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentActivity;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.youngju.csefinal2.Interface.IOnLoadLocationListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GeoQueryEventListener, IOnLoadLocationListener {

    TextToSpeech tts;
    private GoogleMap mMap;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker currentUser;
    private DatabaseReference myLocationRef;
    private GeoFire geoFire;
    private List<LatLng> dangerousArea;
    private IOnLoadLocationListener listener;
    private static final String TAG = "Database: ";

    // realtime change
    private DatabaseReference myCity;
    private Location lastLocation;

    private GeoQuery geoQuery;

    // 위험지역 ID 넣는 곳
    private Integer ID_dangerousArea;

    // 위치 요청하는 퍼미션
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                    tts.setSpeechRate(0.9f);
                }
            }
        });

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                                      @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

                        buildLocationRequest();
                        buildLocationCallback();

                        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);

                        initArea();
                        settingGeoFire();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MapsActivity.this, "You must enable permission", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();

    }

    // 위험지역 넣는 곳
    private void initArea() {

        myCity = FirebaseDatabase.getInstance()
                .getReference("RiskFactor");
        Log.d(TAG, "myCity" + myCity);
        listener = MapsActivity.this;
        // Load from Firebase

        /*
        myCity.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        List<MyLatLng> latLngList = new ArrayList<>();
                        for(DataSnapshot locationSnapShot : dataSnapshot.getChildren())
                        {
                            MyLatLng latLng = locationSnapShot.getValue(MyLatLng.class);
                            latLngList.add(latLng);
                        }
                        listener.onLoadLocationSuccess(latLngList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        listener.onLoadLocationFailed(databaseError.getMessage());
                    }
                });

         */
        myCity.addValueEventListener(new ValueEventListener() {

            // use realtime database --> 실시간으로 움직임임
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Update dangerousArea list
                List<MyLatLng> latLngList = new ArrayList<>();
                for(DataSnapshot locationSnapShot : dataSnapshot.getChildren())
                {
                    Log.d(TAG, "locationSnapShot : " + locationSnapShot);
                    MyLatLng latLng = locationSnapShot.getValue(MyLatLng.class);
                    latLngList.add(latLng);
                }

                listener.onLoadLocationSuccess(latLngList);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // create an array of Location and submit on Firebase
        // After submit this area on Firebase, we will comment it
        /*
        FirebaseDatabase.getInstance()
                .getReference("DangerousArea")
                .child("MyCity")
                .setValue(dangerousArea)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MapsActivity.this, "Updated!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MapsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        */
    }

    private void addUserMarker() {
        Log.d(TAG, "addUserMarker() : ");
        geoFire.setLocation("You", new GeoLocation(lastLocation.getLatitude(),
                lastLocation.getLongitude()), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (currentUser != null) currentUser.remove();
                currentUser = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lastLocation.getLatitude(),
                                lastLocation.getLongitude()))
                        .title("You"));
                //After add marker, move camera
                mMap.animateCamera(CameraUpdateFactory
                        .newLatLngZoom(currentUser.getPosition(), 120.0f));
            }
        });

    }

    // setting geofire on my reference
    private void settingGeoFire() {
        myLocationRef = FirebaseDatabase.getInstance().getReference("MyLocation");
        geoFire = new GeoFire(myLocationRef);
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(final LocationResult locationResult) {
                if (mMap != null) {

                    Log.d(TAG, "buildLocationCallback()");
                    lastLocation = locationResult.getLastLocation();

                    // geofire에 내 위치 저장
                    addUserMarker();
                }
                Log.d(TAG, "Finish buildLocationCallback()");
            }
        };
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (fusedLocationProviderClient != null)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

        // Add Circle for dangerous
        addCircleArea();
    }

    private void addCircleArea() {
        Log.d(TAG, "addCircleArea() : ");
        if(geoQuery != null)
        {
            geoQuery.removeGeoQueryEventListener(this);
            geoQuery.removeAllListeners();
        }

        for(LatLng latLng : dangerousArea)
        {
            mMap.addCircle(new CircleOptions().center(latLng)
                    .radius(3) // 3m
                    .strokeColor(Color.BLUE)
                    .fillColor(0x220000FF)     // 22 is transparent code
                    .strokeWidth(5.0f)
            );


            // Create GeoQuery when user in dangerous location

            geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude), 0.003f); // 500m
            // onkeyentered 이런거 부르는 애
            geoQuery.addGeoQueryEventListener(MapsActivity.this);
        }
    }

    @Override
    protected void onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onStop();
    }

    // 위험 요소 접근시 말함
    @Override
    public void onKeyEntered(String key, GeoLocation location) {

        double lat = location.latitude;
        double lon = location.longitude;
        Location now_location = new Location("");
        now_location.setLatitude(lat);
        now_location.setLongitude(lon);


        Integer count = 0;
        for(LatLng dan : dangerousArea)
        {
            count++;
            double d_lat = dan.latitude;
            double d_lon = dan.longitude;
            Location d_location = new Location("");
            d_location.setLatitude(d_lat);
            d_location.setLongitude(d_lon);
            if(d_location.distanceTo(now_location) <= 5)
            {
                break;
            }

       }
        Log.d(TAG, "onKeyEntered : " + count);
        if(count == 1 || count == 2 || count == 6)
            tts.speak("근처에 계단이 있습니다. 주의해주세요.", TextToSpeech.QUEUE_FLUSH, null, null);
        else if(count == 3 || count == 4)
            tts.speak("근처에 경사진 길이 있습니다. 주의해주세요.", TextToSpeech.QUEUE_FLUSH, null, null);
        else if(count == 5)
            tts.speak("차도와 도보의 구분이 모호합니다. 주의해주세요.", TextToSpeech.QUEUE_FLUSH, null, null);
       // tts.speak("근처에 위험요소가 있습니다. 주의해주세요.", TextToSpeech.QUEUE_FLUSH, null, null);
        //sendNotification("EDMTDev", String.format("%s entered the dangerous area", key));
    }

    @Override
    public void onKeyExited(String key) {
        //sendNotification("EDMTDev", String.format("%s leave the dangerous area", key));
    }


    @Override
    public void onKeyMoved(String key, GeoLocation location) {
        //sendNotification("EDMTDev", String.format("%s move within the dangerous area", key));
    }

    @Override
    public void onGeoQueryReady() {

    }

    @Override
    public void onGeoQueryError(DatabaseError error) {
        Toast.makeText(this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String title, String content) {

        Toast.makeText(this, "" + content, Toast.LENGTH_SHORT).show();
        String NOTIFICATION_CHANNEL_ID = "emdt_multiple_location";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notification",
                    NotificationManager.IMPORTANCE_DEFAULT);

            // Config
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(false)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

        Notification notification = builder.build();
        notificationManager.notify(new Random().nextInt(), notification);

    }

    @Override
    public void onLoadLocationSuccess(List<MyLatLng> latLngs) {
        dangerousArea = new ArrayList<>();

        for(MyLatLng myLatLng : latLngs)
        {
            LatLng convert = new LatLng(myLatLng.getLatitude(), myLatLng.getLongitude());
            dangerousArea.add(convert);

        }
        Log.d("dangerous : ", dangerousArea.toString());
        // New, after dangerous Area is have data, we will call MAp display
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);

        // clear map and add again
        if(mMap != null)
        {

            mMap.clear();;
            // Add user Marker
            Log.d(TAG, "onLoadLocationSuccess()");
            addUserMarker();

            // Add Circle of dangerous area
            addCircleArea();
        }
    }

    @Override
    public void onLoadLocationFailed(String message) {
        Toast.makeText(this, ""+message,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            if(tts.isSpeaking()) {
                tts.stop();
            }
            tts.shutdown();
        }
        super.onDestroy();
    }

}
