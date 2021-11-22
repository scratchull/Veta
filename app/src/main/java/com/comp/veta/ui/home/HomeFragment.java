package com.comp.veta.ui.home;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.comp.veta.AppContext;
import com.comp.veta.Background.Person;
import com.comp.veta.MainActivity;
import com.comp.veta.R;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.installations.FirebaseInstallations;

public class HomeFragment extends Fragment {



    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        TextView homeEmailText = root.findViewById(R.id.accountName);
        Button signOutButton = root.findViewById(R.id.SignOutButton);

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

        ImageView logo = root.findViewById(R.id.viewLogo);
        logo.setBackground(Drawable.createFromPath("file:///android_asset/logo.png"));

        if (user!= null)
        homeEmailText.setText(user.getDisplayName());

        return root;
    }




}