package org.example.listeners;

import com.aventstack.extentreports.*;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
//import org.example.util.HealCounter;
import org.testng.*;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.example.tests.SauceDemoTest.*;

public class ExtentReportListener implements ITestListener {
    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    @Override
    public void onStart(ITestContext context) {
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        ExtentSparkReporter spark = new ExtentSparkReporter("reports/extent-report-" + timestamp + ".html");
        spark.config().setDocumentTitle("Automation Test Report");
        spark.config().setReportName("Selenium TestNG Results");

        extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("Tester", "Automation QA");
        extent.setSystemInfo("Environment", "QA");
    }

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest test = extent.createTest(result.getMethod().getMethodName());
        extentTest.set(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        extentTest.get().pass("Test Passed ✅");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        extentTest.get().fail("Test Failed ❌: " + result.getThrowable());

        Object testClass = result.getInstance();
        if (testClass instanceof WebDriverProvider) {
            WebDriver driver = ((WebDriverProvider) testClass).getDriver();
            if (driver != null) {
                try {
                    String screenshotPath = takeScreenshot(driver, result.getMethod().getMethodName());
                    extentTest.get().addScreenCaptureFromPath(screenshotPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        extentTest.get().skip("Test Skipped ⚠️: " + result.getThrowable());
    }

    @Override
    public void onFinish(ITestContext context) {
        int passed = context.getPassedTests().size();
        int failed = context.getFailedTests().size();
        int skipped = context.getSkippedTests().size();
    //    int healed = HealCounter.Healcount();

        ExtentTest summary = extent.createTest("📊 Execution Summary");
        summary.info("✅ Passed Tests: " + passed);
        summary.info("❌ Failed Tests: " + failed);
        summary.info("⚠️ Skipped Tests: " + skipped);
     //   summary.info("🛠 Healed Locators: " + );
        summary.info("--------------------------------------------------");

//        int minSize = Math.min(Math.min(failedLocators.size(), healedLocators.size()), PageSource.size());
//        for (int i = 0; i < minSize; i++) {
//            summary.info("Failed Locator: " + failedLocators.get(i));
//            summary.info("AI Healed Locator: " + healedLocators.get(i));
//            summary.info("Page URL: " + PageSource.get(i));
//            summary.info("--------------------------------------------------");
//        }

        extent.flush(); // 🔑 Flush only once here
    }

    private String takeScreenshot(WebDriver driver, String testName) throws IOException {
        File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        String path = "reports/screenshots/" + testName + ".png";
        Files.createDirectories(Paths.get("reports/screenshots/"));
        Files.copy(src.toPath(), Paths.get(path));
        return path;
    }
}
