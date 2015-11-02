package locateme.technology.xor.locateme.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.parse.ParseUser;

import locateme.technology.xor.locateme.support.AppData;
import locateme.technology.xor.locateme.R;
import timber.log.Timber;

public class QrcodeDialog {

    /**
     * AlertUser - Used to display the SHA1 hash of the user's device in the form of a QRCode. This
     * is used to synchronize devices and allow remote tracking.
     * @param context
     */
    public void AlertUser(final Context context) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogStyle);
        builder.setTitle("Synchronize Account");
        builder.setMessage("Scan the barcode using the scanner function from the device that will " +
                "be tracking this account.");

        final LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(75, 75, 75, 75);

        final ImageView qrcode = new ImageView(context);
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String qrcodify = ParseUser.getCurrentUser().getObjectId() + ":" + tm.getDeviceId();
            Bitmap bitmap = encodeAsBitmap(qrcodify);
            qrcode.setImageBitmap(bitmap);
         } catch (WriterException e) {
            Timber.e("Error encoding qrcode to bitmap.", e.getMessage());
        }
        layout.addView(qrcode);

        builder.setView(layout);
        builder.setPositiveButton("Done",
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

    /**
     * encodeAsBitmap - Used to take a string and convert it to a QRCode in the form of a Bitmap.
     * This image is displayed to the user in an alert dialog.
     * @param str
     * @return
     * @throws WriterException
     */
    private Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, AppData.WIDTH, AppData.HEIGHT, null);
        } catch (IllegalArgumentException iae) {
            Timber.e("Error encoding bitmap. Unsupported Format?", iae);
            return null;
        }

        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? AppData.BLACK : AppData.WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }
}

