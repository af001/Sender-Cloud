package locateme.technology.xor.locateme.parse;

import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import locateme.technology.xor.locateme.support.GPSTracker;
import locateme.technology.xor.locateme.support.SHA1ify;
import timber.log.Timber;

public class ParseReceiver extends ParsePushBroadcastReceiver {

    private GPSTracker gpsTracker;

    public ParseReceiver() {
        super();
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        gpsTracker = new GPSTracker(context);
        gpsTracker.getLocation();

        if (intent == null)
            return;

        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            String message = json.getString("alert");
            parsePushJson(context, message);
        } catch (JSONException e) {
            Timber.e("Push message json exception.", e.getMessage());
        }
    }

    @Override
    protected void onPushDismiss(Context context, Intent intent) {
        super.onPushDismiss(context, intent);
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        super.onPushOpen(context, intent);
    }

    private void parsePushJson(Context context, String message) {
            String details[] = message.split(":");

            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            SHA1ify sha1ify = new SHA1ify();

            if (details[0].equals(sha1ify.SHA1(tm.getDeviceId()))) {
                double currentLatitude = gpsTracker.getLatitude();
                double currentLongitude = gpsTracker.getLongitude();
                ParseMethods mParseMethod = new ParseMethods();
                mParseMethod.UpdateLocation(currentLatitude, currentLongitude, details[1], false);
                mParseMethod.BackupUserLocation(currentLatitude, currentLongitude, details[1]);
            }
    }
}
