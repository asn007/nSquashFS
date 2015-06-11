/**
 * 
 */
package com.fernsroth.squashfs.model.squashfs;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public class dir {

    /**
     * see squashfs_fs.h.
     */
    public int dir_count;

    /**
     * see squashfs_fs.h.
     */
    public int cur_entry;

    /**
     * see squashfs_fs.h.
     */
    public int mode;

    /**
     * see squashfs_fs.h.
     */
    public long uid;

    /**
     * see squashfs_fs.h.
     */
    public long guid;

    /**
     * see squashfs_fs.h.
     */
    public long mtime;

    /**
     * see squashfs_fs.h.
     */
    public List<dir_ent> dirs = new ArrayList<dir_ent>();
}
