/**
 * 
 */
package com.fernsroth.squashfs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.fernsroth.squashfs.model.BaseFile;
import com.fernsroth.squashfs.model.Directory;
import com.fernsroth.squashfs.model.SFSSquashedFile;
import com.fernsroth.squashfs.model.SymLink;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public class OutputWalkHandler implements WalkHandler {

    /**
     * the XML document builder.
     */
    private static DocumentBuilder builder;

    /**
     * an identity transformer.
     */
    private static Transformer identityTransformer;

    static {
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory
                    .newInstance();
            builder = builderFactory.newDocumentBuilder();

            TransformerFactory transformerFactory = TransformerFactory
                    .newInstance();
            identityTransformer = transformerFactory.newTransformer();
            identityTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
            identityTransformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC,
                    SquashFSGlobals.DOCTYPE_PUBLIC);
            identityTransformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,
                    SquashFSGlobals.DOCTYPE_SYSTEM);
        } catch (ParserConfigurationException e) {
            throw new ExceptionInInitializerError(e);
        } catch (TransformerConfigurationException e) {
            throw new ExceptionInInitializerError(e);
        } catch (TransformerFactoryConfigurationError e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * the reader.
     */
    private SquashFSReader reader;

    /**
     * the destination file.
     */
    private File destFile;

    /**
     * mapping between directories and files.
     */
    private Map<BaseFile, FileData> dataMap = new HashMap<BaseFile, FileData>();

    /**
     * the manifest xml.
     */
    private Document manifest;

    /**
     * the top level manifest element.
     */
    private Element manifestElement;

    /**
     * includes the mtime in the manifest file.
     */
    private boolean includeMTimeInManfest = true;

    /**
     * @param reader the source file.
     * @param destFile the destination file.
     * @throws ParserConfigurationException 
     */
    public OutputWalkHandler(SquashFSReader reader, File destFile)
            throws ParserConfigurationException {
        this.reader = reader;
        this.destFile = destFile;
        this.manifest = builder.newDocument();
        this.manifestElement = this.manifest.createElement("squashfs-manifest");
        this.manifest.appendChild(this.manifestElement);
    }

    /**
     * {@inheritDoc}
     */
    public void visit(Directory[] path, BaseFile file) throws IOException {
        Node parent;
        if (path.length == 0) {
            parent = this.manifestElement;
        } else {
            parent = this.dataMap.get(path[path.length - 1])
                    .getManifestElement();
        }
        Element element = createManifestElement(parent, file);

        // root
        if (path.length == 0) {
            this.dataMap.put(file, new FileData(this.destFile, element));
        }

        // non-root
        else {
            File parentFile = this.dataMap.get(path[path.length - 1]).getFile();
            File f = new File(parentFile, fixName(file.getName()));
            if (file instanceof Directory) {
                f.mkdirs();
            } else if (file instanceof SFSSquashedFile) {
                /* enable this to output explicit file attribute to xml manifest file.
                 element.setAttribute("file", SquashFSUtils.getRelativePath(
                 this.destFile, f));
                 */
                FileOutputStream outFile = new FileOutputStream(f);
                this.reader.writeFile((SFSSquashedFile) file, outFile);
                outFile.close();
            }
            this.dataMap.put(file, new FileData(f, element));
        }
    }

    /**
     * fix a name for the platform.
     * @param name the name to fix.
     * @return the normalized name.
     */
    private String fixName(String name) {
        return name;
    }

    /**
     * creates a manifest element.
     * @param parent parent node.
     * @param file the file to create it from.
     * @return the manifest element.
     */
    private Element createManifestElement(Node parent, BaseFile file) {
        Element elem;
        if (file instanceof Directory && parent == this.manifestElement) {
            elem = this.manifest.createElement("root-directory");
        } else if (file instanceof Directory) {
            elem = this.manifest.createElement("directory");
        } else if (file instanceof SFSSquashedFile) {
            elem = this.manifest.createElement("file");
        } else if (file instanceof SymLink) {
            elem = this.manifest.createElement("symbolic-link");
        } else {
            throw new RuntimeException("unknown file type '"
                    + file.getClass().getName() + "'");
        }

        if (file.getName() != null) {
            elem.setAttribute("name", file.getName());
        }
        if (file instanceof SymLink) {
            elem.setAttribute("link", ((SymLink) file).getLinkName());
        }

        if (file.getUid() != 0) {
            elem.setAttribute("uid", Long.toString(file.getUid()));
        }
        if (file.getGuid() != 0) {
            elem.setAttribute("guid", Long.toString(file.getGuid()));
        }
        elem.setAttribute("mode", SquashFSUtils.getModeString(file));
        if (this.includeMTimeInManfest) {
            elem.setAttribute("mtime", SquashFSUtils.ISO8601_FORMAT
                    .format(SquashFSUtils.getDateFromMTime(file.getMTime())));
        }

        parent.appendChild(elem);
        return elem;
    }

    /**
     * writes the manifest file.
     * @param manifestFile the manifest file to write.
     * @throws IOException 
     * @throws TransformerException 
     */
    public void writeManifest(File manifestFile) throws IOException,
            TransformerException {
        manifestFile.createNewFile();

        identityTransformer.transform(new DOMSource(this.manifest),
                new StreamResult(manifestFile));
    }

    /**
     * contains data about a path.
     */
    private class FileData {

        /**
         * the file.
         */
        private File file;

        /**
         * the manifest element. 
         */
        private Element element;

        /**
         * @param file the file.
         * @param manifestElement the manifest element.
         */
        public FileData(File file, Element manifestElement) {
            this.file = file;
            this.element = manifestElement;
        }

        /**
         * @return the file
         */
        public File getFile() {
            return this.file;
        }

        /**
         * @return the manifestElement
         */
        public Element getManifestElement() {
            return this.element;
        }
    }

    /**
     * @param includeMTimeInManfest
     */
    public void setIncludeMTimeInManfest(boolean includeMTimeInManfest) {
        this.includeMTimeInManfest = includeMTimeInManfest;
    }
}
