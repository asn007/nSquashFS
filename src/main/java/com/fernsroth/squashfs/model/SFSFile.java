/**
 * 
 */
package com.fernsroth.squashfs.model;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public abstract class SFSFile extends BaseFile {

    /**
     * @param name
     * @param mode
     * @param time
     * @param guid
     * @param uid
     */
    public SFSFile(String name, int mode, long time, long guid, long uid) {
        super(name, mode, time, guid, uid);
    }
}
