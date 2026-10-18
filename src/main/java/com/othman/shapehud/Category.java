package com.othman.shapehud;

/** Groups elements in the editor list and gives each family its own color. */
public enum Category {
    CORE("Core", 0xFF4CD97B),
    STATS("Health & Stats", 0xFFFF5A5F),
    TEXT("Text & Chat", 0xFF4DA6FF),
    INFO("Info Panels", 0xFFB98CFF),
    EXTRA("Extras", 0xFFFFB547);

    public final String label;
    /** ARGB, fully opaque. */
    public final int color;

    Category(String label, int color) { this.label = label; this.color = color; }

    public int withAlpha(int alpha) { return (alpha << 24) | (color & 0xFFFFFF); }
}
