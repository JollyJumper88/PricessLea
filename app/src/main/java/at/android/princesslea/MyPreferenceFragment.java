package at.android.princesslea;

import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;


public class MyPreferenceFragment extends PreferenceFragment {
    String TAG = "MyPreferenceFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_general);

        // bindPreferenceSummaryToValue(findPreference("service_switch"));

        //findPreference("service_switch").setOnPreferenceChangeListener(listener);
//        findPreference("name").setOnPreferenceChangeListener(listener);
        findPreference("size_list").setOnPreferenceChangeListener(listener);
        findPreference("exit").setOnPreferenceClickListener(clickListener);

        findPreference("nextpicture").setOnPreferenceClickListener(clickListener);

        PreferenceManager.setDefaultValues(getContext(), R.xml.pref_general, false);


    }

    @Override
    public void onResume() {
        super.onResume();

        // Set Summary on opening
        ListPreference listPreference = (ListPreference) findPreference("size_list");
        int index = listPreference.findIndexOfValue(listPreference.getValue());
        listPreference.setSummary(
                index >= 0
                        ? listPreference.getEntries()[index]
                        : null);
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
//                        Intent intent = new Intent(getContext(), FloatingFaceBubbleService.class);
//                        getActivity().startService(intent);
//                    }
//                    else {
//                        i = new Intent("pref_broadcast");
//                        i.putExtra("service_switch", false);
//                        getContext().sendBroadcast(i);
//                    }
                    break;
                case "size_list":
                    i = new Intent("pref_broadcast");
                    i.putExtra("scale", Integer.parseInt(stringValue));
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
                case "nextpicture":
                    i = new Intent("pref_broadcast");
                    i.putExtra("action", "nextpicture");
                    getContext().sendBroadcast(i);
                    break;
                default:
                    Log.d(TAG, "onPreferenceClick: received event but was not handled.");
                    break;
            }
            return false;
        }
    };
}
