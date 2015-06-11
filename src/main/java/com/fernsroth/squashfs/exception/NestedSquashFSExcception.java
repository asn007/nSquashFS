/**
 * 
 */
package com.fernsroth.squashfs.exception;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public class NestedSquashFSExcception extends SquashFSException {

    /**
     * serialization.
     */
    private static final long serialVersionUID = 6171245459426255804L;

    /**
     * constructor.
     * @param message the message.
     * @param cause the cause.
     */
    public NestedSquashFSExcception(String message, Throwable cause) {
        super(message, cause);
    }

}
