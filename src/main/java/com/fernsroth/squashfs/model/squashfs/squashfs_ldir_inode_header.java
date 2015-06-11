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
public class squashfs_ldir_inode_header extends squashfs_base_inode_header {
    /**
     * see squashfs_fs.h.
     */
    @FieldUINT32
    public int nlink;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT32(bits = 27)
    public int file_size;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT32(bits = 13)
    public int offset;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT32
    public int start_block;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT32(bits = 16)
    public int i_count;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT32
    public int parent_inode;

    // TODO struct squashfs_dir_index	index[0];
}
