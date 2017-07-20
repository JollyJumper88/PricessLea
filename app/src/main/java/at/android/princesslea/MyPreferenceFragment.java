package at.android.princesslea;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;


public class MyPreferenceFragment extends PreferenceFragment {
    String TAG = "mpf";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        addPreferencesFromResource(R.xml.pref_general);

        // bindPreferenceSummaryToValue(findPreference("service_switch"));

//        findPreference("service_switch").setOnPreferenceChangeListener(listener);
//        findPreference("name").setOnPreferenceChangeListener(listener);
//
//
        findPreference("exit").setOnPreferenceClickListener(clickListener);
//
        findPreference("start").setOnPreferenceClickListener(clickListener);


    }

    Preference.OnPreferenceClickListener clickListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            Intent i;
            switch (preference.getKey()) {
                case "service_switch":
                    break;
                case "exit":
                    Log.d(TAG, "aaaaaaaaaaaaaa");
                    i = new Intent("pref_broadcast");
                    i.putExtra("action", "exit");
                    getContext().sendBroadcast(i);
                    break;
                case "start":
                    i = new Intent("pref_broadcast");
                    i.putExtra("action", "size1.5");
                    getContext().sendBroadcast(i);
                    break;
            }
            return false;
        }
    };
}
