package org.yacoob.trampoline;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;

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
                final Intent i = new Intent(context, HopRefreshService.class);
                final SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(context);
                // FIXME: be more specific here, looks like we're getting called >1 time with
                // isConnected = true. Most likely it's "connected to network" and then "got an IP".
                if (isConnected && atHome && prefs.getBoolean("refreshLists", false)) {
                    Hop.debug("On home network - restarting refresh service.");
                    i.setAction(HopRefreshService.RESTART_REFRESH_ACTION);

                } else {
                    Hop.debug("Not on home network - killing refresh service.");
                    i.setAction(HopRefreshService.CANCEL_REFRESH_ACTION);
                }
                context.startService(i);
            }
        }
    }
}
