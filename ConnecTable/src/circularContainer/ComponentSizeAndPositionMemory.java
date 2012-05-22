package circularContainer;

import org.mt4j.components.TransformSpace;
import org.mt4j.components.visibleComponents.shapes.AbstractShape;
import org.mt4j.components.visibleComponents.shapes.MTPolygon;
import org.mt4j.util.math.Vector3D;

public class ComponentSizeAndPositionMemory {
	MTPolygon Shape;
	Vector3D centerPoint;
	float height;
	float width;
	float Zrotation;
	
	public float getOriginalRotation(){
		return Zrotation;
	}
	
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
        System.out.println("Rotation angle : "+Zrotation+"("+Math.toDegrees(Zrotation)+")" );
		
	}
	
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
