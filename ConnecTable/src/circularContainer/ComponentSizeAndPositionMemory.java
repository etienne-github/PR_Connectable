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
	
	public ComponentSizeAndPositionMemory(MTPolygon S){
		Shape=S;
		centerPoint = S.getCenterPointGlobal();
		height = S.getHeightXY(TransformSpace.GLOBAL);
		width = S.getWidthXY(TransformSpace.GLOBAL);
	}
	
	public void recoverSizeAndPosition(){
		Shape.setPositionGlobal(centerPoint);
		Shape.setHeightXYGlobal(height);
		Shape.setWidthXYGlobal(width);
	}
}
