package com.othman.shapehud;

/** Saved per-element settings. Offsets are in scaled GUI pixels relative to the vanilla position. */
public class ElementState {
    public float dx = 0, dy = 0;
    public float scale = 1f;
    public boolean hidden = false;
    /** Group id, or null if not grouped. Elements sharing an id are selected/moved/scaled together. */
    public String group = null;

    public boolean isDefault() { return dx == 0 && dy == 0 && scale == 1f && !hidden; }

    public void reset() { dx = 0; dy = 0; scale = 1f; hidden = false; }
}
