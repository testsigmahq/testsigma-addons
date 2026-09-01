package com.testsigma.addons.web;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

/**
 * Helper to resolve elements inside Shadow DOM using the host element and a CSS selector.
 */
public final class ShadowDomHelper {

    private ShadowDomHelper() {
    }

    /**
     * Finds an element inside the shadow root of the given host element using the provided CSS selector.
     *
     * @param hostElement     the host element that contains the shadow root
     * @param shadowCssLocator CSS selector for the element inside the shadow DOM
     * @return the WebElement inside the shadow DOM
     */
    public static WebElement findElementInShadow(WebElement hostElement, String shadowCssLocator) {
        SearchContext shadowRoot = hostElement.getShadowRoot();
        return shadowRoot.findElement(By.cssSelector(shadowCssLocator));
    }
}
