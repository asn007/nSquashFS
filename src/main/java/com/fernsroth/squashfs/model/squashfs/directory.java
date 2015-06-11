/**
 * 
 */
package com.fernsroth.squashfs.model.squashfs;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public class directory {

    /**
     * 
     */
    public byte[] buff;

    /**
     * 
     */
    public int buffp;

    /**
     * 
     */
    public int size;

    /**
     * 
     */
    public int entry_count;

    /**
     * 
     */
    public int index_count_p;

    /**
     * 
     */
    public int entry_count_p;

    /**
     * 
     */
    public cached_dir_index[] index;

    /**
     * 
     */
    public int i_count;

    /**
     * 
     */
    public int i_size;

    /**
     * 
     */
    public long start_block;

    /**
     * 
     */
    public long inode_number;
}
