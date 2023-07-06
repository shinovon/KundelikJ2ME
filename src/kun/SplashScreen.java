package kun;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class SplashScreen extends UIScreen {
	Image splash;
	
	protected SplashScreen() {
		super(null, null);
		try {
			splash = Image.createImage("/logo.png");
		} catch (Exception e) {
		}
	}

	protected void paint(Graphics g, int w, int h) {
		if(splash != null) {
			g.drawImage(splash, w >> 1, h >> 1, Graphics.VCENTER | Graphics.HCENTER);
		}
		g.setColor(0);
		g.drawString("типо кунделик", w >> 1, h - h / 6, Graphics.TOP | Graphics.HCENTER);
	}

}
