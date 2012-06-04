package circularcontainer;

import java.util.ArrayList;

import org.mt4j.components.MTComponent;
import org.mt4j.util.math.Vector3D;
/**
 * Circularizable is an interface to be implemented for the component entended to be nested in the CircularContainer.
 * @author Etienne Girot
 *
 */
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
	public Vector3D resize(CircularContainer c);


	/**
	 * Delegate interface of component to integrate them on circularContainer
	 */
	public void delegateInterface(CircularContainer c);
	
	/**
	 * Reset size and position of Component Interface item 
	 */
	public void recoverInterface();
	
	/**
	 * Reset size and position of the previously nested component
	 */
	public void recoverSizeAndPosition();
	public MTComponent getMyParent();
	public void giveBackToParent(MTComponent parent);
	
}
