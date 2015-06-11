/**
 * 
 */
package com.fernsroth.squashfs.model.squashfs;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public class stat implements Cloneable {

    /**
     * 
     */
    public static final int S_IR = 0x04;

    /**
     * 
     */
    public static final int S_IW = 0x02;

    /**
     * 
     */
    public static final int S_IX = 0x01;

    /**
     * 
     */
    public static final int S_IRUSR = S_IR << 6;

    /**
     * 
     */
    public static final int S_IWUSR = S_IW << 6;

    /**
     * 
     */
    public static final int S_IXUSR = S_IX << 6;

    /**
     * 
     */
    public static final int S_IRWXU = S_IRUSR | S_IWUSR | S_IXUSR;

    /**
     * Read by group.
     */
    public static final int S_IRGRP = (S_IRUSR >> 3);

    /**
     * Write by group.
     */
    public static final int S_IWGRP = (S_IWUSR >> 3);

    /**
     * Execute by group.
     */
    public static final int S_IXGRP = (S_IXUSR >> 3);

    /**
     * Read, write, and execute by group.
     */
    public static final int S_IRWXG = (S_IRWXU >> 3);

    /**
     * Read by others.
     */
    public static final int S_IROTH = (S_IRGRP >> 3);

    /**
     * Write by others.
     */
    public static final int S_IWOTH = (S_IWGRP >> 3);

    /**
     * Execute by others.
     */
    public static final int S_IXOTH = (S_IXGRP >> 3);

    /**
     * Read, write, and execute by others.
     */
    public static final int S_IRWXO = (S_IRWXG >> 3);

    /**
     * 
     */
    public static final int S_IFMT = 0xF000;

    /**
     * 
     */
    public static final int S_IFDIR = 0x4000;

    /**
     * 
     */
    public static final int S_IFREG = 0x8000;

    /**
     * 
     */
    public static final int S_IFLNK = 0xA000;

    /**
     * 
     */
    public static final int S_IFSOCK = 0xC000;

    /**
     * 
     */
    public static final int S_IFIFO = 0x1000;

    /**
     * 
     */
    public static final int S_IFBLK = 0x6000;

    /**
     * 
     */
    public static final int S_IFCHR = 0x2000;

    /**
     * 
     */
    public int st_mode;

    /**
     * 
     */
    public long st_uid;

    /**
     * 
     */
    public long st_gid;

    /**
     * 
     */
    public long st_mtime;

    /**
     * 
     */
    public int st_dev;

    /**
     * 
     */
    public int st_ino;

    /**
     * 
     */
    public long st_size;

    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
