package com.comp.sickbook.ui.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;

import android.content.pm.PackageManager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import com.comp.sickbook.AppContext;
import com.comp.sickbook.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.Places;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapFragment extends Fragment implements GoogleMap.OnInfoWindowClickListener, GoogleMap.OnPolylineClickListener {

    private static String TAG = MapFragment.class.getSimpleName();

    private ArrayList < PolylineData > mPolylineData = new ArrayList < > (); // Creates arraylist that will store information about polyline in a parallel array based way
    private GeoApiContext mGeoApiContext;// Create the object that will read the API key and connect to to the Geocoding API
    MapView mMapView; // creates the map view
    private GoogleMap googleMap; //creates the programmable map
    private PlacesClient placesClient; // Checks is Places API key words
    private Marker locMark; // Location of marker from autocomplete search bar

    private double userLat; //User home latitude
    private double userLong;//User home longitude
    private double polyLat;//Ending Polyline latitude that can store beginning latitude if it is needed
    private double polyLong;//Ending Polyline longitude that can store beginning longitude if it is needed
    private Marker userMarker;// Marker for the user's home

    private ClusterManager < MyItem > clusterManager;// Manages making all the vaccine/testing points in
    // cluster and determines how the clusters need to be organized based upon the map zoom and location

    /**
     * This is a dummy method used to trick the computer into allowing for polymorphism to occur at a later line of code
     * @param marker
     */
    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    /**
     * This method handles anytime a polyline is clicked
     * It needs to highlight the selected polyline and unhighlight the unselected polylines
     * It also needs to update the marker information window with the new facts about the new route
     * Every polyline represents a route
     *
     * @param polyline:This is the polyline clicked on
     */

    @Override
    public void onPolylineClick(Polyline polyline) {
        int index = 0;      // Index if an arbitrary number assigned to each route
        for (PolylineData polylineData: mPolylineData) { // For every route stored
            index++;
            Log.d(TAG, "onPolylineClick: toString: " + polylineData.toString());  //This is for debugging purposes and to organize the Java output
            if (polyline.getId().equals(polylineData.getPolyline().getId())) { //if the route clicked on matches the route that is currently being indexed
                polylineData.getPolyline().setColor(ContextCompat.getColor(getActivity(), R.color.quantum_googblueA100)); // Change route color to blue
                polylineData.getPolyline().setZIndex(1); // Add semi-3d effect to route

                if (polyLat == userLat && polyLong == userLong) {  // If the origin of the trip is the home then
                    // All of this code is to format userMaker which the the marker that represents the peron's home
                    userMarker.setTitle("Trip: # " + index);  // Since we are starting at home, it would make logical sense to adapt the home info window
                    userMarker.setSnippet("Duration: " + polylineData.getLeg().duration);
                    locMark.setTitle("Location Chosen"); // The location chosen is the endpoint fo the trip
                    locMark.setSnippet("");
                    userMarker.showInfoWindow();
                } else { // if the Directions from selected option is chosen which means if the starting point of the trip was NOT the home marker then
                    locMark.setTitle("Trip: # " + index);
                    locMark.setSnippet("Duration: " + polylineData.getLeg().duration);
                    userMarker.setTitle("YOU!!"); //User marker is where the location selected is
                    userMarker.setSnippet("");
                    locMark.showInfoWindow();

                }
            } else { // If the route currently being indexed is not the route that was clicked on then
                polylineData.getPolyline().setColor(ContextCompat.getColor(getActivity(), R.color.darkGray)); // Sets color of that line to dark gray
                polylineData.getPolyline().setZIndex(0);//Gets rid of any 3d effect
                // meant to make sure the lines not selected are not of great visual importantance to the user
            }
        }
    }

    /**
     * This class is meant to store one marker point's information
     * It is used to make it easier to cluster markers
     *
     */
    public class MyItem implements ClusterItem {

        // All of these had to be final as the title, position and snippets are generated procedurally and at no point will they change
        private final LatLng position;
        private final String title;
        private final String snippet;

        /**
         * Constructor method
         * Used to set the position of the market and it's title and snippet
         * @param lat Latitude component of coordinate
         * @param lng Longitude component of coordinate
         * @param title Title of Info Window
         * @param snippet Snippet Under Title Window
         */
        public MyItem(double lat, double lng, String title, String snippet) {
            position = new LatLng(lat, lng);
            this.title = title;
            this.snippet = snippet;
        }

        /**
         * Used to get position variabe
         *
         * @return Latititude Longitude Coordinate
         */
        @Override
        public LatLng getPosition() {
            return position;
        }

        /**
         * Used to get the title variable of a MyItem object
         *
         * @return String title
         */
        public String getTitle() {
            return title;
        }

        /**
         * Used to get the snippet variable of a MyItem object
         *
         * @return String snippet
         */
        public String getSnippet() {
            return snippet;
        }

    }

    /**
     * Purpose: Set up all clusters when the map is refreshed or initialized
     *
     * This method creates the clusters and adds them to the map
     * It also checks if clusters need to be redistributed in case of a camera zoom
     *
     */
    private void setUpClusterer() {

        // Initialize the manager with the context and the map.
        // Makes an arraylist that will contain all the COVID vaccine/testing center markers and these are then clustered in the arraylist
        clusterManager = new ClusterManager < MyItem > (AppContext.getAppContext(), googleMap);

        // When the camera is not moving then rerender the cluster and see if the clusters in the camera view needs to break apart or come together
        // it listens for when the camera is not doing something, like after a zoom in or out
        googleMap.setOnCameraIdleListener(clusterManager);

        // Add cluster items (markers) to the cluster manager.
        addItems();
    }

    /**
     * Purpose: Gets the Latitude, Longitude, Address, Facility Name and Website of every JSON Object
     * in the VaccineAndTestingProviders.json file
     *
     * This creates a MyItem Object for every JSON Object and then adds it to the list of markers that will be clustered
     *
     *
     */
    private void addItems() {

        // Loads the String JSON file gotten from the loadJSONFromAsset method
        String json = loadJSONFromAsset("VaccineAndTestingProviders.json");

        // The JSON objects in the json String and converted to one element that has all the objects
        // The JSON element is then broken down into a JSON array so that each JSON object is iterable
        // The JSON array contains all the JSON objects
        JsonElement jsonElement = new JsonParser().parse(json);
        JsonArray jsonArray = jsonElement.getAsJsonArray();

        for (int i = 0; i < jsonArray.size(); i++) { //for every JSON object in the jsonArray
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject(); // Convert the JSON Array element to a JSON object element to make it easy to extract information from it
            double lat = jsonObject.get("Latitude").getAsDouble(); // Find Latitude by finding the value for the Latitude key in the JSON object Latitude key-value system
            double lng = jsonObject.get("Longitude").getAsDouble();// Find Longitude by finding the value for the Longitude key in the JSON object Latitude key-value system
            String snip = jsonObject.get("Address").getAsString() + "\n" + jsonObject.get("Website").getAsString(); // makes the snippet (what goes under the title and essentially provides a description) by using the Adress and Website

            MyItem offsetItem = new MyItem(lat, lng, jsonObject.get("Facility_Name").getAsString(), snip); // Turn this information above into a MyItem marker
            clusterManager.addItem(offsetItem); // Add the myItem object into the cluster manager to be clustered
        }
    }

    /**
     *
     * Purpose: Determines the how much zoom in or out needs to be done when a new route is selected and applies that zoom to the map
     *
     * The method makes sure that the user is able to see the entire new route selected
     * This new route is the route for the polyline clicked or for the new zoom for a newly selected starting and end point
     *
     * @param lstLatLngRoute These are major coordinate points for the route from one marker to another for a specific polyline
     */
    public void zoomRoute(List < LatLng > lstLatLngRoute) {
        // if there are no routes or if the map doesn't exist then it is impossible to actually add anything
        if (googleMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        // the boundsBuilder will help determine what the boundries are for the route which can be used for te zoom as the boundries=the perfect zoom
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint: lstLatLngRoute) // For every major point on the specific route from one marker to another
            boundsBuilder.include(latLngPoint); // Include that point when determining the boundary

        int routePadding = 120;
        LatLngBounds latLngBounds = boundsBuilder.build(); // Now that the boundBuilder has the entire route it
        // can generate optimal boundry conditions for our route

        // Animate the camera by allowing it to first determine the the bounds for the view it needs to have for an efficient zoom
        // From there the camera will glide and zoom or out to fit into the boundary points
        googleMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),
                600,
                null
        );
    }

    /**
     *
     * Purpose: Read JSON file from the assets folder and convert it from a JSON file with JSON object to a String
     *
     * This method makes sure that no file reading error occurs
     *
     * This is method used to make it easier to read different JSON files and is used to process the VaccineAndTestingProvides.json file
     *
     * JSON files have to be loaded from the assets folder for Android app development
     *
     * @param file: Name of the JSON file that is being read; just the file name and not the file path
     * @return String with all of the JSON Objects in the JSON file being read
     */
    public String loadJSONFromAsset(String file) {
        String json = null; // At first, there it nothing for the json string as the file has not been read yet
        try { // try to open the file and read it
            InputStream is = getActivity().getAssets().open(file); // Open the file from the assets folder as that is where JSON files must be stored for android app development
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer); // Read through everything in the JSON file by using a buffer to keep track of position and data
            is.close();
            json = new String(buffer, "UTF-8"); // Stringify the JSON file
        } catch (IOException ex) { // if unable to read the file or to open the file then return null
            ex.printStackTrace();
            return null;
        }
        return json; // Return the stringified JSON
    }

    /**
     * Purpose: This creates a search bar that gives options for locations that the user may be trying to type
     *
     * The search bar is said to have an "autocomplete" feature as the user does not have to type the entire location out as the
     * search bar extends itself to gives different autocompleted location names that are clickable
     *
     * This method uses the Place API through a Place API Key in order to allow for autocomplete
     * This requires an internet connection for the device
     *
     */
    public void makeSearchBar() {


        if (!Places.isInitialized()) { // First check to see if the Places API is initialized on the device
            // If the Places API is not initialize then initialize it in context of the app which just means that when initialized, it will work anywhere in the app
            // Get the API Key from the .xml file it is stored in
            Places.initialize(AppContext.getAppContext(), getString(R.string.PLACES_API_KEY));

        }

        // Check to see if a Client can be made as if it can be made then places was initialized properly and it was initialized in context of the ap
        PlacesClient placesClient = Places.createClient(AppContext.getAppContext());

        // The search bar is a fragment the Map is also a fragment so for th search bar to be a a part of the map
        // it must become a child fragment of the Map fragment for it to work in context of the app
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment); // This line finds the search bar in the fragment_map.xml UI file and allows for it to be programmable
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));  // This allows the ID, Name and Coordinate points to be returned
        // anytime a user chooses a location from the search bar
        /**
         * When a place is selected and a user clicks on it in the search bar then this code will run
         *
         * It listens for the time where the user chooses a place
         *
         * It is is an asynchronous task so other code can be run while this code is waiting
         *
         */
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {

            /**
             * When a place is selected the zoom in on the location chosen and add a marker for it
             * @param place
             */
            @Override
            public void onPlaceSelected(Place place) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 13f)); // Zoom in on the place selected
                locMark.setPosition(place.getLatLng()); //Sets the position of a marker to the Latitude and Longitude of the place selected
                locMark.setTitle("Location Chosen");
            }

            /**
             * If any error occurs then print that error
             *
             * @param status is the error message
             */
            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });

    }

    /**
     * Purpose: Adds and renders all the new polyline routes for every efficient path that is between the two markers that the user
     * wanted to find routes for
     *
     * This method will also remove all polylines and polyline data if there are previous polylines that exist
     * from the users previous request for directions between two markers
     *
     * This method also checks to see which route is most efficient
     *
     * @param result These are all the results for the most viable and most efficient directions from one marker to another
     */
    private void addPolylinesToMap(final DirectionsResult result) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {  // This code is run on the main thread of the app to make sure this code is run as fast as possible
            /**
             * This method will add the polylines to the map when the polylines need to be added
             */
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length); // This is for printing values for debugging

                //This code is done to delete any previous routes that exist and delete previous polylines to make way for the new polylines
                if (mPolylineData.size() > 0) { //If there are previous polylines to through then go through and iterate through
                    // and delete previous polyline data
                    for (PolylineData polylineData: mPolylineData) { // Get all previous polyline data and remove all polylines from the map
                        polylineData.getPolyline().remove();

                    }
                    mPolylineData.clear(); // Clear all the data about polylines
                    mPolylineData = new ArrayList < > (); //reset the mPolylineData as an ArrayList
                }
                double duration = 999999;
                for (DirectionsRoute route: result.routes) { // for every route given by the result variable do

                    //The path from one place to another is by default encrypted by google
                    //Start the process of decoding the path by finding all the LatLng coordinates that make up a path
                    List < com.google.maps.model.LatLng > decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());
                    //com.google.maps.model.LatLng is different than a normal new LatLng
                    List < LatLng > newDecodedPath = new ArrayList < > ();

                    // This loops through all the LatLng coordinates of one polyline.
                    // It finds the decoded path of the normal LatLngs, not the com.google.maps.model.LatLng LatLngs
                    for (com.google.maps.model.LatLng latLng: decodedPath) {
                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }

                    // Now that a polyline's path is known, it can be added to the the map and be allwoed to be clickable and it's colot can change
                    Polyline polyline = googleMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(getActivity(), R.color.darkGray));
                    polyline.setClickable(true);

                    //This adds specific data for a polyline and essentially creates a parallel array and makes a PolylineData object with the legs being the chunks of the path
                    // Legs[0] is the chunk of the path that will lead to the starting marker to the ending marker
                    mPolylineData.add(new PolylineData(polyline, route.legs[0]));

                    // The polyline with the lowest time should be highlighted
                    double tempduration = route.legs[0].duration.inSeconds; //the time for the trip of the route of this specific polyline
                    // If the duration of the trip for the current polyline
                    // is the lowest out of all the polylines so far then make the current polyline blue and change the new lowest time
                    if (tempduration < duration) {
                        duration = tempduration;
                        onPolylineClick(polyline); // The polyline being clicked = the polyline turning blue while
                        // the rest turn gray, will also work the same way in this situation as we are "clicking" on the polyline with the shortest path
                        zoomRoute(polyline.getPoints()); // Zoom in on the shortest route
                    }

                }
            }
        });
    }

    /**
     *
     * Purpose: If user deems that the map needs to be refreshed then this will wipe the map and revert it back to the state
     * the map was in when the map tab was opened
     *
     * This method gets rid of all polylines, polyline data, and directional information
     *
     * This method also clears all items that were on the map such as the markers and adds them again as it was tested
     * to be the most efficient way to reset the map
     *
     * Map Camera zoom and position is restored to being on Pennsylvania with proper zoom from where
     * the camera was before and whatever zoom the user had set the camera too
     *
     */

    private void resetMap() {
        if (googleMap != null) {  // if the map is null then it is already cleared as there is no map
            googleMap.clear(); // this clear all markers and zoom and camera orientations

            // if there were polylines on the map then delete all associated information with them
            if (mPolylineData.size() > 0) {
                mPolylineData.clear();
                mPolylineData = new ArrayList < > ();
            }

            setUpClusterer(); //re-add the clusters to the map
            //re-add the user location marker to the map
            userMarker = googleMap.addMarker(new MarkerOptions().position(new LatLng(userLat, userLong)).icon(BitmapDescriptorFactory.defaultMarker(120)).zIndex(1.0f));

            //set information in the info box of the userMarker
            userMarker.setTitle("This is You");
            userMarker.setSnippet("");
            userMarker.setVisible(true);

            // If the user is at the orign of a potential trip then they do not have a selected location and that marker can be added
            //and hidden on the map as it is not used
            if (userLat == polyLat && userLong == polyLong) {
                locMark = googleMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).icon(BitmapDescriptorFactory.defaultMarker(20)).zIndex(1.0f));
                locMark.setPosition(new LatLng(0, 0));
            } else { // The selected location is being used and will be at the start of the previous polyline path which is where it was at before the refresh
                locMark = googleMap.addMarker(new MarkerOptions().position(new LatLng(polyLat, polyLong)).icon(BitmapDescriptorFactory.defaultMarker(20)).zIndex(1.0f));
                locMark.setPosition(new LatLng(polyLat, polyLong));
            }
            //General information about the selected location marker is added
            locMark.setTitle("Location Chosen");
            locMark.setSnippet("");
            // The map zooms in on Pennsylvania
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(41.2033, -78), 6.5f));

        }
    }

    /**
     *
     * Purpose: This find all the optimal directions from a starting marker to an ending marker
     *
     * The Google Geocoding API is used in order to find the directions using external Google generated data
     *
     * This method check for all possible directions and narrow it down to the few that are optimal
     *
     * Directions from Starting to end point is the same as directions from ending to starting point with the Geocoding API
     *
     * The Google Geocoding API allows for all routes to be found with the directions stored that can later be broken down into
     * longitude and latitude components
     *
     * @param marker This is the starting point
     * @param lat This the latitude coordinate of the destination point
     * @param lng This the longitude coordinate of the destination point
     */
    private void calculateDirections(Marker marker, double lat, double lng) {

        // Determine the ending point that the Geocoding API compatible
        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                lat,
                lng
        );

        //Establish API request in order to get directions
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        // Allow for multiple different directions to be given and establishes the origin at the marker variable
        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(
                        marker.getPosition().latitude,
                        marker.getPosition().longitude
                )
        );

        /**
         * When the Geocoding API is able to find all directions that are viable then it will start to run the polyline methods
         *
         * The code will wait for the Geocoding API to return the directions
         *
         */
        directions.destination(destination).setCallback(new PendingResult.Callback < DirectionsResult > () {
            /**
             * When the Geocoding API finds the directions, the result contains lists of those directions with points
             *
             * This will then add al directions to the map in form of polylines
             *
             * @param result These are all the directions that the Geocoding API considered optimal for the course
             */
            @Override
            public void onResult(DirectionsResult result) {

                addPolylinesToMap(result);
            }

            /**
             * If it is unable to find directions or get the directions from the Geocoding API it will throw an error and get a failure
             * @param e
             */
            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage()); // This is for debugging and understanding the error

            }
        });
    }

    /**
     *
     * Purpose: This creates the entire map converting the map from a UI component into a Java/Android component
     *
     * This method sets what the map actually is and is linked to the UI file and displayed the fragment_map.xml UI file
     *
     * It allows for the user to see the map and the map to be rendered when the map tab is clicked
     *
     *
     *
     * @param inflater This is used to set what the view will be and what .xml file(UI file) will be viewed.
     * @param container This is used to allow for the Map to be contained in the entire UI. Essentially, it allows for the Map
     *                  fragments of the UI to coexist with the navigation bar components
     * @param savedInstanceState This is used to actually render the map itself and all the data for the map to be rendered is stored
     *                           in a Bundle. This all allows for an efficient building as it essentially gives the phone direction
     *                           on how to render the map
     * @return This method return the view of the map, or what the user sees. This is NOT to be mistaken with the camera.
     *         This view is being used to view the entire Map tab, including the navigation bar, the camera is being used to view the map
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // First, the fragment_map.xml UI file is attached to the overall UI when the tab is opened and the view is created
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        mMapView = (MapView) rootView.findViewById(R.id.mapView); // The view of the map is found and set so that the user can view the UI files
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        // The app needs to initialize in context of the whole app in order for it to be programmable
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
/**
 * This checks when the google map is ready to be used for Java programming
 * and is called when the map is able to have features added to it
 *
 * It waits until the map is ready to be viewed, then it runs the code inside it
 *
 * Since it is asynchronous, other code ahead of it can run and it will wait until the map is read
 *
 * If the map is not ready to be viewed then it is essentially just a UI component that cannot be manipulated using Java
 *
 */
        mMapView.getMapAsync(new OnMapReadyCallback() {

            /**
             * This waits for when the map is ready then initializes all components of the map
             *
             * When the map is ready, then Java code can be used to manipulate it
             *
             * @param mMap this the map that can be manipulated using Java code and if anything is added to this map then it will show
             *             up on the app
             */
            @Override
            public void onMapReady(GoogleMap mMap) {

                googleMap = mMap;// Set the programmable map

                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(41.2033, -78), 6.5f));//Zoom in on pennslyvannia

                userLat = 40.1409428; // user location
                userLong = -75.1668414; // user location

                // Set the user marker and user positions
                userMarker = googleMap.addMarker(new MarkerOptions().position(new LatLng(userLat, userLong)).icon(BitmapDescriptorFactory.defaultMarker(120)).zIndex(1.0f));
                userMarker.setTitle("This is you");

                // Try to establish the API Key connect for the Geocoding API by using the API key securely stores in the strings.xml file
                if (mGeoApiContext == null) {
                    mGeoApiContext = new GeoApiContext.Builder().apiKey(getString(R.string.GEO_API_KEY)).build();
                }

                // Add the refresh button Button from frament_map.xml to the Map screen and allow it to be programmable
                @SuppressLint("WrongViewCast") AppCompatImageButton refreshButton = (AppCompatImageButton) rootView.findViewById(R.id.btn_reset_map);

                /**
                 * This checks to see if the refresh button is pressed
                 *
                 * It is an asynchronous method therefore other code can run while this lister waits
                 *
                 * This method only runs when the refresh button is clicked and resets the map
                 *
                 */
                refreshButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { // if the refresh button is clicked then reset the map
                        resetMap();
                    }
                });

                /**
                 *
                 * This listener is o check if a polyline is ever clicked
                 *
                 * If a polyline is clicked then it turns blue, becomes zoomed in on, and has its travel time estimated shown
                 *
                 * It is an asynchronous method therefore other code can run while this lister waits
                 *
                 */
                googleMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {

                    /**
                     * Purpose: The method changes the color of the polyline and the information in the info window box
                     *
                     * This changes the info windoe for the userMarker as each path has an arbitrary trip
                     * number associated with it and each path has a duration associated with it
                     *
                     * @param polyline
                     */
                    @Override
                    public void onPolylineClick(Polyline polyline) { // this code is the same method from above but due to Android
                        // polymorphism and scope. it is used to override the method call
                        //                                          //and allows for polylines to be clicked multiple times
                        int index = 0;      // Index if an arbitrary number assigned to each route
                        for (PolylineData polylineData: mPolylineData) { // For every route stored
                            index++;
                            Log.d(TAG, "onPolylineClick: toString: " + polylineData.toString());  //This is for debugging purposes and to organize the Java output
                            if (polyline.getId().equals(polylineData.getPolyline().getId())) { //if the route clicked on matches the route that is currently being indexed
                                polylineData.getPolyline().setColor(ContextCompat.getColor(getActivity(), R.color.quantum_googblueA100)); // Change route color to blue
                                polylineData.getPolyline().setZIndex(1); // Add semi-3d effect ot route

                                if (polyLat == userLat && polyLong == userLong) {  // If the origin of the trip is the home then
                                    // All of this code is to format userMaker which the the marker that represents the peron's home
                                    userMarker.setTitle("Trip: # " + index);  // Since we are starting at home, it would make logical sense to adapt the home info window
                                    userMarker.setSnippet("Duration: " + polylineData.getLeg().duration);
                                    locMark.setTitle("Location Chosen"); // The location chosen is the endpoint fo the trip
                                    locMark.setSnippet("");
                                    userMarker.showInfoWindow();
                                } else { // if the Directions from selected option is chosen which means if the starting point of the trip was NOT the home marker then
                                    locMark.setTitle("Trip: # " + index);
                                    locMark.setSnippet("Duration: " + polylineData.getLeg().duration);
                                    userMarker.setTitle("YOU!!"); //User marker is where the location selected is
                                    userMarker.setSnippet("");
                                    locMark.showInfoWindow();

                                }
                            } else { // If the route currently being indexed is not the route that was clicked on then
                                polylineData.getPolyline().setColor(ContextCompat.getColor(getActivity(), R.color.darkGray)); // Sets color of that line to dark gray
                                polylineData.getPolyline().setZIndex(0);//Gets rid of any 3d effect
                                // meant to make sure the lines not selected are not of great visual importantance to the user
                            }
                        }
                    }
                });
                // A location marker is made and hidden so that it can be called upon easily
                locMark = googleMap.addMarker(new MarkerOptions().position(new LatLng(41.2033, 77.1945)).icon(BitmapDescriptorFactory.defaultMarker(20)).zIndex(1.0f));

                // set the polyLine route to the user location leading to no route being created
                polyLat = userLat;
                polyLong = userLong;

                //Search bar and clusterer are created
                makeSearchBar();
                setUpClusterer();

                /**
                 * This listener is to see if the small infoWindow, that pops up above a marker anytime a marker is clicked, is clicked
                 * and if it is clicked then a big Info Window pops up
                 *
                 * It is an asynchronous method therefore other code can run while this lister waits
                 *
                 */
                googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

                    /**
                     * Purpose: When the InfoWindow is clicked, the big Info Window needs to have options between is the user wants
                     * directions from home or home the location they selected
                     *
                     *
                     * This method also starts the chain of polyline events if the user decides they want to go somewhere
                     *
                     * @param marker This is the starting point of the trip marker
                     */

                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        if (marker.getTitle().equals("This is you")) {//If the marker click on is the user location then no big info window needs to be created
                            marker.hideInfoWindow();
                        } else {

                            //Alerts with buttons are used to make the big info window
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                            builder.setMessage(marker.getSnippet())
                                    .setCancelable(true)
                                    .setPositiveButton("Directions From Home", new DialogInterface.OnClickListener() { // Establishes a new button option
                                        /**
                                         * Purpose: When the Directions From Home button is clicked them it will start to calculate the directions
                                         * from the user location to the end point
                                         *
                                         *
                                         * @param dialog
                                         * @param id
                                         */
                                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {

                                            // Find the location from
                                            polyLat = userLat;
                                            polyLong = userLong;
                                            locMark.setTitle("Location Chosen");
                                            locMark.setSnippet("");
                                            calculateDirections(marker, userLat, userLong);// get the directions from home to the marker the user wants to go to
                                            dialog.dismiss();
                                        }
                                    }).setNeutralButton("Website", new DialogInterface.OnClickListener() { // Allows for the website button to be made
                                public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                    dialog.dismiss();
                                }
                            })
                                    /**
                                     * Purpose: When the Directions From Selected button is clicked them it will start to calculate the directions
                                     * from the location the user selected from the search bar to the end point
                                     *
                                     * If the user did not select anything from the search bar and there is no selected location then the selected
                                     * location defaults to the home location
                                     *
                                     * @param dialog
                                     * @param id
                                     */
                                    .setNegativeButton("Directions From Selected", new DialogInterface.OnClickListener() {
                                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                            if (locMark.getPosition().latitude == 41.2033 && locMark.getPosition().longitude == 77.1945) { // if there is not a selected location then the selected location is set to home
                                                polyLat = userLat;
                                                polyLong = userLong;
                                                locMark.setTitle("Location Chosen");
                                                locMark.setSnippet("");
                                                calculateDirections(marker, userLat, userLong);// get the directions from home to the marker the user wants to go to
                                            } else {//user actually selected a location
                                                userMarker.setTitle("This is you");
                                                userMarker.setSnippet("");
                                                polyLat = locMark.getPosition().latitude;
                                                polyLong = locMark.getPosition().longitude;
                                                calculateDirections(marker, polyLat, polyLong);// get the directions from selected user location to the marker the user wants to go to

                                            }
                                            dialog.cancel();
                                        }
                                    });
                            final AlertDialog alert = builder.create();
                            alert.show();//build the alert
                        }
                    }
                });

                //check for last minute permissions
                if (ActivityCompat.checkSelfPermission(AppContext.getAppContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(AppContext.getAppContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }




            }
        });
        return rootView;
    }

    /**
     * If the map were to ever freeze, this method will allow for it to run after a freeze
     */
    @Override
    public void onResume() {
        super.onResume();

        mMapView.onResume();

    }
    /**
     * If the map were to need to pause, this method will allow for it to pause
     */
    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    /**
     * If the map were to need to destroy a view element, this method would be called
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }
    /**
     * If the device were to have low memory, this method would be called and it would make sure things are running as smooth as
     * possible for the low memory state
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}