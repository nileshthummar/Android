package com.watchback2.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.watchback2.android.R;

@SuppressWarnings({"PMD.AvoidCatchingGenericException", "PMD.EmptyCatchBlock"})
public class ForceUpdateActivity extends Activity {

    public static boolean sIsForceUpdate;

    public static String sStrUrl = "";

    public static boolean sIsDisplayed;

    @SuppressWarnings({"PMD.CallSuperFirst"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            sIsDisplayed = true;
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        } catch (Exception ignored) {
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_forceupdate);

        //registerFinishReceiver();
        TextView closeBox = findViewById(R.id.closeBox);

        if (sIsForceUpdate) {
            closeBox.setVisibility(View.GONE);
        }

        closeBox.setOnClickListener(v -> finish());

        Button buttonUPdate = findViewById(R.id.buttonUPdate);
        buttonUPdate.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(sStrUrl));
                startActivity(intent);
            } catch (Exception e) { // google play app is not installed ?
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=com.watchback2.android"));
                startActivity(intent);
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (!sIsForceUpdate) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        //unregisterFinishReceiver();
        sIsDisplayed = false;
        super.onDestroy();
    }

}
