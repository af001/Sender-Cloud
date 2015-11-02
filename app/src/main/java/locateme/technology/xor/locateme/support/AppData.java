package locateme.technology.xor.locateme.support;

import com.parse.ParseACL;
import com.parse.ParseUser;

public abstract class AppData {

    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    public static final int RC_BARCODE_CAPTURE = 9001;

    public static final int REQUEST_NICKNAME = 2400;

    public static final int REQUEST_PARSE_LOGIN = 1;

    public static final int PARSE_ACCOUNT_EXISTS = 202;

    public final static int WHITE = 0xFFFFFFFF;

    public final static int BLACK = 0xFF000000;

    public final static int WIDTH = 400;

    public final static int HEIGHT = 400;

    public final static boolean AUTO_FOCUS = true;

    public final static boolean USE_FLASH = false;

    public final static String CLOUD_FUNCTION = "sendPushToUser";

    public final static String HEX = "0123456789ABCDEF";

    public final static int WRITE_SUCCESS = 1;

    public final static int WRITE_FAIL = 0;

    public final static int ZOOM_CONTROL = 0x1;

    public final static int MY_LOCATION = 0x2;

    public final static int MAP_CONTROL = 0x4;
}
