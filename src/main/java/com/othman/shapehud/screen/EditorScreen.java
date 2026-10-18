package com.othman.shapehud.screen;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.othman.shapehud.Category;
import com.othman.shapehud.ElementState;
import com.othman.shapehud.ShapeConfig;
import com.othman.shapehud.ShapeHud;
import com.othman.shapehud.HudElement;
import com.othman.shapehud.HudElement.Rect;
import com.othman.shapehud.HudTransform;
import com.othman.shapehud.L;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Type;
import java.util.*;

/**
 * Drag & drop HUD editor with a side panel (element list with on/off switches, actions),
 * example previews, alignment guides, corner resize handle and undo.
 */
public class EditorScreen extends Screen {
    private static final float MIN_SCALE = 0.1f, MAX_SCALE = 6f;
    private static final int PANEL_W = 150, ROW_H = 16, HEADER_H = 13, LIST_TOP = 104, HANDLE = 6, SNAP_DIST = 4;
    private static final int PANEL_BG = 0xE8101218, PANEL_LINE = 0xFF2C313C, ACCENT = 0xFFFFD34D, GUIDE = 0xFFFF4FD8;
    private static final Gson GSON = new Gson();
    private static final Type STATE_MAP = new TypeToken<LinkedHashMap<String, ElementState>>() {}.getType();

    private enum Mode { NONE, MOVE, RESIZE, MARQUEE }

    private final Screen parent;
    private final Set<HudElement> selected = new LinkedHashSet<>();
    private final Deque<String> undoStack = new ArrayDeque<>();

    private Mode mode = Mode.NONE;
    private double startX, startY, curX, curY;
    private boolean marqueeAdditive, marqueeIgnoreGroups, dragSnapshotTaken;
    private final Map<HudElement, float[]> origin = new EnumMap<>(HudElement.class); // dx, dy, scale, curCx, curCy, defCx, defCy
    private Rect originBounds;
    private float resizeCx, resizeCy, resizeDist0;
    private Float guideX, guideY;
    private long lastWheelUndo;
    private int listScroll;
    private HudElement listHover;

    private Button undoBtn, groupBtn, ungroupBtn, previewBtn;

    public EditorScreen(Screen parent) {
        super(Component.literal("ShapeHUD Editor"));
        this.parent = parent;
    }

    private static ShapeConfig cfg() { return ShapeHud.config(); }
    private static ElementState st(HudElement e) { return cfg().get(e); }

    // ================================================================== layout & widgets

    private int panelX() { return cfg().panelRight ? width - PANEL_W : 0; }
    private boolean panelOpen() { return !cfg().panelCollapsed; }
    private boolean inPanel(double mx, double my) {
        return panelOpen() && mx >= panelX() && mx < panelX() + PANEL_W;
    }

    private static Tooltip tip(String key) { return Tooltip.create(L.tip(key)); }
    /** Layout stays the same in every language; only the text changes. */
    private static boolean rtl() { return false; }

    @Override
    protected void init() {
        int px = panelX();
        if (!panelOpen()) {
            int bx = cfg().panelRight ? width - 74 : 4;
            addRenderableWidget(Button.builder(Component.literal("☰ ShapeHUD"), b -> setCollapsed(false))
                .bounds(bx, 4, 70, 16).tooltip(tip("tip.show")).build());
            return;
        }
        // header buttons
        addRenderableWidget(Button.builder(Component.literal(cfg().panelRight ? "◀" : "▶"), b -> {
            cfg().panelRight = !cfg().panelRight; cfg().save(); rebuildWidgets();
        }).bounds(px + PANEL_W - 38, 4, 16, 14).tooltip(tip("tip.dock")).build());
        addRenderableWidget(Button.builder(Component.literal("–"), b -> setCollapsed(true))
            .bounds(px + PANEL_W - 20, 4, 16, 14).tooltip(tip("tip.hide")).build());
        addRenderableWidget(Button.builder(L.c("btn.lang"), b -> {
            cfg().language = L.arabic() ? "en" : "ar"; cfg().save(); rebuildWidgets();
        }).bounds(px + PANEL_W - 58, 4, 18, 14).tooltip(tip("tip.lang")).build());

        int bw = (PANEL_W - 12) / 2, bh = 16, x1 = px + 4, x2 = px + 8 + bw;
        int y = 24;
        undoBtn = addRenderableWidget(Button.builder(L.c("btn.undo"), b -> undo())
            .bounds(x1, y, bw, bh).tooltip(tip("tip.undo")).build());
        addRenderableWidget(Button.builder(L.c("btn.reset"), b -> resetSelection())
            .bounds(x2, y, bw, bh).tooltip(tip("tip.reset")).build());
        y += 18;
        groupBtn = addRenderableWidget(Button.builder(L.c("btn.group"), b -> groupSelection())
            .bounds(x1, y, bw, bh).tooltip(tip("tip.group")).build());
        ungroupBtn = addRenderableWidget(Button.builder(L.c("btn.ungroup"), b -> ungroupSelection())
            .bounds(x2, y, bw, bh).tooltip(tip("tip.ungroup")).build());
        y += 18;
        previewBtn = addRenderableWidget(Button.builder(Component.empty(), b -> {
            cfg().preview = !cfg().preview; cfg().save();
        }).bounds(x1, y, bw, bh).tooltip(tip("tip.preview")).build());
        addRenderableWidget(Button.builder(L.c("btn.settings"), b -> minecraft.setScreen(new SettingsScreen(this)))
            .bounds(x2, y, bw, bh).tooltip(tip("tip.settings")).build());
        y += 18;
        addRenderableWidget(Button.builder(L.c("btn.resetAll"), b -> {
            snapshot();
            for (HudElement e : HudElement.values()) { st(e).reset(); st(e).group = null; }
            selected.clear();
            cfg().save();
        }).bounds(x1, y, bw, bh).tooltip(tip("tip.resetAll")).build());
        addRenderableWidget(Button.builder(L.c("btn.done"), b -> onClose())
            .bounds(x2, y, bw, bh).tooltip(tip("tip.done")).build());
    }

    private void setCollapsed(boolean c) {
        cfg().panelCollapsed = c;
        cfg().save();
        rebuildWidgets();
    }

    private void updateButtons() {
        if (undoBtn == null || !panelOpen()) return;
        undoBtn.active = !undoStack.isEmpty();
        groupBtn.active = selected.size() >= 2;
        ungroupBtn.active = selected.stream().anyMatch(e -> st(e).group != null);
        previewBtn.setMessage(L.c("btn.preview", L.raw(cfg().preview ? "on" : "off")));
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float delta) {
        // no blur / dim: the live HUD stays visible underneath
    }

    @Override
    public void onClose() {
        cfg().save();
        minecraft.setScreen(parent);
    }

    // ================================================================== model helpers

    private Rect cur(HudElement e) { return e.current(width, height); }

    private HudElement elementAt(double mx, double my) {
        if (inPanel(mx, my)) return null;
        HudElement best = null;
        float bestArea = Float.MAX_VALUE;
        for (HudElement e : HudElement.values()) {
            Rect r = cur(e);
            float area = r.w() * r.h();
            if (r.contains(mx, my) && area < bestArea) { best = e; bestArea = area; }
        }
        return best;
    }

    private Set<HudElement> unitOf(HudElement e, boolean ignoreGroup) {
        Set<HudElement> out = new LinkedHashSet<>();
        String g = st(e).group;
        if (g == null || ignoreGroup) { out.add(e); return out; }
        for (HudElement o : HudElement.values()) if (g.equals(st(o).group)) out.add(o);
        return out;
    }

    private Rect bounds(Collection<HudElement> set) {
        Rect u = null;
        for (HudElement e : set) u = u == null ? cur(e) : u.union(cur(e));
        return u;
    }

    private Rect handleRect() {
        if (selected.isEmpty()) return null;
        Rect b = bounds(selected);
        return new Rect(b.x2() - HANDLE / 2f, b.y2() - HANDLE / 2f, HANDLE, HANDLE);
    }

    private static boolean has(int mods, int flag) { return (mods & flag) != 0; }

    private boolean keyDown(int key) { return GLFW.glfwGetKey(minecraft.getWindow().handle(), key) == GLFW.GLFW_PRESS; }
    private boolean ctrlDown() { return keyDown(GLFW.GLFW_KEY_LEFT_CONTROL) || keyDown(GLFW.GLFW_KEY_RIGHT_CONTROL); }

    // ================================================================== undo

    private void snapshot() {
        undoStack.push(GSON.toJson(cfg().elements, STATE_MAP));
        while (undoStack.size() > 60) undoStack.removeLast();
    }

    private void undo() {
        if (undoStack.isEmpty()) return;
        Map<String, ElementState> m = GSON.fromJson(undoStack.pop(), STATE_MAP);
        cfg().elements.clear();
        cfg().elements.putAll(m);
        cfg().save();
    }

    // ================================================================== actions

    private void groupSelection() {
        if (selected.size() < 2) return;
        snapshot();
        String id = "g" + cfg().nextGroupId++;
        for (HudElement e : selected) st(e).group = id;
        cfg().save();
    }

    private void ungroupSelection() {
        if (selected.stream().noneMatch(e -> st(e).group != null)) return;
        snapshot();
        for (HudElement e : selected) st(e).group = null;
        cfg().save();
    }

    private void setHidden(Collection<HudElement> set, boolean hidden) {
        snapshot();
        for (HudElement e : set) st(e).hidden = hidden;
        cfg().save();
    }

    private void toggleHiddenSelection() {
        if (selected.isEmpty()) return;
        setHidden(selected, selected.stream().anyMatch(e -> !st(e).hidden));
    }

    private void resetSelection() {
        if (selected.isEmpty()) return;
        snapshot();
        for (HudElement e : selected) { boolean h = st(e).hidden; st(e).reset(); st(e).hidden = h; }
        cfg().save();
    }

    private void nudge(float dx, float dy) {
        if (selected.isEmpty()) return;
        snapshot();
        for (HudElement e : selected) { st(e).dx += dx; st(e).dy += dy; }
        cfg().save();
    }

    /** Captures dx, dy, scale and centers of the selection, used as the base for drags and resizes. */
    private void captureOrigin() {
        origin.clear();
        for (HudElement e : selected) {
            ElementState s = st(e);
            Rect c = cur(e), d = e.box(width, height);
            origin.put(e, new float[]{s.dx, s.dy, s.scale, c.cx(), c.cy(), d.cx(), d.cy()});
        }
        originBounds = selected.isEmpty() ? null : bounds(selected);
    }

    /** Scales the captured selection by {@code factor} around (cx, cy), keeping relative layout. */
    private void scaleFromOrigin(float factor, float cx, float cy) {
        for (float[] o : origin.values()) {
            float ns = o[2] * factor;
            if (ns < MIN_SCALE) factor = MIN_SCALE / o[2];
            if (ns > MAX_SCALE) factor = MAX_SCALE / o[2];
        }
        for (Map.Entry<HudElement, float[]> en : origin.entrySet()) {
            float[] o = en.getValue();
            ElementState s = st(en.getKey());
            s.scale = Math.round(o[2] * factor * 1000f) / 1000f;
            s.dx = cx + (o[3] - cx) * factor - o[5];
            s.dy = cy + (o[4] - cy) * factor - o[6];
        }
    }

    private void scaleSelection(float factor) {
        if (selected.isEmpty()) return;
        captureOrigin();
        scaleFromOrigin(factor, originBounds.cx(), originBounds.cy());
        cfg().save();
    }

    // ================================================================== input

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (!cfg().seenIntro) {
            cfg().seenIntro = true;
            cfg().save();
            return true;
        }
        if (super.mouseClicked(event, doubleClick)) return true;
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
        double mx = event.x(), my = event.y();
        boolean shift = has(event.modifiers(), GLFW.GLFW_MOD_SHIFT);
        boolean alt = has(event.modifiers(), GLFW.GLFW_MOD_ALT);

        if (inPanel(mx, my)) {
            clickList(mx, my, shift, alt);
            return true;
        }

        startX = curX = mx;
        startY = curY = my;
        dragSnapshotTaken = false;

        Rect handle = handleRect();
        if (handle != null && new Rect(handle.x() - 2, handle.y() - 2, handle.w() + 4, handle.h() + 4).contains(mx, my)) {
            captureOrigin();
            resizeCx = originBounds.cx();
            resizeCy = originBounds.cy();
            resizeDist0 = (float) Math.max(4, Math.hypot(mx - resizeCx, my - resizeCy));
            mode = Mode.RESIZE;
            return true;
        }

        HudElement hit = elementAt(mx, my);
        if (hit != null) {
            Set<HudElement> unit = unitOf(hit, alt);
            if (shift) {
                if (selected.containsAll(unit)) selected.removeAll(unit); else selected.addAll(unit);
                mode = Mode.NONE;
                return true;
            }
            if (!selected.contains(hit) || alt) { selected.clear(); selected.addAll(unit); }
            captureOrigin();
            mode = Mode.MOVE;
        } else {
            if (!shift) selected.clear();
            marqueeAdditive = shift;
            marqueeIgnoreGroups = alt;
            mode = Mode.MARQUEE;
        }
        return true;
    }

    private void clickList(double mx, double my, boolean shift, boolean alt) {
        for (ListRow row : listRows()) {
            if (row.element == null || my < row.y || my >= row.y + ROW_H || row.y < LIST_TOP || row.y + ROW_H > listBottom()) continue;
            HudElement e = row.element;
            int tx = rtl() ? panelX() + 6 : panelX() + PANEL_W - 24;
            if (mx >= tx - 2 && mx <= tx + 18) {
                setHidden(List.of(e), !st(e).hidden);
                return;
            }
            Set<HudElement> unit = unitOf(e, alt);
            if (shift) { if (selected.containsAll(unit)) selected.removeAll(unit); else selected.addAll(unit); }
            else { selected.clear(); selected.addAll(unit); }
            return;
        }
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (mode == Mode.NONE) return super.mouseDragged(event, dragX, dragY);
        curX = event.x();
        curY = event.y();
        if ((mode == Mode.MOVE || mode == Mode.RESIZE) && !dragSnapshotTaken) {
            // take the undo snapshot from the values before this drag started
            Map<String, ElementState> before = new LinkedHashMap<>();
            for (Map.Entry<String, ElementState> en : cfg().elements.entrySet()) before.put(en.getKey(), copy(en.getValue()));
            for (Map.Entry<HudElement, float[]> en : origin.entrySet()) {
                ElementState c = before.get(en.getKey().name());
                if (c != null) { c.dx = en.getValue()[0]; c.dy = en.getValue()[1]; c.scale = en.getValue()[2]; }
            }
            undoStack.push(GSON.toJson(before, STATE_MAP));
            dragSnapshotTaken = true;
        }
        if (mode == Mode.MOVE) dragMove();
        else if (mode == Mode.RESIZE) {
            float d = (float) Math.hypot(curX - resizeCx, curY - resizeCy);
            scaleFromOrigin(Math.max(0.01f, d / resizeDist0), resizeCx, resizeCy);
        }
        return true;
    }

    private static ElementState copy(ElementState s) {
        ElementState c = new ElementState();
        c.dx = s.dx; c.dy = s.dy; c.scale = s.scale; c.hidden = s.hidden; c.group = s.group;
        return c;
    }

    private void dragMove() {
        float ddx = (float) (curX - startX), ddy = (float) (curY - startY);
        guideX = guideY = null;
        boolean free = ctrlDown(); // Ctrl = no snapping at all
        if (!free && originBounds != null) {
            Rect b = originBounds;
            // alignment guides: screen edges and center
            float[][] xs = {{b.x() + ddx, 0}, {b.cx() + ddx, width / 2f}, {b.x2() + ddx, width}};
            float best = SNAP_DIST + 1;
            for (float[] c : xs) {
                float diff = c[1] - c[0];
                if (Math.abs(diff) <= SNAP_DIST && Math.abs(diff) < best) { best = Math.abs(diff); ddx += diff; guideX = c[1]; break; }
            }
            float[][] ys = {{b.y() + ddy, 0}, {b.cy() + ddy, height / 2f}, {b.y2() + ddy, height}};
            best = SNAP_DIST + 1;
            for (float[] c : ys) {
                float diff = c[1] - c[0];
                if (Math.abs(diff) <= SNAP_DIST && Math.abs(diff) < best) { best = Math.abs(diff); ddy += diff; guideY = c[1]; break; }
            }
        }
        int grid = Math.max(1, cfg().gridSize);
        for (Map.Entry<HudElement, float[]> en : origin.entrySet()) {
            ElementState s = st(en.getKey());
            float nx = en.getValue()[0] + ddx, ny = en.getValue()[1] + ddy;
            if (cfg().snapToGrid && !free) {
                if (guideX == null) nx = Math.round(nx / grid) * grid;
                if (guideY == null) ny = Math.round(ny / grid) * grid;
            }
            s.dx = nx;
            s.dy = ny;
        }
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (mode == Mode.MARQUEE) {
            Rect box = Rect.ofCorners(startX, startY, curX, curY);
            if (box.w() > 2 || box.h() > 2) {
                Set<HudElement> picked = new LinkedHashSet<>();
                for (HudElement e : HudElement.values()) if (cur(e).intersects(box)) picked.addAll(unitOf(e, marqueeIgnoreGroups));
                if (!marqueeAdditive) selected.clear();
                selected.addAll(picked);
            }
        } else if (mode == Mode.MOVE || mode == Mode.RESIZE) {
            cfg().save();
        }
        boolean wasActive = mode != Mode.NONE;
        mode = Mode.NONE;
        guideX = guideY = null;
        return wasActive || super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double horizontal, double vertical) {
        if (vertical == 0) return false;
        if (inPanel(mx, my)) {
            listScroll = clampScroll(listScroll - (int) Math.signum(vertical) * ROW_H);
            return true;
        }
        if (selected.isEmpty()) {
            HudElement hit = elementAt(mx, my);
            if (hit == null) return false;
            selected.addAll(unitOf(hit, false));
        }
        long now = System.currentTimeMillis();
        if (now - lastWheelUndo > 700) snapshot();
        lastWheelUndo = now;
        float step = ctrlDown() ? 1.02f : 1.08f;
        scaleSelection(vertical > 0 ? step : 1 / step);
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key(), mods = event.modifiers();
        boolean ctrl = has(mods, GLFW.GLFW_MOD_CONTROL), shift = has(mods, GLFW.GLFW_MOD_SHIFT);
        float step = shift ? 10 : 1;
        switch (key) {
            case GLFW.GLFW_KEY_Z -> { if (ctrl) { undo(); return true; } }
            case GLFW.GLFW_KEY_A -> { if (ctrl) { selected.clear(); selected.addAll(List.of(HudElement.values())); return true; } }
            case GLFW.GLFW_KEY_G -> { if (shift) ungroupSelection(); else groupSelection(); return true; }
            case GLFW.GLFW_KEY_U -> { ungroupSelection(); return true; }
            case GLFW.GLFW_KEY_V -> { toggleHiddenSelection(); return true; }
            case GLFW.GLFW_KEY_R -> { resetSelection(); return true; }
            case GLFW.GLFW_KEY_P -> { cfg().preview = !cfg().preview; cfg().save(); return true; }
            case GLFW.GLFW_KEY_EQUAL, GLFW.GLFW_KEY_KP_ADD -> { snapshot(); scaleSelection(shift ? 1.02f : 1.08f); return true; }
            case GLFW.GLFW_KEY_MINUS, GLFW.GLFW_KEY_KP_SUBTRACT -> { snapshot(); scaleSelection(1 / (shift ? 1.02f : 1.08f)); return true; }
            case GLFW.GLFW_KEY_LEFT -> { nudge(-step, 0); return true; }
            case GLFW.GLFW_KEY_RIGHT -> { nudge(step, 0); return true; }
            case GLFW.GLFW_KEY_UP -> { nudge(0, -step); return true; }
            case GLFW.GLFW_KEY_DOWN -> { nudge(0, step); return true; }
            case GLFW.GLFW_KEY_TAB -> { setCollapsed(panelOpen()); return true; }
            default -> {}
        }
        return super.keyPressed(event);
    }

    // ================================================================== list model

    private record ListRow(int y, Category header, HudElement element) {}

    private int listBottom() { return height - 26; }

    private int contentHeight() {
        return Category.values().length * HEADER_H + HudElement.values().length * ROW_H;
    }

    private int clampScroll(int s) {
        int max = Math.max(0, contentHeight() - (listBottom() - LIST_TOP));
        return Math.max(0, Math.min(max, s));
    }

    private List<ListRow> listRows() {
        List<ListRow> rows = new ArrayList<>();
        int y = LIST_TOP - listScroll;
        for (Category c : Category.values()) {
            rows.add(new ListRow(y, c, null));
            y += HEADER_H;
            for (HudElement e : HudElement.values()) {
                if (e.category != c) continue;
                rows.add(new ListRow(y, null, e));
                y += ROW_H;
            }
        }
        return rows;
    }

    // ================================================================== drawing

    private static final int[] GROUP_COLORS = {
        0xFF4FC3F7, 0xFF81C784, 0xFFFF8A65, 0xFFBA68C8, 0xFFFFF176, 0xFF4DB6AC, 0xFFF06292, 0xFF9575CD
    };

    private static int groupColor(String group) {
        return GROUP_COLORS[Math.floorMod(group.hashCode(), GROUP_COLORS.length)];
    }

    private void chip(GuiGraphics g, Component text, float anchorX, float topY, float bottomY, int color) {
        int tw = font.width(text);
        int x = Math.round(anchorX);
        x = Math.max(2, Math.min(width - tw - 8, x));
        int y = Math.round(topY) - 13;
        if (y < 2) y = Math.round(bottomY) + 3;
        g.fill(x, y, x + tw + 6, y + 11, 0xE6101218);
        g.fill(x, y, x + 2, y + 11, color);
        g.drawString(font, text, x + 4, y + 2, 0xFFFFFFFF, false);
    }

    /** Draws text aligned left, or right-aligned at {@code right} in Arabic mode. */
    private void text(GuiGraphics g, Component c, int left, int right, int y, int color) {
        g.drawString(font, c, rtl() ? right - font.width(c) : left, y, color, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        listScroll = clampScroll(listScroll);
        updateButtons();
        HudElement hover = mode == Mode.NONE ? elementAt(mouseX, mouseY) : null;
        HudElement highlight = hover != null ? hover : listHover;

        // 1) example content for elements that aren't on screen
        if (cfg().preview) {
            for (HudElement e : HudElement.values()) {
                if (st(e).hidden || !Previews.needed(minecraft, e)) continue;
                var pose = g.pose();
                pose.pushMatrix();
                HudTransform.apply(pose, e, width, height);
                Previews.draw(g, font, e, width, height);
                pose.popMatrix();
            }
        }
        g.nextStratum();

        // 2) group outlines
        Map<String, List<HudElement>> groups = new LinkedHashMap<>();
        for (HudElement e : HudElement.values()) {
            String gid = st(e).group;
            if (gid != null) groups.computeIfAbsent(gid, k -> new ArrayList<>()).add(e);
        }
        for (Map.Entry<String, List<HudElement>> en : groups.entrySet()) {
            Rect u = bounds(en.getValue());
            Draw.outline(g, u.x() - 3, u.y() - 3, u.w() + 6, u.h() + 6, groupColor(en.getKey()) & 0x99FFFFFF);
        }

        // 3) element frames: calm corners when idle, full frame when hovered / selected
        for (HudElement e : HudElement.values()) {
            Rect r = cur(e);
            ElementState s = st(e);
            int c = e.category.color;
            boolean sel = selected.contains(e);
            if (s.hidden) {
                Draw.fill(g, r.x(), r.y(), r.w(), r.h(), sel ? 0x40FF4040 : 0x18FF4040);
                Draw.corners(g, r, 0xC0FF5555);
                if (sel || e == highlight) Draw.outline(g, r.x(), r.y(), r.w(), r.h(), 0xFFFF5555);
            } else if (sel) {
                Draw.fill(g, r.x(), r.y(), r.w(), r.h(), e.category.withAlpha(0x38));
                Draw.outline(g, r.x(), r.y(), r.w(), r.h(), c);
            } else if (e == highlight) {
                Draw.fill(g, r.x(), r.y(), r.w(), r.h(), e.category.withAlpha(0x22));
                Draw.outline(g, r.x(), r.y(), r.w(), r.h(), e.category.withAlpha(0xCC));
            } else {
                Draw.corners(g, r, e.category.withAlpha(0x90));
            }
        }

        // 4) guides, marquee, selection chip + resize handle
        if (guideX != null) g.fill(Math.round(guideX), 0, Math.round(guideX) + 1, height, GUIDE);
        if (guideY != null) g.fill(0, Math.round(guideY), width, Math.round(guideY) + 1, GUIDE);

        if (mode == Mode.MARQUEE) {
            Rect m = Rect.ofCorners(startX, startY, curX, curY);
            Draw.fill(g, m.x(), m.y(), m.w(), m.h(), 0x2855AAFF);
            Draw.outline(g, m.x(), m.y(), m.w(), m.h(), 0xFF55AAFF);
        }

        if (!selected.isEmpty()) {
            Rect b = bounds(selected);
            HudElement first = selected.iterator().next();
            int pct = Math.round(st(first).scale * 100);
            String name = selected.size() == 1 ? L.raw("el." + first.name()) : L.raw("chip.many", selected.size());
            if (selected.size() > 1) {
                String gid = st(first).group;
                if (gid != null && selected.stream().allMatch(e -> gid.equals(st(e).group))) name = L.raw("chip.group", selected.size());
            }
            String chipText = name + "  " + pct + "%" + (st(first).hidden ? "  " + L.raw("chip.hidden") : "");
            chip(g, L.comp(L.vis(chipText)), b.x(), b.y(), b.y2(),
                selected.size() == 1 ? first.category.color : ACCENT);
            if (selected.size() > 1) Draw.outline(g, b.x() - 1, b.y() - 1, b.w() + 2, b.h() + 2, 0x80FFFFFF);
            Rect h = handleRect();
            g.fill(Math.round(h.x()) - 1, Math.round(h.y()) - 1, Math.round(h.x2()) + 1, Math.round(h.y2()) + 1, 0xFF101218);
            g.fill(Math.round(h.x()), Math.round(h.y()), Math.round(h.x2()), Math.round(h.y2()), 0xFFFFFFFF);
        } else if (hover != null) {
            Rect r = cur(hover);
            chip(g, L.comp(L.element(hover)), r.x(), r.y(), r.y2(), hover.category.color);
        }

        // 5) side panel
        g.nextStratum();
        listHover = null;
        if (panelOpen()) renderPanel(g, mouseX, mouseY);
        else hint(g);

        super.render(g, mouseX, mouseY, delta);

        // 6) first-time intro card
        if (!cfg().seenIntro) {
            g.nextStratum();
            renderIntro(g);
        }
    }

    private void renderPanel(GuiGraphics g, int mouseX, int mouseY) {
        int px = panelX();
        g.fill(px, 0, px + PANEL_W, height, PANEL_BG);
        g.fill(cfg().panelRight ? px : px + PANEL_W - 1, 0, cfg().panelRight ? px + 1 : px + PANEL_W, height, PANEL_LINE);

        // title
        g.fill(px + 6, 8, px + 9, 16, ACCENT);
        g.drawString(font, "§lShapeHUD", px + 13, 8, 0xFFFFFFFF, false);

        g.fill(px + 4, LIST_TOP - 4, px + PANEL_W - 4, LIST_TOP - 3, PANEL_LINE);

        // list
        int bottom = listBottom();
        g.enableScissor(px, LIST_TOP, px + PANEL_W, bottom);
        for (ListRow row : listRows()) {
            if (row.y + ROW_H < LIST_TOP || row.y > bottom) continue;
            if (row.header != null) {
                int sx = rtl() ? px + PANEL_W - 10 : px + 6;
                g.fill(sx, row.y + 5, sx + 4, row.y + 9, row.header.color);
                Component hc = rtl() ? L.comp(L.category(row.header))
                    : Component.literal(row.header.label.toUpperCase(Locale.ROOT)).withStyle(st -> st.withBold(true));
                text(g, hc, px + 14, px + PANEL_W - 14, row.y + 3, row.header.color);
                continue;
            }
            HudElement e = row.element;
            ElementState s = st(e);
            boolean sel = selected.contains(e);
            boolean hov = mouseX >= px && mouseX < px + PANEL_W && mouseY >= row.y && mouseY < row.y + ROW_H
                && mouseY >= LIST_TOP && mouseY < bottom;
            if (hov) listHover = e;
            if (sel) g.fill(px + 2, row.y, px + PANEL_W - 2, row.y + ROW_H, e.category.withAlpha(0x40));
            else if (hov) g.fill(px + 2, row.y, px + PANEL_W - 2, row.y + ROW_H, 0x22FFFFFF);
            if (sel) g.fill(px + 2, row.y, px + 4, row.y + ROW_H, e.category.color);

            boolean r2l = rtl();
            Draw.icon(g, e, r2l ? px + PANEL_W - 21 : px + 7, row.y + 2, 12);
            int maxW = PANEL_W - 24 - 30;
            Component name = L.comp(L.fit(font, L.raw("el." + e.name()), maxW));
            text(g, name, px + 23, px + PANEL_W - 25, row.y + 4, s.hidden ? 0xFF777A82 : 0xFFE8E8EC);
            if (s.group != null) {
                int gx = r2l ? px + 27 : px + PANEL_W - 30;
                g.fill(gx, row.y + 6, gx + 3, row.y + 9, groupColor(s.group));
            }
            Draw.toggle(g, r2l ? px + 6 : px + PANEL_W - 24, row.y + 4, !s.hidden, e.category.color);
        }
        g.disableScissor();

        // scrollbar
        int viewH = bottom - LIST_TOP, contentH = contentHeight();
        if (contentH > viewH) {
            int barH = Math.max(12, viewH * viewH / contentH);
            int barY = LIST_TOP + (viewH - barH) * listScroll / Math.max(1, contentH - viewH);
            g.fill(px + PANEL_W - 3, barY, px + PANEL_W - 1, barY + barH, 0x80FFFFFF);
        }

        // footer hints
        g.fill(px + 4, bottom + 1, px + PANEL_W - 4, bottom + 2, PANEL_LINE);
        text(g, L.c("foot.1"), px + 6, px + PANEL_W - 6, bottom + 5, 0xFF8A8F9A);
        text(g, L.c("foot.2"), px + 6, px + PANEL_W - 6, bottom + 15, 0xFF8A8F9A);
    }

    private void hint(GuiGraphics g) {
        Component s = L.c("hint");
        int tw = font.width(s);
        int x = width / 2 - tw / 2 - 5, y = height / 2 + 40;
        g.fill(x, y, x + tw + 10, y + 13, 0x90000000);
        g.drawString(font, s, x + 5, y + 3, 0xFFCCCCCC, false);
    }

    private void renderIntro(GuiGraphics g) {
        Component[] lines = new Component[5];
        for (int i = 0; i < 5; i++) lines[i] = L.comp(L.vis((i + 1) + ". " + L.raw("intro." + (i + 1))));
        int w = 0;
        for (Component l : lines) w = Math.max(w, font.width(l));
        w += 24;
        int h = 30 + lines.length * 12 + 20;
        int x = width / 2 - w / 2, y = height / 2 - h / 2;
        g.fill(0, 0, width, height, 0x80000000);
        g.fill(x, y, x + w, y + h, 0xF2101218);
        g.fill(x, y, x + w, y + 2, ACCENT);
        g.drawCenteredString(font, L.c("intro.title"), width / 2, y + 10, ACCENT);
        int ly = y + 28;
        for (Component l : lines) { text(g, l, x + 12, x + w - 12, ly, 0xFFCCCCCC); ly += 12; }
        g.drawCenteredString(font, L.c("intro.start"), width / 2, y + h - 14, 0xFFFFFF55);
    }
}
