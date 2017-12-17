package at.android.lovebubble;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.Date;

import at.android.lovebubble.etc.DonationDialogFragment;
import at.android.lovebubble.etc.SeekBarPreference;


public class MyPreferenceFragment extends PreferenceFragment {
    private static final int RESULT_PICK_IMAGE = 99;

    private SharedPreferences preferences;

    DonationDialogFragment df = null;

    String TAG = "MyPreferenceFragment";
    Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        context = getContext();

        addPreferencesFromResource(R.xml.pref_general);

        //general
        findPreference("exit").setOnPreferenceClickListener(clickListener);
        findPreference("donate").setOnPreferenceClickListener(clickListener);
        findPreference("guide").setOnPreferenceClickListener(clickListener);

        //name
        findPreference("switch_name").setOnPreferenceChangeListener(changeListener);
        findPreference("name").setOnPreferenceChangeListener(changeListener);
        findPreference("y_offset").setOnPreferenceChangeListener(changeListener);
        findPreference("font_size_list").setOnPreferenceChangeListener(changeListener);

        //Date Time
        findPreference("switch_time").setOnPreferenceChangeListener(changeListener);
        findPreference("birthdatetime").setOnPreferenceClickListener(clickListener);
        findPreference("timeformat").setOnPreferenceChangeListener(changeListener);

        //Picture and Shape
        findPreference("choosepic").setOnPreferenceClickListener(clickListener);
        findPreference("size_list").setOnPreferenceChangeListener(changeListener);
        findPreference("mask_list").setOnPreferenceChangeListener(changeListener);

        //support
        findPreference("help").setOnPreferenceClickListener(clickListener);
        findPreference("contact").setOnPreferenceClickListener(clickListener);
        findPreference("rate").setOnPreferenceClickListener(clickListener);

        //more
        findPreference("about").setOnPreferenceClickListener(clickListener);
        findPreference("privpol").setOnPreferenceClickListener(clickListener);
        findPreference("libraries").setOnPreferenceClickListener(clickListener);

        PreferenceManager.setDefaultValues(getContext(), R.xml.pref_general, false);

    }

    @Override
    public void onResume() {
        super.onResume();

        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        // summary Size of Bubble
        ListPreference listPreference = (ListPreference) findPreference("size_list");
        int index = listPreference.findIndexOfValue(listPreference.getValue());
        listPreference.setSummary(
                index >= 0
                        ? listPreference.getEntries()[index]
                        : null);

        // summary Font Size
        listPreference = (ListPreference) findPreference("font_size_list");
        index = listPreference.findIndexOfValue(listPreference.getValue());
        listPreference.setSummary(
                index >= 0
                        ? listPreference.getEntries()[index]
                        : null);

        // summary Mask
        listPreference = (ListPreference) findPreference("mask_list");
        index = listPreference.findIndexOfValue(listPreference.getValue());
        listPreference.setSummary(
                index >= 0
                        ? listPreference.getEntries()[index]
                        : null);

        // summary Text Format
        listPreference = (ListPreference) findPreference("timeformat");
        index = listPreference.findIndexOfValue(listPreference.getValue());
        listPreference.setSummary(
                index >= 0
                        ? listPreference.getEntries()[index]
                        : null);

        // summary orientation
        listPreference = (ListPreference) findPreference("orientation");
        index = listPreference.findIndexOfValue(listPreference.getValue());
        listPreference.setSummary(
                index >= 0
                        ? listPreference.getEntries()[index]
                        : null);

        // summery birthday
        long mills = preferences.getLong("birthdatetime", -1);
        if (mills != -1) {
            findPreference("birthdatetime").setSummary(new DateTime(mills).toString());
        }

        // summary name
        String name = preferences.getString("name", null);
        if (name != null) {
            findPreference("name").setSummary(name);
        }

        // summary seekbar
        int yoffset = preferences.getInt("y_offset", MyImageView.DEFAULT_Y_OFFSET);
        findPreference("y_offset").setSummary(this.getString(R.string.settings_summary).replace("$1", "" + yoffset));


        if (DonationDialogFragment.donationDialogRequired(context)) {
            // only create new dialog when fragment is null or hidden
            DonationDialogFragment df = (DonationDialogFragment) getFragmentManager().findFragmentByTag("dialog");
            if (df == null)
                new DonationDialogFragment().show(getFragmentManager(), "dialog");
            else if (df.isHidden())
                df.show(getFragmentManager(), "dialog");
        }

    }


    Preference.OnPreferenceChangeListener changeListener = new Preference.OnPreferenceChangeListener() {
        // SAMPLE CODE: https://stackoverflow.com/questions/6148952/how-to-get-selected-text-and-value-android-listpreference
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            Intent i;

            switch (preference.getKey()) {
                case "y_offset":
                    i = new Intent("pref_broadcast");
                    i.putExtra("action", "updateYoffset");
                    getContext().sendBroadcast(i);
                    break;
                case "font_size_list":
                    i = new Intent("pref_broadcast");
                    i.putExtra("action", "updateFontSize");
                    getContext().sendBroadcast(i);
                    break;
                case "switch_time":
                case "switch_name":
                    i = new Intent("pref_broadcast");
                    i.putExtra("action", "switch_datetime");
                    getContext().sendBroadcast(i);
                    break;
                case "size_list":
                    i = new Intent("pref_broadcast");
                    i.putExtra("scale", Integer.parseInt(stringValue));
                    getContext().sendBroadcast(i);
                    break;
                case "mask_list":
                    i = new Intent("pref_broadcast");
                    i.putExtra("mask", stringValue);
                    getContext().sendBroadcast(i);
                    break;
                case "name":
                    i = new Intent("pref_broadcast");
                    i.putExtra("name", stringValue);
                    getContext().sendBroadcast(i);
                    break;
                case "timeformat":
                    i = new Intent("pref_broadcast");
                    i.putExtra("action", "timeformat");
                    getContext().sendBroadcast(i);
                    break;
                default:
                    Log.d(TAG, "onPreferenceChange: received event but was not handled.");
                    break;
            }


            // Set the summary to reflect the new value.
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof EditTextPreference) {
                EditTextPreference etp = (EditTextPreference) preference;
                etp.setSummary(stringValue);

            } else if (preference instanceof SeekBarPreference) {
                SeekBarPreference sbp = (SeekBarPreference) preference;
                sbp.setSummary(context.getString(R.string.settings_summary).replace("$1", "" + stringValue));
            }

            // return True to update the state of the Preference with the new value.
            return true;
        }
    };

    Preference.OnPreferenceClickListener clickListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            Intent i;
            switch (preference.getKey()) {
                case "donate":
                    startActivity(new Intent(context, Donate.class));
                    break;
                case "exit":
                    i = new Intent("pref_broadcast");
                    i.putExtra("action", "exit");
                    getContext().sendBroadcast(i);
                    getActivity().finish();
                    break;

                case "guide":
                    try {
                        Toast.makeText(context, R.string.unmonitoredappToast, Toast.LENGTH_LONG).show();
                        startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(context, R.string.setting_not_found, Toast.LENGTH_SHORT).show();
                    }
                    break;

                case "choosepic":
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);

                    startActivityForResult(Intent.createChooser(intent,
                            getString(R.string.selectpicture)), RESULT_PICK_IMAGE);
                    break;

                case "birthdatetime":
                    showDateTimePicker();
                    break;

                //support
                case "help":
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://skmobiledev.wordpress.com/lovebubble/language-selector/")));
                    break;

                case "contact":
                    String subject = "LOVE BUBBLE";
                    if (preferences.getBoolean("donationDone", false))
                        subject += " - DONATED";

                    subject += " (" +
                            // android.os.Build.DEVICE + " " + // Device
                            android.os.Build.MODEL + " " +  // Model
                            // android.os.Build.PRODUCT + " " +// Product
                            "Api:" + Build.VERSION.SDK_INT + ")";    // API Level

                    Intent emailIntent = new Intent(Intent.ACTION_VIEW);
                    Uri data = Uri.parse("mailto:action.jackson187@gmail.com?subject=" + subject); // + "&body=");
                    emailIntent.setData(data);
                    startActivity(emailIntent);
                    break;

                case "rate":
                    String uri = "market://details?id=" + context.getPackageName();
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(context, getString(R.string.playStoreNotFound), Toast.LENGTH_SHORT).show();
                    }
                    break;

                //help
                case "about":
                    showDevDialog();
                    break;
                case "privpol":
                    //startActivity(new Intent(getContext(), PrivacyPolicy.class));
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://skmobiledev.wordpress.com/lovebubble/privacy-policy/")));
                    break;
                case "libraries":
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://skmobiledev.wordpress.com/lovebubble/open-source-libraries/")));
                    break;
                default:
                    Log.d(TAG, "onPreferenceClick: received event but was not handled. Received=" + preference.getKey());
                    break;
            }
            return false;
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult: " + requestCode);

        // TODO: Handle Result NOT OK
        if (resultCode == Activity.RESULT_OK) {
            // choose picture
            if (data != null) {
                switch (requestCode) {
                    case RESULT_PICK_IMAGE:
                        /*
                        Intent CropIntent = new Intent("com.android.camera.action.CROP");
                        CropIntent.setDataAndType(selectedImageUri, "image*//*");
                        startActivityForResult(CropIntent, RESULT_CROP_IMAGE);
                        */
                        Uri selectedImageUri = data.getData();
                        CropImage.activity(selectedImageUri).setFixAspectRatio(true).start(getContext(), this);
                        break;

                    case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:

                        CropImage.ActivityResult result = CropImage.getActivityResult(data);
                        //if (resultCode == RESULT_OK) {
                        Uri resultUri = result.getUri();

                        //} else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        //    Exception error = result.getError();
                        //}
                        Intent i = new Intent("pref_broadcast");
                        i.putExtra("choosepic", resultUri.toString());
                        getContext().sendBroadcast(i);
                        break;

                    default:
                        break;
                }
            }
        }
    }

    private void showDateTimePicker() {
        Date value = new Date();
        final Calendar cal = Calendar.getInstance();
        cal.setTime(value);
        new DatePickerDialog(context,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view,
                                          int y, int m, int d) {
                        cal.set(Calendar.YEAR, y);
                        cal.set(Calendar.MONTH, m);
                        cal.set(Calendar.DAY_OF_MONTH, d);

                        // now show the time picker
                        new TimePickerDialog(context,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view,
                                                          int h, int min) {
                                        cal.set(Calendar.HOUR_OF_DAY, h);
                                        cal.set(Calendar.MINUTE, min);

                                        // Log.d(TAG, "onTimeSet: "+cal.getTimeInMillis());
                                        findPreference("birthdatetime").setSummary(cal.getTime().toString());
                                        Intent i = new Intent("pref_broadcast");
                                        i.putExtra("birthdatetime", cal.getTimeInMillis());
                                        getContext().sendBroadcast(i);
                                    }
                                }, cal.get(Calendar.HOUR_OF_DAY),
                                cal.get(Calendar.MINUTE), true).show();
                    }
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showDevDialog() {
        String versionName = "";
        int versionCode = 0;

        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        AlertDialog.Builder aboutDialog = new AlertDialog.Builder(getContext());

        aboutDialog.setTitle(R.string.about);
        aboutDialog.setMessage(getString(R.string.app_name) + " " + versionName + " (" + versionCode + ")" +
                "\n\n\u00A9 SK Mobile Development 2011-" + new DateTime().getYear() + "\nAll Rights Reserved.");

        aboutDialog.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        aboutDialog.create().show();
    }



/*
    private void showDonationRequestDialog() {
        long lastDonReq = preferences.getLong("lastDonReq", -1);
        long currentMills = new DateTime().getMillis();

        if (lastDonReq == -1) { // never requested
            preferences.edit().putLong("lastDonReq", currentMills).apply();

        } else if (currentMills - lastDonReq > 60 * 60 * 1000) {
            preferences.edit().putLong("lastDonReq", currentMills).apply();

            AlertDialog.Builder bld = new AlertDialog.Builder(context);
            bld.setMessage(R.string.donationDialogMessage);

            // Left
            if (!preferences.getBoolean("ratedOnPlayStore", false)) // not rated yet
                bld.setNeutralButton(R.string.rate, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        preferences.edit().putBoolean("ratedOnPlayStore", true).apply();

                        String uri = "market://details?id=" + context.getPackageName();
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(context, getString(R.string.playStoreNotFound), Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }
                });

            // Middle
            bld.setNegativeButton(R.string.Donate, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(context, Donate.class));
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
    */
}
