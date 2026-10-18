package com.othman.shapehud;

import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tiny built-in translation table (English / Arabic) with Arabic shaping + right-to-left reordering,
 * so Arabic shows correctly even when the game itself is set to English.
 */
public final class L {
    private L() {}

    private static final Map<String, String> EN = new HashMap<>();
    private static final Map<String, String> AR = new HashMap<>();

    private static void put(String key, String en, String ar) { EN.put(key, en); AR.put(key, ar); }

    static {
        // editor buttons
        put("btn.undo", "↶ Undo", "↶ تراجع");
        put("btn.reset", "⟲ Reset", "⟲ إرجاع");
        put("btn.group", "Group", "تجميع");
        put("btn.ungroup", "Ungroup", "فك التجميع");
        put("btn.preview", "Preview: %s", "المعاينة: %s");
        put("on", "ON", "✔");
        put("off", "OFF", "✖");
        put("btn.settings", "Settings", "الإعدادات");
        put("btn.resetAll", "Reset all", "إرجاع الكل");
        put("btn.done", "✔ Done", "✔ تم");
        put("btn.lang", "ع", "EN");
        // tooltips
        put("tip.show", "Show the panel", "إظهار القائمة");
        put("tip.dock", "Move the panel to the other side", "نقل القائمة للجهة الثانية");
        put("tip.hide", "Hide the panel (Tab)", "إخفاء القائمة (Tab)");
        put("tip.lang", "العربية / English", "العربية / English");
        put("tip.undo", "Undo the last change (Ctrl+Z)", "التراجع عن آخر تعديل (Ctrl+Z)");
        put("tip.reset", "Put the selected elements back to vanilla (R)", "إرجاع العناصر المحددة لمكانها الأصلي (R)");
        put("tip.group", "Link the selected elements so they move and resize together (G)", "ربط العناصر المحددة لتتحرك وتتغير أحجامها مع بعض (G)");
        put("tip.ungroup", "Unlink the selected elements (U)", "فك ربط العناصر المحددة (U)");
        put("tip.preview", "Show example content (chat, boss bar, title...) for elements that aren't on screen right now (P)",
            "عرض أمثلة (الشات، شريط البوس، العنوان...) للعناصر غير الظاهرة الآن (P)");
        put("tip.settings", "Shortcut keys and snapping", "أزرار الاختصار والمحاذاة");
        put("tip.resetAll", "Reset every element (can be undone)", "إرجاع كل العناصر (يمكن التراجع)");
        put("tip.done", "Save and close (Esc)", "حفظ وإغلاق (Esc)");
        // selection chip
        put("chip.many", "%d elements", "%d عناصر");
        put("chip.group", "Group • %d", "مجموعة • %d");
        put("chip.hidden", "(hidden)", "(مخفي)");
        // panel footer / hints
        put("foot.1", "Drag: move  •  Wheel: size", "تحريك: اسحب  •  الحجم: العجلة");
        put("foot.2", "Shift+Click: multi  •  Tab: hide", "تحديد أكثر: Shift  •  إخفاء: Tab");
        put("hint", "Drag to move  •  Wheel to resize  •  Esc to save", "اسحب للتحريك  •  العجلة لتغيير الحجم  •  للحفظ Esc");
        // intro
        put("intro.title", "Welcome to ShapeHUD", "أهلاً بك في ShapeHUD");
        put("intro.1", "Click any part of your HUD to select it", "اضغط على أي جزء من الواجهة لتحديده");
        put("intro.2", "Drag to move it — scroll or drag the white corner to resize", "اسحبه لتحريكه، واستخدم العجلة أو الزاوية البيضاء لتغيير حجمه");
        put("intro.3", "Drag on empty space to select several things at once", "اسحب في مكان فارغ لتحديد أكثر من عنصر");
        put("intro.4", "Use the switches in the list to hide what you don't need", "استخدم المفاتيح في القائمة لإخفاء ما لا تحتاجه");
        put("intro.5", "Group elements to move and resize them together", "اجمع العناصر لتحريكها وتغيير حجمها معاً");
        put("intro.start", "Click anywhere to start", "اضغط في أي مكان للبدء");
        // settings
        put("set.title", "ShapeHUD Settings", "إعدادات ShapeHUD");
        put("set.key1", "Shortcut key 1: %s", "زر الاختصار 1: %s");
        put("set.key2", "Shortcut key 2: %s", "زر الاختصار 2: %s");
        put("set.press", "> press a key <", "> اضغط زراً <");
        put("set.snap", "Grid snap: %s", "الشبكة: %s");
        put("set.grid", "Grid: %dpx", "حجم الشبكة: %d");
        put("set.lang", "Language: %s", "اللغة: %s");
        put("lang.auto", "Auto", "تلقائي");
        put("lang.en", "English", "English");
        put("lang.ar", "العربية", "العربية");
        put("set.open", "Open HUD editor", "فتح محرر الواجهة");
        put("set.resetAll", "Reset all HUD elements", "إرجاع كل عناصر الواجهة");
        put("set.help", "Hold both keys in-game to open the editor. Backspace = none.",
            "اضغط الزرين معاً داخل اللعبة لفتح المحرر (Backspace = بدون)");
        put("key.none", "None", "بدون");
        // categories
        put("cat.CORE", "Core", "الأساسي");
        put("cat.STATS", "Health & Stats", "الصحة والحالة");
        put("cat.TEXT", "Text & Chat", "النصوص والشات");
        put("cat.INFO", "Info Panels", "لوحات المعلومات");
        put("cat.EXTRA", "Extras", "إضافات");
        // elements
        put("el.HOTBAR", "Hotbar", "الهوت بار");
        put("el.XP_BAR", "XP / Locator bar", "شريط الخبرة");
        put("el.XP_LEVEL", "XP level", "مستوى الخبرة");
        put("el.CROSSHAIR", "Crosshair", "علامة التصويب");
        put("el.HEALTH", "Health", "القلوب");
        put("el.ARMOR", "Armor", "الدرع");
        put("el.FOOD", "Hunger", "الجوع");
        put("el.CHAT", "Chat", "الشات");
        put("el.ACTION_BAR", "Action bar", "شريط الرسائل");
        put("el.TITLE", "Title / Subtitle", "العنوان");
        put("el.ITEM_NAME", "Held item name", "اسم الغرض");
        put("el.EFFECTS", "Status effects", "التأثيرات");
        put("el.BOSS_BAR", "Boss bar", "شريط البوس");
        put("el.SCOREBOARD", "Scoreboard", "لوحة النقاط");
        put("el.TAB_LIST", "Player list (Tab)", "قائمة اللاعبين");
        put("el.AIR", "Air bubbles", "فقاعات الهواء");
        put("el.MOUNT_HEALTH", "Mount health", "صحة الركوبة");
        put("el.SUBTITLES", "Sound subtitles", "ترجمة الأصوات");
    }

    /** True when the mod UI should be Arabic (explicit setting, or "auto" + Arabic game language). */
    public static boolean arabic() {
        String l = ShapeHud.config().language;
        if ("ar".equals(l)) return true;
        if ("en".equals(l)) return false;
        try {
            String sel = Minecraft.getInstance().getLanguageManager().getSelected();
            return sel != null && sel.startsWith("ar");
        } catch (Throwable t) {
            return false;
        }
    }

    /** Raw (logical order) translation. */
    public static String raw(String key, Object... args) {
        String s = (arabic() ? AR : EN).getOrDefault(key, EN.getOrDefault(key, key));
        return args.length == 0 ? s : String.format(s, args);
    }

    /** Translation ready to draw (shaped + reordered if needed). */
    public static String t(String key, Object... args) { return vis(raw(key, args)); }

    /** Bundled Arabic font (Cairo, SIL OFL) that falls back to the vanilla font for Latin text. */
    public static final FontDescription ARABIC_FONT = new FontDescription.Resource(Identifier.fromNamespaceAndPath("shapehud", "arabic"));

    /** Wraps already-visual text in a component, using the Arabic font when it contains Arabic. */
    public static MutableComponent comp(String visual) {
        MutableComponent c = Component.literal(visual);
        return hasArabic(visual) ? c.withStyle(st -> st.withFont(ARABIC_FONT)) : c;
    }

    public static MutableComponent c(String key, Object... args) { return comp(t(key, args)); }

    /** Tooltip text: wrapped line by line so right-to-left lines keep their order. */
    public static Component tip(String key) {
        Font font = Minecraft.getInstance().font;
        String raw = raw(key);
        return comp(vis(font == null ? raw : wrap(font, raw, 160)));
    }

    public static String element(HudElement e) { return t("el." + e.name()); }
    public static String category(Category c) { return t("cat." + c.name()); }

    public static boolean hasArabic(String s) {
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if ((ch >= 0x0600 && ch <= 0x06FF) || (ch >= 0xFB50 && ch <= 0xFDFF) || (ch >= 0xFE70 && ch <= 0xFEFF)) return true;
        }
        return false;
    }

    /**
     * Shapes Arabic letters and puts the string in visual order — the same thing vanilla does when the
     * game language is right-to-left. Skipped when vanilla will already do it, to avoid doing it twice.
     */
    public static String vis(String s) {
        // Only join the letters (shaping). The right-to-left reordering is done by vanilla when the
        // Component is drawn (ClientLanguage#getVisualOrder); doing it here too would flip it back.
        if (s == null || s.isEmpty() || !hasArabic(s)) return s;
        try {
            return new ArabicShaping(ArabicShaping.LETTERS_SHAPE).shape(s);
        } catch (ArabicShapingException ex) {
            return s;
        }
    }

    /** Raw text cut (in logical order) until its drawn width fits, then shaped. */
    public static String fit(Font font, String raw, int maxWidth) {
        String v = vis(raw);
        if (font.width(comp(v)) <= maxWidth) return v;
        String r = raw;
        while (r.length() > 1) {
            r = r.substring(0, r.length() - 1);
            v = vis(r.stripTrailing() + "…");
            if (font.width(comp(v)) <= maxWidth) return v;
        }
        return v;
    }

    /**
     * Word-wraps logical text into lines no wider than maxWidth and shapes each line on its own,
     * joined with newlines (so RTL lines stay in the right order inside tooltips).
     */
    public static String wrap(Font font, String raw, int maxWidth) {
        if (!hasArabic(raw)) return raw;
        String[] words = raw.split(" ");
        List<String> lines = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (String w : words) {
            String cand = cur.isEmpty() ? w : cur + " " + w;
            if (!cur.isEmpty() && font.width(comp(cand)) > maxWidth) {
                lines.add(cur.toString());
                cur = new StringBuilder(w);
            } else {
                cur = new StringBuilder(cand);
            }
        }
        if (!cur.isEmpty()) lines.add(cur.toString());
        return String.join("\n", lines);
    }
}
