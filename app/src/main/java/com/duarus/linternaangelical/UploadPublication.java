package com.duarus.linternaangelical;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.LinearLayout;

import com.duarus.linternaangelical.items.PuclicationItem;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class UploadPublication {

    private Activity activity;
    private Uri uri;
    private String downloadUrl;
    private FirebaseUser user;
    private StorageReference mStorageRef;
    private StorageReference riversRef;
    private FirebaseFirestore db;
    private UploadImageControler uploadImageControler;
    private LinearLayout publications_container;
    private String publication_id;

    UploadPublication (@NonNull Activity activity, Uri uri, UploadImageControler uploadImageControler, LinearLayout publications_container){
        super();
        this.activity=activity;
        this.publications_container=publications_container;
        this.uploadImageControler=uploadImageControler;
        this.uri=uri;
        mStorageRef = FirebaseStorage.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db = FirebaseFirestore.getInstance();
        db.setFirestoreSettings(settings);
        UploadImage();
    }

    private void UploadImage(){
        if(uri!=null){
            publication_id=UUID.randomUUID().toString();
            riversRef = mStorageRef.child("publications/"+publication_id+".png");
            UploadTask uploadTask = riversRef.putFile(uri);

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    // Continue with the task to get the download URL
                    return riversRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        if(task.getResult()!=null){
                            downloadUrl=task.getResult().toString();
                            addPublicationData();
                        }
                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });
        }
    }

    private void addPublicationData(){
        final Map<String, Object>[] user_map = new Map[]{new HashMap<>()};
        user_map[0].put("id_",publication_id);
        user_map[0].put("id_user", user.getUid());
        db.collection("publications_ids").document(publication_id).set(user_map[0])
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        user_map[0] = new HashMap<>();
                        user_map[0].put("id_user", user.getUid());
                        user_map[0].put("download_link",downloadUrl);
                        user_map[0].put("upload_moment_user", System.currentTimeMillis());
                        user_map[0].put("upload_moment_server", Timestamp.now());

                        db.collection("publications").document(publication_id)
                                .set(user_map[0])
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        uploadImageControler.cancel();
                                        publications_container.addView(new PuclicationItem(activity,activity,publication_id),0);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

    }
}
