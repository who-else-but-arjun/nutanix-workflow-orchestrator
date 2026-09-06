package com.flowforge.engine.scheduler;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ConditionEvaluator {
    private static final Pattern SIMPLE_CONDITION = Pattern.compile("^(input|outputs)\\.([A-Za-z0-9_.-]+)\\s*(==|!=)\\s*(.+)$");

    private ConditionEvaluator() {
    }

    static boolean matches(String expression, Map<String, Object> workflowInput, Map<String, Object> outputs) {
        if (expression == null || expression.isBlank()) {
            return true;
        }
        Matcher matcher = SIMPLE_CONDITION.matcher(expression.trim());
        if (!matcher.matches()) {
            return false;
        }

        Object actual = resolve(matcher.group(1), matcher.group(2), workflowInput, outputs);
        Object expected = parseValue(matcher.group(4));
        boolean equal = String.valueOf(actual).equals(String.valueOf(expected));
        return "==".equals(matcher.group(3)) ? equal : !equal;
    }

    @SuppressWarnings("unchecked")
    private static Object resolve(String root, String path, Map<String, Object> workflowInput, Map<String, Object> outputs) {
        Object current = "input".equals(root) ? workflowInput : outputs;
        for (String segment : path.split("\\.")) {
            if (!(current instanceof Map<?, ?> map)) {
                return null;
            }
            current = ((Map<String, Object>) map).get(segment);
        }
        return current;
    }

    private static Object parseValue(String raw) {
        String value = raw.trim();
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }
        return value;
    }
}
