package  logsearch;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.*;

public class Client {
    private static final String[] HOSTS = {
        // "127.0.0.1",  // or your local test
        "fa24-cs425-4601.cs.illinois.edu",
        "fa24-cs425-4602.cs.illinois.edu",
        "fa24-cs425-4603.cs.illinois.edu",
        "fa24-cs425-4604.cs.illinois.edu",
        "fa24-cs425-4605.cs.illinois.edu",
        "fa24-cs425-4606.cs.illinois.edu",
        "fa24-cs425-4607.cs.illinois.edu",
        "fa24-cs425-4608.cs.illinois.edu",
        "fa24-cs425-4609.cs.illinois.edu",
        "fa24-cs425-4610.cs.illinois.edu"
    };
    private static final int PORT = 7878;

    // For sending the pattern to the server
    private static class CommandRequest {
        private String pattern;
        public CommandRequest() {}
        public CommandRequest(String pattern)   { this.pattern = pattern; }
        public String gerPattern()              { return pattern; }
        public void setPattern(String pattern)  { this.pattern = pattern; }
    }
    private static class QueryResult {
        String host;
        int lineCount;
        long timeTaken;
        String error;

        public QueryResult(String host, int lineCount, long timeTaken, String error) {
            this.host = host;
            this.lineCount = lineCount;
            this.timeTaken = timeTaken;
            this.error = error;
        }
    }

    private static void rmTmpFiles() {
        try {
            Files.list(Paths.get("."))
                .filter(path -> path.toString().endsWith(".temp"))
                .forEach(path -> {
                    System.out.println("[INFO] Removing old temp file: " + path);
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Query a single host, returning the number of matched lines, plus timing info
    private static QueryResult queryHost(String pattern, String host, int port) {
        long start = System.currentTimeMillis();
        ObjectMapper mapper = new ObjectMapper();
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        List<LogEntry> logs;

        try {
            socket = new Socket(host, port);
            // send JSON request 
            CommandRequest req = new CommandRequest(pattern);
            String reqJson = mapper.writeValueAsString(req);
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println(reqJson);

            // Read the response  (JSON array of LogEntry)
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            String response = sb.toString();
            if (response.isEmpty()) {
                // No matches or server returned nothing
                logs = java.util.Collections.emptyList();
            } else {
                logs = mapper.readValue(response, new TypeReference<List<LogEntry>>() {});
            }
            // Write to a .temp file
            File tempFile =  new File(host + ".temp");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                for (LogEntry log : logs) {
                    writer.write(log.toString());
                    writer.newLine();
                }
            }
            long end = System.currentTimeMillis();
            return new QueryResult(host, logs.size(), (end - start), null);
        } catch (IOException e) {
            return new QueryResult(host, 0, 0, "[ERROR] Failed to connect to " + host + ": " + e.getMessage());
        } finally {
            if (out != null) out.close();
            try {
                if (in != null) in.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("[ERROR] No pattern provided");
            System.exit(1);
        }
        String pattern = args[0];
        // Clean up old .temp files
        rmTmpFiles();

        // We'll use a thread pool to run queries in parallel
        ExecutorService executor = Executors.newFixedThreadPool(HOSTS.length);

        // For each host, submit a task
        CompletionService<QueryResult> completionService = new ExecutorCompletionService<>(executor);
        for (String host : HOSTS) {
            completionService.submit(() -> queryHost(pattern, host, PORT));
        }

        int totalLines = 0;
        long startAll = System.currentTimeMillis();

        // collect results
        for (int i = 0; i < HOSTS.length; i++) {
            try {
                Future<QueryResult> future = completionService.take();
                QueryResult result = future.get();
                if (result.error != null) {
                    System.out.println(result.error);
                } else {
                    long durationMs = result.timeTaken;
                    double seconds = durationMs / 1000.0;
                    System.out.printf("From Machine: %s, %d lines matched, took %.4f seconds%n",
                            result.host, result.lineCount, seconds);
                    totalLines += result.lineCount;
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        long endAll = System.currentTimeMillis();
        double totalSec = (endAll - startAll) / 1000.0;

        System.out.printf("%n==========  Covfefe Software with Java  ==========%n");
        System.out.printf("Total %d lines matched across all hosts, total time: %.4f seconds%n", totalLines, totalSec);

        executor.shutdown();
    }
}
