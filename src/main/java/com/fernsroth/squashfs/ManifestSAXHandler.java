/**
 * 
 */
package com.fernsroth.squashfs;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.fernsroth.squashfs.exception.SquashFSException;
import com.fernsroth.squashfs.model.BaseFile;
import com.fernsroth.squashfs.model.Directory;
import com.fernsroth.squashfs.model.Manifest;
import com.fernsroth.squashfs.model.SFSFile;
import com.fernsroth.squashfs.model.SFSSourceFile;
import com.fernsroth.squashfs.model.SymLink;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public class ManifestSAXHandler extends DefaultHandler {

    /**
     * the manifest.
     */
    private Manifest manifest;

    /**
     * object stack to keep track of location during sax calls.
     */
    private Stack<Object> objectStack = new Stack<Object>();

    /**
     * the source directory.
     */
    private File sourceDir;

    /**
     * the system.
     */
    private static SquashFSSystem system = new DefaultSquashFSSystem();

    /**
     * constructor.
     * @param sourceDir the source directory.
     */
    public ManifestSAXHandler(File sourceDir) {
        this.sourceDir = sourceDir;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId)
            throws IOException, SAXException {
        if (publicId.equals(SquashFSGlobals.DOCTYPE_PUBLIC)) {
            return new InputSource(getClass().getResourceAsStream(
                    "/jSquashfs-1.0.dtd"));
        }
        return super.resolveEntity(publicId, systemId);
    }

    /**
     * gets the root directory.
     * @return the root directory.
     */
    public Manifest getManifest() {
        return this.manifest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        this.objectStack.pop();
        super.endElement(uri, localName, qName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        if (qName.equals("squashfs-manifest")) {
            this.manifest = new Manifest();
            this.objectStack.push(this.manifest);
        }

        else if (qName.equals("root-directory") || qName.equals("directory")
                || qName.equals("file") || qName.equals("symbolic-link")) {
            long uid = Long.parseLong(attributes.getValue("uid"));
            long guid = Long.parseLong(attributes.getValue("guid"));
            Date dateMTime;
            String strMTime = attributes.getValue("mtime");
            try {
                if (strMTime == null) {
                    dateMTime = system.getCurrentDate();
                } else {
                    dateMTime = SquashFSUtils.ISO8601_FORMAT.parse(strMTime);
                }
            } catch (ParseException e) {
                throw new SAXException("could not parse mtime '" + strMTime
                        + "'", e);
            }
            long mTime = SquashFSUtils.getMTimeFromDate(dateMTime);
            int mode = SquashFSUtils.getModeFromString(attributes
                    .getValue("mode"));
            String name = attributes.getValue("name");

            if (qName.equals("root-directory") || qName.equals("directory")) {
                Directory dir = new Directory(name, mode, mTime, guid, uid);
                try {
                    addToParent(this.objectStack.peek(), dir);
                } catch (SquashFSException e) {
                    throw new SAXException("adding item to parent", e);
                }
                this.objectStack.push(dir);
            }

            else if (qName.equals("file")) {
                String fileName = attributes.getValue("file");
                File sourceFile = null;
                if (fileName != null) {
                    sourceFile = new File(this.sourceDir, fileName);
                }
                SFSFile file = new SFSSourceFile(name, mode, mTime, guid, uid,
                        sourceFile);
                try {
                    addToParent(this.objectStack.peek(), file);
                } catch (SquashFSException e) {
                    throw new SAXException("adding item to parent", e);
                }
                this.objectStack.push(file);
            }

            else if (qName.equals("symbolic-link")) {
                String linkName = attributes.getValue("link");
                SymLink link = new SymLink(name, mode, mTime, guid, uid,
                        linkName);
                try {
                    addToParent(this.objectStack.peek(), link);
                } catch (SquashFSException e) {
                    throw new SAXException("adding item to parent", e);
                }
                this.objectStack.push(link);
            }
        }

        else {
            throw new SAXException("unknown element '" + qName + "'");
        }
    }

    /**
     * adds an object to it's parent.
     * @param parent the parent to add it to.
     * @param obj the object to add.
     * @throws SquashFSException 
     */
    private void addToParent(Object parent, Object obj)
            throws SquashFSException {
        if (parent instanceof Manifest) {
            ((Manifest) parent).setRoot((Directory) obj);
        } else if (parent instanceof Directory) {
            ((Directory) parent).addSubentry((BaseFile) obj);
        } else {
            throw new SquashFSException("invalid parent '"
                    + parent.getClass().getName() + "' for child '"
                    + obj.getClass().getName() + "'");
        }
    }
}
