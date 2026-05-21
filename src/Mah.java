import java.util.List;
import java.util.Scanner;

public class Mah extends Character {

    public Mah() {
        this.name = "Mah";
        this.hp = 120;
        this.baseAttack = 15;
    }

    protected String skillName(int i) {
        return switch (i) {
            case 1 -> "Observe";
            case 2 -> "Intuition";
            case 3 -> "Analytical Mind";
            default -> "";
        };
    }

    protected void useSkill(int s, Scanner sc, List<Zombie> zombies) {
        switch (s) {
            case 1 -> singleTargetSkill(zombies, sc, 30);
            case 2 -> {
                nextAttackBonus += 10;
                System.out.println(name + " activates Intuition: next attack will deal +10 bonus.");
            }
            case 3 -> allTargetSkill(zombies, 45);
        }
    }

    @Override
    public void useSkillGui(int s, Zombie target, List<Zombie> zombies) {
        switch (s) {
            case 1 -> {
                if (target != null) {
                    singleTargetSkill(zombies, target, 30);
                } else {
                    System.out.println(name + " tried to use Observe but no target was chosen.");
                }
            }
            case 2 -> {
                nextAttackBonus += 10;
                System.out.println(name + " activates Intuition: next attack will deal +10 bonus.");
            }
            case 3 -> allTargetSkill(zombies, 45);
        }
    }
}
