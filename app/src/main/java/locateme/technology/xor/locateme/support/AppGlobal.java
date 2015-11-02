package locateme.technology.xor.locateme.support;


import android.app.Application;
import android.support.annotation.Nullable;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseInstallation;
import com.parse.ParsePush;

import io.fabric.sdk.android.Fabric;
import locateme.technology.xor.locateme.BuildConfig;
import timber.log.Timber;

public class AppGlobal extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        CrashlyticsCore core = new CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build();
        Fabric.with(this, new Crashlytics.Builder().core(core).build());

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        Timber.plant(new CrashlyticsTree());

        // ENABLE LOCAL DATASTORE
        Parse.enableLocalDatastore(this);

        // INITIALIZE PARSE
        Parse.initialize(this, "wlNMu5YPJ1u8Cqv6evoRLXCOl6vzd74y2KKMMEeC", "lvWza9SnJO8OLF6rnfxmJ9M3BoHaf62KdBdZ5LAy");

        // SAVE CURRENT INSTALLATION TO PARSE
        ParseInstallation.getCurrentInstallation().saveInBackground();

        // DEFAULT SUBSCRIPTION
        ParsePush.subscribeInBackground("LocateMe");

        // SET THE DEFAULT ACL
        ParseACL.setDefaultACL(new ParseACL(), true);
    }

    public class CrashlyticsTree extends Timber.Tree {
        private static final String CRASHLYTICS_KEY_PRIORITY = "priority";
        private static final String CRASHLYTICS_KEY_TAG = "tag";
        private static final String CRASHLYTICS_KEY_MESSAGE = "message";

        @Override
        protected void log(int priority, @Nullable String tag, @Nullable String message, @Nullable Throwable t) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
                return;
            }

            Crashlytics.setInt(CRASHLYTICS_KEY_PRIORITY, priority);
            Crashlytics.setString(CRASHLYTICS_KEY_TAG, tag);
            Crashlytics.setString(CRASHLYTICS_KEY_MESSAGE, message);

            if (t == null) {
                Crashlytics.logException(new Exception(message));
            } else {
                Crashlytics.logException(t);
            }
        }
    }
}
