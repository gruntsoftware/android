package ltd.grunt.brainwallet.exceptions;

public class BRKeystoreErrorException extends Exception {
    public static final String TAG = BRKeystoreErrorException.class.getName();

    public BRKeystoreErrorException(String message) {
        super(message);
    }
}
