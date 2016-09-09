package exceptions;

/**
 * Created by dylanaird on 5/09/2016.
 */
public class ProcedureException extends Exception {

    public ProcedureException(String msg) {
        super("" + msg);
    }
}
