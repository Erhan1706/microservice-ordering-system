package nl.tudelft.sem.template.order.order.overview;

public class IllegalOrderIdException extends Exception {

    private static final long serialVersionUID = -5227647556678415532L;

    public IllegalOrderIdException() {
        super("The order you are trying to cancel is not yours!");
    }
}
