package kun;

import javax.microedition.lcdui.Graphics;

public abstract class UIScreen implements UIConstants {
	
	static {
		if(KunUI.inst == null) {
			System.out.println("UIScreen class initialized before AppUI?!?!");
		}
	}
	
	protected static KunUI ui = KunUI.inst;
	
	private String label;
	protected UIScreen parent;
	
	protected float scroll;
	protected int width;
	protected int height;

	protected UIScreen(String label, UIScreen parent) {
		this.label = label;
		this.parent = parent;
	}
	
	protected abstract void paint(Graphics g, int w, int h);
	
	public String getTitle() {
		return label;
	}
	
	public void setTitle(String s) {
		this.label = s;
	}
	
	public UIScreen getParent() {
		return parent;
	}
	
	public boolean backCommand() {
		return parent != null;
	}
	
	public void repaint() {
		ui.repaint();
	}
	
	public void repaint(UIItem item) {
		ui.repaint();
	}

	public int getHeight() {
		return height;
	}


	protected void press(int x, int y) {}
	protected void release(int x, int y) {}
	protected void tap(int x, int y, int time) {}
	protected void keyPress(int i) {}
	protected void keyRelease(int i) {}
	protected void keyRepeat(int i) {}
	
	/** -1: назад */
	public void screenCommand(int i) {}
	
	/**
	 * @return true если успешно, false если уже некуда скроллить
	 */
	protected boolean scroll(int units) {
		return false;
	}

	public int getWidth() {
		return width;
	}

	public boolean hasScrollBar() {
		return false;
	}

	public void setScrollBarY(int y) {
	}
	
	protected void relayout() {
	}

	protected boolean isItemSeenOnScreen(UIItem i) {
		return true;
	}

	protected void show() {}

	protected void hide() {}
	
	public boolean blockScrolling() {
		return false;
	}

}
