package org.example.util;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LocatorStorage {
    private static final String FILE_PATH = "healed_locators.json";
    private static Map<String, String> healedLocators = new HashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    // Load existing locators on class load
    static {
        load();
    }

    public static void saveHealedLocator(String fieldKey, String healedLocator) {
        healedLocators.put(fieldKey, healedLocator);
        save();
    }

    public static String getHealedLocator(String fieldKey) {
        return healedLocators.get(fieldKey);
    }

    private static void save() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE_PATH), healedLocators);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void load() {
        try {
            File file = new File(FILE_PATH);
            if (file.exists()) {
                healedLocators = mapper.readValue(file, Map.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
