package deps;

public class Utils {
  public static final String RED = "\u001B[31m";
  public static final String GREEN = "\u001B[32m";
  public static final String YELLOW = "\u001B[33m";
  public static final String BLUE = "\u001B[34m";
  public static final String GRAY = "\u001B[90m";
  public static final String YELLOW_BG = "\u001B[43m";
  public static final String RESET = "\u001B[0m";

  // helps make things look prettier, or something.
  public static void clearScreen() {
    if (System.getProperty("os.name").contains("Windows")) {
      try {
        new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
      } catch (Exception e) {
        System.out.println();
      }
    } else {
      System.out.print("\033\143");
    }
  }

  // Wait for an integer input.
  // If invalid or no input, return a specified default value.
  public static int nextIntOr(java.util.Scanner s, String def) {
    String n = s.nextLine();
    if (n.equals(""))
      return Integer.parseInt(def);
    try {
      return Integer.parseInt(n);
    } catch (Exception e) {
      System.out.println(RED + "Invalid input, ignoring." + RESET);
      return Integer.parseInt(def);
    }
  }
}
