import deps.SpellingBee;
import deps.Utils;
import java.util.Scanner;
import java.time.LocalDate;

public class Main {
  static Scanner s = new Scanner(System.in);
  static boolean run = true; // Game status

  public static void main(String[] args) throws java.io.IOException, InterruptedException {
    SpellingBee b = new SpellingBee(s);

    System.out.printf("%sWelcome to Spelling Bee!\n%sPlay today's challenge? [Y/n]:%s ", Utils.GREEN, Utils.BLUE,
        Utils.RESET);
    String x = s.nextLine();
    if (!x.equals("") && x.toLowerCase().charAt(0) != 'y') {
      while (true) {
        String[] date = LocalDate.now().toString().split("-");
        System.out.printf("Input year of challenge [%s]: ", date[0]);
        int y = Utils.nextIntOr(s, date[0]);
        System.out.printf("Input month of challenge [%s]: ", date[1]);
        int m = Utils.nextIntOr(s, date[1]);
        System.out.printf("Input day of challenge [%s]: ", date[2]);
        int d = Utils.nextIntOr(s, date[2]);
        try {
          b.setDate(y, m, d);
          System.out.printf("You set: %s\n", b.getDate());
          break;
        } catch (Exception e) {
          System.out.println("Please set a valid date.");
        }
      }
    }

    Utils.clearScreen();
    run = b.startGame();

    while (run) {
      Utils.clearScreen();
      run = b.prompt();
    }

    System.out.println("Thanks for playing!");

    s.close();
  }
}
