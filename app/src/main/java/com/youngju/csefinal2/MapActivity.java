package com.youngju.csefinal2;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapTapi;
import com.skt.Tmap.util.HttpConnect;
import com.youngju.csefinal2.Interface.IOnLoadLocationListener;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import static android.content.Context.LOCATION_SERVICE;


public class MapActivity extends Fragment implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback, GoogleMap.OnInfoWindowClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GeoQueryEventListener, IOnLoadLocationListener {

    private static int RESULT_OK;
    private static int RESULT_CANCELED;

    Intent intent;
    TextView textView;
    TextView txtResult;
    SpeechRecognizer mRecognizer;
    final int PERMISSION = 1;
    TextToSpeech tts;
    String text;
    String destination_text;

    boolean tend = false;

    //Map
    private GoogleMap googleMap;
    private Marker currentMarker = null;

    private static final String TAG = " googlemap";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2002;
    private static final int UPDATE_INTERVAL_MS = 1000;
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; //5초
    private static final int PERMISSIONS_REQUEST_CODE = 100; //1초
    boolean needRequest = false;

    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};

    Location curLocation;
    LatLng curPosition;

    //실시간 위치 이동을 위한
    private GoogleApiClient googleApiClient = null;

    private AppCompatActivity activity;
    boolean askPermissionOnceAgain = false;
    boolean RequestingLocationUpdates = false;
    boolean mMoveMapByUser = false;
    boolean mMoveMapByAPI = true;

    //snackbar 사용을 위한 view
    private View mLayout;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    //private Location location;

    //T-map 연동
    private Element root;
    private TMapTapi tMapTapi;
    private TMapData tMapData;
    PolylineOptions polylineOptions;
    ArrayList<TMapPoint> arrayPoint;

    TMapPoint endpoint;
    TMapPoint startpoint;

    private ListView searchResult_listview;
    private List<String> list_data;
    private ArrayAdapter<String> adapter;
    private boolean display_listView = false;
    private boolean startLocation_finish = false;

    private Button findPath_btn;
    private long btnPressTime = 0;

    private boolean pathok = false;
    private ArrayList<PathItem> pathlist = null;
    private int pathlistIdx = 0;
    private StringBuilder pathPoint = new StringBuilder();
    String totalDistance, totalTime;

    // 지오펜싱 변수
    // realtime change
    private DatabaseReference myCity;
    private Location lastLocation;

    private GeoQuery geoQuery;

    private DatabaseReference myLocationRef;
    private GeoFire geoFire;
    private List<LatLng> dangerousArea;
    private IOnLoadLocationListener iOnLoadLocationListener;

    public static MapActivity newInstance() {
        MapActivity mfrag = new MapActivity();
        return mfrag;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_main_map, container, false);

        PopupActivity popupActivity = new PopupActivity(getActivity());
        popupActivity.callFunction();

        mLayout = v.findViewById(R.id.layout_map);

        activity = (AppCompatActivity) getActivity();

        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        myLocationRef = FirebaseDatabase.getInstance().getReference("MyLocation");
        geoFire = new GeoFire(myLocationRef);

       // TTS

        // 검색 버튼 한번 : 음성출력 / 두번 : 검색
        tts = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                    tts.setSpeechRate(0.9f);
                }
            }
        });

        textView = (TextView)v.findViewById(R.id.srch);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                speechToText();
            }
        });


        //MAP
        searchResult_listview = (ListView) v.findViewById(R.id.searchResultList);
        list_data = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, list_data);
        searchResult_listview.setAdapter(adapter);

        findPath_btn = (Button)v.findViewById(R.id.srch_btn);

        findPath_btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (System.currentTimeMillis() > btnPressTime + 1000) {
                    btnPressTime = System.currentTimeMillis();
                    tts.setSpeechRate(0.9f);
                    tts.speak(findPath_btn.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, null);
                    return;
                }
                if (System.currentTimeMillis() <= btnPressTime + 1000) {
                    try {
                        FindPOI(destination_text);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        tMapData = new TMapData();

        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS)
                .setSmallestDisplacement(10f);

        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);

        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(final LocationResult locationResult) {
                super.onLocationResult(locationResult);
                lastLocation = locationResult.getLastLocation();
                if (googleMap != null) {

                    Log.d(TAG, "buildLocationCallback()");
                    lastLocation = locationResult.getLastLocation();
                    curPosition = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                    // geofire에 내 위치 저장
                    curLocation = lastLocation;
                    Log.d(TAG, "last : " + lastLocation.getLatitude() +", " + lastLocation.getLongitude());
                    Log.d(TAG, "last : " + curLocation.getLatitude() +", " + curLocation.getLongitude());
                    addUserMarker();

                }
                Log.d(TAG, "Finish buildLocationCallback()");

            }

        };
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());



        SupportMapFragment mapFragment = (SupportMapFragment)getChildFragmentManager()
                .findFragmentById(R.id.Map);

        mapFragment.getMapAsync(this);

        Log.d(TAG, "initArea() Start");
        initArea();
        Log.d(TAG, "initArea() end");
        settingGeoFire();
        return v;
    }

    public void stop() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }


    // STT
    private void speechToText()
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

        mRecognizer = SpeechRecognizer.createSpeechRecognizer(getActivity());
        mRecognizer.setRecognitionListener(listener);
        mRecognizer.startListening(intent);
    }

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

            destination_text = textView.getText().toString();
            Intent intent = new Intent(getActivity(), PopupCheckDestActivity.class);
            intent.putExtra("data", destination_text);
            startActivityForResult(intent, 1);

            // Toast.makeText(getContext(), text1, Toast.LENGTH_SHORT).show();

            //http://stackoverflow.com/a/29777304

        }

        @Override
        public void onPartialResults(Bundle partialResults) {}

        @Override
        public void onEvent(int eventType, Bundle params) {}
    };

    //MAP
    // 1. googleMap
    @Override
    public void onResume() {
        super.onResume();

        if(googleApiClient.isConnected()){
            Log.d(TAG,"onResume : call startLocationUpdates");
            if(!RequestingLocationUpdates)
                startLocationUpdates();
        }

        //앱 정보에서 퍼미션을 허가했는지 다시 검사
        if(askPermissionOnceAgain){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                askPermissionOnceAgain = false;

                checkPermission();
            }
        }
    }



    private void addUserMarker() {
        Log.d(TAG, "addUserMarker() : ");
        Log.d(TAG, "curLocation : " + lastLocation.getLatitude() +", " + lastLocation.getLongitude());
        geoFire.setLocation("You", new GeoLocation(lastLocation.getLatitude(),
                lastLocation.getLongitude()), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (currentMarker != null) currentMarker.remove();
                currentMarker = googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lastLocation.getLatitude(),
                                lastLocation.getLongitude()))
                        .title("You"));
                //After add marker, move camera
                googleMap.animateCamera(CameraUpdateFactory
                        .newLatLngZoom(currentMarker.getPosition(), 90.0f));
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        setDefaultLocation();

        //런타임 퍼미션 처리
        //1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if(hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
        hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED){
            //2.퍼미션이 있다면
            //3. 위치 업데이트 시작
            startLocationUpdates();

            //T map 앱 연동
            tMapTapi = new TMapTapi(getContext());
            tMapTapi.setSKTMapAuthentication("l7xx1e068a7450414185a033d514672de76f");
            tMapTapi.setOnAuthenticationListener(new TMapTapi.OnAuthenticationListenerCallback() {
                @Override
                public void SKTMapApikeySucceed() {
                    Log.d("TMAP","SKTMAPAPIKEYSUCCEED");
                }

                @Override
                public void SKTMapApikeyFailed(String s) {
                    Log.d("TMAP","SKTMAPAPIKEYFAILED"+s);
                }
            });
        }
        else{
            //3-1. 퍼미션을 거부 한적이 있는 경우
            if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),REQUIRED_PERMISSIONS[0])){
                //3-2. 필요한 이유`
                Snackbar.make(mLayout,"위치 접근 권환이 필요합니다.",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인",new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        //3-3. 퍼미션 요청
                        ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
                    }
                }).show();
            }
            else{
                //4-1. 거부 한적이 없는 경우 바로 요청
                ActivityCompat.requestPermissions(getActivity(),REQUIRED_PERMISSIONS,PERMISSIONS_REQUEST_CODE);
            }
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());

        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.d(TAG,"onMapClick : ");
            }
        });

    }


    private void startLocationUpdates() {
        if(!checkLocationServicesStatus()) {
            Log.d(TAG,"startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        }
        else{
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION);

            if(hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
            hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED){
                Log.d(TAG,"startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }

            Log.d(TAG,"startLocationUpdates : call fuesedLocationClient.requestLocationUpdates");

            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());

            if(checkPermission())
            {
                googleMap.setMyLocationEnabled(true);
                addCircleArea();
            }

        }
    }

    @Override
    public void onStart() {
        if(googleApiClient != null && googleApiClient.isConnected() == false){
            Log.d(TAG,"onStart : googleApilient Connect");

            googleApiClient.connect();
        }
        super.onStart();
    }

    @Override
    public void onStop() {
        if(RequestingLocationUpdates){
            Log.d(TAG,"onStop : call stopLocationUpdates");
            stopLocationUpdates();
        }

        if(googleApiClient.isConnected()){
            Log.d(TAG,"onStop : GoogleApiClient disconnect");
            googleApiClient.disconnect();
        }

        super.onStop();

    }

    private void stopLocationUpdates() {

        Log.d(TAG,"stopLocationUpdates : LocationServices.FusedLocationApi.removeLocationUpdates");
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, (com.google.android.gms.location.LocationListener)this);
        RequestingLocationUpdates = false;
    }

    /*
    private void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {
        mMoveMapByUser = false;

        if(currentMarker != null) {
            currentMarker.remove();
        }

        LatLng curLatlng = new LatLng(location.getLatitude(),location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(curLatlng);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);

        currentMarker = googleMap.addMarker(markerOptions);

        if(mMoveMapByAPI){
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(curLatlng);
            googleMap.moveCamera(cameraUpdate);
        }

        Log.d(TAG, "addUserMarker() : ");
        geoFire.setLocation("You", new GeoLocation(location.getLatitude(),
                location.getLongitude()), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
            }
        });

    }*/

    private String getCurrentAddress(LatLng latLng) {
        //지오코더.. GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());

        List<Address> addresses;

        try{
            addresses = geocoder.getFromLocation(
                    latLng.latitude,
                    latLng.longitude,
                    1
            );
        }catch (IOException e){
            //네트워크 문제
            Toast.makeText(getActivity(),"지오코더 서비스 사용불가",Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        }catch (IllegalArgumentException e1){
            Toast.makeText(getActivity(),"잘못된 gps 좌표",Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        if(addresses == null || addresses.size() == 0){
            Toast.makeText(getActivity(),"주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        }
        else{
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }
    }

    private boolean checkPermission() {
        //런타임 퍼미션 처리를 위한 메소드들
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if(hasFineLocationPermission != PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED){
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode
                == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION && grantResults.length > 0){
            boolean permissionAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

            if(permissionAccepted){
                if(googleApiClient.isConnected() == false){
                    Log.d(TAG, "onRequestPermissionResult : mGoogleApiClient connect");
                    googleApiClient.connect();
                }
            }

            else
                checkPermission();
        }
    }

    private boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void setDefaultLocation() {
        //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기 전에
        //지도 초기위치 설정

        mMoveMapByUser = false;

        LatLng DEFAUT_LOCATION = new LatLng(37.451076, 126.656574);
        String markerTitle = "위치 정보를 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 요부 확인하세요";

        if(currentMarker != null) {
            currentMarker.remove();
        }

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAUT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = googleMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAUT_LOCATION,15);
        googleMap.moveCamera(cameraUpdate);
    }

    //GPS 활성화를 위한 메소드들
    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치서비스가 필요합니다.\n"
        +"위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent callGPSSettingIntent
                        = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case GPS_ENABLE_REQUEST_CODE:
                //GPS가 활성되었는지 검사
                if(checkLocationServicesStatus()){
                    if(checkLocationServicesStatus()){
                        Log.d(TAG,"onActivityResult : GPS 확성화");
                        needRequest = true;
                        return;
                    }
                }
                break;
            case 1:
                if(resultCode == RESULT_OK)
                {
                    String result = data.getStringExtra("result");
                    textView.setText(result);
                    tend = true;
                }
                else if(resultCode == RESULT_CANCELED) {
                    textView.setText("목적지 입력");
                }
                break;
        }
    }


    //2. T-map
    private void FindPOI(String destination_text) throws IOException {
        final String destination_ = destination_text.replace(" ","");
        //입력받은 목적지의 공백 제거
        System.out.println(destination_);
        //Log.d(text1,destination_);

            tMapData.findAllPOI(destination_text, new TMapData.FindAllPOIListenerCallback() {
                @Override
                public void onFindAllPOI(ArrayList<TMapPOIItem> items) {
                    for (int i = 0; i < items.size(); i++) {
                        TMapPOIItem item = (TMapPOIItem) items.get(i);
//                        Log.d("POI Name: ", item.getPOIName().toString() + ", " +
//                                "Address: " + item.getPOIAddress().replace("null", "") + ", " +
//                                "Point: " + item.getPOIPoint().toString());
                        String d = item.getPOIName();
                        d = d.replace(" ", "");
//                        System.out.println(d);
                        //검색어의 명칭에서 공백제거
                        if (d.equals(destination_)) {
                            endpoint = new TMapPoint(item.getPOIPoint().getLatitude(), item.getPOIPoint().getLongitude());
                            Log.d("POI Name: ", item.getPOIName().toString() + ", " +
                                    "Address: " + item.getPOIAddress().replace("null", "") + ", " +
                                    "Point: " + item.getPOIPoint().toString());
                            //StartSearchPath();
                            new FindPathData().execute();
                            return;
                        }
                    }
                    tts.speak("목적지를 찾지 못했습니다. 정확한 명칭을 입력해주세요.", TextToSpeech.QUEUE_FLUSH, null, null);
    //                return;
                }
            });

    }

    private class FindPathData extends AsyncTask<String, Void, Document> {
        String TAG = "FindPathData>>>";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pathlist = new ArrayList<PathItem>();

            /*  기존의 polyLine제거*/
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    googleMap.clear();
                    pathok = false;
                }
            });
        }

        @Override
        protected Document doInBackground(String... strings) {
            Log.d(TAG, "doInBackground: start!");
            Document document = null;

            int distance;
            startpoint = new TMapPoint(lastLocation.getLatitude(),lastLocation.getLongitude());
            try {
                document = new TMapData().findPathDataAllType(
                        TMapData.TMapPathType.PEDESTRIAN_PATH, startpoint, endpoint);  //  send find query to TMapServer..
                root = document.getDocumentElement();
                return document;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }
            Toast.makeText(activity.getApplicationContext(), "경로탐색에 실패 하였습니다.", Toast.LENGTH_LONG).show();
            return null;    //  if failed...
        }

        @Override
        protected void onPostExecute(Document doc) {
            if (doc != null) {
                /*  Parse by turntype and point information */
                XPathFactory xPathFactory = XPathFactory.newInstance();
                XPath xPath = xPathFactory.newXPath();
                try {
                    XPathExpression expr = xPath.compile("//Placemark");
                    NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
                    LatLng startLatlng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    polylineOptions = new PolylineOptions();
                    polylineOptions.width(30).color(Color.RED).add(startLatlng);

                    NodeList  Dis = root.getElementsByTagName("tmap:totalDistance");
                    int iTotalDistance = Integer.parseInt(Dis.item(0).getChildNodes().item(0).getNodeValue());
                    if (iTotalDistance > 1000) {
                        int km;
                        km = iTotalDistance / 1000;
                        totalDistance = km + "km";
                    } else {
                        totalDistance = iTotalDistance + "m";
                    }

                    NodeList time = root.getElementsByTagName("tmap:totalTime");
                    int iTotalTime = Integer.parseInt(time.item(0).getChildNodes().item(0).getNodeValue());  // 총 소요시간 정보 (second)
                    int h = iTotalTime / 3600;
                    int m = iTotalTime % 3600 / 60;
                    if (h <= 0)
                        totalTime = m + "분";
                    else
                        totalTime = h + "시간 " + m + "분";


                    for (int i = 0; i < nodeList.getLength(); i++) {
                        NodeList child = nodeList.item(i).getChildNodes();
                        int turnType = -1;
                        TMapPoint point;

                        for (int j = 0; j < child.getLength(); j++) {
                            Node node = child.item(j);
                            if (node.getNodeName().equals("tmap:turnType")) {
                                turnType = Integer.parseInt(node.getTextContent());
                            }

                            if (node.getNodeName().equals("Point")) {
                                String[] str = node.getTextContent().split(",");
                                point = new TMapPoint(Double.parseDouble(str[1]), Double.parseDouble(str[0]));
                                PathItem pathItem = new PathItem(true, turnType, point);
                                pathlist.add(pathItem);
                            }
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                pathPoint = new StringBuilder();
                for (int i = 0; i < pathlist.size(); i++) {
                    pathPoint.append(pathlist.get(i).toString() + "\n");
                }
               // System.out.println(pathPoint.toString());

                /*  Parse and draw PloyLine on TMapView*/
                NodeList list = doc.getElementsByTagName("LineString");
                for (int i = 0; i < list.getLength(); i++) {

                    Element item = (Element) list.item(i);
                    String str = HttpConnect.getContentFromNode(item, "coordinates");
                    if (str != null) {
                        String[] str2 = str.split(" ");
                        for (int k = 0; k < str2.length; k++) {
                            try {
                                String[] str3 = str2[k].split(",");
                                TMapPoint point = new TMapPoint(Double.parseDouble(str3[1]), Double.parseDouble(str3[0]));
                                LatLng pt = new LatLng(point.getLatitude(), point.getLongitude());
                                polylineOptions.add(pt);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                googleMap.addPolyline(polylineOptions);
                                LatLng position = new LatLng(endpoint.getLatitude(), endpoint.getLongitude());
                                String name = destination_text;
                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.title(name);
                                markerOptions.snippet("이동거리 : "+ totalDistance+"  소요시간 : "+ totalTime);
                                markerOptions.position(position);
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                                googleMap.addMarker(markerOptions).showInfoWindow();
                            }
                        });
                    }
                }
                pathlistIdx = 0;
            }
            System.out.println(pathPoint.toString());
        }

    }



    @Override
    public void onInfoWindowClick(Marker marker) {
        String markerId = marker.getId();
        Log.e("", "marker.getId()" + marker.getId());
    }

    @Override
    public void onLocationChanged(@NonNull Location lastLocation) {
        curPosition = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());

        Log.d(TAG,"onLocationChanged : ");

        String markerTitle = getCurrentAddress(curPosition);
        String markerSnippet = "위도: "+String.valueOf(lastLocation.getLatitude()) +" 경도: "+ String.valueOf(lastLocation.getLongitude());
        curLocation = lastLocation;
        addUserMarker();


    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(RequestingLocationUpdates == false){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                int hasFineLocationPermission =  ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION);
                if(hasFineLocationPermission == PackageManager.PERMISSION_DENIED){
                    ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                }
                else{
                    Log.d(TAG,"onConnected : 퍼미션 가지고 있음");
                    Log.d(TAG,"onConnected : call startLocationUpdates");
                    startLocationUpdates();
                    googleMap.setMyLocationEnabled(true);
                }
            }
            else{
                Log.d(TAG,"onConnected : call startLocationUpdates");
                startLocationUpdates();
                googleMap.setMyLocationEnabled(true);
            }
        }

    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG,"onConnectionSuspended");

        if(cause == CAUSE_NETWORK_LOST)
            Log.e(TAG,"onConnectionSuspended() : Google Play services "+
                    "connection lost. Cause : network lost.");
        else if(cause == CAUSE_SERVICE_DISCONNECTED)
            Log.e(TAG,"onConnectionSuspended(): Google Play servies "+
                    "connection lost. Cause: service disconnected.");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG,"onConnectionFailed");
        setDefaultLocation();
    }

    // 지오펜싱
    // 위험지역 넣기
    private void initArea() {

        myCity = FirebaseDatabase.getInstance()
                .getReference("RiskFactor");
        Log.d(TAG, "myCity" + myCity);
        iOnLoadLocationListener = this;
        // Load from Firebase

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
                iOnLoadLocationListener.onLoadLocationSuccess(latLngList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // setting geofire on my reference
    private void settingGeoFire() {

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
            googleMap.addCircle(new CircleOptions().center(latLng)
                    .radius(3) // 3m
                    .strokeColor(Color.BLUE)
                    .fillColor(0x220000FF)     // 22 is transparent code
                    .strokeWidth(5.0f)
            );


            // Create GeoQuery when user in dangerous location

            geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude), 0.003f); // 500m
            // onkeyentered 이런거 부르는 애
            geoQuery.addGeoQueryEventListener(this);
        }
    }

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
            tts.speak("차도와 인도의 구분이 모호합니다. 주의해주세요.", TextToSpeech.QUEUE_FLUSH, null, null);
        // tts.speak("근처에 위험요소가 있습니다. 주의해주세요.", TextToSpeech.QUEUE_FLUSH, null, null);
        //sendNotification("EDMTDev", String.format("%s entered the dangerous area", key));
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
        Toast.makeText(getActivity(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoadLocationSuccess(List<MyLatLng> latLngs) {
        Log.d("latLngs : ", latLngs.toString());
        dangerousArea = new ArrayList<>();

        for(MyLatLng myLatLng : latLngs)
        {
            LatLng convert = new LatLng(myLatLng.getLatitude(), myLatLng.getLongitude());
            dangerousArea.add(convert);

        }
        Log.d("dangerous : ", dangerousArea.toString());
        // New, after dangerous Area is have data, we will call MAp display
        SupportMapFragment mapFragment = (SupportMapFragment)getChildFragmentManager()
                .findFragmentById(R.id.Map);

        mapFragment.getMapAsync(this);

        // clear map and add again
        if(googleMap != null)
        {
            googleMap.clear();;
            // Add user Marker
            Log.d(TAG, "onLoadLocationSuccess()");

            addUserMarker();

            // Add Circle of dangerous area
            addCircleArea();
        }
    }

    @Override
    public void onLoadLocationFailed(String message) {
        Toast.makeText(getActivity(), ""+message,Toast.LENGTH_SHORT).show();
    }

}
