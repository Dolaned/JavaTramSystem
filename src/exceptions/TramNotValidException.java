package exceptions;

/**
 * Created by dylanaird on 9/09/2016.
 */
public class TramNotValidException extends Exception {
    public TramNotValidException(String msg){
        super(""+msg);
    }
}
