Selenium Self-Healing Framework - SauceDemo Test
**Overview**
This project demonstrates a self-healing Selenium automation framework using Java. It integrates AI-assisted locator healing to automatically recover from broken or changed locators
on the web page.
The SauceDemoTest class performs an end-to-end test of the SauceDemo application and showcases:

**Features**
Self-Healing Locators
Detects when a Selenium locator fails.
Calls AI to suggest a new valid locator.
Stores the healed locator for future reuse.
Locator Storage by maintaining a persistent JSON storage of healed locators.
Reduces AI calls on subsequent test runs.
Healing Metrics- Tracks number of AI healing calls and self storage calls
Helps in debugging locator failures.
