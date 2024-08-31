package nl.tudelft.sem.template.authentication.domain.user.services;

import nl.tudelft.sem.template.authentication.domain.user.valueobjects.NetId;

/**
 * Exception to indicate the NetID is already in use.
 */
public class NetIdAlreadyInUseException extends Exception {
    static final long serialVersionUID = -3387516993124229948L;
    
    public NetIdAlreadyInUseException(NetId netId) {
        super("The netId: " + netId.toString() + " is already used");
    }
}
