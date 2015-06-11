/**
 * 
 */
package com.fernsroth.squashfs;

import java.util.Date;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public class DefaultSquashFSSystem implements SquashFSSystem {

    /**
     * {@inheritDoc}
     */
    public Date getCurrentDate() {
        return new Date();
    }

}
