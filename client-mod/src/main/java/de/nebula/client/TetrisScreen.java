package de.nebula.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

/** Tetris. Pfeile oder WASD, Leertaste = fallen lassen, R = neu starten, ESC = zurück. */
public class TetrisScreen extends Screen {
    private static final int COLS = 10, ROWS = 20;
    private static final int[][][] SHAPES = {
        {{-1, 0}, {0, 0}, {1, 0}, {2, 0}},   // I
        {{0, 0}, {1, 0}, {0, 1}, {1, 1}},    // O
        {{-1, 0}, {0, 0}, {1, 0}, {0, 1}},   // T
        {{0, 0}, {1, 0}, {-1, 1}, {0, 1}},   // S
        {{-1, 0}, {0, 0}, {0, 1}, {1, 1}},   // Z
        {{-1, 0}, {0, 0}, {1, 0}, {-1, 1}},  // J
        {{-1, 0}, {0, 0}, {1, 0}, {1, 1}},   // L
    };
    private static final int[] COLORS = {0xFF35D9C4, 0xFFF5D547, 0xFFB9A6FF, 0xFF3DDC97, 0xFFFF5C7A, 0xFF5B8CFF, 0xFFFF9F43};

    private final Screen parent;
    private final Random rnd = new Random();
    private final int[][] grid = new int[ROWS][COLS];
    private int[][] cells;
    private int type, nextType, cx, cy;
    private int timer, score, lines, level = 1;
    private boolean over;

    public TetrisScreen(Screen parent) {
        super(Text.literal("Tetris"));
        this.parent = parent;
        nextType = rnd.nextInt(7);
        spawn();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    private void restart() {
        for (int[] row : grid) java.util.Arrays.fill(row, 0);
        score = 0;
        lines = 0;
        level = 1;
        over = false;
        nextType = rnd.nextInt(7);
        spawn();
    }

    private void spawn() {
        type = nextType;
        nextType = rnd.nextInt(7);
        cells = new int[4][2];
        for (int i = 0; i < 4; i++) cells[i] = SHAPES[type][i].clone();
        cx = 4;
        cy = 0;
        timer = 0;
        if (collides(cells, cx, cy)) over = true;
    }

    private boolean collides(int[][] cs, int px, int py) {
        for (int[] p : cs) {
            int gx = px + p[0];
            int gy = py + p[1];
            if (gx < 0 || gx >= COLS || gy >= ROWS) return true;
            if (gy >= 0 && grid[gy][gx] != 0) return true;
        }
        return false;
    }

    private void rotate() {
        if (type == 1) return;
        int[][] r = new int[4][2];
        for (int i = 0; i < 4; i++) {
            r[i][0] = -cells[i][1];
            r[i][1] = cells[i][0];
        }
        for (int kick : new int[]{0, -1, 1, -2, 2}) {
            if (!collides(r, cx + kick, cy)) {
                cells = r;
                cx += kick;
                return;
            }
        }
    }

    private boolean step() {
        if (!collides(cells, cx, cy + 1)) {
            cy++;
            return true;
        }
        for (int[] p : cells) {
            int gy = cy + p[1];
            if (gy >= 0) grid[gy][cx + p[0]] = type + 1;
        }
        int cleared = 0;
        for (int y = ROWS - 1; y >= 0; y--) {
            boolean full = true;
            for (int x = 0; x < COLS; x++) if (grid[y][x] == 0) { full = false; break; }
            if (full) {
                for (int yy = y; yy > 0; yy--) grid[yy] = grid[yy - 1].clone();
                grid[0] = new int[COLS];
                cleared++;
                y++; // dieselbe Zeile nochmal prüfen
            }
        }
        if (cleared > 0) {
            score += new int[]{0, 100, 300, 500, 800}[cleared] * level;
            lines += cleared;
            level = lines / 10 + 1;
        }
        spawn();
        return false;
    }

    @Override
    public void tick() {
        if (over) return;
        if (++timer >= Math.max(2, 12 - level)) {
            timer = 0;
            step();
        }
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int key = input.key();
        if (key == GLFW.GLFW_KEY_R) { restart(); return true; }
        if (over) return super.keyPressed(input);
        switch (key) {
            case GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_A -> { if (!collides(cells, cx - 1, cy)) cx--; return true; }
            case GLFW.GLFW_KEY_RIGHT, GLFW.GLFW_KEY_D -> { if (!collides(cells, cx + 1, cy)) cx++; return true; }
            case GLFW.GLFW_KEY_DOWN, GLFW.GLFW_KEY_S -> { if (step()) score++; return true; }
            case GLFW.GLFW_KEY_UP, GLFW.GLFW_KEY_W -> { rotate(); return true; }
            case GLFW.GLFW_KEY_SPACE -> {
                while (step()) score += 2;
                return true;
            }
            default -> { }
        }
        return super.keyPressed(input);
    }

    private void cell(DrawContext c, int x, int y, int s, int color) {
        c.fill(x, y, x + s, y + s, 0xFF05060A);
        c.fill(x + 1, y + 1, x + s - 1, y + s - 1, color);
        c.fill(x + 1, y + 1, x + s - 1, y + 2, 0x55FFFFFF);
        c.fill(x + 1, y + 1, x + 2, y + s - 1, 0x55FFFFFF);
        c.fill(x + 1, y + s - 2, x + s - 1, y + s - 1, 0x55000000);
        c.fill(x + s - 2, y + 1, x + s - 1, y + s - 1, 0x55000000);
    }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        c.fill(0, 0, width, height, 0xC0070812);
        int s = Math.max(6, Math.min((height - 50) / ROWS, 14));
        int bw = COLS * s;
        int bh = ROWS * s;
        int ox = width / 2 - bw / 2 - 40;
        int oy = (height - bh) / 2;
        Ui.panel(c, ox - 6, oy - 6, bw + 12, bh + 12, Ui.PANEL);
        c.fill(ox, oy, ox + bw, oy + bh, 0xFF07080D);

        for (int y = 0; y < ROWS; y++)
            for (int x = 0; x < COLS; x++)
                if (grid[y][x] != 0) cell(c, ox + x * s, oy + y * s, s, COLORS[grid[y][x] - 1]);
        if (!over)
            for (int[] p : cells) {
                int gy = cy + p[1];
                if (gy >= 0) cell(c, ox + (cx + p[0]) * s, oy + gy * s, s, COLORS[type]);
            }

        int sx = ox + bw + 20;
        Ui.panel(c, sx, oy, 100, 112, Ui.PANEL);
        Ui.text(c, "PUNKTE", sx + 10, oy + 10, Ui.MUTED);
        Ui.text(c, String.valueOf(score), sx + 10, oy + 21, Ui.TEXT);
        Ui.text(c, "LEVEL " + level, sx + 10, oy + 38, Ui.ACCENT2);
        Ui.text(c, "ZEILEN " + lines, sx + 10, oy + 50, Ui.TEXT);
        Ui.text(c, "NÄCHSTER", sx + 10, oy + 68, Ui.MUTED);
        for (int[] p : SHAPES[nextType]) cell(c, sx + 40 + p[0] * 8, oy + 88 + p[1] * 8, 8, COLORS[nextType]);

        if (over) {
            Ui.panel(c, width / 2 - 70 - 40, height / 2 - 20, 140, 40, Ui.PANEL2);
            Ui.centered(c, "GAME OVER", width / 2 - 40, height / 2 - 12, Ui.OK);
            Ui.centered(c, "R = neu starten", width / 2 - 40, height / 2 + 2, Ui.MUTED);
        } else {
            Ui.centered(c, "Pfeile/WASD, Leertaste, ESC", width / 2 - 40, oy + bh + 12, Ui.MUTED);
        }
    }
}
