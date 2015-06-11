/**
 * 
 */
package com.fernsroth.squashfs.model;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public class Manifest {

    /**
     * the root directory.
     */
    private Directory root;

    /**
     * gets the root directory.
     * @return the root directory.
     */
    public Directory getRoot() {
        return this.root;
    }

    /**
     * @param root the root to set
     */
    public void setRoot(Directory root) {
        this.root = root;
    }

    /**
     * @param filename the file name to find.
     * @return the BaseFile with that path.
     */
    public BaseFile find(String filename) {
        return find(getRoot(), filename);
    }

    /**
     * @param start the start node.
     * @param filename the filename to find.
     * @return the base file with that path.
     */
    private BaseFile find(Directory start, String filename) {
        int i = filename.indexOf('/');
        String toFind;
        String left;
        if (i == -1) {
            toFind = filename;
            left = null;
        } else {
            toFind = filename.substring(0, i);
            left = filename.substring(i + 1);
        }
        if (toFind.equals(".")) {
            return find(start, left);
        } else {
            for (BaseFile bf : start.getSubentries()) {
                if (bf.getName().equals(toFind)) {
                    if (left == null) {
                        return bf;
                    } else {
                        return find((Directory) bf, left);
                    }
                }
            }
        }
        return null;
    }

    /**
     * get the path to a file.
     * @param bf the file to find path to.
     * @return the path.
     */
    public String getPath(BaseFile bf) {
        return getPath(getRoot(), bf);
    }

    /**
     * get the path to a file.
     * @param start the start point.
     * @param bf the file to find.
     * @return the path.
     */
    private String getPath(Directory start, BaseFile bf) {
        for (BaseFile f : start.getSubentries()) {
            if (f == bf) {
                return bf.getName();
            }
            if (f instanceof Directory) {
                String path = getPath((Directory) f, bf);
                if (path != null) {
                    return f.getName() + "/" + path;
                }
            }
        }
        return null;
    }
}
