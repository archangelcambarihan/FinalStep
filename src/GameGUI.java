import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Scanner;

public class GameGUI {

    private final Team team;
    private final PipedInputStream pipedIn;
    private final PipedOutputStream pipedOut;
    private final Scanner scanner;
    private final PrintStream printStream;

    private JFrame frame;
    private JTextArea textArea;
    private JTextField inputField;
    private JLabel statusLabel;
    private JPanel enemyPanel;

    private final List<JLabel> heroHpLabels = new ArrayList<>();
    private final List<JLabel> heroCdLabels = new ArrayList<>();
    private final List<JPanel> heroCardPanels = new ArrayList<>();
    private final List<JButton> actorButtons = new ArrayList<>();
    private final List<JButton> actionButtons = new ArrayList<>();
    private final List<JLabel> enemyLabels = new ArrayList<>();
    private final ResourceLoader resources = new ResourceLoader();
    private final Set<String> damagingZombies = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> attackingHeroes = Collections.synchronizedSet(new HashSet<>());
    private final LinkedBlockingQueue<GuiCommand> commandQueue = new LinkedBlockingQueue<>();

    private volatile List<Zombie> currentZombies = new ArrayList<>();
    private volatile Character activeActor;
    private BattlePanel battlePanel;

    public enum GuiActionType {
        ATTACK,
        SKILL_1,
        SKILL_2,
        SKILL_3,
        HEAL,
        PASS
    }

    public static class GuiAction {
        private final GuiActionType type;

        public GuiAction(GuiActionType type) {
            this.type = type;
        }

        public GuiActionType getType() {
            return type;
        }
    }

    private static class GuiCommand {
        enum Type { ACTOR, ACTION }

        final Type type;
        final int index;

        GuiCommand(Type type, int index) {
            this.type = type;
            this.index = index;
        }
    }

    public GameGUI() throws IOException {
        this(new Team());
    }

    public GameGUI(Team team) throws IOException {
        this.team = team;
        pipedOut = new PipedOutputStream();
        pipedIn = new PipedInputStream(pipedOut);
        scanner = new Scanner(pipedIn, StandardCharsets.UTF_8.name());

        OutputStream uiOut = new OutputStream() {
            private final ByteArrayOutputStream buf = new ByteArrayOutputStream();

            @Override
            public void write(int b) {
                synchronized (buf) {
                    buf.write(b);
                    if (b == '\n') flush();
                }
            }

            @Override
            public void flush() {
                final String s;
                synchronized (buf) {
                    s = buf.toString(StandardCharsets.UTF_8);
                    buf.reset();
                }
                if (s.length() == 0) return;
                SwingUtilities.invokeLater(() -> {
                    textArea.append(s);
                    textArea.setCaretPosition(textArea.getDocument().getLength());
                    refreshHeroStatus();
                });
            }
        };

        printStream = new PrintStream(uiOut, true, StandardCharsets.UTF_8.name());
    }

    public Scanner getScanner() {
        return scanner;
    }

    public PrintStream getPrintStream() {
        return printStream;
    }

    public void show() {
        frame = new JFrame("The Next Step — GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1024, 720));

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        textArea.setBackground(new Color(25, 25, 25));
        textArea.setForeground(Color.WHITE);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Battle Log"));
        scrollPane.setPreferredSize(new Dimension(340, 0));

        battlePanel = new BattlePanel();
        JPanel bottomPanel = createBottomPanel();

        frame.getContentPane().setLayout(new BorderLayout(10, 10));
        frame.getContentPane().add(battlePanel, BorderLayout.CENTER);
        frame.getContentPane().add(scrollPane, BorderLayout.EAST);
        frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        frame.getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        inputField.requestFocusInWindow();

        refreshHeroStatus();
        refreshEnemyStatus();
    }

    private JPanel createBottomPanel() {
        statusLabel = new JLabel("Ready.");
        statusLabel.setForeground(Color.WHITE);

        JPanel heroPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        heroPanel.setOpaque(false);
        for (Character member : team.getMembers()) {
            heroPanel.add(createHeroCard(member));
        }

        enemyPanel = new JPanel(new GridLayout(0, 1, 4, 4));
        enemyPanel.setOpaque(false);
        enemyPanel.setBorder(BorderFactory.createTitledBorder("Enemies"));
        for (int i = 0; i < 4; i++) {
            JLabel enemyLabel = new JLabel("Waiting for battle...");
            enemyLabel.setForeground(Color.WHITE);
            enemyLabels.add(enemyLabel);
            enemyPanel.add(enemyLabel);
        }

        JPanel statsArea = new JPanel(new BorderLayout(10, 10));
        statsArea.setOpaque(false);
        statsArea.add(heroPanel, BorderLayout.CENTER);
        statsArea.add(enemyPanel, BorderLayout.EAST);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        actionRow.setOpaque(false);
        actionRow.setBorder(BorderFactory.createTitledBorder("Actions"));

        JButton attackButton = new JButton("Attack");
        JButton skill1Button = new JButton("Skill 1");
        JButton skill2Button = new JButton("Skill 2");
        JButton skill3Button = new JButton("Skill 3");
        JButton healButton = new JButton("Heal");
        JButton passButton = new JButton("Pass");

        actionButtons.add(attackButton);
        actionButtons.add(skill1Button);
        actionButtons.add(skill2Button);
        actionButtons.add(skill3Button);
        actionButtons.add(healButton);
        actionButtons.add(passButton);

        attackButton.addActionListener(e -> enqueueCommand(new GuiCommand(GuiCommand.Type.ACTION, GuiActionType.ATTACK.ordinal())));
        skill1Button.addActionListener(e -> enqueueCommand(new GuiCommand(GuiCommand.Type.ACTION, GuiActionType.SKILL_1.ordinal())));
        skill2Button.addActionListener(e -> enqueueCommand(new GuiCommand(GuiCommand.Type.ACTION, GuiActionType.SKILL_2.ordinal())));
        skill3Button.addActionListener(e -> enqueueCommand(new GuiCommand(GuiCommand.Type.ACTION, GuiActionType.SKILL_3.ordinal())));
        healButton.addActionListener(e -> enqueueCommand(new GuiCommand(GuiCommand.Type.ACTION, GuiActionType.HEAL.ordinal())));
        passButton.addActionListener(e -> enqueueCommand(new GuiCommand(GuiCommand.Type.ACTION, GuiActionType.PASS.ordinal())));

        actionRow.add(attackButton);
        actionRow.add(skill1Button);
        actionRow.add(skill2Button);
        actionRow.add(skill3Button);
        actionRow.add(healButton);
        actionRow.add(passButton);

        JPanel actorRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        actorRow.setOpaque(false);
        actorRow.setBorder(BorderFactory.createTitledBorder("Select Active Hero"));
        for (int i = 0; i < team.getMembers().size(); i++) {
            Character member = team.getMembers().get(i);
            JButton actorButton = new JButton(member.getName());
            final int index = i;
            actorButton.addActionListener(e -> enqueueCommand(new GuiCommand(GuiCommand.Type.ACTOR, index)));
            actorButtons.add(actorButton);
            actorRow.add(actorButton);
        }

        JPanel commandRow = new JPanel(new BorderLayout(8, 8));
        commandRow.setOpaque(false);
        inputField = new JTextField();
        inputField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        inputField.addActionListener((ActionEvent e) -> sendInput());
        JLabel prompt = new JLabel("Command:");
        prompt.setForeground(Color.WHITE);
        commandRow.add(prompt, BorderLayout.WEST);
        commandRow.add(inputField, BorderLayout.CENTER);

        JLabel hint = new JLabel("Use buttons for battle actions or type commands for puzzles.");
        hint.setForeground(new Color(200, 200, 200));

        JPanel controlsPanel = new JPanel(new BorderLayout(10, 10));
        controlsPanel.setOpaque(false);
        controlsPanel.add(actionRow, BorderLayout.NORTH);
        controlsPanel.add(commandRow, BorderLayout.CENTER);
        controlsPanel.add(hint, BorderLayout.SOUTH);

        JPanel bottom = new JPanel(new BorderLayout(12, 12));
        bottom.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80), 2),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        bottom.setBackground(new Color(38, 37, 35));
        bottom.add(statusLabel, BorderLayout.NORTH);
        bottom.add(statsArea, BorderLayout.CENTER);
        bottom.add(actorRow, BorderLayout.WEST);
        bottom.add(controlsPanel, BorderLayout.SOUTH);

        setActorButtonsEnabled(false);
        setActionButtonsEnabled(false);

        return bottom;
    }

    private JPanel createHeroCard(Character member) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setOpaque(false);
        card.setBackground(new Color(28, 28, 28));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        BufferedImage avatar = resources.getStaticImage(member.getName().toLowerCase());
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setIcon(new ImageIcon(resources.scaleImage(avatar != null ? avatar : resources.createPlaceholder(member.getName()), 72, 72)));

        JLabel nameLabel = new JLabel(member.getName());
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 14f));
        JLabel hpLabel = new JLabel("HP: " + member.getHP());
        hpLabel.setForeground(Color.GREEN.brighter());
        JLabel cdLabel = new JLabel("Cooldowns: " + member.cooldownString());
        cdLabel.setForeground(new Color(200, 200, 200));

        heroHpLabels.add(hpLabel);
        heroCdLabels.add(cdLabel);
        heroCardPanels.add(card);

        JPanel textPanel = new JPanel(new GridLayout(3, 1, 4, 4));
        textPanel.setOpaque(false);
        textPanel.add(nameLabel);
        textPanel.add(hpLabel);
        textPanel.add(cdLabel);

        card.add(imageLabel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    public Character chooseCharacter(List<Character> members) {
        setStatus("Select a character to act.");
        setActorButtonsEnabled(true);
        setActionButtonsEnabled(false);
        GuiCommand command = waitForCommand(GuiCommand.Type.ACTOR);
        Character chosen = members.get(command.index);
        activeActor = chosen;
        refreshHeroStatus();
        setStatus("Selected " + chosen.getName() + ". Choose an action.");
        return chosen;
    }

    public GuiAction chooseAction(Character actor) {
        setActionButtonsEnabled(true);
        setActorButtonsEnabled(false);
        GuiCommand command = waitForCommand(GuiCommand.Type.ACTION);
        GuiAction action = new GuiAction(GuiActionType.values()[command.index]);
        setActionButtonsEnabled(false);
        return action;
    }

    public Zombie chooseTarget(List<Zombie> zombies) {
        if (zombies.isEmpty()) return null;
        String[] options = new String[zombies.size()];
        for (int i = 0; i < zombies.size(); i++) {
            Zombie z = zombies.get(i);
            options[i] = z.getName() + " (HP: " + z.getHP() + ")";
        }
        final int[] result = {-1};
        try {
            SwingUtilities.invokeAndWait(() -> {
                int choice = JOptionPane.showOptionDialog(frame,
                        "Select a target:",
                        "Target Selection",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]);
                result[0] = choice;
            });
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            return null;
        }
        if (result[0] < 0 || result[0] >= zombies.size()) return null;
        return zombies.get(result[0]);
    }

    public Item chooseHealItem(Inventory inventory) {
        List<Item> healingItems = new ArrayList<>();
        for (Item item : inventory.getItems()) {
            if ("heal".equals(item.getType()) && item.getQuantity() > 0) {
                healingItems.add(item);
            }
        }
        if (healingItems.isEmpty()) return null;

        String[] options = new String[healingItems.size() + 1];
        for (int i = 0; i < healingItems.size(); i++) {
            Item item = healingItems.get(i);
            options[i] = item.getName() + " x" + item.getQuantity() + " (" + item.getEffectValue() + " HP)";
        }
        options[healingItems.size()] = "Cancel";

        final int[] choice = {-1};
        try {
            SwingUtilities.invokeAndWait(() -> {
                int selected = JOptionPane.showOptionDialog(frame,
                        "Choose a healing item:",
                        "Heal Item Selection",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]);
                choice[0] = selected;
            });
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            return null;
        }

        if (choice[0] < 0 || choice[0] >= healingItems.size()) {
            return null;
        }
        return healingItems.get(choice[0]);
    }

    public void setBattleZombies(List<Zombie> zombies) {
        currentZombies = new ArrayList<>(zombies);
        refreshEnemyStatus();
    }

    public void endBattle() {
        currentZombies = new ArrayList<>();
        refreshEnemyStatus();
        setStatus("Battle finished.");
    }

    public void flashZombieDamage(String zombieName) {
        SwingUtilities.invokeLater(() -> {
            damagingZombies.add(zombieName);
            if (battlePanel != null) battlePanel.repaint();
            Timer timer = new Timer(600, e -> {
                damagingZombies.remove(zombieName);
                if (battlePanel != null) battlePanel.repaint();
            });
            timer.setRepeats(false);
            timer.start();
        });
    }

    public void flashHeroAttack(String heroName) {
        SwingUtilities.invokeLater(() -> {
            attackingHeroes.add(heroName);
            if (battlePanel != null) battlePanel.repaint();
            Timer timer = new Timer(600, e -> {
                attackingHeroes.remove(heroName);
                if (battlePanel != null) battlePanel.repaint();
            });
            timer.setRepeats(false);
            timer.start();
        });
    }

    public void refreshHeroStatus() {
        List<Character> members = team.getMembers();
        for (int i = 0; i < members.size() && i < heroHpLabels.size(); i++) {
            Character member = members.get(i);
            heroHpLabels.get(i).setText("HP: " + member.getHP());
            heroCdLabels.get(i).setText("Cooldowns: " + member.cooldownString());
            JPanel card = heroCardPanels.get(i);
            if (member == activeActor) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(180, 180, 70), 2),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)));
            } else {
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(70, 70, 70), 1),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)));
            }
        }
    }

    public void refreshEnemyStatus() {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < enemyLabels.size(); i++) {
                if (i < currentZombies.size()) {
                    Zombie z = currentZombies.get(i);
                    enemyLabels.get(i).setText(z.getName() + " — HP: " + z.getHP());
                } else {
                    enemyLabels.get(i).setText("No enemy");
                }
            }
        });
    }

    public void setStatus(String text) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(text));
    }

    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            textArea.append(message + "\n");
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
    }

    private void setActorButtonsEnabled(boolean enabled) {
        for (JButton button : actorButtons) button.setEnabled(enabled);
    }

    private void setActionButtonsEnabled(boolean enabled) {
        for (JButton button : actionButtons) button.setEnabled(enabled);
    }

    private void enqueueCommand(GuiCommand command) {
        commandQueue.offer(command);
    }

    private GuiCommand waitForCommand(GuiCommand.Type type) {
        while (true) {
            try {
                GuiCommand command = commandQueue.take();
                if (command.type == type) {
                    return command;
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void sendInput() {
        try {
            String s = inputField.getText();
            if (s == null) s = "";
            byte[] data = (s + "\n").getBytes(StandardCharsets.UTF_8);
            pipedOut.write(data);
            pipedOut.flush();
            inputField.setText("");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private class BattlePanel extends JPanel {
        BattlePanel() {
            setPreferredSize(new Dimension(640, 400));
            setBackground(new Color(65, 106, 64));
            setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 3));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            boolean isBoss = currentZombies.stream().anyMatch(z -> "Dr. RICO".equals(z.getName()));
            String bgKey = isBoss ? "bg_boss" : "bg_normal";
            BufferedImage bg = resources.getStaticImage(bgKey);
            if (bg != null) {
                g2.drawImage(bg, 0, 0, w, h, BattlePanel.this);
            } else {
                g2.setPaint(new GradientPaint(0, 0, new Color(96, 130, 97), 0, h, new Color(72, 95, 72)));
                g2.fillRect(0, 0, w, h);
            }

            drawCharacterSprite(g2, getHeroSprite("mah", "Mah"), 120, 260, "Mah");
            drawCharacterSprite(g2, getHeroSprite("rey", "Rey"), 250, 240, "Rey");
            drawCharacterSprite(g2, getHeroSprite("tess", "Tess"), 380, 260, "Tess");

            int[][] enemyPositions = {{590, 180}, {560, 300}, {680, 320}};
            List<Zombie> zombies = new ArrayList<>(currentZombies);
            if (zombies.isEmpty()) {
                for (int[] pos : enemyPositions) {
                    drawCharacterSprite(g2, resources.getAnimatedImage("zombie_idle"), pos[0], pos[1], "?");
                }
            } else {
                for (int i = 0; i < Math.min(zombies.size(), enemyPositions.length); i++) {
                    Zombie z = zombies.get(i);
                    drawCharacterSprite(g2, getZombieSprite(z.getName()), enemyPositions[i][0], enemyPositions[i][1], z.getName());
                }
            }

            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillRoundRect(20, 20, 260, 42, 18, 18);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
            g2.drawString("Abandoned Facility - Battle Zone", 30, 44);
        }

        private Image getHeroSprite(String key, String displayName) {
            if (attackingHeroes.contains(displayName)) {
                Image atk = resources.getAnimatedImage(key + "_atk");
                if (atk != null) return atk;
            }
            Image idle = resources.getAnimatedImage(key + "_idle");
            return idle != null ? idle : resources.getStaticImage(key);
        }

        private Image getZombieSprite(String name) {
            if ("Dr. RICO".equals(name)) {
                if (damagingZombies.contains(name)) {
                    Image dmg = resources.getAnimatedImage("dr_rico_takingdmg");
                    if (dmg != null) return dmg;
                }
                Image idle = resources.getAnimatedImage("dr_rico_idle");
                return idle != null ? idle : resources.getStaticImage("dr_rico");
            }
            if (damagingZombies.contains(name)) {
                Image dmg = resources.getAnimatedImage("zombie_takingdmg");
                if (dmg != null) return dmg;
            }
            Image idle = resources.getAnimatedImage("zombie_idle");
            return idle != null ? idle : resources.getStaticImage("enemy");
        }

        private void drawCharacterSprite(Graphics2D g2, Image sprite, int x, int y, String name) {
            if (sprite == null) sprite = resources.createPlaceholder(name);
            g2.drawImage(sprite, x - 36, y - 36, 72, 72, BattlePanel.this);
            g2.setColor(new Color(0, 0, 0, 140));
            g2.fillRoundRect(x - 40, y + 40, 80, 18, 10, 10);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            String label = name.length() > 10 ? name.substring(0, 10) : name;
            g2.drawString(label, x - g2.getFontMetrics().stringWidth(label) / 2, y + 54);
        }
    }
}
