package com.comp.veta.ui.dashboard;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;


public class MessagesView extends Fragment {


    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    DocumentReference userRef;
    DocumentReference groupRef;

    View root;
    LinearLayout messageList;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        groupRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                Group group = documentSnapshot.toObject(Group.class);

                if (group != null) {
                    Picasso.get().load(group.getPhotoURL())
                            .centerCrop()
                           .resize(150, 150)
                            .into(groupImage);
                    groupNameDisplay.setText(group.getName());


                }

            }
        });





        return root;
    }









}