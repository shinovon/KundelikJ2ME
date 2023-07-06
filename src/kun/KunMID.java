package kun;

import javax.microedition.midlet.MIDlet;

public class KunMID extends MIDlet {

	private boolean started;
	
	public KunMID() {
		Kun.midlet = this;
	}

	protected void destroyApp(boolean b) {
		Kun.running = false;
	}

	protected void pauseApp() {
	}

	protected void startApp() {
		if(started) {
			return;
		}
		started = true;
		Kun.startApp();
	}

}
