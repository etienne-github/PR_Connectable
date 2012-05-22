package circularContainer;

import java.util.ArrayList;

import org.mt4j.components.MTComponent;
import org.mt4j.util.math.Vector3D;

public interface Circularizable {
	

	public void memorizeInterface();
	/**
	 * Memorize size and position of the component modified in resize() to reset them in recoverSizeAndPosition()
	 */
	public void memorizeSizeAndPosition();
	
	/**
	 * Resize component to fit greatest possible space on the circularContainer
	 * @return 
	 */
	public Vector3D resize(circularContainer c);

	/**
	 * Memorize the position, size etc. of component modified in delegateInterface to reset them in recoverInterface
	 */
	//public void memorizeInterface(circularContainer c);
	
	/**
	 * Delegate interface of component to integrate them on circularContainer
	 */
	public void delegateInterface(circularContainer c);
	
	/**
	 * Reset size and position of Component Interface item 
	 */
	public void recoverInterface();
	
	public void recoverSizeAndPosition();
	
}
