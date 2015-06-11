/**
 * 
 */
package com.fernsroth.squashfs.model.squashfs;

import com.fernsroth.easyio.EasyIOClass;
import com.fernsroth.easyio.field.FieldUINT16;
import com.fernsroth.easyio.field.FieldUINT32;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
@EasyIOClass(pack = 1)
public class squashfs_symlink_inode_header extends squashfs_base_inode_header {

    /**
     * see squashfs.
     */
    @FieldUINT32
    public long nlink;

    /**
     * see squashfs.
     */
    @FieldUINT16
    public int symlink_size;

    /**
     * see squashfs.
     */
    public String symlink;
    // TODO write a rule to do this.
}
