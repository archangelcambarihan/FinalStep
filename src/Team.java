import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Team {

    private final List<Character> members = new ArrayList<>();
    private final Inventory inventory = new Inventory();

    public Team() {
        members.add(new Mah());
        members.add(new Rey());
        members.add(new Tess());
    }

    public void showStatus() {
        System.out.println("\nTeam Status:");
        for (Character c : members) {
            System.out.println(c.getName() + " HP: " + c.getHP() + "  Cooldowns: " + c.cooldownString());
        }
    }

    public void showFinalStatus() {
        System.out.println("\nFinal Status:");
        for (Character c : members) {
            System.out.println(c.getName() + " HP: " + c.getHP());
        }
    }

    public boolean isAnyAlive() {
        for (Character c : members) if (c.isAlive()) return true;
        return false;
    }

    public Character chooseCharacter(Scanner sc) {
        System.out.println("Choose a character to act:");
        for (int i = 0; i < members.size(); i++) {
            Character c = members.get(i);
            System.out.println((i + 1) + ") " + c.getName() + " HP: " + c.getHP());
        }
        int choice = Utils.readChoice(sc, 1, members.size());
        Character chosen = members.get(choice - 1);
        if (!chosen.isAlive()) {
            System.out.println(chosen.getName() + " is down and cannot act. Choose another.");
            return chooseCharacter(sc);
        }
        return chosen;
    }

    public Character getRandomAliveMember() {
        List<Character> alive = new ArrayList<>();
        for (Character c : members) if (c.isAlive()) alive.add(c);
        Random r = new Random();
        return alive.get(r.nextInt(alive.size()));
    }

    public void reduceCooldowns() {
        for (Character c : members) c.reduceCooldowns();
    }

    public void resetCooldowns() {
        for (Character c : members) c.resetCooldowns();
    }

    public Inventory getInventory() {
        return inventory;
    }

    public List<Character> getMembers() {
        return members;
    }
}
