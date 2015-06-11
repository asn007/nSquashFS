/**
 * 
 */
package com.fernsroth.squashfs.model.squashfs;

import com.fernsroth.easyio.EasyIOClass;
import com.fernsroth.easyio.field.FieldINT64;
import com.fernsroth.easyio.field.FieldUINT32;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
@EasyIOClass(pack = 1)
public class squashfs_fragment_entry {

    /**
     * see squashfs_fs.h.
     */
    @FieldINT64
    public long start_block;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT32
    public long size;

    /**
     * see squashfs_fs.h.
     */
    @FieldUINT32
    public long unused;

}
