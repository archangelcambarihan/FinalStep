import java.util.List;
import java.util.Scanner;

public abstract class Character {

    protected String name;
    protected int hp;
    protected int baseAttack;
    protected int[] cooldowns = new int[3];
    protected boolean nextAttackDouble = false;
    protected int nextAttackBonus = 0;

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

    public void reduceCooldowns() {
        for (int i = 0; i < cooldowns.length; i++) if (cooldowns[i] > 0) cooldowns[i]--;
    }

    public void resetCooldowns() {
        for (int i = 0; i < cooldowns.length; i++) cooldowns[i] = 0;
    }

    public String cooldownString() {
        return "[" + cooldowns[0] + "," + cooldowns[1] + "," + cooldowns[2] + "]";
    }

    public int getBaseAttack() {
        return baseAttack;
    }

    public void restoreHP(int amount) {
        hp = Math.min(hp + amount, 100);
    }

    public void act(Scanner sc, List<Zombie> zombies, Inventory inventory) {
        System.out.println("1) Normal Attack");
        System.out.println("2) Use Skill");
        System.out.println("3) Use Item");
        int a = Utils.readChoice(sc, 1, 3);
        if (a == 1) normalAttack(sc, zombies);
        else if (a == 2) useSkillMenu(sc, zombies);
        else useItemMenu(sc, inventory);
    }

    public void performNormalAttack(Zombie z) {
        int dmg = baseAttack;
        if (nextAttackDouble) {
            z.takeDamage(dmg);
            z.takeDamage(dmg);
            System.out.println(name + " performs a doubled normal attack on " + z.getName() + " for " + (dmg * 2) + " total.");
            nextAttackDouble = false;
        } else {
            z.takeDamage(dmg + nextAttackBonus);
            System.out.println(name + " hits " + z.getName() + " for " + (dmg + nextAttackBonus) + " damage.");
            nextAttackBonus = 0;
        }
    }

    public void performSkill(int s, Zombie target, List<Zombie> zombies) {
        if (s < 1 || s > 3) return;
        if (cooldowns[s - 1] > 0) {
            System.out.println(name + " cannot use " + skillName(s) + " yet. Cooldown: " + cooldowns[s - 1]);
            return;
        }
        useSkillGui(s, target, zombies);
        cooldowns[s - 1] = (s == 1) ? 1 : 3;
    }

    public void applyItem(Item item, Inventory inventory) {
        if (item == null) return;
        if (item.getType().equals("heal")) {
            int oldHp = hp;
            hp = Math.min(hp + item.getEffectValue(), 100);
            System.out.println(name + " used " + item.getName() + " and recovered " + (hp - oldHp) + " HP!");
        } else if (item.getType().equals("attack_boost")) {
            nextAttackBonus += item.getEffectValue();
            System.out.println(name + " used " + item.getName() + " and gained +" + item.getEffectValue() + " attack power!");
        } else if (item.getType().equals("defense_boost")) {
            System.out.println(name + " used " + item.getName() + " and gained +" + item.getEffectValue() + " defense!");
        }
    }

    public int[] getCooldowns() {
        return cooldowns.clone();
    }

    private void useItemMenu(Scanner sc, Inventory inventory) {
        inventory.showInventory();
        System.out.println("\nChoose an item to use (0 to cancel):");
        int choice = Utils.readChoice(sc, 0, inventory.getItems().size());
        if (choice == 0) return;
        
        Item item = inventory.getItem(choice - 1);
        if (item == null || item.getQuantity() <= 0) {
            System.out.println("Item not available.");
            return;
        }
        
        useItem(item, sc, inventory);
    }

    private void useItem(Item item, Scanner sc, Inventory inventory) {
        if (item.getType().equals("heal")) {
            int oldHp = hp;
            hp = Math.min(hp + item.getEffectValue(), 100);
            System.out.println(name + " used " + item.getName() + " and recovered " + (hp - oldHp) + " HP!");
        } else if (item.getType().equals("attack_boost")) {
            nextAttackBonus += item.getEffectValue();
            System.out.println(name + " used " + item.getName() + " and gained +" + item.getEffectValue() + " attack power!");
        } else if (item.getType().equals("defense_boost")) {
            System.out.println(name + " used " + item.getName() + " and gained +" + item.getEffectValue() + " defense!");
        }
    }

    private void normalAttack(Scanner sc, List<Zombie> zombies) {
        Zombie z = Utils.selectZombie(sc, zombies);
        int dmg = baseAttack;
        if (nextAttackDouble) {
            z.takeDamage(dmg);
            z.takeDamage(dmg);
            System.out.println(name + " performs a doubled normal attack on " + z.getName() + " for " + (dmg * 2) + " total.");
            nextAttackDouble = false;
        } else {
            z.takeDamage(dmg + nextAttackBonus);
            System.out.println(name + " hits " + z.getName() + " for " + (dmg + nextAttackBonus) + " damage.");
            nextAttackBonus = 0;
        }
    }

    private void useSkillMenu(Scanner sc, List<Zombie> zombies) {
        System.out.println("Choose a skill:");
        System.out.println("1) " + skillName(1) + " (CD 1)");
        System.out.println("2) " + skillName(2) + " (CD 3)");
        System.out.println("3) " + skillName(3) + " (CD 3)");
        int s = Utils.readChoice(sc, 1, 3);
        if (cooldowns[s - 1] > 0) {
            System.out.println("That skill is on cooldown for " + cooldowns[s - 1] + " more turns.");
            return;
        }
        useSkill(s, sc, zombies);
        cooldowns[s - 1] = (s == 1) ? 1 : 3;
    }

    protected void singleTargetSkill(List<Zombie> zombies, Zombie target, int dmg) {
        if (target == null || !target.isAlive()) return;
        target.takeDamage(dmg);
        target.takeDamage(dmg);
        System.out.println(name + " uses a focused skill on " + target.getName() + " for " + (dmg * 2) + " total damage.");
    }

    protected void singleTargetSkill(List<Zombie> zombies, Scanner sc, int dmg) {
        Zombie z = Utils.selectZombie(sc, zombies);
        singleTargetSkill(zombies, z, dmg);
    }

    protected void allTargetSkill(List<Zombie> zombies, int dmg) {
        for (Zombie z : zombies) {
            z.takeDamage(dmg);
            z.takeDamage(dmg);
        }
        System.out.println(name + " uses an ultimate dealing " + (dmg * 2) + " to all targets.");
    }

    protected abstract String skillName(int i);
    protected abstract void useSkill(int s, Scanner sc, List<Zombie> zombies);
    protected abstract void useSkillGui(int s, Zombie target, List<Zombie> zombies);
}
