package locateme.technology.xor.locateme.parse;

import android.content.Context;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

public class ParseRelationship {

    private String tracker;
    private String tracked;

    public void AddRelationship(final String tracker, final String tracked, final String hashedSecret,
                                final String nickname, final Context context) {

        this.tracker = tracker;
        this.tracked = tracked;

        ParseQuery<ParseObject> unique = new ParseQuery<ParseObject>("AccessList");
        unique.whereEqualTo("trackerId", tracker);
        unique.whereEqualTo("trackedId", tracked);
        unique.whereEqualTo("hashedSecret", hashedSecret);
        unique.setLimit(1);
        unique.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {
                        Toast.makeText(context, "Account already exists!", Toast.LENGTH_SHORT).show();
                    } else {
                        ParseObject relationship = new ParseObject("AccessList");
                        relationship.put("trackerId", tracker);
                        relationship.put("trackedId", tracked);
                        relationship.put("hashedSecret", hashedSecret);
                        relationship.put("nickname", nickname);
                        relationship.put("isTracked", true);
                        relationship.setACL(SetACL());
                        relationship.pinInBackground();
                        relationship.saveInBackground();
                    }
                }
            }
        });
    }

    private ParseACL SetACL() {
        ParseACL acl = new ParseACL();
        acl.setPublicReadAccess(false);
        acl.setPublicWriteAccess(false);
        acl.setReadAccess(tracker, true);
        acl.setReadAccess(tracked, true);
        acl.setWriteAccess(tracker, true);
        acl.setWriteAccess(tracked, true);
        return acl;
    }
}
