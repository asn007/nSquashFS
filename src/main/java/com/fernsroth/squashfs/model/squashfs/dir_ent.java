/**
 * 
 */
package com.fernsroth.squashfs.model.squashfs;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public class dir_ent {
    /**
     * see squashfs_fs.h.
     */
    public static final int SQUASHFS_NAME_LEN = 256;

    /**
     * see squashfs_fs.h.
     */
    public String name;

    /**
     * see squashfs_fs.h.
     */
    public long start_block;

    /**
     * see squashfs_fs.h.
     */
    public long offset;

    /**
     * see squashfs_fs.h.
     */
    public int type;

    /**
     * see squashfs_fs.h.
     */
    public String pathname;

    /**
     * see squashfs_fs.h.
     */
    public dir_info dir;

    /**
     * see squashfs_fs.h.
     */
    public inode_info inode;

    /**
     * see squashfs_fs.h.
     */
    public dir_info our_dir;

    /**
     * see squashfs_fs.h.
     */
    public old_root_entry_info data;
}
