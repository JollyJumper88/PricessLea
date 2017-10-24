package at.android.lovebubble;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import at.android.lovebubble.util.IabBroadcastReceiver;
import at.android.lovebubble.util.IabBroadcastReceiver.IabBroadcastListener;
import at.android.lovebubble.util.IabHelper;
import at.android.lovebubble.util.IabHelper.IabAsyncInProgressException;
import at.android.lovebubble.util.IabResult;
import at.android.lovebubble.util.Inventory;
import at.android.lovebubble.util.Purchase;

public class Donate extends Activity implements
        IabBroadcastListener, View.OnClickListener {

    static final String TAG = "Donate";

    // Does the user have the premium upgrade?
    boolean mIsPremium = false;

    // SKUs for our products: the premium upgrade (non-consumable) and gas (consumable)
    //static final String SKU_PREMIUM = "premium";
    static final String SKU_S = "donate_small";
    static final String SKU_M = "donate_medium";
    static final String SKU_L = "donate_large";

    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10001;

    // The helper object
    IabHelper mHelper;

    // Provides purchase notification while this app is running
    IabBroadcastReceiver mBroadcastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);

        findViewById(R.id.small).setOnClickListener(this);
        findViewById(R.id.medium).setOnClickListener(this);
        findViewById(R.id.large).setOnClickListener(this);

        // TODO: fix key
        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjfD4xypeFFHh7Hn0GdoF5HrHIHCIEH2FZ+q7dJXx3iURNIirDoIRKRhV6jteYN7/BHjvD6CFAtfbTizoIRPzlJhdEBPgwxqT8vu5mFqh7IjQxT0u+zQ0cxMZ8GFPsVGFpK7K0lO5HWc/Qcr0qLWZXuVE4Yc9cAVq12m2HF95tlG+n+obnmqidYSlbg7E3G7EaB3xykXlm+Pfb3m+mPdtlGoCltgtJb5Eq178Y345bF5/Ky4Z5/UlQna9g/yBT2F2yohWe5MA4IeptJdSj1y9n7HAi8erxUJm9/+P/3ioMhICJ3yiEKOB/9nxcSbYa47IGYqyen6pgkO+3EVTJke6GQIDAQAB";

        mHelper = new IabHelper(this, base64EncodedPublicKey);
        // Todo: disable here
        mHelper.enableDebugLogging(true);

        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    complain("Problem setting up in-app billing: " + result);
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;

                // Important: Dynamically register for broadcast messages about updated purchases.
                // We register the receiver here instead of as a <receiver> in the Manifest
                // because we always call getPurchases() at startup, so therefore we can ignore
                // any broadcasts sent while the app isn't running.
                // Note: registering this listener in an Activity is a bad idea, but is done here
                // because this is a SAMPLE. Regardless, the receiver must be registered after
                // IabHelper is setup, but before first call to getPurchases().
                mBroadcastReceiver = new IabBroadcastReceiver(Donate.this);
                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                registerReceiver(mBroadcastReceiver, broadcastFilter);

                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d(TAG, "Setup successful. Querying inventory.");
                try {
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                } catch (IabAsyncInProgressException e) {
                    Log.d(TAG, "onIabSetupFinished: Error querying inventory. Another async operation in progress.");
                }
            }
        });
    }

    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                return;
            }

            Log.d(TAG, "Query inventory was successful.");

            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */
            // TODO: check items we own and reflect on the button
            // Do we have the premium upgrade?
            Purchase premiumPurchase = inventory.getPurchase(SKU_S);
            mIsPremium = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
            Log.d(TAG, "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));


            updateUi();
            setWaitScreen(false);
            Log.d(TAG, "Initial inventory query finished; enabling main UI.");
        }
    };

    @Override
    public void receivedBroadcast() {
        // Received a broadcast notification that the inventory of items has changed
        Log.d(TAG, "Received broadcast notification. Querying inventory.");
        try {
            mHelper.queryInventoryAsync(mGotInventoryListener);
        } catch (IabAsyncInProgressException e) {
            complain("Error querying inventory. Another async operation in progress.");
        }
    }



    @Override
    public void onClick(View view) {
    //public void onClick(DialogInterface dialog, int id) {

        String SKU = "";

        switch (view.getId()) {
            case R.id.small:
                SKU = SKU_S;
                break;
            case R.id.medium:
                SKU = SKU_M;
                break;
            case R.id.large:
                SKU = SKU_L;
                break;
            default:
                Log.d(TAG, "onClick: id not handled but received.");
        }
        if (!SKU.equals("")) {
            Log.d(TAG, "Upgrade button clicked; launching purchase flow for upgrade.");
            setWaitScreen(true);

        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
            String payload = "";

            try {
                mHelper.launchPurchaseFlow(this, SKU, RC_REQUEST,
                        mPurchaseFinishedListener, payload);
            } catch (IabAsyncInProgressException e) {
                complain("Error launching purchase flow. Another async operation in progress.");
                setWaitScreen(false);
            }

        } else  {
            // There are only four buttons, this should not happen
            Log.e(TAG, "Unknown button clicked in subscription dialog: ");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }


    /**
     * Verifies the developer payload of a purchase.
     */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                complain("Error purchasing: " + result);
                setWaitScreen(false);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");
                setWaitScreen(false);
                return;
            }

            Log.d(TAG, "Purchase successful.");

            if (purchase.getSku().equals(SKU_S)) {
                // bought the premium upgrade!
                Log.d(TAG, "Purchase is premium upgrade. Congratulating user.");
                alert("Thank you for upgrading to premium!");
                mIsPremium = true;
                updateUi();
                setWaitScreen(false);
            }

        }
    };


    // updates UI to reflect model
    public void updateUi() {
//        // update the car color to reflect premium status or lack thereof
//        ((ImageView)findViewById(R.id.free_or_premium)).setImageResource(mIsPremium ? R.drawable.premium : R.drawable.free);
//
//        // "Upgrade" button is only visible if the user is not premium
//        findViewById(R.id.upgrade_button).setVisibility(mIsPremium ? View.GONE : View.VISIBLE);
//
//        ImageView infiniteGasButton = (ImageView) findViewById(R.id.infinite_gas_button);
//        if (mSubscribedToInfiniteGas) {
//            // If subscription is active, show "Manage Infinite Gas"
//            infiniteGasButton.setImageResource(R.drawable.manage_infinite_gas);
//        } else {
//            // The user does not have infinite gas, show "Get Infinite Gas"
//            infiniteGasButton.setImageResource(R.drawable.get_infinite_gas);
//        }
//
//        // update gas gauge to reflect tank status
//        if (mSubscribedToInfiniteGas) {
//            ((ImageView)findViewById(R.id.gas_gauge)).setImageResource(R.drawable.gas_inf);
//        }
//        else {
//            int index = mTank >= TANK_RES_IDS.length ? TANK_RES_IDS.length - 1 : mTank;
//            ((ImageView)findViewById(R.id.gas_gauge)).setImageResource(TANK_RES_IDS[index]);
//        }
    }


    // Enables or disables the "please wait" screen.
    void setWaitScreen(boolean set) {
//        findViewById(R.id.screen_main).setVisibility(set ? View.GONE : View.VISIBLE);
//        findViewById(R.id.screen_wait).setVisibility(set ? View.VISIBLE : View.GONE);
        findViewById(R.id.wait).setVisibility(set ? View.VISIBLE : View.INVISIBLE);
    }


    void complain(String message) {
        Log.e(TAG, "**** TrivialDrive Error: " + message);
        alert("Error: " + message);
    }

    void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d(TAG, "Showing alert dialog: " + message);
        bld.create().show();
    }


}
