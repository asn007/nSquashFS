/**
 * 
 */
package com.fernsroth.squashfs;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import com.fernsroth.squashfs.model.BaseFile;
import com.fernsroth.squashfs.model.Directory;
import com.fernsroth.squashfs.model.SFSFile;
import com.fernsroth.squashfs.model.SymLink;
import com.fernsroth.squashfs.model.squashfs.stat;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public final class SquashFSUtils {

    /**
     * linux date format.
     */
    public static final SimpleDateFormat LINUX_DATE_FORMAT = new SimpleDateFormat(
            "MMM dd HH:mm");

    /**
     * iso 8601 date formatter.
     */
    public static SimpleDateFormat ISO8601_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss");

    /**
     * hide constructor.
     */
    private SquashFSUtils() {
        // empty.
    }

    /**
     * gets the mode string.
     * @param baseFile the file to get the string from.
     * @return the mode string.
     */
    public static String getModeString(BaseFile baseFile) {
        String line = "";
        if (baseFile instanceof Directory) {
            line += "d";
        } else if (baseFile instanceof SymLink) {
            line += "l";
        } else if (baseFile instanceof SFSFile) {
            line += "-";
        }
        line += getPermissionsString((baseFile.getMode() >> 6) & 0x07);
        line += getPermissionsString((baseFile.getMode() >> 3) & 0x07);
        line += getPermissionsString((baseFile.getMode() >> 0) & 0x07);
        return line;
    }

    /**
     * gets the permissions.
     * @param permissions
     * @return the permissions string.
     */
    public static String getPermissionsString(long permissions) {
        String line = "";
        line += (permissions & 0x04) == 0x04 ? "r" : "-";
        line += (permissions & 0x02) == 0x02 ? "w" : "-";
        line += (permissions & 0x01) == 0x01 ? "x" : "-";
        return line;
    }

    /**
     * gets the combination mode.
     * @param owner the owner mode.
     * @param group the group mode.
     * @param other the other mode.
     * @return get the combination mode.
     */
    public static int getMode(int owner, int group, int other) {
        int mode = 0;
        mode |= (owner & 0x7) << 6;
        mode |= (group & 0x7) << 3;
        mode |= (other & 0x7) << 0;
        return mode;
    }

    /**
     * gets the mode from a string.
     * @param value the value to convert.
     * @return the mode.
     */
    public static int getModeFromString(String value) {
        String type = value.substring(0, 1);
        String ownerStr = value.substring(1, 4);
        String groupStr = value.substring(4, 7);
        String allStr = value.substring(7, 10);
        return getTypeFromChar(type.charAt(0))
                | (getPermissionsFromString(ownerStr) << 6)
                | (getPermissionsFromString(groupStr) << 3)
                | (getPermissionsFromString(allStr) << 0);
    }

    /**
     * @param c the type char.
     * @return the type.
     */
    private static int getTypeFromChar(char c) {
        switch (c) {
        case 'd':
            return stat.S_IFDIR;
        case 'l':
            return stat.S_IFLNK;
        case '-':
            return stat.S_IFREG;
        default:
            throw new RuntimeException("unknwon type character '" + c + "'");
        }
    }

    /**
     * gets the permissions bit mask from a string.
     * @param str the string to get it from.
     * @return the permissions bitmask.
     */
    public static int getPermissionsFromString(String str) {
        int result = 0;
        if (str.charAt(0) == 'r') {
            result |= 0x04;
        }
        if (str.charAt(1) == 'w') {
            result |= 0x02;
        }
        if (str.charAt(2) == 'x') {
            result |= 0x01;
        }
        return result;
    }

    /**
     * gets a date from a mtime.
     * @param mtime the mtime to convert.
     * @return the date.
     */
    public static Date getDateFromMTime(long mtime) {
        return new Date(((long) mtime) * 1000);
    }

    /**
     * gets the mtime from the date.
     * @param dateMTime the date to convert.
     * @return the mtime.
     */
    public static long getMTimeFromDate(Date dateMTime) {
        return (dateMTime.getTime() / 1000);
    }

    /**
     * walks a squashfs directory.
     * @param dir the directory to walk.
     * @param handler the walk handler to get called for each entry.
     * @throws Exception 
     */
    public static void walk(Directory dir, WalkHandler handler)
            throws Exception {
        walk(new Directory[] {}, dir, handler);
    }

    /**
     * walks a squashfs directory.
     * @param path path to the file.
     * @param file the file.
     * @param handler the handler.
     * @throws Exception 
     */
    private static void walk(Directory[] path, BaseFile file,
            WalkHandler handler) throws Exception {
        handler.visit(path, file);

        if (file instanceof Directory) {
            ArrayList<Directory> pathList = new ArrayList<Directory>(Arrays
                    .asList(path));
            pathList.add((Directory) file);
            for (BaseFile f : ((Directory) file).getSubentries()) {
                walk(pathList.toArray(new Directory[pathList.size()]), f,
                        handler);
            }
        }
    }

    /**
     * gets the relative path of a file.
     * @param root the root path to get relative from.
     * @param file the file to get the relative path of.
     * @return the relative path of the file.
     */
    public static String getRelativePath(File root, File file) {
        String rel = file.getAbsolutePath().substring(
                root.getAbsolutePath().length() + 1);
        rel = rel.replaceAll("\\\\", "/");
        return rel;
    }

    /**
     * gets the owner mode from the mode.
     * @param mode the mode to get permisions from.
     * @return the owner mode.
     */
    public static int getOwnerMode(int mode) {
        return (mode >> 6) & 0x07;
    }

    /**
     * gets the group mode from the mode.
     * @param mode the mode to get permisions from.
     * @return the group mode.
     */
    public static int getGroupMode(int mode) {
        return (mode >> 3) & 0x07;
    }

    /**
     * gets the other mode from the mode.
     * @param mode the mode to get permisions from.
     * @return the other mode.
     */
    public static int getOtherMode(int mode) {
        return (mode >> 0) & 0x07;
    }
}