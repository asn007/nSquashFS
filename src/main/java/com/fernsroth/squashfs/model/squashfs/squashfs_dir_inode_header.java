/**
 * 
 */
package com.fernsroth.squashfs.model.squashfs;

import com.fernsroth.easyio.EasyIOClass;
import com.fernsroth.easyio.field.FieldUINT32;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
@EasyIOClass(pack = 1)
public class squashfs_dir_inode_header extends squashfs_base_inode_header {
    /**
     * see squashfs_fs.h.
     */
    @FieldUINT32
    public long nlink;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT32(bits = 19)
    public long file_size;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT32(bits = 13)
    public long offset;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT32
    public long start_block;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT32
    public long parent_inode;
}
