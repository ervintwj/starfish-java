package sg.dex.starfish.exception;

public class StarfishValidationException extends RuntimeException {

    public StarfishValidationException(String message) {
        super(message);
    }

    public StarfishValidationException(String message, Throwable e) {
        super(message, e);
    }


}

