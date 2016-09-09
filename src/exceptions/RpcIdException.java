package exceptions;

/**
 * Created by dylanaird on 9/09/2016.
 */
public class RpcIdException extends Exception {
    public RpcIdException(String msg){
        super("Rpc Id is incorrect: " + msg);
    }
}
