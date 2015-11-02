package locateme.technology.xor.locateme.mains;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import locateme.technology.xor.barcodereader.BarcodeCaptureActivity;
import locateme.technology.xor.locateme.R;
import locateme.technology.xor.locateme.dialogs.NicknameDialog;
import locateme.technology.xor.locateme.parse.ParseMethods;
import locateme.technology.xor.locateme.parse.ParseRelationship;
import locateme.technology.xor.locateme.support.AccountsAdapter;
import locateme.technology.xor.locateme.support.AppData;
import locateme.technology.xor.locateme.support.SHA1ify;
import locateme.technology.xor.locateme.support.TrackedAccount;
import timber.log.Timber;

public class AccountsActivity extends AppCompatActivity {

    private AccountsAdapter mAdapter;
    private ListView listView;
    private TextView emptyList;
    private TextView header;
    private SharedPreferences sharedPref;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_list);

        sharedPref = getSharedPreferences("locate_me", Context.MODE_PRIVATE);

        listView = (ListView) findViewById(R.id.lv_list);
        emptyList = (TextView) findViewById(R.id.tv_empty_list);
        header = (TextView) findViewById(R.id.tv_header);
        progressBar = (ProgressBar) findViewById(R.id.pb_refresh);
        listView.setLongClickable(true);

        progressBar.setVisibility(View.VISIBLE);
        new BackgroundTask().execute();
    }

    /**
     * FetchAccounts - Query for accounts that are being tracked by the handset. Once complete,
     * make them visible on in the list. If no devices are visible, tell the user that there aren't
     * any devices to track.
     */
    private void FetchAccounts() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("AccessList");
        query.whereEqualTo("trackerId", ParseUser.getCurrentUser().getObjectId());
        query.findInBackground(new FindCallback<ParseObject>() {

            public void done(List<ParseObject> accountList, ParseException e) {
                if (e == null) {
                    if (accountList.size() > 0) {

                        ArrayList<TrackedAccount> trackedAccounts = new ArrayList<TrackedAccount>();
                        for (ParseObject account : accountList) {
                            TrackedAccount mAccount = new TrackedAccount(account.getObjectId(),
                                    account.getString("hashedSecret"), account.getString("nickname"),
                                    account.getBoolean("isTracked"));
                            trackedAccounts.add(mAccount);
                        }
                        mAdapter = new AccountsAdapter(AccountsActivity.this, trackedAccounts);
                        header.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.VISIBLE);
                        emptyList.setVisibility(View.GONE);
                        listView.setAdapter(mAdapter);

                        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                            @Override
                            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                                TrackedAccount trackedAccount = (TrackedAccount) parent.getItemAtPosition(position);
                                AlertUser(trackedAccount.nickname);

                                return true;
                            }
                        });
                    } else {
                        header.setVisibility(View.GONE);
                        listView.setVisibility(View.GONE);
                        emptyList.setVisibility(View.VISIBLE);
                    }
                } else {
                    Timber.e("Error returned form access list query.", e.getMessage());
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter != null) {
            mAdapter.clear();
            header.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            new BackgroundTask().execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.account_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // HANDLE ITEM SELECTION
        switch (item.getItemId()) {
            case R.id.system_exit:
                finish();
                return true;
            case R.id.add_account:
                Intent intent = new Intent(AccountsActivity.this, BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, AppData.AUTO_FOCUS);
                intent.putExtra(BarcodeCaptureActivity.UseFlash, AppData.USE_FLASH);
                startActivityForResult(intent, AppData.RC_BARCODE_CAPTURE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AppData.RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    SharedPreferences.Editor editor = sharedPref.edit();

                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    editor.putString("barcode", barcode.displayValue);
                    editor.apply();

                    Intent nicknameIntent = new Intent(AccountsActivity.this, NicknameDialog.class);
                    startActivityForResult(nicknameIntent, AppData.REQUEST_NICKNAME);

                } else {
                    Timber.d("Barcode", "Failed to capture barcode.");
                }
            } else {
                Timber.e("Error from barcode scan.", String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        } else if (requestCode == AppData.REQUEST_NICKNAME) {
            if (resultCode == RESULT_OK) {
                AddRelationship();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void AddRelationship() {
        String nickname = CheckResults();

        if (nickname != null) {
            String value = sharedPref.getString("barcode", null);
            ParseRelationship setRelationship = new ParseRelationship();
            setRelationship.AddRelationship(ParseUser.getCurrentUser().getObjectId(),
                    ParseBarcodeSerial(value), ParseBarcodeSecret(value), nickname, getBaseContext());

            if (mAdapter != null) {
                mAdapter.clear();
            } else {
                header.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                new BackgroundTask().execute();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private String ParseBarcodeSerial(String barcode) {
        String[] separated = barcode.split(":");
        return separated[0];
    }

    private String ParseBarcodeSecret(String barcode) {
        SHA1ify hashSecret = new SHA1ify();
        String[] separated = barcode.split(":");
        return hashSecret.SHA1(separated[1]);
    }

    private String CheckResults() {
        String nickname = null;
        SharedPreferences.Editor editor = sharedPref.edit();
        int success = sharedPref.getInt("saved", 0);

        if (success == AppData.WRITE_SUCCESS) {
            nickname = sharedPref.getString("nickname", null);
            editor.putString("nickname", null);
        }

        editor.putInt("saved", 0);
        editor.apply();

        return nickname;
    }

    private void AlertUser(final String nickname) {

        AlertDialog.Builder builder = new AlertDialog.Builder(AccountsActivity.this, R.style.AlertDialogStyle);
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete this account?");

        final LinearLayout layout = new LinearLayout(AccountsActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(75, 75, 75, 75);

        builder.setView(layout);
        builder.setPositiveButton("Confirm",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ParseMethods parseMethods = new ParseMethods();
                        parseMethods.RemoveAccount(nickname);

                        if (mAdapter != null) {
                            mAdapter.clear();
                            header.setVisibility(View.GONE);
                            progressBar.setVisibility(View.VISIBLE);
                            new BackgroundTask().execute();
                        }
                        dialog.dismiss();
                    }
                });

        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.show();
    }

    public class BackgroundTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            progressBar.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in));
        }

        @Override
        protected void onPostExecute(Void result) {
            progressBar.setVisibility(View.GONE);
            progressBar.clearAnimation();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(3000);
                FetchAccounts();
            } catch (InterruptedException e) {
                Timber.e("MapsActivity", "Error querying map data.");
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressBar.setProgress(values[0]);
        }
    }
}
