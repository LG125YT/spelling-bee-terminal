package deps;

import java.time.LocalDate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Random; // the kids at the AP course dont teach us this? lol
import java.util.List;
import java.util.Map;
import java.util.regex.*;
import java.net.http.*;
import java.net.URI;

import deps.MiniJson;
import deps.Utils;

// Technically, all of these values can be private. I simply don't like relying on getter functions when I can just get the value directly, when needed.
public class SpellingBee {
  public int month;
  public int day;
  public int year;
  public int points;
  public int max_points = 0;
  public ArrayList<String> answered_words = new ArrayList<String>();
  public ArrayList<String> valid_words = new ArrayList<String>(); // Only available for unarchived games.
  public ArrayList<Integer> rankNums = new ArrayList<Integer>(); // Matching required points to achieve a certain rank
  public String[] letters;

  private final String[] statuses = {
      Utils.GREEN + "Good word!" + Utils.RESET,
      Utils.RED + "Words must contain at least 4 letters." + Utils.RESET,
      Utils.RED + "Words must include the center letter." + Utils.RESET,
      Utils.RED + "Our word list does not include words that have numbers, hyphens, or are proper nouns." + Utils.RESET,
      Utils.RED + "No cussing either, sorry." + Utils.RESET,
      Utils.RED + "Word does not exist!" + Utils.RESET,
      Utils.RED + "Only use the provided letters." + Utils.RESET,
      Utils.GREEN + "Game start!" + Utils.RESET,
      Utils.RED + "You already said this word!" + Utils.RESET,
  };
  private final String table = "  %s   %s  \n" +
      Utils.GRAY + "   \\ /   \n" + Utils.RESET +
      "%s " + Utils.GRAY + "- " + Utils.YELLOW + "%s" + Utils.GRAY + " -" + Utils.RESET + " %s\n" +
      Utils.GRAY + "   / \\   \n" + Utils.RESET +
      "  %s   %s  \n";
  private final String RULES = String.format(Utils.RULES, statuses[1].replace(Utils.RED, Utils.BLUE),
      statuses[2].replace(Utils.RED, Utils.BLUE), statuses[3].replace(Utils.RED, Utils.BLUE),
      statuses[4].replace(Utils.RED, Utils.BLUE));
  private final String[] badWords = { "fuck", "shit", "piss", "dick", "bitch" };
  private final String[] ranks = { "Genius!", "Amazing!", "Great!", "Nice!", "Solid!", "Good!", "Moving up!",
      "Good start!" };

  private int code = 7;
  private String message = "";
  private HttpClient client = HttpClient.newHttpClient();
  private Scanner s;
  private Random r = new Random();

  public SpellingBee(Scanner s) {
    this.s = s;
    String[] d = LocalDate.now().toString().split("-");
    setDate(Integer.parseInt(d[0]), Integer.parseInt(d[1]), Integer.parseInt(d[2]));
  }

  public void setDate(int y, int m, int d) {
    year = y;
    month = m;
    day = d;

    LocalDate x = LocalDate.of(y, m, d);
  }

  public String getDate() {
    // I cannot simply do string formatting because single-digit dates will mess
    // things up.
    return LocalDate.of(year, month, day).toString();
  }

  // NYTimes archives all previous Spelling Bee games in their crosswords forum.
  // However, the accepted words for the game are not easily available through
  // this.
  // By getting the HTML page of the current Spelling Bee, you can also get game
  // data of this entire week and the previous week.
  // This includes their words, but since its a limited time frame, this function
  // exists to check if our set game date falls within that time frame.
  public boolean needsArchive() {
    LocalDate tmp = LocalDate.now().minusWeeks(1).minusDays(LocalDate.now().getDayOfWeek().getValue() - 1);
    return tmp.isAfter(LocalDate.of(year, month, day));
  }

  public boolean startGame() throws IOException, InterruptedException {
    getData();
    try {
      letters[0] = letters[0];
    } catch (Exception e) {
      return false;
    }

    System.out.print(RULES);
    System.out.println(Utils.BLUE + "Type " + Utils.YELLOW_BG + "\"/exit\"" + Utils.RESET + Utils.BLUE
        + " at any point to exit the game." + Utils.GREEN);
    System.out.println("Press the enter key to start..." + Utils.RESET);
    s.nextLine();
    return prompt();
  }

  public boolean prompt() throws IOException, InterruptedException {
    if (points == max_points && max_points != 0) {
      System.out.println(Utils.GREEN + "Game win!" + Utils.RESET);
      return false;
    }

    String mp = max_points == 0 ? "" : "/" + max_points;
    String words = "Note: Run \"/help\" for the command list.";
    words += needsArchive()
        ? Utils.YELLOW + " This game does not know when all words have been found.\n"
            + Utils.GREEN + "Answered words:\n"
        : "\nAnswered words:" + Utils.GREEN + "\n";
    words += Utils.formatWords(answered_words);

    System.out.print(message);

    System.out.printf(table, letters[1], letters[2], letters[3], letters[0], letters[4], letters[5], letters[6]);

    System.out.printf("%s\n%s\n%sPoints: %d%s\n", statuses[code], words + Utils.RESET, Utils.GREEN, points, mp);
    System.out.print(getRank() + Utils.RESET);
    System.out.print("> ");
    String in = s.nextLine().toLowerCase();

    if (in.startsWith("/"))
      return commandHandler(in);
    else
      message = "";

    code = checkInput(in);
    return true;
  }

  public boolean commandHandler(String i) {
    switch (i) {
      case "/exit":
        return false;
      case "/help":
        message = Utils.HELP.replace("%s", getDate().replaceAll("-", "/"));
        break;
      case "/rules":
        message = RULES;
        break;
      case "/ranks":
        message = getRankList();
        break;
      case "/shuffle":
        message = Utils.GREEN + "Shuffled letters!\n" + Utils.RESET;
        shuffleLetters();
        break;
      default:
        message = Utils.RED + "Invalid command. Run \"/help\" for the command list.\n" + Utils.RESET;
    }
    return true;
  }

  // Instead of returning a boolean, the function will return a status code
  // indicating why a word is invalid.
  // The first 4 codes coincide with the rule list found above.
  // Text interpretations of the statuses can be found at the index of the
  // "statuses" array.
  public int checkInput(String i) throws IOException, InterruptedException {
    if (i.length() < 4)
      return 1;
    if (i.indexOf(letters[0]) == -1)
      return 2;
    if (i.contains("-") || i.matches("[0-9]")) // too lazy to do proper nouns
      return 3;
    for (int idx = 0; idx < badWords.length; idx++)
      if (i.contains(badWords[idx]))
        return 4;
    if (answered_words.contains(i))
      return 8;

    // String representation of all letters,
    // because arrays don't have a "find at index" method.
    String s = letters[0] + letters[1] + letters[2] + letters[3] + letters[4] + letters[5] + letters[6];
    for (int idx = 0; idx < i.length(); idx++) {
      if (!s.contains(Character.toString(i.charAt(idx))))
        return 6;
    }

    if (needsArchive()) {
      // HTTP request to check for word in dictionary
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create("https://api.dictionaryapi.dev/api/v2/entries/en/" + i)).build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.body().contains("No Definitions Found"))
        return 5;
    } else {
      if (!valid_words.contains(i))
        return 5;
    }

    points += getPoints(i);

    answered_words.add(i);

    return 0;
  }

  public void shuffleLetters() {
    for (int i = 0; i < 5; i++) {
      int[] vals = { r.nextInt(6) + 1, r.nextInt(6) + 1 };
      String tmp = letters[vals[0]];
      letters[vals[0]] = letters[vals[1]];
      letters[vals[1]] = tmp;
    }
  }

  public int getPoints(String i) {
    int p = i.length();
    // 1 point for a 4 letter word,
    // but longer words gives a point for every letter in it.
    if (p == 4)
      p -= 3;
    p += 7; // Pangrams get 7 extra points.
    message = Utils.YELLOW + "Found pangram!\n" + Utils.RESET;

    // The loop here will find if a letter is not a pangram, and if so,
    // remove the 7 points given to only pangrams
    for (int idx = 0; idx < letters.length; idx++)
      if (i.indexOf(letters[idx]) == -1) {
        p -= 7;
        message = "";
        break;
      }

    return p;
  }

  public String getRank() {
    if (max_points == 0)
      return "";
    if (points < rankNums.get(7))
      return "Rank: Beginner\n";

    int idx = 0;
    while (points < rankNums.get(idx)) {
      idx++;
    }

    return "Rank: " + ranks[idx] + "\n";
  }

  public String getRankList() {
    String r = "----------------\n";

    for (int i = 0; i < ranks.length; i++) {
      if (needsArchive()) {
        r += "Sorry, no ranks are available for this game.\n";
        break;
      }

      if (points < rankNums.get(i)) {
        r += "  " + Utils.GRAY;
      } else if (i == 0 || points < rankNums.get(i - 1)) {
        r += "> " + Utils.YELLOW;
      } else {
        r += "  " + Utils.GREEN;
      }
      r += ranks[i] + " | at " + rankNums.get(i) + " points";

      if (i == 0 && points >= rankNums.get(i)) {
        r += Utils.RESET + " <\n";
        continue;
      }

      if (points > rankNums.get(i) && points < rankNums.get(i - 1)) {
        r += " | " + (rankNums.get(i - 1) - points) + " points until " + ranks[i - 1] + " "
            + (rankNums.get(0) - points) + " points until Genius!" + Utils.RESET + " <";
      }

      r += "\n" + Utils.RESET;
    }

    r += "----------------\n";

    return r;
  }

  public void getData() throws IOException, InterruptedException {
    if (!needsArchive()) {
      HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://www.nytimes.com/puzzles/spelling-bee"))
          .GET().build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

      // Regex source:
      // https://github.com/Plastix/Buzz/blob/bd97fa3e23944e8d86ae3cc1e71a9a95ab663d1f/app/src/main/java/io/github/plastix/buzz/network/PuzzleFetcher.kt#L28
      String regex = "gameData = (\\{.*?\\}\\})";

      Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
      Matcher matcher = pattern.matcher(response.body());

      if (matcher.find()) {
        String data = matcher.group(1).replaceAll("\\s+", "").replace("gameData = ", "");

        MiniJson obj = new MiniJson(data);
        Map<String, Object> parsed = obj.parseObject();

        // I know the JSON structure of the game data, so I can ignore the LSP and
        // compiler warnings.
        parsed = (Map<String, Object>) parsed.get("pastPuzzles");
        List<Map<String, Object>> week = (List<Map<String, Object>>) parsed.get("thisWeek");
        for (int i = 0; i < 2; i++) {
          for (int j = 0; j < week.size(); j++) {
            if (getDate().equals((String) week.get(j).get("printDate"))) {
              List<String> l = (List<String>) week.get(j).get("validLetters");

              // Weird workaround, because I can't directly assign. Thanks Java.
              String[] e = { l.get(0), l.get(1), l.get(2), l.get(3), l.get(4), l.get(5), l.get(6) };
              letters = e;

              l = (List<String>) week.get(j).get("answers");
              valid_words = new ArrayList<String>(l);
              for (int k = 0; k < valid_words.size(); k++)
                max_points += getPoints(valid_words.get(k));

              double[] rank_points = { 0.7, 0.5, 0.4, 0.25, 0.15, 0.08, 0.05, 0.02 };
              for (int k = 0; k < ranks.length; k++)
                rankNums.add((int) Math.round(max_points * rank_points[k]));

              break;
            }
          }

          // If letters aren't set, we haven't found anything in this week,
          // so our date is from last week.
          try {
            letters[0] = letters[0];
            break;
          } catch (Exception e) {
            week = (List<Map<String, Object>>) parsed.get("lastWeek");
          }
        }
        return;
      }
    }

    String[] d = { String.valueOf(year), String.valueOf(month), String.valueOf(day) };
    if (d[1].length() == 1)
      d[1] = "0" + d[1];
    if (d[2].length() == 1)
      d[2] = "0" + d[2];

    // NYTimes doesn't like web scraping too much.
    // Lets fake some headers to get around that.
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(
            String.format("https://www.nytimes.com/%s/%s/%s/crosswords/spelling-bee-forum.html", d[0], d[1], d[2])))
        .GET()
        .setHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:145.0) Gecko/20100101 Firefox/145.0")
        .setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
        .setHeader("Accept-Language", "en-US,en;q=0.5")
        // Browsers usually send accept gzip and whatever, but I don't want to deal with
        // decompressing the data myself.
        .setHeader("Accept-Encoding", "none")
        .setHeader("Sec-GPC", "1")
        // This is a restricted header? I
        // can't set it for some reason.
        // .setHeader("Connection", "keep-alive")
        .setHeader("Cookie",
            "nyt-a=gCKkR-FIEKbX8SKDNhuc_k; nyt-purr=cfshprhohckrhdrshgas2taaa; nyt-jkidd=uid=0&lastRequest=1764868715572&activeDays=%5B0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C1%2C0%2C1%2C1%5D&adv=3&a7dv=3&a14dv=3&a21dv=3&lastKnownType=anon&newsStartDate=&entitlements=; datadome=U2V42MSLfVG31CuvVoMU9Hftq~9z7eHM_y_ELauXPmg24ztmEls4LwsBkY_qb3hpMUSePD99mTd4YHiiVMvh814WqC_L1I6E2fYQR2QgkYZ31YFTTjNJSgeTInOFSbyu; nyt-traceid=000000000000000013c9e2aa22bf943c; nyt-b-sid=BklqSxrxMFRHLM8-KQwZ9GHe; nyt-gdpr=0")
        .setHeader("Upgrade-Insecure-Requests", "1")
        .setHeader("Sec-Fetch-Dest", "document")
        .setHeader("Sec-Fetch-Mode", "navigate")
        .setHeader("Sec-Fetch-Site", "none")
        .setHeader("Sec-Fetch-User", "?1")
        .setHeader("If-Modified-Since", "Mon, 01 Dec 2025 17:21:39 GMT")
        .setHeader("Priority", "u=0, i")
        .setHeader("TE", "trailers")
        .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    // thanks chat gippity. I don't know enough regex to do this myself.

    String regex = "<p class=\"content\" style=\"text-transform:\\s*uppercase\"[^>]*>\\s*" +
        "<span[^>]*>([^<]*)</span>\\s*" +
        "<span[^>]*>([^<]*)</span>";

    Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    Matcher matcher = pattern.matcher(response.body());

    if (matcher.find()) {
      String center = matcher.group(1).replaceAll("\\s+", "");
      String others = matcher.group(2).replaceAll("\\s+", "");
      letters = (center + others).split("");
    } else {
      System.out.println("No game found. Try a different date.");
    }
  }
}
