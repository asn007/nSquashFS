/**
 * 
 */
package com.fernsroth.squashfs.model.squashfs;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public class dir_info {

    /**
     * 
     */
    public dir_ent dir_ent;

    /**
     * 
     */
    public long directory_count;

    /**
     * 
     */
    public boolean dir_is_ldir;

    /**
     * 
     */
    public List<dir_ent> list = new ArrayList<dir_ent>();

    /**
     * 
     */
    public long current_count;

    /**
     * 
     */
    public long byte_count;

    /**
     * 
     */
    public long count;

    /**
     * 
     */
    public String pathname;

}
