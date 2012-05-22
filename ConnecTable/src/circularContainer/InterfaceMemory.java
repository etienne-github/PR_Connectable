package circularContainer;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;

import org.mt4j.components.MTComponent;
import org.mt4j.components.TransformSpace;
import org.mt4j.components.visibleComponents.shapes.AbstractShape;
import org.mt4j.components.visibleComponents.shapes.MTPolygon;
import org.mt4j.util.math.Vector3D;

public class InterfaceMemory {
		ArrayList<InterfaceItem> InterfaceItemList;
		MTComponent myComponent;
	
		public InterfaceMemory(MTComponent c){
			InterfaceItemList = new ArrayList<InterfaceItem>();
			myComponent = c;
			
		}
		
		public void addItem(MTComponent C){
			InterfaceItem i = new InterfaceItem(C);
			InterfaceItemList.add(i);
		}
		
		public void recoverInterface(){
			Iterator it = InterfaceItemList.iterator();
			while(it.hasNext()){
				
				InterfaceItem current = (InterfaceItem) it.next();		
				
				myComponent.addChild(current.comp);
				current.recoverComponent();
				
				
				
			}
		}
		
		class InterfaceItem {
			MTComponent comp;
			MTPolygon Shape;
			Vector3D centerPoint;
			float height;
			float width;
			float angle;

			
			public InterfaceItem(MTComponent S){
				
				comp=S;
				Shape = (MTPolygon) S;
				//centerPoint=Shape.getCenterPointLocal();
				centerPoint = Shape.getCenterPointGlobal();
				width = Shape.getWidthXY(TransformSpace.GLOBAL);
				height = Shape.getHeightXY(TransformSpace.GLOBAL);
				
				Vector3D translationStore = new Vector3D();
		        Vector3D rotationStore = new Vector3D();
		        Vector3D scaleStore = new Vector3D();
		        S.getGlobalMatrix().decompose(translationStore, rotationStore, scaleStore);
		        angle=rotationStore.z;
				
				
				//System.err.println("add comp "+S.getName()+toString()+" pos("+centerPoint+")");
			}
			
			public void recoverComponent(){
				//System.out.println("recover center("+centerPoint.toString()+")/height("+height+")/width("+width+")" );
				
				Shape.setPositionGlobal(centerPoint);
				Shape.setHeightXYGlobal(height);
				Shape.setWidthXYGlobal(width);
				Shape.rotateZGlobal(centerPoint, (float) Math.toDegrees(angle*-1));
			}
			
		}
	
}
