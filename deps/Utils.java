package deps;

import java.util.ArrayList;

public class Utils {
  public static final String RED = "\u001B[31m";
  public static final String GREEN = "\u001B[32m";
  public static final String YELLOW = "\u001B[33m";
  public static final String BLUE = "\u001B[34m";
  public static final String GRAY = "\u001B[90m";
  public static final String YELLOW_BG = "\u001B[43m";
  public static final String RESET = "\u001B[0m";

  public static final String RULES = "----------------\n" + YELLOW + "Rules:\n" + RESET +
      "1. %s\n" +
      "2. %s\n" +
      "3. %s\n" +
      "4. %s\n" +
      "----------------\n" +
      YELLOW + "Letters can be used more than once.\n" +
      "Score points to increase your rating.\n" + RESET +
      "1. " + BLUE + "4-letter words are worth 1 point each.\n" + RESET +
      "2. " + BLUE + "Longer words earn 1 point per letter.\n" + RESET +
      "3. " + BLUE +
      "Each puzzle includes at least one “pangram” which uses every letter. These are worth 7 extra points!\n" + RESET +
      "----------------\n";
  public static final String HELP = "----------------\n" + YELLOW + "Help:\n" + RESET +
      "/help - Shows this message.\n" +
      "/rules - Shows the rules of the game.\n" +
      "/exit - Exit the game.\n" +
      "/ranks - Show the rank list with your current rank.\n" +
      "----------------\n";

  // how many words to print before wrapping to the next column
  private final static int LOOPVAL = 10;

  // mightve put too much work into this tbh
  public static String formatWords(ArrayList<String> words) {
    int columns = words.size() / LOOPVAL;
    if (words.size() % LOOPVAL != 0)
      columns++;

    int width = 0;
    // today i learned:
    for (String w : words)
      width = Math.max(width, w.length());
    width += 2; // spacing

    StringBuilder result = new StringBuilder();

    for (int i = 0; i < Math.min(words.size(), LOOPVAL); i++) {
      for (int j = 0; j < columns; j++) {
        int idx = j * LOOPVAL + i;
        if (idx < words.size()) {
          result.append(String.format("%-" + width + "s", words.get(idx)));
        }
      }
      result.append("\n");
    }

    return result.toString();
  }

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
