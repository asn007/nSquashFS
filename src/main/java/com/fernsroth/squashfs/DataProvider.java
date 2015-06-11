/**
 * 
 */
package com.fernsroth.squashfs;

import java.io.IOException;

import com.fernsroth.easyio.IRandomAccessSource;
import com.fernsroth.squashfs.model.BaseFile;
import com.fernsroth.squashfs.model.Manifest;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public interface DataProvider {

    /**
     * @param source source manifest.
     * @param bf the file to get the length from.
     * @return the length of the file.
     * @throws IOException 
     */
    long getLength(Manifest source, BaseFile bf) throws IOException;

    /**
     * @param source source manifest.
     * @param bf the file to get the ino from.
     * @return the ino.
     */
    int getIno(Manifest source, BaseFile bf);

    /**
     * @param source source manifest.
     * @param bf the file name.
     * @return the random access source.
     * @throws IOException 
     */
    IRandomAccessSource getData(Manifest source, BaseFile bf)
            throws IOException;

}
