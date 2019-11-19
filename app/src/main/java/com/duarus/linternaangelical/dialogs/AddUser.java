package com.duarus.linternaangelical.dialogs;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.duarus.linternaangelical.R;
import com.firebase.ui.auth.AuthUI;

import java.util.Collections;
import java.util.List;

public class AddUser {
    private Activity activity;
    public static final int RC_SIGN_IN=5;
    public AddUser(Activity activity){
        super();
        this.activity=activity;
    }
    public AddUser showUserDialog() {

        final AlertDialog.Builder builder =
                new AlertDialog.Builder(activity, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Crear cuenta de usuario");
        builder.setMessage("Al crear una cuenta de usuario podras usar funciones como estados con imagenes y compartir memes en la memenet");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                goToSignIn();
            }
        });
        builder.setNegativeButton(activity.getResources().getString(R.string.fui_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
        return this;
    }

    private void goToSignIn(){
        List<AuthUI.IdpConfig> providers = Collections.singletonList(
                new AuthUI.IdpConfig.PhoneBuilder().build());

// Create and launch sign-in intent
        activity.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setTheme(R.style.PhoneAuth)
                        .build(),
                RC_SIGN_IN);
    }
}
