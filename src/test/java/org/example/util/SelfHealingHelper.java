package org.example.util;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SelfHealingHelper {
    private final WebDriver driver;
    private final OpenAIClient client;
  //  String pageSource;
    public SelfHealingHelper(WebDriver driver, String apiKey) {
        this.driver = driver;
        this.client = OpenAIOkHttpClient.builder().apiKey(apiKey).build();
    }

    public WebElement findElement(By by, String fieldKey) {
        try {
            // 1. Try with the original locator
            return driver.findElement(by);
        } catch (Exception e1) {
            System.out.println("❌ Original locator failed for [" + fieldKey + "] on " +driver.getCurrentUrl());
            // 2. Try with healed locator from the storage Json
            String healed = LocatorStorage.getHealedLocator(fieldKey);
            if (healed != null) {
                try {
                    System.out.println("🔁 Reusing healed locator for [" + fieldKey + "] on " +driver.getCurrentUrl());
                    HealCounter.incrementStorageReuse();   // ✅ count reuse
                    return driver.findElement(extractLocator(healed));
                } catch (Exception e2) {
                    System.out.println("❌ Reused healed locator also failed for [" + fieldKey + "] on " +driver.getCurrentUrl());
                    // Now Proceed to AI healing
                }
            }

            // 3. Call to Open AI for healing + save to storage
            System.out.println(" Calling AI for [" + fieldKey + "] on " +driver.getCurrentUrl());
            String newHealed = callAIToHealLocator(by.toString());
            LocatorStorage.saveHealedLocator(fieldKey, newHealed);
            HealCounter.incrementAIHeal();
            return driver.findElement(extractLocator(newHealed));
        }
    }


    private String callAIToHealLocator(String brokenLocator) {
        try {
            String pageSource = driver.getPageSource();
            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .model("gpt-4o-mini") // or your Azure/Gemini model
                    .addUserMessage("Given this HTML:\n" + pageSource +
                            "\nThe Selenium locator failed: " + brokenLocator +
                            "\nSuggest ONLY a valid Selenium locator in the format: id=xxx OR name=xxx OR css=xxx OR xpath=xxx. No explanation.")
                    .maxTokens(100)
                    .temperature(0.2)
                    .build();

            ChatCompletion completion = client.chat().completions().create(params);
            String rawSuggestion = completion.choices().get(0).message()._content().toString().trim();
            System.out.println("✅ AI suggested locator: " + rawSuggestion);
            return rawSuggestion;
        } catch (Exception e) {
            throw new RuntimeException("❌ AI healing failed for: " + brokenLocator, e);
        }
    }


    private By extractLocator(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Locator text cannot be null/empty");
        }

        // Clean input
        String cleaned = text.trim()
                .replaceAll("[`]", "")
                .replaceAll("\"", "'")
                .replaceAll("\\r?\\n", " ")
                .trim();

        String lower = cleaned.toLowerCase();
    // Check patterns
        if (lower.startsWith("id=")) {
            return By.id(cleaned.substring(3).trim());
        }
        if (lower.startsWith("name=")) {
            return By.name(cleaned.substring(5).trim());
        }
        if (lower.startsWith("class=")) {
            return By.className(cleaned.substring(6).trim());
        }
        if (lower.startsWith("css=")) {
            return By.cssSelector(cleaned.substring(4).trim());
        }
        if (lower.startsWith("xpath=")) {
            return By.xpath(cleaned.substring(6).trim());
        }

        if (lower.startsWith("by.id")) {
            return By.id(cleaned.replaceAll("(?i)by\\.id[: ]*\\(?['\"]?(.*?)['\"]?\\)?", "$1").trim());
        }
        if (lower.startsWith("by.name")) {
            return By.name(cleaned.replaceAll("(?i)by\\.name[: ]*\\(?['\"]?(.*?)['\"]?\\)?", "$1").trim());
        }
        if (lower.startsWith("by.cssselector")) {
            return By.cssSelector(cleaned.replaceAll("(?i)by\\.cssselector[: ]*\\(?['\"]?(.*?)['\"]?\\)?", "$1").trim());
        }
        if (lower.startsWith("by.xpath")) {
            return By.xpath(cleaned.replaceAll("(?i)by\\.xpath[: ]*\\(?['\"]?(.*?)['\"]?\\)?", "$1").trim());
        }


        if (lower.startsWith("css ")) {
            return By.cssSelector(cleaned.substring(4).trim());
        }
        if (lower.startsWith("xpath ")) {
            return By.xpath(cleaned.substring(6).trim());
        }


        if (cleaned.startsWith("/") || cleaned.startsWith("(")) {
            return By.xpath(cleaned);
        }


        if (cleaned.startsWith("#") || cleaned.startsWith(".") || cleaned.startsWith("[") || cleaned.contains("[")) {
            return By.cssSelector(cleaned);
        }


        if (cleaned.matches("^[a-zA-Z]+$")) {
            return By.tagName(cleaned);
        }


        if (cleaned.matches("^[a-zA-Z0-9_-]+$")) {
            return By.id(cleaned);
        }

        return By.xpath(cleaned);
    }

}


