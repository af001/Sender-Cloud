package locateme.technology.xor.locateme;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.parse.ParseUser;

import locateme.technology.xor.locateme.mains.LoginActivity;
import locateme.technology.xor.locateme.mains.MapsActivity;
import locateme.technology.xor.locateme.support.AppData;

public class AppController extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            LoginToApp();
        } else {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivityForResult(loginIntent, AppData.REQUEST_PARSE_LOGIN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == RESULT_OK){
            LoginToApp();
        } else if (resultCode == RESULT_CANCELED) {
            finish();
        }
    }

    private void LoginToApp() {
        Intent mapIntent = new Intent(this, MapsActivity.class);
        startActivity(mapIntent);
        finish();
    }
}
