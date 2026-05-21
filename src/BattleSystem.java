import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class BattleSystem {

    private final Team team;
    private final Scanner sc;
    private final Random rand = new Random();

    public BattleSystem(Team team, Scanner sc) {
        this.team = team;
        this.sc = sc;
    }

    public void startFightRandom() {
        int count = 1 + rand.nextInt(3);
        List<Zombie> zombies = new ArrayList<>();
        for (int i = 0; i < count; i++) zombies.add(new Zombie("Zombie " + (i + 1)));
        fight(zombies);
    }

    public boolean bossFight(Zombie boss) {
        List<Zombie> single = new ArrayList<>();
        single.add(boss);
        fight(single);
        return team.isAnyAlive();
    }

    public void startFightRandomGui(GameGUI gui) {
        int count = 1 + rand.nextInt(3);
        List<Zombie> zombies = new ArrayList<>();
        for (int i = 0; i < count; i++) zombies.add(new Zombie("Zombie " + (i + 1)));
        fightGui(zombies, gui);
    }

    public boolean bossFightGui(Zombie boss, GameGUI gui) {
        List<Zombie> single = new ArrayList<>();
        single.add(boss);
        fightGui(single, gui);
        return team.isAnyAlive();
    }

    private void fight(List<Zombie> zombies) {
        int turn = 1;
        while (team.isAnyAlive() && anyAlive(zombies)) {
            System.out.println("\n--- TURN " + turn + " ---");
            team.showStatus();
            showZombies(zombies);
            team.reduceCooldowns();
            Character actor = team.chooseCharacter(sc);
            actor.act(sc, zombies, team.getInventory());
            removeDead(zombies);
            if (anyAlive(zombies)) zombiesAttack(zombies);
            removeDead(zombies);
            turn++;
        }
    }

    private void fightGui(List<Zombie> zombies, GameGUI gui) {
        int turn = 1;
        gui.setBattleZombies(zombies);
        while (team.isAnyAlive() && anyAlive(zombies)) {
            gui.log("\n--- TURN " + turn + " ---");
            gui.refreshHeroStatus();
            showZombiesGui(zombies, gui);
            team.reduceCooldowns();
            Character actor = gui.chooseCharacter(team.getMembers());
            gui.log(actor.getName() + " is taking action.");
            GameGUI.GuiAction action = gui.chooseAction(actor);
            executeGuiAction(actor, zombies, action, gui);
            removeDead(zombies);
            gui.setBattleZombies(zombies);
            if (anyAlive(zombies)) {
                zombiesAttackGui(zombies, gui);
                removeDead(zombies);
                gui.setBattleZombies(zombies);
            }
            turn++;
        }
        gui.endBattle();
    }

    private void executeGuiAction(Character actor, List<Zombie> zombies, GameGUI.GuiAction action, GameGUI gui) {
        if (action == null) {
            gui.log("No action selected. Turn skipped.");
            return;
        }
        switch (action.getType()) {
            case ATTACK -> {
                Zombie target = gui.chooseTarget(zombies);
                if (target != null) {
                    gui.flashHeroAttack(actor.getName());
                    actor.performNormalAttack(target);
                    gui.flashZombieDamage(target.getName());
                } else {
                    gui.log("No target selected. Attack cancelled.");
                }
            }
            case SKILL_1, SKILL_2, SKILL_3 -> {
                Zombie target = gui.chooseTarget(zombies);
                int skillIndex = action.getType() == GameGUI.GuiActionType.SKILL_1 ? 1 : action.getType() == GameGUI.GuiActionType.SKILL_2 ? 2 : 3;
                gui.flashHeroAttack(actor.getName());
                actor.performSkill(skillIndex, target, zombies);
                if (target != null) {
                    gui.flashZombieDamage(target.getName());
                } else {
                    for (Zombie z : zombies) gui.flashZombieDamage(z.getName());
                }
            }
            case HEAL -> {
                Item potion = gui.chooseHealItem(team.getInventory());
                if (potion != null) {
                    team.getInventory().consumeItem(potion.getName());
                    actor.applyItem(potion, team.getInventory());
                    gui.log(actor.getName() + " uses " + potion.getName() + ".");
                } else {
                    gui.log("No healing items available.");
                }
            }
            case PASS -> gui.log(actor.getName() + " passes the turn.");
        }
    }

    private void zombiesAttackGui(List<Zombie> zombies, GameGUI gui) {
        gui.log("Zombies attack!");
        for (Zombie z : zombies) {
            if (!z.isAlive()) continue;
            Character target = team.getRandomAliveMember();
            int dmg = z.attack();
            target.takeDamage(dmg);
            gui.log(z.getName() + " deals " + dmg + " damage to " + target.getName());
            if (!team.isAnyAlive()) break;
        }
    }

    private void showZombiesGui(List<Zombie> zombies, GameGUI gui) {
        gui.log("Enemies:");
        for (int i = 0; i < zombies.size(); i++) {
            Zombie z = zombies.get(i);
            gui.log((i + 1) + ") " + z.getName() + " HP: " + z.getHP());
        }
    }

    private void zombiesAttack(List<Zombie> zombies) {
        System.out.println("Zombies attack!");
        for (Zombie z : zombies) {
            if (!z.isAlive()) continue;
            Character target = team.getRandomAliveMember();
            int dmg = z.attack();
            target.takeDamage(dmg);
            System.out.println(z.getName() + " deals " + dmg + " damage to " + target.getName());
            if (!team.isAnyAlive()) break;
        }
    }

    private boolean anyAlive(List<Zombie> list) {
        for (Zombie z : list) if (z.isAlive()) return true;
        return false;
    }

    private void removeDead(List<Zombie> list) {
        list.removeIf(z -> !z.isAlive());
    }

    private void showZombies(List<Zombie> zombies) {
        for (int i = 0; i < zombies.size(); i++) {
            Zombie z = zombies.get(i);
            System.out.println((i + 1) + ") " + z.getName() + " HP: " + z.getHP());
        }
    }
}
