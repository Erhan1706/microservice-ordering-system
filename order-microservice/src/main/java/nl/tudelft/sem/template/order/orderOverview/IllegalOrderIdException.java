package nl.tudelft.sem.template.order.orderOverview;

public class IllegalOrderIdException extends Exception{

    public IllegalOrderIdException(){
        super("The order you are trying to cancel is not yours!");
    }
}
