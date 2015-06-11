/**
 * 
 */
package com.fernsroth.squashfs.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public class Directory extends BaseFile {

    /**
     * subentries.
     */
    private List<BaseFile> subentries = new ArrayList<BaseFile>();

    /**
     * constructor. 
     * @param name the name of the file.
     * @param mode the mode.
     * @param mTime the modify time.
     * @param guid the group id.
     * @param uid the user id.
     */
    public Directory(String name, int mode, long mTime, long guid, long uid) {
        super(name, mode, mTime, guid, uid);
    }

    /**
     * adds a subentry.
     * @param subentry the subentry to add.
     */
    public void addSubentry(BaseFile subentry) {
        this.subentries.add(subentry);
    }

    /**
     * @return the subentries
     */
    public List<BaseFile> getSubentries() {
        return this.subentries;
    }

}
