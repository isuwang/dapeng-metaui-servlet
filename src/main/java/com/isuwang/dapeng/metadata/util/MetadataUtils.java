package com.isuwang.dapeng.metadata.util;

import com.google.gson.Gson;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;

public class MetadataUtils {

    public static <T> T parseBoby(HttpServletRequest request, Class<T> target) throws IOException {
        String body = request.getReader().lines().reduce("", (a, b) -> a + b);
        return new Gson().fromJson(body, target);
    }

    public static String parseFieldName(String fieldNameInSql) {
        String[] columnParts = fieldNameInSql.split("_");
        String fieldName = columnParts[0];
        if (columnParts.length > 1) {
            fieldName += Arrays.asList(columnParts).subList(1, columnParts.length).stream().reduce("",
                    (left, right) -> new StringBuilder(left)
                            .append(right.substring(0, 1).toUpperCase())
                            .append(right.substring(1)).toString());
        }
        return fieldName;
    }
}
