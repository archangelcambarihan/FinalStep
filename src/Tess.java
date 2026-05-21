import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Tess extends Character {

    private final Random rand = new Random();

    public Tess() {
        this.name = "Tess";
        this.hp = 120;
        this.baseAttack = 15;
    }

    protected String skillName(int i) {
        return switch (i) {
            case 1 -> "Practicality";
            case 2 -> "Agility";
            case 3 -> "Endurance";
            default -> "";
        };
    }

    protected void useSkill(int s, Scanner sc, List<Zombie> zombies) {
        switch (s) {
            case 1 -> {
                int dmg = 35 + rand.nextInt(11);
                singleTargetSkill(zombies, sc, dmg);
            }
            case 2 -> {
                nextAttackDouble = true;
                System.out.println(name + " activates Agility: next normal attack will hit twice.");
            }
            case 3 -> {
                for (Zombie z : zombies) {
                    int dmg = 50 + rand.nextInt(21);
                    z.takeDamage(dmg);
                    z.takeDamage(dmg);
                }
                System.out.println(name + " uses Endurance across enemies.");
            }
        }
    }

    public void useSkillGui(int s, Zombie target, List<Zombie> zombies) {
        switch (s) {
            case 1 -> {
                if (target != null) {
                    int dmg = 35 + rand.nextInt(11);
                    target.takeDamage(dmg);
                }
            }
            case 2 -> {
                nextAttackDouble = true;
                System.out.println(name + " activates Agility: next normal attack will hit twice.");
            }
            case 3 -> {
                for (Zombie z : zombies) {
                    int dmg = 50 + rand.nextInt(21);
                    z.takeDamage(dmg);
                    z.takeDamage(dmg);
                }
                System.out.println(name + " uses Endurance across enemies.");
            }
        }
    }
}
