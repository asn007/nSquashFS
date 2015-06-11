/**
 * 
 */
package com.fernsroth.squashfs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.fernsroth.easyio.EasyIORandomAccessFile;
import com.fernsroth.easyio.IRandomAccessSource;
import com.fernsroth.squashfs.model.BaseFile;
import com.fernsroth.squashfs.model.Manifest;
import com.fernsroth.squashfs.model.SFSSourceFile;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public class FileSystemDataProvider implements DataProvider {

    /**
     * the source directory.
     */
    private File sourceDir;

    /**
     * ino counter.
     */
    private static int ino = 1;

    /**
     * @param sourceDir
     */
    public FileSystemDataProvider(File sourceDir) {
        this.sourceDir = sourceDir;
    }

    /**
     * {@inheritDoc}
     */
    public IRandomAccessSource getData(Manifest source, BaseFile bf)
            throws IOException {
        if (bf instanceof SFSSourceFile
                && ((SFSSourceFile) bf).getSourceFile() != null) {
            return new EasyIORandomAccessFile(((SFSSourceFile) bf)
                    .getSourceFile(), "r");
        } else {
            String path = source.getPath(bf);
            File f = new File(this.sourceDir, path);
            return new EasyIORandomAccessFile(f, "r");
        }
    }

    /**
     * {@inheritDoc}
     */
    public int getIno(Manifest source, BaseFile bf) {
        return ino++;
    }

    /**
     * {@inheritDoc}
     */
    public long getLength(Manifest source, BaseFile bf) throws IOException {
        if (bf == null || bf.getName() == null) {
            return 0;
        }
        String path = source.getPath(bf);
        if (path == null) {
            throw new FileNotFoundException("could not file '" + bf.getName()
                    + "'");
        }
        File f = new File(this.sourceDir, path);
        return f.length();
    }
}
