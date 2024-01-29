package http.exception;

public class NotFoundFreemarkerExtensionConfigException extends RuntimeException {

    public NotFoundFreemarkerExtensionConfigException() {
    }

    public NotFoundFreemarkerExtensionConfigException(String message) {
        super(message);
    }

    public NotFoundFreemarkerExtensionConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundFreemarkerExtensionConfigException(Throwable cause) {
        super(cause);
    }

    public NotFoundFreemarkerExtensionConfigException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
