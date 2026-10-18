package com.othman.shapehud;

/**
 * Every vanilla HUD piece ShapeHUD can control, with its default on-screen box
 * (in scaled GUI pixels). The box is the pivot for scaling and the hit area in the editor.
 */
public enum HudElement {
    HOTBAR("Hotbar", Category.CORE, Icon.item("diamond_sword"))                { public Rect box(int w, int h) { return new Rect(w / 2f - 91, h - 22, 182, 22); } },
    XP_BAR("XP / Locator bar", Category.CORE, Icon.item("experience_bottle"))  { public Rect box(int w, int h) { return new Rect(w / 2f - 91, h - 29, 182, 5); } },
    XP_LEVEL("XP level", Category.CORE, Icon.item("enchanting_table"))          { public Rect box(int w, int h) { return new Rect(w / 2f - 15, h - 36, 30, 9); } },
    CROSSHAIR("Crosshair", Category.CORE, Icon.item("target"))                  { public Rect box(int w, int h) { return new Rect(w / 2f - 8, h / 2f - 8, 16, 16); } },
    HEALTH("Health", Category.STATS, Icon.sprite("hud/heart/full"))             { public Rect box(int w, int h) { return new Rect(w / 2f - 91, h - 39, 81, 9); } },
    ARMOR("Armor", Category.STATS, Icon.sprite("hud/armor_full"))               { public Rect box(int w, int h) { return new Rect(w / 2f - 91, h - 49, 81, 9); } },
    FOOD("Hunger", Category.STATS, Icon.sprite("hud/food_full"))                { public Rect box(int w, int h) { return new Rect(w / 2f + 10, h - 39, 81, 9); } },
    CHAT("Chat", Category.TEXT, Icon.item("writable_book"))                     { public Rect box(int w, int h) { return new Rect(0, h - 130, 324, 92); } },
    ACTION_BAR("Action bar", Category.TEXT, Icon.item("oak_sign"))              { public Rect box(int w, int h) { return new Rect(w / 2f - 100, h - 74, 200, 12); } },
    TITLE("Title / Subtitle", Category.TEXT, Icon.item("book"))                 { public Rect box(int w, int h) { return new Rect(w / 2f - 120, h / 2f - 42, 240, 72); } },
    ITEM_NAME("Held item name", Category.TEXT, Icon.item("name_tag"))           { public Rect box(int w, int h) { return new Rect(w / 2f - 75, h - 60, 150, 12); } },
    EFFECTS("Status effects", Category.INFO, Icon.item("potion"))               { public Rect box(int w, int h) { return new Rect(w - 80, 0, 80, 27); } },
    BOSS_BAR("Boss bar", Category.INFO, Icon.item("dragon_head"))               { public Rect box(int w, int h) { return new Rect(w / 2f - 91, 0, 182, 20); } },
    SCOREBOARD("Scoreboard", Category.INFO, Icon.item("map"))                   { public Rect box(int w, int h) { return new Rect(w - 110, h / 2f - 32, 110, 64); } },
    TAB_LIST("Player list (Tab)", Category.INFO, Icon.item("player_head"))      { public Rect box(int w, int h) { return new Rect(w / 2f - 130, 10, 260, 60); } },
    AIR("Air bubbles", Category.EXTRA, Icon.sprite("hud/air"))                  { public Rect box(int w, int h) { return new Rect(w / 2f + 10, h - 49, 81, 9); } },
    MOUNT_HEALTH("Mount health", Category.EXTRA, Icon.item("saddle"))           { public Rect box(int w, int h) { return new Rect(w / 2f + 10, h - 39, 81, 9); } },
    SUBTITLES("Sound subtitles", Category.EXTRA, Icon.item("note_block"))       { public Rect box(int w, int h) { return new Rect(w - 130, h - 72, 130, 34); } };

    public final String label;
    public final Category category;
    public final Icon icon;

    HudElement(String label, Category category, Icon icon) {
        this.label = label;
        this.category = category;
        this.icon = icon;
    }

    /** List/editor icon: either a vanilla GUI sprite or an item (looked up lazily). */
    public record Icon(String sprite, String item) {
        static Icon sprite(String path) { return new Icon(path, null); }
        static Icon item(String id) { return new Icon(null, id); }
    }

    /** Default (untransformed) box for a scaled screen of w x h. */
    public abstract Rect box(int w, int h);

    /** Box after the user's offset + scale are applied. */
    public Rect current(int w, int h) {
        ElementState s = ShapeHud.config().get(this);
        Rect b = box(w, h);
        float cw = b.w() * s.scale, ch = b.h() * s.scale;
        return new Rect(b.cx() + s.dx - cw / 2f, b.cy() + s.dy - ch / 2f, cw, ch);
    }

    public record Rect(float x, float y, float w, float h) {
        public float cx() { return x + w / 2f; }
        public float cy() { return y + h / 2f; }
        public float x2() { return x + w; }
        public float y2() { return y + h; }
        public boolean contains(double px, double py) { return px >= x && px <= x2() && py >= y && py <= y2(); }
        public boolean intersects(Rect o) { return x < o.x2() && x2() > o.x && y < o.y2() && y2() > o.y; }
        public Rect union(Rect o) {
            float nx = Math.min(x, o.x), ny = Math.min(y, o.y);
            return new Rect(nx, ny, Math.max(x2(), o.x2()) - nx, Math.max(y2(), o.y2()) - ny);
        }
        public static Rect ofCorners(double x1, double y1, double x2, double y2) {
            float ax = (float) Math.min(x1, x2), ay = (float) Math.min(y1, y2);
            return new Rect(ax, ay, (float) Math.abs(x2 - x1), (float) Math.abs(y2 - y1));
        }
    }
}
