package com.duarus.linternaangelical;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserData {

    private Activity activity;
    private FirebaseUser user;
    private StorageReference mStorageRef;
    private String user_id;
    private FirebaseFirestore db;
    private String TAG="UserData";

    public UserData(Activity activity, String user_id){
        super();
        this.activity=activity;
        mStorageRef = FirebaseStorage.getInstance().getReference();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db = FirebaseFirestore.getInstance();
        db.setFirestoreSettings(settings);
        this.user_id=user_id;
    }

    public UserData downloadAndPutUserName(final ArrayList<TextView> textViews){
        DocumentReference docRef = db.collection("users").document(user_id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if(document!=null){
                        if (document.exists()) {
                            if(document.get("user_name")!=null){
                                for (int x=0;x<textViews.size();x++){
                                    textViews.get(x).setText(document.get("user_name").toString());
                                }
                            }
                            else{
                                for (int x=0;x<textViews.size();x++){
                                    textViews.get(x).setText("Error");
                                }
                            }
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
        return this;
    }

    public UserData downloadAndPutProfileImage(final ArrayList<CircleImageView> circleImageViews, @Nullable final ArrayList<CircleImageView> circleImageViews_thumbs){
        if(!isNotSessionStarted()){
            user = FirebaseAuth.getInstance().getCurrentUser();
            StorageReference riversRef = mStorageRef.child(user_id + "/profilePicture/profile.png");
            if(circleImageViews!=null){
                for (int x=0;x<circleImageViews.size();x++){
                    Glide.with(activity).load(riversRef).into(circleImageViews.get(x));
                }
            }
            if(circleImageViews_thumbs!=null){
                for (int x=0;x<circleImageViews_thumbs.size();x++){
                    Glide.with(activity).load(riversRef).into(circleImageViews_thumbs.get(x));
                }
            }
        }
        return this;
    }


    private boolean isNotSessionStarted(){
        user=FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            return user.getPhoneNumber() == null;
        }
        return true;
    }
}
