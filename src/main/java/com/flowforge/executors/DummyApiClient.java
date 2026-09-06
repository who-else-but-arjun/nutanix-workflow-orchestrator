package com.flowforge.executors;

import java.util.Map;

final class DummyApiClient {
    private DummyApiClient() {
    }

    static Map<String, Object> call(String method, String url) {
        return Map.of(
                "status", 200,
                "body", "dummy API response accepted",
                "method", method,
                "url", url
        );
    }
}
