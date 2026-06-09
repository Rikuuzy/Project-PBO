import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class GameGUI extends JFrame implements BattleSystem.BattleListener {
    private CardLayout cardLayout;
    private JPanel mainContainer;

    // Screens
    private JPanel heroScreen;
    private JPanel enemyScreen;
    private JLayeredPane battleScreen;

    // Selection Data
    private Job selectedJob;
    private String selectedHeroName;
    private String selectedHeroIcon;
    private Enemy selectedEnemyTemplate;
    private double difficultyMul = 1.0;

    // GUI Elements for Hero Screen
    private List<JPanel> heroCardsList = new ArrayList<>();
    private List<JButton> diffButtonsList = new ArrayList<>();
    private JButton nextToEnemyBtn;

    // GUI Elements for Enemy Screen
    private List<JPanel> enemyCardsList = new ArrayList<>();
    private JButton startBattleBtn;

    // GUI Elements for Battle Arena Screen
    private JPanel arenaPanel;
    private JLabel heroIconLbl, heroNameLbl, heroClassLbl, heroHpLbl, heroMpLbl;
    private JLabel enemyIconLbl, enemyNameLbl, enemyClassLbl, enemyHpLbl;
    private JProgressBar heroHpBar, heroMpBar, enemyHpBar;
    private JPanel heroStatusPanel, enemyStatusPanel;
    private JLabel turnIndicatorLbl;
    
    private JButton btnAttack, btnSkill, btnItem, btnDefend;
    private JTextPane logTextPane;
    private StringBuilder logBuffer = new StringBuilder();

    // Modal Overlays (JLayeredPane layers)
    private JPanel modalOverlayPanel;
    private JPanel skillModal;
    private JPanel itemModal;
    private JPanel resultOverlay;

    // Game Core State
    private Hero activeHero;
    private Enemy activeEnemy;
    private BattleSystem battleSystem;

    // Curated Palette Color
    private static final Color BG_DARK = new Color(0x1a, 0x1a, 0x2e);
    private static final Color BG_CARD = new Color(0x24, 0x24, 0x3e);
    private static final Color BG_MODAL = new Color(13, 13, 26, 230); // semi-transparent
    private static final Color COLOR_GOLD = new Color(0xe0, 0xc9, 0x7a);
    private static final Color COLOR_TEXT = Color.WHITE;
    private static final Color COLOR_TEXT_MUTED = new Color(160, 160, 180);
    private static final Color COLOR_PURPLE = new Color(0x7c, 0x3a, 0xed);
    private static final Color COLOR_RED = new Color(0xdc, 0x26, 0x26);
    
    private static final Color HP_GREEN = new Color(0x4a, 0xde, 0x80);
    private static final Color MP_BLUE = new Color(0x60, 0xa5, 0xfa);

    public GameGUI() {
        super("RPG Battle System - OOP Java Plain");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 720);
        setLocationRelativeTo(null);
        setResizable(false);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        mainContainer.setBackground(BG_DARK);

        initHeroScreen();
        initEnemyScreen();
        initBattleScreen();

        mainContainer.add(heroScreen, "heroScreen");
        mainContainer.add(enemyScreen, "enemyScreen");
        mainContainer.add(battleScreen, "battleScreen");

        add(mainContainer);
        cardLayout.show(mainContainer, "heroScreen");
    }

    // Helper: Buat tombol dengan style premium
    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(bg.brighter(), 1, true));
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(bg.brighter());
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    // Screen 1: Pemilihan Hero
    private void initHeroScreen() {
        heroScreen = new JPanel(new BorderLayout());
        heroScreen.setBackground(BG_DARK);
        heroScreen.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        headerPanel.setOpaque(false);
        JLabel titleLbl = new JLabel("⚔ RPG Battle System", JLabel.CENTER);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLbl.setForeground(COLOR_GOLD);
        JLabel subtitleLbl = new JLabel("Pilih hero dan mulai petualanganmu", JLabel.CENTER);
        subtitleLbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLbl.setForeground(COLOR_TEXT_MUTED);
        headerPanel.add(titleLbl);
        headerPanel.add(subtitleLbl);
        heroScreen.add(headerPanel, BorderLayout.NORTH);

        // Center Panel: Hero Cards & Difficulty
        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(25, 0, 25, 0));

        // Hero Cards Container
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsPanel.setOpaque(false);

        // Arin - Warrior
        JPanel warriorCard = createHeroCard("Arin", "Warrior", "⚔️", 120, 22, 14, Job.createWarrior());
        // Lyra - Mage
        JPanel mageCard = createHeroCard("Lyra", "Mage", "🔮", 80, 30, 6, Job.createMage());
        // Zhen - Rogue
        JPanel rogueCard = createHeroCard("Zhen", "Rogue", "🗡️", 95, 26, 9, Job.createRogue());

        cardsPanel.add(warriorCard);
        cardsPanel.add(mageCard);
        cardsPanel.add(rogueCard);
        centerPanel.add(cardsPanel, BorderLayout.CENTER);

        // Difficulty Panel
        JPanel diffPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        diffPanel.setOpaque(false);
        JLabel diffLbl = new JLabel("Tingkat kesulitan: ");
        diffLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        diffLbl.setForeground(COLOR_TEXT_MUTED);
        diffPanel.add(diffLbl);

        String[][] difficulties = {
            {"easy", "Mudah (0.7x)"},
            {"normal", "Normal (1.0x)"},
            {"hard", "Keras (1.4x)"}
        };

        for (String[] d : difficulties) {
            JButton dBtn = createStyledButton(d[1], BG_CARD, COLOR_TEXT_MUTED);
            if (d[0].equals("normal")) {
                dBtn.setBorder(new LineBorder(COLOR_PURPLE, 2, true));
                dBtn.setForeground(COLOR_PURPLE);
            }
            dBtn.addActionListener(e -> {
                if (d[0].equals("easy")) difficultyMul = 0.7;
                else if (d[0].equals("normal")) difficultyMul = 1.0;
                else if (d[0].equals("hard")) difficultyMul = 1.4;

                for (JButton btn : diffButtonsList) {
                    btn.setBorder(new LineBorder(BG_CARD.brighter(), 1, true));
                    btn.setForeground(COLOR_TEXT_MUTED);
                }
                dBtn.setBorder(new LineBorder(COLOR_PURPLE, 2, true));
                dBtn.setForeground(COLOR_PURPLE);
            });
            diffButtonsList.add(dBtn);
            diffPanel.add(dBtn);
        }
        centerPanel.add(diffPanel, BorderLayout.SOUTH);
        heroScreen.add(centerPanel, BorderLayout.CENTER);

        // Footer
        nextToEnemyBtn = createStyledButton("Pilih Musuh →", COLOR_PURPLE, COLOR_TEXT);
        nextToEnemyBtn.setPreferredSize(new Dimension(0, 50));
        nextToEnemyBtn.setEnabled(false);
        nextToEnemyBtn.addActionListener(e -> cardLayout.show(mainContainer, "enemyScreen"));
        heroScreen.add(nextToEnemyBtn, BorderLayout.SOUTH);
    }

    private JPanel createHeroCard(String name, String jobClass, String icon, int hp, int atk, int def, Job jobObj) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BG_CARD.brighter(), 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 42));
        iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        nameLbl.setForeground(COLOR_TEXT);
        nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel classLbl = new JLabel(jobClass);
        classLbl.setFont(new Font("SansSerif", Font.ITALIC, 12));
        classLbl.setForeground(COLOR_TEXT_MUTED);
        classLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Mini Stats Panel
        JPanel statsPanel = new JPanel(new GridLayout(3, 1, 0, 5));
        statsPanel.setOpaque(false);
        statsPanel.setMaximumSize(new Dimension(200, 70));
        statsPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        statsPanel.add(createMiniStatBar("HP", hp, 120, HP_GREEN));
        statsPanel.add(createMiniStatBar("ATK", atk, 40, COLOR_RED));
        statsPanel.add(createMiniStatBar("DEF", def, 20, MP_BLUE));

        card.add(Box.createVerticalStrut(5));
        card.add(iconLbl);
        card.add(Box.createVerticalStrut(8));
        card.add(nameLbl);
        card.add(classLbl);
        card.add(Box.createVerticalGlue());
        card.add(statsPanel);

        heroCardsList.add(card);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedJob = jobObj;
                selectedHeroName = name;
                selectedHeroIcon = icon;

                for (JPanel c : heroCardsList) {
                    c.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(BG_CARD.brighter(), 1, true),
                        new EmptyBorder(15, 15, 15, 15)
                    ));
                }
                card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(COLOR_PURPLE, 2, true),
                    new EmptyBorder(14, 14, 14, 14)
                ));
                nextToEnemyBtn.setEnabled(true);
            }
        });

        return card;
    }

    private JPanel createMiniStatBar(String label, int val, int max, Color color) {
        JPanel p = new JPanel(new BorderLayout(5, 0));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label + " (" + val + ")");
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
        lbl.setForeground(COLOR_TEXT_MUTED);
        p.add(lbl, BorderLayout.NORTH);

        JProgressBar bar = new JProgressBar(0, max);
        bar.setValue(val);
        bar.setForeground(color);
        bar.setBackground(new Color(30, 30, 50));
        bar.setBorderPainted(false);
        bar.setPreferredSize(new Dimension(100, 5));
        p.add(bar, BorderLayout.CENTER);
        return p;
    }

    // Screen 2: Pemilihan Musuh
    private void initEnemyScreen() {
        enemyScreen = new JPanel(new BorderLayout());
        enemyScreen.setBackground(BG_DARK);
        enemyScreen.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        headerPanel.setOpaque(false);
        JLabel titleLbl = new JLabel("👾 Pilih Musuh", JLabel.CENTER);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLbl.setForeground(COLOR_GOLD);
        JLabel subtitleLbl = new JLabel("Siapa yang akan kamu hadapi?", JLabel.CENTER);
        subtitleLbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLbl.setForeground(COLOR_TEXT_MUTED);
        headerPanel.add(titleLbl);
        headerPanel.add(subtitleLbl);
        enemyScreen.add(headerPanel, BorderLayout.NORTH);

        // Center Panel: Grid Musuh
        JPanel centerPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(25, 0, 25, 0));

        // Define Enemies
        Enemy goblin = new Enemy("Goblin King", 80, 30, 14, 5, 6, "👺", "Mudah");
        goblin.addSkill(new DamageSkill("Goblin Rush", "Serangan belati cepat bertubi-tubi", 8, 18, "atk", false));
        goblin.addSkill(new DamageSkill("Taunt", "Mengejek lawan dengan tawa menjengkelkan", 5, 5, "atk", false));

        Enemy orc = new Enemy("Orc Berserker", 140, 20, 24, 10, 5, "🧟", "Sedang");
        orc.addSkill(new DamageSkill("Rage", "Serangan kapak brutal yang mengabaikan DEF", 10, 35, "atk", false));
        orc.addSkill(new StatusSkill("Slam", "Menghempas bumi dan memberikan stun pada musuh", 8, 25, "stun", StatusEffect.Type.STUN, 1, false));

        Enemy dragon = new Enemy("Dark Dragon", 200, 80, 32, 16, 10, "🐉", "Sulit");
        dragon.addSkill(new StatusSkill("Dragon Breath", "Semburan api naga yang membakar musuh selama 2 ronde", 20, 55, "burn", StatusEffect.Type.BURN, 2, false));
        dragon.addSkill(new DamageSkill("Tail Sweep", "Sapuan ekor naga raksasa yang menyapu lawan", 12, 38, "atk", false));

        Enemy lich = new Enemy("Lich Lord", 110, 120, 28, 8, 11, "💀", "Sulit");
        lich.addSkill(new DamageSkill("Dark Bolt", "Tembakan sihir hitam korosif", 15, 45, "atk", false));
        lich.addSkill(new StatusSkill("Soul Drain", "Menyerap 15 HP musuh untuk memulihkan diri sendiri", 20, 30, "execute", StatusEffect.Type.STUN, 0, false));

        centerPanel.add(createEnemyCard(goblin));
        centerPanel.add(createEnemyCard(orc));
        centerPanel.add(createEnemyCard(dragon));
        centerPanel.add(createEnemyCard(lich));

        enemyScreen.add(centerPanel, BorderLayout.CENTER);

        // Footer Navigation
        JPanel footerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        footerPanel.setOpaque(false);
        footerPanel.setPreferredSize(new Dimension(0, 50));

        JButton backBtn = createStyledButton("← Kembali", BG_CARD, COLOR_TEXT);
        backBtn.addActionListener(e -> cardLayout.show(mainContainer, "heroScreen"));

        startBattleBtn = createStyledButton("Mulai Battle! ⚔", COLOR_RED, COLOR_TEXT);
        startBattleBtn.setEnabled(false);
        startBattleBtn.addActionListener(e -> {
            setupAndStartBattle();
            cardLayout.show(mainContainer, "battleScreen");
        });

        footerPanel.add(backBtn);
        footerPanel.add(startBattleBtn);
        enemyScreen.add(footerPanel, BorderLayout.SOUTH);
    }

    private JPanel createEnemyCard(Enemy enemy) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BG_CARD.brighter(), 1, true),
            new EmptyBorder(15, 20, 15, 20)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel iconLbl = new JLabel(enemy.getIcon());
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel nameLbl = new JLabel(enemy.getName());
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 15));
        nameLbl.setForeground(COLOR_TEXT);

        JLabel typeLbl = new JLabel("HP: " + enemy.getFullHp() + " | ATK: " + enemy.getAttack() + " | DEF: " + enemy.getDefence());
        typeLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        typeLbl.setForeground(COLOR_TEXT_MUTED);

        JLabel diffLbl = new JLabel("● " + enemy.getDifficulty());
        diffLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        if (enemy.getDifficulty().equals("Mudah")) diffLbl.setForeground(HP_GREEN);
        else if (enemy.getDifficulty().equals("Sedang")) diffLbl.setForeground(COLOR_GOLD);
        else diffLbl.setForeground(COLOR_RED);

        textPanel.add(nameLbl);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(typeLbl);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(diffLbl);

        card.add(iconLbl, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        enemyCardsList.add(card);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedEnemyTemplate = enemy;

                for (JPanel c : enemyCardsList) {
                    c.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(BG_CARD.brighter(), 1, true),
                        new EmptyBorder(15, 20, 15, 20)
                    ));
                }
                card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(COLOR_RED, 2, true),
                    new EmptyBorder(14, 19, 14, 19)
                ));
                startBattleBtn.setEnabled(true);
            }
        });

        return card;
    }

    // Screen 3: Battle Arena Screen
    private void initBattleScreen() {
        battleScreen = new JLayeredPane();
        battleScreen.setBackground(BG_DARK);
        battleScreen.setOpaque(true);

        // Main Arena Panel (Layer 1)
        arenaPanel = new JPanel(new BorderLayout());
        arenaPanel.setBackground(BG_DARK);
        arenaPanel.setBounds(0, 0, 940, 680);
        arenaPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Arena Header: Title
        JLabel titleLbl = new JLabel("⚔ Battle Arena", JLabel.CENTER);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLbl.setForeground(COLOR_GOLD);
        arenaPanel.add(titleLbl, BorderLayout.NORTH);

        // Arena Center: VS Panel & Action Logs
        JPanel centerGrid = new JPanel();
        centerGrid.setLayout(new BoxLayout(centerGrid, BoxLayout.Y_AXIS));
        centerGrid.setOpaque(false);
        centerGrid.setBorder(new EmptyBorder(15, 0, 0, 0));

        // 1. Fighters Row (Hero, VS, Enemy)
        JPanel fightersPanel = new JPanel(new GridBagLayout());
        fightersPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Hero Card Panel
        JPanel heroCardPanel = createFighterCardPanel(true);
        gbc.gridx = 0;
        gbc.weightx = 0.45;
        fightersPanel.add(heroCardPanel, gbc);

        // VS Label
        JLabel vsLbl = new JLabel("VS", JLabel.CENTER);
        vsLbl.setFont(new Font("SansSerif", Font.BOLD, 22));
        vsLbl.setForeground(COLOR_GOLD);
        gbc.gridx = 1;
        gbc.weightx = 0.1;
        fightersPanel.add(vsLbl, gbc);

        // Enemy Card Panel
        JPanel enemyCardPanel = createFighterCardPanel(false);
        gbc.gridx = 2;
        gbc.weightx = 0.45;
        fightersPanel.add(enemyCardPanel, gbc);

        centerGrid.add(fightersPanel);
        centerGrid.add(Box.createVerticalStrut(10));

        // 2. Turn Indicator
        turnIndicatorLbl = new JLabel("Giliran: -", JLabel.CENTER);
        turnIndicatorLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        turnIndicatorLbl.setForeground(COLOR_TEXT_MUTED);
        turnIndicatorLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerGrid.add(turnIndicatorLbl);
        centerGrid.add(Box.createVerticalStrut(15));

        // 3. Action Panel
        JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.setBackground(new Color(0xff, 0xff, 0xff, 12));
        actionPanel.setOpaque(true);
        actionPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0xff, 0xff, 0xff, 25), 1, true),
            new EmptyBorder(12, 12, 12, 12)
        ));
        
        JLabel actTitle = new JLabel("AKSI BATTLE");
        actTitle.setFont(new Font("SansSerif", Font.BOLD, 10));
        actTitle.setForeground(COLOR_TEXT_MUTED);
        actionPanel.add(actTitle, BorderLayout.NORTH);

        JPanel actionGrid = new JPanel(new GridLayout(1, 4, 10, 0));
        actionGrid.setOpaque(false);
        actionGrid.setBorder(new EmptyBorder(8, 0, 0, 0));

        btnAttack = createStyledButton("<html><center>⚔️<br><b>Serang</b><br><font size='2' color='#f87171'>Fisik</font></center></html>", BG_CARD, COLOR_TEXT);
        btnAttack.addActionListener(e -> battleSystem.executeHeroAttack());

        btnSkill = createStyledButton("<html><center>🔮<br><b>Skill</b><br><font size='2' id='skill-mp-label' color='#60a5fa'>- MP</font></center></html>", BG_CARD, COLOR_TEXT);
        btnSkill.addActionListener(e -> openSkillModal());

        btnItem = createStyledButton("<html><center>🎒<br><b>Item</b><br><font size='2' color='#4ade80'>3 Item</font></center></html>", BG_CARD, COLOR_TEXT);
        btnItem.addActionListener(e -> openItemModal());

        btnDefend = createStyledButton("<html><center>🛡️<br><b>Bertahan</b><br><font size='2' color='#60a5fa'>+DEF</font></center></html>", BG_CARD, COLOR_TEXT);
        btnDefend.addActionListener(e -> battleSystem.executeHeroDefend());

        actionGrid.add(btnAttack);
        actionGrid.add(btnSkill);
        actionGrid.add(btnItem);
        actionGrid.add(btnDefend);

        actionPanel.add(actionGrid, BorderLayout.CENTER);
        centerGrid.add(actionPanel);
        centerGrid.add(Box.createVerticalStrut(15));

        // 4. Log Panel
        logTextPane = new JTextPane();
        logTextPane.setContentType("text/html");
        logTextPane.setEditable(false);
        logTextPane.setBackground(new Color(10, 10, 20));
        logTextPane.setForeground(COLOR_TEXT);
        logTextPane.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane logScroll = new JScrollPane(logTextPane);
        logScroll.setBackground(new Color(10, 10, 20));
        logScroll.setBorder(new LineBorder(new Color(0xff, 0xff, 0xff, 12), 1, true));
        logScroll.setPreferredSize(new Dimension(0, 110));
        logScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        centerGrid.add(logScroll);
        arenaPanel.add(centerGrid, BorderLayout.CENTER);

        // Add Arena Panel to LayeredPane
        battleScreen.add(arenaPanel, JLayeredPane.DEFAULT_LAYER);

        // Init Modals
        initModalOverlays();
    }

    private JPanel createFighterCardPanel(boolean isHero) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(255, 255, 255, 18));
        panel.setOpaque(true);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(255, 255, 255, 30), 1, true),
            new EmptyBorder(12, 15, 12, 15)
        ));

        JLabel icon = new JLabel(isHero ? "⚔️" : "👾");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel name = new JLabel(isHero ? "Hero" : "Musuh");
        name.setFont(new Font("SansSerif", Font.BOLD, 14));
        name.setForeground(COLOR_TEXT);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel cls = new JLabel(isHero ? "Class" : "Lvl");
        cls.setFont(new Font("SansSerif", Font.PLAIN, 11));
        cls.setForeground(COLOR_TEXT_MUTED);
        cls.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Progress HP
        JPanel hpRow = new JPanel(new BorderLayout(5, 0));
        hpRow.setOpaque(false);
        hpRow.setMaximumSize(new Dimension(300, 20));
        JLabel hpLblText = new JLabel("HP");
        hpLblText.setFont(new Font("SansSerif", Font.PLAIN, 10));
        hpLblText.setForeground(COLOR_TEXT_MUTED);
        hpRow.add(hpLblText, BorderLayout.WEST);

        JProgressBar hpBar = new JProgressBar(0, 100);
        hpBar.setValue(100);
        hpBar.setForeground(HP_GREEN);
        hpBar.setBackground(new Color(50, 50, 70));
        hpBar.setBorderPainted(false);
        hpBar.setStringPainted(true);
        hpBar.setFont(new Font("SansSerif", Font.BOLD, 9));
        hpBar.setForeground(Color.BLACK);
        hpRow.add(hpBar, BorderLayout.CENTER);

        JLabel hpValLbl = new JLabel("100/100");
        hpValLbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
        hpValLbl.setForeground(COLOR_TEXT_MUTED);
        hpRow.add(hpValLbl, BorderLayout.EAST);

        // Progress MP (Only for Hero)
        JPanel mpRow = null;
        JProgressBar mpBar = null;
        JLabel mpValLbl = null;
        if (isHero) {
            mpRow = new JPanel(new BorderLayout(5, 0));
            mpRow.setOpaque(false);
            mpRow.setMaximumSize(new Dimension(300, 20));
            JLabel mpLblText = new JLabel("MP");
            mpLblText.setFont(new Font("SansSerif", Font.PLAIN, 10));
            mpLblText.setForeground(COLOR_TEXT_MUTED);
            mpRow.add(mpLblText, BorderLayout.WEST);

            mpBar = new JProgressBar(0, 100);
            mpBar.setValue(100);
            mpBar.setForeground(MP_BLUE);
            mpBar.setBackground(new Color(50, 50, 70));
            mpBar.setBorderPainted(false);
            mpBar.setStringPainted(true);
            mpBar.setFont(new Font("SansSerif", Font.BOLD, 9));
            mpBar.setForeground(Color.BLACK);
            mpRow.add(mpBar, BorderLayout.CENTER);

            mpValLbl = new JLabel("100/100");
            mpValLbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
            mpValLbl.setForeground(COLOR_TEXT_MUTED);
            mpRow.add(mpValLbl, BorderLayout.EAST);
        }

        // Status Effects Panel
        JPanel statusEffectsPanel = new JPanel(new FlowLayout(isHero ? FlowLayout.LEFT : FlowLayout.RIGHT, 4, 0));
        statusEffectsPanel.setOpaque(false);
        statusEffectsPanel.setMaximumSize(new Dimension(300, 25));

        panel.add(icon);
        panel.add(Box.createVerticalStrut(3));
        panel.add(name);
        panel.add(cls);
        panel.add(Box.createVerticalStrut(10));
        panel.add(hpRow);
        if (isHero) {
            panel.add(Box.createVerticalStrut(5));
            panel.add(mpRow);
        }
        panel.add(Box.createVerticalStrut(8));
        panel.add(statusEffectsPanel);

        if (isHero) {
            heroIconLbl = icon;
            heroNameLbl = name;
            heroClassLbl = cls;
            heroHpBar = hpBar;
            heroHpLbl = hpValLbl;
            heroMpBar = mpBar;
            heroMpLbl = mpValLbl;
            heroStatusPanel = statusEffectsPanel;
        } else {
            enemyIconLbl = icon;
            enemyNameLbl = name;
            enemyClassLbl = cls;
            enemyHpBar = hpBar;
            enemyHpLbl = hpValLbl;
            enemyStatusPanel = statusEffectsPanel;
        }

        return panel;
    }

    // Pembuatan Overlay Modal
    private void initModalOverlays() {
        modalOverlayPanel = new JPanel(null);
        modalOverlayPanel.setBackground(BG_MODAL);
        modalOverlayPanel.setBounds(0, 0, 940, 680);
        modalOverlayPanel.setOpaque(true);
        modalOverlayPanel.setVisible(false);

        // 1. Skill Modal Panel
        skillModal = new JPanel(new BorderLayout());
        skillModal.setBackground(BG_DARK);
        skillModal.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COLOR_GOLD, 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));
        skillModal.setBounds(250, 150, 440, 360);

        JLabel skTitle = new JLabel("✦ Pilih Skill", JLabel.LEFT);
        skTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        skTitle.setForeground(COLOR_GOLD);
        skillModal.add(skTitle, BorderLayout.NORTH);

        // 2. Item Modal Panel
        itemModal = new JPanel(new BorderLayout());
        itemModal.setBackground(BG_DARK);
        itemModal.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COLOR_GOLD, 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));
        itemModal.setBounds(250, 150, 440, 360);

        JLabel itemTitle = new JLabel("🎒 Pilih Item", JLabel.LEFT);
        itemTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        itemTitle.setForeground(COLOR_GOLD);
        itemModal.add(itemTitle, BorderLayout.NORTH);

        // 3. Result Overlay
        resultOverlay = new JPanel();
        resultOverlay.setLayout(new BoxLayout(resultOverlay, BoxLayout.Y_AXIS));
        resultOverlay.setBackground(new Color(13, 13, 26, 240));
        resultOverlay.setBounds(0, 0, 940, 680);
        resultOverlay.setBorder(new EmptyBorder(100, 200, 100, 200));
        resultOverlay.setVisible(false);

        battleScreen.add(modalOverlayPanel, JLayeredPane.MODAL_LAYER);
        modalOverlayPanel.add(skillModal);
        modalOverlayPanel.add(itemModal);
        battleScreen.add(resultOverlay, JLayeredPane.POPUP_LAYER);
    }

    private void openSkillModal() {
        skillModal.removeAll();
        
        JLabel skTitle = new JLabel("✦ Pilih Skill", JLabel.LEFT);
        skTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        skTitle.setForeground(COLOR_GOLD);
        skTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        skillModal.add(skTitle, BorderLayout.NORTH);

        JPanel listPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        listPanel.setOpaque(false);

        for (Skill sk : activeHero.getJob().getSkills()) {
            boolean noMp = activeHero.getMp() < sk.getCost();
            String skLabel = "<html><body style='width: 140px;'><b style='color:#ffffff; font-size:12px;'>" + sk.getName() + "</b><br>" +
                             "<font size='2' color='#b0b0d0'>" + sk.getDesc() + "</font><br>" +
                             "<font size='2' color='#60a5fa'>MP: " + sk.getCost() + "</font>" +
                             (sk.getDamage() > 0 ? " <font size='2' color='#f87171'>DMG: ~" + sk.getDamage() + "</font>" : "") +
                             "</body></html>";

            JButton btn = createStyledButton(skLabel, BG_CARD, COLOR_TEXT);
            btn.setEnabled(!noMp);
            if (noMp) {
                btn.setOpaque(true);
                btn.setBackground(new Color(50, 50, 60, 80));
                btn.setBorder(new LineBorder(Color.DARK_GRAY, 1, true));
            }
            btn.addActionListener(e -> {
                closeModal();
                battleSystem.executeHeroSkill(sk);
            });
            listPanel.add(btn);
        }

        skillModal.add(listPanel, BorderLayout.CENTER);

        JButton cancelBtn = createStyledButton("Batal", BG_CARD, COLOR_TEXT_MUTED);
        cancelBtn.setPreferredSize(new Dimension(0, 35));
        cancelBtn.addActionListener(e -> closeModal());
        skillModal.add(cancelBtn, BorderLayout.SOUTH);

        skillModal.setVisible(true);
        itemModal.setVisible(false);
        modalOverlayPanel.setVisible(true);
        battleScreen.revalidate();
        battleScreen.repaint();
    }

    private void openItemModal() {
        itemModal.removeAll();

        JLabel itemTitle = new JLabel("🎒 Pilih Item", JLabel.LEFT);
        itemTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        itemTitle.setForeground(COLOR_GOLD);
        itemTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        itemModal.add(itemTitle, BorderLayout.NORTH);

        JPanel listPanel = new JPanel(new GridLayout(3, 1, 0, 8));
        listPanel.setOpaque(false);

        for (Item it : activeHero.getInventory()) {
            boolean noItem = it.getQuantity() <= 0;
            String itemLabel = "<html><body style='width: 320px;'>" + it.getIcon() + " <b style='color:#ffffff; font-size:12px;'>" + it.getName() + "</b> - <font size='2' color='#b0b0d0'>" + it.getDescription() + "</font><br>" +
                               "<font size='2' color='#a78bfa'>Jumlah Sisa: " + it.getQuantity() + "</font>" +
                               "</body></html>";

            JButton btn = createStyledButton(itemLabel, BG_CARD, COLOR_TEXT);
            btn.setEnabled(!noItem);
            if (noItem) {
                btn.setOpaque(true);
                btn.setBackground(new Color(50, 50, 60, 80));
                btn.setBorder(new LineBorder(Color.DARK_GRAY, 1, true));
            }
            btn.addActionListener(e -> {
                closeModal();
                battleSystem.executeHeroItem(it);
            });
            listPanel.add(btn);
        }

        itemModal.add(listPanel, BorderLayout.CENTER);

        JButton cancelBtn = createStyledButton("Batal", BG_CARD, COLOR_TEXT_MUTED);
        cancelBtn.setPreferredSize(new Dimension(0, 35));
        cancelBtn.addActionListener(e -> closeModal());
        itemModal.add(cancelBtn, BorderLayout.SOUTH);

        skillModal.setVisible(false);
        itemModal.setVisible(true);
        modalOverlayPanel.setVisible(true);
        battleScreen.revalidate();
        battleScreen.repaint();
    }

    private void closeModal() {
        modalOverlayPanel.setVisible(false);
        skillModal.setVisible(false);
        itemModal.setVisible(false);
    }

    // Menyiapkan Data Karakter dan Memulai Sistem Battle
    private void setupAndStartBattle() {
        // Caster kloning hero agar bisa diulang game-nya tanpa mengotori template
        activeHero = new Hero(selectedHeroName, selectedJob, selectedHeroIcon);

        // Kloning musuh template
        activeEnemy = new Enemy(
            selectedEnemyTemplate.getName(),
            selectedEnemyTemplate.getFullHp(),
            selectedEnemyTemplate.getFullMp(),
            selectedEnemyTemplate.getAttack(),
            selectedEnemyTemplate.getDefence(),
            selectedEnemyTemplate.getSpeed(),
            selectedEnemyTemplate.getIcon(),
            selectedEnemyTemplate.getDifficulty()
        );
        for (Skill s : selectedEnemyTemplate.getSkills()) {
            activeEnemy.addSkill(s);
        }

        // Terapkan multiplier tingkat kesulitan pada stat musuh
        int scaledHp = (int) Math.round(activeEnemy.getFullHp() * difficultyMul);
        int scaledAtk = (int) Math.round(activeEnemy.getAttack() * difficultyMul);
        activeEnemy.setHp(scaledHp);
        // workaround to set scaled max HP
        // we can cast or just write character.setHp to scaled and keep it. To update full_hp, since full_hp is private in Character,
        // we can construct Enemy with scaled stats directly, or add scaled values.
        // Let's check how activeEnemy was created:
        activeEnemy = new Enemy(
            selectedEnemyTemplate.getName(),
            scaledHp,
            selectedEnemyTemplate.getFullMp(),
            scaledAtk,
            selectedEnemyTemplate.getDefence(),
            selectedEnemyTemplate.getSpeed(),
            selectedEnemyTemplate.getIcon(),
            selectedEnemyTemplate.getDifficulty()
        );
        for (Skill s : selectedEnemyTemplate.getSkills()) {
            activeEnemy.addSkill(s);
        }

        // Hubungkan GUI ke BattleSystem
        battleSystem = new BattleSystem(activeHero, activeEnemy, difficultyMul);
        battleSystem.addListener(this);

        // Reset Logs
        logBuffer.setLength(0);
        logTextPane.setText("");

        // Setup GUI Labels
        heroIconLbl.setText(activeHero.getIcon());
        heroNameLbl.setText(activeHero.getName());
        heroClassLbl.setText(activeHero.getJob().getName());
        heroHpBar.setMaximum(activeHero.getFullHp());
        heroMpBar.setMaximum(activeHero.getFullMp());

        enemyIconLbl.setText(activeEnemy.getIcon());
        enemyNameLbl.setText(activeEnemy.getName());
        enemyClassLbl.setText("HP: " + activeEnemy.getFullHp() + " | ATK: " + activeEnemy.getAttack());
        enemyHpBar.setMaximum(activeEnemy.getFullHp());

        // Jalankan
        battleSystem.startBattle();
    }

    // Implementasi Interface Listener BattleSystem
    @Override
    public void onLogAdded(String message, String styleClass) {
        String color = "#ffffff";
        if (styleClass.equals("log-attack")) color = "#f87171";
        else if (styleClass.equals("log-skill")) color = "#c084fc";
        else if (styleClass.equals("log-heal")) color = "#4ade80";
        else if (styleClass.equals("log-enemy")) color = "#fb923c";
        else if (styleClass.equals("log-info")) color = "#60a5fa";
        else if (styleClass.equals("log-crit")) color = "#fbbf24";
        
        logBuffer.append("<div style='font-family:sans-serif; font-size:11px; margin-bottom:3px; color:")
                .append(color)
                .append(";'>")
                .append(message)
                .append("</div>");
        
        logTextPane.setText("<html><body style='background-color:#0d0d1a; margin:5px;'>" + logBuffer.toString() + "</body></html>");
        
        // Auto scroll to bottom
        SwingUtilities.invokeLater(() -> {
            logTextPane.setCaretPosition(logTextPane.getDocument().getLength());
        });
    }

    @Override
    public void onBattleUpdated() {
        // Update stats values
        heroHpBar.setValue(activeHero.getHp());
        heroHpBar.setString(activeHero.getHp() + "/" + activeHero.getFullHp());
        heroHpLbl.setText(activeHero.getHp() + "/" + activeHero.getFullHp());

        heroMpBar.setValue(activeHero.getMp());
        heroMpBar.setString(activeHero.getMp() + "/" + activeHero.getFullMp());
        heroMpLbl.setText(activeHero.getMp() + "/" + activeHero.getFullMp());

        enemyHpBar.setValue(activeEnemy.getHp());
        enemyHpBar.setString(activeEnemy.getHp() + "/" + activeEnemy.getFullHp());
        enemyHpLbl.setText(activeEnemy.getHp() + "/" + activeEnemy.getFullHp());

        // Update status effects tags
        updateStatusEffectsTags(heroStatusPanel, activeHero);
        updateStatusEffectsTags(enemyStatusPanel, activeEnemy);

        // Turn indicator
        if (battleSystem.isHeroTurn()) {
            turnIndicatorLbl.setText("Giliran: " + activeHero.getName());
            turnIndicatorLbl.setForeground(COLOR_GOLD);
            enableActionsPanel(true);
        } else {
            turnIndicatorLbl.setText("Giliran: " + activeEnemy.getName());
            turnIndicatorLbl.setForeground(COLOR_RED);
            enableActionsPanel(false);
        }

        // Update MP and Item counts on buttons
        int minMp = 99;
        for (Skill s : activeHero.getJob().getSkills()) {
            if (s.getCost() < minMp) minMp = s.getCost();
        }
        btnSkill.setText("<html><center>🔮<br><b>Skill</b><br><font size='2' color='#60a5fa'>" + minMp + "+ MP</font></center></html>");

        int totalItems = 0;
        for (Item it : activeHero.getInventory()) {
            totalItems += it.getQuantity();
        }
        btnItem.setText("<html><center>🎒<br><b>Item</b><br><font size='2' color='#4ade80'>" + totalItems + " item</font></center></html>");
    }

    private void updateStatusEffectsTags(JPanel statusPanel, Character character) {
        statusPanel.removeAll();
        for (StatusEffect effect : character.getStatusEffects()) {
            JLabel tag = new JLabel(effect.getName());
            tag.setFont(new Font("SansSerif", Font.BOLD, 9));
            tag.setOpaque(true);
            tag.setBorder(new EmptyBorder(2, 6, 2, 6));

            switch (effect.getType()) {
                case POISON:
                    tag.setBackground(new Color(168, 85, 247, 60));
                    tag.setForeground(new Color(0xc0, 0x84, 0xfc));
                    break;
                case BURN:
                    tag.setBackground(new Color(251, 146, 60, 60));
                    tag.setForeground(new Color(0xfb, 0x92, 0x3c));
                    break;
                case EVADE:
                    tag.setBackground(new Color(34, 197, 94, 60));
                    tag.setForeground(new Color(0x4a, 0xde, 0x80));
                    break;
                case STUN:
                    tag.setBackground(new Color(234, 179, 8, 60));
                    tag.setForeground(new Color(0xea, 0xb3, 0x08));
                    break;
                case BUFF:
                    tag.setBackground(new Color(96, 165, 250, 60));
                    tag.setForeground(new Color(0x60, 0xa5, 0xfa));
                    break;
                case BERSERK:
                    tag.setBackground(new Color(239, 68, 68, 60));
                    tag.setForeground(new Color(0xf8, 0x71, 0x71));
                    break;
            }
            statusPanel.add(tag);
        }
        
        // Handle Shield (Defending) as custom tag
        if (character.isDefending()) {
            JLabel tag = new JLabel("Bertahan");
            tag.setFont(new Font("SansSerif", Font.BOLD, 9));
            tag.setOpaque(true);
            tag.setBorder(new EmptyBorder(2, 6, 2, 6));
            tag.setBackground(new Color(96, 165, 250, 60));
            tag.setForeground(new Color(0x60, 0xa5, 0xfa));
            statusPanel.add(tag);
        }

        statusPanel.revalidate();
        statusPanel.repaint();
    }

    private void enableActionsPanel(boolean enabled) {
        btnAttack.setEnabled(enabled);
        btnSkill.setEnabled(enabled);
        btnItem.setEnabled(enabled);
        btnDefend.setEnabled(enabled);
    }

    @Override
    public void onBattleOver(boolean heroWon, int turns, int heroDmg, int enemyDmg, int remainingHp, int maxHp) {
        enableActionsPanel(false);
        closeModal();

        resultOverlay.removeAll();

        JLabel resTitle = new JLabel(heroWon ? "🏆 MENANG!" : "💀 KALAH", JLabel.CENTER);
        resTitle.setFont(new Font("SansSerif", Font.BOLD, 36));
        resTitle.setForeground(heroWon ? HP_GREEN : COLOR_RED);
        resTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel resSub = new JLabel(heroWon ? activeEnemy.getName() + " berhasil dikalahkan!" : activeHero.getName() + " gugur dalam pertempuran...", JLabel.CENTER);
        resSub.setFont(new Font("SansSerif", Font.PLAIN, 15));
        resSub.setForeground(COLOR_TEXT_MUTED);
        resSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Stats Box Panel
        JPanel statsBox = new JPanel(new GridLayout(4, 1, 0, 5));
        statsBox.setBackground(BG_CARD);
        statsBox.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0xff, 0xff, 0xff, 20), 1, true),
            new EmptyBorder(15, 25, 15, 25)
        ));
        statsBox.setMaximumSize(new Dimension(360, 110));
        statsBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        statsBox.add(createStatRow("Total Ronde", turns));
        statsBox.add(createStatRow("Total Damage Diberikan", heroDmg));
        statsBox.add(createStatRow("Damage Diterima", enemyDmg));
        statsBox.add(createStatRow("HP Sisa Hero", remainingHp + "/" + maxHp));

        JButton playAgainBtn = createStyledButton("Main Lagi ↺", COLOR_GOLD, Color.BLACK);
        playAgainBtn.setPreferredSize(new Dimension(200, 45));
        playAgainBtn.setMaximumSize(new Dimension(200, 45));
        playAgainBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        playAgainBtn.addActionListener(e -> {
            resultOverlay.setVisible(false);
            resetGameSelection();
            cardLayout.show(mainContainer, "heroScreen");
        });

        resultOverlay.add(Box.createVerticalGlue());
        resultOverlay.add(resTitle);
        resultOverlay.add(Box.createVerticalStrut(10));
        resultOverlay.add(resSub);
        resultOverlay.add(Box.createVerticalStrut(25));
        resultOverlay.add(statsBox);
        resultOverlay.add(Box.createVerticalStrut(30));
        resultOverlay.add(playAgainBtn);
        resultOverlay.add(Box.createVerticalGlue());

        resultOverlay.setVisible(true);
        battleScreen.revalidate();
        battleScreen.repaint();
    }

    private JPanel createStatRow(String label, Object value) {
        JPanel r = new JPanel(new BorderLayout());
        r.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(COLOR_TEXT_MUTED);
        
        JLabel val = new JLabel(value.toString());
        val.setFont(new Font("SansSerif", Font.BOLD, 12));
        val.setForeground(COLOR_GOLD);

        r.add(lbl, BorderLayout.WEST);
        r.add(val, BorderLayout.EAST);
        return r;
    }

    private void resetGameSelection() {
        selectedJob = null;
        selectedHeroName = null;
        selectedEnemyTemplate = null;
        startBattleBtn.setEnabled(false);
        nextToEnemyBtn.setEnabled(false);

        // Reset Hero borders
        for (JPanel c : heroCardsList) {
            c.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BG_CARD.brighter(), 1, true),
                new EmptyBorder(15, 15, 15, 15)
            ));
        }

        // Reset Enemy borders
        for (JPanel c : enemyCardsList) {
            c.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BG_CARD.brighter(), 1, true),
                new EmptyBorder(15, 20, 15, 20)
            ));
        }
    }
}
