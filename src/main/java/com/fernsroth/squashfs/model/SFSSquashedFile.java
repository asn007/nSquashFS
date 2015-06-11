/**
 * 
 */
package com.fernsroth.squashfs.model;

import com.fernsroth.squashfs.model.squashfs.squashfs_fragment_entry;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public class SFSSquashedFile extends SFSFile {

    /**
     * 
     */
    private long blockPtr;

    /**
     * 
     */
    private long start;

    /**
     * 
     */
    private int blocks;

    /**
     * 
     */
    private long offset;

    /**
     * the fragment entry.
     */
    private squashfs_fragment_entry fragmentEntry;

    /**
     * number of fragment bytes.
     */
    private long fragmentBytes;

    /**
     * constructor. 
     * @param name the name of the file.
     * @param mode the mode.
     * @param mTime the modify time.
     * @param guid the group id.
     * @param uid the user id.
     * @param start 
     * @param blocks 
     * @param offset 
     * @param blockPtr 
     * @param fragmentEntry 
     * @param fragmentBytes 
     */
    public SFSSquashedFile(String name, int mode, long mTime, long guid,
            long uid, long start, int blocks, long offset, long blockPtr,
            squashfs_fragment_entry fragmentEntry, long fragmentBytes) {
        super(name, mode, mTime, guid, uid);
        this.start = start;
        this.blocks = blocks;
        this.offset = offset;
        this.blockPtr = blockPtr;
        this.fragmentEntry = fragmentEntry;
        this.fragmentBytes = fragmentBytes;
    }

    /**
     * @return the blockPtr
     */
    public long getBlockPtr() {
        return this.blockPtr;
    }

    /**
     * @return the blocks
     */
    public int getBlocks() {
        return this.blocks;
    }

    /**
     * @return the offset
     */
    public long getOffset() {
        return this.offset;
    }

    /**
     * @return the start
     */
    public long getStart() {
        return this.start;
    }

    /**
     * @return the fragment entry.
     */
    public squashfs_fragment_entry getFragmentEntry() {
        return this.fragmentEntry;
    }

    /**
     * @return number of fragment bytes.
     */
    public long getFragmentBytes() {
        return this.fragmentBytes;
    }

}
