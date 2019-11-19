package com.duarus.linternaangelical;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class SettingProfile extends AppCompatActivity {


    private StorageReference mStorageRef;
    FirebaseUser user;
    FrameLayout frame_image_selector_container;
    EditText textview_user_name;
    TextView add_iamge;
    CircleImageView profile_iamge;
    private StorageReference riversRef;
    FloatingActionButton floatingActionButton_check;
    FirebaseFirestore db;
    int CODE_SELECT_IMAGE=35;
    String downloadUrl;
    String name;
    String TAG="SettingProfile";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_setting_profile);

        //Init vars
        /*-------------------------*/
        mStorageRef = FirebaseStorage.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        frame_image_selector_container=findViewById(R.id.frame_image_selector_container);
        textview_user_name=findViewById(R.id.edittext_user_name);
        profile_iamge=findViewById(R.id.image_profile_setting);
        add_iamge=findViewById(R.id.textview_add_iamge);
        floatingActionButton_check=findViewById(R.id.floating_button_check);
        db = FirebaseFirestore.getInstance();
        /*-------------------------*/

        if(isNotSessionStarted()){
            goToInit();
        }
        else{

            frame_image_selector_container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pickImage();
                }
            });
            floatingActionButton_check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(downloadUrl!=null&&isNameValid()){
                        addName(name);
                    }
                }
            });
        }

    }

    private void addName(String name){
        Map<String, Object> user_map = new HashMap<>();
        user_map.put("user_name", name);
        db.collection("users").document(user.getUid())
                .set(user_map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        goToInit();
                        SettingProfile.this.finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if(requestCode==CODE_SELECT_IMAGE){
            if(resultCode==RESULT_OK){
                if(data!=null){
                    Uri file = data.getData();
                    riversRef = mStorageRef.child(user.getUid()+"/profilePicture/profile.png");

                    UploadTask uploadTask = riversRef.putFile(file);

                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
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
                                downloadUrl=task.getResult().toString();
                                InputStream stream=null;
                                try{
                                    stream=getContentResolver().openInputStream(data.getData());
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                Bitmap bitmap=BitmapFactory.decodeStream(stream);
                                profile_iamge.setImageBitmap(bitmap);
                                profile_iamge.setVisibility(View.VISIBLE);
                                add_iamge.setVisibility(View.INVISIBLE);
                            } else {
                                // Handle failures
                                // ...
                            }
                        }
                    });
                }
            }
        }
    }

    private boolean isNameValid(){
        String name1=textview_user_name.getText().toString();

        if(name1.isEmpty()||name1==null){
            return false;
        }
        name=name1;
        Log.w("NOMBRE",name);
        return true;
    }

    private boolean isNotSessionStarted(){
        if(user!=null){
            if(user.getPhoneNumber()!=null){
                return false;
            }
            else{
                return true;
            }
        }
        return true;
    }

    private void goToInit(){
        Intent intent=new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, CODE_SELECT_IMAGE);
    }

}
