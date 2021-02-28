package com.crickettechnology.vsmp;

import android.content.*;

// start activity at boot
public class BootReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent) 
    {
        System.out.println(intent.getAction());

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) 
        {
            Intent appIntent = new Intent(context, MainActivity.class);
            appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(appIntent);
        }
    }
}

