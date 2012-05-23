package circularcontainer;

import org.mt4j.components.TransformSpace;
import org.mt4j.components.visibleComponents.shapes.AbstractShape;
import org.mt4j.components.visibleComponents.shapes.MTPolygon;
import org.mt4j.util.math.Vector3D;

/**
 * ComponentSizeAndPositionMemory is a class storing information about a component at the time of integration in the CircularContainer.
 * It is useful when getting back to the original container.
 * @author Etienne Girot
 *
 */
public class ComponentSizeAndPositionMemory {
	private MTPolygon Shape;
	private Vector3D centerPoint;
	private float height;
	private float width;
	private float Zrotation;
	
	/**
	 * 
	 * @return original rotation of the nested component
	 */
	public float getOriginalRotation(){
		return Zrotation;
	}
	
	/**
	 * Stores the size and position of the component.
	 * @param S the component
	 */
	public ComponentSizeAndPositionMemory(MTPolygon S){
		Shape=S;
		centerPoint = S.getCenterPointGlobal();
		height = S.getHeightXY(TransformSpace.GLOBAL);
		width = S.getWidthXY(TransformSpace.GLOBAL);
		Vector3D translationStore = new Vector3D();
        Vector3D rotationStore = new Vector3D();
        Vector3D scaleStore = new Vector3D();
        S.getGlobalMatrix().decompose(translationStore, rotationStore, scaleStore);
        Zrotation=rotationStore.z;
        //System.out.println("Rotation angle : "+Zrotation+"("+Math.toDegrees(Zrotation)+")" );
		
	}
	
	/**
	 * Reset the size and position of the proviousle nested component
	 * @param keepLastAngle whether the original or last angles has to be kept
	 */
	public void recoverSizeAndPosition(boolean keepLastAngle){
		Shape.setPositionGlobal(centerPoint);
		Shape.setHeightXYGlobal(height);
		Shape.setWidthXYGlobal(width);
		if(keepLastAngle){
			Vector3D translationStore = new Vector3D();
	        Vector3D rotationStore = new Vector3D();
	        Vector3D scaleStore = new Vector3D();
	        Shape.getGlobalMatrix().decompose(translationStore, rotationStore, scaleStore);
	       float a =rotationStore.z;
	       Shape.rotateZGlobal(centerPoint, (float) Math.toDegrees(a));
		}else{
			Shape.rotateZGlobal(centerPoint, (float) Math.toDegrees(Zrotation));

		}
		
	}
}
