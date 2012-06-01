package ShellCirculaire;

import java.awt.event.KeyEvent;
import java.io.File;

import org.mt4j.AbstractMTApplication;
import org.mt4j.MTApplication;
import org.mt4j.components.TransformSpace;
import org.mt4j.components.visibleComponents.shapes.MTPolygon;
import org.mt4j.components.visibleComponents.shapes.MTRectangle;
import org.mt4j.components.visibleComponents.widgets.MTListCell;
import org.mt4j.components.visibleComponents.widgets.MTTextArea;
import org.mt4j.input.gestureAction.InertiaDragAction;
import org.mt4j.input.inputProcessors.IGestureEventListener;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor;
import org.mt4j.input.inputProcessors.globalProcessors.CursorTracer;
import org.mt4j.sceneManagement.AbstractScene;
import org.mt4j.sceneManagement.Iscene;
import org.mt4j.sceneManagement.transition.BlendTransition;
import org.mt4j.sceneManagement.transition.FadeTransition;
import org.mt4j.util.MTColor;
import org.mt4j.util.font.FontManager;
import org.mt4j.util.font.IFont;
import org.mt4j.util.logging.ILogger;
import org.mt4j.util.logging.MTLoggerFactory;
import org.mt4j.util.math.Vector3D;
import org.mt4j.util.math.Vertex;
import org.mt4j.util.opengl.GLFBO;

import paint.MainDrawingScene;
import pdfreader.pdfReaderScene;
import pdfreader.PdfViewer;
import processing.core.PApplet;
import processing.core.PImage;
import scenes.WaterSceneExportObf;
import advanced.flickrMT.FlickrScene;
import advanced.fluidSimulator.FluidSimulationScene;
import menu.MTCircularList;
import menu.MTSceneMenu;
import menu.MTSceneWindow;
import models3D.Models3DScene;
import advanced.modestMapsMT.MapsScene;
import advanced.mtShell.ICreateScene;
import advanced.physics.scenes.AirHockeyScene;
import advanced.physics.scenes.PhysicsScene;
import advanced.puzzle.PuzzleScene;
import advanced.space3D.Space3DScene;
import advanced.touchTail.TouchTailScene;

import Vignettes.VignettesScene;

import org.mt4jx.components.visibleComponents.widgets.MTBrowserScene;

import circularcontainer.myMTSceneWindow;

public class MTShellCirculaire extends AbstractScene {
	/** The Constant logger. */
	private static final ILogger logger = MTLoggerFactory.getLogger(MTShellCirculaire.class.getName());
	static{
		logger.setLevel(ILogger.INFO);
	}
	
	/** The app. */
	private AbstractMTApplication app;
	
	/** The has fbo. */
	private boolean hasFBO;
	
	/** The list. */
	private MTCircularList list;
	
	/** The font. */
	private IFont font;
	
	/** The preferred icon height. */
	private int preferredIconHeight;
	
	/** The list width. */
	private float listWidth;
	
	/** The list height. */
	private int listHeight;
	
	/** The preferred icon width. */
	private int preferredIconWidth;
	
	/** The switch directly to scene. */
	private boolean switchDirectlyToScene = false;
	
	//TODO (dont allow throwing stuff out of the screen) or destroy it then
	//TODO loading screen
	
	/**
	 * Instantiates a new mT shell scene.
	 * 
	 * @param mtApplication the mt application
	 * @param name the name
	 */
	public MTShellCirculaire(AbstractMTApplication mtApplication, String name) {
		super(mtApplication, name);
		this.app = mtApplication;
		this.hasFBO = GLFBO.isSupported(app);
		this.switchDirectlyToScene = !this.hasFBO || switchDirectlyToScene;
		
		this.registerGlobalInputProcessor(new CursorTracer(app, this));
		
		
		
		this.setClearColor(new MTColor(0,0,0,255));
		
		//BACKGROUND
		preferredIconWidth = 128;
		preferredIconHeight = 96;
		
		//CREATE LIST
		PImage background = app.loadImage(this.getPathToIcons() + "shell_background.jpg");
		listWidth = app.width;
		listHeight = app.height;
		list = new MTCircularList(0, 0, listWidth, listHeight, 0, mtApplication);
		list.setNoStroke(true);
		list.setTexture(background);

		font = FontManager.getInstance().createFont(app, "Calibri", 16, MTColor.BLACK);
		
		/*this.addScene(new ICreateScene() {
			public Iscene getNewScene() {
				return new TouchTailScene(app, "Touch Tails");
			}
			public String getTitle() {
				return "Touch Tails";
			}
		}, app.loadImage(this.getPathToIcons() + "touchtails_s.png"));*/
		
		this.addScene(new ICreateScene() {
			public Iscene getNewScene() {
				return new MapsScene(app, "Maps");
			}
			public String getTitle() {
				return "Maps";
			}
		}, app.loadImage(this.getPathToIcons() + "maps_ss.png"));
		
		/*this.addScene(new ICreateScene() {
			public Iscene getNewScene() {
				return new AirHockeyScene(app, "Jeu du palet");
			}
			public String getTitle() {
				return "Jeu du palet";
			}
		}, app.loadImage(this.getPathToIcons() + "airhockey_s.png"));*/
		
		/*this.addScene(new ICreateScene() {
			public Iscene getNewScene() {
				return new PuzzleScene(app, "Puzzle");
			}
			public String getTitle() {
				return "Puzzle";
			}
		}, app.loadImage(this.getPathToIcons() + "puzz.png"));*/
		
		if (this.hasFBO){
			this.addScene(new ICreateScene() {
				public Iscene getNewScene() {
					return new MainDrawingScene(app, "Paint");
				}
				public String getTitle() {
					return "Paint";
				}
			}, app.loadImage(this.getPathToIcons() + "drawing_s.png"));
		}
		
		/*this.addScene(new ICreateScene() {
			public Iscene getNewScene() {
				return new FlickrScene(app, "Flickr");
			}
			public String getTitle() {
				return "Photo Search";
			}
		}, app.loadImage(this.getPathToIcons() + "flickr_s.png"));*/
		
		this.addScene(new ICreateScene() {
			public Iscene getNewScene() {
				return new WaterSceneExportObf(app, "Virtual Water"); 
			}
			public String getTitle() {
				return "Virtual Water";
			}
		}, app.loadImage(this.getPathToIcons() + "water_s.png"));
		
		this.addScene(new ICreateScene() {
			public Iscene getNewScene() {
				return new FluidSimulationScene(app, "Virtual Particles");
			}
			public String getTitle() {
				return "Virtual Particles";
			}
		}, app.loadImage(this.getPathToIcons() + "fluidparticles_s.png"));
		
		this.addScene(new ICreateScene() {
			public Iscene getNewScene() {
				return new Models3DScene(app, "Virtual 3D", 1);
			}
			public String getTitle() {
				return "Virtual 3D";
			}
		}, app.loadImage(this.getPathToIcons() + "teapot.jpg"));


		/*this.addScene(new ICreateScene() {
			public Iscene getNewScene() {
				return new Models3DScene(app, "Virtual 3D", 2);
			}
			public String getTitle() {
				return "Virtual 3D";
			}
		}, app.loadImage(this.getPathToIcons() + "teapot.jpg"));
		
		/*this.addScene(new ICreateScene() {
			public Iscene getNewScene() {
				return new PhysicsScene(app, "Physics Playground");
			}
			public String getTitle() {
				return "Physics Playground";
			}
		}, app.loadImage(this.getPathToIcons() + "pyhsics_s.png"));*/
		
		/*this.addScene(new ICreateScene() {
			public Iscene getNewScene() {
				return new Space3DScene(app, "Earth 3D");
			}
			public String getTitle() {
				return "Earth 3D";
			}
		}, app.loadImage(this.getPathToIcons() + "earth_s.png"));*/
		
		this.addScene(new ICreateScene() {
			public Iscene getNewScene() {
				return new MTBrowserScene(app, "Navigateur");
			}
			public String getTitle() {
				return "Navigateur";
			}
		}, app.loadImage(this.getPathToIcons() + "Navigateur.jpg"));
		
		
		this.addScene(new ICreateScene() {
			public Iscene getNewScene() {
				return new pdfReaderScene((MTApplication) app, "pdfReader","./src/pdfReader/data/UTC.pdf");
			}
			public String getTitle() {
				return "pdfReader";
			}
		}, app.loadImage(this.getPathToIcons() + "Navigateur.jpg"));
		
		
		//We look in the folders to create instances of virtual view for each folder
		final File virtualViewDirectory = new File(System.getProperty("user.dir" ) + System.getProperty("file.separator" ) + "data"+ System.getProperty("file.separator" ) + "VirtualViews" + System.getProperty("file.separator" ));
		final String viewsNames[] = virtualViewDirectory.list();
		for (int i = 0; i < viewsNames.length ; i++) {
			//The name of the view taken from the name of the folder
			final String viewName = viewsNames[i];

			//creation of the scene
			this.addScene(new ICreateScene() {
				public Iscene getNewScene() {
					return new VignettesScene(app, viewName, virtualViewDirectory.getAbsolutePath() + System.getProperty("file.separator" ) + viewName);
				}
				public String getTitle() {
					return viewName;
				}
			}, app.loadImage(virtualViewDirectory.getAbsolutePath() + System.getProperty("file.separator" ) + viewName + "/thumbnail.jpg"));
		}
		
		getCanvas().addChild(list);
		list.rotateZ(list.getCenterPointLocal(), 90, TransformSpace.LOCAL);
		list.setPositionGlobal(new Vector3D(app.width/2f, app.height/2f));
		getCanvas().setFrustumCulling(true); 
		
		//PdfViewer demoViewer =new PdfViewer(0.4f,"./src/pdfReader/data/UTC.pdf",(MTApplication) app);
		//this.getCanvas().addChild(demoViewer);
		//demoViewer.setPositionGlobal(new Vector3D(app.width/2f,app.height/2f));
		

		
		//Scene transition effect
		if (this.hasFBO){
			this.setTransition(new BlendTransition(app, 2000));	
		}else{
			this.setTransition(new FadeTransition(app, 2000));	
		}
	}
	
	
	
	/**
	 * Gets the path to icons.
	 * 
	 * @return the path to icons
	 */
	private String getPathToIcons(){
		return  System.getProperty("user.dir" ) + System.getProperty("file.separator" ) + "data"+ System.getProperty("file.separator" ) + "shell" + System.getProperty("file.separator" );
	}
	
	/**
	 * Adds the tap processor.
	 * 
	 * @param cell the cell
	 * @param createScene the create scene
	 */
	private void addTapProcessor(MTListCell cell, final ICreateScene createScene){
		cell.registerInputProcessor(new TapProcessor(app, 15));
		cell.addGestureListener(TapProcessor.class, new IGestureEventListener() {
			public boolean processGestureEvent(MTGestureEvent ge) {
				TapEvent te = (TapEvent)ge;
				if (te.isTapped()){
					//System.out.println("Clicked cell: " + te.getTargetComponent());
					final Iscene scene = createScene.getNewScene();
							
					if (!switchDirectlyToScene){//We have FBO support -> show scene in a window first
						
						if (hasFBO && scene instanceof AbstractScene){
							((AbstractScene) scene).setTransition(new BlendTransition(app, 300));	
						}
						
						final myMTSceneWindow sceneWindow = new myMTSceneWindow(scene,100, 100,(MTApplication) app);
						sceneWindow.setFillColor(new MTColor(50,50,50,200));
						sceneWindow.scaleGlobal(0.5f, 0.5f, 0.5f, sceneWindow.getCenterPointGlobal());
						sceneWindow.addGestureListener(DragProcessor.class, new InertiaDragAction());
						getCanvas().addChild(sceneWindow);
					}else{
						//No FBO available -> change to the new scene fullscreen directly
						
						float menuWidth = 64;
						float menuHeight = 64;
						MTSceneMenu sceneMenu = 
						//new MTSceneMenu(this, app.width-menuWidth/2f, 0-menuHeight/2f, menuWidth, menuHeight, app);
						new MTSceneMenu(app, scene, (app.width-menuWidth)/2, (app.height-menuHeight)/2, menuWidth, menuHeight);
						sceneMenu.addToScene();
						
						app.addScene(scene);
						app.pushScene();
						app.changeScene(scene);
					}
				}
				return false;
			}
		});
	}

	/**
	 * Adds the scene.
	 * 
	 * @param sceneToCreate the scene to create
	 * @param icon the icon
	 */
	public void addScene(ICreateScene sceneToCreate, PImage icon){
		float border = 1;
		float bothBorders = 2*border;
		float topShift = 20;
		
		float listCellWidth = listWidth;		
		float realListCellWidth = listCellWidth - bothBorders - app.width/2 + 10;
		float listCellHeight = preferredIconWidth - border;
		//float listCellHeight = preferredIconWidth ;
		
		MTListCell cell = new MTListCell(app ,  realListCellWidth, listCellHeight);
		cell.setNoFill(true);
		cell.setNoStroke(true);
		
		Vertex[] vertices = new Vertex[]{
				new Vertex(realListCellWidth-topShift, 				border,		  		0, 0,0),
				new Vertex(realListCellWidth-topShift, 				 listCellHeight -border,	0, 1,0),
				new Vertex(realListCellWidth-topShift - preferredIconHeight, listCellHeight -border,	0, 1,1),
				new Vertex(realListCellWidth-topShift - preferredIconHeight,	border,		  		0, 0,1),
				new Vertex(realListCellWidth-topShift, 				border,		  		0, 0,0),
		};
		MTPolygon p = new MTPolygon(getMTApplication(), vertices);
		p.setTexture(icon);
		p.setStrokeColor(new MTColor(80,80,80, 255));
				
		cell.addChild(p);
		
		list.addListElement(cell);
		addTapProcessor(cell, sceneToCreate);
		
		///Add scene title
		MTTextArea text = new MTTextArea(app, font);
		text.setFillColor(new MTColor(150,150,250,200));
		text.setNoFill(true);
		text.setNoStroke(true);
		text.setText(sceneToCreate.getTitle());
		text.rotateZ(text.getCenterPointLocal(), 90, TransformSpace.LOCAL);
		cell.addChild(text);
		
		text.setPositionRelativeToParent(cell.getCenterPointLocal());
		text.translate(new Vector3D(realListCellWidth*0.5f - text.getHeightXY(TransformSpace.LOCAL)*0.3f, 0));
	}
	
	public void onEnter() {
		getMTApplication().registerKeyEvent(this);
	}
	
	public void onLeave() {	
		getMTApplication().unregisterKeyEvent(this);
	}
	
	/**
	 * Key event.
	 * 
	 * @param e the e
	 */
	public void keyEvent(KeyEvent e){
		int evtID = e.getID();
		if (evtID != KeyEvent.KEY_PRESSED)
			return;
		switch (e.getKeyCode()){
		case KeyEvent.VK_F:
			System.out.println("FPS: " + getMTApplication().frameRate);
			break;
		case KeyEvent.VK_M:
			System.out.println("Max memory: " + Runtime.getRuntime().maxMemory() + " <-> Free memory: " + Runtime.getRuntime().freeMemory());
			break;	
		case KeyEvent.VK_C:
			getMTApplication().invokeLater(new Runnable() {
				public void run() {
//					System.gc();
//					GC.maxObjectInspectionAge();
//					System.runFinalization();
				}
			});
			break;
		default:
			break;
		}
	}

}
