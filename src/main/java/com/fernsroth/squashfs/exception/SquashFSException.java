/**
 * 
 */
package com.fernsroth.squashfs.exception;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public class SquashFSException extends Exception {

    /**
     * serialization.
     */
    private static final long serialVersionUID = -1413430130745668442L;

    /**
     * constructor.
     * @param message the message.
     * @param cause the cause.
     */
    public SquashFSException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * constructor.
     * @param message the message.
     */
    public SquashFSException(String message) {
        super(message);
    }

}
