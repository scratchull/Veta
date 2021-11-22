package com.comp.veta.ui.dashboard;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import com.comp.veta.AppContext;
import com.comp.veta.Background.Group;
import com.comp.veta.Background.Person;
import com.comp.veta.Background.SendMail;
import com.comp.veta.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Class for the fragment which houses the groups section
 * Used to manage the UI and logic of that fragment
 */
public class DashboardFragment extends Fragment {

    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String gString;
    String userID;
    int numGroups;
    View root;
    private Uri mImageUri = null;
    LinearLayout list;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private EditText groupName;
    private EditText groupPassword;
    private Button closeDialog, createGroup;
    Handler handler = new Handler();
    Runnable runnable;
    String meetUpGroup;

    /**
     * On the creation of this page it will load the previous savedInstanceState
     * @param savedInstanceState : the saved instance from previous use
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     *Called when the View is created
     * Fills the view with the fragment_dashboard.xml layout
     * Binds views from layout to objects
     * Sets the onClickListeners for the buttons in the layout
     * @param inflater
     * @param container : the container of which this view was created
     * @param savedInstanceState : the saved instance from previous use
     * @return : returns the root view
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        list = root.findViewById(R.id.group_list);

        makeGroupList();

        AppCompatImageView AddButton = root.findViewById(R.id.makeButton);
        AddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewGroupUI();
            }
        });

        AppCompatImageView NotifyButton = root.findViewById(R.id.alertButton);
        NotifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeNotifyGroupPopup();



            }
        });



        return root;
    }

    /**
     * Creates the popup for the notify groups UI
     * Attaches each button and textview to a corresponding object
     * It will go through every group the user is in and create a list of toggleable views
     * User must input a name for the sickness they are reporting
     * At the end when the user selects the groups to notify then it will add those groups to pickGroups which will be passed to the notifyGroups method
     * This is the self report feature
     */

    public void makeNotifyGroupPopup() {

        dialogBuilder = new AlertDialog.Builder(getContext());
        final View notifyPopup = getLayoutInflater().inflate(R.layout.alert_group_popup, null); // Inflates the alert_group_popup.xml file into a view
        LinearLayout notifyList = notifyPopup.findViewById(R.id.groupNotifyList);  // Makes the LinearLayout in the layout file an object
        ArrayList < String > pickGroups = new ArrayList < > ();
        dialogBuilder.setView(notifyPopup); // sets the view for the dialog builder to  the popup view
        dialog = dialogBuilder.create(); //creates the dialog object
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText text = notifyPopup.findViewById(R.id.sickText); // The user enters the sickness name into this

        Button cancelNotify = (Button) notifyPopup.findViewById(R.id.cancelNotifyButton);
        cancelNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();  //dismisses the popup when clicked

            }
        });
        Button notifyButton = notifyPopup.findViewById(R.id.notifyButton);
        notifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (!text.getText().toString().equals("") && !pickGroups.isEmpty()) { //makes sure sickness name has correct syntax
                    notifyGroups(pickGroups, text.getText().toString()); // notifies the groups in pickGroups when clicked
                    dialog.dismiss();
                }


            }
        });

        FirebaseInstallations.getInstance().getId() // attempt to get unique firebase user ID
                .addOnCompleteListener(new OnCompleteListener < String > () {
                    @Override
                    public void onComplete(@NonNull Task < String > task) {
                        if (task.isSuccessful()) {

                            userID = task.getResult(); // sets the firebase ID to userID. Firebase ID used to identify user
                            DatabaseReference userRef = rootRef.child("people").child(userID);

                            ValueEventListener eventListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) { // runs when data in reference changes
                                    if (dataSnapshot.child("numGroups").getValue() == null) {
                                        userRef.child("numGroups").setValue(0);  //Gives reference a value so not null
                                    }
                                    if (dataSnapshot.child("groupString").getValue() == null) {
                                        userRef.child("groupString").setValue(""); //Gives reference a value so not null
                                    }

                                    gString = dataSnapshot.child("groupString").getValue(String.class);
                                    if (gString == null)
                                        gString = "";//Gives reference a value so not null

                                    if (dataSnapshot.child("numGroups").getValue() != null)
                                        numGroups = dataSnapshot.child("numGroups").getValue(Integer.class);

                                    int base = 0;
                                    for (int x = 0; x < numGroups; x++) { // goes through each group that that user is in

                                        View view = LayoutInflater.from(AppContext.getAppContext()).inflate(R.layout.notify_group_temp, null); // inflates a smaller view into the popup (It goes in the linearlayout)
                                        //links UI elements to their objects
                                        TextView textTest = view.findViewById(R.id.notify_group_name);
                                        LinearLayout layout = view.findViewById(R.id.group);
                                        final Boolean[] isButtonClicked = {
                                                false
                                        };

                                        int i = gString.indexOf(",", base);

                                        String groupName = gString.substring(base, i);

                                        base = i + 1;
                                        textTest.setText(groupName);

                                        view.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) { // sets on click listener. When clicked then the group will be "selected" from the list


                                                isButtonClicked[0] = !isButtonClicked[0]; // toggle the boolean
                                                layout.setBackgroundResource(isButtonClicked[0] ? R.drawable.notify_group_list_bg : R.drawable.notification_border); // changes color when selected

                                                if (isButtonClicked[0]) {
                                                    pickGroups.add(groupName); // adds to pickGroups if selected

                                                } else {
                                                    pickGroups.remove(groupName);
                                                }

                                            }
                                        });


                                        notifyList.addView(view); // adds the group to the list

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {}
                            };

                            userRef.addListenerForSingleValueEvent(eventListener);  // adds the listener created earlier
                        } else {
                            Log.e("Installations", "Unable to get Installation ID");
                        }

                        dialog.show(); // displays the dialog (popup)
                    }

                });

    }

    /**
     * Goes through each selected group that the user chose and sends an in-app notification
     *  Also sends an email to that user if they are signed in
     * @param groupArray : array that hold the groups that need to be notified
     * @param message : the message is the name of the sickness the user chose
     */
    public void notifyGroups(ArrayList < String > groupArray, String message) {

        for (String groupName: groupArray) {  // for each group

            DocumentReference groupRef = db.collection("groups").document(groupName);
            groupRef.get().addOnSuccessListener(new OnSuccessListener < DocumentSnapshot > () {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                    Group group = documentSnapshot.toObject(Group.class);
                    for (String ID: group.getPeopleID()) {  // for each person in that group
                        DocumentReference personRef = db.collection("people").document(ID);

                        if (ID.equals(userID)) { // If the user is the current user using the app (they get a different notification)
                            personRef.update("notifications", FieldValue.arrayUnion("SickBook,Your notification has successfully sent to the group " + group.getName()));

                        } else {

                            personRef.update("notifications", FieldValue.arrayUnion(group.getName() + ",Someone in this group has " + message));
                            personRef.get().addOnSuccessListener(new OnSuccessListener < DocumentSnapshot > () {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    Person person = documentSnapshot.toObject(Person.class); // gets the person reference from firebase and makes it a usable object
                                    sendEmail(person.getEmail(), "SickBook Notification", "Someone in the group " + group.getName() + " has " + message + ". Stay Safe!");


                                }
                            });
                        }
                    }



                }

            });


        }

    }

    /**
     * Creates a SandMail object, which launches an async task
     * We do not have access to an online server so the mail has to be sent through the users device
     * @param email : the email of the recipient
     * @param subject : the subject of the email
     * @param message : the message of the email
     */
    private void sendEmail(String email, String subject, String message) {

        SendMail sm = new SendMail(AppContext.getAppContext(), email, subject, message);
        sm.execute();  //starts async task
    }

    /**
     * Sets up the main list of groups
     * Each group in the list is its own separate view
     * Sets on click listeners for the group views
     * Also sets up the options popup when a group is clicked on
     */
    public void makeGroupList() {
        FirebaseInstallations.getInstance().getId() // attempt to get unique firebase user ID
                .addOnCompleteListener(new OnCompleteListener < String > () {
                    @Override
                    public void onComplete(@NonNull Task < String > task) { // process here is very similar to the one in makeNotifyGroupPopup
                        if (task.isSuccessful()) {

                            userID = task.getResult(); //gets firebase ID, unique to user's device
                            DatabaseReference userRef = rootRef.child("people").child(userID);

                            userRef.child("test").setValue("game");

                            ValueEventListener eventListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.child("numGroups").getValue() == null) {
                                        userRef.child("numGroups").setValue(0); //Gives reference a value so not null
                                    }
                                    if (dataSnapshot.child("groupString").getValue() == null) {
                                        userRef.child("groupString").setValue(""); //Gives reference a value so not null
                                    }

                                    gString = dataSnapshot.child("groupString").getValue(String.class);

                                    if (dataSnapshot.child("numGroups").getValue() != null)
                                        numGroups = dataSnapshot.child("numGroups").getValue(Integer.class);

                                    int base = 0;
                                    for (int x = 0; x < numGroups; x++) { // goes through each group the user is in

                                        View view = LayoutInflater.from(AppContext.getAppContext()).inflate(R.layout.group_temp, null); //inflates the group_temp layout into a view
                                        TextView textTest = view.findViewById(R.id.notify_group_name); // Links UI elements to objects
                                        TextView sideNote = view.findViewById(R.id.notif_text);

                                        view.setOnClickListener(new View.OnClickListener() { // adds an on click listener for each group view in the list
                                            @Override
                                            public void onClick(View v) {
                                                dialogBuilder = new AlertDialog.Builder(getContext()); //makes a popup when clicked
                                                final View optionsPopup = getLayoutInflater().inflate(R.layout.options_popup, null); // inflates the options layout into a view for the popup

                                                Button removeButton = optionsPopup.findViewById(R.id.startSurvey);

                                                removeButton.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) { // removes the group from the list and also the firebase

                                                        String name = textTest.getText().toString();
                                                        if (gString.contains(name + ",")) {
                                                            int i = gString.indexOf(name + ",");
                                                            gString = gString.substring(0, i) + gString.substring(i + name.length() + 1);
                                                            numGroups--;
                                                            userRef.child("groupString").setValue(gString);
                                                            userRef.child("numGroups").setValue(numGroups);
                                                        }

                                                        removeGroup(name);
                                                        dialog.dismiss(); // closes the group options popup
                                                        list.removeAllViews();
                                                        makeGroupList(); //refresh the list
                                                    }
                                                });

                                                Button meetupButton = optionsPopup.findViewById(R.id.startMeetupButton);
                                                meetupButton.setOnClickListener(new View.OnClickListener() {

                                                    @Override
                                                    public void onClick(View v) { // Starts the meet up for that group (includes a survey and mask check)

                                                        dialog.dismiss(); // closes the options popup
                                                        final View meetUpPopup = getLayoutInflater().inflate(R.layout.meetup_popup, null); // inflates the meetup popup into a view

                                                        Button maskCheck = meetUpPopup.findViewById(R.id.startMeetupButton);
                                                        maskCheck.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                meetUpGroup = textTest.getText().toString();;
                                                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                                                startActivityForResult(intent, 0); // starts the picture taking process, will run onActivityResult when finished



                                                            }
                                                        });
                                                        dialogBuilder.setView(meetUpPopup);
                                                        dialog = dialogBuilder.create();
                                                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                                        dialog.show();  // shows the meet up popup

                                                    }
                                                });

                                                dialogBuilder.setView(optionsPopup);
                                                dialog = dialogBuilder.create();
                                                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                                dialog.show(); // shows the group options popup

                                            }
                                        });
                                        int i = gString.indexOf(",", base);
                                        String groupNameInView = gString.substring(base, i);
                                        base = i + 1;
                                        textTest.setText(groupNameInView);

                                        DocumentReference docRef = db.collection("groups").document(groupNameInView);
                                        docRef.get().addOnSuccessListener(new OnSuccessListener < DocumentSnapshot > () {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {

                                                Group group = documentSnapshot.toObject(Group.class);
                                                sideNote.setText("Number Of People In Group: " + group.getNumPeople()); // Lets the usder know how many people are in that group

                                            }
                                        });

                                        list.addView(view); // ats the view to the scrolling view

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {}
                            };

                            userRef.addListenerForSingleValueEvent(eventListener);
                        } else {
                            Log.e("Installations", "Unable to get Installation ID");
                        }
                    }
                });
    }

    /**
     * This method is called when the camera application is done taking a photo
     * The thumbnail of the photo is temporarily uploaded to the firebase
     * The image is used by external python code to determine if a face is wearing a mask
     * The result of that code is them passed into the doSurvey method
     * @param requestCode : needed to know what activity was preformed
     * @param resultCode : also needed to know what activity way preformed
     * @param data : the data from the activity, contains the results
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://sickbook-56291.appspot.com");
        StorageReference imageRef = storageRef.child(userID + ".jpg"); //creates a reference to the image file in the firebase

        if (requestCode == 0 && resultCode == -1) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data"); //gets the bitmap of the image from the camera activity

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] bytes = baos.toByteArray();  // converts to a byte array to send to firebase

            UploadTask uploadTask = imageRef.putBytes(bytes);// creates an upload task to firebase storage
            uploadTask.addOnSuccessListener(new OnSuccessListener < UploadTask.TaskSnapshot > () {   // saves the image to the image reference created earlier
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener < Uri > () { // gets uri of the image in the firebase
                        @Override
                        public void onSuccess(Uri uri) {

                            DatabaseReference userRef = rootRef.child("people").child(userID);
                            Toast.makeText(AppContext.getAppContext(), "Loading...", Toast.LENGTH_LONG).show();
                            dialog.dismiss(); // closes the current popup on the screen (the meet up popup)
                            userRef.child("faceimg").setValue(uri + ""); // sets the value of faceimg to the the uri of the image in the firebase storage
                            userRef.child("new").setValue("true"); // the value of "new" in the firebase is used to track if the python code has finished running for that photo

                            userRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) { // this will run when a value in the userRef from firebase changes
                                    Boolean bool;
                                    bool = Boolean.valueOf(snapshot.child("new").getValue(String.class));

                                    if (!bool) { // if the value of "new" changes to false, the python code is done and this code runs
                                        handler.post(runnable = new Runnable() {
                                            @Override
                                            public void run() {
                                                final View startSurveyPopup = getLayoutInflater().inflate(R.layout.start_survey_popup, null); //inflates the start survey popup into a view

                                                dialogBuilder.setView(startSurveyPopup);
                                                dialog = dialogBuilder.create();
                                                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                                dialog.show(); // shows the start survey popup

                                                Button startSurvey = startSurveyPopup.findViewById(R.id.startSurvey);
                                                startSurvey.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                       dialog.dismiss(); // closes the start survey popup
                                                        boolean maskResult = Boolean.parseBoolean(snapshot.child("MaskOn").getValue(String.class)); // gets the vlaue of  "MaskOn" from the firebase
                                                       doSurvey(maskResult);

                                                    }
                                                });

                                            }
                                        });
                                        userRef.removeEventListener(this);

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }


                            });
                        }
                    });




                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });


        }

    }

    /**
     * Creates a survey popup that checks if the user is at risk of having a disease
     * @param maskResult : The result of the photo mask check, used to determine if a meeting is safe or not
     */
    public void doSurvey(boolean maskResult){
        final View surveyPopup = getLayoutInflater().inflate(R.layout.survey_popup, null); //inflates layout survey popup into  a view

        dialogBuilder.setView(surveyPopup);
        dialog = dialogBuilder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //sets UI elements into objects
         CheckBox cough, fever, headache, dryThroat, chestPain, muscleAches, diarrhea, fatigue;
         Switch question1, question2, question3;
         Button submit;
        cough = surveyPopup.findViewById(R.id.checkBox1);
        fever = surveyPopup.findViewById(R.id.checkBox2);
        headache = surveyPopup.findViewById(R.id.checkBox3);
        dryThroat = surveyPopup.findViewById(R.id.checkBox4);
        chestPain = surveyPopup.findViewById(R.id.checkBox5);
        muscleAches = surveyPopup.findViewById(R.id.checkBox6);
        diarrhea = surveyPopup.findViewById(R.id.checkBox7);
        fatigue = surveyPopup.findViewById(R.id.checkBox8);

        question1 = surveyPopup.findViewById(R.id.switch1);
        question2 = surveyPopup.findViewById(R.id.switch2);
        question3 = surveyPopup.findViewById(R.id.switch3);

        submit = surveyPopup.findViewById(R.id.button);



        // Checks if any of the checkboxes and switches are checked. If any are checked, return true. Else, return false.

            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { // user submits answers to the survey
                    boolean badResult = false;
                    if((cough.isChecked() || fever.isChecked() || headache.isChecked() || dryThroat.isChecked()
                            || chestPain.isChecked() || muscleAches.isChecked() || diarrhea.isChecked() || fatigue.isChecked() || question1.isChecked() || question2.isChecked() || question3.isChecked())) {
                        badResult = true;
                    }
                        if (!maskResult){
                            badResult = true;
                        }

                        DocumentReference docRef = db.collection("groups").document(meetUpGroup);
                    boolean finalBadResult = badResult;
                    docRef.get().addOnSuccessListener(new OnSuccessListener < DocumentSnapshot > () {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    Group group = documentSnapshot.toObject(Group.class);

                                    int numComplete = group.getNumComplete() +1;
                                    int numBad =group.getNumBadComplete();


                                    if(finalBadResult){ //runs if  the person has no mask or if they show symptoms or are at risk
                                        group.setNumBadComplete(group.getNumBadComplete()+1);
                                        DocumentReference personRef = db.collection("people").document(userID);
                                        personRef.update("notifications", FieldValue.arrayUnion("SickBook,You failed the requirements for this meet up. It is recommended that you do not attend. " ));// adds a notification in-app for that person
                                        numBad += 1; //adds one to the number of badResults
                                    }

                                    if (numComplete >= group.getNumPeople()){ // if everyone in the group has completed the meet up check

                                        group.setNumBadComplete(numBad);
                                        group.setNumComplete(0); // sets the number of people who completed the check to 0 for the next time it is used
                                        numComplete = 0;

                                        docRef.set(group);
                                        for (String ID: group.getPeopleID()) {  // for each person in that group
                                            DocumentReference personRef = db.collection("people").document(ID);

                                            personRef.update("notifications", FieldValue.arrayUnion(group.getName()+ ",Everyone in this group has completed the meet up requirements. " + numBad + " failed the requirements." ));// adds a notification in-app for that person

                                            int finalNumBad = numBad;
                                            personRef.get().addOnSuccessListener(new OnSuccessListener < DocumentSnapshot > () {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    Person person = documentSnapshot.toObject(Person.class); // gets the person reference from firebase and makes it a usable object
                                                    sendEmail(person.getEmail(), "SickBook Notification", "Everyone in the group " + group.getName()+" has completed the meet up requirements. " + finalNumBad + " failed the requirements."); //sends an email to user about the results after everyone in the group completed the meet check


                                                }
                                            });

                                        }
                                        group.setNumBadComplete(0);
                                    }
                                    else{
                                        for (String ID: group.getPeopleID()) {  // for each person in that group
                                            DocumentReference personRef = db.collection("people").document(ID);

                                            if (!ID.equals(userID))
                                            personRef.update("notifications", FieldValue.arrayUnion(group.getName()+ ",Someone in this group has completed a meet up check. Go to the group page to complete it." )); // adds a notification in-app for that person

                                        }



                                    }


                                    group.setNumComplete(numComplete);
                                    docRef.set(group); // updates the group reference in firebase to the current group (changes values)

                                }
                            }
                        });


                    dialog.dismiss(); // closes the survey popup


                }
            });



        dialog.show(); // shows the survey popup




    }


    /**
     * Creates the popup for creating or joining a new group
     * User inputs a name and password
     * If that group exist and the password is correct then the user joins the group
     * If the group does not exist then it makes a new group with the entered name and password
     */
    public void createNewGroupUI() {
        DocumentReference personRef = db.collection("people").document(userID);
        personRef.get().addOnSuccessListener(new OnSuccessListener < DocumentSnapshot > () {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (!documentSnapshot.exists()) { // makes a person reference if one is not there already
                    Person person = new Person();
                    personRef.set(person);
                }
            }
        });

        dialogBuilder = new AlertDialog.Builder(getContext());
        final View createPopup = getLayoutInflater().inflate(R.layout.create_popup, null);
        groupName = (EditText) createPopup.findViewById(R.id.sickText);
        groupPassword = (EditText) createPopup.findViewById(R.id.groupPassword);
        closeDialog = (Button) createPopup.findViewById(R.id.cancelCreateButton);
        createGroup = (Button) createPopup.findViewById(R.id.createButton);


        dialogBuilder.setView(createPopup);
        dialog = dialogBuilder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            } // close the create group popup
        });
        createGroup.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                String name = groupName.getText().toString();
                String password = groupPassword.getText().toString();
                DocumentReference docRef = db.collection("groups").document(name);
                docRef.get().addOnSuccessListener(new OnSuccessListener < DocumentSnapshot > () {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String realPassword = password;
                        if (documentSnapshot.exists()) {
                            Group group = documentSnapshot.toObject(Group.class);
                            group.getPassword();
                            realPassword = group.getPassword();
                        }

                        if (!name.equals("") && !name.contains(",") && !gString.contains(name + ",") && password.equals(realPassword)) { //check syntax and password to see if they are correct



                            rootRef.child("people").child(userID).child("groupString").setValue(gString + name + ",");
                            rootRef.child("people/" + userID + "/numGroups").setValue(numGroups + 1);


                            addGroup(name, password);
                            list.removeAllViews();
                            makeGroupList(); //refresh

                            dialog.dismiss(); //closes the create group popup

                        } else if (!password.equals(realPassword)) {
                            Toast toast = Toast.makeText(AppContext.getAppContext(),
                                    "That is not the correct password", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
                            toast.show(); //tells user if password is correct

                        }

                    }


                });

            }
        });
    }


    /**
     * Adds a group to the list in the groups page
     * Also adds the group to firebase
     *  If the group exists then user will join that group
     *  If the group does not exist then a new group will be made with the passoword given
     * @param groupName : the name of the group to create/join
     * @param pass : the password to the created group
     */
    public void addGroup(String groupName, String pass) {

        DocumentReference docRef = db.collection("groups").document(groupName);
        docRef.get().addOnSuccessListener(new OnSuccessListener < DocumentSnapshot > () {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.exists()) { //if group is already made
                    Group group = documentSnapshot.toObject(Group.class);
                    group.addPerson(userID);
                    docRef.set(group);

                } else {

                    Group group = new Group(groupName, pass);
                    group.addPerson(userID);

                    docRef.set(group);
                    Toast toast = Toast.makeText(AppContext.getAppContext(),
                            "You have made a new group", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();


                }
            }

        });

    }

    /**
     * removes a group from the list in the groups page
     * also removes the group from the firebase
     * If no one is left in the group then it removes the group entirely
     * @param groupName : the name of the group to remove for that person
     */
    public void removeGroup(String groupName) {
        DocumentReference docRef = db.collection("groups").document(groupName);
        docRef.get().addOnSuccessListener(new OnSuccessListener < DocumentSnapshot > () {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.exists()) { //checks to see if group exists
                    Group group = documentSnapshot.toObject(Group.class);
                    group.removePerson(userID);
                    docRef.set(group);
                    if (group.getNumPeople() <= 0)
                        docRef.delete();

                }

            }




        });
    }


}