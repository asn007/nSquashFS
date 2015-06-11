/**
 * 
 */
package com.fernsroth.squashfs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.InflaterInputStream;


import com.fernsroth.easyio.EasyIOFormatter;
import com.fernsroth.easyio.EasyIOInputStream;
import com.fernsroth.easyio.IRandomAccessSource;
import com.fernsroth.easyio.exception.EasyIOException;
import com.fernsroth.squashfs.model.Directory;
import com.fernsroth.squashfs.model.SFSSquashedFile;
import com.fernsroth.squashfs.model.SymLink;
import com.fernsroth.squashfs.model.squashfs.dir;
import com.fernsroth.squashfs.model.squashfs.dir_ent;
import com.fernsroth.squashfs.model.squashfs.squashfs_base_inode_header;
import com.fernsroth.squashfs.model.squashfs.squashfs_constants;
import com.fernsroth.squashfs.model.squashfs.squashfs_dir_entry;
import com.fernsroth.squashfs.model.squashfs.squashfs_dir_header;
import com.fernsroth.squashfs.model.squashfs.squashfs_dir_inode_header;
import com.fernsroth.squashfs.model.squashfs.squashfs_fragment_entry;
import com.fernsroth.squashfs.model.squashfs.squashfs_ldir_inode_header;
import com.fernsroth.squashfs.model.squashfs.squashfs_reg_inode_header;
import com.fernsroth.squashfs.model.squashfs.squashfs_super_block;
import com.fernsroth.squashfs.model.squashfs.squashfs_symlink_inode_header;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public final class SquashFSReader {

    /**
     * logging.
     */

    /**
     * the read in directory. 
     */
    private Directory rootDirectory;

    /**
     * the source to read.
     */
    private IRandomAccessSource source;

    /**
     * the super block.
     */
    private squashfs_super_block superBlock;

    /**
     * see unsquashfs.c.
     */
    private long[] uid_table;

    /**
     * see unsquashfs.c.
     */
    private long[] guid_table;

    /**
     * see unsquashfs.c.
     */
    private squashfs_fragment_entry[] fragment_table;

    /**
     * see unsquashfs.c.
     */
    private ByteArrayOutputStream inode_table = new ByteArrayOutputStream();

    /**
     * see unsquashfs.c.
     * <start, offset>
     */
    private Map<Long, Long> inode_table_hash = new HashMap<Long, Long>();

    /**
     * see unsquashfs.c.
     */
    private ByteArrayOutputStream directory_table = new ByteArrayOutputStream();

    /**
     * see unsquashfs.c.
     * <start, offset>
     */
    private Map<Long, Long> directory_table_hash = new HashMap<Long, Long>();

    /**
     * constructor.
     * @param source the data source to read.
     * @throws IOException 
     * @throws EasyIOException 
     */
    public SquashFSReader(IRandomAccessSource source) throws EasyIOException,
            IOException {
        this.source = source;
        this.source.seek(0);

        read_super();

        read_uids_guids();
        read_fragment_table();
        uncompress_inode_table(this.superBlock.inode_table_start,
                this.superBlock.directory_table_start);
        uncompress_directory_table(this.superBlock.directory_table_start,
                this.superBlock.fragment_table_start);
        this.rootDirectory = dir_scan(null, squashfs_constants
                .SQUASHFS_INODE_BLK(this.superBlock.root_inode),
                squashfs_constants
                        .SQUASHFS_INODE_OFFSET(this.superBlock.root_inode));
    }

    /**
     * @param name the name of the directory.
     * @param start_block the block to start reading at.
     * @param offset the offset of the first block.
     * @return the directory.
     * @throws IOException 
     * @throws EasyIOException 
     */
    private Directory dir_scan(String name, long start_block, long offset)
            throws IOException, EasyIOException {
        dir dir = squashfs_openddir(start_block, offset);
        if (dir == null) {
            throw new IOException("dir_scan: Failed to read directory ("
                    + start_block + ":" + offset + ")");
        }
        Directory result = new Directory(name, dir.mode, dir.mtime, getGuid(
                dir.guid, dir.uid), getUid(dir.uid));

        if (dir.dirs != null) {
            for (dir_ent dirent : dir.dirs) {

                switch (dirent.type) {
                case squashfs_constants.SQUASHFS_DIR_TYPE:
                    Directory subdir = dir_scan(dirent.name,
                            dirent.start_block, dirent.offset);
                    result.addSubentry(subdir);
                    break;

                case squashfs_constants.SQUASHFS_FILE_TYPE:
                    SFSSquashedFile subfile = read_file(dirent.name,
                            dirent.start_block, dirent.offset);
                    result.addSubentry(subfile);
                    break;

                case squashfs_constants.SQUASHFS_SYMLINK_TYPE:
                    SymLink subsymlink = read_symlink(dirent.name,
                            dirent.start_block, dirent.offset);
                    result.addSubentry(subsymlink);
                    break;

                case squashfs_constants.SQUASHFS_BLKDEV_TYPE:
                    throw new RuntimeException("not implemented");

                case squashfs_constants.SQUASHFS_CHRDEV_TYPE:
                    throw new RuntimeException("not implemented");

                case squashfs_constants.SQUASHFS_FIFO_TYPE:
                    throw new RuntimeException("not implemented");

                case squashfs_constants.SQUASHFS_SOCKET_TYPE:
                    throw new RuntimeException("not implemented");

                case squashfs_constants.SQUASHFS_LDIR_TYPE:
                    throw new RuntimeException("not implemented");

                case squashfs_constants.SQUASHFS_LREG_TYPE:
                    throw new RuntimeException("not implemented");

                }
            }
        }
        return result;
    }

    /**
     * reads a symbolic link from the squashfs.
     * @param name the name of the link.
     * @param start_block the start block.
     * @param offset the offset.
     * @return the symbolic link object.
     * @throws IOException 
     * @throws EasyIOException 
     */
    private SymLink read_symlink(String name, long start_block, long offset)
            throws EasyIOException, IOException {
        long start = this.superBlock.inode_table_start + start_block;
        long block_ptr;
        long bytes = this.inode_table_hash.get(start);
        byte[] inode_table_bytes = this.inode_table.toByteArray();

        if (bytes == -1) {
            throw new IOException(
                    "create_inode: inode block start out of range!");
        }
        block_ptr = bytes + offset;

        /*
         if(swap) {
         squashfs_base_inode_header sinode;
         memcpy(&sinode, block_ptr, sizeof(header.base));
         SQUASHFS_SWAP_BASE_INODE_HEADER(&header.base, &sinode, sizeof(squashfs_base_inode_header));
         } else {
         */
        EasyIOInputStream in = new EasyIOInputStream(new ByteArrayInputStream(
                inode_table_bytes, (int) block_ptr,
                squashfs_constants.SQUASHFS_SYMLINK_INODE_HEADER_SIZE));
        squashfs_symlink_inode_header inodep = in
                .read(new squashfs_symlink_inode_header());
        /* } */


        inodep.symlink = new String(
                inode_table_bytes,
                (int) (block_ptr + squashfs_constants.SQUASHFS_SYMLINK_INODE_HEADER_SIZE),
                inodep.symlink_size);

        return new SymLink(name, (int) inodep.mode, inodep.mtime, getGuid(
                inodep.guid, inodep.uid), getUid(inodep.uid), inodep.symlink);
    }

    /**
     * @param uid
     * @return get uid.
     */
    private long getUid(long uid) {
        return this.uid_table[(int) uid];
    }

    /**
     * @param guid
     * @param uid
     * @return get the guid.
     */
    private long getGuid(long guid, long uid) {
        return (guid == squashfs_constants.SQUASHFS_GUIDS) ? uid
                : this.guid_table[(int) guid];
    }

    /**
     * reads a file from the squashfs.
     * @param name the name of the file.
     * @param start_block the start block.
     * @param offset the offset.
     * @return the file object.
     * @throws IOException 
     * @throws EasyIOException 
     */
    private SFSSquashedFile read_file(String name, long start_block, long offset)
            throws IOException, EasyIOException {
        long start = this.superBlock.inode_table_start + start_block;
        long block_ptr;
        long bytes = this.inode_table_hash.get(start);
        byte[] inode_table_bytes = this.inode_table.toByteArray();

        if (bytes == -1) {
            throw new IOException(
                    "create_inode: inode block start out of range!");
        }
        block_ptr = bytes + offset;

        /*
         if(swap) {
         squashfs_base_inode_header sinode;
         memcpy(&sinode, block_ptr, sizeof(header.base));
         SQUASHFS_SWAP_BASE_INODE_HEADER(&header.base, &sinode, sizeof(squashfs_base_inode_header));
         } else {
         */
        EasyIOInputStream in = new EasyIOInputStream(new ByteArrayInputStream(
                inode_table_bytes, (int) block_ptr,
                squashfs_constants.SQUASHFS_REG_INODE_HEADER_SIZE));
        squashfs_reg_inode_header inode = in
                .read(new squashfs_reg_inode_header());
        /* } */

        int blocks;

        long frag_bytes = (inode.fragment == squashfs_constants.SQUASHFS_INVALID_FRAG) ? 0
                : (inode.file_size % this.superBlock.block_size);
        offset = inode.offset;
        blocks = (int) (inode.fragment == squashfs_constants.SQUASHFS_INVALID_FRAG ? (inode.file_size
                + this.superBlock.block_size - 1) >> this.superBlock.block_log
                : inode.file_size >> this.superBlock.block_log);
        start = inode.start_block;


        squashfs_fragment_entry fragmentEntry = null;
        if (frag_bytes != 0) {
            fragmentEntry = this.fragment_table[(int) inode.fragment];
        }

        return new SFSSquashedFile(name, (int) inode.mode, inode.mtime,
                getGuid(inode.guid, inode.uid), getUid(inode.uid), start,
                blocks, offset, block_ptr
                        + squashfs_constants.SQUASHFS_REG_INODE_HEADER_SIZE,
                fragmentEntry, frag_bytes);
    }

    /**
     * @param block_start the start block.
     * @param offset the offset.
     * @return the new dir entry.
     * @throws IOException 
     * @throws EasyIOException 
     */
    private dir squashfs_openddir(long block_start, long offset)
            throws IOException, EasyIOException {
        long start = this.superBlock.inode_table_start + block_start;
        Long bytes = this.inode_table_hash.get(start);
        squashfs_base_inode_header header;
        long dir_count, size;
        byte[] directory_table_bytes = this.directory_table.toByteArray();
        byte[] inode_table_bytes = this.inode_table.toByteArray();

        if (bytes == -1) {
            throw new IOException("squashfs_opendir: inode block "
                    + block_start + " not found!");
        }

        int baOffset = (int) (bytes + offset);
        int baLength = (int) (inode_table_bytes.length - (bytes + offset));
        EasyIOInputStream in = new EasyIOInputStream(new ByteArrayInputStream(
                inode_table_bytes, baOffset, baLength));

        /*
         if(swap) {
         squashfs_dir_inode_header sinode;
         memcpy(&sinode, block_ptr, sizeof(header.dir));
         SQUASHFS_SWAP_DIR_INODE_HEADER(&header.dir, &sinode);
         } else {
         */
        header = in.read(new squashfs_dir_inode_header());
        /* { */

        switch ((int) header.inode_type) {
        case squashfs_constants.SQUASHFS_DIR_TYPE:
            block_start = ((squashfs_dir_inode_header) header).start_block;
            offset = ((squashfs_dir_inode_header) header).offset;
            size = ((squashfs_dir_inode_header) header).file_size;
            break;
        case squashfs_constants.SQUASHFS_LDIR_TYPE:
            /*
             if(swap) {
             squashfs_ldir_inode_header sinode;
             memcpy(&sinode, block_ptr, sizeof(header.ldir));
             SQUASHFS_SWAP_LDIR_INODE_HEADER(&header.ldir, &sinode);
             } else { */
            in = new EasyIOInputStream(new ByteArrayInputStream(
                    inode_table_bytes, (int) (bytes + offset),
                    (int) (inode_table_bytes.length - (bytes + offset))));
            header = in.read(new squashfs_ldir_inode_header());
            /* } */
            block_start = ((squashfs_ldir_inode_header) header).start_block;
            offset = ((squashfs_ldir_inode_header) header).offset;
            size = ((squashfs_ldir_inode_header) header).file_size;
            break;
        default:
            throw new IOException("squashfs_opendir: inode not a directory");
        }

        start = this.superBlock.directory_table_start + block_start;
        bytes = this.directory_table_hash.get(start);

        if (bytes == null) {
            throw new IOException("squashfs_opendir: directory block "
                    + block_start + " not found!");
        }
        bytes += offset;
        size += bytes - 3;

        squashfs_dir_entry dire = new squashfs_dir_entry();
        squashfs_dir_header dirh = new squashfs_dir_header();
        dir dir = new dir();

        dir.dir_count = 0;
        dir.cur_entry = 0;
        dir.mode = (int) header.mode;
        dir.uid = header.uid;
        dir.guid = header.guid;
        dir.mtime = header.mtime;
        dir.dirs = null;

        while (bytes < size) {
            /*
             if(swap) {
             squashfs_dir_header sdirh;
             memcpy(&sdirh, directory_table + bytes, sizeof(sdirh));
             SQUASHFS_SWAP_DIR_HEADER(&dirh, &sdirh);
             } else {
             */
            baOffset = (int) bytes.longValue();
            baLength = squashfs_constants.SQUASHFS_DIR_HEADER_SIZE;
            in = new EasyIOInputStream(new ByteArrayInputStream(
                    directory_table_bytes, baOffset, baLength));
            dirh = in.read(new squashfs_dir_header());
            /* } */


            dir_count = dirh.count + 1;
            bytes += squashfs_constants.SQUASHFS_DIR_HEADER_SIZE;

            while (dir_count-- != 0) {
                /*
                 if(swap) {
                 squashfs_dir_entry sdire;
                 memcpy(&sdire, directory_table + bytes, sizeof(sdire));
                 SQUASHFS_SWAP_DIR_ENTRY(dire, &sdire);
                 } else {
                 */
                baOffset = (int) bytes.longValue();
                baLength = squashfs_constants.SQUASHFS_DIR_ENTRY_SIZE;
                in = new EasyIOInputStream(new ByteArrayInputStream(
                        directory_table_bytes, baOffset, baLength));
                dire = in.read(new squashfs_dir_entry());
                /* } */
                bytes += squashfs_constants.SQUASHFS_DIR_ENTRY_SIZE;

                dire.name = new String(directory_table_bytes, (int) bytes
                        .longValue(), dire.size + 1);

                if ((dir.dir_count % squashfs_constants.DIR_ENT_SIZE) == 0) {
                    /*TODO not sure.
                     if((new_dir = realloc(dir->dirs, (dir->dir_count + DIR_ENT_SIZE) * sizeof(struct dir_ent))) == NULL) {
                     log.error("squashfs_opendir: realloc failed!\n");
                     free(dir->dirs);
                     free(dir);
                     return NULL;
                     }
                     */
                    //TODO dir.dirs = new_dir;
                }
                dir_ent dirent = new dir_ent();
                dirent.name = dire.name;
                dirent.start_block = dirh.start_block;
                dirent.offset = dire.offset;
                dirent.type = dire.type;
                if (dir.dirs == null) {
                    dir.dirs = new ArrayList<dir_ent>();
                }
                dir.dirs.add(dirent);
                dir.dir_count++;
                bytes += dire.size + 1;
            }
        }

        return dir;
    }

    /**
     * @param start start offset.
     * @param end end offset.
     * @throws IOException 
     */
    private void uncompress_directory_table(long start, long end)
            throws IOException {
        while (start < end) {

            long[] next = new long[1];
            byte[] readdata = read_block(start, next);
            this.directory_table_hash.put(start, (long) this.directory_table
                    .size());
            this.directory_table.write(readdata, 0, readdata.length);
            start = next[0];
        }
    }

    /**
     * @param start start offset.
     * @param end end offset.
     * @throws IOException 
     */
    private void uncompress_inode_table(long start, long end)
            throws IOException {
        while (start < end) {

            long[] next = new long[1];
            byte[] readdata = read_block(start, next);
            this.inode_table_hash.put(start, (long) this.inode_table.size());
            this.inode_table.write(readdata, 0, readdata.length);
            start = next[0];
        }
    }

    /**
     * @param squashfs the squashfs to read into.
     * @throws IOException 
     * @throws EasyIOException 
     */
    private void read_super() throws EasyIOException, IOException {
        EasyIOInputStream easyin = new EasyIOInputStream(this.source);

        this.superBlock = easyin.read(new squashfs_super_block());
        if (this.superBlock.s_magic != squashfs_constants.SQUASHFS_MAGIC) {
            throw new IOException("magic does not match");
        }
    }

    /**
     * @param squashfs the squash fs structure.
     * @throws IOException
     */
    private void read_uids_guids() throws IOException {
        this.uid_table = new long[this.superBlock.no_uids];
        this.guid_table = new long[this.superBlock.no_guids];

        this.source.seek((int) this.superBlock.uid_start);

        EasyIOInputStream in = new EasyIOInputStream(this.source);
        for (int i = 0; i < this.uid_table.length; i++) {
            this.uid_table[i] = in.readUINT32();
        }
        for (int i = 0; i < this.guid_table.length; i++) {
            this.guid_table[i] = in.readUINT32();
        }
    }

    /**
     * @throws IOException
     * @throws EasyIOException 
     */
    private void read_fragment_table() throws IOException, EasyIOException {
        EasyIOInputStream in = new EasyIOInputStream(this.source);
        int i, indexes = (int) squashfs_constants
                .SQUASHFS_FRAGMENT_INDEXES(this.superBlock.fragments);
        long[] fragment_table_index = new long[indexes];
        if (this.superBlock.fragments == 0) {
            return;
        }

        this.fragment_table = new squashfs_fragment_entry[(int) this.superBlock.fragments];

        /*
         if(squashfs.swap) {
         long[] sfragment_table_index = new long[indexes];
         read_bytes(squashfs.superBlock.fragment_table_start, SQUASHFS_FRAGMENT_INDEX_BYTES(squashfs.superBlock.fragments), (char *) sfragment_table_index);
         SQUASHFS_SWAP_FRAGMENT_INDEXES(fragment_table_index, sfragment_table_index, indexes);
         } else {
         */
        this.source.seek((int) this.superBlock.fragment_table_start);
        for (i = 0; i < squashfs_constants
                .SQUASHFS_FRAGMENT_INDEX_BYTES(this.superBlock.fragments) / 8; i++) {
            fragment_table_index[i] = in.readINT64();
        }
        /*}*/

        for (i = 0; i < indexes; i++) {
            byte[] buffer = read_block(fragment_table_index[i], null);
            EasyIOInputStream fragmentIn = new EasyIOInputStream(
                    new ByteArrayInputStream(buffer));
            this.fragment_table[i * squashfs_constants.SQUASHFS_METADATA_SIZE] = fragmentIn
                    .read(new squashfs_fragment_entry());
        }

        /*
         if(swap) {
         squashfs_fragment_entry sfragment;
         for(i = 0; i < sBlk->fragments; i++) {
         SQUASHFS_SWAP_FRAGMENT_ENTRY((&sfragment), (&fragment_table[i]));
         memcpy((char *) &fragment_table[i], (char *) &sfragment, sizeof(squashfs_fragment_entry));
         }
         }
         */
    }

    /**
     * reads a block.
     * @param start the start of the block.
     * @param next the next block location (1 length array).
     * @return the data.
     * @throws IOException 
     */
    private byte[] read_block(long start, long[] next) throws IOException {
        int c_byte;
        int offset = 2;

        this.source.seek((int) start);
        EasyIOInputStream in = new EasyIOInputStream(this.source);

        /*
         if(swap) {
         if(read_bytes(start, 2, block) == FALSE)
         goto failed;
         ((unsigned char *) &c_byte)[1] = block[0];
         ((unsigned char *) &c_byte)[0] = block[1]; 
         } else
         */
        c_byte = in.readUINT16();


        if (squashfs_constants.SQUASHFS_CHECK_DATA(this.superBlock.flags))
            offset = 3;
        if (squashfs_constants.SQUASHFS_COMPRESSED(c_byte)) {
            byte[] buffer = new byte[squashfs_constants.SQUASHFS_METADATA_SIZE];

            c_byte = squashfs_constants.SQUASHFS_COMPRESSED_SIZE(c_byte);
            this.source.seek((int) (start + offset));
            if (this.source.read(buffer, 0, c_byte) != c_byte) {
                throw new IOException("read past end of stream");
            }

            byte[] block = uncompress(buffer, c_byte);

            if (next != null) {
                next[0] = start + offset + c_byte;
            }
            return block;
        } else {
            c_byte = squashfs_constants.SQUASHFS_COMPRESSED_SIZE(c_byte);
            this.source.seek((int) (start + offset));
            byte[] block = new byte[c_byte];
            if (this.source.read(block, 0, c_byte) != c_byte) {
                throw new IOException("read past end of file");
            }
            if (next != null) {
                next[0] = start + offset + c_byte;
            }
            return block;
        }
    }

    /**
     * reads a data block.
     * @param start the start offset.
     * @param size the size.
     * @return the data read.
     * @throws IOException
     */
    private byte[] read_data_block(long start, long size) throws IOException {
        if (size == 0) {
            return new byte[0];
        }

        long c_byte = squashfs_constants.SQUASHFS_COMPRESSED_SIZE_BLOCK(size);


        byte[] readdata = new byte[(int) c_byte];
        this.source.seek((int) start);
        int rc;
        if ((rc = this.source.read(readdata)) != readdata.length) {
            throw new IOException("could not read file at offset " + start
                    + " read " + rc + " bytes, expected " + readdata.length
                    + " bytes.");
        }

        if (squashfs_constants.SQUASHFS_COMPRESSED_BLOCK(size)) {
            readdata = uncompress(readdata, readdata.length);
        }
        return readdata;
    }

    /**
     * @param fragment_entry
     * @return the fragment data.
     * @throws IOException 
     */
    private byte[] read_fragment(squashfs_fragment_entry fragment_entry)
            throws IOException {
        return read_data_block(fragment_entry.start_block, fragment_entry.size);
    }

    /**
     * uncompress compressed data.
     * @param buffer the data to uncompress.
     * @param length the length to uncompress.
     * @return the uncompressed data.
     * @throws IOException 
     */
    private byte[] uncompress(byte[] buffer, int length) throws IOException {
        InflaterInputStream in = new InflaterInputStream(
                new ByteArrayInputStream(buffer, 0, length));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int r;
        while ((r = in.read()) != -1) {
            baos.write(r);
        }
        return baos.toByteArray();
    }

    /**
     * @param sourceFile the source squashfs file.
     * @param squashFile the squash file to write out.
     * @param destFile the destination file.
     * @throws IOException 
     */
    public void writeFile(SFSSquashedFile squashFile, OutputStream destFile)
            throws IOException {
        List<Long> block_list = new ArrayList<Long>();

        /*
         if(swap) {
         unsigned int sblock_list[blocks];
         memcpy(sblock_list, block_ptr, blocks * sizeof(unsigned int));
         SQUASHFS_SWAP_INTS(block_list, sblock_list, blocks);
         } else {
         */
        byte[] inode_table_bytes = this.inode_table.toByteArray();
        EasyIOInputStream in = new EasyIOInputStream(new ByteArrayInputStream(
                inode_table_bytes, (int) squashFile.getBlockPtr(), squashFile
                        .getBlocks() * 4));
        for (int i = 0; i < squashFile.getBlocks(); i++) {
            block_list.add(in.readUINT32());
        }
        /* } */

        long start = squashFile.getStart();
        for (int i = 0; i < squashFile.getBlocks(); i++) {
            byte[] file_data = read_data_block(start, block_list.get(i));

            destFile.write(file_data);

            start += squashfs_constants
                    .SQUASHFS_COMPRESSED_SIZE_BLOCK(block_list.get(i));
        }

        if (squashFile.getFragmentEntry() != null) {
            byte[] fragment_data = read_fragment(squashFile.getFragmentEntry());

            int offset = (int) squashFile.getOffset();
            int size = (int) squashFile.getFragmentBytes();
            destFile.write(fragment_data, offset, size);
        }
    }

    /**
     * gets the root directory.
     * @return the root directory.
     */
    public Directory getRootDirectory() {
        return this.rootDirectory;
    }
}
