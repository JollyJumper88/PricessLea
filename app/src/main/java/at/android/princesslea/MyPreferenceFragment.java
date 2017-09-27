package at.android.princesslea;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.theartofdev.edmodo.cropper.CropImage;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.Date;

import at.android.princesslea.etc.PrivacyPolicy;


public class MyPreferenceFragment extends PreferenceFragment {
    private static final int RESULT_PICK_IMAGE = 99;

    String TAG = "MyPreferenceFragment";
    Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        context = getContext();
        addPreferencesFromResource(R.xml.pref_general);


        //findPreference("service_switch").setOnPreferenceChangeListener(listener);
        findPreference("name").setOnPreferenceChangeListener(listener);
        findPreference("size_list").setOnPreferenceChangeListener(listener);
        findPreference("textformat").setOnPreferenceChangeListener(listener);
        findPreference("mask_list").setOnPreferenceChangeListener(listener);

        findPreference("exit").setOnPreferenceClickListener(clickListener);
        findPreference("choosepic").setOnPreferenceClickListener(clickListener);
        findPreference("birthdatetime").setOnPreferenceClickListener(clickListener);
        findPreference("dev").setOnPreferenceClickListener(clickListener);
        findPreference("privpol").setOnPreferenceClickListener(clickListener);

        PreferenceManager.setDefaultValues(getContext(), R.xml.pref_general, false);


    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        // summary Size of Bubble
        ListPreference listPreference = (ListPreference) findPreference("size_list");
        int index = listPreference.findIndexOfValue(listPreference.getValue());
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
        listPreference = (ListPreference) findPreference("textformat");
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

    }


    Preference.OnPreferenceChangeListener listener = new Preference.OnPreferenceChangeListener() {
        // SAMPLE CODE: https://stackoverflow.com/questions/6148952/how-to-get-selected-text-and-value-android-listpreference
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            Intent i;

            switch (preference.getKey()) {

                case "service_switch":
//                    if (Boolean.parseBoolean(stringValue)) {
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

                case "textformat":
                    i = new Intent("pref_broadcast");
                    i.putExtra("action", "textformat");
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
                case "exit":
                    i = new Intent("pref_broadcast");
                    i.putExtra("action", "exit");
                    getContext().sendBroadcast(i);

                    getActivity().finish();

                    break;
                case "choosepic":
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);

                    startActivityForResult(Intent.createChooser(intent,
                            getString(R.string.selectpicture)), RESULT_PICK_IMAGE);

                    break;
                /*
                case "nextmask":
                    i = new Intent("pref_broadcast");
                    i.putExtra("action", "nextmask");
                    getContext().sendBroadcast(i);
                    break;
                */
                case "dev":
                    showDevDialog();
                    break;
                case "privpol":
                    startActivity(new Intent(getContext(),
                            PrivacyPolicy.class));
                    break;
                case "birthdatetime":
                    showDateTimePicker();
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
        AlertDialog.Builder aboutDialog = new AlertDialog.Builder(getContext());
        aboutDialog.setTitle(R.string.about);
        aboutDialog.setMessage("Princess Lea (v0.1.0)\n\n\u00A9 SK Mobile Development\nAll Rights Reserved");

        aboutDialog.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        aboutDialog.create().show();
    }
}
