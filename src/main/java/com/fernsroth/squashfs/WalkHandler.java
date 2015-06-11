/**
 * 
 */
package com.fernsroth.squashfs;

import com.fernsroth.squashfs.model.BaseFile;
import com.fernsroth.squashfs.model.Directory;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public interface WalkHandler {

    /**
     * @param path the path where the file resides.
     * @param file the file visited.
     * @throws Exception 
     */
    void visit(Directory[] path, BaseFile file) throws Exception;

}
