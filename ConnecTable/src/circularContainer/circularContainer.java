package circularcontainer;

import java.awt.Component;
import java.io.File;



import org.mt4j.MTApplication;
import org.mt4j.components.MTComponent;
import org.mt4j.components.TransformSpace;
import org.mt4j.components.interfaces.IMTController;
import org.mt4j.components.visibleComponents.shapes.MTEllipse;
import org.mt4j.components.visibleComponents.shapes.MTPolygon;
import org.mt4j.components.visibleComponents.shapes.MTRectangle;
import org.mt4j.components.visibleComponents.widgets.MTSceneTexture;

import org.mt4j.input.gestureAction.DefaultDragAction;
import org.mt4j.input.gestureAction.InertiaCircuDragAction;
import org.mt4j.input.inputProcessors.IGestureEventListener;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragEvent;
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor;
import org.mt4j.input.inputProcessors.globalProcessors.CursorTracer;
import org.mt4j.sceneManagement.AbstractScene;
import org.mt4j.sceneManagement.Iscene;
import org.mt4j.util.MTColor;
import org.mt4j.util.math.ToolsMath;
import org.mt4j.util.math.Vector3D;

import pdfreader.MTImageButton;
import processing.core.PImage;
/**
 * The Class CircularContainer. A Scene that set a circularizable component or another scene in the middle of the screen with maximal size.
 * The container can be rotated and is specially convenient for circular screens.
 * @author Etienne Girot
 */
public class CircularContainer extends AbstractScene {


	private MTSceneTexture sceneTexture;
	private Vector3D center;
	private float sceneRadius;
	private MTApplication app;
	private MTComponent myCompParent;
	//StubInterfaceMemory myStubInterfaceMemory; /*Use if delegate interface bt copying interface items*/
	
	/**
	 * 
	 * @return the radius of the circular scene
	 */
	public float getSceneRadius() {
		return sceneRadius;
	}

	/**
	 * Constructor for nested component
	 * @param mtApplication
	 * @param name
	 * @param comp
	 */
	public CircularContainer(MTApplication mtApplication,String name, Circularizable comp){
		super(mtApplication,name);
		
		app=mtApplication;
		
		MTColor alphaZero = new MTColor(0,0,0);
		alphaZero.setAlpha(0);
		final Circularizable component = comp;
		myCompParent = comp.getMyParent();
		
		//Get tracer
		CursorTracer c = new CursorTracer(mtApplication, this);
		
		//Set background
		MTRectangle background = new MTRectangle(0,0,mtApplication.width, mtApplication.height , mtApplication);
		background.setStrokeColor(alphaZero);
		

		PImage backImage = mtApplication.loadImage("."+((String)File.separator)+"src"+((String)File.separator)+"circularcontainer"+((String)File.separator)+"data"+((String)File.separator)+"image"+((String)File.separator)+"circularContainer_background.jpg");
		background.setTexture(backImage);
		background.setPickable(false);
		getCanvas().addChild(background);
		
		//register to global input
		registerGlobalInputProcessor(c);
		
		//Set geometrical helper
		center = new Vector3D(mtApplication.width/2,mtApplication.height/2);		
		sceneRadius = mtApplication.width < mtApplication.height ? mtApplication.width/2f : mtApplication.height/2f;
		
		//Draw borders
		MTEllipse centerCircle = new MTEllipse(mtApplication, new Vector3D(mtApplication.width/2f, mtApplication.height/2f), sceneRadius, sceneRadius);
		centerCircle.setPickable(true);
		centerCircle.setNoFill(true);
//		centerCircle.setStrokeColor(new MTColor(0,0,0));
		centerCircle.setStrokeColor(new MTColor(0,0,0));
		centerCircle.setStrokeWeight(0.5f);
		centerCircle.removeAllGestureEventListeners();
		this.getCanvas().addChild(centerCircle);
		
		//Add listener on the external circle to rotate the scene canvas around the center
		centerCircle.addGestureListener(DragProcessor.class, new CircularContainerDragListener(centerCircle,this));
		
		//Add exit button
		MTImageButton exitButton=new MTImageButton(app.loadImage("."+((String)File.separator)+"src"+((String)File.separator)+"circularcontainer"+((String)File.separator)+"data"+((String)File.separator)+"image"+((String)File.separator)+"exit.png"
),app);
		exitButton.setNoStroke(true);
		getCanvas().addChild(exitButton);
		exitButton.setPositionGlobal(new Vector3D(mtApplication.width/2f,mtApplication.height/2f+sceneRadius-32));
		exitButton.registerInputProcessor(new TapProcessor(app));
		exitButton.addGestureListener(TapProcessor.class, new IGestureEventListener(){

			@Override
			public boolean processGestureEvent(MTGestureEvent ge) {
				TapEvent te = (TapEvent)ge;
				if (te.isTapped()){
					if (app.popScene()){

						component.recoverSizeAndPosition();
						component.recoverInterface();
						
						
						component.giveBackToParent(myCompParent);
						//app.getCurrentScene().getCanvas().addChild((MTComponent)component);
						
						
						//System.out.println("popped");
					}
				}
				
				return false;
			}});
		
		//Memorize configuration of the neasted component
		comp.memorizeInterface();
		comp.memorizeSizeAndPosition();
		
		Vector3D translationStore = new Vector3D();
        Vector3D rotationStore = new Vector3D();
        Vector3D scaleStore = new Vector3D();
        
        //Sync component angle with container angle
        ((MTComponent)component).getGlobalMatrix().decompose(translationStore, rotationStore, scaleStore);
       // System.out.println("Angle is:"+String.valueOf(ToolsMath.RAD_TO_DEG *rotationStore.getZ()));
        ((MTComponent)component).rotateZ(new Vector3D(translationStore.getX(),translationStore.getY()), (float) (ToolsMath.RAD_TO_DEG * rotationStore.getZ())*-1);
		
        
		//Resize component to fit the maximal size
		Vector3D dimension = comp.resize(this);
		
		//Move component interface item onto the container
		comp.delegateInterface(this);
		
		//Sync container angle with component angle - following
		this.getCanvas().rotateZ(center, (float) (ToolsMath.RAD_TO_DEG * rotationStore.getZ()));
		this.getCanvas().addChild((MTComponent)comp);
		

	
	}
	
/**
 * Constructor for nested scene.
 * @param mtApplication
 * @param name
 * @param theScene
 * @param fboWidth
 * @param fboHeight
 */
	public CircularContainer(MTApplication mtApplication, String name, Iscene theScene, int fboWidth, int fboHeight) {
		super(mtApplication, name);
		app=mtApplication;
		MTColor alphaZero = new MTColor(0,0,0);
		alphaZero.setAlpha(0);
		
		//get tracer
		CursorTracer c = new CursorTracer(mtApplication, this);
		
		//set Background
		MTRectangle background = new MTRectangle(0,0,mtApplication.width, mtApplication.height , mtApplication);
		background.setStrokeColor(alphaZero);
		PImage backImage = mtApplication.loadImage("."+((String)File.separator)+"src"+((String)File.separator)+"circularcontainer"+((String)File.separator)+"data"+((String)File.separator)+"image"+((String)File.separator)+"circularContainer_background.jpg");
		background.setTexture(backImage);
		background.setPickable(false);
		getCanvas().addChild(background);
		
		//register to input
		registerGlobalInputProcessor(c);
		
		//set geometrical helpers
		center = new Vector3D(mtApplication.width/2,mtApplication.height/2);	
		sceneRadius = mtApplication.width < mtApplication.height ? mtApplication.width/2f : mtApplication.height/2f;
		
		//draw scene border
		MTEllipse centerCircle = new MTEllipse(mtApplication, new Vector3D(mtApplication.width/2f, mtApplication.height/2f), sceneRadius, sceneRadius);

		centerCircle.setNoFill(true);

		centerCircle.setStrokeColor(new MTColor(0,0,0));
		centerCircle.setStrokeWeight(0.5f);
		this.getCanvas().addChild(centerCircle);
		
		//add listener to rotate the scene canvas
		centerCircle.removeAllGestureEventListeners();
		centerCircle.addGestureListener(DragProcessor.class, new CircularContainerDragListener(centerCircle,this));

		
		//nest scene
		sceneTexture=new MTSceneTexture(mtApplication,0,0,mtApplication.width ,mtApplication.height, theScene);

		

		sceneTexture.setStrokeColor(new MTColor(0,0,0));
		

		sceneTexture.setSizeXYGlobal(fboWidth, fboHeight);
		
		
		this.getCanvas().addChild(sceneTexture);
		

		
        //add exit button
		MTImageButton exitButton=new MTImageButton(app.loadImage("."+((String)File.separator)+"src"+((String)File.separator)+"circularcontainer"+((String)File.separator)+"data"+((String)File.separator)+"image"+((String)File.separator)+"exit.png"
),app);
		exitButton.setNoStroke(true);
		getCanvas().addChild(exitButton);
		exitButton.setPositionGlobal(new Vector3D(mtApplication.width/2f,mtApplication.height/2f+sceneRadius-32));
		exitButton.registerInputProcessor(new TapProcessor(app));
		exitButton.addGestureListener(TapProcessor.class, new IGestureEventListener(){

			@Override
			public boolean processGestureEvent(MTGestureEvent ge) {
				TapEvent te = (TapEvent)ge;
				if (te.isTapped()){
					if (app.popScene()){
					}
				}
				
				return false;
			}});
		
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutDown() {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
	/**
	 * CircularContainerDragListener, rotate the scene with inertia based on MTCircularList one.
	 * @author Etienne Girot
	 *
	 */
	private class CircularContainerDragListener implements IGestureEventListener{
		private MTEllipse theListCellContainer;
		private AbstractScene circularScene;
		private double angle = 1;
		
		private boolean canDrag;
		
		/**
		 * Constructor
		 * @param c
		 * @param s
		 */
		public CircularContainerDragListener(MTEllipse c, CircularContainer s){
			theListCellContainer=c;
			circularScene=s;
			//this.canDrag = false;
		}
		

		public boolean processGestureEvent(MTGestureEvent ge) {
			DragEvent de = (DragEvent)ge;
			
			Vector3D dir = de.getTranslationVect();
			dir.transformDirectionVector(theListCellContainer.getGlobalInverseMatrix());
			Vector3D to = new Vector3D(de.getTo().x -center.x, de.getTo().y - center.y, 0);
			Vector3D from = new Vector3D(de.getFrom().x - center.x, de.getFrom().y - center.y, 0);
			
			if(from.length() > 100)
			{
				switch (de.getId()) {
				case MTGestureEvent.GESTURE_DETECTED:
				case MTGestureEvent.GESTURE_UPDATED:
						angle = Math.atan2(to.y,to.x) - Math.atan2(from.y,from.x);
						//System.err.println("angle listener : "+angle);
						circularScene.getCanvas().rotateZ(center, (float) Math.toDegrees(angle));
					break;
				case MTGestureEvent.GESTURE_ENDED:
					Vector3D vel = de.getDragCursor().getVelocityVector(140);
					vel.scaleLocal(0.8f);
					vel = vel.getLimited(15);
					IMTController oldController = circularScene.getCanvas().getController();
					//theListCellContainer.setController(new InertiaListController(theListCellContainer, vel, oldController, (float) (Math.abs(angle)/angle)));
					circularScene.getCanvas().setController(new InertiaCircularContainerController(circularScene.getCanvas(), vel, oldController, (float) (Math.abs(angle)/angle)));

					break;
				default:
					break;
				}
			}
			return false;
		}
		
		
		
		
		
		/**
		 * The Class InertiaListController.
		 * Controller to add an inertia scrolling after scrolling/dragging the list content.
		 * 
		 * @author Christopher Ruff
		 */
		private class InertiaCircularContainerController implements IMTController{
			private MTComponent target;
			private Vector3D startVelocityVec;
			private float dampingValue = 0.95f;
			private float rotationDir;
//			private float dampingValue = 0.80f;
			
			private IMTController oldController;
			
			public InertiaCircularContainerController(MTComponent target, Vector3D startVelocityVec, IMTController oldController, float dir) {
				super();
				this.target = target;
				this.startVelocityVec = startVelocityVec;
				this.oldController = oldController;
				this.rotationDir = dir;
				
			//	System.err.println("vel :"+startVelocityVec);
//				System.out.println(startVelocityVec);
				//Animation inertiaAnim = new Animation("Inertia anim for " + target, new MultiPurposeInterpolator(startVelocityVec.length(), 0, 100, 0.0f, 0.5f, 1), target);
			}
			
			public void update(long timeDelta) {
				if (false){//
					//theListCellContainer.isDragging();
					startVelocityVec.setValues(Vector3D.ZERO_VECTOR);
					target.setController(oldController);
					return;
				}
				
				if (Math.abs(startVelocityVec.x) < 0.05f && Math.abs(startVelocityVec.y) < 0.05f){
					startVelocityVec.setValues(Vector3D.ZERO_VECTOR);
					target.setController(oldController);
					return;
				}
				startVelocityVec.scaleLocal(dampingValue);
				
				Vector3D transVect = new Vector3D(startVelocityVec);
				transVect.transformDirectionVector(theListCellContainer.getGlobalInverseMatrix());
				//System.out.println(rotationDir);
				//theListCellContainer.translate(new Vector3D(0, transVect.y), TransformSpace.LOCAL);
				float angle=((rotationDir * transVect.length())/2);
				//System.err.println("angle cont "+angle);
				if(!Float.isNaN(angle)){
					circularScene.getCanvas().rotateZGlobal(center, angle);
				}
				
				if (oldController != null){
					oldController.update(timeDelta);
				}
			}
		}
		
	}
}
	//-------------------------------------------
	
	
	
	
	
	
	
	/*
	
	
	
	
	private class CircularContainerDragListener implements IGestureEventListener{
		private AbstractScene circularScene;
		private double angle = 1;
		
		private boolean canDrag;
		
		public CircularContainerDragListener(AbstractScene scene){
			this.circularScene = scene;
			//this.canDrag = false;
		}
		
		public boolean processGestureEvent(MTGestureEvent ge) {
			DragEvent de = (DragEvent)ge;
			
			Vector3D dir = de.getTranslationVect();
			//Transform the global direction vector into listCellContainer local coordiante space
			dir.transformDirectionVector(circularScene.getCanvas().getGlobalInverseMatrix());
			Vector3D to = new Vector3D(de.getTo().x - center.x, de.getTo().y - center.y, 0);
			Vector3D from = new Vector3D(de.getFrom().x - center.x, de.getFrom().y - center.y, 0);
			
			if(from.length() > 200)
			{
				switch (de.getId()) {
				case MTGestureEvent.GESTURE_DETECTED:
					//System.out.println("Gesture detected");
				case MTGestureEvent.GESTURE_UPDATED:
					//System.out.println("Gesture updated");
						angle = Math.atan2(to.y,to.x) - Math.atan2(from.y,from.x);
						circularScene.getCanvas().rotateZ(center, (float) (ToolsMath.RAD_TO_DEG * angle));
					break;
				case MTGestureEvent.GESTURE_ENDED:
					//System.out.println("Gesture ended");
					Vector3D vel = de.getDragCursor().getVelocityVector(140);
					vel.scaleLocal(0.8f);
					vel = vel.getLimited(15);
					//TODO add inertia
					//IMTController oldController = circularScene.getController();
					//circularScene.setController(new InertiaListController(circularScene, vel, oldController, (float) (Math.abs(angle)/angle)));
					break;
				default:
					break;
				}
			}
			return false;
		}
		
		
		public void	memorizeStubItem(MTPolygon StubInterfaceItem){
			myStubInterfaceMemory.addItem(StubInterfaceItem);
		}
		
		
		
		
		

		
		
		
		
		
		
		
		
		
		*//**
		 * The Class InertiaListController.
		 * Controller to add an inertia scrolling after scrolling/dragging the list content.
		 * 
		 * @author Christopher Ruff
		 *//*
		private class InertiaCircularContainerController implements IMTController{
			private MTComponent target;
			private Vector3D startVelocityVec;
			private float dampingValue = 0.95f;
			private float rotationDir;
//			private float dampingValue = 0.80f;
			
			private IMTController oldController;
			
			public InertiaCircularContainerController(MTComponent target, Vector3D startVelocityVec, IMTController oldController, float dir) {
				super();
				this.target = target;
				this.startVelocityVec = startVelocityVec;
				this.oldController = oldController;
				this.rotationDir = dir;
				
//				System.out.println(startVelocityVec);
				//Animation inertiaAnim = new Animation("Inertia anim for " + target, new MultiPurposeInterpolator(startVelocityVec.length(), 0, 100, 0.0f, 0.5f, 1), target);
			}
			
			public void update(long timeDelta) {
				if (true){ //TODO implement method is dragging as in MTCircularList
					startVelocityVec.setValues(Vector3D.ZERO_VECTOR);
					target.setController(oldController);
					return;
				}
				
				if (Math.abs(startVelocityVec.x) < 0.05f && Math.abs(startVelocityVec.y) < 0.05f){0.05
					startVelocityVec.setValues(Vector3D.ZERO_VECTOR);
					target.setController(oldController);
					return;
				}
				startVelocityVec.scaleLocal(dampingValue);
				
				Vector3D transVect = new Vector3D(startVelocityVec);
				transVect.transformDirectionVector(circularScene.getCanvas().getGlobalInverseMatrix());
				//System.out.println(rotationDir);
				//theListCellContainer.translate(new Vector3D(0, transVect.y), TransformSpace.LOCAL);
				circularScene.getCanvas().rotateZGlobal(center, (rotationDir * transVect.length())/2);
				if (oldController != null){
					oldController.update(timeDelta);
				}
			}
		}
		
	}

}
*/