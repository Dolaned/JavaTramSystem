package exceptions;

/**
 * Created by dylanaird on 9/09/2016.
 */
public class TransactionException extends Exception {
    public TransactionException(String msg) {
        super("" + msg);
    }
}
