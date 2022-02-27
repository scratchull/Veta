package com.comp.veta.ui.dashboard;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.comp.veta.Background.Announcement;
import com.comp.veta.Background.Event;
import com.comp.veta.Background.Group;
import com.comp.veta.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;


public class GroupEventFragment extends Fragment {




    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    DocumentReference userRef;
    DocumentReference groupRef;

    View root;
    View makeAnnPopup;
    LinearLayout eventList;
    ScrollView scrollView;
    private AlertDialog.Builder dialogBuilder ;
    private AlertDialog dialog;
    private BottomSheetDialog bottomDialog;


    private String groupCode;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialogBuilder = new AlertDialog.Builder(getContext());
        bottomDialog = new BottomSheetDialog(getContext(), R.style.BottomSheetDialog);

        if (user != null) {
            userRef = db.collection("users").document(user.getUid());
        }
        if (getArguments()!=null)
       groupRef = db.collection("groups").document(getArguments().getString("groupID"));






    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_group_events, container, false);
        eventList = root.findViewById(R.id.event_list);
        ImageView groupImage= root.findViewById(R.id.groupImageMessages);
        scrollView = root.findViewById(R.id.eventScrollView);


        View makeEventButton =  root.findViewById(R.id.makeEventButton);

        makeEventButton.setOnClickListener(v -> {
            makeEventUI();

        });



        View backButton = root.findViewById(R.id.backToMessagesButton);

        backButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("groupID", groupCode);
            Navigation.findNavController(v).navigate(R.id.action_navigation_eventView_to_navigation_messages,bundle);
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






        return root;
    }


    public void makeGroupEventsList(){



        groupRef.get().addOnSuccessListener(documentSnapshot -> {

            Group group = documentSnapshot.toObject(Group.class);

            if (group != null) {
                ArrayList<Event> events = group.getEvents();

                for (int inx = 0; inx < events.size(); inx++) {

                    View view = getLayoutInflater().inflate(R.layout.temp_event, null);
                    TextView nameText = view.findViewById(R.id.event_name); // Links UI elements to objects
                    TextView locationText = view.findViewById(R.id.event_location);
                    TextView timeText = view.findViewById(R.id.event_time);

                    nameText.setText(events.get(inx).getEventName());
                    locationText.setText(events.get(inx).getEventStringLocation());
                    timeText.setText(DateFormat.format("MM/dd/yyyy (hh:mm aa)",
                            events.get(inx).getEventTime()));


                    eventList.addView(view, inx);


                }
                scrollView.post(new Runnable() {
                    public void run() {
                        scrollView.setSmoothScrollingEnabled(false);
                        scrollView.fullScroll(View.FOCUS_DOWN);
                        scrollView.setSmoothScrollingEnabled(true);

                    }
                });




            }

        });




    }



    public void  makeEventUI(){

        View eventCreation =  getLayoutInflater().inflate(R.layout.sheet_make_event, null, false);

        EditText eventNameET = eventCreation.findViewById(R.id.eventNameEditText);
        EditText eventDesET = eventCreation.findViewById(R.id.eventDescriptionEditText);
        Button eventSetLocationButton = eventCreation.findViewById(R.id.eventSetLocation);
        Button eventSetDateButton = eventCreation.findViewById(R.id.eventEnterDate);
        Button eventSetTimeButton = eventCreation.findViewById(R.id.eventEnterTime);

        TextView cancelEventButton = eventCreation.findViewById(R.id.eventCancelCreateButton);
        cancelEventButton.setOnClickListener(v -> {
            bottomDialog.dismiss();
        });


        Button createEventButton = eventCreation.findViewById(R.id.eventCreateButton);
        createEventButton.setOnClickListener(v ->{

        });



        bottomDialog.setContentView((eventCreation));
        bottomDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        bottomDialog.getWindow().getAttributes().windowAnimations = R.style.BottomDialogAnimation;
        bottomDialog.show();


    }
















}