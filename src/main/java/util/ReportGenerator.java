package util;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;
public final class ReportGenerator {
    private ReportGenerator() {}
    public static String generate(Path logsFolder, Path reportFile) throws IOException {
        long deposits=0, withdrawals=0; StringBuilder details=new StringBuilder("MINI BANK END-OF-DAY REPORT\n");
        if(Files.isDirectory(logsFolder)) try(DirectoryStream<Path> stream=Files.newDirectoryStream(logsFolder,"*.log")) {
            for(Path file:stream) {
                try(BufferedReader reader=Files.newBufferedReader(file)) { String line; while((line=reader.readLine())!=null) {
                    String[] p=line.split("\\|"); if(p.length>=3) try { long amount=Long.parseLong(p[2]); if(p[1].equals("DEPOSIT")) deposits+=amount; else if(p[1].equals("WITHDRAW")) withdrawals+=amount; } catch(NumberFormatException ignored) { }
                }}
                BasicFileAttributes a=Files.readAttributes(file,BasicFileAttributes.class);
                details.append(file.getFileName()).append(" | size=").append(a.size()).append(" bytes | modified=").append(a.lastModifiedTime()).append('\n');
            }
        }
        details.append("Total deposits: ₹").append(deposits).append('\n').append("Total withdrawals: ₹").append(withdrawals).append('\n').append("Net movement: ₹").append(deposits-withdrawals).append('\n');
        Path parent=reportFile.toAbsolutePath().getParent(); if(parent!=null) Files.createDirectories(parent); Files.writeString(reportFile,details.toString()); return details.toString();
    }
}
