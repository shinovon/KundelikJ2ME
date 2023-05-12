package kun;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

public class TestCanvas extends Canvas {
	
	public TestCanvas() {
		setFullScreenMode(true);
	}

	protected void paint(Graphics g) {
	}
	
	public void pointerDragged(int x, int y) {}
	public void pointerPressed(int x, int y) {}
	public void pointerReleased(int x, int y) {}

}