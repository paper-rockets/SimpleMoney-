package io.github.paperrockets.simplemoney;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsServiceConnection;
import androidx.browser.customtabs.CustomTabsSession;
import androidx.browser.trusted.TrustedWebActivityIntentBuilder;

public class MainActivity extends Activity {
    private static final String CHROME_PACKAGE = "com.android.chrome";
    private static final Uri MONEY_URL = Uri.parse("https://paper-rockets.github.io/SimpleMoney-/");
    private CustomTabsServiceConnection connection;
    private boolean bound;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        Uri target = MONEY_URL;
        connection = new CustomTabsServiceConnection() {
            @Override public void onCustomTabsServiceConnected(ComponentName name, CustomTabsClient client) {
                client.warmup(0);
                CustomTabsSession session = client.newSession(null);
                if (session == null) {
                    openBrowser(target);
                    return;
                }
                try {
                    new TrustedWebActivityIntentBuilder(target)
                            .build(session)
                            .launchTrustedWebActivity(MainActivity.this);
                    finish();
                } catch (ActivityNotFoundException error) {
                    openBrowser(target);
                }
            }

            @Override public void onServiceDisconnected(ComponentName name) {
                bound = false;
            }
        };
        bound = CustomTabsClient.bindCustomTabsService(this, CHROME_PACKAGE, connection);
        if (!bound) openBrowser(target);
    }

    private void openBrowser(Uri target) {
        Intent browser = new Intent(Intent.ACTION_VIEW, target);
        browser.addCategory(Intent.CATEGORY_BROWSABLE);
        try {
            startActivity(browser);
        } catch (ActivityNotFoundException missing) {
            Toast.makeText(this, "Install a browser to open SimpleMoney", Toast.LENGTH_LONG).show();
        }
        finish();
    }

    @Override protected void onDestroy() {
        if (bound) unbindService(connection);
        super.onDestroy();
    }
}
