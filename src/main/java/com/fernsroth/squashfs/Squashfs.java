/**
 * 
 */
package com.fernsroth.squashfs;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import com.fernsroth.easyio.EasyIORandomAccessFile;
import com.fernsroth.squashfs.model.Manifest;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public final class Squashfs {

    /**
     * hide constructor. 
     */
    private Squashfs() {
        // empty.
    }

    /**
     * main entry point.
     * @param args the command line arguments.
     * @throws Exception 
     */
    public static void main(String args[]) throws Exception {

        Queue<String> argQueue = new LinkedList<String>(Arrays.asList(args));

        if (argQueue.isEmpty()) {
            printUsage("source is required");
            return;
        }
        String source = argQueue.remove();
        if (argQueue.isEmpty()) {
            printUsage("destination is required");
            return;
        }
        String dest = argQueue.remove();

        while (argQueue.size() > 1) {
            {
                printUsage("invalid command line argument '" + argQueue.peek()
                        + "'");
                return;
            }
        }

        File sourceFile = new File(source);
        File sourceDir = sourceFile.getParentFile();
        Manifest manifest = SquashFSManifest.load(new FileInputStream(
                sourceFile), sourceDir);
        SquashFSWriter writer = new SquashFSWriter(manifest,
                new FileSystemDataProvider(sourceDir));
        File destFile = new File(dest);
        EasyIORandomAccessFile randDest = new EasyIORandomAccessFile(destFile,
                "rw");
        writer.write(randDest);
    }

    /**
     * prints the usage.
     * @param message the message to print in the help.
     */
    private static void printUsage(String message) {
        if (message != null) {
            System.err.println(message);
        }
        System.err.println("SYNTAX: " + Squashfs.class.getName()
                + " source destination [options]");
    }

}
