package com.watchback2.android.api;

import android.content.Context;
import android.util.Log;

import com.watchback2.android.models.PerkUser;
import com.watchback2.android.utils.PerkUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by Nilesh on 11/25/16.
 */
public class PerkFileManager {
    private static final String LOG_TAG = PerkFileManager.class.getSimpleName();
    final  static String fileNamePerkUser = "PerkUser";
    private static PerkUser sPerkUser;
    public static void savePerkUser(PerkUser user)
    {
        try {
            Log.w(LOG_TAG,"9");
            if (user != null)
            {

                Log.w(LOG_TAG,"10");
                FileOutputStream fos = PerkUtils.getAppContext().openFileOutput(fileNamePerkUser, Context.MODE_PRIVATE);
                Log.w(LOG_TAG,"11");
                ObjectOutputStream os = new ObjectOutputStream(fos);
                Log.w(LOG_TAG,"12");
                os.writeObject(user);
                Log.w(LOG_TAG,"13");
                os.close();
                Log.w(LOG_TAG,"14");
                fos.close();
                Log.w(LOG_TAG,"15");
                sPerkUser = user;
                Log.w(LOG_TAG,"16");
            }
        }catch (Exception e){
            Log.w(LOG_TAG,"17");
        }

    }
    public  static PerkUser loadPerkUser()
    {
        try
        {
            Log.w(LOG_TAG,"1");
            if(sPerkUser == null)
            {
                Log.w(LOG_TAG,"2");
                FileInputStream fis = PerkUtils.getAppContext().openFileInput(fileNamePerkUser);
                Log.w(LOG_TAG,"3");
                ObjectInputStream is = new ObjectInputStream(fis);
                Log.w(LOG_TAG,"4");
                sPerkUser = (PerkUser) is.readObject();
                Log.w(LOG_TAG,"5");
                is.close();
                Log.w(LOG_TAG,"6");
                fis.close();
                Log.w(LOG_TAG,"7");
            }
        }catch (Exception e){
            Log.w(LOG_TAG,"8");
            Log.w(LOG_TAG,e.getMessage());
        }
        return sPerkUser;
    }
    public  static  void deletePerkUser()
    {
        try {
            File f = new File(fileNamePerkUser);
            f.delete();
        }catch (Exception e){}
    }
    /////
}
