package advanced.physics.scenes;

import java.util.ArrayList;
import java.util.List;

import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.mt4j.AbstractMTApplication;
import org.mt4j.components.MTComponent;
import org.mt4j.components.TransformSpace;
import org.mt4j.input.inputProcessors.globalProcessors.CursorTracer;
import org.mt4j.sceneManagement.AbstractScene;
import org.mt4j.util.MTColor;
import org.mt4j.util.math.ToolsMath;
import org.mt4j.util.math.Vector3D;
import org.mt4j.util.math.Vertex;

import advanced.physics.physicsShapes.PhysicsCircle;
import advanced.physics.physicsShapes.PhysicsPolygon;
import advanced.physics.physicsShapes.PhysicsRectangle;
import advanced.physics.physicsShapes.myPhysicsRectangle;
import advanced.physics.util.PhysicsHelper;
import advanced.physics.util.UpdatePhysicsAction;

public class myPhysicsScene extends AbstractScene {
	private float timeStep = 1.0f / 60.0f;
	private int constraintIterations = 10;
	
	/** THE CANVAS SCALE **/
	private float scale = 20;
	private AbstractMTApplication app;
	private World world;
	
	private MTComponent physicsContainer;
	
	public myPhysicsScene(AbstractMTApplication mtApplication, String name) {
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
		int radius = 400;
		int def = 96;
		float twoPi = (float) (Math.PI*2);
		/*
		var radius:Number = 100;
		var numSegments:Number = 24;
		var twoPi:Number = Math.PI * 2;*/
		
		Vertex[] emptyCircleVertices=new Vertex[def];
		PhysicsCircle test;
		for(int i=0;i<def;i++){
			System.out.println(Math.cos((i/(float)def)*twoPi)*radius+" "+Math.sin((i/(float)def)*twoPi)*radius);
			emptyCircleVertices[i]=new Vertex((float) (app.width/2f+Math.cos((i/(float)def)*twoPi)*radius) , (float)(app.height/2f+Math.sin((i/(float)def)*twoPi)*radius));
			test = new PhysicsCircle(app,emptyCircleVertices[i], 1, world, 0, 0, 0, scale);
			test.setFillColor(MTColor.WHITE);
			test.setStrokeColor(MTColor.RED);
			//physicsContainer.addChild(test);
			System.out.println("added en "+emptyCircleVertices[i].x+" "+emptyCircleVertices[i].y);
		}
		Vertex AB;
		Vertex halfAB;
		PhysicsRectangle pR;
		for(int i=0;i<def-1; i++){
			AB = new Vertex();
			AB.x = emptyCircleVertices[i+1].x-emptyCircleVertices[i].x;
			AB.y = emptyCircleVertices[i+1].y-emptyCircleVertices[i].y;
			float l = (float) Math.sqrt(Math.pow(AB.x, 2)+Math.pow(AB.y, 2));
			halfAB = new Vertex(AB.x/2f,AB.y/2f);
			pR = new PhysicsRectangle(new Vertex(emptyCircleVertices[i].x+halfAB.x,emptyCircleVertices[i].y+halfAB.y), l, 8, app, world, 0, 0, 0, scale);
			if((i>def/4)&&(i<3*(def/4))){
				pR.rotateZ(pR.getCenterPointLocal(), (float) Math.toDegrees(AB.angleBetween(Vector3D.X_AXIS))*-1, TransformSpace.LOCAL);
			}else{
				pR.rotateZ(pR.getCenterPointLocal(), (float) Math.toDegrees(AB.angleBetween(Vector3D.X_AXIS)), TransformSpace.LOCAL);
			}
			pR.setDepthBufferDisabled(true);
			physicsContainer.addChild(pR);
		}
		//Add last segment
		AB = new Vertex();
		AB.x = emptyCircleVertices[0].x-emptyCircleVertices[def-1].x;
		AB.y = emptyCircleVertices[0].y-emptyCircleVertices[def-1].y;
		float l = (float) Math.sqrt(Math.pow(AB.x, 2)+Math.pow(AB.y, 2));
		halfAB = new Vertex(AB.x/2f,AB.y/2f);
		pR = new PhysicsRectangle(new Vertex(emptyCircleVertices[def-1].x+halfAB.x,emptyCircleVertices[def-1].y+halfAB.y), l, 8, app, world, 0, 0, 0, scale);
		pR.rotateZ(pR.getCenterPointLocal(), (float) Math.toDegrees(AB.angleBetween(Vector3D.X_AXIS)), TransformSpace.LOCAL);
		pR.setDepthBufferDisabled(true);
		physicsContainer.addChild(pR);	
		
		//add bouncing circle
		PhysicsCircle c = new PhysicsCircle(app, new Vertex(app.width/2f,app.height/2F), 10, world, 1.0f, 0, 1, scale);
		MTColor col1 = new MTColor(ToolsMath.getRandom(60, 255),ToolsMath.getRandom(60, 255),ToolsMath.getRandom(60, 255));
		c.setFillColor(col1);
		c.setStrokeColor(col1);
		PhysicsHelper.addDragJoint(world, c, c.getBody().isDynamic(), scale);
		c.setDepthBufferDisabled(true);
		physicsContainer.addChild(c);
		
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
