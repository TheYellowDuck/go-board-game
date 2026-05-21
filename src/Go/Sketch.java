package Go;

import processing.core.PApplet;
import processing.core.PImage;

public class Sketch extends PApplet {

	static final int LEN = 600;
	static final int[][] DIRS = {{1,0},{-1,0},{0,1},{0,-1}};

	int player = 1;
	int[][] G = new int[19][19], v = new int[19][19];
	int[][] prevG = null;
	int consecutivePasses = 0;
	boolean gameOver = false;
	float blackScore, whiteScore;
	int[][] territory;

	public void startSketch() {
		String[] args = {this.getClass().getName()};
		PApplet.runSketch(args, this);
	}

	@Override
	public void settings() {
		size(LEN + LEN / 20, LEN + LEN / 20 + 70);
	}

	@Override
	public void setup() {
		PImage icon = loadImage("logo.png");
		surface.setIcon(icon);  // window icon: works on Windows, Linux, macOS
		// Dock/taskbar icon (Java 9+) — check support before calling
		try {
			if (java.awt.Taskbar.isTaskbarSupported()) {
				java.awt.Taskbar taskbar = java.awt.Taskbar.getTaskbar();
				if (taskbar.isSupported(java.awt.Taskbar.Feature.ICON_IMAGE))
					taskbar.setIconImage((java.awt.Image) icon.getNative());
			}
		} catch (Exception ignored) {}
	}

	@Override
	public void draw() {
		int margin = LEN / 20;
		int cell   = (LEN - margin) / 19;
		int barY   = LEN + margin;

		// ── Board ─────────────────────────────────────────────────────
		background(120, 82, 34);
		noStroke();
		fill(221, 176, 100);
		rect(margin / 2, margin / 2, LEN, LEN, 4);

		stroke(55, 33, 5, 210);
		strokeWeight(1);
		for (int i = cell; i <= LEN - margin; i += cell) {
			line(i, cell, i, LEN - margin);
			line(cell, i, LEN - margin, i);
		}

		noStroke();
		fill(48, 28, 4);
		int[] sp = {3, 9, 15};
		for (int si : sp)
			for (int sj : sp)
				circle(intToPx(si), intToPx(sj), 6);

		// ── Territory overlay (game over) ─────────────────────────────
		if (gameOver && territory != null) {
			noStroke();
			for (int i = 0; i < 19; i++) {
				for (int j = 0; j < 19; j++) {
					if (territory[i][j] == 1) {
						fill(0, 0, 0, 190);
						rect(intToPx(i) - 4, intToPx(j) - 4, 8, 8);
					} else if (territory[i][j] == 2) {
						fill(255, 255, 255, 190);
						rect(intToPx(i) - 4, intToPx(j) - 4, 8, 8);
					}
				}
			}
		}

		// ── Hover preview ─────────────────────────────────────────────
		if (!gameOver && mouseY < barY) {
			int hx = pxToInt(mouseX), hy = pxToInt(mouseY);
			if (hx >= 0 && hx < 19 && hy >= 0 && hy < 19 && G[hx][hy] == 0) {
				float dx = mouseX - intToPx(hx), dy = mouseY - intToPx(hy);
				if (dx * dx + dy * dy < (cell * 0.5f) * (cell * 0.5f)) {
					noStroke();
					if (player == 1) fill(0, 0, 0, 100);
					else             fill(255, 255, 255, 130);
					circle(intToPx(hx), intToPx(hy), cell * 0.9f);
				}
			}
		}

		// ── Stones ────────────────────────────────────────────────────
		noStroke();
		for (int i = 0; i < 19; i++)
			for (int j = 0; j < 19; j++)
				if (G[i][j] != 0)
					drawStone(intToPx(i), intToPx(j), cell * 0.93f, G[i][j]);

		// ── Status bar ────────────────────────────────────────────────
		noStroke();
		fill(38, 25, 9);
		rect(0, barY, width, 70);

		int btnH = 36, btnW = 75;
		int btnResetX = width - 15 - btnW;
		int btnPassX  = btnResetX - 8 - btnW;
		int btnY = barY + 17;

		if (gameOver) {
			fill(210, 190, 150);
			textSize(13);
			textAlign(LEFT, CENTER);
			String winner = blackScore > whiteScore ? "Black wins!" : "White wins!";
			text(String.format("Black: %.0f   White: %.1f   |   %s",
				blackScore, whiteScore, winner), 15, barY + 35);
		} else {
			drawStone(32, barY + 35, 20, player);
			fill(210, 190, 150);
			textSize(15);
			textAlign(LEFT, CENTER);
			text(player == 1 ? "Black's turn" : "White's turn", 48, barY + 35);

			// Pass button
			fill(72, 51, 18);
			stroke(150, 115, 55);
			strokeWeight(1);
			rect(btnPassX, btnY, btnW, btnH, 5);
			noStroke();
			fill(210, 190, 150);
			textSize(14);
			textAlign(CENTER, CENTER);
			text("Pass", btnPassX + btnW / 2f, btnY + btnH / 2f);
		}

		// Reset button (always visible)
		fill(72, 51, 18);
		stroke(150, 115, 55);
		strokeWeight(1);
		rect(btnResetX, btnY, btnW, btnH, 5);
		noStroke();
		fill(210, 190, 150);
		textSize(14);
		textAlign(CENTER, CENTER);
		text("Reset", btnResetX + btnW / 2f, btnY + btnH / 2f);
	}

	void drawStone(float cx, float cy, float d, int c) {
		noStroke();
		if (c == 1) {
			fill(0, 0, 0, 65);
			ellipse(cx + d * 0.07f, cy + d * 0.07f, d, d);
			fill(28, 28, 28);
			circle(cx, cy, d);
			fill(54, 54, 54);
			circle(cx - d * 0.13f, cy - d * 0.13f, d * 0.62f);
			fill(255, 255, 255, 50);
			ellipse(cx - d * 0.22f, cy - d * 0.27f, d * 0.3f, d * 0.2f);
		} else {
			fill(0, 0, 0, 50);
			ellipse(cx + d * 0.07f, cy + d * 0.07f, d, d);
			fill(206, 206, 206);
			circle(cx, cy, d);
			fill(243, 243, 243);
			circle(cx - d * 0.1f, cy - d * 0.12f, d * 0.72f);
			fill(255, 255, 255, 200);
			ellipse(cx - d * 0.22f, cy - d * 0.27f, d * 0.26f, d * 0.17f);
		}
	}

	@Override
	public void mouseClicked() {
		int margin = LEN / 20;
		int cell   = (LEN - margin) / 19;
		int barY   = LEN + margin;
		int btnH = 36, btnW = 75;
		int btnResetX = width - 15 - btnW;
		int btnPassX  = btnResetX - 8 - btnW;
		int btnY = barY + 17;

		// Reset button
		if (mouseX >= btnResetX && mouseX <= btnResetX + btnW
				&& mouseY >= btnY && mouseY <= btnY + btnH) {
			G = new int[19][19];
			v = new int[19][19];
			prevG = null;
			player = 1;
			consecutivePasses = 0;
			gameOver = false;
			territory = null;
			return;
		}

		if (gameOver) return;

		// Pass button
		if (mouseX >= btnPassX && mouseX <= btnPassX + btnW
				&& mouseY >= btnY && mouseY <= btnY + btnH) {
			consecutivePasses++;
			if (consecutivePasses >= 2) {
				endGame();
			} else {
				player = player == 1 ? 2 : 1;
			}
			return;
		}

		// Board click
		int bx = pxToInt(mouseX), by = pxToInt(mouseY);
		float dx = mouseX - intToPx(bx), dy = mouseY - intToPx(by);
		if (dx * dx + dy * dy < (cell * 0.5f) * (cell * 0.5f)
				&& bx >= 0 && by >= 0 && bx < 19 && by < 19 && G[bx][by] == 0) {
			int[][] savedG = copyBoard(G);
			G[bx][by] = player;
			check(bx, by, player);
			if (G[bx][by] != 0) {
				// Ko rule: reject if this recreates the board before the last move
				if (prevG != null && boardsEqual(G, prevG)) {
					G = savedG;
					return;
				}
				prevG = savedG;
				consecutivePasses = 0;
				player = player == 1 ? 2 : 1;
			}
		}
	}

	void endGame() {
		gameOver = true;
		territory = calcTerritory();
		float[] scores = calculateScore();
		blackScore = scores[0];
		whiteScore = scores[1];
	}

	// Flood-fill to find empty regions and their owning color (Chinese area scoring)
	int[][] calcTerritory() {
		int[][] terr = new int[19][19];
		boolean[][] visited = new boolean[19][19];
		int[] stackX = new int[361], stackY = new int[361];
		int[] regionX = new int[361], regionY = new int[361];

		for (int i = 0; i < 19; i++) {
			for (int j = 0; j < 19; j++) {
				if (!visited[i][j] && G[i][j] == 0) {
					int top = 0, regionSize = 0;
					boolean touchesBlack = false, touchesWhite = false;
					visited[i][j] = true;
					stackX[0] = i; stackY[0] = j; top = 1;

					while (top > 0) {
						top--;
						int x = stackX[top], y = stackY[top];
						regionX[regionSize] = x;
						regionY[regionSize] = y;
						regionSize++;
						for (int[] d : DIRS) {
							int nx = x + d[0], ny = y + d[1];
							if (nx < 0 || ny < 0 || nx > 18 || ny > 18) continue;
							if      (G[nx][ny] == 1) touchesBlack = true;
							else if (G[nx][ny] == 2) touchesWhite = true;
							else if (!visited[nx][ny]) {
								visited[nx][ny] = true;
								stackX[top] = nx; stackY[top] = ny; top++;
							}
						}
					}

					int owner = (touchesBlack && !touchesWhite) ? 1
					          : (touchesWhite && !touchesBlack) ? 2 : 0;
					for (int k = 0; k < regionSize; k++)
						terr[regionX[k]][regionY[k]] = owner;
				}
			}
		}
		return terr;
	}

	// Stones on board + enclosed empty territory + 7.5 komi for White
	float[] calculateScore() {
		int black = 0, white = 0;
		for (int i = 0; i < 19; i++)
			for (int j = 0; j < 19; j++) {
				if      (G[i][j] == 1 || territory[i][j] == 1) black++;
				else if (G[i][j] == 2 || territory[i][j] == 2) white++;
			}
		return new float[]{black, white + 7.5f};
	}

	int[][] copyBoard(int[][] src) {
		int[][] copy = new int[19][19];
		for (int i = 0; i < 19; i++) copy[i] = src[i].clone();
		return copy;
	}

	boolean boardsEqual(int[][] a, int[][] b) {
		for (int i = 0; i < 19; i++)
			for (int j = 0; j < 19; j++)
				if (a[i][j] != b[i][j]) return false;
		return true;
	}

	private void check(int b) {
		if (b == 0) {
			for (int i = 0; i < 19; i++)
				for (int j = 0; j < 19; j++)
					if (v[i][j] == 1) { G[i][j] = 0; v[i][j] = 0; }
		} else {
			for (int i = 0; i < 19; i++)
				for (int j = 0; j < 19; j++)
					v[i][j] = 0;
		}
	}

	public void check(int x, int y, int c) {
		v[x][y] = 2;
		if (x < 18 && G[x+1][y] == (c==1?2:1)) { if (checkh(x+1, y, (c==1?2:1))) check(0); else check(1); }
		if (x > 0  && G[x-1][y] == (c==1?2:1)) { if (checkh(x-1, y, (c==1?2:1))) check(0); else check(1); }
		if (y < 18 && G[x][y+1] == (c==1?2:1)) { if (checkh(x, y+1, (c==1?2:1))) check(0); else check(1); }
		if (y > 0  && G[x][y-1] == (c==1?2:1)) { if (checkh(x, y-1, (c==1?2:1))) check(0); else check(1); }
		v[x][y] = 0;
		if (checkh(x, y, c)) { check(0); } else { check(1); }
	}

	private boolean checkh(int x, int y, int c) {
		if (x < 0 || y < 0 || x > 18 || y > 18) return true;
		if (v[x][y] != 0) return true;
		if (G[x][y] == (c==1?2:1)) return true;
		v[x][y] = 1;
		if (G[x][y] == 0) return false;
		return checkh(x+1,y,c) && checkh(x-1,y,c) && checkh(x,y+1,c) && checkh(x,y-1,c);
	}

	public int intToPx(int i) {
		return (i + 1) * (LEN - LEN / 20) / 19;
	}

	public int pxToInt(int p) {
		return (int) Math.round((double) p / ((LEN - LEN / 20) / 19) - 1);
	}
}
