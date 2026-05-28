package com.nms.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class PingService {

    private static final int PING_COUNT = 4;
    private static final int TIMEOUT_SECONDS = 5;

    public PingResult ping(String ipAddress) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            List<String> command = buildCommand(ipAddress, os);

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            process.waitFor();
            return parseOutput(output.toString(), os);

        } catch (Exception e) {
            return new PingResult("DOWN", -1, 100.0);
        }
    }

    private List<String> buildCommand(String ipAddress, String os) {
        List<String> command = new ArrayList<>();
        if (os.contains("win")) {
            command.add("ping");
            command.add("-n");
            command.add(String.valueOf(PING_COUNT));
            command.add("-w");
            command.add(String.valueOf(TIMEOUT_SECONDS * 1000));
            command.add(ipAddress);
        } else {
            // Linux / Mac
            command.add("ping");
            command.add("-c");
            command.add(String.valueOf(PING_COUNT));
            command.add("-W");
            command.add(String.valueOf(TIMEOUT_SECONDS));
            command.add(ipAddress);
        }
        return command;
    }

    private PingResult parseOutput(String output, String os) {
        try {
            double latency = extractLatency(output, os);
            double packetLoss = extractPacketLoss(output);
            String status = (packetLoss < 100.0) ? "UP" : "DOWN";
            return new PingResult(status, latency, packetLoss);
        } catch (Exception e) {
            return new PingResult("DOWN", -1, 100.0);
        }
    }

    private double extractLatency(String output, String os) {
        try {
            if (os.contains("win")) {
                // Windows: "Average = 12ms"
                for (String line : output.split("\n")) {
                    if (line.contains("Average")) {
                        String[] parts = line.split("=");
                        String avg = parts[parts.length - 1].trim().replace("ms", "").trim();
                        return Double.parseDouble(avg);
                    }
                }
            } else {
                // Linux: "rtt min/avg/max/mdev = 0.123/0.456/0.789/0.100 ms"
                for (String line : output.split("\n")) {
                    if (line.contains("rtt") || line.contains("round-trip")) {
                        String stats = line.split("=")[1].trim();
                        String avg = stats.split("/")[1].trim();
                        return Double.parseDouble(avg);
                    }
                }
            }
        } catch (Exception ignored) {}
        return -1;
    }

    private double extractPacketLoss(String output) {
        try {
            for (String line : output.split("\n")) {
                if (line.contains("packet loss") || line.contains("lost")) {
                    // Matches "25% packet loss" or "25% loss"
                    String[] tokens = line.split(" ");
                    for (String token : tokens) {
                        if (token.contains("%")) {
                            return Double.parseDouble(token.replace("%", "").trim());
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return 100.0;
    }

    // Inner result class
    public static class PingResult {
        private final String status;
        private final double latencyMs;
        private final double packetLoss;

        public PingResult(String status, double latencyMs, double packetLoss) {
            this.status = status;
            this.latencyMs = latencyMs;
            this.packetLoss = packetLoss;
        }

        public String getStatus() { return status; }
        public double getLatencyMs() { return latencyMs; }
        public double getPacketLoss() { return packetLoss; }
    }
}