package kun;
/*
Copyright (c) 2022 Arman Jussupgaliyev

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.lcdui.Graphics;

public class KunCanvas extends GameCanvas implements UIConstants {
	
	private KunUI ui;
	int width;
	int height;
	
	private boolean pressed;
	private int pressX;
	private int pressY;
	private int lastX;
	private int lastY;
	private long pressTime;
	private long releaseTime;
	private boolean draggedMuch;
	private boolean scrollPreSlide;
	private float scrollSlideMaxTime;
	private float scrollSlideSpeed;
	private boolean scrollSlide;
	private boolean controlBlock;
	//private int flushTime;
	//private Keyboard keyboard;
	
	KunCanvas(KunUI ui) {
		super(false);
		this.ui = ui;
		super.setFullScreenMode(true);
		width = super.getWidth();
		height = super.getHeight();
		if(!super.hasPointerEvents()) {
			ui.keyInput = true;
		}
		updateScreen();
	}

	public void updateScreen() {
		Graphics g = getGraphics();
		g.setColor(-1);
		g.fillRect(0, 0, width, height);
		UIScreen s = ui.current;
		//long l = System.currentTimeMillis();
		if(s != null) {
			int h = height;
			if(!ui.keyInput && ui.scrolling) {
				if(!scrollPreSlide && (releaseTime - pressTime) > 0) {
					if (scrollSlide && Math.abs(scrollSlideSpeed) > 0.8F && (System.currentTimeMillis() - releaseTime) < scrollSlideMaxTime && s.scroll((int) scrollSlideSpeed)) {
						final float f = 0.967f;
						scrollSlideSpeed *= f;
					} else {
						scrollSlideSpeed = 0;
						ui.scrolling = false;
					}
				}
			}
			try {
				s.paint(g, width, h);
			} catch (Error e) {
				e.printStackTrace();
			}
		}
		//l = System.currentTimeMillis();
		flushGraphics();
		//flushTime = (int)(System.currentTimeMillis() - l);
	}
	
	public void resetScreen() {
	}
	
	private void tap(int x, int y, int time) {
		UIScreen s = ui.current;
		if(s != null) s.tap(x, y, time);
	}
	
	public void pointerPressed(int x, int y) {
		//if(keyboard != null && keyboard.pointerPressed(x, y)) return;
		ui.keyInput = false;
		pressed = true;
		lastX = pressX = x;
		lastY = pressY = y;
		pressTime = System.currentTimeMillis();
		draggedMuch = false;
		scrollSlide = false;
		scrollPreSlide = false;
		needRepaint();
	}
	
	public void pointerReleased(int x, int y) {
		//if(keyboard != null && keyboard.pointerReleased(x, y)) return;
		lastX = x;
		lastY = y;
		if(controlBlock) {
			needRepaint();
			return;
		}
		int time = (int) ((releaseTime = System.currentTimeMillis()) - pressTime);
		int dx = Math.abs(x - pressX);
		int dy = y - pressY;
		int ady = Math.abs(dy);
		if(pressed) {
			UIScreen s = ui.current;
			if(s != null) s.release(x, y);
			if(draggedMuch) {
				if(time < 200) {
					scrollSlide = scrollPreSlide;
					ui.scrolling = true;
				} else {
					scrollSlide = false;
				}
			} else {
				if(dx <= 6 && ady <= 6) {
					tap(x, y, time);
				} else if(time < 200 && dx < 12) {
					if(s != null) {
						s.scroll(-dy);
					}
				}
			}
			scrollPreSlide = false;
			pressed = false;
		}
		needRepaint();
	}

	public void pointerDragged(int x, int y) {
		//if(keyboard != null && keyboard.pointerDragged(x, y)) return;
		if(controlBlock) {
			needRepaint();
			return;
		}
		UIScreen s = ui.current;
		if(s != null && !s.blockScrolling()) {
			//dragTime = System.currentTimeMillis();
			final int sdX = Math.abs(pressX - x);
			final int sdY = Math.abs(pressY - y);
			final int dX = lastX - x;
			final int dY = lastY - y;
			final int adX = Math.abs(dX);
			final int adY = Math.abs(dY);
			// if(draggedMuch) {
			if(adY > 0 && adX < 16) {
				float f1 = 8F;
				float f2 = 2.5F;
				scrollPreSlide = true;
				scrollSlideMaxTime += (Math.abs(dY) / f1) * 2400F;
				float m = -dY / f2;
				// разные направления
				if(scrollSlideSpeed > 0 && m < 0 || scrollSlideSpeed < 0 && m > 0) scrollSlideSpeed = 0;
				if(Math.abs(scrollSlideSpeed) > 60) {
					scrollSlideSpeed *= 0.95;
					m *= 0.8;
				}
				scrollSlideSpeed += m;
				s.scroll(-dY);
				//preDrift += -deltaY;
				//if (adY < adX - 2) scrollHorizontally(dX);
			}
			// }
			if(sdY > 1 || sdX > 1) {
				draggedMuch = true;
				if(!ui.scrolling) {
					needRepaint();
					ui.scrolling = true;
				}
			}
		}
		lastX = x;
		lastY = y;
	}
	
	public void keyPressed(int i) { 
		UIScreen s = ui.current;
		if(s != null) {
			s.keyPress(i);
		}
		needRepaint();
	}
	
	public void keyReleased(int i) {
		UIScreen s = ui.current;
		if(s != null) {
			s.keyRelease(i);
		}
		needRepaint();
	}
	
	public void keyRepeated(int i) {
		UIScreen s = ui.current;
		if(s != null) {
			s.keyRepeat(i);
		}
		needRepaint();
	}
	
	protected void sizeChanged(int w, int h) {
		width = w;
		height = h;
		Util.testCanvas();
		if(ui != null) needRepaint();
	}

	private void needRepaint() {
		ui.repaint(false);
	}
	
	public void hideNotify() {
		if(ui.current != null) {
			ui.current.hide();
		}
	}
	
	public void showNotify() {
		if(ui.current != null) {
			ui.current.show();
		}
	}

	/*public void setKeyboard(Keyboard keyboard) {
		this.keyboard = keyboard;
	}

	public Keyboard getKeyboard() {
		return this.keyboard;
	}*/

}
