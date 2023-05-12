package kun;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public class Button extends UIItem implements UIConstants {
	
	String text;
	protected boolean hover;
	private ButtonHandler buttonHandler;
	private int h;
	private long lastTime;

	public Button(String text, ButtonHandler buttonHandler) {
		this.text = text;
		this.buttonHandler = buttonHandler;
	}

	public void paint(Graphics g, int w, int x, int y, int sc) {
		g.setFont(mediumfont);
		if(hover)
			g.setColor(0xeeeeee);
		else
			g.setColor(-1);
		int tw = mediumfont.stringWidth(text)+8;
		int tx = (w-tw)>>1;
		g.fillRect(tx, y+1, tw, h-1);
		g.setColor(0);
		g.drawString(text, x + (w>>1), y + ((h - mediumfont.getHeight()) >> 1), Graphics.TOP | Graphics.HCENTER);
		g.setColor(0);
		g.drawRect(tx, y+1, tw-1, h-2);
		if(inFocus && ui.keyInput) {
			g.setColor(0xadadad);
			g.drawRect(tx, y+1, tw-1, h-2);
			//g.drawRect(x+1, y+1, w-3, h-3);
		}
	}

	public int getHeight() {
		return h;
	}

	protected void layout(int w) {
		h = Math.max(24, mediumfontheight + 8);
	}

	private void action() {
		if(System.currentTimeMillis()-lastTime < 500) return;
		lastTime = System.currentTimeMillis();
		if(buttonHandler != null) {
			buttonHandler.handleButton(this);
		}
	}
	
	protected void press(int x, int y) {
		hover();
	}
	
	protected void release(int x, int y) {
		unhover();
	}
	
	protected void tap(int x, int y, int time) {
		unhover();
		if(time <= 200 && time >= 5) {
			action();
		}
	}

	protected void keyPress(int i) {
		if(i == Canvas.FIRE || i == -5 || i == Canvas.KEY_NUM5) {
			hover();
			action();
		}
	}

	protected void keyRelease(int i) {
		if(i == Canvas.FIRE || i == -5 || i == Canvas.KEY_NUM5) {
			unhover();
		}
	}
	
	public void defocus() {
		super.defocus();
		hover = false;
	}

	protected void hover() {
		hover = true;
	}

	protected void unhover() {
		hover = false;
	}

}
