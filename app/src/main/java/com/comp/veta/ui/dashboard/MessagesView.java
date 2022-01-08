package com.comp.veta.ui.dashboard;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.comp.veta.Background.Group;
import com.comp.veta.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;


public class MessagesView extends Fragment {


    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    DocumentReference userRef;
    DocumentReference groupRef;

    View root;
    LinearLayout messageList;
    private AlertDialog.Builder dialogBuilder ;
    private AlertDialog dialog;

    private String groupCode;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialogBuilder = new AlertDialog.Builder(getContext());

        if (user != null) {
            userRef = db.collection("users").document(user.getUid());
        }
        if (getArguments()!=null)
       groupRef = db.collection("groups").document(getArguments().getString("groupID"));






    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_messages, container, false);
        messageList = root.findViewById(R.id.messages_list);
        ImageView groupImage= root.findViewById(R.id.groupImageMessages);
        TextView groupNameDisplay = root.findViewById(R.id.groupNameMessages);





        View backButton = root.findViewById(R.id.backToDashButton);

        backButton.setOnClickListener(v -> {

            Navigation.findNavController(v).navigate(R.id.action_navigation_messages_to_navigation_dashboard);
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







        return root;
    }


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

                if(group.getNumPeople()==1){
                    groupRef.delete();
                }
                else {
                    groupRef.update("numPeople", FieldValue.increment(-1));
                }






                Navigation.findNavController(root).navigate(R.id.action_navigation_messages_to_navigation_dashboard);
                progress.dismiss();

            }

        });

    }

    public void makeSettingsDialog(){

        View groupSettings =  getLayoutInflater().inflate(R.layout.popup_group_settings, null, false);

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
            dialog.dismiss();
        });

        AppCompatButton leaveGroupButton = groupSettings.findViewById(R.id.leaveGroupButton);

        leaveGroupButton.setOnClickListener(v -> {
            leaveGroup();
            dialog.dismiss();
        });


        dialogBuilder.setView(groupSettings);
        dialog = dialogBuilder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

    }









}