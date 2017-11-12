package at.android.lovebubble;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;

import at.android.lovebubble.etc.DonationDialogFragment;
import at.android.lovebubble.etc.DonationRequestDialog;


public class MainActivity extends Activity implements View.OnClickListener, DonationDialogFragment.DialogFragmentListener {

    public final static int REQUEST_CODE = 101;
    private static final String TAG = "MainActivity";
    private Button button;
    private TextView textViewTitle, textViewMain, textViewStatusbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        textViewTitle = (TextView) findViewById(R.id.textViewTitle);
        textViewMain = (TextView) findViewById(R.id.textViewMain);
        button = (Button) findViewById(R.id.buttonMain);

        button.setOnClickListener(this);
        textViewStatusbar = (TextView) findViewById(R.id.textViewStatusbar);

        if (Settings.canDrawOverlays(this)) {

            textViewTitle.setText(R.string.textviewTitleWelcome);
            textViewMain.setText(R.string.startup);
            button.setText(R.string.gotIt);
            textViewStatusbar.setText("");

            if (DonationDialogFragment.donationDialogRequired(this)) {
                if (getFragmentManager().findFragmentByTag("dialog") == null)
                    new DonationDialogFragment().show(getFragmentManager(), "dialog");

            } else {
                startService(new Intent(MainActivity.this, FloatingFaceBubbleService.class));
            }
            // Toast.makeText(this, R.string.doubleTapSettings, Toast.LENGTH_LONG).show();
            // finish();

        } else {
            textViewTitle.setText(R.string.textviewTitleWelcome);
            textViewMain.setText(R.string.grantPermission);
            button.setText(R.string.grantPermissionButton);
            textViewStatusbar.setText(R.string.status_step1);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /* check if received result code is equal our requested code for draw permission */
        if (requestCode == REQUEST_CODE) {
            if (Settings.canDrawOverlays(this)) {

                startService(new Intent(MainActivity.this, FloatingFaceBubbleService.class));

                textViewTitle.setText(R.string.main_title_success);
                textViewMain.setText(R.string.startup);
                button.setText(R.string.gotIt);
                textViewStatusbar.setText(R.string.status_step2);
            }
        }
    }

    @Override
    public void onClick(View view) {
        Button b = (Button) view;
        String buttonText = b.getText().toString();

        if (getString(R.string.grantPermissionButton).equals(buttonText)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE);

        } else if (getResources().getString(R.string.gotIt).equals(buttonText)) {
            finish();
        } else {
            Log.e(TAG, "onClick: Received unhandled click event" + b.getText());
        }

    }

    @Override
    public void onDialogFragmentClick(int button) {
        // It does not matter which button was click, we start service in any case
        startService(new Intent(MainActivity.this, FloatingFaceBubbleService.class));
    }

    /*
    private boolean donationDialogRequired() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!preferences.getBoolean("donationDone", false)) {// not yet donated
            long lastDonReq = preferences.getLong("lastDonReq", -1);
            long currentMills = new DateTime().getMillis();
            int minutes = 1;

            if (lastDonReq == -1) { // never requested
                return false;

            } else if (currentMills - lastDonReq > minutes * 60 * 1000) {// time exceeded
                return true;
            }
            return false;
            //DonationRequestDialog.showDonationRequestDialog(this, preferences);
            //showDonationRequestDialog();
        } else {
            return false;
        }
    }
    */

/*
    public boolean checkDrawOverlayPermission() {
        // check if we already  have permission to draw over other apps
    }
    */

    /*
    @Override
    protected void onResume() {
    //        Bundle bundle = getIntent().getExtras();
    //        if(bundle != null && bundle.getString("LAUNCH").equals("YES")) {
    //            startService(new Intent(MainActivity.this, FloatingFaceBubbleService.class));
    //        }

        if (Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(MainActivity.this, FloatingFaceBubbleService.class);
            startService(intent);
        }

        super.onResume();
    }
    */
    /*
    @Override
    protected void onPause() {

        stopService(new Intent(MainActivity.this, FloatingFaceBubbleService.class));

        super.onPause();
    }
    */
    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    */
}
