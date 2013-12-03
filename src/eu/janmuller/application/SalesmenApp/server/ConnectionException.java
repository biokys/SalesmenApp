package eu.janmuller.application.salesmenapp.server;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 02.12.13
 * Time: 15:58
 */
public class ConnectionException extends Exception {

    private String mMessage;

    public ConnectionException(String message) {

        mMessage = message;
    }

    public String getMessage() {

        return mMessage;
    }
}
