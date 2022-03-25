package com.comp.veta.ui.dashboard;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.VectorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.comp.veta.Background.Group;
import com.comp.veta.Background.User;
import com.comp.veta.R;
import com.comp.veta.ui.profile.ProfileFragment;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Class for the fragment which houses the groups section
 * Used to manage the UI and logic of that fragment
 */
public class DashboardFragment extends Fragment {


    static FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    DocumentReference userRef;

    View root;
    LinearLayout list;

    private AlertDialog dialog;
    private BottomSheetDialog bottomDialog;



    StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    View createPopup;
    View joinPopup;

    ImageView groupImage;
    ImageView userImage;


    ActivityResultLauncher<String> mGetPhotoContent;

    /**
     * Run on the fragment creation
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (user != null) {
            userRef = db.collection("users").document(user.getUid());
        }


        bottomDialog = new BottomSheetDialog(getContext(), R.style.BottomSheetDialog);
        createPopup = getLayoutInflater().inflate(R.layout.popup_create_group, null, false);
        joinPopup = getLayoutInflater().inflate(R.layout.popup_join_group, null, false);

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


    /**
     * Runs after onCreate, which sets up the view
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        View profileSettings =  getLayoutInflater().inflate(R.layout.sheet_profile, null, false);

        root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        list = root.findViewById(R.id.group_list);


        makeGroupList();


        userImage = root.findViewById(R.id.openProfileButton);

        if(user!=null) {
            Picasso.get().load(user.getPhotoUrl())
                    .centerCrop()
                    .resize(55, 55)
                    .placeholder(R.drawable.logo)
                    .into(userImage);


        }

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ProfileFragment profileFragment = new ProfileFragment();
                profileFragment.show(getActivity().getSupportFragmentManager(),profileFragment.getTag());


            }
        });

        AppCompatImageView JoinButton = root.findViewById(R.id.addAGroupButton);
        JoinButton.setOnClickListener(view -> makeJoinNewGroupUI());


        return root;
    }


    /**
     * Makes a list of views which represent each group the user is in
     */
    public void makeGroupList() {

        if(user!= null) {
            userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                    User vUser = documentSnapshot.toObject(User.class);
                    if (vUser != null && vUser.getGroupIDs() != null) {
                        ArrayList<String> groupArray = vUser.getGroupIDs();


                        for (int inx = 0; inx < groupArray.size(); inx++) { // goes through each group the user is in

                           View view = getLayoutInflater().inflate(R.layout.temp_group, null);//inflates the group_temp layout into a view
                            TextView textTest = view.findViewById(R.id.notify_group_name); // Links UI elements to objects
                            TextView sideNote = view.findViewById(R.id.notif_text);
                            ImageView groupPreImage = view.findViewById(R.id.groupPreImage);

                            int tempPos = inx;


                            DocumentReference tempGroupRef = db.collection("groups").document(groupArray.get(inx));
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

                                        view.setTag(R.string.GROUP_ID,groupArray.get(tempPos));
                                        // adds an on click listener for each group view in the list
                                        view.setOnClickListener(v -> {
                                            String gID = (String) v.getTag(R.string.GROUP_ID);
                                            Bundle bundle = new Bundle();
                                            bundle.putString("groupID", gID);
                                            Navigation.findNavController(v).navigate(R.id.action_navigation_dashboard_to_navigation_messages, bundle);


                                        });
                                        list.addView(view, tempPos);


                                    }
                                    else{
                                        userRef.update("groupIDs", FieldValue.arrayRemove(groupArray.get(tempPos)));

                                    }

                                }
                            });



                        }
                    }


                }
            });
        }

    }


    /**
     * Makes the UI pop-up which allows the user to join or create a group
     */
    AtomicBoolean onPasswordCheck = new AtomicBoolean(false);
    public void makeJoinNewGroupUI(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        EditText enterCode = (EditText) joinPopup.findViewById(R.id.groupCodeEditText);
        Button closeDialogButton = (Button) joinPopup.findViewById(R.id.cancelJoinGroupButton);
        Button joinGroupButton = (Button) joinPopup.findViewById(R.id.joinButton);
        TextView title = (TextView) joinPopup.findViewById(R.id.joinGroupTitle);
        Button createButton = joinPopup.findViewById(R.id.makeAGroupButton);

        createButton.setOnClickListener(v -> makeCreateNewGroupUI());

        enterCode.setText("");
        enterCode.setHint("Unique Group Code");
        title.setText("Enter a Group Code");

        if (joinPopup.getParent() != null) {
            ((ViewGroup) joinPopup.getParent()).removeView(joinPopup);
        }
        dialogBuilder.setView(joinPopup);
        dialog = dialogBuilder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();







        AtomicReference<Group> group = new AtomicReference<>();
        joinGroupButton.setOnClickListener(view -> {
            String input = enterCode.getText().toString();
            if (onPasswordCheck.get()) {

                if (group.get() != null){
                    if (input.equals(group.get().getPassword())){
                        joinGroup(group.get().getGroupID());
                        dialog.dismiss();
                        onPasswordCheck.set(false);
                    }
                    else{
                        Toast.makeText(getActivity(), "Wrong Password", Toast.LENGTH_LONG).show();
                        enterCode.setText("");
                    }


                }

            } else {
                if (!input.equals("")) {
                    DocumentReference groupRef = db.collection("groups").document(input);
                    groupRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                group.set(document.toObject(Group.class));

                                if (group.get().isHasPassword()) {
                                    enterCode.setText("");
                                    enterCode.setHint("Enter Password");
                                    title.setText("Enter Password for " + group.get().getName());
                                    onPasswordCheck.set(true);

                                } else {

                                    joinGroup(input);
                                    dialog.dismiss();
                                    onPasswordCheck.set(false);
                                }
                            } else {
                                Toast.makeText(getActivity(), "That group code doesn't exist", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

            }
        });


        closeDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                onPasswordCheck.set(false);
            } // close the create group popup
        });

    }

    /**
     * Joins a group with the given groupID
     * @param groupID
     */
    public void joinGroup(String groupID){

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    User user = document.toObject(User.class);
                    if (user!= null){
                       if ( user.getGroupIDs()==null || !user.getGroupIDs().contains(groupID)){
                           userRef.update("groupIDs", FieldValue.arrayUnion(groupID));
                           DocumentReference groupRef = db.collection("groups").document(groupID);
                           groupRef.update("numPeople", FieldValue.increment(1) );
                           list.removeAllViews();
                           makeGroupList();
                       }
                       else{
                           Toast.makeText(getActivity(), "You are already in this group", Toast.LENGTH_LONG).show();
                       }


                    }



                }
            }
        });






    }


    /**
     * Makes the UI that allows the user to create a new group
     */
    @SuppressLint("ClickableViewAccessibility")
    public void makeCreateNewGroupUI() {
        dialog.dismiss();


        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        EditText enterName = (EditText) createPopup.findViewById(R.id.groupCodeEditText);
        EditText enterPassword = (EditText) createPopup.findViewById(R.id.groupPassword);
        Button closeDialog = (Button) createPopup.findViewById(R.id.cancelCreateGroupButton);
        Button createGroup = (Button) createPopup.findViewById(R.id.createGroupButton);
        SwitchCompat passwordSwitch = createPopup.findViewById(R.id.passwordSwitch);
        SwitchCompat editAccessSwitch = createPopup.findViewById(R.id.allAccessSwitch);

        groupImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_add));


        if (createPopup.getParent() != null) {
            ((ViewGroup) createPopup.getParent()).removeView(createPopup);
        }
        dialogBuilder.setView(createPopup);
        dialog = dialogBuilder.create();
        dialog.setCanceledOnTouchOutside(false);
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

                createNewGroup(enterName.getText().toString(),enterPassword.getText().toString(),passwordSwitch.isChecked(), editAccessSwitch.isChecked() );

            }

        });


    }

    /**
     *Takes a bunch of parameters with are needed to set up a group with those setting
     * This is the method that actually construct and stores the group in the Firebase database
     * @param gName
     * @param gPassword
     * @param gHasPassword
     * @param gAllAccess
     */
    public void createNewGroup(String gName, String gPassword, Boolean gHasPassword, Boolean gAllAccess){
        String name = gName;
        String password = gPassword;
        Boolean hasPassword = gHasPassword;

        if (!name.equals("") && (!hasPassword || !password.equals(""))) {
            ProgressDialog progress = new ProgressDialog(getActivity());
            progress.setTitle("Loading");
            progress.setMessage("Please wait while the group is made");
            progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
            progress.show();

            Group group = new Group(name, hasPassword, password, gAllAccess, user.getUid());
            group.addPerson();

            String strID = getRandomNumberString();



                db.collection("groups").document(strID).set(group).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {

                    db.collection("groups").document(strID).update("groupID", strID);
                    userRef.update("groupIDs", FieldValue.arrayUnion(strID));

                    StorageReference groupRef = storageRef.child("groups/" + strID + "/groupImage.png");

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
                                db.collection("groups").document(strID).update("photoURL", "" + downloadUri);
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


    /**
     * This is just a method that gets a random 8 integer number to act as the ID for that group
     * It also checks to make sure that ID doesn't already exist
     */
    boolean docExists = true;
    public static String getRandomNumberString() {


        ArrayList<String> tempList = new ArrayList<>();

        db.collection("groups").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                tempList.add(document.getId());
                            }


                        } else {
                            //failed
                        }
                    }
                });

        Random rnd = new Random();
        int num = rnd.nextInt(99999999);
        String randID = String.format("%08d", num);

        while(tempList.contains(randID)){
            num = rnd.nextInt(99999999);
            randID = String.format("%08d", num);
        }


        return randID;

    }



}