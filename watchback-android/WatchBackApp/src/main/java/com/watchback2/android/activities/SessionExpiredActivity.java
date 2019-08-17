package com.watchback2.android.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;

import com.watchback2.android.utils.AppConstants;
import com.watchback2.android.utils.PerkUtils;

public class SessionExpiredActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        forceLogout();
    }
    static AlertDialog dialogForceUpdate;
    public  void forceLogout()
    {
        Log.w("PerkSession","3");
        if(dialogForceUpdate != null && dialogForceUpdate.isShowing())
        {
            return;
        }
        Log.w("PerkSession","4");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Setting Dialog Title
        builder.setTitle("Error!");

        // Setting Dialog Message
        builder.setMessage("Your Session has expired. Please log in to continue.");
        Log.w("PerkSession","4_1");
        builder.setCancelable(false);
        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        Log.w("PerkSession","18");

                        sendBroadcast(new Intent(AppConstants.ACTION_FINISH));
                        int secondsDelayed = 2;
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                PerkUtils.logoutUser();
                                finish();
                            }
                        }, secondsDelayed * 1000);

                    }
                });

        // Showing Alert Message
        try {
            //builder.show();
            dialogForceUpdate = builder.create();

            dialogForceUpdate.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode,
                                     KeyEvent event) {

                    if (keyCode == KeyEvent.KEYCODE_BACK
                            && event.getAction() == KeyEvent.ACTION_UP
                            && !event.isCanceled()) {
                        Log.w("PerkSession","4_2");
                        return true;
                    }
                    return false;
                }
            });
            Log.w("PerkSession","4_3");
            dialogForceUpdate.setCanceledOnTouchOutside(false);

            Log.w("PerkSession","4_4");
            dialogForceUpdate.show();
            Log.w("PerkSession","4_5");
        } catch (Exception e) {
            Log.w("PerkSession","4_6");
            e.printStackTrace();
        }

    }
}
