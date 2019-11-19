package com.duarus.linternaangelical.items;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.duarus.linternaangelical.BuildConfig;
import com.duarus.linternaangelical.R;
import com.duarus.linternaangelical.UserData;
import com.duarus.linternaangelical.dialogs.Report;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;

import de.hdodenhof.circleimageview.CircleImageView;

public class PuclicationItem extends FrameLayout implements ViewTreeObserver.OnScrollChangedListener{

    private CircleImageView image_profile_top;
    private TextView user_name_top;
    private ProgressBar progressBar_loading_image;
    private ImageView image_view_publication;
    private TextView cant_likes_count;
    private ImageButton like;
    private ImageButton report;
    private ImageButton download;
    private Activity activity;
    private String publication_id;
    private String TAG="PublicationItem";
    private FirebaseFirestore db;
    private String file_path;
    private DocumentReference docRef;
    private boolean isLiked;
    private int cant_likes=0;
    private String like_id;
    private boolean loaded;
    private int download_images_tryes=0;

    //Rotate animation
    private final RotateAnimation rotate = new RotateAnimation(
            0, 360,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
    );

    /*Button names*/
    /*---------------*/
    private final int BUTTON_DOWNLOAD=1;
    private final int BUTTON_LIKE=2;
    private final int BUTTON_REPORT=3;
    /*------------------*/

    /*Downlaoded information*/
    /*-----------------------*/
    private String id_user;
    private String download_image_link;
    /*-------------------------*/

    /*String index values*/
    /*--------------------*/
    private final String ID_USER_INDEX="id_user";
    private final String DOWNLAOD_LINK_INDEX="download_link";
    /*--------------------*/

    public PuclicationItem(Context context){
        super(context);
    }

    public PuclicationItem(Context context, Attributes attr){
        super(context);
    }

    public PuclicationItem(Context context,Activity activity,String publication_id) {
        super(context);
        this.activity=activity;
        this.publication_id=publication_id;
        initView(context);
    }

    private void initView(Context context) {
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db = FirebaseFirestore.getInstance();
        db.setFirestoreSettings(settings);
        View view = LayoutInflater.from(context).inflate(R.layout.item_image_memenet, null);
        addView(view);
        image_profile_top=findViewById(R.id.profile_iamge_memenet_publication);
        user_name_top=findViewById(R.id.textview_user_name_memenet);
        progressBar_loading_image=findViewById(R.id.progressBar);
        image_view_publication=findViewById(R.id.iamge_view_big_memenet);
        cant_likes_count=findViewById(R.id.textview_likes_counter_memenet);
        like=findViewById(R.id.button_like);
        report=findViewById(R.id.button_report);
        download=findViewById(R.id.button_download);
        /*Rotate Settings*/
        /*-----------------*/
        rotate.setDuration(450);
        rotate.setRepeatCount(Animation.INFINITE);
        /*-------------------*/
        progressBar_loading_image.startAnimation(rotate);
        downloadPublicationData();
    }

    private boolean isVisible(final View view) {
        if(loaded){
            return false;
        }
        if (view == null) {
            return false;
        }
        if (!view.isShown()) {
            return false;
        }
        final Rect actualPosition = new Rect();
        view.getGlobalVisibleRect(actualPosition);
        DisplayMetrics displayMetrics=new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final Rect screen = new Rect(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        return actualPosition.intersect(screen);
    }

    public boolean isVisible(){
        if(isVisible(this)){
            downloadImagePublication();
            return true;
        }
        return false;
    }

    private void downloadPublicationData(){

        docRef = db.collection("publications").document(publication_id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if(document!=null){
                        if (document.exists()) {
                            if(document.get(DOWNLAOD_LINK_INDEX)!=null){
                                download_image_link=document.get(DOWNLAOD_LINK_INDEX).toString();
                                isVisible();
                                if(document.get(ID_USER_INDEX)!=null){
                                    id_user=document.get(ID_USER_INDEX).toString();
                                    setPublicationData();
                                    docRef.collection("likes").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if(task.isSuccessful()){
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    if(document.getData().containsValue(id_user)){
                                                        setButtonLiked();
                                                        like_id=document.getId();
                                                    }
                                                    cant_likes++;
                                                }
                                                updateLikeCount();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG,e.getMessage());
                                        }
                                    });
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
    }

    private void setPublicationData(){
        ArrayList<TextView> textViews_user_names= new ArrayList<>();
        textViews_user_names.add(user_name_top);
        ArrayList<CircleImageView> circleImageViews_thumbs = new ArrayList<>();
        circleImageViews_thumbs.add(image_profile_top);
        new UserData(activity,id_user).downloadAndPutUserName(textViews_user_names).downloadAndPutProfileImage(null,circleImageViews_thumbs);
        updateLikeCount();
    }

    private boolean isImageCached(String id){
        File file = new File(activity.getCacheDir(), id+".png");
        if(file.exists()){
            return file.isFile();
        }
        return false;
    }

    private void updateLikeCount(){
        cant_likes_count.setText(NumberFormat.getInstance().format(cant_likes)+"\u2000 Me gusta");
    }

    private void like(){
        if(docRef!=null){
            like_1();
            final Map<String, Object> user_map = new HashMap<>();
            user_map.put("id_user", id_user);
            db.collection("publications").document(publication_id).collection("likes").add(user_map).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    like_id=documentReference.getId();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    setButtonDisLiked();
                    Toast.makeText(activity, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void like_1(){
        setButtonLiked();
        cant_likes++;
        updateLikeCount();
    }

    private void dislike_1(){
        setButtonDisLiked();
        cant_likes--;
        updateLikeCount();
    }

    private void dislike(){
        if(docRef!=null){
            dislike_1();
            db.collection("publications").document(publication_id).collection("likes").document(like_id).delete().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG,e.getMessage());
                }
            });
        }
    }

    private void setButtonLiked(){
        ViewGroup.LayoutParams layoutParams=like.getLayoutParams();
        like.setImageResource(R.drawable.ic_action_liked);
        like.setLayoutParams(layoutParams);
        isLiked=true;
    }

    private void setButtonDisLiked(){
        ViewGroup.LayoutParams layoutParams=like.getLayoutParams();
        like.setImageResource(R.drawable.ic_action_like);
        like.setLayoutParams(layoutParams);
        isLiked=false;
    }

    private void enableButton(int button){
        if(button==BUTTON_DOWNLOAD){
            download.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveImageInGalery();
                }
            });
        }else{
            if(button==BUTTON_REPORT){
                report.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showReportDialog();
                    }
                });
            }else{
                if(button==BUTTON_LIKE){
                    like.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (isLiked) {
                                dislike();
                            } else {
                                like();
                            }
                        }
                    });
                }
            }
        }
    }

    private void disableButton(int button){
        if(button==BUTTON_DOWNLOAD){
            download.setOnClickListener(null);
        }else{
            if(button==BUTTON_REPORT){
                report.setOnClickListener(null);
            }else{
                if(button==BUTTON_LIKE){
                    like.setOnClickListener(null);
                }
            }
        }
    }

    private void showReportDialog(){
        new Report(activity).showReportDialog(publication_id);
    }

    private void enableAllButtons(){
        enableButton(BUTTON_DOWNLOAD);
        enableButton(BUTTON_REPORT);
        enableButton(BUTTON_LIKE);
    }

    private void downloadImagePublication(){
        if(!loaded){
            loaded=true;
            if(isImageCached(publication_id)){
                File file = new File(activity.getCacheDir(), publication_id+".png");
                Bitmap bitmap=BitmapFactory.decodeFile(file.getAbsolutePath());
                progressBar_loading_image.clearAnimation();
                progressBar_loading_image.setVisibility(GONE);
                image_view_publication.setImageBitmap(Bitmap.createScaledBitmap(bitmap,500,500,false));
                image_view_publication.setVisibility(VISIBLE);
                file_path=file.getAbsolutePath();
                enableAllButtons();
            }else{
                if(download_image_link!=null){
                    StorageReference riversRef = FirebaseStorage.getInstance().getReferenceFromUrl(download_image_link);
                    final long ONE_MEGABYTE = 512 * 512;
                    riversRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bitmap=BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                            progressBar_loading_image.clearAnimation();
                            progressBar_loading_image.setVisibility(GONE);
                            image_view_publication.setImageBitmap(Bitmap.createScaledBitmap(bitmap,500,500,false));
                            image_view_publication.setVisibility(VISIBLE);
                            File file = new File(activity.getCacheDir(), publication_id+".png");
                            file.deleteOnExit();
                            FileOutputStream fileOutputStream;
                            try {
                                fileOutputStream = new FileOutputStream(file.getAbsolutePath());
                                BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                                bos.flush();
                                bos.close();
                                file_path=file.getAbsolutePath();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            enableAllButtons();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            download_images_tryes++;
                            if(download_images_tryes<3){
                                loaded=false;
                                downloadImagePublication();
                            }
                        }
                    });
                }else{
                    Log.w(TAG,"DOWNLOAD_LIN NULL"+publication_id);
                    download_images_tryes++;
                    if(download_images_tryes<3){
                        loaded=false;
                        downloadImagePublication();
                    }

                }
            }
        }
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewTreeObserver vto = getViewTreeObserver();
        if (vto != null) {
            vto.addOnScrollChangedListener(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ViewTreeObserver vto = getViewTreeObserver();
        if (vto != null) {
            vto.removeOnScrollChangedListener(this);
        }
    }

    @Override
    public void onScrollChanged() {
        if(!loaded){isVisible();}
    }

    private void saveImageInGalery()
    {
        if(isStoragePermissiongranted()){
            File root1 = new File(Environment.getExternalStorageDirectory()+"/Memenet");
            if(!root1.exists())
            {
                String storageDirectory=(Environment.getExternalStorageDirectory()+"/Memenet");
                File mynewfolder=new File(storageDirectory);
                mynewfolder.mkdir();
                saveImageInGalery();
            }else {
                File file_=new File(file_path);
                File file_dest=new File(root1+"/"+publication_id+".png");
                try {
                    copy(file_,file_dest);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                Uri file_uri=FileProvider.getUriForFile(activity,BuildConfig.APPLICATION_ID+".provider",file_dest);
                activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file_dest)));
                final Intent intent=new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(file_uri,"image/png");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Snackbar.make(activity.findViewById(R.id.corrdinator_layout_activity_complete),"Guardado!",Snackbar.LENGTH_LONG).setAction("Abrir", new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.startActivity(intent);
                    }
                }).show();
            }
        }
    }

    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    public int REQUES_CODE=45;
    public boolean isStoragePermissiongranted()
    {
        if(Build.VERSION.SDK_INT>=23)
        {
            if(!(activity.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED))
            {
                activity.requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUES_CODE);
                return false;
            }
            else
            {
                return true;
            }
        }
        else
        {
            return true;
        }
    }
}
