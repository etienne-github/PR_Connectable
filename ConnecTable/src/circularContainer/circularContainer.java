package circularContainer;

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

import pdfReader.MTImageButton;
import processing.core.PImage;

public class circularContainer extends AbstractScene {

	
	private MTSceneTexture sceneTexture;
	private Vector3D center;
	private float sceneRadius;
	MTApplication app;
	//StubInterfaceMemory myStubInterfaceMemory;
	
	public float getSceneRadius() {
		return sceneRadius;
	}

	//Constuctor for neasted component
	public circularContainer(MTApplication mtApplication,String name, Circularizable comp){
		super(mtApplication,name);
		
		app=mtApplication;
		MTColor alphaZero = new MTColor(0,0,0);
		alphaZero.setAlpha(0);
		final Circularizable component = comp;
		

		CursorTracer c = new CursorTracer(mtApplication, this);
		MTRectangle background = new MTRectangle(0,0,mtApplication.width, mtApplication.height , mtApplication);
		background.setStrokeColor(alphaZero);
		PImage backImage = mtApplication.loadImage("."+((String)File.separator)+"src"+((String)File.separator)+"circularContainer"+((String)File.separator)+"data"+((String)File.separator)+"image"+((String)File.separator)+"circularContainer_background.jpg");
		background.setTexture(backImage);
		background.setPickable(false);
		getCanvas().addChild(background);
		registerGlobalInputProcessor(c);
		center = new Vector3D(mtApplication.width/2,mtApplication.height/2);
		
		sceneRadius = mtApplication.width < mtApplication.height ? mtApplication.width/2f : mtApplication.height/2f;
		
		MTEllipse centerCircle = new MTEllipse(mtApplication, new Vector3D(mtApplication.width/2f, mtApplication.height/2f), sceneRadius, sceneRadius);
		centerCircle.setPickable(false);
		centerCircle.setNoFill(true);
//		centerCircle.setStrokeColor(new MTColor(0,0,0));
		centerCircle.setStrokeColor(new MTColor(0,0,0));
		centerCircle.setStrokeWeight(0.5f);
		this.getCanvas().addChild(centerCircle);

		MTImageButton exitButton=new MTImageButton(app.loadImage("."+((String)File.separator)+"src"+((String)File.separator)+"circularContainer"+((String)File.separator)+"data"+((String)File.separator)+"image"+((String)File.separator)+"exit.png"
),app);
		exitButton.setNoStroke(true);
		getCanvas().addChild(exitButton);
		exitButton.setPositionGlobal(new Vector3D(mtApplication.width/2f,50));
		exitButton.registerInputProcessor(new TapProcessor(app));
		exitButton.addGestureListener(TapProcessor.class, new IGestureEventListener(){

			@Override
			public boolean processGestureEvent(MTGestureEvent ge) {
				TapEvent te = (TapEvent)ge;
				if (te.isTapped()){
					if (app.popScene()){

						component.recoverSizeAndPosition();
						component.recoverInterface();
						
						
						
						app.getCurrentScene().getCanvas().addChild((MTComponent)component);
						
						
						System.out.println("popped");
					}
				}
				
				return false;
			}});
		
		comp.memorizeInterface();
		comp.memorizeSizeAndPosition();
		
		Vector3D translationStore = new Vector3D();
        Vector3D rotationStore = new Vector3D();
        Vector3D scaleStore = new Vector3D();
        
        ((MTComponent)component).getGlobalMatrix().decompose(translationStore, rotationStore, scaleStore);
        System.out.println("Angle is:"+String.valueOf(ToolsMath.RAD_TO_DEG *rotationStore.getZ()));
        ((MTComponent)component).rotateZ(new Vector3D(translationStore.getX(),translationStore.getY()), (float) (ToolsMath.RAD_TO_DEG * rotationStore.getZ())*-1);
		
        
		
		Vector3D dimension = comp.resize(this);
		comp.delegateInterface(this);
		
		//Sync container angle with component angle
		this.getCanvas().rotateZ(center, (float) (ToolsMath.RAD_TO_DEG * rotationStore.getZ()));
		this.getCanvas().addChild((MTComponent)comp);
		

		//Set rotation area in accordance with neasted component dimension
		final MTRectangle rotationAreaLeft = new MTRectangle((mtApplication.width/2)-dimension.x/2f-(sceneRadius-dimension.x/2), (mtApplication.height/2)-dimension.y/2, (sceneRadius-dimension.x/2), dimension.y, mtApplication); 
		final MTRectangle rotationAreaRight = new MTRectangle((mtApplication.width/2)+dimension.x/2, (mtApplication.height/2)-dimension.y/2, (sceneRadius-dimension.x/2), dimension.y, mtApplication); 

		//Set rotation area invisible
		rotationAreaLeft.setFillColor(alphaZero);
		rotationAreaRight.setFillColor(alphaZero);
		rotationAreaRight.setStrokeColor(alphaZero);
		rotationAreaLeft.setStrokeColor(alphaZero);
		
		//Set Gesture listener
		rotationAreaRight.removeAllGestureEventListeners(); 
		rotationAreaLeft.removeAllGestureEventListeners(); 
		
        rotationAreaRight.addGestureListener(DragProcessor.class, new CircularContainerDragListener(this));
        rotationAreaLeft.addGestureListener(DragProcessor.class, new CircularContainerDragListener(this));
		
		//Add them on the container
		getCanvas().addChild(rotationAreaRight); 
        getCanvas().addChild(rotationAreaLeft); 
		
	
	}
	
	public circularContainer(MTApplication mtApplication, String name, Iscene theScene, int fboWidth, int fboHeight) {
		super(mtApplication, name);
		
		MTColor alphaZero = new MTColor(0,0,0);
		alphaZero.setAlpha(0);
		
		//System.out.println("circularScene created");
		CursorTracer c = new CursorTracer(mtApplication, this);
		MTRectangle background = new MTRectangle(0,0,mtApplication.width, mtApplication.height , mtApplication);
		background.setStrokeColor(alphaZero);
		PImage backImage = mtApplication.loadImage("."+((String)File.separator)+"src"+((String)File.separator)+"circularContainer"+((String)File.separator)+"data"+((String)File.separator)+"image"+((String)File.separator)+"circularContainer_background.jpg");
		background.setTexture(backImage);
		background.setPickable(false);
		getCanvas().addChild(background);
		registerGlobalInputProcessor(c);
		center = new Vector3D(mtApplication.width/2,mtApplication.height/2);
		
		sceneRadius = mtApplication.width < mtApplication.height ? mtApplication.width/2f : mtApplication.height/2f;
		
		MTEllipse centerCircle = new MTEllipse(mtApplication, new Vector3D(mtApplication.width/2f, mtApplication.height/2f), sceneRadius, sceneRadius);
		centerCircle.setPickable(false);
		centerCircle.setNoFill(true);
//		centerCircle.setStrokeColor(new MTColor(0,0,0));
		centerCircle.setStrokeColor(new MTColor(0,0,0));
		centerCircle.setStrokeWeight(0.5f);
		this.getCanvas().addChild(centerCircle);
		

		
		
		sceneTexture=new MTSceneTexture(mtApplication,0,0,mtApplication.width ,mtApplication.height, theScene);

		

		sceneTexture.setStrokeColor(new MTColor(0,0,0));
		

		sceneTexture.setSizeXYGlobal(fboWidth, fboHeight);
		
		
		this.getCanvas().addChild(sceneTexture);
		
		final MTRectangle rotationAreaLeft = new MTRectangle((mtApplication.width/2)-fboWidth/2-(sceneRadius-fboWidth/2), (mtApplication.height/2)-fboHeight/2, (sceneRadius-fboWidth/2), fboHeight, mtApplication); 
		final MTRectangle rotationAreaRight = new MTRectangle((mtApplication.width/2)+fboWidth/2, (mtApplication.height/2)-fboHeight/2, (sceneRadius-fboWidth/2), fboHeight, mtApplication); 

		rotationAreaLeft.setFillColor(alphaZero);
		rotationAreaRight.setFillColor(alphaZero);
		rotationAreaRight.setStrokeColor(alphaZero);
		rotationAreaLeft.setStrokeColor(alphaZero);
		
		
		rotationAreaRight.removeAllGestureEventListeners(); 
        getCanvas().addChild(rotationAreaRight); 
		
        rotationAreaRight.addGestureListener(DragProcessor.class, new CircularContainerDragListener(this));
		
		
		rotationAreaLeft.removeAllGestureEventListeners(); 
        getCanvas().addChild(rotationAreaLeft); 
		
        rotationAreaLeft.addGestureListener(DragProcessor.class, new CircularContainerDragListener(this));
        
        MTImageButton exitButton=new MTImageButton(app.loadImage("."+((String)File.separator)+"src"+((String)File.separator)+"circularContainer"+((String)File.separator)+"data"+((String)File.separator)+"image"+((String)File.separator)+"exit.png"),app);
		exitButton.setNoStroke(true);
		getCanvas().addChild(exitButton);
		exitButton.setPositionGlobal(new Vector3D(mtApplication.width/2f,50));
		exitButton.registerInputProcessor(new TapProcessor(app));
		exitButton.addGestureListener(TapProcessor.class, new IGestureEventListener(){

			@Override
			public boolean processGestureEvent(MTGestureEvent ge) {
				TapEvent te = (TapEvent)ge;
				if (te.isTapped()){
					if (app.popScene()){
						System.out.println("popped");
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
		
		
		/*public void	memorizeStubItem(MTPolygon StubInterfaceItem){
			myStubInterfaceMemory.addItem(StubInterfaceItem);
		}*/
		
		
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
				
//				System.out.println(startVelocityVec);
				//Animation inertiaAnim = new Animation("Inertia anim for " + target, new MultiPurposeInterpolator(startVelocityVec.length(), 0, 100, 0.0f, 0.5f, 1), target);
			}
			
			public void update(long timeDelta) {
				if (true){ //TODO implement method is dragging as in MTCircularList
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
