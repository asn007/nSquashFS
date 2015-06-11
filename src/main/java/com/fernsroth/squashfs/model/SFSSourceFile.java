/**
 * 
 */
package com.fernsroth.squashfs.model;

import java.io.File;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public class SFSSourceFile extends SFSFile {

    /**
     * the source file.
     */
    private File sourceFile;

    /**
     * @param name
     * @param mode
     * @param time
     * @param guid
     * @param uid
     * @param sourceFile
     */
    public SFSSourceFile(String name, int mode, long time, long guid, long uid,
            File sourceFile) {
        super(name, mode, time, guid, uid);
        this.sourceFile = sourceFile;
    }

    /**
     * @return the sourceFile
     */
    public File getSourceFile() {
        return this.sourceFile;
    }

}
