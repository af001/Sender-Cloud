package locateme.technology.xor.locateme.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseUser;

import locateme.technology.xor.locateme.R;

public class VerifyDialog {

    /**
     * AlertUser - Used to notify the user that their email has not been verified. Disables buttons
     * and general functionality until the user uses a valid email. Provide the option for the user
     * to enter a new email if they entered a fake one.
     * @param context
     */
    public void AlertUser(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogStyle);
        builder.setTitle("Verify Account");
        builder.setMessage("Please verify your email address. " +
                "A verification link was sent to the registred email at signup. " +
                "It may be necessary to check your spam folder if you failed to " +
                "receive the verification email in your inbox.");

        final int maxLength = 25;
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(maxLength);

        final LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(75, 75, 75, 75);

        TextView tv = new TextView(context);
        tv.setTextSize(16);
        tv.setTextColor(context.getResources().getColor(R.color.text));
        tv.setText("Register a different email?");
        layout.addView(tv);

        final EditText label = new EditText(context);
        label.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        label.setFilters(fArray);
        label.setTextColor(context.getResources().getColor(R.color.text));
        label.setHintTextColor(context.getResources().getColor(R.color.text));
        label.setHint("Enter email...");
        layout.addView(label);

        builder.setView(layout);
        builder.setPositiveButton("Send",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {

                        if (label.getText().toString().equals("")) {
                            AlertUser(context);
                            label.setError("Empty emails are not allowed!");
                        } else {
                            if (IsValidEmail(label.getText().toString())) {
                                ParseUser user = ParseUser.getCurrentUser();
                                user.setEmail(label.getText().toString());
                                user.saveEventually();

                                Toast.makeText(context, "Please verify your new email and login to Sender.", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else {
                                AlertUser(context);
                                label.setError("Invalid email!");
                            }
                        }
                    }
                });

        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Toast.makeText(context, "You must validate your email before using Sender", Toast.LENGTH_SHORT).show();
                    }
                });

        builder.setNeutralButton("Resend",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ParseUser user = ParseUser.getCurrentUser();
                        String email = user.getEmail();
                        user.setEmail("bob999909@grr.la");
                        user.saveEventually();
                        user.setEmail(email);
                        user.saveEventually();
                        Toast.makeText(context, "A new confirmation email has been sent!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
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
     * IsValidEmail - Validate the user's input in the event they wish to enter a new email.
     * @param email
     * @return
     */
    private boolean IsValidEmail(String email) {
        CharSequence target = email;
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}
