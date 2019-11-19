package com.duarus.linternaangelical.dialogs;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import com.duarus.linternaangelical.R;

public class Report {
    private Activity activity;
    private AlertDialog.Builder builder;
    public Report(Activity activity){
        super();
        this.activity=activity;
        builder=new AlertDialog.Builder(activity, R.style.AppCompatAlertDialogStyle);
    }

    public Report showReportDialog(@NonNull String id_pub) {

        builder.setTitle(getStringReport(R.string.report_title));
        builder.setMessage(getStringReport(R.string.report_message));
        builder.setPositiveButton(activity.getResources().getString(R.string.fui_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();
        return this;
    }

    private String getStringReport(@NonNull int resId){
        return activity.getResources().getString(resId);
    }
}
