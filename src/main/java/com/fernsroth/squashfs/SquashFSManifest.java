/**
 * 
 */
package com.fernsroth.squashfs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import com.fernsroth.squashfs.exception.NestedSquashFSExcception;
import com.fernsroth.squashfs.exception.SquashFSException;
import com.fernsroth.squashfs.model.Manifest;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public class SquashFSManifest {

    /**
     * the sax parser factory.
     */
    private static SAXParserFactory factory;

    static {
        factory = SAXParserFactory.newInstance();
        factory.setValidating(true);
    }

    /**
     * loads a manifest file.
     * @param manifestInput the manifest input. 
     * @param rootDirectory the root directory.
     * @return the loaded manifest.
     * @throws SquashFSException 
     * @throws IOException 
     */
    public static Manifest load(InputStream manifestInput, File rootDirectory)
            throws SquashFSException, IOException {
        try {
            SAXParser parser = factory.newSAXParser();
            ManifestSAXHandler manifestHandler = new ManifestSAXHandler(
                    rootDirectory);
            parser.parse(manifestInput, manifestHandler);
            return manifestHandler.getManifest();
        } catch (ParserConfigurationException e) {
            throw new NestedSquashFSExcception("could not load manifest file",
                    e);
        } catch (SAXException e) {
            throw new NestedSquashFSExcception("could not load manifest file",
                    e);
        }
    }
}
