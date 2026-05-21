import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ResourceLoader {

    private static final String[] SEARCH_PATHS = {
        "resources/character/charImage/",
        "resources/character/",
        "resource/character/charImage/",
        "resource/character/",
        ""
    };

    private final Map<String, BufferedImage> staticImages = new HashMap<>();
    private final Map<String, Image> animatedImages = new HashMap<>();

    public ResourceLoader() {
        staticImages.put("mah",      loadStatic("mahh_east.png"));
        staticImages.put("rey",      loadStatic("Reyy_east.png", "rey_east.png"));
        staticImages.put("tess",     loadStatic("tess_east.png"));
        staticImages.put("enemy",    loadStatic("zombie_west.png", "zombie_blue.png"));
        staticImages.put("dr_rico",  loadStatic("Dr.Rico_west.png"));
        staticImages.put("bg_normal", loadStatic("background_battle_normal.png"));
        staticImages.put("bg_boss",   loadStatic("battle_background_boss.png"));

        animatedImages.put("zombie_idle",       loadAnimated("Zombie_Idle_animation.gif"));
        animatedImages.put("zombie_takingdmg",  loadAnimated("Zombie_TakingDmg_animation.gif"));
        animatedImages.put("dr_rico_idle",      loadAnimated("Dr.Rico_Idle_animation.gif"));
        animatedImages.put("dr_rico_takingdmg", loadAnimated("Dr.Rico_TakingDmg_animation.gif"));
        animatedImages.put("mah_idle",          loadAnimated("mahh_Idle_animation.gif"));
        animatedImages.put("mah_atk",           loadAnimated("mahh_Atk_animation.gif"));
        animatedImages.put("tess_idle",          loadAnimated("tess_Idle_animation.gif"));
        animatedImages.put("tess_atk",           loadAnimated("tess_Atk_animation.gif"));
        animatedImages.put("rey_idle",          loadAnimated("Reyy_Idle_animation.gif"));
        animatedImages.put("rey_atk",           loadAnimated("Reyy_Atk_animation.gif"));
    }

    public BufferedImage getStaticImage(String key) {
        return staticImages.get(key);
    }

    public Image getAnimatedImage(String key) {
        return animatedImages.get(key);
    }

    public BufferedImage createPlaceholder(String name) {
        int size = 96;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setColor(new Color(78, 95, 112));
        g2.fillRoundRect(0, 0, size, size, 18, 18);
        g2.setColor(new Color(220, 220, 220));
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();
        String label = name.substring(0, Math.min(name.length(), 3)).toUpperCase();
        int width = fm.stringWidth(label);
        g2.drawString(label, (size - width) / 2, (size + fm.getAscent()) / 2 - 4);
        g2.dispose();
        return image;
    }

    public Image scaleImage(BufferedImage image, int width, int height) {
        if (image == null) image = createPlaceholder("?");
        return image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }

    private BufferedImage loadStatic(String... fileNames) {
        if (fileNames == null) return null;
        for (String fileName : fileNames) {
            if (fileName == null) continue;
            for (String path : SEARCH_PATHS) {
                File file = new File(path + fileName);
                if (file.exists()) {
                    try { return ImageIO.read(file); } catch (IOException ignored) {}
                }
            }
            try {
                java.net.URL url = ResourceLoader.class.getResource("/character/charImage/" + fileName);
                if (url == null) url = ResourceLoader.class.getResource("/character/" + fileName);
                if (url != null) return ImageIO.read(url);
            } catch (IOException ignored) {}
        }
        return null;
    }

    private Image loadAnimated(String fileName) {
        for (String path : SEARCH_PATHS) {
            File file = new File(path + fileName);
            if (file.exists()) return new ImageIcon(file.getAbsolutePath()).getImage();
        }
        try {
            java.net.URL url = ResourceLoader.class.getResource("/character/charImage/" + fileName);
            if (url == null) url = ResourceLoader.class.getResource("/character/" + fileName);
            if (url != null) return new ImageIcon(url).getImage();
        } catch (Exception ignored) {}
        return null;
    }
}
