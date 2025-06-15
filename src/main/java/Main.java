import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class Main {
  public static void main(String[] args) {
    if (args.length < 2) {
      System.out.println("Missing <database path> and <command>");
      return;
    }

    String databaseFilePath = args[0];
    String command = args[1];

    switch (command) {
      case ".dbinfo" -> {
        try (FileInputStream databaseFile = new FileInputStream(new File(databaseFilePath))) {
          // Read database page size
          databaseFile.skip(16); // Skip the first 16 bytes of the header
          byte[] pageSizeBytes = new byte[2]; // The following 2 bytes are the page size
          databaseFile.read(pageSizeBytes);
          short pageSizeSigned = ByteBuffer.wrap(pageSizeBytes).getShort();
          int pageSize = Short.toUnsignedInt(pageSizeSigned);

          // Reset to start of file to read page header for sqlite_schema (page 1)
          databaseFile.getChannel().position(0); // Move to start of file
          databaseFile.skip(100); // Skip the 100-byte database file header to reach page header
          byte[] cellCountBytes = new byte[2]; // The number of cells is a 2-byte big-endian value
          databaseFile.read(cellCountBytes);
          int numberOfCells = ByteBuffer.wrap(cellCountBytes).getShort() & 0xFFFF; // Convert to unsigned int

          // Output results
          System.out.println("database page size: " + pageSize);
          System.out.println("number of tables: " + numberOfCells);
        } catch (IOException e) {
          System.out.println("Error reading file: " + e.getMessage());
        }
      }
      default -> System.out.println("Missing or invalid command passed: " + command);
    }
  }
}