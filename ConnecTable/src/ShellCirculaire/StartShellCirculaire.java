package ShellCirculaire;

import org.mt4j.MTApplication;
import org.mt4j.input.inputSources.MacTrackpadSource;

public class StartShellCirculaire extends MTApplication {
	private static final long serialVersionUID = 1L;

	public static void main(String args[]){
		initialize();
	}
	
	public void startUp(){
		//getInputManager().registerInputSource(new MacTrackpadSource(this));
		this.addScene(new MTShellCirculaire(this, "Multi-Touch Shell Scene"));
	}
	
}
