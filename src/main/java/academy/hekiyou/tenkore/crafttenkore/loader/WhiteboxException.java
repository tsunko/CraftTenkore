package academy.hekiyou.tenkore.crafttenkore.loader;

public class WhiteboxException extends RuntimeException {

    public WhiteboxException(){
        super();
    }
    
    public WhiteboxException(String message, Throwable cause){
        super(message, cause);
    }
    
    public WhiteboxException(String message){
        super(message);
    }
    
    public WhiteboxException(Throwable cause){
        super(cause);
    }

}
