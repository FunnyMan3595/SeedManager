public interface IHasTooltip {
    // Returns null if tooltip isn't active.
    public String getActiveTooltip(int mouse_x, int mouse_y);
}
