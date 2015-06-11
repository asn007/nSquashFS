/**
 * 
 */
package com.fernsroth.squashfs;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import com.fernsroth.easyio.EasyIORandomAccessFile;
import com.fernsroth.easyio.exception.EasyIOException;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public final class Unsquashfs {

    /**
     * default destination.
     */
    private static final String DEFAULT_DESTINATION = "squashfs-root";

    /**
     * hide constructor. 
     */
    private Unsquashfs() {
        // empty.
    }

    /**
     * main entry point.
     * @param args the command line arguments.
     * @throws Exception 
     */
    public static void main(String args[]) throws Exception {
        String dest = null;
        boolean lsonly = false;

        Queue<String> argQueue = new LinkedList<String>(Arrays.asList(args));
        while (argQueue.size() > 1) {
            if (argQueue.peek().equalsIgnoreCase("-dest")) {
                argQueue.remove();
                if (argQueue.isEmpty()) {
                    printUsage("'-dest' requires an argument");
                    return;
                }
                dest = argQueue.remove();
            } else if (argQueue.peek().equalsIgnoreCase("-ls")) {
                argQueue.remove();
                lsonly = true;
            } else {
                printUsage("invalid command line argument '" + argQueue.peek()
                        + "'");
                return;
            }
        }
        if (argQueue.isEmpty()) {
            printUsage("no source file specified");
            return;
        }
        if (lsonly && dest != null) {
            printUsage("cannot specify '-ls' and '-dest' at the same time.");
            return;
        }

        String source = argQueue.remove();
        File sourceFile = new File(source);
        if (!sourceFile.canRead()) {
            System.err.println("cannot read source file '"
                    + sourceFile.toString() + "'");
            return;
        }

        File destFile = null;
        if (!lsonly) {
            if (dest == null) {
                dest = DEFAULT_DESTINATION;
            }
            if (dest != null) {
                destFile = new File(dest);
                destFile.mkdirs();
            }
        }

        SquashFSReader reader;
        try {
            reader = new SquashFSReader(new EasyIORandomAccessFile(sourceFile,
                    "r"));
        } catch (EasyIOException e) {
            throw new Exception("could not read source '" + sourceFile + "'", e);
        } catch (IOException e) {
            throw new Exception("could not read source '" + sourceFile + "'", e);
        }

        if (lsonly) {
            SquashFSUtils.walk(reader.getRootDirectory(), new LSWalkHandler());
        } else {
            OutputWalkHandler out = new OutputWalkHandler(reader, destFile);
            out.setIncludeMTimeInManfest(false);
            SquashFSUtils.walk(reader.getRootDirectory(), out);
            out.writeManifest(new File(destFile, "manifest.xml"));
        }
    }

    /**
     * prints the usage.
     * @param message the message to print in the help.
     */
    private static void printUsage(String message) {
        if (message != null) {
            System.err.println(message);
        }
        System.err.println("SYNTAX: " + Unsquashfs.class.getName()
                + " [-ls | -dest] filesystem");
        System.err.println("\t-ls              list filesystem only");
        System.err
                .println("\t-dest <pathname> unsquash to <pathname>, default \""
                        + DEFAULT_DESTINATION + "\"");
    }

}
