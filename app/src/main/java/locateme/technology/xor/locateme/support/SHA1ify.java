package locateme.technology.xor.locateme.support;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import timber.log.Timber;

public class SHA1ify {
    public String SHA1(String text) {

        try {
            MessageDigest md;
            md = MessageDigest.getInstance("SHA-1");
            md.update(text.getBytes("UTF-8"),
                    0, text.length());
            byte[] sha1hash = md.digest();

            return toHex(sha1hash);

        } catch (NoSuchAlgorithmException e) {
            Timber.e("No such algorithm: SHA1", e.getMessage());
        } catch (UnsupportedEncodingException e) {
            Timber.e("Unsupported encoding: SHA1", e.getMessage());
        }

        return null;
    }

    public String toHex(byte[] buf) {

        if (buf == null) return "";

        int l = buf.length;
        StringBuffer result = new StringBuffer(2 * l);

        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }

        return result.toString();

    }

    private void appendHex(StringBuffer sb, byte b) {

        sb.append(AppData.HEX.charAt((b >> 4) & 0x0f))
                .append(AppData.HEX.charAt(b & 0x0f));

    }
}
