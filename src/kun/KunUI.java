package kun;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

public class KunUI implements Runnable {
	
	public static final Display display = Display.getDisplay(Kun.midlet);
	
	static KunUI inst;
	static KunCanvas canv;

	static int width;
	static int height;
	
	protected UIScreen current;

	public boolean scrolling;
	public int repaintTime;
	private Object repaintLock = new Object();
	private Object repaintResLock = new Object();

	private Thread repaintThread = new Thread(this);

	public boolean keyInput;

	public KunUI() {
		inst = this;
	}
	
	public void init() {
		canv = new KunCanvas(this);
		repaintThread.start();
		setScreen(new SplashScreen());
	}
	
	public void postInit() {
		if(Kun.accessToken != null) {
			try {
				loggedIn();
			} catch (RuntimeException e) {
				if(e.getMessage().equals("Invalid token!")) {
					Kun.accessToken = null;
					Kun.user = null;
					setScreen(new LoginScreen());
				}
			}
		} else {
			setScreen(new LoginScreen());
		}
	}

	public void loggedIn() {
		Kun.loggedInit();
		setScreen(new UserfeedScreen());
	}
	
	public void run() {
		boolean wasScrolling = false;
		try {
			while(Kun.running) {
				while(display.getCurrent() != canv) {
					Thread.sleep(100);
				}
				if(!scrolling) { 
					if(wasScrolling) {
						_repaint();
						wasScrolling = false;
					}
					synchronized (repaintLock) {
						repaintLock.wait(1000);
					}
				}
				_repaint();
				if(scrolling) {
					wasScrolling = true;
				} else {
					synchronized (repaintResLock) {
						repaintResLock.notify();
					}
				}
				waitRepaint();
			}
		} catch (InterruptedException e) {
		}
	}

	private void waitRepaint() throws InterruptedException {
		int i = 30;
		i -= repaintTime;
		if(i > 0) Thread.sleep(i);
	}

	private void _repaint() {
		long time = System.currentTimeMillis();
		canv.updateScreen();
		repaintTime = (int) (System.currentTimeMillis() - time);
	}

	public void repaint(boolean wait) {
		if(display.getCurrent() != canv || scrolling) return;
		synchronized (repaintLock) {
			repaintLock.notify();
		}
		if(wait) {
			try {
				synchronized (repaintResLock) {
					repaintResLock.wait(1000);
				}
			} catch (Exception e) {
			}
		}
	}
	
	public void setScreen(UIScreen s) {
		display(null);
		if(current != null) {
			current.hide();
		}
		current = s;
		canv.resetScreen();
		repaint(true);
		s.show();
	}

	public void display(Displayable d) {
		if(d == null) {
			if(display.getCurrent() == canv) return;
			display.setCurrent(canv);
			if(current != null) {
				current.show();
			}
			return;
		}
		if(!(d instanceof Alert)) {
			display.setCurrent(d);
		} else {
			display.setCurrent((Alert) d, canv);
		}
	}
	
	public int getWidth() {
		return canv.width;
	}
	
	public int getHeight() {
		return canv.height;
	}

}
