package com.comp.veta.ui.map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.comp.veta.Background.Event;
import com.comp.veta.Background.Group;
import com.comp.veta.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.JsonObject;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.GeoJson;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.map.NavigationMapboxMap;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;



public class MapFragment extends Fragment implements OnMapReadyCallback, MapboxMap.OnMapClickListener, PermissionsListener, MapboxMap.OnMarkerClickListener {

    View root;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    DocumentReference userRef;
    DocumentReference groupRef;

    MapView mapView;
    MapboxMap mapboxMap;
    LocationComponent locationComponent;
    PermissionsManager permissionsManager;
    DirectionsRoute currentRoute;
    NavigationMapRoute navigationMapRoute;
    private static final String ID_ICON = "ICON_ID";
    private FloatingActionButton fabSearch;

    private LatLng currentLocation;

    private Button button;
    private static Point currentPoint2,destinationPoint2;

    private GeoJsonSource source;
    private String geoJsonSourceLayerId="geoJsonSourceLayerId";
    public CarmenFeature place1;
    public CarmenFeature place2;
    public CarmenFeature place3;

    Intent sendThrough;

    @Override
     public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(getActivity(), "sk.eyJ1Ijoia2FyYW5qYWluMjIiLCJhIjoiY2t6dm52bm93N3BtdTJ5cHIyYTQ4MGtxbCJ9.KPY2XQfiS7ahtTRPKY5jLw");

       // setContentView(R.layout.activity_main);




    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = (MapView) root.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        Button navButton = root.findViewById(R.id.startNavigationButton);
        navButton.setOnClickListener(this::startNavigationBtnClick);

        fabSearch= root.findViewById(R.id.floatingActionButtonSearch);
        fabSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchForLocation();

            }
        });

        return root;

    }




    private void searchForLocation(){

        Point pointofProx = Point.fromLngLat(currentLocation.getLongitude(),currentLocation.getLatitude());
        //add stuff here
      //  buildPlaces();

        ArrayList<Event> events = new ArrayList();
    //    Log.d("poop","events.get(2).getEventName()");
        db.collection("groups")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //  Log.d(TAG, document.getId() + " => " + document.getData());
                                Group group = document.toObject(Group.class);
                                events.addAll(group.getEvents());

                            }
                            Collections.sort(events);

                            SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yy hh:mm a");

                            if(events.get(0)!=null)
                            place1 = CarmenFeature.builder().text(events.get(0).getEventName()+" "+dateFormatter.format(events.get(0).getEventTime()))
                                    .geometry(Point.fromLngLat(events.get(0).getLONGITUDE(), events.get(0).getLATITUDE()))
                                    .placeName(events.get(0).getEventStringLocation())
                                    .properties(new JsonObject())
                                    .build();
                            if(events.get(1)!=null)
                            place2 =  CarmenFeature.builder().text(events.get(1).getEventName()+" "+dateFormatter.format(events.get(1).getEventTime()))
                                    .geometry(Point.fromLngLat(events.get(1).getLONGITUDE(), events.get(1).getLATITUDE()))
                                    .placeName(events.get(1).getEventStringLocation())
                                    .properties(new JsonObject())
                                    .build();
                            if(events.get(2)!=null)
                            place3 =  CarmenFeature.builder().text(events.get(2).getEventName()+" "+dateFormatter.format(events.get(2).getEventTime()))
                                    .geometry(Point.fromLngLat(events.get(2).getLONGITUDE(), events.get(2).getLATITUDE()))
                                    .placeName(events.get(2).getEventStringLocation())
                                    .properties(new JsonObject())
                                    .build();

                            sendThrough = new PlaceAutocomplete.IntentBuilder().accessToken(Mapbox.getAccessToken()).placeOptions(
                                    PlaceOptions.builder().backgroundColor(Color.WHITE).hint("Enter an Address").proximity(pointofProx).addInjectedFeature(place1).addInjectedFeature(place2).addInjectedFeature(place3)
                                            .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS, GeocodingCriteria.TYPE_POI, GeocodingCriteria.TYPE_PLACE).limit(5).build(PlaceOptions.MODE_CARDS)
                            ).build(getActivity());

                            startActivityForResult(sendThrough, 1);

                        }
                    }
                });


    }



    public void startNavigationBtnClick(View v){
        if (currentRoute == null) {
            return; // Route has not been set, so we ignore the button press
        }
        boolean simulateRoute = true;
        NavigationLauncherOptions options = NavigationLauncherOptions.builder().directionsRoute(currentRoute).shouldSimulateRoute(simulateRoute).build();

        NavigationLauncher.startNavigation(getActivity(),options);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {
        if(granted){
            enableLocationComponent(mapboxMap.getStyle());
        }
        else{
            Toast.makeText(getActivity(), "Permission Not granted",Toast.LENGTH_LONG).show();
            getActivity().finish();
        }
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {


        Point destinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        Point originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                locationComponent.getLastKnownLocation().getLatitude());

        GeoJsonSource source= mapboxMap.getStyle().getSourceAs("destination-source-id");

        if(source!=null){

            source.setGeoJson(Feature.fromGeometry(destinationPoint));

        }


        getRoute(originPoint, destinationPoint);

        return true;





    }

    private void getRoute(Point originPoint, Point destinationPoint) {
        Log.d("hello?", originPoint.toString());
        Log.d("hello?", destinationPoint.toString());
        NavigationRoute.builder(getActivity())
                .accessToken(Mapbox.getAccessToken())
                .origin(originPoint).destination(destinationPoint)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {


                        if(response.body()!=null && response.body().routes().size()>=1){
                            currentRoute = response.body().routes().get(0);

                            if(navigationMapRoute!=null){
                                navigationMapRoute.removeRoute();
                            }
                            else{
                                navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);

                            }

                            navigationMapRoute.addRoute(currentRoute);

                        }
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                        Log.d("LOGG", "fail");

                    }
                });



    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {

        this.mapboxMap = mapboxMap;

        this.mapboxMap.setMinZoomPreference(15);

        //     Marker mark = mapboxMap.addMarker(new MarkerOptions().position(new LatLng(42.1,75.0)));


//        mapboxMap.setOnMarkerClickListener(this);
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                enableLocationComponent(style);
                addDestinationIconLayer(style);

                mapboxMap.addOnMapClickListener(MapFragment.this::onMapClick);
              //  button = root.findViewById(R.id.nav);


               // currentLocation = new LatLng(mapboxMap.getLocationComponent().getLastKnownLocation().getLatitude(),mapboxMap.getLocationComponent().getLastKnownLocation().getLongitude());

                currentLocation = new LatLng(40,-75);

            }


        });



    }
    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Log.d("THINGMARK",""+marker.getPosition());
        return true;
    }
    private void addDestinationIconLayer(Style style)
    {

        style.addImage("destination-icon-id", BitmapFactory.decodeResource(this.getResources(),R.drawable.mapbox_marker_icon_default),false);

        GeoJsonSource geoJsonSource = new GeoJsonSource("destination-source-id");
        style.addSource(geoJsonSource);

        SymbolLayer destinationSymbolLayer= new SymbolLayer("destination-symbol-layer-id", "destination-source-id");

        destinationSymbolLayer.withProperties(iconImage("destination-icon-id"), iconAllowOverlap(true), iconIgnorePlacement(true));

        style.addLayer(destinationSymbolLayer);


    }

    private void enableLocationComponent(@NonNull Style loadedMapStyle) {

        if (PermissionsManager.areLocationPermissionsGranted(getActivity())) {
            locationComponent = mapboxMap.getLocationComponent();
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationComponent.activateLocationComponent(getActivity(), loadedMapStyle);
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING );

        }
        else{
            permissionsManager = new PermissionsManager(this);

            permissionsManager.requestLocationPermissions(getActivity());

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode==getActivity().RESULT_OK && requestCode == 1){
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);

            if (mapboxMap!=null){
                Style style = mapboxMap.getStyle();

                if(style!=null){

                    GeoJsonSource geoJsonSource = style.getSourceAs(geoJsonSourceLayerId);
                    if(geoJsonSource!=null){
                        geoJsonSource.setGeoJson(FeatureCollection.fromFeatures(new Feature[] {
                                        Feature.fromJson(selectedCarmenFeature.toJson())

                                }
                        ));
                    }
                    getCurrentLocation();

                    destinationPoint2 = (Point) selectedCarmenFeature.geometry();
                    Log.d("TEST",destinationPoint2.toString());
                    LatLng destination = new LatLng(destinationPoint2.latitude(), destinationPoint2.longitude());
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(destination).zoom(15).build()), 2000);

                    source = mapboxMap.getStyle().getSourceAs("destination-source-id");

                    if(source!=null){
                        source.setGeoJson(Feature.fromGeometry(destinationPoint2));
                    }
//                    currentPoint2 = Point.fromLngLat(-75,40);
                    getRoute(currentPoint2, destinationPoint2);

                }

            }

        }
    }

    private void getCurrentLocation() {

        currentLocation = new LatLng(mapboxMap.getLocationComponent().getLastKnownLocation().getLatitude(), mapboxMap.getLocationComponent().getLastKnownLocation().getLongitude());

        currentPoint2 = Point.fromLngLat(currentLocation.getLongitude(),currentLocation.getLatitude());


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);


    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }
/*
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }


 */



    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}