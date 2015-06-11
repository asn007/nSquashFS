/**
 * 
 */
package com.fernsroth.squashfs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import com.fernsroth.easyio.EasyIOInputStream;
import com.fernsroth.easyio.EasyIOOutputStream;
import com.fernsroth.easyio.IRandomAccessSource;
import com.fernsroth.easyio.RandomAccessByteArray;
import com.fernsroth.easyio.RandomAccessByteArrayInputStreamAdaptor;
import com.fernsroth.easyio.RandomAccessByteArrayOutputStreamAdapter;
import com.fernsroth.easyio.exception.EasyIOException;
import com.fernsroth.squashfs.exception.SquashFSException;
import com.fernsroth.squashfs.model.BaseFile;
import com.fernsroth.squashfs.model.Directory;
import com.fernsroth.squashfs.model.Manifest;
import com.fernsroth.squashfs.model.SymLink;
import com.fernsroth.squashfs.model.squashfs.cached_dir_index;
import com.fernsroth.squashfs.model.squashfs.dir_ent;
import com.fernsroth.squashfs.model.squashfs.dir_info;
import com.fernsroth.squashfs.model.squashfs.directory;
import com.fernsroth.squashfs.model.squashfs.duplicate_buffer_handle;
import com.fernsroth.squashfs.model.squashfs.file_info;
import com.fernsroth.squashfs.model.squashfs.fragment;
import com.fernsroth.squashfs.model.squashfs.inode_info;
import com.fernsroth.squashfs.model.squashfs.old_root_entry_info;
import com.fernsroth.squashfs.model.squashfs.squashfs_base_inode_header;
import com.fernsroth.squashfs.model.squashfs.squashfs_constants;
import com.fernsroth.squashfs.model.squashfs.squashfs_dir_entry;
import com.fernsroth.squashfs.model.squashfs.squashfs_dir_header;
import com.fernsroth.squashfs.model.squashfs.squashfs_dir_inode_header;
import com.fernsroth.squashfs.model.squashfs.squashfs_fragment_entry;
import com.fernsroth.squashfs.model.squashfs.squashfs_reg_inode_header;
import com.fernsroth.squashfs.model.squashfs.squashfs_super_block;
import com.fernsroth.squashfs.model.squashfs.squashfs_symlink_inode_header;
import com.fernsroth.squashfs.model.squashfs.stat;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public final class SquashFSWriter {

    /**
     * logging.
     */

    /**
     * the system.
     */
    private static SquashFSSystem system = new DefaultSquashFSSystem();

    /**
     * 
     */
    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat(
            "#0.00");

    /**
     * 
     */
    private static final int FRAG_SIZE = 32768;

    /**
     * 
     */
    private static final fragment empty_fragment;

    static {
        empty_fragment = new fragment();
        empty_fragment.index = (int) squashfs_constants.SQUASHFS_INVALID_FRAG;
    }

    /**
     * 
     */
    private Map<String, inode_info> inode_info = new HashMap<String, inode_info>();

    /**
     * 
     */
    private squashfs_super_block superBlock;

    /**
     * 
     */
    private long global_uid = -1;

    /**
     * 
     */
    private long global_gid = -1;

    /**
     * 
     */
    private byte[] directory_data_cache;

    /**
     * 
     */
    private byte[] directory_table;

    /**
     * 
     */
    private int directory_cache_bytes;

    /**
     * 
     */
    private int total_bytes;

    /**
     * 
     */
    private int uid_count;

    /**
     * 
     */
    private int total_inode_bytes;

    /**
     * 
     */
    private int total_directory_bytes;

    /**
     * 
     */
    private int guid_count;

    /**
     * 
     */
    private int inode_bytes;

    /**
     * 
     */
    private int directory_bytes;

    /**
     * 
     */
    private int file_count;

    /**
     * 
     */
    private int dup_files;

    /**
     * 
     */
    private int inode_count;

    /**
     * 
     */
    private int dev_count;

    /**
     * 
     */
    private int fifo_count;

    /**
     * 
     */
    private int sock_count;

    /**
     * 
     */
    private int dir_count;

    /**
     * 
     */
    private int sym_count;

    /**
     * 
     */
    private long[] uids = new long[squashfs_constants.SQUASHFS_UIDS];

    /**
     * 
     */
    private long[] guids = new long[squashfs_constants.SQUASHFS_GUIDS];

    /**
     * the root inode number.
     */
    private long root_inode_number = 0;

    /**
     * 
     */
    private long bytes;

    /**
     * the manifest.
     */
    private Manifest manifest;

    /**
     * the destination.
     */
    private IRandomAccessSource dest;

    /**
     * the easy out.
     */
    private EasyIOOutputStream out;

    /**
     * 
     */
    private squashfs_fragment_entry[] fragment_table;

    /**
     * 
     */
    private boolean be;

    /**
     * 
     */
    private boolean noI;

    /**
     * 
     */
    private boolean noD;

    /**
     * 
     */
    private boolean check_data;

    /**
     * 
     */
    private boolean noF;

    /**
     * 
     */
    private boolean no_fragments;

    /**
     * 
     */
    private boolean always_use_fragments;

    /**
     * 
     */
    private boolean duplicate_checking;

    /**
     * 
     */
    private boolean nopad;

    /**
     * 
     */
    private int block_offset;

    /**
     * 
     */
    private int dir_inode_no = 1;

    /**
     * 
     */
    //private int dummy_uid = 0;
    /**
     * 
     */
    //private int dummy_gid = 0;
    /**
     * 
     */
    private int inode_no;

    /**
     * 
     */
    private int directory_size;

    /**
     * 
     */
    private int cache_bytes;

    /**
     * 
     */
    private byte[] inode_table;

    /**
     * 
     */
    private int inode_size;

    /**
     * flag whether destination file is a block device.
     */
    private int block_device = 0;

    /**
     * 
     */
    private byte[] data_cache;

    /**
     * 
     */
    private int fragment_size;

    /**
     * 
     */
    private byte[] fragment_data = new byte[(int) squashfs_constants.SQUASHFS_FILE_SIZE];

    /**
     * 
     */
    private int total_uncompressed;

    /**
     * 
     */
    private int total_compressed;

    /**
     * 
     */
    private DataProvider dataProvider;

    /**
     * 
     */
    private long cache_size = 0;

    /**
     * 
     */
    private int directory_cache_size = 0;

    /**
     * 
     */
    private boolean swap = false;

    /**
     * the dev where the file resides.
     */
    private int st_dev = 1;

    /**
     * 
     */
    private byte[] cached_fragment = new byte[(int) squashfs_constants.SQUASHFS_FILE_SIZE];

    /**
     * 
     */
    private int cached_frag1 = -1;

    /**
     * 
     */
    private file_info[] dupl = new file_info[65536];

    /**
     * 
     */
    private file_info[] frag_dups = new file_info[65536];

    /**
     * constructor. 
     * @param manifest the manifest.
     * @param dataProvider the data provider.
     */
    public SquashFSWriter(Manifest manifest, DataProvider dataProvider) {
        this.dataProvider = dataProvider;
        this.manifest = manifest;
        this.be = false;
        this.noI = true;
        this.noD = true;
        this.noF = true;
        this.check_data = false;
        this.no_fragments = false;
        this.always_use_fragments = false;
        this.duplicate_checking = true;
        this.nopad = false;
    }

    /**
     * writes the directory to the destination file.
     * @param dir the directory to write.
     * @param randDest the destination.
     * @throws IOException 
     * @throws EasyIOException 
     * @throws EasyIOException 
     * @throws SquashFSException 
     */
    public void write(IRandomAccessSource randDest) throws IOException,
            EasyIOException, SquashFSException {
        this.dest = randDest;
        this.dest.setLength(0);
        this.out = new EasyIOOutputStream(this.dest);

        this.block_offset = this.check_data ? 3 : 2;
        this.total_bytes = 0;
        this.uid_count = 0;
        this.total_inode_bytes = 0;
        this.total_directory_bytes = 0;
        this.guid_count = 0;

        this.superBlock = new squashfs_super_block();
        this.superBlock.inodes = 0;
        this.superBlock.s_magic = squashfs_constants.SQUASHFS_MAGIC;
        this.superBlock.s_major = squashfs_constants.SQUASHFS_MAJOR;
        this.superBlock.s_minor = squashfs_constants.SQUASHFS_MINOR;
        this.superBlock.block_size = squashfs_constants.SQUASHFS_FILE_SIZE;
        this.superBlock.block_log = squashfs_constants
                .slog(this.superBlock.block_size);
        this.superBlock.flags = squashfs_constants.SQUASHFS_MKFLAGS(this.noI,
                this.noD, this.check_data, this.noF, this.no_fragments,
                this.always_use_fragments, this.duplicate_checking);
        this.superBlock.mkfs_time = SquashFSUtils.getMTimeFromDate(system
                .getCurrentDate());
        this.superBlock.fragments = 0;

        this.bytes = squashfs_constants.SQUASHFS_SUPER_BLOCK_SIZE;

        long inode = dir_scan(this.manifest.getRoot());
        this.superBlock.root_inode = inode;

        write_fragment();
        this.superBlock.inode_table_start = write_inodes();
        this.superBlock.directory_table_start = write_directories();
        this.superBlock.fragment_table_start = write_fragment_table();


        if ((this.superBlock.no_uids = this.uid_count) != 0) {
            /* if(!swap) { */
            this.dest.seek(this.bytes);
            for (int i = 0; i < this.uid_count; i++) {
                this.out.writeUINT32(this.uids[i]);
            }
            /*
             } else {
             squashfs_uid uids_copy[squashfs.uid_count];

             SQUASHFS_SWAP_DATA(uids, uids_copy, squashfs.uid_count, sizeof(squashfs_uid) * 8);
             write_bytes(fd, bytes, squashfs.uid_count * sizeof(squashfs_uid), (char *) uids_copy);
             }
             */
            this.superBlock.uid_start = this.bytes;
            this.bytes += this.uid_count * squashfs_constants.SQUASHFS_UID_SIZE;
        } else {
            this.superBlock.uid_start = 0;
        }

        if ((this.superBlock.no_guids = this.guid_count) != 0) {
            /* if(!swap) { */
            this.dest.seek(this.bytes);
            for (int i = 0; i < this.guid_count; i++) {
                this.out.writeUINT32(this.guids[i]);
            }
            /* 
             } else {
             squashfs_uid guids_copy[squashfs.guid_count];

             SQUASHFS_SWAP_DATA(guids, guids_copy, guid_count, sizeof(squashfs_uid) * 8);
             write_bytes(fd, bytes, squashfs.guid_count * sizeof(squashfs_uid), (char *) guids_copy);
             }
             */
            this.superBlock.guid_start = this.bytes;
            this.bytes += this.guid_count
                    * squashfs_constants.SQUASHFS_UID_SIZE;
        } else {
            this.superBlock.guid_start = 0;
        }

        this.superBlock.bytes_used = this.bytes;
        this.superBlock.inodes = this.inode_count;
        this.superBlock.unused = new BigInteger("ffffffffffffffff", 16);

        /*
         if(!swap) {
         */
        this.dest.seek(squashfs_constants.SQUASHFS_START);
        this.out.write(this.superBlock);
        /*
         } else {
         squashfs_super_block sBlk_copy;

         SQUASHFS_SWAP_SUPER_BLOCK((&sBlk), &sBlk_copy); 
         write_bytes(fd, SQUASHFS_START, sizeof(squashfs_super_block), (char *) &sBlk_copy);
         }
         */

        long i;
        if (!this.nopad && (i = (this.bytes & (4096 - 1))) != 0) {
            byte[] temp = new byte[4096];
            this.dest.seek(this.bytes);
            this.out.write(temp, 0, (int) (4096 - i));
        }

        this.total_bytes += this.total_inode_bytes + this.total_directory_bytes
                + (this.uid_count * 4) + (this.guid_count * 4)
                + squashfs_constants.SQUASHFS_SUPER_BLOCK_SIZE;


        for (i = 0; i < this.uid_count; i++) {
            String userName = getpwuid(this.uids[(int) i]);
        }

        for (i = 0; i < this.guid_count; i++) {
            String userName = getgrgid(this.guids[(int) i]);
        }
    }

    /**
     * @param l
     * @return z.
     */
    private String getgrgid(long l) {
        if (l == 0) {
            return "root";
        }
        // TODO get other user names.
        return "";
    }

    /**
     * @param l
     * @return z.
     */
    private String getpwuid(long l) {
        if (l == 0) {
            return "root";
        }
        // TODO get other group names.
        return "";
    }

    /**
     * @param squashfs
     * @return start.
     * @throws IOException 
     * @throws EasyIOException 
     */
    private long write_fragment_table() throws IOException, EasyIOException {
        long frag_bytes = squashfs_constants
                .SQUASHFS_FRAGMENT_BYTES(this.superBlock.fragments);
        long meta_blocks = squashfs_constants
                .SQUASHFS_FRAGMENT_INDEXES(this.superBlock.fragments);
        long start_bytes = 0;
        long[] list = new long[(int) meta_blocks];
        int compressed_size;
        int c_byte;
        byte[] cbuffer = new byte[(squashfs_constants.SQUASHFS_METADATA_SIZE << 2) + 2];
        RandomAccessByteArray cbuffer_rand = new RandomAccessByteArray(cbuffer);
        EasyIOOutputStream cbuffer_out = new EasyIOOutputStream(
                new RandomAccessByteArrayOutputStreamAdapter(cbuffer_rand));
        byte[] buffer = new byte[(int) frag_bytes];
        RandomAccessByteArray buffer_rand = new RandomAccessByteArray(buffer);
        EasyIOOutputStream buffer_out = new EasyIOOutputStream(buffer_rand);

        /*
         squashfs_fragment_entry *p = (squashfs_fragment_entry *) buffer;
         */

        int p = 0;
        for (int i = 0; i < this.superBlock.fragments; i++, p += squashfs_constants.SQUASHFS_FRAGMENT_ENTRY_SIZE) {

            /* if(!swap) { */
            buffer_rand.seek(p);
            buffer_out.write(this.fragment_table[i]);
            /* } else {
             SQUASHFS_SWAP_FRAGMENT_ENTRY(&fragment_table[i], p);
             } */
        }
        for (int i = 0; i < meta_blocks; i++) {
            int avail_bytes = (int) (i == meta_blocks - 1 ? frag_bytes
                    % squashfs_constants.SQUASHFS_METADATA_SIZE
                    : squashfs_constants.SQUASHFS_METADATA_SIZE);
            cbuffer_rand.seek(this.block_offset);
            buffer_rand.seek(i * squashfs_constants.SQUASHFS_METADATA_SIZE);
            c_byte = (int) mangle(cbuffer_out, buffer_rand, avail_bytes,
                    squashfs_constants.SQUASHFS_METADATA_SIZE, this.noF, 0);
            /*if(!swap) {*/
            cbuffer_rand.seek(0);
            cbuffer_out.writeUINT16(c_byte);
            /*} else {
             SQUASHFS_SWAP_SHORTS((&c_byte), cbuffer, 1);
             }*/
            if (this.check_data) {
                cbuffer[this.block_offset - 1] = squashfs_constants.SQUASHFS_MARKER_BYTE;
            }
            list[i] = this.bytes;
            compressed_size = squashfs_constants
                    .SQUASHFS_COMPRESSED_SIZE(c_byte)
                    + this.block_offset;
            this.dest.seek(this.bytes);
            this.dest.write(cbuffer, 0, compressed_size);
            this.bytes += compressed_size;
        }

        /* if(!swap) { */
        this.dest.seek(this.bytes);
        for (int i = 0; i < list.length; i++) {
            this.out.writeINT64(list[i]);
        }
        /* } else {
         squashfs_fragment_index slist[meta_blocks];
         SQUASHFS_SWAP_FRAGMENT_INDEXES(list, slist, meta_blocks);
         write_bytes(fd, bytes, sizeof(list), (char *) slist);
         } */

        start_bytes = this.bytes;
        this.bytes += squashfs_constants.SQUASHFS_FRAGMENT_INDEX_SIZE
                * meta_blocks;

        return start_bytes;
    }

    /**
     * @param d
     * @param s
     * @param size
     * @param block_size
     * @param uncompressed
     * @param data_block
     * @return z
     * @throws IOException 
     */
    private long mangle(EasyIOOutputStream d, RandomAccessByteArray s,
            int size, int block_size, boolean uncompressed, int data_block)
            throws IOException {
        long c_byte = block_size << 1;

        byte[] readdata = new byte[size];
        s.read(readdata);
        if (!uncompressed) {
            long start = d.getCount();
            DeflaterOutputStream o = new DeflaterOutputStream(d);
            o.write(readdata);
            o.close();
            c_byte = d.getCount() - start;
        }

        if (uncompressed || c_byte >= size) {
            d.write(readdata);
            return size
                    | (data_block != 0 ? squashfs_constants.SQUASHFS_COMPRESSED_BIT_BLOCK
                            : squashfs_constants.SQUASHFS_COMPRESSED_BIT);
        }

        return c_byte;
    }

    /**
     * @param squashfs
     * @return start.
     * @throws IOException 
     */
    private long write_directories() throws IOException {
        long start_bytes = this.bytes;

        int c_byte;
        int avail_bytes;
        int directoryp = 0;

        while (this.directory_cache_bytes != 0) {
            if (this.directory_size - this.directory_bytes < ((squashfs_constants.SQUASHFS_METADATA_SIZE << 1) + 2)) {
                this.directory_table = realloc(
                        this.directory_table,
                        this.directory_size
                                + ((squashfs_constants.SQUASHFS_METADATA_SIZE << 1) + 2));
                this.directory_size += (squashfs_constants.SQUASHFS_METADATA_SIZE << 1) + 2;
            }
            avail_bytes = this.directory_cache_bytes > squashfs_constants.SQUASHFS_METADATA_SIZE ? squashfs_constants.SQUASHFS_METADATA_SIZE
                    : this.directory_cache_bytes;
            RandomAccessByteArray directory_table_array = new RandomAccessByteArray(
                    this.directory_table);
            EasyIOOutputStream directory_table_stream = new EasyIOOutputStream(
                    new RandomAccessByteArrayOutputStreamAdapter(
                            directory_table_array));
            RandomAccessByteArray directory_data_cache_array = new RandomAccessByteArray(
                    this.directory_data_cache);
            directory_data_cache_array.seek(directoryp);
            directory_table_array
                    .seek(this.directory_bytes + this.block_offset);
            c_byte = (int) mangle(directory_table_stream,
                    directory_data_cache_array, avail_bytes,
                    squashfs_constants.SQUASHFS_METADATA_SIZE, this.noI, 0);

            /* if(!swap) { */
            directory_table_array.seek(this.directory_bytes);
            directory_table_stream.writeUINT16(c_byte);
            /* } else {
             SQUASHFS_SWAP_SHORTS((&c_byte), (directory_table + directory_bytes), 1);
             } */
            if (this.check_data) {
                this.directory_table[this.directory_bytes + this.block_offset
                        - 1] = squashfs_constants.SQUASHFS_MARKER_BYTE;
            }
            this.directory_bytes += squashfs_constants
                    .SQUASHFS_COMPRESSED_SIZE(c_byte)
                    + this.block_offset;
            this.total_directory_bytes += avail_bytes + this.block_offset;
            directoryp += avail_bytes;
            this.directory_cache_bytes -= avail_bytes;
        }
        this.dest.seek(this.bytes);
        this.dest.write(this.directory_table, 0, this.directory_bytes);
        this.bytes += this.directory_bytes;

        return start_bytes;
    }

    /**
     * @param prev
     * @param new_size
     * @return the new buffer.
     */
    private byte[] realloc(byte[] prev, int new_size) {
        byte[] results = new byte[new_size];
        if (prev != null) {
            System.arraycopy(prev, 0, results, 0, prev.length);
        }
        return results;
    }

    /**
     * @param prev
     * @param new_size
     * @return the new array.
     */
    private squashfs_fragment_entry[] realloc(squashfs_fragment_entry[] prev,
            int new_size) {
        squashfs_fragment_entry[] results = new squashfs_fragment_entry[new_size];
        if (prev != null) {
            System.arraycopy(prev, 0, results, 0, prev.length);
        }
        return results;
    }

    /**
     * @param prev
     * @param new_size
     * @return the new array.
     */
    private cached_dir_index[] realloc(cached_dir_index[] prev, int new_size) {
        cached_dir_index[] results = new cached_dir_index[new_size];
        System.arraycopy(prev, 0, results, 0, prev.length);
        return results;
    }

    /**
     * @param squashfs
     * @return start.
     * @throws IOException 
     */
    private long write_inodes() throws IOException {
        int c_byte;
        int avail_bytes;
        int datap = 0;
        long start_bytes = this.bytes;

        while (this.cache_bytes != 0) {
            if (this.inode_size - this.inode_bytes < ((squashfs_constants.SQUASHFS_METADATA_SIZE << 1) + 2)) {
                this.inode_table = realloc(
                        this.inode_table,
                        this.inode_size
                                + ((squashfs_constants.SQUASHFS_METADATA_SIZE << 1) + 2));
                this.inode_size += (squashfs_constants.SQUASHFS_METADATA_SIZE << 1) + 2;
            }
            avail_bytes = this.cache_bytes > squashfs_constants.SQUASHFS_METADATA_SIZE ? squashfs_constants.SQUASHFS_METADATA_SIZE
                    : this.cache_bytes;
            RandomAccessByteArray inode_table_array = new RandomAccessByteArray(
                    this.inode_table);
            EasyIOOutputStream inode_table_stream = new EasyIOOutputStream(
                    new RandomAccessByteArrayOutputStreamAdapter(
                            inode_table_array));
            RandomAccessByteArray data_cache_array = new RandomAccessByteArray(
                    this.data_cache);
            inode_table_array.seek(this.inode_bytes + this.block_offset);
            data_cache_array.seek(datap);
            c_byte = (int) mangle(inode_table_stream, data_cache_array,
                    avail_bytes, squashfs_constants.SQUASHFS_METADATA_SIZE,
                    this.noI, 0);
            /* if(!swap) { */
            inode_table_array.seek(this.inode_bytes);
            inode_table_stream.writeUINT16(c_byte);
            /* } else {
             SQUASHFS_SWAP_SHORTS((&c_byte), (inode_table + inode_bytes), 1);
             } */
            if (this.check_data) {
                this.inode_table[this.inode_bytes + this.block_offset - 1] = squashfs_constants.SQUASHFS_MARKER_BYTE;
            }
            this.inode_bytes += squashfs_constants
                    .SQUASHFS_COMPRESSED_SIZE(c_byte)
                    + this.block_offset;
            this.total_inode_bytes += avail_bytes + this.block_offset;
            datap += avail_bytes;
            this.cache_bytes -= avail_bytes;
        }

        this.dest.seek(this.bytes);
        this.out.write(this.inode_table, 0, this.inode_bytes);
        this.bytes += this.inode_bytes;

        return start_bytes;
    }

    /**
     * @param squashfs
     * @throws IOException 
     */
    private void write_fragment() throws IOException {
        int compressed_size;
        byte[] buffer = new byte[(int) (this.superBlock.block_size << 1)];
        RandomAccessByteArray fragment_data_array = new RandomAccessByteArray(
                this.fragment_data);
        RandomAccessByteArray buffer_array = new RandomAccessByteArray(buffer);
        EasyIOOutputStream buffer_stream = new EasyIOOutputStream(
                new RandomAccessByteArrayOutputStreamAdapter(buffer_array));

        if (this.fragment_size == 0) {
            return;
        }

        if (this.superBlock.fragments % FRAG_SIZE == 0) {
            this.fragment_table = realloc(this.fragment_table,
                    ((int) this.superBlock.fragments + FRAG_SIZE)
                            * squashfs_constants.SQUASHFS_FRAGMENT_ENTRY_SIZE);
        }
        if (this.fragment_table[(int) this.superBlock.fragments] == null) {
            this.fragment_table[(int) this.superBlock.fragments] = new squashfs_fragment_entry();
        }
        this.fragment_table[(int) this.superBlock.fragments].size = mangle(
                buffer_stream, fragment_data_array, this.fragment_size,
                (int) this.superBlock.block_size, this.noF, 1);
        this.fragment_table[(int) this.superBlock.fragments].start_block = this.bytes;
        compressed_size = (int) squashfs_constants
                .SQUASHFS_COMPRESSED_SIZE_BLOCK(this.fragment_table[(int) this.superBlock.fragments].size);
        this.dest.seek(this.bytes);
        this.out.write(buffer, 0, compressed_size);
        this.bytes += compressed_size;
        this.total_uncompressed += this.fragment_size;
        this.total_compressed += compressed_size;

        this.superBlock.fragments++;
        this.fragment_size = 0;
    }

    /**
     * @param squashfs 
     * @param dir
     * @return the root inode.
     * @throws SquashFSException 
     * @throws IOException 
     * @throws EasyIOException 
     */
    private long dir_scan(Directory dir) throws SquashFSException, IOException,
            EasyIOException {
        dir_info dir_info = dir_scan1(dir, ".");
        dir_ent dir_ent;
        inode_info inode_info_var;

        if (dir_info == null) {
            return -1;
        }

        dir_ent = new dir_ent();
        inode_info_var = new inode_info();

        dir_ent.name = dir_ent.pathname = dir.getName();
        dir_ent.dir = dir_info;
        dir_ent.inode = inode_info_var;
        dir_ent.our_dir = null;
        dir_ent.data = null;
        inode_info_var.nlink = 1;
        inode_info_var.inode_number = this.root_inode_number != 0 ? this.root_inode_number
                : this.dir_inode_no++;
        dir_info.dir_ent = dir_ent;
        inode_info_var.buf = new stat();

        /*
         if (dir.getName() == null || dir.getName().length() == 0) {
         // dummy top level directory, if multiple sources specified on command line
         inode_info_var.buf.st_mode = stat.S_IRWXU | stat.S_IRWXG
         | stat.S_IRWXO;
         inode_info_var.buf.st_uid = this.dummy_uid;
         inode_info_var.buf.st_gid = this.dummy_gid;
         inode_info_var.buf.st_mtime = SquashFSUtils.getMTimeFromDate(system
         .getCurrentDate());
         } else {
         */
        inode_info_var.buf = fromBaseFile(dir);
        /*
         }
         */

        /*
         if(sorted)
         sort_files_and_write(dir_info);
         */
        return dir_scan2(dir_info);
    }

    /**
     * @param dir_info
     * @return the inode
     * @throws SquashFSException 
     * @throws IOException 
     * @throws EasyIOException 
     */
    private long dir_scan2(dir_info dir_info) throws SquashFSException,
            IOException, EasyIOException {
        int squashfs_type;
        boolean[] duplicate_file;
        String pathname = dir_info.pathname;
        directory dir = new directory();
        dir_ent dir_ent;
        long inode = 0;

        scan2_init_dir(dir);

        while ((dir_ent = scan2_readdir(dir, dir_info)) != null) {
            inode_info inode_info_var = dir_ent.inode;
            stat buf = inode_info_var.buf;
            String filename = dir_ent.pathname;
            String dir_name = dir_ent.name;
            long inode_number = ((buf.st_mode & stat.S_IFMT) == stat.S_IFDIR) ? dir_ent.inode.inode_number
                    : dir_ent.inode.inode_number + this.dir_inode_no;

            if (dir_ent.inode.inode == squashfs_constants.SQUASHFS_INVALID_BLK) {
                switch ((int) (buf.st_mode & stat.S_IFMT)) {
                case stat.S_IFREG:
                    squashfs_type = squashfs_constants.SQUASHFS_FILE_TYPE;
                    duplicate_file = new boolean[] { false };
                    inode = write_file(dir_ent, buf.st_size, duplicate_file);
                    break;

                case stat.S_IFDIR:
                    squashfs_type = squashfs_constants.SQUASHFS_DIR_TYPE;
                    inode = dir_scan2(dir_ent.dir);
                    break;

                case stat.S_IFLNK:
                    squashfs_type = squashfs_constants.SQUASHFS_SYMLINK_TYPE;
                    inode = create_inode(dir_ent, squashfs_type, 0, 0, 0, null,
                            0, null, null);
                    this.sym_count++;
                    break;

                case stat.S_IFCHR:
                    squashfs_type = squashfs_constants.SQUASHFS_CHRDEV_TYPE;
                    inode = create_inode(dir_ent, squashfs_type, 0, 0, 0, null,
                            0, null, null);
                    this.dev_count++;
                    break;

                case stat.S_IFBLK:
                    squashfs_type = squashfs_constants.SQUASHFS_BLKDEV_TYPE;
                    inode = create_inode(dir_ent, squashfs_type, 0, 0, 0, null,
                            0, null, null);
                    this.dev_count++;
                    break;

                case stat.S_IFIFO:
                    squashfs_type = squashfs_constants.SQUASHFS_FIFO_TYPE;
                    inode = create_inode(dir_ent, squashfs_type, 0, 0, 0, null,
                            0, null, null);
                    this.fifo_count++;
                    break;

                case stat.S_IFSOCK:
                    squashfs_type = squashfs_constants.SQUASHFS_SOCKET_TYPE;
                    inode = create_inode(dir_ent, squashfs_type, 0, 0, 0, null,
                            0, null, null);
                    this.sock_count++;
                    break;

                default:
                    throw new SquashFSException(filename
                            + " unrecognised file type, mode is " + buf.st_mode);
                }
                dir_ent.inode.inode = inode;
                dir_ent.inode.type = squashfs_type;
            } else {
                inode = dir_ent.inode.inode;
                squashfs_type = dir_ent.inode.type;
                switch (squashfs_type) {
                case squashfs_constants.SQUASHFS_FILE_TYPE:
                    /* if(!sorted) { */
                    /* } */
                    break;
                case squashfs_constants.SQUASHFS_SYMLINK_TYPE:

                    break;
                case squashfs_constants.SQUASHFS_CHRDEV_TYPE:

                    break;
                case squashfs_constants.SQUASHFS_BLKDEV_TYPE:

                    break;
                case squashfs_constants.SQUASHFS_FIFO_TYPE:

                    break;
                case squashfs_constants.SQUASHFS_SOCKET_TYPE:

                    break;
                }
            }

            add_dir(inode, inode_number, dir_name, squashfs_type, dir);
        }

        inode = write_dir(dir_info, dir);


        scan2_freedir(dir);

        return inode;
    }

    /**
     * @param dir
     */
    private void scan2_freedir(directory dir) {
        // do nothing.
    }

    /**
     * @param dir_info 
     * @param dir 
     * @return z.
     * @throws IOException 
     * @throws EasyIOException 
     * @throws SquashFSException 
     */
    private long write_dir(dir_info dir_info, directory dir)
            throws EasyIOException, IOException, SquashFSException {
        long dir_size = dir.buffp;
        int data_space = (this.directory_cache_size - this.directory_cache_bytes);
        long directory_block, directory_offset, i_count, index;
        int c_byte;

        if (data_space < dir_size) {
            int realloc_size = (int) (this.directory_cache_size == 0 ? ((dir_size + squashfs_constants.SQUASHFS_METADATA_SIZE) & ~(squashfs_constants.SQUASHFS_METADATA_SIZE - 1))
                    : dir_size - data_space);

            this.directory_data_cache = realloc(this.directory_data_cache,
                    this.directory_cache_size + realloc_size);
            this.directory_cache_size += realloc_size;
        }

        if (dir_size != 0) {
            squashfs_dir_header dir_header = new squashfs_dir_header();

            dir_header.count = dir.entry_count - 1;
            dir_header.start_block = dir.start_block;
            dir_header.inode_number = dir.inode_number;
            /* if(!swap) { */
            RandomAccessByteArray buff_array = new RandomAccessByteArray(
                    dir.buff);
            EasyIOOutputStream buff_stream = new EasyIOOutputStream(
                    new RandomAccessByteArrayOutputStreamAdapter(buff_array));
            buff_array.seek(dir.entry_count_p);
            buff_stream.write(dir_header);
            /* } else {
             SQUASHFS_SWAP_DIR_HEADER((&dir_header), (squashfs_dir_header *) dir->entry_count_p);
             } */
            RandomAccessByteArray directory_data_cache_array = new RandomAccessByteArray(
                    this.directory_data_cache);
            directory_data_cache_array.seek(this.directory_cache_bytes);
            directory_data_cache_array.write(dir.buff, 0, (int) dir_size);
        }

        directory_offset = this.directory_cache_bytes;
        directory_block = this.directory_bytes;
        this.directory_cache_bytes += dir_size;
        i_count = 0;
        index = squashfs_constants.SQUASHFS_METADATA_SIZE - directory_offset;

        while (true) {
            while (i_count < dir.i_count
                    && dir.index[(int) i_count].index.index < index) {
                dir.index[(int) i_count++].index.start_block = this.directory_bytes;
            }
            index += squashfs_constants.SQUASHFS_METADATA_SIZE;

            if (this.directory_cache_bytes < squashfs_constants.SQUASHFS_METADATA_SIZE) {
                break;
            }

            if ((this.directory_size - this.directory_bytes) < ((squashfs_constants.SQUASHFS_METADATA_SIZE << 1) + 2)) {
                this.directory_table = realloc(
                        this.directory_table,
                        this.directory_size
                                + (squashfs_constants.SQUASHFS_METADATA_SIZE << 1)
                                + 2);
                this.directory_size += squashfs_constants.SQUASHFS_METADATA_SIZE << 1;
            }

            RandomAccessByteArray directory_table_array = new RandomAccessByteArray(
                    this.directory_table);
            EasyIOOutputStream directory_table_stream = new EasyIOOutputStream(
                    new RandomAccessByteArrayOutputStreamAdapter(
                            directory_table_array));
            RandomAccessByteArray directory_data_cache_array = new RandomAccessByteArray(
                    this.directory_data_cache);
            directory_table_array
                    .seek(this.directory_bytes + this.block_offset);
            c_byte = (int) mangle(directory_table_stream,
                    directory_data_cache_array,
                    squashfs_constants.SQUASHFS_METADATA_SIZE,
                    squashfs_constants.SQUASHFS_METADATA_SIZE, this.noI, 0);
            /*if(!swap) {*/
            directory_table_array = new RandomAccessByteArray(
                    this.directory_table);
            directory_table_stream = new EasyIOOutputStream(
                    new RandomAccessByteArrayOutputStreamAdapter(
                            directory_table_array));
            directory_table_stream.writeUINT16(c_byte);
            /*} else { 
             SQUASHFS_SWAP_SHORTS((&c_byte), (directory_table + directory_bytes), 1);
             }*/
            if (this.check_data) {
                this.directory_table[this.directory_bytes + this.block_offset
                        - 1] = squashfs_constants.SQUASHFS_MARKER_BYTE;
            }
            this.directory_bytes += squashfs_constants
                    .SQUASHFS_COMPRESSED_SIZE(c_byte)
                    + this.block_offset;
            this.total_directory_bytes += squashfs_constants.SQUASHFS_METADATA_SIZE
                    + this.block_offset;
            directory_data_cache_array = new RandomAccessByteArray(
                    this.directory_data_cache);
            directory_data_cache_array.write(this.directory_data_cache,
                    squashfs_constants.SQUASHFS_METADATA_SIZE,
                    this.directory_cache_bytes
                            - squashfs_constants.SQUASHFS_METADATA_SIZE);
            this.directory_cache_bytes -= squashfs_constants.SQUASHFS_METADATA_SIZE;
        }

        long inode;
        if (dir_info.dir_is_ldir) {
            inode = create_inode(dir_info.dir_ent,
                    squashfs_constants.SQUASHFS_LDIR_TYPE, dir_size + 3,
                    directory_block, directory_offset, null, 0, null, dir);
        } else {
            inode = create_inode(dir_info.dir_ent,
                    squashfs_constants.SQUASHFS_DIR_TYPE, dir_size + 3,
                    directory_block, directory_offset, null, 0, null, null);
        }


        this.dir_count++;

        return inode;
    }

    /**
     * @param inode
     * @param inode_number
     * @param name
     * @param type
     * @param dir
     * @throws IOException 
     * @throws EasyIOException 
     */
    private void add_dir(long inode, long inode_number, String name, int type,
            directory dir) throws EasyIOException, IOException {
        long size;
        long start_block = inode >> 16;
        squashfs_dir_entry idir = new squashfs_dir_entry();
        long offset = inode & 0xffff;

        if ((size = name.length()) > squashfs_constants.SQUASHFS_NAME_LEN) {
            size = squashfs_constants.SQUASHFS_NAME_LEN;
        }

        if (dir.buffp + squashfs_constants.SQUASHFS_DIR_ENTRY_SIZE + size
                + squashfs_constants.SQUASHFS_DIR_HEADER_SIZE >= dir.size) {
            dir.buff = realloc(dir.buff,
                    dir.size += squashfs_constants.SQUASHFS_METADATA_SIZE);
        }

        if (dir.entry_count == 256
                || start_block != dir.start_block
                || ((dir.entry_count_p >= 0) && ((dir.buffp
                        + squashfs_constants.SQUASHFS_DIR_ENTRY_SIZE + size - dir.index_count_p) > squashfs_constants.SQUASHFS_METADATA_SIZE))
                || ((long) inode_number - dir.inode_number) > 32767
                || ((long) inode_number - dir.inode_number) < -32768) {
            if (dir.entry_count_p >= 0) {
                squashfs_dir_header dir_header = new squashfs_dir_header();

                if ((dir.buffp + squashfs_constants.SQUASHFS_DIR_ENTRY_SIZE
                        + size - dir.index_count_p) > squashfs_constants.SQUASHFS_METADATA_SIZE) {
                    if (dir.i_count % squashfs_constants.I_COUNT_SIZE == 0) {
                        dir.index = realloc(
                                dir.index,
                                (dir.i_count + squashfs_constants.I_COUNT_SIZE)
                                        * squashfs_constants.CACHED_DIR_INDEX_SIZE);
                    }
                    dir.index[dir.i_count].index.index = dir.buffp;
                    dir.index[dir.i_count].index.size = (int) (size - 1);
                    dir.index[dir.i_count++].name = name;
                    dir.i_size += squashfs_constants.SQUASHFS_DIR_INDEX_SIZE
                            + size;
                    dir.index_count_p = dir.buffp;
                }

                dir_header.count = dir.entry_count - 1;
                dir_header.start_block = dir.start_block;
                dir_header.inode_number = dir.inode_number;
                /* if(!swap) { */
                ByteArrayOutputStream zbuffer = new ByteArrayOutputStream();
                EasyIOOutputStream zbuffer_stream = new EasyIOOutputStream(
                        zbuffer);
                zbuffer_stream.write(dir_header);
                zbuffer_stream.close();

                RandomAccessByteArray buff_array = new RandomAccessByteArray(
                        dir.buff);
                buff_array.seek(dir.entry_count_p);
                EasyIOOutputStream buff_out = new EasyIOOutputStream(
                        new RandomAccessByteArrayOutputStreamAdapter(buff_array));
                buff_out.write(zbuffer.toByteArray());
                /* } else {
                 SQUASHFS_SWAP_DIR_HEADER((&dir_header), (squashfs_dir_header *) dir->entry_count_p);
                 } */
            }

            dir.entry_count_p = dir.buffp;
            dir.start_block = start_block;
            dir.entry_count = 0;
            dir.inode_number = inode_number;
            dir.buffp += squashfs_constants.SQUASHFS_DIR_HEADER_SIZE;
        }

        idir.offset = (int) offset;
        idir.type = type;
        idir.size = (int) (size - 1);
        idir.inode_number = (int) ((long) inode_number - dir.inode_number);
        /* if(!swap) { */
        ByteArrayOutputStream zbuffer = new ByteArrayOutputStream();
        EasyIOOutputStream zbuffer_stream = new EasyIOOutputStream(zbuffer);
        zbuffer_stream.write(idir);
        zbuffer_stream.close();

        RandomAccessByteArray buff_array = new RandomAccessByteArray(dir.buff);
        buff_array.seek(dir.buffp);
        buff_array.write(zbuffer.toByteArray());
        /* } else {
         SQUASHFS_SWAP_DIR_ENTRY((&idir), idirp);
         }*/
        buff_array.write(name.getBytes(), 0, (int) size);
        dir.buffp += squashfs_constants.SQUASHFS_DIR_ENTRY_SIZE + size;
        dir.entry_count++;
    }

    /**
     * @param dir
     * @param dir_info
     * @return z
     * @throws IOException 
     * @throws EasyIOException 
     */
    private dir_ent scan2_readdir(directory dir, dir_info dir_info)
            throws EasyIOException, IOException {
        int current_count;

        while ((current_count = (int) dir_info.current_count++) < dir_info.count) {
            if (dir_info.list.get(current_count).data != null) {
                add_dir(dir_info.list.get(current_count).data.inode,
                        dir_info.list.get(current_count).data.inode_number,
                        dir_info.list.get(current_count).name, dir_info.list
                                .get(current_count).data.type, dir);
            } else {
                return dir_info.list.get(current_count);
            }
        }
        return null;
    }

    /**
     * @param dir
     */
    private void scan2_init_dir(directory dir) {
        dir.buff = new byte[squashfs_constants.SQUASHFS_METADATA_SIZE];

        dir.size = squashfs_constants.SQUASHFS_METADATA_SIZE;
        dir.buffp = dir.index_count_p = 0;
        dir.entry_count = 256;
        dir.entry_count_p = -1;
        dir.index = null;
        dir.i_count = dir.i_size = 0;
    }

    /**
     * @param dir_ent 
     * @param type 
     * @param byte_size 
     * @param start_block 
     * @param offset 
     * @param block_list 
     * @param block_listp 
     * @param fragment 
     * @param dir_in 
     * @return inode.
     * @throws IOException 
     * @throws EasyIOException 
     * @throws SquashFSException 
     */
    private long create_inode(dir_ent dir_ent, int type, long byte_size,
            long start_block, long offset, long[] block_list, int block_listp,
            fragment fragment, directory dir_in) throws EasyIOException,
            IOException, SquashFSException {
        stat buf = dir_ent.inode.buf;
        squashfs_base_inode_header inode_header;
        int inode_number = (int) ((type == squashfs_constants.SQUASHFS_LDIR_TYPE || type == squashfs_constants.SQUASHFS_DIR_TYPE) ? dir_ent.inode.inode_number
                : dir_ent.inode.inode_number + this.dir_inode_no);
        int inode_offset;
        String filename = dir_ent.pathname;
        long nlink = dir_ent.inode.nlink;

        if (type == squashfs_constants.SQUASHFS_FILE_TYPE) {
            inode_header = new squashfs_reg_inode_header();
        }

        else if (type == squashfs_constants.SQUASHFS_DIR_TYPE) {
            inode_header = new squashfs_dir_inode_header();
        }

        else if (type == squashfs_constants.SQUASHFS_SYMLINK_TYPE) {
            inode_header = new squashfs_symlink_inode_header();
        }

        else {
            throw new SquashFSException("unknown type '" + type + "'");
        }

        inode_header.mode = squashfs_constants.SQUASHFS_MODE(buf.st_mode);
        inode_header.uid = get_uid(this.global_uid == -1 ? buf.st_uid
                : this.global_uid);
        inode_header.inode_type = type;
        inode_header.guid = get_guid(this.global_uid == -1 ? buf.st_uid
                : this.global_uid, this.global_gid == -1 ? buf.st_gid
                : this.global_gid);
        inode_header.mtime = buf.st_mtime;
        inode_header.inode_number = inode_number;

        if (type == squashfs_constants.SQUASHFS_FILE_TYPE) {
            int i;

            inode_offset = get_inode(squashfs_constants.SQUASHFS_REG_INODE_HEADER_SIZE
                    + offset * 4);
            ((squashfs_reg_inode_header) inode_header).file_size = byte_size;
            ((squashfs_reg_inode_header) inode_header).start_block = start_block;
            ((squashfs_reg_inode_header) inode_header).fragment = fragment.index;
            ((squashfs_reg_inode_header) inode_header).offset = fragment.offset;
            /*if(!swap) {*/
            ByteArrayOutputStream zbuffer = new ByteArrayOutputStream();
            EasyIOOutputStream zout = new EasyIOOutputStream(zbuffer);
            zout.write(inode_header);
            zout.close();
            RandomAccessByteArray inode_array = get_inode_array(inode_offset);
            EasyIOOutputStream inode_stream = new EasyIOOutputStream(
                    new RandomAccessByteArrayOutputStreamAdapter(inode_array));
            byte[] zbuffer_array = zbuffer.toByteArray();
            inode_stream.write(zbuffer_array, 0, zbuffer_array.length);
            for (int z = 0; z < offset; z++) {
                inode_stream.writeUINT32(block_list[z]);
            }
            /*} else {
             SQUASHFS_SWAP_REG_INODE_HEADER(reg, inodep);
             SQUASHFS_SWAP_INTS(block_list, inodep->block_list, offset);
             }*/
        }

        else if (type == squashfs_constants.SQUASHFS_LREG_TYPE) {
            throw new RuntimeException("not implemented");
            /*
             int i;
             squashfs_lreg_inode_header *reg = &inode_header.lreg, *inodep;

             inode = get_inode(sizeof(*reg) + offset * sizeof(unsigned int));
             inodep = (squashfs_lreg_inode_header *) inode;
             reg->nlink = nlink;
             reg->file_size = byte_size;
             reg->start_block = start_block;
             reg->fragment = fragment->index;
             reg->offset = fragment->offset;
             if(!swap) {
             memcpy(inodep, reg, sizeof(*reg));
             memcpy(inodep->block_list, block_list, offset * sizeof(unsigned int));
             } else {
             SQUASHFS_SWAP_LREG_INODE_HEADER(reg, inodep);
             SQUASHFS_SWAP_INTS(block_list, inodep->block_list, offset);
             }
             TRACE("Long file inode, file_size %lld, start_block %llx, blocks %d, fragment %d, offset %d, size %d, nlink %d\n", byte_size,
             start_block, offset, fragment->index, fragment->offset, fragment->size, nlink);
             for(i = 0; i < offset; i++)
             TRACE("Block %d, size %d\n", i, block_list[i]);
             */
        }

        else if (type == squashfs_constants.SQUASHFS_LDIR_TYPE) {
            throw new RuntimeException("not implemented");

            /* 
             int i;
             unsigned char *p;
             squashfs_ldir_inode_header *dir = &inode_header.ldir, *inodep;
             struct cached_dir_index *index = dir_in->index;
             unsigned int i_count = dir_in->i_count;
             unsigned int i_size = dir_in->i_size;

             if(byte_size >= 1 << 27)
             BAD_ERROR("directory greater than 2^27-1 bytes!\n");

             inode = get_inode(sizeof(*dir) + i_size);
             inodep = (squashfs_ldir_inode_header *) inode;
             dir->inode_type = SQUASHFS_LDIR_TYPE;
             dir->nlink = dir_ent->dir->directory_count + 2;
             dir->file_size = byte_size;
             dir->offset = offset;
             dir->start_block = start_block;
             dir->i_count = i_count;
             dir->parent_inode = dir_ent->our_dir ? dir_ent->our_dir->dir_ent->inode->inode_number : dir_inode_no + inode_no;

             if(!swap)
             memcpy(inode, dir, sizeof(*dir));
             else
             SQUASHFS_SWAP_LDIR_INODE_HEADER(dir, inode);
             p = (unsigned char *) inodep->index;
             for(i = 0; i < i_count; i++) {
             if(!swap)
             memcpy(p, &index[i].index, sizeof(squashfs_dir_index));
             else
             SQUASHFS_SWAP_DIR_INDEX(&index[i].index, p);
             memcpy(((squashfs_dir_index *)p)->name, index[i].name, index[i].index.size + 1);
             p += sizeof(squashfs_dir_index) + index[i].index.size + 1;
             }
             TRACE("Long directory inode, file_size %d, start_block %llx, offset %x, nlink %d\n", (int) byte_size,
             start_block, offset, dir_ent->dir->directory_count + 2);
             */
        }

        else if (type == squashfs_constants.SQUASHFS_DIR_TYPE) {
            inode_offset = get_inode(squashfs_constants.SQUASHFS_DIR_INODE_HEADER_SIZE);
            ((squashfs_dir_inode_header) inode_header).nlink = dir_ent.dir.directory_count + 2;
            ((squashfs_dir_inode_header) inode_header).file_size = byte_size;
            ((squashfs_dir_inode_header) inode_header).offset = offset;
            ((squashfs_dir_inode_header) inode_header).start_block = start_block;
            ((squashfs_dir_inode_header) inode_header).parent_inode = dir_ent.our_dir != null ? dir_ent.our_dir.dir_ent.inode.inode_number
                    : this.dir_inode_no + this.inode_no;
            /* if(!swap) { */
            ByteArrayOutputStream zbuffer = new ByteArrayOutputStream();
            EasyIOOutputStream zout = new EasyIOOutputStream(zbuffer);
            zout.write(inode_header);
            zout.close();
            RandomAccessByteArray inode_array = get_inode_array(inode_offset);
            byte[] zbuffer_array = zbuffer.toByteArray();
            inode_array.write(zbuffer_array, 0, zbuffer_array.length);
            /*} else {
             SQUASHFS_SWAP_DIR_INODE_HEADER(dir, inode);
             }*/
        }

        else if (type == squashfs_constants.SQUASHFS_CHRDEV_TYPE
                || type == squashfs_constants.SQUASHFS_BLKDEV_TYPE) {
            throw new RuntimeException("not implemented");

            /* 

             squashfs_dev_inode_header *dev = &inode_header.dev;

             inode = get_inode(sizeof(*dev));
             dev->nlink = nlink;
             dev->rdev = (unsigned short) ((major(buf->st_rdev) << 8) |
             (minor(buf->st_rdev) & 0xff));
             if(!swap)
             memcpy(inode, dev, sizeof(*dev));
             else
             SQUASHFS_SWAP_DEV_INODE_HEADER(dev, inode);
             TRACE("Device inode, rdev %x, nlink %d\n", dev->rdev, nlink);
             */
        }

        else if (type == squashfs_constants.SQUASHFS_SYMLINK_TYPE) {
            int b;
            byte[] buff = new byte[65536];

            if ((b = readlink(filename, buff, 65536)) == -1) {
                throw new SquashFSException("Error in reading symbolic link");
            }

            if (b == 65536) {
                throw new SquashFSException(
                        "Symlink is greater than 65536 bytes!");
            }

            inode_offset = get_inode(squashfs_constants.SQUASHFS_SYMLINK_INODE_HEADER_SIZE
                    + b);
            ((squashfs_symlink_inode_header) inode_header).nlink = nlink;
            ((squashfs_symlink_inode_header) inode_header).symlink_size = b;
            /* if(!swap) { */
            ByteArrayOutputStream zbuffer = new ByteArrayOutputStream();
            EasyIOOutputStream zout = new EasyIOOutputStream(zbuffer);
            zout.write(inode_header);
            zout.close();
            RandomAccessByteArray inode_array = get_inode_array(inode_offset);
            byte[] zbuffer_array = zbuffer.toByteArray();
            inode_array.write(zbuffer_array, 0, zbuffer_array.length);
            /* } else {
             SQUASHFS_SWAP_SYMLINK_INODE_HEADER(symlink, inode);
             } */
            inode_array.write(buff, 0, b);
        }

        else if (type == squashfs_constants.SQUASHFS_FIFO_TYPE
                || type == squashfs_constants.SQUASHFS_SOCKET_TYPE) {
            throw new RuntimeException("not implemented");

            /* 

             squashfs_ipc_inode_header *ipc = &inode_header.ipc;

             inode = get_inode(sizeof(*ipc));
             ipc->nlink = nlink;
             if(!swap)
             memcpy(inode, ipc, sizeof(*ipc));
             else
             SQUASHFS_SWAP_IPC_INODE_HEADER(ipc, inode);
             TRACE("ipc inode, type %s, nlink %d\n", type == SQUASHFS_FIFO_TYPE ? "fifo" : "socket", nlink);
             */
        }

        else {
            throw new SquashFSException("unknown type '" + type + "'");
        }

        long i_no = MKINODE(inode_offset);
        this.inode_count++;


        return i_no;
    }

    /**
     * @param filename the filename to find.
     * @param buff the buffer to populate with link.
     * @param i the max.
     * @return the number of bytes read.
     * @throws SquashFSException 
     */
    private int readlink(String filename, byte[] buff, int i)
            throws SquashFSException {
        BaseFile bf = this.manifest.find(filename);
        if (bf == null) {
            throw new SquashFSException("could not find file '" + filename
                    + "'");
        }
        if (bf instanceof SymLink) {
            String linkName = ((SymLink) bf).getLinkName();
            int cp = Math.min(i, linkName.length());
            System.arraycopy(linkName.getBytes(), 0, buff, 0, cp);
            return cp;
        }
        throw new SquashFSException("file is not a symbolic link");
    }

    /**
     * @param inode_offset
     * @return inode.
     */
    private long MKINODE(int inode_offset) {
        return ((long) (((long) this.inode_bytes << 16) + inode_offset));
    }

    /**
     * @param req_size
     * @return the offset into {@link SquashFSWriter#data_cache}.
     * @throws IOException 
     * @throws EasyIOException 
     */
    private int get_inode(long req_size) throws IOException, EasyIOException {
        int data_space;
        int c_byte;

        RandomAccessByteArray inode_table_array = new RandomAccessByteArray(
                this.inode_table);
        EasyIOOutputStream inode_table_stream = new EasyIOOutputStream(
                new RandomAccessByteArrayOutputStreamAdapter(inode_table_array));
        RandomAccessByteArray data_cache_array = new RandomAccessByteArray(
                this.data_cache);

        while (this.cache_bytes >= squashfs_constants.SQUASHFS_METADATA_SIZE) {
            if ((this.inode_size - this.inode_bytes) < ((squashfs_constants.SQUASHFS_METADATA_SIZE << 1)) + 2) {
                this.inode_table = realloc(this.inode_table, this.inode_size
                        + (squashfs_constants.SQUASHFS_METADATA_SIZE << 1) + 2);
                this.inode_size += (squashfs_constants.SQUASHFS_METADATA_SIZE << 1) + 2;
            }

            inode_table_array.seek(this.inode_bytes + this.block_offset);
            c_byte = (int) mangle(inode_table_stream, data_cache_array,
                    squashfs_constants.SQUASHFS_METADATA_SIZE,
                    squashfs_constants.SQUASHFS_METADATA_SIZE, this.noI, 0);
            /* if(!swap) { */
            inode_table_array.seek(this.inode_bytes);
            inode_table_stream.writeUINT16(c_byte);
            /* } else {
             SQUASHFS_SWAP_SHORTS((&c_byte), (inode_table + inode_bytes), 1);
             } */
            if (this.check_data) {
                this.inode_table[this.inode_bytes + this.block_offset - 1] = squashfs_constants.SQUASHFS_MARKER_BYTE;
            }
            this.inode_bytes += squashfs_constants
                    .SQUASHFS_COMPRESSED_SIZE(c_byte)
                    + this.block_offset;
            this.total_inode_bytes += squashfs_constants.SQUASHFS_METADATA_SIZE
                    + this.block_offset;
            data_cache_array.seek(0);
            data_cache_array.write(this.data_cache,
                    squashfs_constants.SQUASHFS_METADATA_SIZE, this.cache_bytes
                            - squashfs_constants.SQUASHFS_METADATA_SIZE);
            this.cache_bytes -= squashfs_constants.SQUASHFS_METADATA_SIZE;
        }

        data_space = (int) (this.cache_size - this.cache_bytes);
        if (data_space < req_size) {
            int realloc_size = (int) (this.cache_size == 0 ? ((req_size + squashfs_constants.SQUASHFS_METADATA_SIZE) & ~(squashfs_constants.SQUASHFS_METADATA_SIZE - 1))
                    : req_size - data_space);

            this.data_cache = realloc(this.data_cache,
                    (int) (this.cache_size + realloc_size));
            this.cache_size += realloc_size;
        }

        this.cache_bytes += req_size;

        return (int) (this.cache_bytes - req_size);
    }

    /**
     * @param offset
     * @return the array.
     */
    private RandomAccessByteArray get_inode_array(int offset) {
        RandomAccessByteArray array = new RandomAccessByteArray(this.data_cache);
        array.seek(offset);
        return array;
    }

    /**
     * @param uid
     * @param guid
     * @return z.
     */
    private int get_guid(long uid, long guid) {
        int i;

        if (uid == guid) {
            return squashfs_constants.SQUASHFS_GUIDS;
        }

        for (i = 0; (i < this.guid_count) && this.guids[i] != guid; i++) {
            // empty
        }
        if (i == this.guid_count) {
            if (this.guid_count == squashfs_constants.SQUASHFS_GUIDS) {
                return squashfs_constants.SQUASHFS_GUIDS;
            } else {
                this.guids[this.guid_count++] = guid;
            }
        }

        return i;
    }

    /**
     * @param uid
     * @return z.
     */
    private int get_uid(long uid) {
        int i;

        for (i = 0; (i < this.uid_count) && this.uids[i] != uid; i++) {
            // empty.
        }
        if (i == this.uid_count) {
            if (this.uid_count == squashfs_constants.SQUASHFS_UIDS) {
                i = 0;
            } else {
                this.uids[this.uid_count++] = uid;
            }
        }

        return i;
    }

    /**
     * @param dir_ent
     * @param size
     * @param duplicate_file
     * @return z.
     * @throws SquashFSException 
     * @throws IOException 
     * @throws EasyIOException 
     */
    private long write_file(dir_ent dir_ent, long size, boolean[] duplicate_file)
            throws SquashFSException, IOException, EasyIOException {
        long read_size = (size > squashfs_constants.SQUASHFS_MAX_FILE_SIZE) ? squashfs_constants.SQUASHFS_MAX_FILE_SIZE
                : size;
        int blocks = (int) ((read_size + this.superBlock.block_size - 1) >> this.superBlock.block_log);
        int allocated_blocks = blocks;
        long[] block_list;
        int block_listp;
        long frag_bytes;
        String filename = dir_ent.pathname;
        int whole_file = 1;
        byte[] c_buffer;
        long start;
        int block = 0;
        int i;
        long file_bytes = 0;
        long bbytes;
        byte[] buff = new byte[(int) this.superBlock.block_size];
        long c_byte;
        duplicate_buffer_handle handle = new duplicate_buffer_handle();
        file_info dupl_ptr = null;
        fragment fragment = new fragment();

        RandomAccessByteArray buff_array = new RandomAccessByteArray(buff);

        block_list = new long[blocks];
        block_listp = 0;

        if (!this.no_fragments
                && (read_size < this.superBlock.block_size || this.always_use_fragments)) {
            allocated_blocks = blocks = (int) (read_size >> this.superBlock.block_log);
            frag_bytes = read_size % this.superBlock.block_size;
        } else {
            frag_bytes = 0;
        }

        if (size > read_size) {
            throw new SquashFSException("file " + filename + " truncated to "
                    + squashfs_constants.SQUASHFS_MAX_FILE_SIZE + " bytes\n");
        }

        this.total_bytes += read_size;
        BaseFile bf = this.manifest.find(filename);
        IRandomAccessSource randFile = this.dataProvider.getData(this.manifest,
                bf);

        {
            long bytes_var = (((long) allocated_blocks) + 1) << this.superBlock.block_log;
            c_buffer = new byte[(int) bytes_var];
        }

        RandomAccessByteArray c_buffer_array = new RandomAccessByteArray(
                c_buffer);
        EasyIOOutputStream c_buffer_stream = new EasyIOOutputStream(
                new RandomAccessByteArrayOutputStreamAdapter(c_buffer_array));

        for (start = this.bytes; block < blocks; file_bytes += bbytes) {
            for (i = 0, bbytes = 0; (i < allocated_blocks) && (block < blocks); i++) {
                int available_bytes = (int) (read_size
                        - (block * this.superBlock.block_size) > this.superBlock.block_size ? this.superBlock.block_size
                        : read_size - (block * this.superBlock.block_size));
                randFile.read(buff, 0, available_bytes);
                c_buffer_array.seek((int) bbytes);
                buff_array.seek(0);
                c_byte = mangle(c_buffer_stream, buff_array, available_bytes,
                        (int) this.superBlock.block_size, this.noD, 1);
                block_list[block++] = c_byte;
                bbytes += squashfs_constants
                        .SQUASHFS_COMPRESSED_SIZE_BLOCK(c_byte);
            }
            if (whole_file == 0) {
                this.dest.seek(this.bytes);
                this.dest.write(c_buffer, 0, (int) bbytes);
                this.bytes += bbytes;
            }
        }

        if (frag_bytes != 0) {
            randFile.read(buff, 0, (int) frag_bytes);
        }

        if (whole_file != 0) {
            handle.ptr = 0;
            handle.ptrdata = c_buffer;
            int[] zzblock_listp = new int[] { block_listp };
            long[] zzstart = new long[] { start };
            fragment[] zzfragment = new fragment[] { fragment };
            if (this.duplicate_checking
                    && (dupl_ptr = duplicate(new read_from_buffer(), handle,
                            file_bytes, zzblock_listp, zzstart, blocks,
                            zzfragment, buff, frag_bytes)) == null) {
                duplicate_file[0] = true;
            } else {
                this.dest.seek(this.bytes);
                this.dest.write(c_buffer, 0, (int) file_bytes);
                this.bytes += file_bytes;
            }
        } else {
            handle.start = start;
            int[] zzblock_listp = new int[] { block_listp };
            long[] zzstart = new long[] { start };
            fragment[] zzfragment = new fragment[] { fragment };
            if (this.duplicate_checking
                    && (dupl_ptr = duplicate(new read_from_file(), handle,
                            file_bytes, zzblock_listp, zzstart, blocks,
                            zzfragment, buff, frag_bytes)) == null) {
                start = zzstart[0];
                this.bytes = start;
                if (this.block_device == 0) {
                    this.dest.setLength(this.bytes);
                }
                duplicate_file[0] = true;
            }
        }

        if (!duplicate_file[0]) {
            fragment = get_and_fill_fragment(buff, frag_bytes);
            if (this.duplicate_checking) {
                dupl_ptr.fragment = fragment;
            }

            duplicate_file[0] = false;
        }

        this.file_count++;
        if (dir_ent.inode.nlink == 1 && read_size < ((long) (1 << 30) - 1)) {
            return create_inode(dir_ent, squashfs_constants.SQUASHFS_FILE_TYPE,
                    read_size, start, (long) blocks, block_list, block_listp,
                    fragment, null);
        } else {
            return create_inode(dir_ent, squashfs_constants.SQUASHFS_LREG_TYPE,
                    read_size, start, (long) blocks, block_list, block_listp,
                    fragment, null);
        }
    }

    /**
     * @param get_next_file_block
     * @param file_start
     * @param bytesarg
     * @param block_list
     * @param start
     * @param blocks
     * @param fragment
     * @param frag_data
     * @param frag_bytes
     * @return z.
     * @throws IOException 
     */
    private file_info duplicate(IReadFrom get_next_file_block,
            duplicate_buffer_handle file_start, long bytesarg,
            int[] block_list, long[] start, int blocks, fragment[] fragment,
            byte[] frag_data, long frag_bytes) throws IOException {
        int checksum = get_checksum(get_next_file_block, file_start, bytesarg);
        duplicate_buffer_handle handle = new duplicate_buffer_handle();
        handle.ptrdata = frag_data;
        int fragment_checksum = get_checksum(new read_from_buffer(), handle,
                frag_bytes);
        file_info dupl_ptr;

        dupl_ptr = bytesarg != 0 ? this.dupl[checksum]
                : this.frag_dups[fragment_checksum];

        for (; dupl_ptr != null; dupl_ptr = dupl_ptr.next)
            if (bytesarg == dupl_ptr.bytes
                    && frag_bytes == dupl_ptr.fragment.size
                    && fragment_checksum == dupl_ptr.fragment_checksum) {
                byte[] buffer1 = new byte[(int) squashfs_constants.SQUASHFS_FILE_MAX_SIZE];
                long dup_bytes = dupl_ptr.bytes;
                long dup_start = dupl_ptr.start;
                duplicate_buffer_handle position = file_start;
                byte[] buffer;
                while (dup_bytes != 0) {
                    int avail_bytes = (int) (dup_bytes > squashfs_constants.SQUASHFS_FILE_MAX_SIZE ? squashfs_constants.SQUASHFS_FILE_MAX_SIZE
                            : dup_bytes);

                    buffer = get_next_file_block.read_from(position,
                            avail_bytes);
                    this.dest.seek(dup_start);
                    this.dest.read(buffer1, 0, avail_bytes);
                    if (memcmp(buffer, buffer1, avail_bytes) != 0) {
                        break;
                    }
                    dup_bytes -= avail_bytes;
                    dup_start += avail_bytes;
                }
                if (dup_bytes == 0) {
                    byte[] fragment_buffer1;

                    if (dupl_ptr.fragment.index == this.superBlock.fragments
                            || dupl_ptr.fragment.index == squashfs_constants.SQUASHFS_INVALID_FRAG) {
                        fragment_buffer1 = copyArrayFromOffset(
                                this.fragment_data, dupl_ptr.fragment.offset);
                    }

                    else if (dupl_ptr.fragment.index == this.cached_frag1) {
                        fragment_buffer1 = copyArrayFromOffset(
                                this.cached_fragment, dupl_ptr.fragment.offset);
                    }

                    else {
                        fragment_buffer1 = get_fragment(this.cached_fragment,
                                dupl_ptr.fragment);
                        this.cached_frag1 = dupl_ptr.fragment.index;
                    }

                    if (frag_bytes == 0
                            || memcmp(frag_data, fragment_buffer1,
                                    (int) frag_bytes) == 0) {
                        block_list[0] = dupl_ptr.block_list;
                        start[0] = dupl_ptr.start;
                        fragment[0] = dupl_ptr.fragment;
                        return null;
                    }
                }
            }

        dupl_ptr = new file_info();

        dupl_ptr.bytes = bytesarg;
        dupl_ptr.checksum = checksum;
        dupl_ptr.start = start[0];
        dupl_ptr.fragment_checksum = fragment_checksum;
        dupl_ptr.block_list = block_list[0];

        this.dup_files++;
        if (bytesarg != 0) {
            dupl_ptr.next = this.dupl[checksum];
            this.dupl[checksum] = dupl_ptr;
        } else {
            dupl_ptr.next = this.frag_dups[fragment_checksum];
            this.frag_dups[fragment_checksum] = dupl_ptr;
        }

        return dupl_ptr;
    }

    /**
     * @param buffer
     * @param fragment
     * @return z.
     * @throws IOException 
     */
    private byte[] get_fragment(byte[] buffer, fragment fragment)
            throws IOException {
        squashfs_fragment_entry disk_fragment = this.fragment_table[fragment.index];
        int size = (int) squashfs_constants
                .SQUASHFS_COMPRESSED_SIZE_BLOCK(disk_fragment.size);

        if (squashfs_constants.SQUASHFS_COMPRESSED_BLOCK(disk_fragment.size)) {
            byte[] cbuffer = new byte[(int) this.superBlock.block_size];

            this.dest.seek(disk_fragment.start_block);
            this.dest.read(cbuffer, 0, size);

            InflaterInputStream in = new InflaterInputStream(
                    new ByteArrayInputStream(cbuffer));
            in.read(buffer);
            in.close();
        } else {
            this.dest.seek(disk_fragment.start_block);
            this.dest.read(buffer, 0, size);
        }

        return copyArrayFromOffset(buffer, fragment.offset);
    }

    /**
     * @param buffer
     * @param offset
     * @return new array.
     */
    private byte[] copyArrayFromOffset(byte[] buffer, int offset) {
        byte[] result = new byte[buffer.length - offset];
        System.arraycopy(buffer, offset, result, 0, result.length);
        return result;
    }

    /**
     * @param buffer
     * @param buffer1
     * @param avail_bytes
     * @return z.
     */
    private int memcmp(byte[] buffer, byte[] buffer1, int avail_bytes) {
        for (int i = 0; i < avail_bytes; i++) {
            if (buffer[i] < buffer1[i]) {
                return -1;
            }
            if (buffer[i] > buffer1[i]) {
                return 1;
            }
        }
        return 0;
    }

    /**
     * @param get_next_file_block
     * @param handle
     * @param l
     * @return the checksum.
     * @throws IOException 
     */
    private int get_checksum(IReadFrom get_next_file_block,
            duplicate_buffer_handle handle, long l) throws IOException {
        int chksum = 0;
        long bytes_var = 0;
        byte[] b;
        int bp;
        duplicate_buffer_handle position = handle;

        while (l != 0) {
            bytes_var = l > squashfs_constants.SQUASHFS_FILE_MAX_SIZE ? squashfs_constants.SQUASHFS_FILE_MAX_SIZE
                    : l;
            l -= bytes_var;
            b = get_next_file_block.read_from(position, bytes_var);
            bp = 0;
            while (bytes_var-- != 0) {
                chksum = (chksum & 1) != 0 ? (chksum >> 1) | 0x8000
                        : chksum >> 1;
                chksum += b[bp++];
            }
        }

        return chksum;
    }

    /**
     * @param buff
     * @param size 
     * @return z.
     * @throws IOException 
     */
    private fragment get_and_fill_fragment(byte[] buff, long size)
            throws IOException {
        fragment ffrg;

        if (size == 0)
            return empty_fragment;

        if (this.fragment_size + size > this.superBlock.block_size) {
            write_fragment();
        }

        ffrg = new fragment();

        ffrg.index = (int) this.superBlock.fragments;
        ffrg.offset = this.fragment_size;
        ffrg.size = size;
        RandomAccessByteArray array = new RandomAccessByteArray(
                this.fragment_data);
        array.seek(this.fragment_size);
        array.write(buff, 0, (int) size);
        this.fragment_size += size;

        return ffrg;
    }

    /**
     * @param path
     * @param destDir
     * @return the dir info.
     * @throws IOException 
     */
    private dir_info dir_scan1(Directory path, String destDir)
            throws IOException {
        dir_info dir, sub_dir;
        stat buf;
        String filename;
        String dir_name;

        if ((dir = scan1_opendir(path)) == null) {
            return dir;
        }

        for (BaseFile f : path.getSubentries()) {
            filename = destDir + "/" + f.getName();
            dir_name = f.getName();
            buf = fromBaseFile(f);

            /*
             if(excluded(filename, &buf))
             continue;
             */

            if (f instanceof Directory) {
                if ((sub_dir = dir_scan1((Directory) f, filename)) == null)
                    continue;
                dir.directory_count++;
            } else
                sub_dir = null;

            add_dir_entry(dir_name, filename, sub_dir, lookup_inode(buf), null,
                    dir);
        }

        scan1_freedir(dir);
        sort_directory(dir);

        return dir;
    }

    /**
     * @param name
     * @param pathname
     * @param sub_dir
     * @param inode_info_param
     * @param data 
     * @param dir
     */
    private void add_dir_entry(String name, String pathname, dir_info sub_dir,
            inode_info inode_info_param, Object data, dir_info dir) {

        dir_ent dirent = new dir_ent();
        if (dir.list == null) {
            dir.list = new ArrayList<dir_ent>();
        }
        dir.list.add(dirent);

        if (sub_dir != null)
            sub_dir.dir_ent = dirent;
        dirent.name = name;
        dirent.pathname = pathname != null ? pathname : null;
        dirent.inode = inode_info_param;
        dirent.dir = sub_dir;
        dirent.our_dir = dir;
        dirent.data = (old_root_entry_info) data;
        dir.count++;
        dir.byte_count += pathname.length()
                + squashfs_constants.SQUASHFS_DIR_ENTRY_SIZE;
    }

    /**
     * @param buf
     * @return the inode info.
     */
    private inode_info lookup_inode(stat buf) {
        inode_info inode, inodefound;

        inodefound = inode = this.inode_info.get(buf.st_dev + "" + buf.st_ino);

        while (inode != null) {
            if (buf.equals(inode.buf)) {
                inode.nlink++;
                return inode;
            }
            inode = inode.next;
        }

        inode = new inode_info();

        try {
            if (buf == null) {
                inode.buf = null;
            } else {
                inode.buf = (stat) buf.clone();
            }
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

        inode.inode = squashfs_constants.SQUASHFS_INVALID_BLK;
        inode.nlink = 1;
        if ((buf.st_mode & stat.S_IFMT) == stat.S_IFDIR) {
            inode.inode_number = this.dir_inode_no++;
        } else {
            inode.inode_number = this.inode_no++;
        }

        inode.next = inodefound;
        this.inode_info.put(buf.st_dev + "" + buf.st_ino, inode);

        return inode;
    }

    /**
     * @param dir
     */
    private void sort_directory(dir_info dir) {
        if (dir.list == null) {
            dir.list = new ArrayList<dir_ent>();
        }
        Collections.sort(dir.list, new Comparator<dir_ent>() {

            public int compare(dir_ent o1, dir_ent o2) {
                if (o1.name == null || o2.name == null) {
                    return 0;
                }
                return o1.name.compareTo(o2.name);
            }
        });

        if ((dir.count < 257 && dir.byte_count < squashfs_constants.SQUASHFS_METADATA_SIZE)) {
            dir.dir_is_ldir = false;
        }
    }

    /**
     * @param dir
     */
    private void scan1_freedir(dir_info dir) {
        // do nothing.
    }

    /**
     * @param path
     * @return the open dir.
     */
    private dir_info scan1_opendir(Directory path) {
        dir_info dir;

        dir = new dir_info();

        if (path == null || path.getName() == null) {
            dir.pathname = ".";
        } else {
            dir.pathname = "./" + this.manifest.getPath(path);
        }
        dir.count = dir.directory_count = dir.current_count = dir.byte_count = 0;
        dir.dir_is_ldir = true;
        dir.list = null;

        return dir;
    }

    /**
     * 
     */
    private interface IReadFrom {
        /**
         * @param handle
         * @param avail_bytes
         * @return data.
         * @throws IOException 
         */
        byte[] read_from(duplicate_buffer_handle handle, long avail_bytes)
                throws IOException;
    }

    /**
     * 
     */
    private class read_from_buffer implements IReadFrom {

        /**
         * {@inheritDoc}
         */
        public byte[] read_from(duplicate_buffer_handle handle, long avail_bytes) {
            byte[] v = new byte[handle.ptrdata.length];
            System.arraycopy(handle.ptrdata, handle.ptr, v, 0, Math.min(
                    v.length, handle.ptrdata.length - handle.ptr));
            handle.ptr += avail_bytes;
            return v;
        }
    }

    /**
     * 
     */
    private class read_from_file implements IReadFrom {

        /**
         * 
         */
        byte[] read_from_file_buffer = new byte[(int) squashfs_constants.SQUASHFS_FILE_MAX_SIZE];

        /**
         * {@inheritDoc}
         * @throws IOException 
         */
        public byte[] read_from(duplicate_buffer_handle handle, long avail_bytes)
                throws IOException {
            SquashFSWriter.this.dest.seek(handle.start);
            SquashFSWriter.this.dest.read(this.read_from_file_buffer, 0,
                    (int) avail_bytes);
            handle.start += avail_bytes;
            return this.read_from_file_buffer;
        }
    }

    /**
     * @param bf
     * @return the stat
     * @throws IOException 
     */
    private stat fromBaseFile(BaseFile bf) throws IOException {
        stat result = new stat();
        result.st_mode = bf.getMode();
        result.st_uid = bf.getUid();
        result.st_gid = bf.getGuid();
        result.st_mtime = bf.getMTime();
        result.st_dev = this.st_dev;
        result.st_ino = this.dataProvider.getIno(this.manifest, bf);
        result.st_size = this.dataProvider.getLength(this.manifest, bf);
        return result;
    }

    /**
     * @param system the system to set
     */
    public static void setSystem(SquashFSSystem system) {
        SquashFSWriter.system = system;
    }

}
