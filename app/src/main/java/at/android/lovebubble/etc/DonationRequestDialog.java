package at.android.lovebubble.etc;


import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.Toast;

import org.joda.time.DateTime;

import at.android.lovebubble.Donate;
import at.android.lovebubble.R;

public class DonationRequestDialog {
    public static void showDonationRequestDialog(final Context context, final SharedPreferences preferences) {

        // Todo: donation done not show dialog again
        long lastDonReq = preferences.getLong("lastDonReq", -1);
        long currentMills = new DateTime().getMillis();
        int minutes = 1;

        if (lastDonReq == -1) { // never requested
            preferences.edit().putLong("lastDonReq", currentMills).apply();

        } else if (currentMills - lastDonReq > minutes * 60 * 1000/*every hour*/) {
            preferences.edit().putLong("lastDonReq", currentMills).apply();

            AlertDialog.Builder bld = new AlertDialog.Builder(context, R.style.DialogTheme);
            bld.setMessage(R.string.donationDialogMessage);

            // Left
            if (!preferences.getBoolean("ratedOnPlayStore", false)) // not rated yet
                bld.setNeutralButton(R.string.rate, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        preferences.edit().putBoolean("ratedOnPlayStore", true).apply();

                        String uri = "market://details?id=" + context.getPackageName();
                        try {
                            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(context, context.getResources().getString(R.string.playStoreNotFound), Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }
                });

            // Middle
            bld.setNegativeButton(R.string.Donate, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    context.startActivity(new Intent(context, Donate.class));
                    dialog.dismiss();
                }
            });

            // Right
            bld.setPositiveButton(R.string.NoThanks, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    dialog.dismiss();
                }
            });

            bld.create().show();
        }


    }
}
