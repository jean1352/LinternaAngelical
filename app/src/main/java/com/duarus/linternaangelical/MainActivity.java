package com.duarus.linternaangelical;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.duarus.linternaangelical.adapters.AdapterMenuBottom;
import com.duarus.linternaangelical.dialogs.AddUser;
import com.duarus.linternaangelical.items.PuclicationItem;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    Switch switch1;
    MediaPlayer myMediaPlayer;
    public static Camera cam = null;// has to be static, otherwise onDestroy() destroys it
    LinearLayout linearLayout_image_stories_contain;
    HorizontalScrollView horizontalScrollView;
    BottomSheetBehavior bottomSheetBehavior;
    BottomNavigationView bottomNavigationView;
    NestedScrollView page_one;
    NestedScrollView page_two;
    NestedScrollView page_tree;
    LinearLayout memenet,publications_layout_container_account;
    CircleImageView profile_iamge;
    TextView name_user_account;
    MaterialButton sign_out;
    CircleImageView profile_image_min_memenet;
    TextView name_user_account_memenet_header;
    private final int[] resources = new int[]{
            R.drawable.sample1,
            R.drawable.sample2,
            R.drawable.sample3,
            R.drawable.sample4,
            R.drawable.sample5,
            R.drawable.yui
    };
    DocumentSnapshot lastVisible_memenet;
    String TAG="MainActivity";
    ViewPager viewPager;
    int PAGE_RATING=0;
    int PAGE_MEMENET=1;
    int PAGE_ACCOUNT=2;

    //Flags Initiated
    private boolean MEMENET_INITIALIZED;
    private boolean ACCOUNT_INITIALIZED;
    private boolean RATING_INITIALIZED;

    UploadImageControler uploadImageControler;
    //Firebase
    FirebaseUser user;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_complete);
        switch1=findViewById(R.id.switch1);
        isAllPermisinsGranted();

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        linearLayout_image_stories_contain=findViewById(R.id.layout_image_stories_contain);
        horizontalScrollView=findViewById(R.id.horizontal_container);
        myMediaPlayer = MediaPlayer.create(this, R.raw.audio);


        if(!isNotSessionStarted()){
            initStoriesContainer();
            enableBottomMenu();
        }

        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleFlashLight(isChecked);
                managerOfSound();
            }
        });

    }

    private void enableBottomMenu(){
        uploadImageControler=new UploadImageControler(this);
        findViewById(R.id.bottom_sheet).setVisibility(View.VISIBLE);
        sign_out=findViewById(R.id.sign_out_button);
        sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
                new AddUser(MainActivity.this).showUserDialog();
            }
        });
        name_user_account=findViewById(R.id.textview_name_user_account);
        profile_iamge=findViewById(R.id.image_profile_account);
        viewPager=findViewById(R.id.view_pager_bottom_sheet);
        memenet=findViewById(R.id.liner_layout_memenet_container_publications);
        publications_layout_container_account=findViewById(R.id.publications_account_container);
        page_one=findViewById(R.id.page_one);
        page_two=findViewById(R.id.page_two);
        page_tree=findViewById(R.id.page_tree);
        profile_image_min_memenet=findViewById(R.id.profile_iamge_min);
        name_user_account_memenet_header=findViewById(R.id.textview_name_user_account_memenet_header);
        //Bottom Sheet
        bottomSheetBehavior= BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
        bottomSheetBehavior.setHideable(false);

        //Bottom navigationview
        bottomNavigationView=findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id=menuItem.getItemId();
                if(bottomSheetBehavior.getState()!=BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    setStatusBarDim(false);
                }
                if(id==R.id.nav_toggle_sheet){
                    viewPager.setCurrentItem(PAGE_MEMENET,false);
                }
                if(id==R.id.nav_rating){
                    viewPager.setCurrentItem(PAGE_RATING,false);
                }
                if(id==R.id.nav_user){
                    viewPager.setCurrentItem(PAGE_ACCOUNT,false);
                }
                return true;
            }
        });

        //View pager
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(new AdapterMenuBottom());
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                int id=R.id.nav_rating;
                if(i==PAGE_MEMENET){
                    id=R.id.nav_toggle_sheet;
                    if(!MEMENET_INITIALIZED){
                        initiateMemeNet();
                    }
                }else if(i==PAGE_ACCOUNT){
                    id=R.id.nav_user;
                    if(!ACCOUNT_INITIALIZED){
                        onAccountSelected();
                    }
                }else{
                    if(!RATING_INITIALIZED){
                        onRatingSelected();
                    }
                }
                bottomNavigationView.setSelectedItemId(id);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        ArrayList<TextView> textViews_user_names= new ArrayList<>();
        textViews_user_names.add(name_user_account);
        textViews_user_names.add(name_user_account_memenet_header);
        ArrayList<CircleImageView> circleImageViews = new ArrayList<>();
        circleImageViews.add(profile_iamge);
        ArrayList<CircleImageView> circleImageViews_thumbs = new ArrayList<>();
        circleImageViews_thumbs.add(profile_image_min_memenet);
        new UserData(this,user.getUid()).downloadAndPutUserName(textViews_user_names).downloadAndPutProfileImage(circleImageViews,circleImageViews_thumbs);
        uploadImageControler.init();
    }

    void initiateScrollMemenet(){
        page_two.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView nestedScrollView, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (nestedScrollView.getChildAt(nestedScrollView.getChildCount() - 1) != null) {
                    if ((scrollY >= (nestedScrollView.getChildAt(nestedScrollView.getChildCount() - 1).getMeasuredHeight() - nestedScrollView.getMeasuredHeight())) &&
                            scrollY > oldScrollY) {
                        initiateMemeNet();
                    }

                }
            }
        });
    }

     void disableScrollMemenet(){
        page_two.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView nestedScrollView, int i, int i1, int i2, int i3) {

            }
        });
     }

    private void onRatingSelected(){
        RATING_INITIALIZED=true;
    }

    private void onAccountSelected(){
        CollectionReference colRef = db.collection("publications_ids");
        colRef.whereEqualTo("id_user", user.getUid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if(task.getResult()!=null){
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            publications_layout_container_account.addView(new PuclicationItem(MainActivity.this,MainActivity.this,document.getId()));
                        }
                    }
                    else{
                        Log.w(TAG, "NULL DATA");
                    }
                } else {
                    Log.w(TAG, "Error getting documents: ", task.getException());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
        publications_layout_container_account.refreshDrawableState();
        ACCOUNT_INITIALIZED=true;
    }

    private void initiateMemeNet(){
        CollectionReference colRef = db.collection("publications_ids");
        Query first;
        if(lastVisible_memenet!=null){
            if(lastVisible_memenet.exists()){
                Log.w("ENTER","1");
                first=colRef.startAfter(lastVisible_memenet).limit(5);
            }else{
                first=colRef.limit(5);
            }
        }
        else{
            first=colRef.limit(5);
        }
        first.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if(task.getResult()!=null){
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            memenet.addView(new PuclicationItem(MainActivity.this,MainActivity.this,document.getId()));
                        }
                        initiateScrollMemenet();
                        if(lastVisible_memenet!=null){
                            if(lastVisible_memenet.exists()){
                                upPageMemenet();
                            }
                        }
                        if(task.getResult().getDocuments().size()!=0){
                            lastVisible_memenet=task.getResult().getDocuments().get(task.getResult().getDocuments().size()-1);
                        }

                    }
                    else{
                        Log.w(TAG, "NULL DATA");
                        disableScrollMemenet();
                    }
                } else {
                    Log.w(TAG, "Error getting documents: ", task.getException());
                    disableScrollMemenet();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                disableScrollMemenet();
            }
        });
        memenet.refreshDrawableState();
        MEMENET_INITIALIZED=true;
    }

    void upPageMemenet(){
        page_two.setScrollY(page_two.getScrollY()+(int)Conversions.convertDpToPixel(10,this));
    }

    private void setStatusBarDim(boolean dim) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(dim ? Color.TRANSPARENT :
                    ContextCompat.getColor(this, getThemedResId()));
        }
    }

    private int getThemedResId() {
        TypedArray a = getTheme().obtainStyledAttributes(new int[]{R.attr.colorPrimaryDark});
        int resId = a.getResourceId(0, 0);
        a.recycle();
        return resId;
    }


    @Override
    public void onBackPressed() {
        if(bottomSheetBehavior!=null){
            if(bottomSheetBehavior.getState()==BottomSheetBehavior.STATE_EXPANDED){
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                setStatusBarDim(true);
            }
            else{
                super.onBackPressed();
            }
        }
        else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AddUser.RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                Intent go_to_settings_profile=new Intent(MainActivity.this,SettingProfile.class);
                startActivity(go_to_settings_profile);
                MainActivity.this.finish();
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                if(response!=null){
                    if(response.getError()!=null){
                        Toast.makeText(this, String.format("Error:%s", response.getError().getMessage()), Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        }else if(requestCode==uploadImageControler.CODE_IMAGE_FOR_MEMENET){
            if(resultCode==RESULT_OK){
                if(data!=null){
                    InputStream stream= null;
                    try {
                        if(data.getData()!=null){
                            stream = getContentResolver().openInputStream(data.getData());
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if(stream!=null){
                        uploadImageControler.setImage(BitmapFactory.decodeStream(stream),data.getData());
                    }
                }
            }
        }
    }

    private void initStoriesContainer(){
        LayoutInflater inf = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int x=0;x<10;x++){
            View v=inf.inflate(R.layout.item_iamge,null);
            ImageView image=v.findViewById(R.id.image_profile_status);
            int valorEntero = (int) Math.floor(Math.random()*6);
            image.setImageBitmap(cropDrawable(resources[valorEntero]));
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToStories();
                }
            });
            linearLayout_image_stories_contain.addView(v);
        }
        horizontalScrollView.refreshDrawableState();

    }

    private void goToStories(){
        Intent stories=new Intent(this,Stories.class);
        startActivity(stories);
    }

    private Bitmap cropDrawable(int resId){
        Drawable drawable;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            drawable=getDrawable(resId);
        }
        else{
            drawable=getResources().getDrawable(resId);
        }
        int size=(int)Conversions.convertDpToPixel(60,this);
        assert drawable != null;
        return Bitmap.createScaledBitmap(((BitmapDrawable)drawable).getBitmap(), size,size, false);
    }

    /*private Bitmap cropDrawable(int resId){
        Drawable drawable;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            drawable=getDrawable(resId);
        }
        else{
            drawable=getResources().getDrawable(resId);
        }

        int size=(int)Conversions.convertDpToPixel(dp,this);
        DisplayMetrics displayMetrics=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width=displayMetrics.widthPixels;
        assert drawable != null;
        return Bitmap.createScaledBitmap(((BitmapDrawable)drawable).getBitmap(), width,size, false);
    }*/

    private void flashOff(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CameraManager cameraManager;
            cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            try {
                assert cameraManager != null;
                String cameraId = cameraManager.getCameraIdList()[0];

                cameraManager.setTorchMode(cameraId, false);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }else {
            flashLightOff();
        }
    }

    private void toggleFlashLight(boolean isFlashLightOn){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CameraManager cameraManager;
            cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            try {
                assert cameraManager != null;
                String cameraId = cameraManager.getCameraIdList()[0];
                cameraManager.setTorchMode(cameraId, isFlashLightOn);
            } catch (CameraAccessException e) {
                e.printStackTrace();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        else{
            if(isFlashLightOn){
                flashLightOn();
            }
            else{
                flashLightOff();
            }
        }
    }

    public void flashLightOn() {

        try {
            if (getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_FLASH)) {
                cam = Camera.open();
                Parameters p = cam.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                cam.setParameters(p);
                cam.startPreview();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getBaseContext(), "Exception flashLightOn()",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNotSessionStarted(){
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            return user.getPhoneNumber() == null;
        }
        return true;
    }

    public void flashLightOff() {
        try {
            if (getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_FLASH)) {
                cam.stopPreview();
                cam.release();
                cam = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getBaseContext(), "Exception flashLightOff",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        if (myMediaPlayer != null) {
            myMediaPlayer.release();
            myMediaPlayer = null;
        }
        flashOff();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (myMediaPlayer != null) {
            myMediaPlayer.release();
            myMediaPlayer = null;
        }
        flashOff();
        super.onDestroy();
    }

    int REQUES_CODE=2;


    private void isAllPermisinsGranted(){
        if(Build.VERSION.SDK_INT>=23)
        {
            ArrayList<String> permisions=new ArrayList<>();

            if(!(checkSelfPermission(Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED))
            {
                permisions.add(Manifest.permission.CAMERA);
            }
            if(!(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED))
            {
                permisions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if(!(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED))
            {
                permisions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if(permisions.size()>0){
                String[] per=new String[permisions.size()];
                for (int x=0;x<permisions.size();x++){
                    per[x]=permisions.get(x);
                }
                requestPermissions(per,REQUES_CODE);
            }
            else{
                if(isNotSessionStarted()){
                    new AddUser(this).showUserDialog();
                }
            }
        }
        else{
            if(isNotSessionStarted()){
                new AddUser(this).showUserDialog();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUES_CODE)
        {
            int count_denied=0;
            for (int x=0;x<permissions.length;x++){
                if(grantResults[x]==PackageManager.PERMISSION_DENIED){
                    count_denied++;
                }
            }
            String[] per=new String[count_denied];
            int c=0;
            for (int x=0;x<permissions.length;x++){
                if(grantResults[x]==PackageManager.PERMISSION_DENIED){
                    per[c]=permissions[x];
                    c++;
                }
            }
            if(count_denied>0){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(per,REQUES_CODE);
                }
            }
            else{
                this.recreate();
            }
        }
    }

    private void managerOfSound() {
        if(myMediaPlayer!=null){
            if (!myMediaPlayer.isPlaying()) {
                myMediaPlayer.start();
                myMediaPlayer.setLooping(true);
            } else {
                myMediaPlayer.reset();
                myMediaPlayer.stop();
            }
            myMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.reset();
                    mp.release();
                    myMediaPlayer=MediaPlayer.create(MainActivity.this,R.raw.audio);
                }
            });
        }
        else{
            myMediaPlayer=MediaPlayer.create(MainActivity.this,R.raw.audio);
            managerOfSound();
        }
    }

    public void signOut() {
        // [START auth_fui_signout]
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });
        // [END auth_fui_signout]
    }

    /*public void delete() {
        // [START auth_fui_delete]
        AuthUI.getInstance()
                .delete(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });
        // [END auth_fui_delete]
    }*/


    /*public void privacyAndTerms() {
        List<AuthUI.IdpConfig> providers = Collections.emptyList();
        // [START auth_fui_pp_tos]
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setTosAndPrivacyPolicyUrls(
                                "https://example.com/terms.html",
                                "https://example.com/privacy.html")
                        .build(),
                RC_SIGN_IN);
        // [END auth_fui_pp_tos]
    }*/
}



