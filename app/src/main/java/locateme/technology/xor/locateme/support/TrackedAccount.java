package locateme.technology.xor.locateme.support;

public class TrackedAccount {

    public String trackedId;
    public String hashedSecret;
    public String nickname;
    public boolean isTracked;

    public TrackedAccount(String trackedId, String hashedSecret, String nickname, boolean isTracked) {
        this.trackedId = trackedId;
        this.hashedSecret = hashedSecret;
        this.nickname = nickname;
        this.isTracked = isTracked;
    }
}
