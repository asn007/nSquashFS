/**
 * 
 */
package com.fernsroth.squashfs.model.squashfs;

import java.math.BigInteger;

import com.fernsroth.easyio.EasyIOClass;
import com.fernsroth.easyio.field.FieldINT64;
import com.fernsroth.easyio.field.FieldUINT16;
import com.fernsroth.easyio.field.FieldUINT32;
import com.fernsroth.easyio.field.FieldUINT64;
import com.fernsroth.easyio.field.FieldUINT8;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
@EasyIOClass(pack = 1)
public class squashfs_super_block {

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT32
    public long s_magic;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT32
    public long inodes;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT32
    public long bytes_used_2;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT32
    public long uid_start_2;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT32
    public long guid_start_2;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT32
    public long inode_table_start_2;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT32
    public long directory_table_start_2;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT16
    public int s_major;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT16
    public int s_minor;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT16
    public int block_size_1;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT16
    public int block_log;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT8
    public int flags;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT8
    public int no_uids;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT8
    public int no_guids;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT32
    public long mkfs_time;

    /**
     * see squashfs_fs.h.
     */
    @FieldINT64
    public long root_inode;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT32
    public long block_size;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT32
    public long fragments;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT32
    public long fragment_table_start_2;

    /**
     * see squashfs_fs.h.
     */
    @FieldINT64
    public long bytes_used;

    /**
     * see squashfs_fs.h.
     */
    @FieldINT64
    public long uid_start;

    /**
     * see squashfs_fs.h.
     */
    @FieldINT64
    public long guid_start;

    /**
     * see squashfs_fs.h.
     */
    @FieldINT64
    public long inode_table_start;

    /**
     * see squashfs_fs.h.
     */
    @FieldINT64
    public long directory_table_start;

    /**
     * see squashfs_fs.h.
     */
    @FieldINT64
    public long fragment_table_start;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT64
    public BigInteger unused;
}
