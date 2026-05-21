import java.util.Scanner;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        boolean gui = args.length == 0 || (args.length > 0 && args[0].equals("--gui"));
        boolean cli = args.length > 0 && args[0].equals("--cli");
        if (gui && !cli) {
            try {
                Team team = new Team();
                GameGUI guiApp = new GameGUI(team);
                System.setOut(guiApp.getPrintStream());
                Scanner sc = guiApp.getScanner();
                SwingUtilities.invokeLater(guiApp::show);
                new Thread(() -> runGame(sc, team, guiApp)).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            runGame(new Scanner(System.in), new Team(), null);
        }
    }

    public static void runGame(Scanner sc, Team team, GameGUI gui) {
        BattleSystem battle = new BattleSystem(team, sc);
        Storyline story = new Storyline(sc);

        intro(sc);
        boolean cont;

        cont = stage(1, story, team, battle, sc, gui);
        if (!cont) gameOver("You did not survive Stage 1.", story, team);

        cont = stage(2, story, team, battle, sc, gui);
        if (!cont) gameOver("You did not survive Stage 2.", story, team);

        cont = stage(3, story, team, battle, sc, gui);
        if (!cont) gameOver("You did not survive Stage 3.", story, team);

        cont = stage(4, story, team, battle, sc, gui);
        if (!cont) gameOver("You did not survive Stage 4.", story, team);

        cont = stage5(story, team, battle, gui);
        if (!cont) gameOver("You failed at the final challenge.", story, team);

        conclude();
    }

    private static void intro(Scanner sc) {
        System.out.println("=== THE NEXT STEP: Dr. RICO's Experiment ===");
        System.out.println("Year 2098. A secret facility in ruins.");
        System.out.println("At Abistado College of Technology, three students—Mah, Rey, and Tess—are drawn into an underground experiment.");
        System.out.println("A recorded voice echoes through the halls: \"Survive my test or become part of it.\"");
        System.out.println("Press Enter to begin...");
        sc.nextLine();
    }

    private static boolean stage(int n, Storyline story, Team team, BattleSystem battle, Scanner sc, GameGUI gui) {
        switch (n) {
            case 1:
                System.out.println("\n--- Stage 1: Entrance Chamber ---");
                break;
            case 2:
                System.out.println("\n--- Stage 2: Library of Echoes ---");
                break;
            case 3:
                System.out.println("\n--- Stage 3: Training Hall ---");
                break;
            case 4:
                System.out.println("\n--- Stage 4: Observation Wing ---");
                break;
            default:
                System.out.println("\n--- Stage ---");
        }

        boolean logical = story.presentPuzzle(stageTitle(n), n);
        if (logical) {
            System.out.println("You solved the puzzle correctly — no zombies appear.");
            team.resetCooldowns();
        } else {
            System.out.println("Wrong choice — zombies awaken!");
            if (gui != null) {
                battle.startFightRandomGui(gui);
            } else {
                battle.startFightRandom();
            }
            team.resetCooldowns();
            if (!team.isAnyAlive()) return false;
        }

        story.presentScene(n);
        return team.isAnyAlive();
    }

    private static String stageTitle(int n) {
        return switch (n) {
            case 1 -> "Entrance Chamber";
            case 2 -> "Library of Echoes";
            case 3 -> "Training Hall";
            case 4 -> "Observation Wing";
            default -> "Stage";
        };
    }

    private static boolean stage5(Storyline story, Team team, BattleSystem battle, GameGUI gui) {
        System.out.println("\n--- STAGE 5: The Heart of the Experiment ---");
        team.showStatus();
        boolean finalChoiceGood = story.presentPuzzle("Final Console", 5);
        boolean alerted = story.wasFinalPuzzleNonLogical();
        if (alerted) {
            System.out.println("Your choice alerted defenses. Dr. RICO's chamber activates.");
        } else {
            System.out.println("You acted carefully. Proceed to the confrontation.");
        }
        Zombie boss = new Zombie("Dr. RICO", 350, 30);
        boolean survived;
        if (gui != null) {
            survived = battle.bossFightGui(boss, gui);
        } else {
            survived = battle.bossFight(boss);
        }
        team.resetCooldowns();
        if (!survived) return false;
        if (finalChoiceGood && team.isAnyAlive()) {
            System.out.println("\n--- GOOD ENDING ---");
            System.out.println("You sabotaged the experiment and escape. The truth is exposed.");
            story.revealChoicesOutcome(true);
            team.showFinalStatus();
        } else {
            System.out.println("\n--- BAD ENDING ---");
            System.out.println("Dr. RICO's plan persists. You leave the facility changed by the choice.");
            story.revealChoicesOutcome(false);
            team.showFinalStatus();
        }
        return true;
    }

    private static void conclude() {
        System.out.println("The run has ended. Thank you for playing.");
        System.exit(0);
    }

    private static void gameOver(String reason, Storyline story, Team team) {
        System.out.println("\n+++ GAME OVER +++");
        System.out.println(reason);
        story.revealChoicesOutcome(false);
        team.showFinalStatus();
        System.exit(0);
    }
}
