/**
 * 
 */
package com.fernsroth.squashfs.model.squashfs;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public class file_info {

    /**
     * 
     */
    public fragment fragment;

    /**
     * 
     */
    public file_info next;

    /**
     * 
     */
    public long bytes;

    /**
     * 
     */
    public long start;

    /**
     * 
     */
    public int checksum;

    /**
     * 
     */
    public int fragment_checksum;

    /**
     * 
     */
    public int block_list;

}
