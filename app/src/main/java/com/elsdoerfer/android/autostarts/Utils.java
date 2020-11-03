package com.elsdoerfer.android.autostarts;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import com.topjohnwu.superuser.Shell;

import androidx.annotation.NonNull;

public class Utils {
    static final String TAG = "Autostarts";

	/**
     * Stupid Java's LinkedHashMap has no indexOf() method.
     */
    static int getHashMapIndex(@NonNull LinkedHashMap<?, ?> map, Object search) {
        Set<?> keys = map.keySet();
        Iterator<?> i = keys.iterator();
        Object curr;
        int count = -1;
        do {
            curr = i.next();
            count++;
            if (curr.equals(search))
                return count;
        }
        while (i.hasNext());
        return -1;
    }

    static boolean runRootCommand(final String command) {
		Shell.Result result = Shell.su(command).exec();
		return result.isSuccess();
    }

    /**
     * http://stackoverflow.com/a/25379180/15677
     */
    public static boolean containsIgnoreCase(String src, String what) {
        if (src == null) return false;

        final int length = what.length();
        if (length == 0) return true; // Empty string is contained

        final char firstLo = Character.toLowerCase(what.charAt(0));
        final char firstUp = Character.toUpperCase(what.charAt(0));

        for (int i = src.length() - length; i >= 0; i--) {
            // Quick check before calling the more expensive regionMatches() method:
            final char ch = src.charAt(i);
            if (ch != firstLo && ch != firstUp) continue;

            if (src.regionMatches(true, i, what, 0, length)) {
				return true;
			}
        }
        return false;
    }

}
