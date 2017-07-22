package at.android.princesslea.etc;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.webkit.WebView;

import at.android.princesslea.R;

public class PrivacyPolicy extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        WebView webview = (WebView) findViewById(R.id.wv1);
        //webview.getSettings().setJavaScriptEnabled(true);
        //webview.loadData(getHtml(), "text/html", "UTF-8");
        webview.loadDataWithBaseURL(null, getHtml(), "text/html", "utf-8", null);

    }

    private String getHtml() {

        String html = "<p class=\"font_8\">Last Revised: February 10, 2017</p>\n" +
                "<p class=\"font_8\">SK Mobile Development (“Company”, “we”, “our” or “us”) respects the privacy of the users of our mobile application (“App”), and is fully committed to protect the personal information that users share with it in connection with the use of our App.</p>\n" +
                "\n" +
                "<h4>INFORMATION ABOUT PERMISSIONS</h4>\n" +
                "<em>- Read Contacts:\n" +
                "</em>We need this permission to access you phone contacts so that you can choose a contact and create a shortcut with it. We do not collect nor store this information.\n" +
                "\n" +
                "<em>- Call Phone:\n" +
                "</em>Using SpeedDial App, you can directly call the phone number you have added as shortcut. Also you can send a text message to this specific number. Non of these phone numbers or message content is stored, sent or used anywhere else.\n" +
                "<h4>PERSONAL INFORMATION</h4>\n" +
                "Personal information is data that can be used to uniquely identify or contact a single person. We DO NOT collect, send, store or use any personal information while you us the SpeedDial App.\n" +
                "<h4>NON-PERSONAL INFORMATION</h4>\n" +
                "Non-personal information is data in a form that does not permit direct association with any specific individual, such as your Android ID, CPN model, memory size, your phone IMEI number, phone model, rom, phone operator, location, install, uninstall, frequency of use, etc. We may collect and use non-personal information in the following circumstances. To have a better understanding in user’s behavior, solve problems in products and services, improve our products, services and advertising, we may collect non-personal information such as the data of install, frequency of use, country, equipment and channel. If non-personal information is combined with personal information, we treat the combined information as personal information for the purposes of this Privacy Policy.\n" +
                "<h4>GOOGLE AND THIRD-PARTY ADVERTISERS</h4>\n" +
                "No advertisement.\n" +
                "<h4 class=\"grayText\">THIRD-PARTY DISCLOSURE</h4>\n" +
                "<div class=\"innerText\">We do not sell, trade, or otherwise transfer to outside parties your personally identifiable information.</div>\n" +
                "<h4>THIRD-PARTY LINKS</h4>\n" +
                "<div class=\"innerText\">We do not include or offer third party products or services on our website.</div>\n" +
                "<h4>PRIVACY QUESTIONS</h4>\n" +
                "If you have any questions or concerns about our Privacy Policy or data processing, please <a href=\"mailto:action.jackson187@gmail.com\">contact us</a> We may update Privacy Policy from time to time. When we change the policy in a material way, a notice will be posted on our website along with the updated Privacy Policy.";

         return html;
    }

}
