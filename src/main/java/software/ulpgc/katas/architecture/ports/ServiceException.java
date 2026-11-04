package software.ulpgc.katas.architecture.ports;

/** Errores comprensibles para la aplicación, sin exponer tipos de la librería HTTP. */
public final class ServiceException extends Exception {
    public enum Kind { NETWORK, HTTP_STATUS, INVALID_RESPONSE }

    private final Kind kind;
    private final int statusCode;

    private ServiceException(Kind kind, int statusCode, Throwable cause) {
        super(kind.toString(), cause);
        this.kind = kind;
        this.statusCode = statusCode;
    }

    public static ServiceException network(Throwable cause) {
        return new ServiceException(Kind.NETWORK, -1, cause);
    }

    public static ServiceException httpStatus(int statusCode) {
        return new ServiceException(Kind.HTTP_STATUS, statusCode, null);
    }

    public static ServiceException invalidResponse(Throwable cause) {
        return new ServiceException(Kind.INVALID_RESPONSE, -1, cause);
    }

    public Kind kind() {
        return kind;
    }

    public int statusCode() {
        return statusCode;
    }
}
