/**
 * 
 */
package com.fernsroth.squashfs.model.squashfs;

import java.util.List;

import com.fernsroth.easyio.EasyIOClass;
import com.fernsroth.easyio.field.FieldINT64;
import com.fernsroth.easyio.field.FieldUINT32;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
@EasyIOClass(pack = 1)
public class squashfs_reg_inode_header extends squashfs_base_inode_header {

    /**
     * see squashfs_fs.h.
     */
    @FieldINT64
    public long start_block;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT32
    public long fragment;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT32
    public long offset;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT32
    public long file_size;

    /**
     * see squashfs_fs.h.
     */
    public List<Long> block_list;
    // TODO write a handler for this.
}
