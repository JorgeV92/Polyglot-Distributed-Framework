package logsearch;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Server {
    private static final int PORT = 7878;
    private static final String HOST = "0.0.0.0";

    private static final String LOG_PATH = "server.log";

    // Simple class representing the request JSON
    // { "pattern": "some-value" }
    private static class CommandRequest {
        private String pattern;
        public CommandRequest() {}
        public String getPattern()                  { return pattern; }
        public void setPattern(String pattern)      { this.pattern = pattern; }
    }
    private static class LogEntries {
        private final java.util.List<LogEntry> entries = new java.util.ArrayList<>();
        public void addEntry(LogEntry entry) {
            entries.add(entry);
        }
        public java.util.List<LogEntry> getEntries() { return entries; }
    }


    private static void handleClient(Socket clientSocket) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        // Read JSON from client 
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String line = in.readLine();
        if (line == null || line.isEmpty()) {
            System.out.println("[WARN] No input from client.");
            return;
        }
        // Parse JSON 
        CommandRequest request = mapper.readValue(line, CommandRequest.class);
        String pattern = request.getPattern();
        System.out.println("[INFO] Recieved pattern: " + pattern);

        // Actually run shell command (e.g., grep), capturing line number using -n
        // WARNING: Not recommended for production unless carefully sanitized!
        Process process = Runtime.getRuntime().exec(new String[] {
            "bash", "-c", "grep -n '" + pattern + "' " + LOG_PATH
        });

        BufferedReader processOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader processErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        // Build log entries
        // The format of `grep -n` is:  lineNumber:the actual line ...
        // Example:  45:Hello world
        // We'll parse that into lineNumber + content.
        String host = HOST;
        String port = String.valueOf(PORT);
        int exitCode = -1;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Collect mathced lines
        int lineNumber = 0;
        LogEntries result = new LogEntry();
        String grepLine;
        while ((grepLine = processOut.readLine()) != null) {
            // parse "lineNumber:actual line text"
            int idx = grepLine.indexOf(':');
            if (idx > 0) {
                String lnStr = grepLine.substring(0, idx);
                String content = grepLine.substring(idx + 1);
                try {
                    lineNumber = Integer.parseInt(lnStr);
                } catch (NumberFormatException e) {
                    lineNumber = 0;
                }
                LogEntry entry = new LogEntry(LOG_PATH, host, port, lineNumber, content);
                result.addEntry(entry);
            }
        }
        // If [ERROR] from grep 
        String errorLine;
        while ((errorLine = processErr.readLine()) != null) {
            System.out.println("[ERROR] from grep: " + errorLine);
        }
        System.out.println("[INFO] grep exit code: " + exitCode);
        // Conver resutls to JSON and send back
        String responseJson = mapper.writeValueAsString(result.getEntries());
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        out.println(responseJson);

        // cleanup
        processOut.close();
        processErr.close();
        in.close();
        out.close();
        clientSocket.close();
    }
    public static void main(String[] args) {
        try(ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[INFO] Server listening on " + HOST + ":" + PORT);
            // Continously accept connections
            while (true) { 
                Socket clientSocket = serverSocket.accept();
                System.out.println("[INFO] Accepted connection from " + clientSocket.getRemoteSocketAddress());

                // Handle each connection in a dedicated thread
                new Thread(() -> {
                    try {
                        handleClient(clientSocket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}