import java.util.Scanner;

public class Storyline {

    private final Scanner sc;
    private final int[] choices = new int[5];
    private boolean finalDecisionGood = false;
    private boolean finalPuzzleNonLogical = false;

    public Storyline(Scanner sc) {
        this.sc = sc;
    }

    public boolean presentPuzzle(String title, int stage) {
        System.out.println("\n" + title + " — A puzzle blocks your path.");
        switch (stage) {
            case 1:
                return puzzleOne();
            case 2:
                return puzzleTwo();
            case 3:
                return puzzleThree();
            case 4:
                return puzzleFour();
            case 5:
                return puzzleFive();
            default:
                return true;
        }
    }

    private boolean puzzleOne() {
        System.out.println("You find three glyphs etched into a door. Which do you press first?");
        System.out.println("1) The glyph matching the faded mural's pattern");
        System.out.println("2) The largest glyph");
        System.out.println("3) The glyph that glows faintly");
        int c = Utils.readChoice(sc, 1, 3);
        choices[0] = c;
        return c == 1;
    }

    private boolean puzzleTwo() {
        System.out.println("A ledger lists broken coordinates. How do you reconstruct the path?");
        System.out.println("1) Match partial lines to the campus map; fill gaps logically");
        System.out.println("2) Follow the newest-looking notes only");
        System.out.println("3) Try random corridors until one opens");
        int c = Utils.readChoice(sc, 1, 3);
        choices[1] = c;
        return c == 1;
    }

    private boolean puzzleThree() {
        System.out.println("A console shows three color-coded switches. Which is safest to flip?");
        System.out.println("1) The switch that completes the circuit indicated by the diagram");
        System.out.println("2) The switch marked with an unknown symbol");
        System.out.println("3) Flip all switches at once to be quick");
        int c = Utils.readChoice(sc, 1, 3);
        choices[2] = c;
        return c == 1;
    }

    private boolean puzzleFour() {
        System.out.println("A stabilizer hums. How do you proceed?");
        System.out.println("1) Carefully disengage the stabilizer using the manual latch");
        System.out.println("2) Smash the casing to stop it immediately");
        System.out.println("3) Walk around and ignore it");
        int c = Utils.readChoice(sc, 1, 3);
        choices[3] = c;
        return c == 1;
    }

    private boolean puzzleFive() {
        System.out.println("The final console offers three choices. Which action do you take?");
        System.out.println("1) Release valves to flood and sabotage the experiment");
        System.out.println("2) Leave valves and confront Dr. RICO directly");
        System.out.println("3) Accept Dr. RICO's offer displayed on the console");
        int c = Utils.readChoice(sc, 1, 3);
        choices[4] = c;
        finalDecisionGood = (c == 1);
        finalPuzzleNonLogical = (c != 1);
        return c == 1;
    }

    public boolean wasFinalPuzzleNonLogical() {
        return finalPuzzleNonLogical;
    }

    public boolean getFinalDecision() {
        return finalDecisionGood;
    }

    public void presentScene(int stage) {
        switch (stage) {
            case 1:
                System.out.println("\nYou enter a rusted chamber. The mural on the wall tells of ambition and regret.");
                break;
            case 2:
                System.out.println("\nDusty stacks reveal research notes about a serum meant to unlock the mind.");
                break;
            case 3:
                System.out.println("\nTraining apparatus and diagrams show subjects being tested for speed and cognition.");
                break;
            case 4:
                System.out.println("\nObservation windows stare into tanks and notebooks with names crossed out.");
                break;
            default:
                System.out.println("\nYou continue onward.");
        }
    }

    public void revealChoicesOutcome(boolean goodEnding) {
        System.out.println("\n--- Choices Revealed ---");
        for (int i = 0; i < choices.length; i++) {
            System.out.print("Stage " + (i + 1) + ": ");
            int c = choices[i];
            if (i < 4) {
                if (c == 1) System.out.print("You chose the logical/careful path. ");
                else if (c == 2) System.out.print("You chose a pragmatic/quicker path. ");
                else System.out.print("You chose an extreme or reckless option. ");
            } else {
                if (c == 1) System.out.print("You sabotaged the lab. ");
                else if (c == 2) System.out.print("You confronted the scientist. ");
                else System.out.print("You allied with Dr. RICO. ");
            }
            if (goodEnding) System.out.println("This helped produce a better outcome.");
            else System.out.println("This contributed to the darker result.");
        }
    }
}
