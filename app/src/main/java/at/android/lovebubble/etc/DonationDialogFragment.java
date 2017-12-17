package at.android.lovebubble.etc;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.joda.time.DateTime;

import at.android.lovebubble.Donate;
import at.android.lovebubble.R;

public class DonationDialogFragment extends DialogFragment {

    private Context context;
    private SharedPreferences preferences;

    public interface DialogFragmentListener {
        void onDialogFragmentClick(int button);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if (!(context instanceof DialogFragmentListener)) {
            throw new ClassCastException(context.toString() + " must implement DialogFragmentListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        setCancelable(false);

        preferences = PreferenceManager.getDefaultSharedPreferences(context);

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

                    ((DialogFragmentListener) getActivity()).onDialogFragmentClick(AlertDialog.BUTTON_NEUTRAL);

                    preferences.edit().putLong("lastDonReq", new DateTime().getMillis()).apply();

                    dialog.dismiss();
                }
            });

        // Middle
        bld.setNegativeButton(R.string.Donate, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                context.startActivity(new Intent(context, Donate.class));

                ((DialogFragmentListener) getActivity()).onDialogFragmentClick(AlertDialog.BUTTON_NEGATIVE);

                preferences.edit().putLong("lastDonReq", new DateTime().getMillis()).apply();

                dialog.dismiss();
            }
        });

        // Right
        bld.setPositiveButton(R.string.NoThanks, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {

                ((DialogFragmentListener) getActivity()).onDialogFragmentClick(AlertDialog.BUTTON_POSITIVE);

                preferences.edit().putLong("lastDonReq", new DateTime().getMillis()).apply();

                dialog.dismiss();
            }
        });


        return bld.create();
    }


    public static boolean donationDialogRequired(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (!preferences.getBoolean("donationDone", false)) {// not yet donated

            long lastDonReq = preferences.getLong("lastDonReq", -1);
            long currentMills = new DateTime().getMillis();
            int minutes = 2;

            if (lastDonReq == -1) { // never requested
                preferences.edit().putLong("lastDonReq", new DateTime().getMillis()).apply(); // initial set of dialog
                return false;

            } else if (currentMills - lastDonReq > minutes * 60 * 1000) {// time exceeded
                return true;
            }
            // time not exceeded
            return false;

        } else {
            return false;
        }
    }
}