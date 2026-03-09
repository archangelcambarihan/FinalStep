import java.util.Random;

public class Zombie {

    private final String name;
    private int hp;
    private final int attackBase;
    private final Random rand = new Random();

    public Zombie(String name) {
        this.name = name;
        this.hp = 30 + rand.nextInt(21);
        this.attackBase = 6 + rand.nextInt(5);
    }

    public Zombie(String name, int hp, int attackBase) {
        this.name = name;
        this.hp = hp;
        this.attackBase = attackBase;
    }

    public String getName() {
        return name;
    }

    public int getHP() {
        return hp;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public void takeDamage(int dmg) {
        if (hp <= 0) return;
        hp = Math.max(0, hp - dmg);
        System.out.println(name + " takes " + dmg + " damage.");
    }

    public int attack() {
        return attackBase + rand.nextInt(6);
    }
}
