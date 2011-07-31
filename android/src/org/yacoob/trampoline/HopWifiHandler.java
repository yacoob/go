package org.yacoob.trampoline;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

/**
 * BroadcastReceiver handling wifi network states. It's supposed to make sure refresh service is not
 * running outside of home network.
 */
public class HopWifiHandler extends BroadcastReceiver {

    @Override
    public final void onReceive(final Context context, final Intent intent) {
        if (intent != null) {
            final NetworkInfo info = (NetworkInfo) intent
                    .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                final Hop app = (Hop) context.getApplicationContext();
                final Boolean isConnected = info.isConnected();
                final Boolean atHome = app.onHomeNetwork();
                if (isConnected && atHome) {
                    Hop.debug("Would start refresh service");
                } else {
                    Hop.debug("Would stop refresh service");
                }
            }
        }
    }
}
