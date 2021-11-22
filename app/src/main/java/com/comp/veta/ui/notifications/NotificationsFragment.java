package com.comp.veta.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import com.comp.veta.AppContext;
import com.comp.veta.Background.Person;
import com.comp.veta.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.installations.FirebaseInstallations;

import java.util.ArrayList;

public class NotificationsFragment extends Fragment {
    static LinearLayout list ;
   static View root;
   static Person person;
   static String userID;

    /**
     *loads the saved instance
     * @param savedInstanceState : saved instance from the last state
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     *  Runs on the create of the fragment
     *  makes the on click listeners for the refresh button
     *  makes a list of views which hold the notifications
     * @param inflater : used to inflate the views into layout
     * @param container : the contain which this viw is held
     * @param savedInstanceState :  saved instance from the last state
     * @return : root view
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_notifications, container, false);
        list =  root.findViewById(R.id.notification_list);


        AppCompatImageView refreshButton = root.findViewById(R.id.refreshNotif);
        refreshButton.setOnClickListener(new View.OnClickListener() { //refreshes the list
            @Override
            public void onClick(View v) {
                list.removeAllViews();
                createNotifList();
            }
        });


        createNotifList();

            return root;
    }

    /**
     * Makes the list of views which are the notifications
     * Gets the notifications from the firebase in the users reference
     */
    public void createNotifList(){
        FirebaseInstallations.getInstance().getId()        // attempt to get unique firebase user ID
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        userID = task.getResult(); //gets the unique user Id from firebase
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        DocumentReference personRef = db.collection("people").document(userID); //ref to the person

                        personRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    person = documentSnapshot.toObject(Person.class);
                                    ArrayList<String> notifs = person.getNotifications();

                                    for (String notif : notifs){ //for each notification in the list of notifications in firebase
                                        int i = notif.indexOf(",");
                                        String title = notif.substring(0,i);
                                        String message = notif.substring(i+1);
                                        addNotif(title,message);

                                    }



                                }

                            }
                        });
                    }
                });
    }

    /**
     * Adds a notification to the list in the notification tab
     * Makes a view for each notification
     * Each view has a remove button to delete that notification
     * Also adds the notification to the firebase under the person reference
     * @param Title : the title to the notification
     * @param Message : the message of the notification
     */
    public static void addNotif (String Title, String Message){

        View view = LayoutInflater.from(AppContext.getAppContext()).inflate(R.layout.notification_temp, null);
        AppCompatImageView clear = view.findViewById(R.id.makeButton);
        clear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                list.removeView(view);


                String notif = Title+","+Message;
                person.removeNotification(notif);
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference personRef = db.collection("people").document(userID);
                personRef.set(person);

            }
        });
        TextView tmpTitle = view.findViewById(R.id.notify_group_name);
        tmpTitle.setText(Title);
        TextView tmpMessage = view.findViewById(R.id.notif_text);
        tmpMessage.setText(Message);

        list.addView(view);





    }
}