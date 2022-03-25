package com.comp.veta.ui.dashboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.comp.veta.Background.Announcement;
import com.comp.veta.Background.Event;
import com.comp.veta.Background.Group;
import com.comp.veta.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.type.DateTime;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * This fragment/class manages the Group Events screen
 * It allows for the creation of events and also displays all the events in a nice list
 */

public class GroupEventFragment extends Fragment
        implements DatePickerDialog.OnDateSetListener {


    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    DocumentReference userRef;
    DocumentReference groupRef;

    View root;
    View makeAnnPopup;
    LinearLayout eventList;
    ScrollView scrollView;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private BottomSheetDialog bottomDialog;


    private String groupCode;

    private String addressName;
    private LatLng latLng;
    private String date;
    private String time;

    private Boolean userCanMakeEvents = false;

    Button eventSetLocationButton;
    Button eventSetDateButton;


    SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm");               //set time text
    SimpleDateFormat _12HourSDF = new SimpleDateFormat("hh:mm a");


    /**
     * This is the first thing that runs when this fragment is initialized
     * It sets up some variables like the user and group that was passed in by the messages fragment
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        dialogBuilder = new AlertDialog.Builder(getContext());
        bottomDialog = new BottomSheetDialog(getContext(), R.style.BottomSheetDialog);

        if (user != null) {
            userRef = db.collection("users").document(user.getUid());
        }
        if (getArguments() != null)
            groupRef = db.collection("groups").document(getArguments().getString("groupID"));

        if (groupRef != null) {
            groupRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Group tempGroup = documentSnapshot.toObject(Group.class);

                    if (tempGroup.isAllowAllAccess() || tempGroup.getCreatorID().equals(user.getUid())) {
                        userCanMakeEvents = true;
                    }


                }
            });
        }

    }


    /**
     * Runs after the onCreate to inflate the view on the screen
     * This sets up all the on screen UI components
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_group_events, container, false);
        eventList = root.findViewById(R.id.event_list);
        ImageView groupImage = root.findViewById(R.id.groupImageMessages);
        scrollView = root.findViewById(R.id.eventScrollView);


        View makeEventButton = root.findViewById(R.id.makeEventButton);

        makeEventButton.setOnClickListener(v -> {
            if(userCanMakeEvents) {
                makeEventUI();
            }
            else{
                Toast t =  Toast.makeText(getActivity(),"You do not have access to make an Event",Toast.LENGTH_LONG);
                t.setGravity(Gravity.TOP,0,0);
                t.show();

            }


        });


        View backButton = root.findViewById(R.id.backToMessagesButton);

        backButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("groupID", groupCode);
            Navigation.findNavController(v).navigate(R.id.action_navigation_eventView_to_navigation_messages, bundle);
        });


        groupRef.get().addOnSuccessListener(documentSnapshot -> {

            Group group = documentSnapshot.toObject(Group.class);

            groupCode = group.getGroupID();

            if (group != null) {
                Picasso.get().load(group.getPhotoURL())
                        .centerCrop()
                        .resize(150, 150)
                        .into(groupImage);


            }

        });

        makeGroupEventsList();
        return root;
    }


    /**
     * This method takes all the events within a group and adds them all to a list
     * Each item in the list (a single event) has it's own set of UI components
     */
    public void makeGroupEventsList() {


        groupRef.get().addOnSuccessListener(documentSnapshot -> {

            Group group = documentSnapshot.toObject(Group.class);

            if (group != null) {
                ArrayList<Event> events = group.getEvents();


                for (int inx = 0; inx < events.size(); inx++) {

                    Event event = events.get(inx);

                    View view = getLayoutInflater().inflate(R.layout.temp_event, null);
                    TextView nameText = view.findViewById(R.id.event_name); // Links UI elements to objects
                    TextView locationText = view.findViewById(R.id.event_location);
                    TextView timeText = view.findViewById(R.id.event_time);

                    TextView deleteEvent = view.findViewById(R.id.removeAnEvent);
                    if (group.getCreatorID().equals(user.getUid()) || event.getEventCreator().equals(user.getUid())){
                        deleteEvent.setVisibility(View.VISIBLE);
                        deleteEvent.setOnClickListener(v -> {
                            eventList.removeView(view);
                            groupRef.update("events", FieldValue.arrayRemove(event));
                            Toast t = Toast.makeText(getActivity(), "The event " +event.getEventName()+" was removed", Toast.LENGTH_LONG);
                            t.setGravity(Gravity.TOP, 0, 0);
                            t.show();

                        });
                    }
                    else
                        deleteEvent.setVisibility(View.INVISIBLE);


                    nameText.setText(event.getEventName());
                    locationText.setText(event.getEventStringLocation());

                    Date dt = event.getEventTime();
                    SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yy 'at' hh:mm a");
                    timeText.setText(dateFormatter.format(dt));


                    eventList.addView(view, inx);


                }



            }

        });


    }


    /**
     * This method create the bottom pop-up that allows for the creation of an event
     */
    public void makeEventUI() {

        View eventCreation = getLayoutInflater().inflate(R.layout.sheet_make_event, null, false);

        EditText eventNameET = eventCreation.findViewById(R.id.eventNameEditText);
        EditText eventDesET = eventCreation.findViewById(R.id.eventDescriptionEditText);


        eventSetLocationButton = eventCreation.findViewById(R.id.eventSetLocation);             //set Location
        eventSetLocationButton.setOnClickListener(v -> {

            // Set the fields to specify which types of place data to
            // return after the user has made a selection.
            List<Place.Field> fields = Arrays.asList(Place.Field.LAT_LNG, Place.Field.ADDRESS);

            // Start the autocomplete intent.
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .build(getActivity());

            locationSearchActivityResult.launch(intent);


        });

        eventSetDateButton = eventCreation.findViewById(R.id.eventEnterDate);                                       //Set Date
        eventSetDateButton.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this,
                    Calendar.getInstance().get(Calendar.YEAR),
                    Calendar.getInstance().get(Calendar.MONTH),
                    Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });


        Button eventSetTimeButton = eventCreation.findViewById(R.id.eventEnterTime);                                                    //set Time
        eventSetTimeButton.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                    time = selectedHour + ":" + selectedMinute;

                    try {

                        Date _24HourDt = _24HourSDF.parse(time);
                        eventSetTimeButton.setText(_12HourSDF.format(_24HourDt));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }


                }
            }, Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), false);
            timePickerDialog.show();
            eventSetTimeButton.setTextColor(getResources().getColor(R.color.goodGreen));
        });

        TextView cancelEventButton = eventCreation.findViewById(R.id.eventCancelCreateButton);                              //cancel
        cancelEventButton.setOnClickListener(v -> {
            bottomDialog.dismiss();
        });


        Button createEventButton = eventCreation.findViewById(R.id.eventCreateButton);                                  //create
        createEventButton.setOnClickListener(v -> {

            String eventName = eventNameET.getText().toString();
            String eventDes = eventDesET.getText().toString();
            if (!eventName.equals("") && !eventDes.equals("") && addressName != null && time != null && date != null) {


                SimpleDateFormat dateParser = new SimpleDateFormat("MM/dd/yyyy hh:mm");
                {
                    try {

                        Date dateTime = dateParser.parse(date + " " + time);


                        //  SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yy");
                        //DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm a");

                        Event event = new Event(eventName, eventDes, addressName, user.getUid(), latLng.longitude, latLng.latitude, dateTime);
                        groupRef.update("events", FieldValue.arrayUnion(event));
                        groupRef.get().addOnSuccessListener(documentSnapshot -> {

                            Group group = documentSnapshot.toObject(Group.class);

                            if (group != null) {
                                ArrayList<Event> events = group.getEvents();
                                Collections.sort(events);
                                groupRef.update("events", events);
                            }
                            eventList.removeAllViews();
                            makeGroupEventsList();

                            bottomDialog.dismiss();

                        });


                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }


            } else {
                Toast t = Toast.makeText(getActivity(), "Make sure everything is filled in!", Toast.LENGTH_LONG);
                t.setGravity(Gravity.TOP, 0, 0);
                t.show();
            }


        });

        bottomDialog.setContentView((eventCreation));
        bottomDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        bottomDialog.getWindow().getAttributes().windowAnimations = R.style.BottomDialogAnimation;
        bottomDialog.setCancelable(true);
        FrameLayout bottomSheet = (FrameLayout) bottomDialog.findViewById(R.id.design_bottom_sheet);
        BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomDialog.show();


    }


    /**
     * This is an Activity that waits for a result, and when that result is gotten it will run this code with that information
     * Basically, this waits for a location to be picked by the user from the Google Places API, and then the API gives that information so it can be stored in the event
     */
    ActivityResultLauncher<Intent> locationSearchActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {

                @Override
                public void onActivityResult(ActivityResult result) {
                    Intent data = result.getData();
                    if (data != null) {
                        if (result.getResultCode() == AutocompleteActivity.RESULT_OK) {                                         //After found location
                            Place place = Autocomplete.getPlaceFromIntent(data);
                            addressName = place.getAddress();
                            latLng = place.getLatLng();
                            eventSetLocationButton.setText(addressName);
                            Log.i("Places", "Place: " + place.getAddress() + ", " + place.getLatLng());

                        } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR) {

                            Status status = Autocomplete.getStatusFromIntent(data);
                            Log.i("Places", status.getStatusMessage());

                        } else if (result.getResultCode() == AutocompleteActivity.RESULT_CANCELED) {

                        }
                    }


                }


            });


    /**
     * This will show a confirmation to the user when they put in a correct time while creating an event
     * @param datePicker
     * @param year
     * @param month
     * @param day
     */
    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        date = (month + 1) + "/" + day + "/" + year;
        eventSetDateButton.setText(date);
        eventSetDateButton.setTextColor(getResources().getColor(R.color.goodGreen));

    }
}