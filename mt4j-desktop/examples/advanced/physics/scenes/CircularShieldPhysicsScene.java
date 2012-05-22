package advanced.physics.scenes;

import java.util.ArrayList;
import java.util.List;

import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.mt4j.AbstractMTApplication;
import org.mt4j.components.MTComponent;
import org.mt4j.components.TransformSpace;
import org.mt4j.input.inputProcessors.IGestureEventListener;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor;
import org.mt4j.input.inputProcessors.globalProcessors.CursorTracer;
import org.mt4j.sceneManagement.AbstractScene;
import org.mt4j.util.MTColor;
import org.mt4j.util.math.ToolsMath;
import org.mt4j.util.math.Vector3D;
import org.mt4j.util.math.Vertex;

import com.sun.xml.internal.messaging.saaj.util.TeeInputStream;

import advanced.physics.physicsShapes.PhysicsCircle;
import advanced.physics.physicsShapes.PhysicsPolygon;
import advanced.physics.physicsShapes.PhysicsRectangle;
import advanced.physics.physicsShapes.myPhysicsRectangle;
import advanced.physics.util.PhysicsHelper;
import advanced.physics.util.UpdatePhysicsAction;

public class CircularShieldPhysicsScene extends AbstractScene {
	private float timeStep = 1.0f / 60.0f;
	private int constraintIterations = 10;
	
	/** THE CANVAS SCALE **/
	private float scale = 20;
	private AbstractMTApplication app;
	private World world;
	PhysicsCircle c;
	private MTComponent physicsContainer;
	
	public CircularShieldPhysicsScene(AbstractMTApplication mtApplication, String name) {
		super(mtApplication, name);
		this.app = mtApplication;
		
		float worldOffset = 10; //Make Physics world slightly bigger than screen borders
		//Physics world dimensions
		AABB worldAABB = new AABB(new Vec2(-worldOffset, -worldOffset), new Vec2((app.width)/scale + worldOffset, (app.height)/scale + worldOffset));
		//Vec2 gravity = new Vec2(0, 10);
		Vec2 gravity = new Vec2(0, 0);
		boolean sleep = true;
		//Create the pyhsics world
		this.world = new World(worldAABB, gravity, sleep);
		
		this.registerGlobalInputProcessor(new CursorTracer(app, this));
		
		//Update the positions of the components according the the physics simulation each frame
		this.registerPreDrawAction(new UpdatePhysicsAction(world, timeStep, constraintIterations, scale));
		
		physicsContainer = new MTComponent(app);
		//Scale the physics container. Physics calculations work best when the dimensions are small (about 0.1 - 10 units)
		//So we make the display of the container bigger and add in turn make our physics object smaller
		physicsContainer.scale(scale, scale, 1, Vector3D.ZERO_VECTOR);
		this.getCanvas().addChild(physicsContainer);
		
		//Create borders around the screen
		//this.createScreenBorders(physicsContainer);
		
		
		
		
		
		//Create empty circle
		int bigRadius= 400;
		int smallRadius = 350;
		int smallDef = 25;
		int bigDef = 25;
		double twoPi = Math.PI*2;
		double coveredAngle = Math.toRadians(50); //50 degrees
		
		Vertex[] shieldVertices = new Vertex[smallDef+bigDef];
		
		int i;
		int j;
		for(i=0; i<smallDef;i++){
			shieldVertices[i]=new Vertex((float)(/*app.width/2f+*/Math.cos((Math.PI-coveredAngle)/2f+(i/(float)smallDef)*coveredAngle)*smallRadius),(float)(/*app.height/2f+*/Math.sin((Math.PI-coveredAngle)/2f+(i/(float)smallDef)*coveredAngle)*smallRadius));
			// c = new PhysicsCircle(app, shieldVertices[i], 2, world, 0, 0, 0, scale);
			//physicsContainer.addChild(c);

		}
		for(j=0; j<bigDef;j++){
			shieldVertices[i+j]=new Vertex((float)(/*app.width/2f+*/Math.cos((Math.PI-coveredAngle)/2f+((bigDef-(j+1))/(float)bigDef)*coveredAngle)*bigRadius),(float)(/*app.height/2f+*/Math.sin((Math.PI-coveredAngle)/2f+((bigDef-(j+1))/(float)bigDef)*coveredAngle)*bigRadius));
			//c = new PhysicsCircle(app, shieldVertices[i+j], 2, world, 0, 0, 0, scale);
			//physicsContainer.addChild(c);
		}
		
		//shieldVertices[smallDef+bigDef]=shieldVertices[0];
		
		for(int k=0;k<bigDef+smallDef;k++){
			System.out.println("V["+k+"]=("+shieldVertices[k].x+" "+shieldVertices[k].y+")");
		}
		
		PhysicsPolygon shieldPoly = new PhysicsPolygon(shieldVertices, new Vector3D(app.width/2f,app.height/2f), app, world, 1.0f, 0.3f, 0.4f, scale);
		MTColor polyCol = new MTColor(ToolsMath.getRandom(60, 255),ToolsMath.getRandom(60, 255),ToolsMath.getRandom(60, 255));
		shieldPoly.setFillColor(polyCol);
		shieldPoly.setStrokeColor(polyCol);
		//PhysicsHelper.addDragJoint(world, shieldPoly, shieldPoly.getBody().isDynamic(), scale);
		//For an anti-aliased outline
		List<Vertex[]> contours = new ArrayList<Vertex[]>();
		contours.add(shieldVertices);
		shieldPoly.setOutlineContours(contours);
		shieldPoly.setNoStroke(false);
		physicsContainer.addChild(shieldPoly);
		shieldPoly.registerInputProcessor(new TapProcessor(app));
		
		 c= new PhysicsCircle(app,shieldPoly.getCenterPointGlobal(), 20, world, 0, 0, 0, scale);
		 System.out.println("center : "+physicsContainer.globalToLocal(shieldPoly.getCenterPointGlobal()).x+" "+physicsContainer.globalToLocal(shieldPoly.getCenterPointGlobal()).y);
		// physicsContainer.globalToLocal(shieldPoly.getCenterPointGlobal());
		 physicsContainer.addChild(c);
		
		
		shieldPoly.addGestureListener(TapProcessor.class, new IGestureEventListener(){

			
			
			
			@Override
			public boolean processGestureEvent(MTGestureEvent ge) {
				
				TapEvent tE = (TapEvent)ge;
				if(tE!=null){
					c.setPositionGlobal(((PhysicsPolygon)ge.getTarget()).getCenterPointGlobal());
					System.out.println("-> listener"+((PhysicsPolygon)ge.getTarget()).getCenterPointGlobal());
					
					//System.out.println("angle "+((PhysicsPolygon)ge.getTarget()).getAngle());
					//c.setPositionRelativeToOther(((PhysicsPolygon)ge.getTarget()), new Vertex(((PhysicsPolygon)ge.getTarget()).getCenterPointLocal().x-600,((PhysicsPolygon)ge.getTarget()).getCenterPointLocal().y-(500)));
					//c.setPositionGlobal(new Vertex(((PhysicsPolygon)ge.getTarget()).getCenterPointLocal().x-600,((PhysicsPolygon)ge.getTarget()).getCenterPointLocal().y-(500)));
					//System.out.println("hey");
					//System.out.println(new Vertex(((PhysicsPolygon)ge.getTarget()).getCenterPointLocal().x-600,((PhysicsPolygon)ge.getTarget()).getCenterPointLocal().y-(500)));
					//((PhysicsPolygon)ge.getTarget()).rotateZGlobal(((PhysicsPolygon)ge.getTarget()).getCenterPointGlobal(), 5);
					//((PhysicsPolygon)ge.getTarget()).rotateZ(((PhysicsPolygon)ge.getTarget()).getCenterPointGlobal(), 5, TransformSpace.GLOBAL);	
					//((PhysicsPolygon)ge.getTarget()).rotateZGlobal(new Vertex(((PhysicsPolygon)ge.getTarget()).getCenterPointGlobal().x-600,((PhysicsPolygon)ge.getTarget()).getCenterPointGlobal().y-(500)), 5);
					//((PhysicsPolygon)ge.getTarget()).setCenterRotation(5);
					if(tE.isTapped()){
						
						System.err.println("??");
						((PhysicsPolygon)ge.getTarget()).setCenterRotation(5);
					}
				}
				

				
				return false;
			}
			
		});
	//shieldPoly.rotateZ(new Vertex(shieldPoly.getCenterPointLocal().x,shieldPoly.getCenterPointLocal().y-(bigRadius-smallRadius)/2f), 90, TransformSpace.LOCAL);	

		
	}
	
	
	
	private void createScreenBorders(MTComponent parent){
		/*//Left border 
		float borderWidth = 50f;
		float borderHeight = app.height;
		Vector3D pos = new Vector3D(-(borderWidth/2f) , app.height/2f);
		PhysicsRectangle borderLeft = new PhysicsRectangle(pos, borderWidth, borderHeight, app, world, 0,0,0, scale);
		borderLeft.setName("borderLeft");
		parent.addChild(borderLeft);
		//Right border
		pos = new Vector3D(app.width + (borderWidth/2), app.height/2);
		PhysicsRectangle borderRight = new PhysicsRectangle(pos, borderWidth, borderHeight, app, world, 0,0,0, scale);
		borderRight.setName("borderRight");
		parent.addChild(borderRight);
		//Top border
		borderWidth = app.width;
		borderHeight = 50f;
		pos = new Vector3D(app.width/2, -(borderHeight/2));
		PhysicsRectangle borderTop = new PhysicsRectangle(pos, borderWidth, borderHeight, app, world, 0,0,0, scale);
		borderTop.setName("borderTop");
		parent.addChild(borderTop);
		//Bottom border
		pos = new Vector3D(app.width/2 , app.height + (borderHeight/2));
		PhysicsRectangle borderBottom = new PhysicsRectangle(pos, borderWidth, borderHeight, app, world, 0,0,0, scale);
		borderBottom.setName("borderBottom");
		//parent.addChild(borderBottom);*/
	}

	public void onEnter() {
	}
	
	public void onLeave() {	
	}

}
