package com.comp.veta.ui.events;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.comp.veta.Background.Event;
import com.comp.veta.Background.Group;
import com.comp.veta.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.JsonObject;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * This fragment manages all of the events tab at the bottom of the screen
 */
public class EventsFragment extends Fragment {



    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    DocumentReference userRef;
    DocumentReference groupRef;



    View root;
    LinearLayout eventList;
    ScrollView scrollView;
    private AlertDialog.Builder dialogBuilder ;




    /**
     * This is the first thing that runs when this fragment is initialized
     * It sets up some variables like the user
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialogBuilder = new AlertDialog.Builder(getContext());


        if (user != null) {
            userRef = db.collection("users").document(user.getUid());
        }







    }

    /**
     * Runs after the onCreate to inflate the view on the screen
     *  This sets up all the on screen UI components
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_events, container, false);
        eventList = root.findViewById(R.id.event_list);
        scrollView = root.findViewById(R.id.eventScrollView);





        makeEventsList();




        return root;
    }


    /**
     * This is much like all the other methods used to generate a list on screen
     * This time it will get all the events from every group the user is in and display them all in the list
     */
    public void makeEventsList() {
        ArrayList<Event> events = new ArrayList();

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

                            for (int inx = 0; inx < events.size(); inx++) {

                                View view = getLayoutInflater().inflate(R.layout.temp_event, null);
                                TextView nameText = view.findViewById(R.id.event_name); // Links UI elements to objects
                                TextView locationText = view.findViewById(R.id.event_location);
                                TextView timeText = view.findViewById(R.id.event_time);

                                TextView deleteEvent = view.findViewById(R.id.removeAnEvent);
                                deleteEvent.setVisibility(View.INVISIBLE);

                                nameText.setText(events.get(inx).getEventName());
                                locationText.setText(events.get(inx).getEventStringLocation());

                                Date dt = events.get(inx).getEventTime();
                                timeText.setText(dateFormatter.format(dt));

                                eventList.addView(view, inx);


                            }
                        }


                    }
                });



                /*
                scrollView.post(new Runnable() {
                    public void run() {
                        scrollView.setSmoothScrollingEnabled(false);
                        scrollView.fullScroll(View.FOCUS_DOWN);
                        scrollView.setSmoothScrollingEnabled(true);

                    }
                });

                 */




    }
















}