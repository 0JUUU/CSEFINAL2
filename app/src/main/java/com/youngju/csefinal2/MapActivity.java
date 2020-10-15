package com.youngju.csefinal2;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.content.Context.LOCATION_SERVICE;

public class MapActivity extends Fragment implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {
    private  GoogleMap mMap;
    private Marker currentMarker=null;

    Intent intent;
    TextView textView;
    Context mContext;
    SpeechRecognizer mRecognizer;
    final int PERMISSION = 1;
    TextToSpeech tts;
    String text;

    private static final String TAG = "googlemap_example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS =1000;
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500;

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    boolean needRequest = false;

    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};

    Location mCurrentLocation;
    LatLng currentPosition;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;

    private View mLayout;

    public static MapActivity newInstance() {
        MapActivity mfrag = new MapActivity();
        return mfrag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_main_map, container,false);

        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mLayout = v.findViewById(R.id.layout_map);

        // TTS
        textView = (TextView)v.findViewById(R.id.srch);
        tts = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
                {
                    if ( Build.VERSION.SDK_INT >= 23 ){
                        // 퍼미션 체크
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.INTERNET,
                                Manifest.permission.RECORD_AUDIO},PERMISSION);
                    }

                    //intent = getIntent();


                    intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getActivity().getPackageName());
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");




                    text = "목적지를 말씀해주세요.";
                    Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();

                    //http://stackoverflow.com/a/29777304
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ttsGreater21(text);
                    } else {
                        ttsUnder20(text);
                    }

                    mRecognizer = SpeechRecognizer.createSpeechRecognizer(getActivity());
                    mRecognizer.setRecognitionListener(listener);
                    mRecognizer.startListening(intent);
            }
        });


        locationRequest = new LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        SupportMapFragment mapFragment = (SupportMapFragment)getChildFragmentManager()
                .findFragmentById(R.id.Map);
        mapFragment.getMapAsync(this);
        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(tts !=null){
            tts.stop();
            tts.shutdown();
        }
    }


    // TTS 관련
    @SuppressWarnings("deprecation")
    private void ttsUnder20(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsGreater21(String text) {
        String utteranceId=this.hashCode() + "";
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    // STT
    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            Toast.makeText(getContext(),"음성인식을 시작합니다.",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBeginningOfSpeech() {}

        @Override
        public void onRmsChanged(float rmsdB) {}

        @Override
        public void onBufferReceived(byte[] buffer) {}

        @Override
        public void onEndOfSpeech() {}

        @Override
        public void onError(int error) {
            String message;

            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "오디오 에러";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "클라이언트 에러";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "퍼미션 없음";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "네트워크 에러";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "네트웍 타임아웃";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "찾을 수 없음";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RECOGNIZER가 바쁨";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "서버가 이상함";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "말하는 시간초과";
                    break;
                default:
                    message = "알 수 없는 오류임";
                    break;
            }

            Toast.makeText(getContext(), "에러가 발생하였습니다. : " + message,Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResults(Bundle results) {
            // 말을 하면 ArrayList에 단어를 넣고 textView에 단어를 이어줍니다.
            ArrayList<String> matches =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            for(int i = 0; i < matches.size() ; i++){
                textView.setText(matches.get(i));
            }

            text = textView.getText().toString();
            Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();

            //http://stackoverflow.com/a/29777304
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ttsGreater21(text);
            } else {
                ttsUnder20(text);
            }
        }

        @Override
        public void onPartialResults(Bundle partialResults) {}

        @Override
        public void onEvent(int eventType, Bundle params) {}
    };

    // MAP 관련 시작
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        Log.d(TAG,"onMapReady: ");
        mMap = googleMap;

        setDefaultLocation();

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_COARSE_LOCATION);

        if(hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED){
            startLocationUpdates();
        }else{
            if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),REQUIRED_PERMISSIONS[0])) {
                Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권환이 필요합니다.", Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
                    }
                }).show();
            }else {
                ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.d(TAG,"onMapClick: ");
            }
        });
    }

    LocationCallback locationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            List<Location> locationList = locationResult.getLocations();

            if(locationList.size()>0){
                location = locationList.get(locationList.size()-1);

                currentPosition = new LatLng(location.getLatitude(),location.getLongitude());

                String markerTitle = getCurrentAddress(currentPosition);
                String markerSnippet = "위도: "+String.valueOf(location.getLatitude())+"경도: "+String.valueOf(location.getLongitude());

                Log.d(TAG,"onLocationResult: "+markerSnippet);

                setCurrentLocation(location,markerTitle,markerSnippet);

                mCurrentLocation = location;
            }
        }
    };

    private void startLocationUpdates(){
        if(!checkLocationServicesStatus()){
            Log.d(TAG,"startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        }else{
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_COARSE_LOCATION);

            if(hasFineLocationPermission!=PackageManager.PERMISSION_GRANTED || hasCoarseLocationPermission!=PackageManager.PERMISSION_GRANTED){
                Log.d(TAG,"startLocationUpdates : 퍼미션 안갖고 있음");
                return;
            }
            Log.d(TAG,"startLocationUpdates :  call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());

            if(checkPermission())
                mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");

        if(checkPermission()){
            Log.d(TAG,"onStart : call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest,locationCallback,null);

            if(mMap!=null)
                mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if(mFusedLocationClient!=null){
            Log.d(TAG,"onStop : call stopLocationUpdates");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private boolean checkPermission() {
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermssion = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_COARSE_LOCATION);

        if(hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermssion == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        return false;
    }

    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"+"위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent,GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    private boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager)getActivity().getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {
        if(currentMarker!=null)
            currentMarker.remove();

        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);

        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        mMap.moveCamera(cameraUpdate);
    }

    private String getCurrentAddress(LatLng currentPosition) {
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());

        List<Address> addresses;

        try{
            addresses = geocoder.getFromLocation(
                    currentPosition.latitude,
                    currentPosition.longitude,
                    1);
        }catch (IOException e){
            Toast.makeText(getActivity(),"지오코더 서비스 사용불가",Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        }catch (IllegalArgumentException e1){
            Toast.makeText(getActivity(),"잘못된 GPS 좌표",Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        if(addresses==null||addresses.size()==0) {
            Toast.makeText(getActivity(), "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        }else{
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }
    }

    private void setDefaultLocation() {
        LatLng DEFAULT_LOCATION = new LatLng(37.450604, 126.657202);
        String markerTitle = "위치정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 여부 확인하세요";

        if(currentMarker!=null)
            currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION,15);
        mMap.moveCamera(cameraUpdate);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSIONS_REQUEST_CODE && grantResults.length == REQUIRED_PERMISSIONS.length){
            boolean check_result = true;

            for(int result : grantResults){
                if(result!=PackageManager.PERMISSION_GRANTED){
                    check_result = false;
                    break;
                }
            }

            if(check_result){
                startLocationUpdates();
            }
            else{
                if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),REQUIRED_PERMISSIONS[0])||
                ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),REQUIRED_PERMISSIONS[1])){
                    Snackbar.make(mLayout,"퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.",Snackbar.LENGTH_INDEFINITE).setAction("확인",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    getActivity().finish();
                                }
                            }).show();
                }else{
                    Snackbar.make(mLayout,"퍼미션이 거부되었습니다. 설정에서 퍼미션을 허용해야 합니다.",Snackbar.LENGTH_INDEFINITE).setAction("확인",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    getActivity().finish();
                                }
                            }).show();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d(TAG, "onActivityResult : GPS 활성화 되있음");

                        needRequest = true;

                        return;
                    }
                }
                break;
        }
    }
}
