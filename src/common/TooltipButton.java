import net.minecraft.src.GuiButton;

public class TooltipButton extends GuiButton implements IHasTooltip {
    public String tooltip = "";

    public TooltipButton(int id, int x, int y, int w, int h, String label) {
        super(id, x, y, w, h, label);
    }

    public String getActiveTooltip(int mouse_x, int mouse_y) {
        if (mouse_x < xPosition || mouse_x >= xPosition + width) {
            // Left/right of button.
            return null;
        }

        if (mouse_y < yPosition || mouse_y >= yPosition + height) {
            // Above/below button.
            return null;
        }

        // On button.
        return tooltip;
    }
}
