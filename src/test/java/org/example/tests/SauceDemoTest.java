package org.example.tests;
import io.github.cdimascio.dotenv.Dotenv;
//import org.example.util.HealCounter;
import org.example.util.HealCounter;
import org.example.util.SelfHealingHelper;
import org.openqa.selenium.By;
import org.testng.annotations.*;

public class SauceDemoTest extends BaseTest {
    private SelfHealingHelper healer;

    @BeforeMethod
    public void browserLaunch() {
        initDriver();
        Dotenv dotenv = Dotenv.load();
        String apiKey = dotenv.get("MY_KEY");
        healer = new SelfHealingHelper(driver, apiKey);
    }

    @Test
    public void testHealingAndReuse() {
        driver.get("https://www.saucedemo.com/");
        healer.findElement(By.id("user----nam"), "login.username").sendKeys("standard_user"); // ❌ locator broken to trigger healing
        healer.findElement(By.id("password"), "login.password").sendKeys("secret_sauce");
        healer.findElement(By.id("login-----buttn"), "login.button").click(); // ❌ locator broken to trigger healing
        healer.findElement(By.xpath("//div[text()='Sauce Labs Bike Light']/ancestor::div[@class='inventory_item']//button"),
                "product.bikelight.addtocart").click();
        healer.findElement(By.className("shopping_cart_link"), "cart.icon").click();
        healer.findElement(By.id("checkout"), "checkout.button").click();
        healer.findElement(By.id("first-----nam"), "checkout.firstname").sendKeys("John"); // ❌ locator broken to trigger healing
        healer.findElement(By.name("lastN"), "checkout.lastname").sendKeys("Doe");        // ❌ locator broken to trigger healing
        healer.findElement(By.id("postal-code123"), "checkout.postalcode").sendKeys("12345"); // ❌ locator broken to trigger healing
        healer.findElement(By.id("continue"), "checkout.continue").click();
        healer.findElement(By.id("finish"), "checkout.finish").click();
        healer.findElement(By.id("back-to-products"), "checkout.backtoproducts").click();


        // ---------------- Reuses healed locators ----------------
        healer.findElement(By.xpath("//div[text()='Sauce Labs Bike Light']/ancestor::div[@class='inventory_item']//button"),
                "product.bikelight.addtocart").click();
        healer.findElement(By.className("shopping_cart_link"), "cart.icon").click();
        healer.findElement(By.id("checkout"), "checkout.button").click();
        healer.findElement(By.id("first-----nam"), "checkout.firstname").sendKeys("John");  // reused healed locator
        healer.findElement(By.name("lastN"), "checkout.lastname").sendKeys("Doe");        // reused healed locator
        healer.findElement(By.id("postal-code123"), "checkout.postalcode").sendKeys("12345");  // reused healed locator

        System.out.println("✅ Test Completed Successfully!");
        System.out.println(" ===== Healing Statistics =====");
        HealCounter.printStats();
        System.out.println("========================");
    }

    @AfterTest
    public void tearDown() {
        quitDriver();
    }
}
