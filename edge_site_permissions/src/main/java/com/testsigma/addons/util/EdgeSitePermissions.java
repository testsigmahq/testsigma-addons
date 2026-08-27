package com.testsigma.addons.util;

import com.testsigma.sdk.Logger;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Drives the per-site permission rows on Edge's built-in settings page.
 *
 * <p>edge://settings is a web-component app: every permission row sits several shadow roots
 * deep, and XPath cannot cross a shadow boundary, so the locators below are JavaScript that
 * walks into each {@code shadowRoot}. Elements are located in JS and clicked through
 * Selenium, so real input events still fire.
 *
 * <p>Two rules carry the whole implementation:
 * <ul>
 *   <li>a row is found by its <em>visible title</em>, never by element name or aria-label,
 *       because both change between Edge builds (139 sets {@code aria-label="true"});</li>
 *   <li>the trigger, the options and the read-back are all resolved <em>relative to that one
 *       row</em>, because every row offers the same "Allow"/"Block" labels - a page-wide
 *       lookup can silently change a different permission.</li>
 * </ul>
 *
 * <p>Verified against Edge 130-153 (stable, beta and dev channels).
 */
public final class EdgeSitePermissions {

	  private EdgeSitePermissions() {
	  }

	  /** Time allowed for the dropdown to open. */
	  private static final long MENU_MS = 600L;

	  /** Time allowed for the row to report the new value. */
	  private static final long VERIFY_MS = 5000L;

	  private static final String JS =
	      "function all(root,out,d){"
	    + "  if(d>16) return out;"
	    + "  if(root.shadowRoot) all(root.shadowRoot,out,d+1);"
	    + "  var k; try{ k=root.querySelectorAll('*'); }catch(e){ return out; }"
	    + "  for(var i=0;i<k.length;i++){ out.push(k[i]); if(k[i].shadowRoot) all(k[i].shadowRoot,out,d+1); }"
	    + "  return out;"
	    + "}"
	    + "function attr(e,n){ return (e.getAttribute && e.getAttribute(n)) || ''; }"
	      // 'Allow' and 'Allow (default)' must compare equal; case and spacing are irrelevant
	    + "function norm(s){ return (s||'').replace(/\\(default\\)/gi,'')"
	    + "  .replace(/\\s+/g,' ').trim().toLowerCase(); }"
	    + "function host(e){ return (typeof ShadowRoot!=='undefined' && e.parentNode instanceof ShadowRoot)"
	    + "  ? e.parentNode.host : e.parentElement; }"
	    + "function text(e){"
	    + "  var t='';"
	    + "  for(var i=0;i<e.childNodes.length;i++) if(e.childNodes[i].nodeType===3) t+=e.childNodes[i].nodeValue;"
	    + "  return t.replace(/\\s+/g,' ').trim();"
	    + "}"
	    + "function shown(e){ try{ return !!(e.getClientRects && e.getClientRects().length); }catch(x){ return false; } }"
	    + "function isTrigger(e){"
	    + "  return e.tagName==='BUTTON' || e.tagName==='SELECT'"
	    + "    || /^(combobox|listbox|button)$/i.test(attr(e,'role'))"
	    + "    || !!attr(e,'aria-haspopup')"
	    + "    || /MENU-BUTTON|SELECT-BUTTON|DROPDOWN/.test(e.tagName);"
	    + "}"
	    + "function isOption(e){"
	    + "  return /^(option|menuitemradio|menuitemcheckbox|menuitem)$/i.test(attr(e,'role')); }"
	      // smallest element holding both the row's title and its control
	    + "function row(name){"
	    + "  var a=all(document,[],0), hits=[], i;"
	    + "  for(i=0;i<a.length;i++) if(norm(text(a[i]))===norm(name)) hits.push(a[i]);"
	    + "  for(i=0;i<hits.length;i++){ var n=hits[i];"
	    + "    for(var l=0;l<8;l++){"
	    + "      n=host(n); if(!n) break;"
	    + "      var inner=all(n,[],0);"
	    + "      for(var k=0;k<inner.length;k++) if(isTrigger(inner[k])) return n;"
	    + "    } }"
	    + "  return null;"
	    + "}"
	    + "function trigger(r){"
	    + "  var inner=all(r,[],0);"
	    + "  for(var i=0;i<inner.length;i++) if(isTrigger(inner[i])) return inner[i];"
	    + "  return null;"
	    + "}"
	    + "function itemText(e){"
	    + "  var inner=all(e,[],0);"
	    + "  for(var i=0;i<inner.length;i++){ var y=inner[i];"
	    + "    if(y.classList && y.classList.contains('item-title')) return y.textContent; }"
	    + "  return e.textContent;"
	    + "}"
	      // this row's options; a menu rendered outside the row is matched only while visible
	    + "function options(r){"
	    + "  var inner=all(r,[],0), out=[], i;"
	    + "  for(i=0;i<inner.length;i++) if(isOption(inner[i])) out.push(inner[i]);"
	    + "  if(out.length) return out;"
	    + "  var a=all(document,[],0);"
	    + "  for(i=0;i<a.length;i++) if(isOption(a[i]) && shown(a[i])) out.push(a[i]);"
	    + "  return out;"
	    + "}"
	    + "function optionLabels(r){"
	    + "  var o=options(r), out=[];"
	    + "  for(var i=0;i<o.length;i++) out.push((itemText(o[i])||'').replace(/\\s+/g,' ').trim());"
	    + "  return out;"
	    + "}"
	    + "function pick(r,v){"
	    + "  var o=options(r), i;"
	    + "  for(i=0;i<o.length;i++) if(norm(itemText(o[i]))===norm(v)) return o[i];"
	    + "  for(i=0;i<o.length;i++) if(norm(o[i].textContent)===norm(v)) return o[i];"
	    + "  return null;"
	    + "}"
	      // newer builds show the value in a dedicated label; older ones only on the trigger
	    + "function value(r){"
	    + "  var inner=all(r,[],0);"
	    + "  for(var i=0;i<inner.length;i++){ var y=inner[i];"
	    + "    if(y.classList && y.classList.contains('selected-label')) return y.textContent.trim(); }"
	    + "  var t=trigger(r);"
	    + "  return t ? (attr(t,'aria-label')+' '+(t.textContent||'')).replace(/\\s+/g,' ').trim() : '';"
	    + "}"
	      // row titles, so a mistyped permission name produces an actionable error
	    + "function titles(){"
	    + "  var a=all(document,[],0), out=[], seen={}, i;"
	    + "  for(i=0;i<a.length && out.length<40;i++){ var e=a[i];"
	    + "    if(isOption(e) || isTrigger(e)) continue;"
	    + "    var t=text(e);"
	    + "    if(t && t.length<40 && !seen[t] && row(t)){ seen[t]=1; out.push(t); } }"
	    + "  return out;"
	    + "}";

	  /* ------------------------------ public API ------------------------------ */

	  /**
	   * Deep link to a single site's permission list.
	   *
	   * @throws IllegalArgumentException if the URL has no scheme
	   */
	  public static String settingsUrl(String targetUrl) {
	    String[] parts = String.valueOf(targetUrl).trim().split("://");
	    if (parts.length < 2) {
	      throw new IllegalArgumentException(
	          "Target-URL must include a scheme, e.g. https://example.com (got '" + targetUrl + "')");
	    }
	    return "edge://settings/content/siteDetails?site=" + parts[0] + "%3A%2F%2F" + parts[1];
	  }

	  /**
	   * Sets one permission on the settings page that is already open.
	   *
	   * @return empty once the row itself reports the new value, otherwise a user-facing reason
	   */
	  public static Optional<String> set(WebDriver driver, Logger logger, String name, String value)
	      throws InterruptedException {
	    WebElement row = (WebElement) js(driver, "return row(arguments[0]);", name);
	    if (row == null) {
	      log(logger, "no permission named '" + name + "'; this page offers: "
	          + strings(driver, "return titles();"));
	      return Optional.of("Permission '" + name + "' was not found on the site settings page");
	    }

	    String before = valueOf(driver, row);
	    log(logger, "'" + name + "' is currently '" + before + "'; setting it to '" + value + "'");
	    if (applied(before, value)) {
	      return Optional.empty();
	    }

	    WebElement trigger = (WebElement) js(driver, "return trigger(arguments[0]);", row);
	    if (trigger == null) {
	      return Optional.of("Permission '" + name + "' has no editable control");
	    }
	    click(driver, logger, trigger, "the dropdown");
	    Thread.sleep(MENU_MS);

	    WebElement option =
	        (WebElement) js(driver, "return pick(arguments[0], arguments[1]);", row, value);
	    if (option == null) {
	      List<String> choices = strings(driver, "return optionLabels(arguments[0]);", row);
	      log(logger, "no option '" + value + "' for '" + name + "'; offered: " + choices);
	      return Optional.of("Permission value '" + value + "' is not one of " + choices);
	    }
	    click(driver, logger, option, "option '" + value + "'");

	    long deadline = System.currentTimeMillis() + VERIFY_MS;
	    String now = "";
	    while (System.currentTimeMillis() < deadline) {
	      now = valueOf(driver, row);
	      if (applied(now, value)) {
	        log(logger, "'" + name + "' is now '" + value + "'");
	        return Optional.empty();
	      }
	      Thread.sleep(200);
	    }
	    log(logger, "'" + name + "' still reads '" + now + "' after selecting '" + value + "'");
	    return Optional.of("Could not set '" + name + "' to '" + value + "'");
	  }

	  /* ------------------------------ internals ------------------------------ */

	  private static void log(Logger logger, String message) {
	    if (logger != null) {
	      logger.info(message);
	    }
	  }

	  private static Object js(WebDriver driver, String body, Object... args) {
	    return ((JavascriptExecutor) driver)
	        .executeScript("return (function(){" + JS + body + "}).apply(null, arguments);", args);
	  }

	  private static Object quiet(WebDriver driver, String body, Object... args) {
	    try {
	      return js(driver, body, args);
	    } catch (Exception e) {
	      return null;
	    }
	  }

	  @SuppressWarnings("unchecked")
	  private static List<String> strings(WebDriver driver, String body, Object... args) {
	    Object out = quiet(driver, body, args);
	    return out instanceof List ? (List<String>) out : new ArrayList<String>();
	  }

	  private static String valueOf(WebDriver driver, WebElement row) {
	    Object v = quiet(driver, "return value(arguments[0]);", row);
	    return v == null ? "" : v.toString();
	  }

	  private static String norm(String s) {
	    return s == null ? ""
	        : s.replaceAll("(?i)\\(default\\)", "").replaceAll("\\s+", " ").trim().toLowerCase();
	  }

	  /** 'Allow' matches 'Allow (default)', and a trigger reading '<permission> <value>'. */
	  private static boolean applied(String shown, String wanted) {
	    if (shown == null || shown.isEmpty()) {
	      return false;
	    }
	    String s = norm(shown);
	    String w = norm(wanted);
	    return s.equals(w) || (" " + s + " ").contains(" " + w + " ");
	  }

	  private static void click(WebDriver driver, Logger logger, WebElement el, String what) {
	    quiet(driver, "arguments[0].scrollIntoView({block:'center'}); return null;", el);
	    try {
	      el.click();
	    } catch (Exception e) {
	      // custom dropdowns are not always hit-testable; a scripted click still fires the handler
	      log(logger, "clicking " + what + " directly failed ("
	          + ExceptionUtils.getMessage(e) + "), using a scripted click");
	      quiet(driver, "arguments[0].click(); return null;", el);
	    }
	  }
}
