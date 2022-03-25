package com.comp.veta.ui.dashboard;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.comp.veta.Background.Announcement;
import com.comp.veta.Background.Group;
import com.comp.veta.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * This fragment/class manages all of the components to the message/announcement screen
 *
 */
public class MessagesFragment extends Fragment {




    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    DocumentReference userRef;
    DocumentReference groupRef;

    View root;
    View makeAnnPopup;
    LinearLayout messageList;
    ScrollView scrollView;
    private AlertDialog.Builder dialogBuilder ;
    private BottomSheetDialog bottomDialog;
    private AlertDialog dialog;



    private String groupCode;



    /**
     * This is the first thing that runs when this fragment is initialized
     * It sets up some variables like the user and group that was passed in by the dashboard fragment
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialogBuilder = new AlertDialog.Builder(getContext());
        bottomDialog = new BottomSheetDialog(getContext(), R.style.BottomSheetDialog);
        makeAnnPopup = getLayoutInflater().inflate(R.layout.popup_make_announcement, null, false);

        if (user != null) {
            userRef = db.collection("users").document(user.getUid());
        }
        if (getArguments()!=null)
       groupRef = db.collection("groups").document(getArguments().getString("groupID"));








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

        root = inflater.inflate(R.layout.fragment_messages, container, false);
        messageList = root.findViewById(R.id.messages_list);
        ImageView groupImage= root.findViewById(R.id.groupImageMessages);
        TextView groupNameDisplay = root.findViewById(R.id.groupNameMessages);
        scrollView = root.findViewById(R.id.annScrollView);





       TextView makeAnnButton = root.findViewById((R.id.makeAnnButton));

        makeAnnButton.setOnClickListener(v -> {

            makeAnnUI();

        });

        View backButton = root.findViewById(R.id.backToDashButton);

        backButton.setOnClickListener(v -> {

            Navigation.findNavController(v).navigate(R.id.action_navigation_messages_to_navigation_dashboard);
        });

        View openEventsButton = root.findViewById(R.id.openEventButton);

        openEventsButton.setOnClickListener(v -> {

                Bundle bundle = new Bundle();
                bundle.putString("groupID", groupCode);
                Navigation.findNavController(v).navigate(R.id.action_navigation_messages_to_navigation_eventView, bundle);


        });

        AppCompatImageView settingsButton = root.findViewById(R.id.groupSettingsButton);

        settingsButton.setOnClickListener(v -> {
            makeSettingsDialog();
        });




        groupRef.get().addOnSuccessListener(documentSnapshot -> {

            Group group = documentSnapshot.toObject(Group.class);

            groupCode = group.getGroupID();

            if (group != null) {
                Picasso.get().load(group.getPhotoURL())
                        .centerCrop()
                       .resize(150, 150)
                        .into(groupImage);
                groupNameDisplay.setText(group.getName());


            }

        });






        makeAnnouncementList();


        return root;
    }

    /**
     * Inflates the UI responsible for creating a new announcement
     */
    public void makeAnnUI(){
        EditText messageText = (EditText) makeAnnPopup.findViewById(R.id.annEditText);
        TextView closeDialogButton =  makeAnnPopup.findViewById(R.id.cancelMakeAnnButton);
        Button createAnnButton = (Button) makeAnnPopup.findViewById(R.id.createAnnButton);



        closeDialogButton.setOnClickListener(v ->{
            messageText.setText("");
            bottomDialog.dismiss();
        });

        createAnnButton.setOnClickListener(v ->{
            String text = messageText.getText().toString();
            if(!text.equals("")) {
                Announcement announcement = new Announcement(text, user.getDisplayName());
                groupRef.update("announcements", FieldValue.arrayUnion(announcement));
                messageList.removeAllViews();
                makeAnnouncementList();
                messageText.setText("");
                bottomDialog.dismiss();

            }
        });

        if (makeAnnPopup.getParent() != null) {
            ((ViewGroup) makeAnnPopup.getParent()).removeView(makeAnnPopup);
        }

        bottomDialog.setContentView((makeAnnPopup));
        bottomDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        bottomDialog.getWindow().getAttributes().windowAnimations = R.style.BottomDialogAnimation;
        bottomDialog.show();



    }

    /**
     * Functions a lot like the makeEventsList where it takes all the announcements in a group and puts them in a list
     */
    public void makeAnnouncementList(){



        groupRef.get().addOnSuccessListener(documentSnapshot -> {

            Group group = documentSnapshot.toObject(Group.class);

            if (group != null) {
                ArrayList<Announcement> announcements = group.getAnnouncements();

                for (int inx = 0; inx < announcements.size(); inx++) {

                    View view = getLayoutInflater().inflate(R.layout.temp_announcement, null);
                    TextView senderText = view.findViewById(R.id.ann_sender); // Links UI elements to objects
                    TextView messageText = view.findViewById(R.id.ann_text);
                    TextView timeText = view.findViewById(R.id.ann_time);

                    senderText.setText(announcements.get(inx).getSender());
                    messageText.setText(announcements.get(inx).getMessage());
                    timeText.setText(DateFormat.format("MM/dd/yyyy (hh:mm aa)",
                            announcements.get(inx).getTime()));


                    messageList.addView(view, inx);


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


    /**
     * A method that will make the user leave the group they are currently viewing
     * This is called through the settings of a certain group
     * If there are no people left in the group, the group is deleted
     */
    public void leaveGroup(){

        ProgressDialog progress = new ProgressDialog(getActivity());
        progress.setTitle("Loading");
        progress.setMessage("You are being removed from this group");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();

        groupRef.get().addOnSuccessListener(documentSnapshot -> {

            Group group = documentSnapshot.toObject(Group.class);

            if (group != null) {
                userRef.update("groupIDs", FieldValue.arrayRemove(group.getGroupID()));

                if(group.getNumPeople()<=1){
                    groupRef.delete();
                }
                else {
                    groupRef.update("numPeople", FieldValue.increment(-1));

                }
                Toast t =  Toast.makeText(getActivity(),"You left "+ group.getName(),Toast.LENGTH_LONG);
                t.setGravity(Gravity.TOP,0,0);
                t.show();


                Navigation.findNavController(root).navigate(R.id.action_navigation_messages_to_navigation_dashboard);
                progress.dismiss();

            }

        });

    }

    /**
     * Inflates the UI responsible for the settings of a group
     * This includes the share code and ways to leave the group
     * If you are the owner of the group, you have the ability to delete the group entirely
     *
     */
    public void makeSettingsDialog(){

        View groupSettings =  getLayoutInflater().inflate(R.layout.sheet_group_settings, null, false);

        TextView code = groupSettings.findViewById(R.id.groupJoinCodeText);
        code.setText(groupCode);

        AppCompatImageView copyButton = groupSettings.findViewById(R.id.copyCodeText);

        copyButton.setOnClickListener(v ->{
            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("GroupIDCode", groupCode);
            clipboard.setPrimaryClip(clip);

            Toast t =  Toast.makeText(getActivity(),"Copied!",Toast.LENGTH_LONG);
           t.setGravity(Gravity.TOP,0,0);
           t.show();
        });

        AppCompatImageView closeButton = groupSettings.findViewById(R.id.closeGroupSettingsButton);

        closeButton.setOnClickListener(v ->{
            bottomDialog.dismiss();
        });

        AppCompatButton leaveGroupButton = groupSettings.findViewById(R.id.leaveGroupButton);

        leaveGroupButton.setOnClickListener(v -> {
            leaveGroup();
            bottomDialog.dismiss();
        });

         AppCompatButton deleteGroupButton = groupSettings.findViewById(R.id.deleteGroupButton);

        deleteGroupButton.setOnClickListener(v -> deleteGroup());


        bottomDialog.setContentView((groupSettings));


        bottomDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        bottomDialog.getWindow().getAttributes().windowAnimations = R.style.BottomDialogAnimation;
       // bottomDialog.setCanceledOnTouchOutside(false);
        bottomDialog.show();




    }


    /**
     * Can only be run if the owner of the group chooses to delete the group
     */
    public void deleteGroup(){


        groupRef.get().addOnSuccessListener(documentSnapshot -> {

            Group group = documentSnapshot.toObject(Group.class);

            if (group != null) {
                if(group.getCreatorID().equals(user.getUid())){
                    ProgressDialog progress = new ProgressDialog(getActivity());
                    progress.setTitle("Loading");
                    progress.setMessage("Deleting this group forever");
                    progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                    progress.show();
                    groupRef.delete();
                    Navigation.findNavController(root).navigate(R.id.action_navigation_messages_to_navigation_dashboard);
                    Toast t =  Toast.makeText(getActivity(),"The group has been deleted",Toast.LENGTH_LONG);
                    t.setGravity(Gravity.TOP,0,0);
                    t.show();
                    progress.dismiss();


                }

                else{
                    Toast t =  Toast.makeText(getActivity(),"You must be the owner of the group to delete it",Toast.LENGTH_LONG);
                    t.setGravity(Gravity.TOP,0,0);
                    t.show();
                }


            }

        });
    }









}