package exceptions;

/**
 * Created by dylanaird on 9/09/2016.
 */
public class MessageTypeException extends Exception {
    public MessageTypeException(String msg){
        super(""+msg);
    }
}
