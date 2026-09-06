package com.flowforge.executors;

import java.io.IOException;
import java.util.Map;

final class DummyShellRunner {
    private DummyShellRunner() {
    }

    static Map<String, Object> run(String command) {
        ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", "echo", "dummy shell task:", command);
        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();
            String output = new String(process.getInputStream().readAllBytes()).trim();
            int exitCode = process.waitFor();
            return Map.of("exitCode", exitCode, "stdout", output);
        } catch (IOException exception) {
            return Map.of("exitCode", 1, "stdout", "", "error", exception.getMessage());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return Map.of("exitCode", 1, "stdout", "", "error", "shell task interrupted");
        }
    }
}
