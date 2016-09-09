package exceptions;

/**
 * Created by dylanaird on 9/09/2016.
 */
public class StatusException extends Exception {
    public StatusException(String msg){
        super(""+msg);
    }
}
