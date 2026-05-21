import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Rey extends Character {

    private final Random rand = new Random();

    public Rey() {
        this.name = "Rey";
        this.hp = 120;
        this.baseAttack = 15;
    }

    protected String skillName(int i) {
        return switch (i) {
            case 1 -> "Riddles";
            case 2 -> "Strat Master";
            case 3 -> "Mind Bank";
            default -> "";
        };
    }

    protected void useSkill(int s, Scanner sc, List<Zombie> zombies) {
        switch (s) {
            case 1 -> {
                for (Zombie z : zombies) {
                    int dmg = 10 + rand.nextInt(11);
                    z.takeDamage(dmg);
                    z.takeDamage(dmg);
                }
                System.out.println(name + " uses Riddles across enemies.");
            }
            case 2 -> singleTargetSkill(zombies, sc, 25);
            case 3 -> {
                for (Zombie z : zombies) {
                    int dmg = 30 + rand.nextInt(11);
                    z.takeDamage(dmg);
                    z.takeDamage(dmg);
                }
                System.out.println(name + " uses Mind Bank across enemies.");
            }
        }
    }

    @Override
    public void useSkillGui(int s, Zombie target, List<Zombie> zombies) {
        switch (s) {
            case 1 -> {
                for (Zombie z : zombies) {
                    int dmg = 10 + rand.nextInt(11);
                    z.takeDamage(dmg);
                    z.takeDamage(dmg);
                }
                System.out.println(name + " uses Riddles across enemies.");
            }
            case 2 -> {
                if (target != null) {
                    target.takeDamage(25);
                }
                System.out.println(name + " uses Strat Master on a single enemy.");
            }
            case 3 -> {
                for (Zombie z : zombies) {
                    int dmg = 30 + rand.nextInt(11);
                    z.takeDamage(dmg);
                    z.takeDamage(dmg);
                }
                System.out.println(name + " uses Mind Bank across enemies.");
            }
        }
    }
}
