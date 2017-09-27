package at.android.princesslea;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends Activity {

    public final static int REQUEST_CODE = -1010101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);


        checkDrawOverlayPermission();

        if (Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(MainActivity.this, FloatingFaceBubbleService.class);
            startService(intent);
        }

        finish();
    }

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
/*
    @Override
    protected void onPause() {

        stopService(new Intent(MainActivity.this, FloatingFaceBubbleService.class));

        super.onPause();
    }
*/

    public void checkDrawOverlayPermission() {
        // check if we already  have permission to draw over other apps */
        if (!Settings.canDrawOverlays(getApplicationContext())) {
            // if not construct intent to request permission */
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            // request permission via start activity for result */
            startActivityForResult(intent, REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /* check if received result code
         is equal our requested code for draw permission  */



        if (requestCode == REQUEST_CODE) {
            if (Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(MainActivity.this, FloatingFaceBubbleService.class);
                startService(intent);
            }
        }
    }

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



}
