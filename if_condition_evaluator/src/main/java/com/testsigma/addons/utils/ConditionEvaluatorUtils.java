package com.testsigma.addons.utils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ConditionEvaluatorUtils {

  private ConditionEvaluatorUtils() {
  }

  public static boolean evaluateCondition(String condition) throws Exception {
    condition = toJavaScriptExpression(condition);

    ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
    if (engine == null) {
      engine = new ScriptEngineManager(ConditionEvaluatorUtils.class.getClassLoader()).getEngineByName("JavaScript");
    }
    if (engine == null) {
      engine = new ScriptEngineManager(null).getEngineByName("JavaScript");
    }
    if (engine == null) {
      try {
        engine = new org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory().getScriptEngine();
      } catch (Throwable t) {
        // ignore
      }
    }
    if (engine == null) {
      throw new Exception("JavaScript Engine not available. Check Java version.");
    }

    Object evalResult = engine.eval(condition);
    if (evalResult instanceof Boolean) {
      return (Boolean) evalResult;
    }
    if (evalResult instanceof Number) {
      return ((Number) evalResult).doubleValue() != 0;
    }
    return false;
  }

  public static String toJavaScriptExpression(String condition) {
    try {
      condition = expandContainsWithParenthesizedOrAnd(condition);
      return convertLogicalExpressionToJavaScript(condition);
    } catch (Exception e) {
      return condition;
    }
  }


  private static String expandContainsWithParenthesizedOrAnd(String condition) {
    Pattern startPattern = Pattern.compile("(?i)(\"[^\"]*\"|'[^']*'|[^\\s&|()]+(?:\\s+[^\\s&|()]+)*)\\s+(NOT\\s+)?CONTAINS\\s*\\(");
    Matcher matcher = startPattern.matcher(condition);
    StringBuffer sb = new StringBuffer();
    int lastEnd = 0;
    while (matcher.find()) {
      int openParen = matcher.end() - 1;
      int closeParen = findMatchingParen(condition, openParen);
      if (closeParen < 0) {
        continue;
      }
      sb.append(condition, lastEnd, matcher.start());
      String left = matcher.group(1).trim();
      String notPart = matcher.group(2) != null ? matcher.group(2).trim() + " " : "";
      String inner = condition.substring(openParen + 1, closeParen).trim();
      String expanded = expandInner(left, notPart, inner);
      sb.append(expanded != null ? expanded : condition.substring(matcher.start(), closeParen + 1));
      lastEnd = closeParen + 1;
    }
    sb.append(condition, lastEnd, condition.length());
    String result = sb.toString();
    return result.equals(condition) ? result : expandContainsWithParenthesizedOrAnd(result);
  }

  private static int findMatchingParen(String s, int openIndex) {
    int depth = 1;
    char quote = 0;
    for (int i = openIndex + 1; i < s.length(); i++) {
      char c = s.charAt(i);
      if (quote != 0) {
        if (c == quote && s.charAt(i - 1) != '\\') {
          quote = 0;
        }
        continue;
      }
      if (c == '"' || c == '\'') {
        quote = c;
      } else if (c == '(') depth++;
      else if (c == ')') {
        depth--;
        if (depth == 0) return i;
      }
    }
    return -1;
  }

  private static List<String> splitByTopLevel(String inner, String delimiter) {
    List<String> parts = new ArrayList<>();
    String delim = " " + delimiter.trim() + " ";
    int depth = 0;
    char quote = 0;
    int start = 0;
    for (int i = 0; i <= inner.length() - delim.length(); i++) {
      char c = inner.charAt(i);
      if (quote != 0) {
        if (c == quote && (i == 0 || inner.charAt(i - 1) != '\\')) {
          quote = 0;
        }
        continue;
      }
      if (c == '"' || c == '\'') {
        quote = c;
        continue;
      }
      if (c == '(') depth++;
      else if (c == ')') depth--;
      if (depth == 0 && i >= start && inner.regionMatches(true, i, delim, 0, delim.length())) {
        parts.add(inner.substring(start, i).trim());
        start = i + delim.length();
        i = start - 1;
      }
    }
    parts.add(inner.substring(start).trim());
    return parts;
  }

  private static String stripOuterParens(String s) {
    s = s.trim();
    while (s.startsWith("(")) {
      int close = findMatchingParen(s, 0);
      if (close == s.length() - 1) {
        s = s.substring(1, close).trim();
      } else {
        break;
      }
    }
    return s;
  }

  private static String expandInner(String left, String notPart, String inner) {
    inner = stripOuterParens(inner);
    String op = "CONTAINS";
    // Keep grouped CONTAINS terms together: "High OR Risk AND Override" -> (High OR Risk) AND Override
    List<String> andTerms = splitByTopLevel(inner, "AND");
    if (andTerms.size() > 1) {
      if (!allPartsValid(andTerms)) return null;
      StringBuilder expansion = new StringBuilder();
      for (int i = 0; i < andTerms.size(); i++) {
        if (i > 0) expansion.append(" AND ");
        String term = expandTerm(left, notPart, op, andTerms.get(i));
        expansion.append(term.contains(" OR ") ? "(" + term + ")" : term);
      }
      return expansion.toString();
    }
    List<String> orTerms = splitByTopLevel(inner, "OR");
    if (orTerms.size() > 1) {
      if (!allPartsValid(orTerms)) return null;
      StringBuilder expansion = new StringBuilder();
      for (int i = 0; i < orTerms.size(); i++) {
        if (i > 0) expansion.append(" OR ");
        String term = expandTerm(left, notPart, op, orTerms.get(i));
        expansion.append(term.contains(" AND ") ? "(" + term + ")" : term);
      }
      return expansion.toString();
    }
    return null;
  }

  private static String expandTerm(String left, String notPart, String op, String term) {
    term = term.trim();
    String inner = term.startsWith("(") ? stripOuterParens(term) : term;
    if (term.startsWith("(") || containsLogicalOperator(inner, "OR") || containsLogicalOperator(inner, "AND")) {
      String expanded = expandInner(left, notPart, inner);
      return expanded != null ? expanded : (left + " " + notPart + op + " " + inner);
    }
    return left + " " + notPart + op + " " + term;
  }

  private static boolean allPartsValid(List<String> parts) {
    for (String p : parts) {
      if (p.trim().length() < 2) return false;
    }
    return true;
  }

  private static String convertLogicalExpressionToJavaScript(String expression) {
    expression = stripOuterParens(expression.trim());

    List<String> orTerms = splitByTopLevel(expression, "OR");
    if (orTerms.size() > 1) {
      return joinAsGroupedExpression(orTerms, " || ");
    }

    List<String> andTerms = splitByTopLevel(expression, "AND");
    if (andTerms.size() > 1) {
      return joinAsGroupedExpression(andTerms, " && ");
    }

    return convertAtomicExpressionToJavaScript(expression);
  }

  private static String joinAsGroupedExpression(List<String> terms, String operator) {
    StringBuilder groupedExpression = new StringBuilder("(");
    for (int i = 0; i < terms.size(); i++) {
      if (i > 0) {
        groupedExpression.append(operator);
      }
      groupedExpression.append(convertLogicalExpressionToJavaScript(terms.get(i)));
    }
    groupedExpression.append(")");
    return groupedExpression.toString();
  }

  private static String convertAtomicExpressionToJavaScript(String expression) {
    Matcher matcher = Pattern.compile("(?i)^(.+?)\\s+(NOT\\s+)?CONTAINS\\s+(.+)$").matcher(expression.trim());
    if (!matcher.matches()) {
      return expression;
    }

    String left = unquote(stripOuterParens(matcher.group(1).trim()));
    String right = unquote(stripOuterParens(matcher.group(3).trim()));
    String containsExpression = "String(" + quoteForJs(left) + ").indexOf(String(" + quoteForJs(right) + "))";
    return matcher.group(2) == null ? containsExpression + " !== -1" : containsExpression + " === -1";
  }

  private static boolean containsLogicalOperator(String expression, String operator) {
    return splitByTopLevel(expression, operator).size() > 1;
  }

  private static String unquote(String value) {
    if (value.length() >= 2 && ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'")))) {
      return value.substring(1, value.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\");
    }
    return value;
  }

  private static String quoteForJs(String value) {
    return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
  }
}
