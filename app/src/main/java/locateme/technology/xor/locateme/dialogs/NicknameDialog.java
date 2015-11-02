package locateme.technology.xor.locateme.dialogs;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import locateme.technology.xor.locateme.support.AppData;
import locateme.technology.xor.locateme.R;

/**
 * NicnameDialog - Used to receive a nickname to be associated with a new account.
 */
public class NicknameDialog extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank);
        AlertUser();
    }

    /**
     * AlertUser - Display an alert dialog to the user requesting a nickname.
     */
    public void AlertUser() {

        AlertDialog.Builder builder = new AlertDialog.Builder(NicknameDialog.this, R.style.AlertDialogStyle);
        builder.setTitle("Account Nickname");
        builder.setMessage("Set a nickname to be associated with this account.");
        // builder.setIcon(R.id.xxxx)

        final int maxLength = 20;
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(maxLength);

        final LinearLayout layout = new LinearLayout(NicknameDialog.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(75, 75, 75, 75);

        TextView tv = new TextView(this);
        tv.setTextSize(16);
        tv.setTextColor(getResources().getColor(R.color.text));
        tv.setText("Account Nickname");
        layout.addView(tv);

        final EditText label = new EditText(NicknameDialog.this);
        label.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        label.setFilters(fArray);
        label.setTextColor(getResources().getColor(R.color.text));
        label.setHintTextColor(getResources().getColor(R.color.text));
        label.setHint("Set nickname...");
        layout.addView(label);

        builder.setView(layout);
        builder.setPositiveButton("Save",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (label.getText().toString().equals("")) {
                            AlertUser();
                            label.setError("Empty nicknames are not allowed!");
                        } else {
                            WriteStatus(AppData.WRITE_SUCCESS, label.getText().toString());
                            Intent returnIntent = new Intent();
                            setResult(RESULT_OK, returnIntent);
                            finish();
                        }
                    }
                });

        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        WriteStatus(AppData.WRITE_FAIL, null);
                        Intent returnIntent = new Intent();
                        setResult(RESULT_CANCELED, returnIntent);
                        finish();
                    }
                });

        label.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                label.setError(null);
                label.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.show();
    }

    /**
     * WriteStatus - Write the results of the alert dialog to SharedPreferences so that they can be
     * read in the calling Activity.
     * @param saved
     * @param nickname
     */
    private void WriteStatus(int saved, String nickname) {

        SharedPreferences sharedPref = getSharedPreferences("locate_me", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("saved", saved);
        editor.putString("nickname", nickname);

        editor.apply();
    }
}


