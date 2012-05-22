package advanced.physics;

import org.mt4j.MTApplication;
import org.mt4j.input.inputSources.MacTrackpadSource;

import advanced.physics.scenes.CircularShieldPhysicsScene;
import advanced.physics.scenes.PhysicsScene;
import advanced.physics.scenes.myPhysicsScene;

public class StartPhysicsExample extends MTApplication {
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		initialize();
	}
	
	@Override
	public void startUp() {
		//getInputManager().registerInputSource(new MacTrackpadSource(this));
		addScene(new CircularShieldPhysicsScene(this, "Physics Example Scene"));
	}

}
