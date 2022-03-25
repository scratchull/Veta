package com.comp.veta.ui.profile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.comp.veta.AppContext;
import com.comp.veta.R;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

/**
 * This is the profile page as a bottom screen pop-up
 * It is set up differently than the rest of the bottom pop-ups because the is more functionality
 */
public class ProfileFragment extends BottomSheetDialogFragment {

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    ImageView userImage;
    StorageReference storageRef = FirebaseStorage.getInstance().getReference();


    /**
     * This code runs when a result is gotten from the device when picking a photo
     */
    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if (uri != null) {
                        StorageReference pfpRef = storageRef.child("users/" + user.getUid() + "/pfp.png");

                        UploadTask uploadTask = pfpRef.putFile(uri);

                        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }

                                // Continue with the task to get the download URL
                                return pfpRef.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Uri downloadUri = task.getResult();
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setPhotoUri(downloadUri)
                                            .build();

                                    user.updateProfile(profileUpdates)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        getActivity().recreate();
                                                    }


                                                }
                                            });

                                } else {
                                    Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_LONG).show();
                                }
                            }
                        });


                    }
                }
            });


    /**
     * This is the first thing that runs when this fragment is initialized
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetDialog);



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
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetDialog);
        getDialog().getWindow().getAttributes().windowAnimations =  R.style.BottomDialogAnimation;

        View root = inflater.inflate(R.layout.sheet_profile, container, false);

        TextView displayNameText = root.findViewById(R.id.accountName);
        Button signOutButton = root.findViewById(R.id.SignOutButton);
        Button changeNameButton = root.findViewById(R.id.ChangeNameButton);

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AuthUI.getInstance().signOut(AppContext.getAppContext()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(AppContext.getAppContext(), "You have been signed out", Toast.LENGTH_LONG).show();
                        //refresh fragment
                        getActivity().recreate();

                    }
                });

            }
        });
        changeNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (user!= null){
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity(), R.style.VetaDialogTheme);

                    alert.setTitle("Set Display Name");
                    alert.setMessage("This is the name others will see you as.");

                    // Set an EditText view to get user input
                    final EditText input = new EditText(getActivity());
                    input.getBackground().setColorFilter(getResources().getColor(R.color.blue_hint),
                            PorterDuff.Mode.SRC_ATOP);
                    alert.setView(input);

                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String value = input.getText().toString();

                            if (!value.equals("")) {
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(value)
                                        .build();

                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(getActivity(), "Name Changed", Toast.LENGTH_LONG).show();
                                                    getActivity().recreate();
                                                }
                                            }
                                        });
                            }
                        }
                    });

                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                        }
                    });


                    alert.show();
                }


            }
        });

        userImage = root.findViewById(R.id.ViewUserImage);
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mGetContent.launch("image/*");

            }
        });

        if (user!= null) {
            displayNameText.setText(user.getDisplayName());
          Picasso.get().load(user.getPhotoUrl())
                  .centerCrop()
                  .resize(250, 250)
                  .placeholder(R.drawable.logo)
                  .into(userImage);

        }


        return root;
    }






}