package logsearch;

public class LogEntry {
    private String logPath;
    private String host;
    private String port;
    private int lineNumber;
    private String content;

    public LogEntry() {}
    public LogEntry(String logPath, String host, String port, int lineNumber, String content) {
        this.logPath = logPath;
        this.host = host;
        this.port = port;
        this.lineNumber = lineNumber;
        this.content = content;
    }
    public String getLogPath()                  { return logPath; }
    public void setLogPath(String logPath)      { this.logPath = logPath; }
    public String getHost()                     { return host; }
    public void setHost(String host)            { this.host = host; }
    public String getPort()                     { return port; }
    public void setPort(String port)            { this.port = port; }
    public int getLineNumber()                  { return lineNumber; }
    public void setLineNumber(int lineNumber)   { this.lineNumber = lineNumber; }
    public String getContent()                  { return content; }
    public void setContent(String content)      { this.content = content; }

    @Override
    public String toString() {
        return String.format("%s %s %s %d %s", host, port, logPath, lineNumber, content);
    }
 }