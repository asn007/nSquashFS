/**
 * 
 */
package com.fernsroth.squashfs.model;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public class SymLink extends BaseFile {

    /**
     * link name.
     */
    private String linkName;

    /**
     * constructor. 
     * @param name the name of the file.
     * @param mode the mode.
     * @param mTime the modify time.
     * @param guid the group id.
     * @param uid the user id.
     * @param linkName the linked file. 
     */
    public SymLink(String name, int mode, long mTime, long guid, long uid,
            String linkName) {
        super(name, mode, mTime, guid, uid);
        this.linkName = linkName;
    }

    /**
     * @return the linkName
     */
    public String getLinkName() {
        return this.linkName;
    }
}
