package locateme.technology.xor.locateme.parse;

import com.parse.FindCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import timber.log.Timber;

public class ParseMethods {

    public ParseACL SetRestrictedACL(ParseUser user) {
        ParseACL acl = new ParseACL();
        acl.setPublicReadAccess(false);
        acl.setPublicWriteAccess(false);
        acl.setReadAccess(user, true);
        acl.setWriteAccess(user, true);
        return acl;
    }

    public void BecomeSession() {
        ParseUser.becomeInBackground(ParseUser.getCurrentUser().getSessionToken());
        ParseUser user = ParseUser.getCurrentUser();
        user.setACL(SetRestrictedACL(user));
        user.saveEventually();
    }

    private ParseACL SetUserRestriction(String trackerId, String trackedId) {
        ParseACL acl = new ParseACL();
        acl.setPublicReadAccess(false);
        acl.setPublicWriteAccess(false);
        acl.setReadAccess(trackerId, true);
        acl.setReadAccess(trackedId, true);
        acl.setWriteAccess(trackerId, true);
        acl.setWriteAccess(trackedId, true);
        return acl;
    }

    public void AddUserToInstallation() {
        ParseInstallation parseInstallation = ParseInstallation.getCurrentInstallation();
        parseInstallation.put("user", ParseUser.getCurrentUser());
        parseInstallation.setACL(SetRestrictedACL(ParseUser.getCurrentUser()));
        parseInstallation.saveEventually();
    }

    public void BackupUserLocation(Double latitude, Double longitude, String trackerId) {
        ParseObject location = new ParseObject("OldTracks");
        ParseGeoPoint point = new ParseGeoPoint(latitude, longitude);
        location.put("trackerId", trackerId);
        location.put("trackedId", ParseUser.getCurrentUser().getObjectId());
        location.put("grid", point);
        location.setACL(SetUserRestriction(trackerId, ParseUser.getCurrentUser().getObjectId()));
        location.saveEventually();
    }

    public void UpdateLocation(Double latitude, Double longitude, final String tracker, final boolean isDelete) {
        final ParseGeoPoint point = new ParseGeoPoint(latitude, longitude);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");
        if (isDelete) {
            query.whereEqualTo("trackerId", ParseUser.getCurrentUser().getObjectId());
            query.whereEqualTo("trackedId", tracker);
        } else {
            query.whereEqualTo("trackerId", tracker);
            query.whereEqualTo("trackedId", ParseUser.getCurrentUser().getObjectId());
        }
        query.setLimit(1);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> results, ParseException e) {
                if (e == null) {
                    if (results.size() == 1) {
                        for (ParseObject result : results) {
                            if (isDelete) {
                                result.unpinInBackground();
                                result.deleteEventually();
                            } else {
                                result.put("grid", point);
                                result.saveEventually();
                            }
                        }
                    } else {
                        if (!isDelete) {
                            ParseObject location = new ParseObject("Location");
                            location.put("trackerId", tracker);
                            location.put("trackedId", ParseUser.getCurrentUser().getObjectId());
                            location.put("grid", point);
                            location.setACL(SetUserRestriction(tracker, ParseUser.getCurrentUser().getObjectId()));
                            location.saveEventually();
                        }
                    }
                } else {
                    Timber.e("UpdateLocation", "Failed to update user location.");
                }
            }
        });
    }

    public void RemoveAccount(String nickname) {
        ParseQuery<ParseObject> parseObject = new ParseQuery<ParseObject>("AccessList");
        parseObject.whereEqualTo("nickname", nickname);
        parseObject.setLimit(1);
        parseObject.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    for (ParseObject object : objects) {
                        UpdateLocation(0.0, 0.0, object.getString("trackedId"), true);
                        object.deleteEventually();
                    }
                } else {
                    Timber.e("RemoveAccount", "Error removing user account.");
                }
            }
        });
    }
}
