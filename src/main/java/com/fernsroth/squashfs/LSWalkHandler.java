/**
 * 
 */
package com.fernsroth.squashfs;

import com.fernsroth.squashfs.model.BaseFile;
import com.fernsroth.squashfs.model.Directory;
import com.fernsroth.squashfs.model.SymLink;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public class LSWalkHandler implements WalkHandler {

    /**
     * {@inheritDoc}
     */
    public void visit(Directory[] path, BaseFile bf) {
        String line = SquashFSUtils.getModeString(bf);

        line += String.format(" %5d %5d ", new Object[] { bf.getUid(),
                bf.getGuid() });

        line += SquashFSUtils.LINUX_DATE_FORMAT.format(SquashFSUtils
                .getDateFromMTime(bf.getMTime()));

        line += " ";
        line += printPath(path);
        line += bf.getName() == null ? "" : bf.getName();
        line += bf instanceof Directory ? "/" : "";
        if (bf instanceof SymLink) {
            line += " -> " + ((SymLink) bf).getLinkName();
        }
        System.out.println(line);
    }

    /**
     * prints the path to the string.
     * @param path the path to print.
     * @return the string.
     */
    private String printPath(Directory[] path) {
        StringBuffer result = new StringBuffer();
        for (Directory dir : path) {
            if (dir.getName() != null) {
                result.append(dir.getName());
                result.append('/');
            }
        }
        return result.toString();
    }

}
