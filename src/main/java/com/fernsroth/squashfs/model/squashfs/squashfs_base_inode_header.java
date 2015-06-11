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
public class squashfs_base_inode_header {

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT32(bits = 4)
    public long inode_type;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT32(bits = 12)
    public long mode;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT32(bits = 8)
    public long uid;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT32(bits = 8)
    public long guid;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT32
    public long mtime;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT32
    public long inode_number;

}
