import java.util.List;
import java.util.Scanner;

public class Utils {

    public static int readChoice(Scanner sc, int min, int max) {
        while (true) {
            System.out.print("> ");
            String line = sc.nextLine().trim();
            try {
                int c = Integer.parseInt(line);
                if (c >= min && c <= max) return c;
            } catch (Exception ignored) {}
            System.out.println("Enter a number between " + min + " and " + max);
        }
    }

    public static Zombie selectZombie(Scanner sc, List<Zombie> zombies) {
        System.out.println("Select a target:");
        for (int i = 0; i < zombies.size(); i++) {
            System.out.println((i + 1) + ") " + zombies.get(i).getName() + " HP: " + zombies.get(i).getHP());
        }
        int c = readChoice(sc, 1, zombies.size());
        return zombies.get(c - 1);
    }
}
