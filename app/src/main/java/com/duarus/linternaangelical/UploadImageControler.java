package com.duarus.linternaangelical;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.button.MaterialButton;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

class UploadImageControler {

    private Activity activity;
    private MaterialButton upload;
    private MaterialButton cancel;
    private ImageView icon_help,thumb_image;
    private TextView textview_help;
    private LinearLayout linearLayout_container_buttons,container_image_and_help;
    private Uri uri_file;

    int CODE_IMAGE_FOR_MEMENET=23;


    UploadImageControler(@NonNull Activity activity){
        super();
        this.activity=activity;
        upload=activity.findViewById(R.id.btn_upload_image_memenet);
        cancel=activity.findViewById(R.id.btn_cancel_image_memenet);
        icon_help=activity.findViewById(R.id.imageview_icon_upload_image_memenet);
        thumb_image=activity.findViewById(R.id.imageview_thumb_image_for_memenet);
        textview_help=activity.findViewById(R.id.textview_help_upload_image_memenet);
        linearLayout_container_buttons=activity.findViewById(R.id.linealayout_buttons_container_memenet_header);
        container_image_and_help=activity.findViewById(R.id.linearlayout_upload_image_to_memenet);
    }

    void init(){
        container_image_and_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        activity.startActivityForResult(intent, CODE_IMAGE_FOR_MEMENET);
    }

    void setImage(Bitmap bitmap,Uri uri){
        if(bitmap!=null){
            int size=(int)Conversions.convertDpToPixel(150,activity);
            thumb_image.setImageBitmap(Bitmap.createScaledBitmap(bitmap, size,size, false));
            imageSelected();
            setUri_file(uri);
        }
    }

    private void imageSelected(){
        icon_help.setVisibility(View.GONE);
        textview_help.setVisibility(View.GONE);
        linearLayout_container_buttons.setVisibility(View.VISIBLE);
        thumb_image.setVisibility(View.VISIBLE);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload();
            }
        });
    }

    void cancel(){
        icon_help.setVisibility(View.VISIBLE);
        textview_help.setVisibility(View.VISIBLE);
        linearLayout_container_buttons.setVisibility(View.GONE);
        thumb_image.setImageDrawable(null);
        thumb_image.setVisibility(View.GONE);
        upload.setOnClickListener(null);
        cancel.setOnClickListener(null);
    }

    private void setUri_file(Uri uri_file) {
        this.uri_file = uri_file;
    }

    private void upload(){
        new UploadPublication(activity,uri_file,this,(LinearLayout) activity.findViewById(R.id.liner_layout_memenet_container_publications));
    }
}
