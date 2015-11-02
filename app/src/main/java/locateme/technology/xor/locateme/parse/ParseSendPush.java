package locateme.technology.xor.locateme.parse;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import java.util.HashMap;

import locateme.technology.xor.locateme.support.AppData;
import timber.log.Timber;

public class ParseSendPush {

    public void LocateUser() {

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("senderId", ParseUser.getCurrentUser().getObjectId());
        params.put("message", ParseInstallation.getCurrentInstallation().getInstallationId());

        ParseCloud.callFunctionInBackground(AppData.CLOUD_FUNCTION, params, new FunctionCallback<String>() {

            public void done(String success, ParseException e) {
                if (e != null) {
                    Timber.e("Error sending push message!", e.getCode());
                }
            }
        });
    }
}
