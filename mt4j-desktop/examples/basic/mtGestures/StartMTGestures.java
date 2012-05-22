package basic.mtGestures;

import org.mt4j.MTApplication;
import org.mt4j.input.inputSources.MacTrackpadSource;

public class StartMTGestures extends MTApplication {
	private static final long serialVersionUID = 1L;
	
	public static void main(String[] args) {
		initialize();
	}
	@Override
	public void startUp() {
		getInputManager().registerInputSource(new MacTrackpadSource(this));

		addScene(new MTGesturesExampleScene(this, "Multi-touch Gestures Example Scene"));
	}
}
