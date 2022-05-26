package com.viloveul.context.util.helper;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class GeneralHelper {

    private GeneralHelper() {
        // not for initialize
    }

    public static String trim(String value, char character) {
        return StringUtils.trimWhitespace(
            StringUtils.trimTrailingCharacter(
                StringUtils.trimLeadingCharacter(value, character),
                character
            )
        );
    }

    @SafeVarargs
    public static <T> Collection<T> mergeCollection(Collection<T>... xx) {
        List<T> collections = new ArrayList<>();
        for (Collection<T> x : xx) {
            if (!x.isEmpty()) {
                collections.removeAll(x);
                collections.addAll(x);
            }
        }
        return collections;
    }

    public static <T> T caseWhen(Boolean condition, T yes, T no) {
        return Boolean.TRUE.equals(condition) ? yes : no;
    }

    // https://www.techiedelight.com/partition-list-multiple-sublists-java
    public static<T> List<List<T>> chunk(List<T> lists, int n) {
        List<List<T>> res = new ArrayList<>();
        int size = lists.size();
        int m = size / n;
        if (size % n != 0) {
            m++;
        }

        for (int i = 0; i < m; i++) {
            int fromIndex = i * n;
            int toIndex = Math.min(i * n + n, size);
            res.add(lists.subList(fromIndex, toIndex));
        }
        return res;
    }

    public static<T> List<List<T>> chunk(List<T> lists) {
        return chunk(lists, 500);
    }
}
