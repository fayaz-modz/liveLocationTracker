import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;

public class Watch {
    private static final String JSON_FILE_PATH = "./locations.json"; // Path to your JSON file

    public static void main(String[] args) {
        renderJson(); // Render the JSON content at the beginning
        watchJsonFile();
    }

    private static void watchJsonFile() {
        try {
            Path path = Paths.get(JSON_FILE_PATH).getParent();
            WatchService watchService = FileSystems.getDefault().newWatchService();
            path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            while (true) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }
                    Path changed = (Path) event.context();
                    if (changed.endsWith(new File(JSON_FILE_PATH).getName())) {
                        renderJson();
                    }
                }
                key.reset();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void renderJson() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(JSON_FILE_PATH));
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }
            reader.close();

            // Check if JSON is valid and not empty
            String jsonString = jsonContent.toString().trim();
            if (jsonString.isEmpty() || !jsonString.startsWith("{")) {
                System.out.println("Invalid JSON format.");
                return;
            }

            jsonString = jsonString.substring(1, jsonString.length() - 1); // Remove curly braces

            // Clear console
            System.out.print("\033[H\033[2J");
            System.out.flush();

            // Split the JSON string into key-value pairs
            String[] pairs = jsonString.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

            // Render key-value pairs
            for (String pair : pairs) {
                String[] keyValue = pair.split(":", 2); // Limit to 2 parts
                if (keyValue.length < 2) {
                    System.out.println("Invalid key-value pair: " + pair);
                    continue;
                }
                String key = keyValue[0].trim().replaceAll("\"", ""); // Remove quotes
                String value = keyValue[1].trim().replaceAll("\"", ""); // Remove quotes
                System.out.println(key + " : " + value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

