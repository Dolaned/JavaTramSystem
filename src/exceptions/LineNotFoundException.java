package exceptions;

/**
 * Created by dylanaird on 9/09/2016.
 */
public class LineNotFoundException extends Exception {
    public LineNotFoundException(String msg){
        super(""+ msg);
    }
}
