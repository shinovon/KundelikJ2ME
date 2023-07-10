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
	private boolean repaint;

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
				if("Invalid token!".equals(e.getMessage())) {
					Kun.accessToken = null;
					Kun.user = null;
					setScreen(new LoginScreen());
				} else {
					e.printStackTrace();
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
		try {
			while(Kun.running) {
				while(display.getCurrent() != canv) {
					Thread.sleep(500);
				}
				if(!scrolling) {
					repaint = false;
					_repaint();
					if(repaint) continue;
					synchronized (repaintLock) {
						repaintLock.wait(2000);
					}
					continue;
				}
				_repaint();
				limitFramerate();
			}
		} catch (InterruptedException e) {
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void limitFramerate() throws InterruptedException {
		int i = 30;
		i -= repaintTime;
		if(i > 0) Thread.sleep(i);
	}

	private void _repaint() {
		long time = System.currentTimeMillis();
		canv.updateScreen();
		repaintTime = (int) (System.currentTimeMillis() - time);
	}

	public void repaint() {
		if(display.getCurrent() != canv) return;
		repaint = true;
		synchronized (repaintLock) {
			repaintLock.notify();
		}
	}
	
	public void setScreen(UIScreen s) {
		display(null);
		if(current != null) {
			current.hide();
		}
		current = s;
		canv.resetScreen();
		repaint();
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
