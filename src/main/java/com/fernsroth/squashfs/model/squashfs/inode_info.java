/**
 * 
 */
package com.fernsroth.squashfs.model.squashfs;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public class inode_info {

    /**
     * 
     */
    public long nlink;

    /**
     * 
     */
    public long inode_number;

    /**
     * 
     */
    public stat buf;

    /**
     * 
     */
    public inode_info next;

    /**
     * 
     */
    public long inode;

    /**
     * 
     */
    public int type;

}
