package com.comp.veta.ui.dashboard;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.comp.veta.AppContext;
import com.comp.veta.Background.Group;
import com.comp.veta.Background.User;
import com.comp.veta.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
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
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Class for the fragment which houses the groups section
 * Used to manage the UI and logic of that fragment
 */
public class DashboardFragment extends Fragment {

    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    DocumentReference userRef;

    View root;
    LinearLayout list;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;


    StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    View createPopup;
    ImageView groupImage;


    ActivityResultLauncher<String> mGetPhotoContent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (user != null) {
            userRef = db.collection("users").document(user.getUid());
        }

        createPopup = getLayoutInflater().inflate(R.layout.create_popup, null, false);
        groupImage = createPopup.findViewById(R.id.groupImage);
        mGetPhotoContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) {
                            groupImage.setImageURI(uri);

                        }
                    }
                });

    }


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


        return root;
    }


    public void makeGroupList() {

        if(user!= null) {
            userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                    User vUser = documentSnapshot.toObject(User.class);
                    if (vUser != null && vUser.getGroupIDs() != null) {
                        ArrayList<String> groupArray = vUser.getGroupIDs();


                        for (int inx = 0; inx < groupArray.size(); inx++) { // goes through each group the user is in

                            View view = LayoutInflater.from(AppContext.getAppContext()).inflate(R.layout.group_temp, null); //inflates the group_temp layout into a view
                            TextView textTest = view.findViewById(R.id.notify_group_name); // Links UI elements to objects
                            TextView sideNote = view.findViewById(R.id.notif_text);
                            ImageView groupPreImage = view.findViewById(R.id.groupPreImage);

                            DocumentReference tempGroupRef = db.collection("groups").document(groupArray.get(inx));
                            int listIndex = inx;
                            tempGroupRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {

                                    Group tempGroup = documentSnapshot.toObject(Group.class);

                                    if (tempGroup != null) {
                                        Picasso.get().load(tempGroup.getPhotoURL())
                                                .centerCrop()
                                                .resize(150, 150)
                                                .into(groupPreImage);
                                        textTest.setText(tempGroup.getName());
                                        sideNote.setText(tempGroup.getNumPeople() + " People");

                                    }

                                }
                            });
                            list.addView(view, listIndex);

                    /*
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

                     */


                        }
                    }


                }
            });
        }

    }




                        /*

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

                         */


    @SuppressLint("ClickableViewAccessibility")
    public void createNewGroupUI() {


        dialogBuilder = new AlertDialog.Builder(getContext());
        EditText enterName = (EditText) createPopup.findViewById(R.id.groupNameText);
        EditText enterPassword = (EditText) createPopup.findViewById(R.id.groupPassword);
        Button closeDialog = (Button) createPopup.findViewById(R.id.cancelCreateButton);
        Button createGroup = (Button) createPopup.findViewById(R.id.createButton);
        SwitchCompat passwordSwitch = createPopup.findViewById(R.id.passwordSwitch);

        groupImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_add));


        if (createPopup.getParent() != null) {
            ((ViewGroup) createPopup.getParent()).removeView(createPopup);
        }
        dialogBuilder.setView(createPopup);
        dialog = dialogBuilder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        enterPassword.setVisibility(View.INVISIBLE);
        passwordSwitch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return event.getActionMasked() == MotionEvent.ACTION_MOVE;
            }
        });
        passwordSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (passwordSwitch.isChecked()) {
                    enterPassword.setVisibility(View.VISIBLE);
                } else
                    enterPassword.setVisibility(View.INVISIBLE);
                enterPassword.setText("");

            }
        });

        groupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGetPhotoContent.launch("image/*");
            }
        });


        closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            } // close the create group popup
        });


        createGroup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String name = enterName.getText().toString();
                String password = enterPassword.getText().toString();
                Boolean hasPassword = passwordSwitch.isChecked();

                if (!name.equals("") && (!hasPassword || !password.equals(""))) {
                    ProgressDialog progress = new ProgressDialog(getActivity());
                    progress.setTitle("Loading");
                    progress.setMessage("Please wait while the group is made");
                    progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                    progress.show();

                    Group group = new Group(name, hasPassword, password);
                    group.addPerson(user.getUid());

                    db.collection("groups").add(group).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {

                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            String groupID = documentReference.getId();
                            userRef.update("groupIDs", FieldValue.arrayUnion(groupID));

                            StorageReference groupRef = storageRef.child("groups/" + groupID + "/groupImage.png");

                            Bitmap bitmap;
                            if (groupImage.getDrawable() instanceof VectorDrawable) {
                                bitmap = BitmapFactory.decodeResource(getActivity().getResources(),
                                        R.drawable.logo);

                            }else{
                                bitmap = ((BitmapDrawable) groupImage.getDrawable()).getBitmap();
                            }
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                                byte[] data = baos.toByteArray();

                            UploadTask uploadTask = groupRef.putBytes(data);
                            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                    if (!task.isSuccessful()) {
                                        throw task.getException();
                                    }

                                    // Continue with the task to get the download URL
                                    return groupRef.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        Uri downloadUri = task.getResult();
                                        db.collection("groups").document(groupID).update("photoURL", "" + downloadUri);
                                        list.removeAllViews();
                                        makeGroupList(); //refresh
                                        progress.dismiss();

                                    } else {
                                        Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                            dialog.dismiss(); //closes the create group popup
                        }
                    });



                }

            }

        });


    }


    /*
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

     */


}