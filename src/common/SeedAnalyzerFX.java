import net.minecraft.client.Minecraft;
import net.minecraft.src.RenderEngine;
import net.minecraft.src.TextureFX;
import net.minecraft.src.TexturePackBase;
import java.util.Random;
import java.io.InputStream;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import org.lwjgl.opengl.GL11;

public class SeedAnalyzerFX extends TextureFX {
    public long tickCount;
    public TexturePackBase loadedPack = null;
    public static Random random = new Random();

    // Are we currently in need of updating the image, even if it otherwise
    // shouldn't change this tick?
    public boolean needRender = true;

    // How long to wait between frames, in Minecraft ticks (20/s).
    public static final int DELAY = 5;

    // How many frames there are.
    public static final int FRAMES = 6;

    // How many variants of the animation sequence there are.
    public static final int VARIANTS = 4;

    // Which variant are we showing?
    int variant = 0;

    // Which texture does this override?
    public static final int TARGET_TEXTURE = 2;

    public byte[][][] spriteSheet;

    public SeedAnalyzerFX() {
        super(TARGET_TEXTURE);

        tickCount = (new Random()).nextInt(DELAY * FRAMES * VARIANTS);
    }

    public boolean checkTexturePack() {
        TexturePackBase tp = null;
        try {
            tp = Minecraft.getMinecraft().renderEngine.texturePack.getSelectedTexturePack();
        } catch (Exception e) {}

        if (tp != null && tp != loadedPack) {
            InputStream imageStream = tp.getResourceAsStream("/fm_seedmanager_anim.png");
            BufferedImage rawSpriteSheet = null;
            try {
                rawSpriteSheet = ImageIO.read(imageStream);
            } catch (Exception e) {
                return loadedPack != null;
            }

            int sprite_width = rawSpriteSheet.getWidth() / 16;
            int sprite_height = rawSpriteSheet.getHeight() / 16;

            // Initialize and populate the spriteSheet from the source image.
            spriteSheet = new byte[VARIANTS][FRAMES][sprite_width * sprite_height * 4];
            for (int variant = 0; variant < VARIANTS; variant++) {
                int y_off = variant * sprite_height;
                for (int frame = 0; frame < FRAMES; frame++) {
                    int x_off = frame * sprite_width;
                    for (int x=0; x < sprite_width; x++) {
                        for (int y=0; y < sprite_height; y++) {
                            int pixel = rawSpriteSheet.getRGB(x+x_off, y+y_off);
                            // 0xAARRGGBB -> R,G,B,A
                            int index = (y * sprite_width + x) * 4;
                            spriteSheet[variant][frame][index+3] = (byte) ((pixel>>24)&255);
                            spriteSheet[variant][frame][index+0] = (byte) ((pixel>>16)&255);
                            spriteSheet[variant][frame][index+1] = (byte) ((pixel>>8)&255);
                            spriteSheet[variant][frame][index+2] = (byte) ((pixel)&255);
                        }
                    }
                }
            }

            needRender = true;
            loadedPack = tp;
        }

        return loadedPack != null;
    }

    public void onTick() {
        if (!checkTexturePack()) {
            return;
        }

        tickCount += 1;

        if (tickCount < 0) {
            tickCount = 0;
        }

        if (needRender || tickCount % DELAY == 0) {
            int phase = (int) (tickCount / DELAY);

            // Once every DELAY ticks, we switch frames.
            int frame = phase % FRAMES;

            // Once every DELAY*FRAMES ticks, we pick a new variant.
            if (needRender || phase % FRAMES == 0) {
                variant = random.nextInt(VARIANTS);
            }

            imageData = spriteSheet[variant][frame];

            needRender = false;
        }
    }

    // This lets us write to the correct texture.
    public void bindImage(RenderEngine engine)
    {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, engine.getTexture("/fm_seedmanager.png"));
    }
}
