package at.android.lovebubble.etc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.widget.Toast;

import at.android.lovebubble.FloatingFaceBubbleService;

public class StartMyServiceAtBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("start_at_boot", true)) {
                context.startService(new Intent(context, FloatingFaceBubbleService.class));
            }
        }
    }
}