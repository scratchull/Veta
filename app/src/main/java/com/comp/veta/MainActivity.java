package com.comp.veta;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.comp.veta.ui.home.HomeFragment;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;


public class MainActivity extends AppCompatActivity {
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private int SIGN_IN_REQUEST_CODE = 123;
    private static String TAG = MainActivity.class.getSimpleName();
    private FusedLocationProviderClient mFusedLocationClient;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!Places.isInitialized()) {
            Places.initialize(this, "AIzaSyBkVhZrjxQa14PfrRiDwuPoyVPHmtAwQnA");

        }
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        NavigationUI.setupWithNavController(navView, navController);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        if(FirebaseAuth.getInstance().getCurrentUser()==null){
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(),SIGN_IN_REQUEST_CODE);
        } else{
            Toast.makeText(this, "Welcome "+FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),Toast.LENGTH_LONG).show();
        }


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SIGN_IN_REQUEST_CODE){
            if(resultCode== RESULT_OK){
                Toast.makeText(this, "Successfully signed in. Welcome!", Toast.LENGTH_LONG).show();
                finish();
                startActivity(getIntent());
            } else{
                Toast.makeText(this, "There was an issue signing you in. Please try again.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }



    }


