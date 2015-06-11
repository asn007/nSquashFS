/**
 * 
 */
package com.fernsroth.squashfs.model.squashfs;

import java.io.IOException;

import com.fernsroth.easyio.exception.EasyIOException;
import com.fernsroth.easyio.util.EasyIOUtils;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public final class squashfs_constants {
    /**
     * see squashfs_fs.h.
     */
    public static final int SQUASHFS_MAJOR = 3;

    /**
     * see squashfs_fs.h.
     */
    public static final int SQUASHFS_MINOR = 0;

    /**
     * see squashfs_fs.h.
     */
    public static final int SQUASHFS_MAGIC = 0x73717368;

    /**
     * see squashfs_fs.h.
     */
    public static final int SQUASHFS_MAGIC_SWAP = 0x68737173;

    /**
     * see squashfs_fs.h.
     */
    public static final int SQUASHFS_START = 0;

    /**
     * the size of {@link squashfs_dir_entry}.
     */
    public static final int SQUASHFS_DIR_ENTRY_SIZE;

    /**
     * the size of {@link squashfs_dir_header}.
     */
    public static final int SQUASHFS_DIR_HEADER_SIZE;

    /**
     * the size of {@link squashfs_fragment_entry}.
     */
    public static final int SQUASHFS_FRAGMENT_ENTRY_SIZE;

    /**
     * the size of {@link squashfs_reg_inode_header}.
     */
    public static final int SQUASHFS_REG_INODE_HEADER_SIZE;

    /**
     * the size of {@link squashfs_symlink_inode_header}.
     */
    public static final int SQUASHFS_SYMLINK_INODE_HEADER_SIZE;

    /**
     * the size of {@link squashfs_super_block}.
     */
    public static final int SQUASHFS_SUPER_BLOCK_SIZE;

    /**
     * the size of {@link squashfs_uid}.
     */
    public static final int SQUASHFS_UID_SIZE;

    /**
     * 
     */
    public static final int SQUASHFS_UIDS = 256;

    /**
     * 
     */
    public static final int SQUASHFS_GUIDS = 255;

    /**
     * 
     */
    public static final int SQUASHFS_FRAGMENT_INDEX_SIZE;

    static {
        try {
            SQUASHFS_UID_SIZE = 4;
            SQUASHFS_FRAGMENT_INDEX_SIZE = 8;
            SQUASHFS_SUPER_BLOCK_SIZE = EasyIOUtils
                    .getClassSize(squashfs_super_block.class);
            SQUASHFS_DIR_ENTRY_SIZE = EasyIOUtils
                    .getClassSize(squashfs_dir_entry.class);
            SQUASHFS_DIR_HEADER_SIZE = EasyIOUtils
                    .getClassSize(squashfs_dir_header.class);
            SQUASHFS_FRAGMENT_ENTRY_SIZE = EasyIOUtils
                    .getClassSize(squashfs_fragment_entry.class);
            SQUASHFS_REG_INODE_HEADER_SIZE = EasyIOUtils
                    .getClassSize(squashfs_reg_inode_header.class);
            SQUASHFS_SYMLINK_INODE_HEADER_SIZE = EasyIOUtils
                    .getClassSize(squashfs_symlink_inode_header.class);
            SQUASHFS_DIR_INODE_HEADER_SIZE = EasyIOUtils
                    .getClassSize(squashfs_dir_inode_header.class);
            CACHED_DIR_INDEX_SIZE = EasyIOUtils
                    .getClassSize(cached_dir_index.class);
            SQUASHFS_DIR_INDEX_SIZE = EasyIOUtils
                    .getClassSize(squashfs_dir_index.class);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        } catch (EasyIOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /* size of metadata (inode and directory) blocks */
    /**
     * see squashfs_fs.h.
     */
    public static final int SQUASHFS_METADATA_SIZE = 8192;

    /**
     * see squashfs_fs.h.
     */
    public static final int SQUASHFS_METADATA_LOG = 13;

    /**
     * see squashfs_fs.h.
     */
    public static final int SQUASHFS_NOI = 0;

    /**
     * see squashfs_fs.h.
     */
    public static final int SQUASHFS_NOD = 1;

    /**
     * see squashfs_fs.h.
     */
    public static final int SQUASHFS_CHECK = 2;

    /**
     * see squashfs_fs.h.
     */
    public static final int SQUASHFS_NOF = 3;

    /**
     * see squashfs_fs.h.
     */
    public static final int SQUASHFS_NO_FRAG = 4;

    /**
     * see squashfs_fs.h.
     */
    public static final int SQUASHFS_ALWAYS_FRAG = 5;

    /**
     * see squashfs_fs.h.
     */
    public static final int SQUASHFS_DUPLICATE = 6;

    /**
     * see squashfs_fs.h.
     */
    public static final int SQUASHFS_COMPRESSED_BIT = (1 << 15);

    /**
     * see squashfs_fs.h.
     */
    public static final int SQUASHFS_DIR_TYPE = 1;

    /**
     * see squashfs_fs.h.
     */
    public static final int SQUASHFS_FILE_TYPE = 2;

    /**
     * see squashfs_fs.h.
     */
    public static final int SQUASHFS_SYMLINK_TYPE = 3;

    /**
     * see squashfs_fs.h.
     */
    public static final int SQUASHFS_BLKDEV_TYPE = 4;

    /**
     * see squashfs_fs.h.
     */
    public static final int SQUASHFS_CHRDEV_TYPE = 5;

    /**
     * see squashfs_fs.h.
     */
    public static final int SQUASHFS_FIFO_TYPE = 6;

    /**
     * see squashfs_fs.h.
     */
    public static final int SQUASHFS_SOCKET_TYPE = 7;

    /**
     * see squashfs_fs.h.
     */
    public static final int SQUASHFS_LDIR_TYPE = 8;

    /**
     * see squashfs_fs.h.
     */
    public static final int SQUASHFS_LREG_TYPE = 9;

    /**
     * see squashfs_fs.h.
     */
    public static final int DIR_ENT_SIZE = 16;

    /**
     * Max length of filename (not 255).
     */
    public static final int SQUASHFS_NAME_LEN = 256;

    /**
     * see squashfs_fs.h.
     */
    public static final long SQUASHFS_INVALID = 0xffffffffffffL;

    /**
     * see squashfs_fs.h.
     */
    public static final long SQUASHFS_INVALID_FRAG = 0xffffffffL;

    /**
     * see squashfs_fs.h.
     */
    public static final long SQUASHFS_INVALID_BLK = -1;

    /**
     * see squashfs_fs.h.
     */
    public static final long SQUASHFS_USED_BLK = -2;

    /**
     * see squashfs_fs.h.
     */
    public static final long SQUASHFS_FILE_SIZE = 65536;

    /**
     * see squashfs_fs.h.
     */
    public static final long SQUASHFS_FILE_LOG = 16;

    /**
     * see squashfs_fs.h.
     */
    public static final long SQUASHFS_FILE_MAX_SIZE = 65536;

    /**
     * @param A
     * @return zz
     */
    public static long SQUASHFS_FRAGMENT_INDEXES(long A) {
        return ((SQUASHFS_FRAGMENT_BYTES(A) + SQUASHFS_METADATA_SIZE - 1) / SQUASHFS_METADATA_SIZE);
    }

    /**
     * @param A
     * @return zz
     */
    public static long SQUASHFS_FRAGMENT_BYTES(long A) {
        return (A * SQUASHFS_FRAGMENT_ENTRY_SIZE);
    }

    /**
     * @param A
     * @return zz
     */
    public static long SQUASHFS_FRAGMENT_INDEX_BYTES(long A) {
        return (SQUASHFS_FRAGMENT_INDEXES(A) * 8);
    }

    /**
     * @param B
     * @return zz
     */
    public static int SQUASHFS_COMPRESSED_SIZE(int B) {
        return (((B) & ~SQUASHFS_COMPRESSED_BIT) != 0 ? (B)
                & ~SQUASHFS_COMPRESSED_BIT : SQUASHFS_COMPRESSED_BIT);
    }

    /**
     * @param B
     * @return zz
     */
    public static boolean SQUASHFS_COMPRESSED(int B) {
        return (!(((B) & SQUASHFS_COMPRESSED_BIT) != 0));
    }

    /**
     * @param flags
     * @return zz
     */
    public static boolean SQUASHFS_CHECK_DATA(int flags) {
        return SQUASHFS_BIT(flags, SQUASHFS_CHECK);
    }

    /**
     * @param flag
     * @param bit
     * @return zz
     */
    public static boolean SQUASHFS_BIT(int flag, int bit) {
        return ((flag >> bit) & 1) != 0;
    }

    /**
     * @param a
     * @return zz.
     */
    public static long SQUASHFS_INODE_BLK(long a) {
        return (((a) >> 16));
    }

    /**
     * @param a
     * @return zz.
     */
    public static long SQUASHFS_INODE_OFFSET(long a) {
        return (((a) & 0xffff));
    }

    /**
     * 
     */
    public static final long SQUASHFS_COMPRESSED_BIT_BLOCK = (1 << 24);

    /**
     * 
     */
    public static final byte SQUASHFS_MARKER_BYTE = (byte) 0xff;

    /**
     * 
     */
    public static final long SQUASHFS_MAX_FILE_SIZE_LOG = 64;

    /**
     * 
     */
    public static final long SQUASHFS_MAX_FILE_SIZE = ((long) 1 << (SQUASHFS_MAX_FILE_SIZE_LOG - 2));

    /**
     * 
     */
    public static final long SQUASHFS_DIR_INODE_HEADER_SIZE;

    /**
     * 
     */
    public static final int I_COUNT_SIZE = 128;

    /**
     * 
     */
    public static final int CACHED_DIR_INDEX_SIZE;

    /**
     * 
     */
    public static final long SQUASHFS_DIR_INDEX_SIZE;

    /**
     * @param B
     * @return the size.
     */
    public static long SQUASHFS_COMPRESSED_SIZE_BLOCK(long B) {
        return (((B) & ~SQUASHFS_COMPRESSED_BIT_BLOCK) != 0 ? (B)
                & ~SQUASHFS_COMPRESSED_BIT_BLOCK
                : SQUASHFS_COMPRESSED_BIT_BLOCK);
    }

    /**
     * @param B
     * @return true, if compressed.
     */
    public static boolean SQUASHFS_COMPRESSED_BLOCK(long B) {
        return (!((B & SQUASHFS_COMPRESSED_BIT_BLOCK) != 0));
    }

    /**
     * hide it. 
     */
    private squashfs_constants() {
        // empty.
    }

    /**
     * @param block
     * @return the slog.
     */
    public static int slog(long block) {
        int i;

        for (i = 12; i <= 16; i++) {
            if (block == (1 << i)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * @param noI
     * @param noD
     * @param check_data
     * @param noF
     * @param no_fragments
     * @param always_use_fragments
     * @param duplicate_checking
     * @return flags.
     */
    public static int SQUASHFS_MKFLAGS(boolean noI, boolean noD,
            boolean check_data, boolean noF, boolean no_fragments,
            boolean always_use_fragments, boolean duplicate_checking) {
        return ((noI ? 1 : 0) | ((noD ? 1 : 0) << 1)
                | ((check_data ? 1 : 0) << 2) | ((noF ? 1 : 0) << 3)
                | ((no_fragments ? 1 : 0) << 4)
                | ((always_use_fragments ? 1 : 0) << 5) | ((duplicate_checking ? 1
                : 0) << 6));
    }

    /**
     * @param a
     * @return mode.
     */
    public static int SQUASHFS_MODE(int a) {
        return ((a) & 0xfff);
    }
}
