package at.android.lovebubble;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import at.android.lovebubble.util.IabBroadcastReceiver;
import at.android.lovebubble.util.IabBroadcastReceiver.IabBroadcastListener;
import at.android.lovebubble.util.IabHelper;
import at.android.lovebubble.util.IabHelper.IabAsyncInProgressException;
import at.android.lovebubble.util.IabResult;
import at.android.lovebubble.util.Inventory;
import at.android.lovebubble.util.Purchase;
import at.android.lovebubble.util.SkuDetails;

public class Donate extends Activity implements
        IabBroadcastListener, View.OnClickListener {

    static final String TAG = "Donate";

    // Does the user have the premium upgrade?
    boolean boughtS = false;
    boolean boughtM = false;
    boolean boughtL = false;

    // SKUs for our products: the premium upgrade (non-consumable) and gas (consumable)
    //static final String SKU_PREMIUM = "premium";
    static final String SKU_S = "donate_small";
    static final String SKU_M = "donate_medium";
    static final String SKU_L = "donate_large";
    static final String SKU_purchased = "android.test.purchased";
    static final String SKU_canceled = "android.test.canceled";
//            "android.test.refunded";
//            "android.test.item_unavailable";

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

        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjfD4xypeFFHh7Hn0GdoF5HrHIHCIEH2FZ+q7dJXx3iURNIirDoIRKRhV6jteYN7/BHjvD6CFAtfbTizoI";
        String part2 = "RPzlJhdEBPgwxqT8vu5mFqh7IjQxT0u+zQ0cxMZ8GFPsVGFpK7K0lO5HWc/Qcr0qLWZXuVE4Yc9cAVq12m2HF95tlG+n+obnmqidYSlbg7E3G7EaB3xykXlm+Pfb3m+mPdtlGoCltgtJb5Eq178Y3";
        String part3 = "45bF5/Ky4Z5/UlQna9g/yBT2F2yohWe5MA4IeptJdSj1y9n7HAi8erxUJm9/+P/3ioMhICJ3yiEKOB/9nxcSbYa47IGYqyen6pgkO+3EVTJke6GQIDAQAB";

        mHelper = new IabHelper(this, base64EncodedPublicKey + part2 + part3);

        mHelper.enableDebugLogging(true);

        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // TODO: unavailable-disable donation?
                    // Oh noes, there was a problem.
                    complain("Problem setting up in-app billing: " + result);
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;

                // Todo: ?? hää? do I need this receiver? where to call getPurchaes -> query finished?
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

            // how to get the price
            // https://stackoverflow.com/questions/16502765/in-app-billing-getprice-android

            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */
            // TODO: check items we own and reflect on the button

            Purchase purchaseS = inventory.getPurchase(SKU_S); // returns null if no purchase
            Purchase purchaseM = inventory.getPurchase(SKU_M); // returns null if no purchase
            Purchase purchaseL = inventory.getPurchase(SKU_L); // returns null if no purchase
            //boolean boughtS = inventory.hasPurchase(SKU_S);

            // SkuDetails detailsS = inventory.getSkuDetails(SKU_S); // returns null if no purchase
            // Log.d(TAG, "onQueryInventoryFinished: "+ detailsS.toString());

            boughtS = (purchaseS != null && verifyDeveloperPayload(purchaseS));
            boughtM = (purchaseM != null && verifyDeveloperPayload(purchaseM));
            boughtL = (purchaseL != null && verifyDeveloperPayload(purchaseL));

            // Log.d(TAG, "User is " + (boughtS2 ? "purchaseS" : "NOT purchaseS"));
            // alert(boughtS ? "purchaseS" : "NOT purchaseS");

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
                // SKU = SKU_purchased;
                break;
            case R.id.large:
                SKU = SKU_L;
                // SKU = SKU_canceled;
                break;
            default:
                Log.d(TAG, "onClick: id not handled but received.");
        }
        if (!SKU.equals("")) {
            Log.d(TAG, "Upgrade button clicked; launching purchase flow for upgrade.");
            setWaitScreen(true);

            /* for security, generate your payload here for verification. See the comments on
            *  verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
            *  an empty string, but on a production app you should carefully generate this. */
            String payload = "Princess Lea is a character of Star Wars and also my daughter";

            try {
                mHelper.launchPurchaseFlow(this, SKU, RC_REQUEST, mPurchaseFinishedListener, payload);

            } catch (IabAsyncInProgressException e) {
                complain("Error launching purchase flow. Another async operation in progress.");
                setWaitScreen(false);
            }

        } else {
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
        if (payload.contains("Star Wars"))
            return true;
        else
            return false;
    }

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            // todo:
            // if Users cancels, we get here
            // already owned
            // todo: change message or remove
            if (result.isFailure()) {
                complain("Finished listener: Error purchasing: " + result);
                setWaitScreen(false);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain("Finished listener: Error purchasing. Authenticity verification failed. (Payload)");
                setWaitScreen(false);
                return;
            }

            Log.d(TAG, "Purchase successful.");

            if (purchase.getSku().equals(SKU_S)) {
                // bought the premium upgrade!
                // Log.d(TAG, "PThank you for buying SKU_S");
                //alert("Thank you for buying SKU_S");
                boughtS = true;
            }
            if (purchase.getSku().equals((SKU_M))) {
                boughtM = true;
                //alert("Thank you for buying SKU_M");
            }
            if (purchase.getSku().equals((SKU_L))) {
                boughtL = true;
                //alert("Thank you for buying SKU_L");
            }

            setWaitScreen(false);
            updateUi();

        }
    };


    // updates UI to reflect model
    public void updateUi() {

        Button S = (Button) findViewById(R.id.small);
        Button M = (Button) findViewById(R.id.medium);
        Button L = (Button) findViewById(R.id.large);

        S.setText("SMALL: " + boughtS);
        M.setText("MEDIUM " + boughtM);
        L.setText("LARGE " + boughtL);

//        // update the car color to reflect premium status or lack thereof
//        ((ImageView)findViewById(R.id.free_or_premium)).setImageResource(boughtS2 ? R.drawable.premium : R.drawable.free);
//
//        // "Upgrade" button is only visible if the user is not premium
//        findViewById(R.id.upgrade_button).setVisibility(boughtS2 ? View.GONE : View.VISIBLE);


        // non UI operation but a good place to write preference (called after querying and purchase finshed
        if (boughtL||boughtM||boughtS) {
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("donationDone", true).apply();
        }

    }


    void setWaitScreen(boolean set) {
//        findViewById(R.id.screen_main).setVisibility(set ? View.GONE : View.VISIBLE);
//        findViewById(R.id.screen_wait).setVisibility(set ? View.VISIBLE : View.GONE);
        findViewById(R.id.wait).setVisibility(set ? View.VISIBLE : View.INVISIBLE);
    }


    void complain(String message) {
        alert("Error: " + message);
    }

    void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        bld.create().show();
    }


}
