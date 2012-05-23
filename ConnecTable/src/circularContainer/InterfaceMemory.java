package circularcontainer;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;

import org.mt4j.components.MTComponent;
import org.mt4j.components.TransformSpace;
import org.mt4j.components.visibleComponents.shapes.AbstractShape;
import org.mt4j.components.visibleComponents.shapes.MTPolygon;
import org.mt4j.util.math.Vector3D;
/**
 * “nterfaceMemory is a class storing information about a component interface at the time of integration in the CircularContainer.
 * It is useful when getting back to the original container.
 * @author Etienne Girot
 *
 */
public class InterfaceMemory {
		private ArrayList<InterfaceItem> InterfaceItemList;
		private MTComponent myComponent;
	
		/**
		 * Constructor
		 * @param c component whose interface has to be kept
		 */
		public InterfaceMemory(MTComponent c){
			InterfaceItemList = new ArrayList<InterfaceItem>();
			myComponent = c;
			
		}
		
		/**
		 * Adding a interface item to be remembered
		 * @param C
		 */
		public void addItem(MTComponent C){
			InterfaceItem i = new InterfaceItem(C);
			InterfaceItemList.add(i);
		}
		
		/**
		 * Reset interface item original size and position
		 */
		public void recoverInterface(){
			Iterator it = InterfaceItemList.iterator();
			while(it.hasNext()){
				
				InterfaceItem current = (InterfaceItem) it.next();		
				
				myComponent.addChild(current.comp);
				current.recoverComponent();
				
				
				
			}
		}
		/**
		 * Helper class for storing and recovering position and size information
		 * @author Etienne Girot
		 *
		 */
		class InterfaceItem {
			MTComponent comp;
			MTPolygon Shape;
			Vector3D centerPoint;
			float height;
			float width;
			float angle;

			
			/**
			 * Constructor
			 * @param S
			 */
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
			
			/**
			 * Reset composent original size and position
			 */
			public void recoverComponent(){
				//System.out.println("recover center("+centerPoint.toString()+")/height("+height+")/width("+width+")" );
				
				Shape.setPositionGlobal(centerPoint);
				Shape.setHeightXYGlobal(height);
				Shape.setWidthXYGlobal(width);
				Shape.rotateZGlobal(centerPoint, (float) Math.toDegrees(angle*-1));
			}
			
		}
	
}
