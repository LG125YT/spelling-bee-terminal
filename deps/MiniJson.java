package deps;

import java.util.*;

// dude i asked chatgpt for how to parse json using only built-in libraries, it said it couldnt, and then wrote this shit for me right after

public class MiniJson {

  private final String s;
  private int i = 0;

  public MiniJson(String s) {
    this.s = s;
  }

  public Object parse() {
    skipWs();
    char c = peek();
    if (c == '{')
      return parseObject();
    if (c == '[')
      return parseArray();
    throw new RuntimeException("Invalid JSON");
  }

  public Map<String, Object> parseObject() {
    Map<String, Object> map = new LinkedHashMap<>();
    expect('{');
    skipWs();
    if (peek() == '}') {
      i++;
      return map;
    }

    while (true) {
      skipWs();
      String key = parseString();
      skipWs();
      expect(':');
      skipWs();
      map.put(key, parseValue());
      skipWs();
      char c = expect(',', '}');
      if (c == '}')
        return map;
    }
  }

  public List<Object> parseArray() {
    List<Object> list = new ArrayList<>();
    expect('[');
    skipWs();
    if (peek() == ']') {
      i++;
      return list;
    }

    while (true) {
      skipWs();
      list.add(parseValue());
      skipWs();
      char c = expect(',', ']');
      if (c == ']')
        return list;
    }
  }

  private Object parseValue() {
    skipWs();
    char c = peek();

    if (c == '"')
      return parseString();
    if (c == '{')
      return parseObject();
    if (c == '[')
      return parseArray();
    if (c == 't' && s.startsWith("true", i)) {
      i += 4;
      return true;
    }
    if (c == 'f' && s.startsWith("false", i)) {
      i += 5;
      return false;
    }
    if (c == 'n' && s.startsWith("null", i)) {
      i += 4;
      return null;
    }

    return parseNumber();
  }

  private Number parseNumber() {
    int start = i;
    while (i < s.length() && "-+0123456789.eE".indexOf(s.charAt(i)) >= 0)
      i++;
    return Double.valueOf(s.substring(start, i));
  }

  private String parseString() {
    expect('"');
    StringBuilder sb = new StringBuilder();
    while (true) {
      char c = s.charAt(i++);
      if (c == '"')
        break;
      if (c == '\\') {
        char esc = s.charAt(i++);
        switch (esc) {
          case '"':
            sb.append('"');
            break;
          case '\\':
            sb.append('\\');
            break;
          case '/':
            sb.append('/');
            break;
          case 'b':
            sb.append('\b');
            break;
          case 'f':
            sb.append('\f');
            break;
          case 'n':
            sb.append('\n');
            break;
          case 'r':
            sb.append('\r');
            break;
          case 't':
            sb.append('\t');
            break;
          case 'u':
            int code = Integer.parseInt(s.substring(i, i + 4), 16);
            sb.append((char) code);
            i += 4;
            break;
          default:
            throw new RuntimeException("Bad escape");
        }
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  private void skipWs() {
    while (i < s.length() && Character.isWhitespace(s.charAt(i)))
      i++;
  }

  private char peek() {
    return s.charAt(i);
  }

  private void expect(char c) {
    if (s.charAt(i) != c)
      throw new RuntimeException("Expected '" + c + "'");
    i++;
  }

  private char expect(char c1, char c2) {
    char c = s.charAt(i++);
    if (c != c1 && c != c2)
      throw new RuntimeException("Expected '" + c1 + "' or '" + c2 + "'");
    return c;
  }
}
