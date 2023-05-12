package kun;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.TextBox;

public class TextInput extends UIItem implements UIConstants, CommandListener {

	private static final Command okCmd = new Command("OK", Command.OK, 1);
	
	private int h;
	private String text = "";
	private String hint = "";

	public TextInput() {
	}

	public TextInput(String hint) {
		this.hint = hint;
	}

	public void paint(Graphics g, int w, int x, int y, int sc) {
		g.setFont(mediumfont);
		g.setColor(-1);
		g.fillRect(x, y, w, h);
		g.setColor(text.length() > 0 ? 0 : 0x555555);
		String s = text.length() > 0 ? text : hint;
		g.drawString(s, x + 1, y + ((h - mediumfontheight) >> 1), 0);
		g.setColor(0);
		g.drawRect(x, y, w-1, h-2);
		if(inFocus && ui.keyInput) {
			g.setColor(0xadadad);
			g.drawRect(x, y, w-1, h-2);
		}
	}

	public int getHeight() {
		return h;
	}

	protected void layout(int w) {
		h = Math.max(24, mediumfontheight + 8);
	}

	public String getText() {
		return text;
	}

	private void open() {
		TextBox t = new TextBox("", "", 1000, javax.microedition.lcdui.TextField.ANY);
		t.setString(text);
		t.setCommandListener(this);
		t.addCommand(okCmd);
		ui.display(t);
		return;
	}
	
	protected void tap(int x, int y, int time) {
		open();
	}

	protected void keyPress(int i) {
		if(i == -5 || i == Canvas.KEY_NUM5) {
			open();
		}
	}

	public void commandAction(Command c, Displayable d) {
		text = ((TextBox)d).getString();
		ui.display(null);
	}

}
