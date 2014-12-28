package ninja.leaping.configurate;

import com.google.common.base.Objects;

import java.util.Arrays;

/**
 * Utilities for working with paths
 */
public class PathUtils {
    private PathUtils() {
        // Nevar
    }

    public static boolean isDirectChild(Object[] child, Object[] parent) {
        if (child.length != parent.length + 1) {
            return false;
        }
        for (int i = 0; i < parent.length; ++i) {
            if (!Objects.equal(child[i], parent[i])) {
                return false;
            }
        }
        return true;
    }

    public static Object[] appendPath(Object[] path, Object next) {
        Object[] newArray = Arrays.copyOf(path, path.length + 1);
        newArray[path.length] = next;
        return newArray;
    }

    public static Object[] dropPathTail(Object[] path) {
        return Arrays.copyOf(path, path.length - 1);
    }
}
