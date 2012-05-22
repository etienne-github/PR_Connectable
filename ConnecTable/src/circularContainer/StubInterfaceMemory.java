package circularContainer;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;

import org.mt4j.components.MTComponent;
import org.mt4j.components.TransformSpace;
import org.mt4j.components.visibleComponents.shapes.AbstractShape;
import org.mt4j.components.visibleComponents.shapes.MTPolygon;
import org.mt4j.util.math.Vector3D;

public class StubInterfaceMemory {
		ArrayList<MTPolygon> StubInterfaceitemList;
		circularContainer myContainer;
	
		public StubInterfaceMemory(circularContainer c){
			StubInterfaceitemList = new ArrayList<MTPolygon>();
			myContainer = c;
			
		}
		
		public void addItem(MTPolygon StubInterfaceItem){
			StubInterfaceitemList.add(StubInterfaceItem);
		}
		
		public void recoverInterface(){
			Iterator it = StubInterfaceitemList.iterator();
			while(it.hasNext()){
				myContainer.getCanvas().removeChild(myContainer.getCanvas().getChildIndexOf((MTComponent) it.next()));
			}
		}
		
		class StubInterfaceItem {
			MTPolygon Shape;
			Vector3D centerPoint;
			float height;
			float width;

			
			public StubInterfaceItem(MTPolygon S){
				Shape = S;
				centerPoint = S.getCenterPointGlobal();
				width = S.getWidthXY(TransformSpace.GLOBAL);
				height = S.getHeightXY(TransformSpace.GLOBAL);
			}
			
			public void recoverComponent(){
				Shape.setPositionGlobal(centerPoint);
				Shape.setHeightXYGlobal(height);
				Shape.setWidthXYGlobal(width);
			}
			
		}
	
}
