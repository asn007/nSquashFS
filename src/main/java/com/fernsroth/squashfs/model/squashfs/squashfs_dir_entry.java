/**
 * 
 */
package com.fernsroth.squashfs.model.squashfs;

import com.fernsroth.easyio.EasyIOClass;
import com.fernsroth.easyio.field.FieldUINT16;
import com.fernsroth.easyio.field.FieldUINT8;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
@EasyIOClass(pack = 1)
public class squashfs_dir_entry {

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT16(bits = 13)
    public int offset;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT16(bits = 3)
    public int type;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT8
    public int size;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT16
    public int inode_number;

    /**
     * see squashfs_fs.h.
     */
    public String name;
    // TODO create a custom handler to read this in.
}
