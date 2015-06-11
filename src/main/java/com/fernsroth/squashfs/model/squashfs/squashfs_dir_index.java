/**
 * 
 */
package com.fernsroth.squashfs.model.squashfs;

import com.fernsroth.easyio.EasyIOClass;
import com.fernsroth.easyio.field.FieldUINT32;
import com.fernsroth.easyio.field.FieldUINT8;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
@EasyIOClass(pack = 1)
public class squashfs_dir_index {
    /**
     * 
     */
    @FieldUINT32
    public long index;

    /**
     * 
     */
    @FieldUINT32
    public long start_block;

    /**
     * 
     */
    @FieldUINT8
    public int size;

    /**
     * 
     */
    public String name;
}
