package pdfReader;

import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

import org.mt4j.AbstractMTApplication;
import org.mt4j.MTApplication;
import org.mt4j.components.MTCanvas;
import org.mt4j.components.StateChangeEvent;
import org.mt4j.components.StateChangeListener;
import org.mt4j.components.TransformSpace;
import org.mt4j.components.clipping.Clip;
import org.mt4j.components.visibleComponents.shapes.MTEllipse;
import org.mt4j.components.visibleComponents.shapes.MTLine;
import org.mt4j.components.visibleComponents.shapes.MTRectangle;
import org.mt4j.components.visibleComponents.shapes.MTRoundRectangle;
import org.mt4j.components.visibleComponents.shapes.MTRectangle.PositionAnchor;
import org.mt4j.components.visibleComponents.widgets.MTList;
import org.mt4j.components.visibleComponents.widgets.MTListCell;
import org.mt4j.components.visibleComponents.widgets.MTSceneTexture;
import org.mt4j.components.visibleComponents.widgets.MTTextArea;
import org.mt4j.components.visibleComponents.widgets.MTTextField;
import org.mt4j.components.visibleComponents.widgets.MTWindow;
import org.mt4j.components.visibleComponents.widgets.keyboard.ITextInputListener;
import org.mt4j.components.visibleComponents.widgets.keyboard.MTKeyboard;
import org.mt4j.input.inputProcessors.IGestureEventListener;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragEvent;
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.flickProcessor.FlickEvent;
import org.mt4j.input.inputProcessors.componentProcessors.flickProcessor.FlickProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.rotate3DProcessor.Rotate3DProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.rotateProcessor.RotateProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.scaleProcessor.ScaleProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor;
import org.mt4j.sceneManagement.AbstractScene;
import org.mt4j.sceneManagement.IPreDrawAction;
import org.mt4j.sceneManagement.Iscene;
import org.mt4j.sceneManagement.transition.ITransition;
import org.mt4j.util.MTColor;
import org.mt4j.util.camera.Icamera;
import org.mt4j.util.font.FontManager;
import org.mt4j.util.font.IFont;
import org.mt4j.util.math.Vector3D;

import circularContainer.Circularizable;
import circularContainer.ComponentSizeAndPositionMemory;
import circularContainer.InterfaceMemory;
import circularContainer.PropertyChangePropagator;
import circularContainer.StubInterfaceMemory;
import circularContainer.circularContainer;


import advanced.mtShell.ICreateScene;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import scenes.WaterSceneExportObf;

public class pdfViewer extends MTRectangle implements PropertyChangeListener, Circularizable{

	private MTTextField displayPageNumber;
	final myMTPDF pdf;	
	MTApplication app;
	private MTSceneTexture myScene;
	MTEllipse pdfCenter;
	MTEllipse sceneCenter;
	MTWindow myWindow;
	MTImageButton nextPage;
	MTImageButton previousPage;
	MTImageButton center;
	MTRectangle subPane;

	
	float pdfWidth;
	float pdfHeight;
	
	float ListWidth;
	float borderThickness;
	float CellWidth;		
	float CellHeight;
	float contentWidth;
	float contentHeight;
	float contentXOffset;
	float contentYOffset;
	float subPaneHeight;
	
	
	ComponentSizeAndPositionMemory mySizeAndPosition;
	InterfaceMemory myInterfaceMemory;
	PropertyChangePropagator pCp = new PropertyChangePropagator();
	
	
	
	public pdfViewer(float scale,String path, MTApplication appl){
		super(0,0,appl);

		this.app = appl;
		
		

		class ScalableScene extends AbstractScene{

			public ScalableScene(AbstractMTApplication mtApplication,
					String name) {
				super(mtApplication, name);

				
			}
			
		}

		this.setFillColor(MTColor.BLACK);
		
		pdf = new myMTPDF(app, new File(path));
		//pdf.scaleGlobal(scale, scale, scale, pdf.getAnchor());
		
		pdf.getSupport().addPropertyChangeListener(this);
		//pdf.removeAllGestureEventListeners(DragProcessor.class);
		pdf.removeAllGestureEventListeners(Rotate3DProcessor.class);
		//pdf.removeAllGestureEventListeners(RotateProcessor.class);
		
		
		
		
		

		
		pdfWidth=(float) ((pdf.getWidthXY(TransformSpace.LOCAL))*scale);
		pdfHeight=(float) ((pdf.getHeightXY(TransformSpace.LOCAL))*scale);
		pdf.setSizeLocal(pdfWidth, pdfHeight);
		
		ListWidth=pdfWidth/3;
		borderThickness=ListWidth*(1/5f);
		CellWidth=ListWidth*(3/5f)+2*borderThickness;		
		CellHeight=(pdfHeight/3)*(3/5f)+2*borderThickness;
		contentWidth=ListWidth*(3/5f);
		contentHeight=(pdfHeight/3)*(3/5f);
		contentXOffset=borderThickness;
		contentYOffset=borderThickness;
		subPaneHeight=pdfWidth/6f;
		
		myScene = new MTSceneTexture(app, 0, 0, new ScalableScene(app,"pdf scalable scene"));
		
		float scaledH=app.height/pdfHeight;
		float scaledW=app.width/pdfWidth;
		
		
		System.err.println("rapport initial : "+String.valueOf(pdfWidth/pdfHeight));
		System.err.println("fin : "+String.valueOf(1f/scaledW)+"-"+String.valueOf(1f/scaledH));
		
		/*myScene.setSizeXYGlobal(pdfWidth, pdfHeight);
		this.addChild(myScene);
		myScene.getScene().getCanvas().addChild(pdf);*/

		//pdf.setSizeLocal(pdf.getWidthXY(TransformSpace.LOCAL)*scaledW, pdf.getHeightXY(TransformSpace.LOCAL)*scaledH);
		//myScene
		this.setSizeLocal(pdfWidth+ListWidth, pdfHeight+subPaneHeight);
		
		//myScene.setPositionRelativeToOther(this, new Vector3D(pdfWidth/2f,pdfHeight/2f,0/*(this.getWidthXY(TransformSpace.LOCAL)/2f)*1,(this.getHeightXY(TransformSpace.LOCAL)/2f)*1,0*/));
		

		
		
		
		
		MTList list=new MTList(pdfWidth,0,ListWidth,pdfHeight,0,app);
		list.setNoStroke(true);
		this.addChild(list);
		MTListCell currentCell;
		//Vertex[] vertices = new Vertex[]{
				//new Vertex(pdf.getWidthXY(TransformSpace.LOCAL)/10, pdf.getHeightXY(TransformSpace.LOCAL)/20,0, 0,0)};
		
		class MTTapGestureCell implements IGestureEventListener{

			int myIndex;
			pdfReader.myMTPDF myMTPDF;
			public MTTapGestureCell(int i, pdfReader.myMTPDF pdf){
				myIndex=i;
				myMTPDF=pdf;
			}
			@Override
			public boolean processGestureEvent(MTGestureEvent ge) {
				TapEvent te = (TapEvent)ge;
				if (te.isTapped()){
					pdf.setPageNumber(myIndex);
				}
				
				return false;
			}
			
		}
		
		for(int i=0;i<pdf.getNumberOfPage();i++){
			System.out.println(i);
			MTRectangle rect=new MTRectangle(contentXOffset, contentYOffset/*pdf.getWidthXY(TransformSpace.LOCAL)/18*/, 0,contentWidth, contentHeight, app);
			rect.setTexture(new PImage(pdf.getPreviewOfPage(i+1)));
			rect.setStrokeColor(new MTColor(80,80,80, 255));
			currentCell = new MTListCell(CellWidth,CellHeight,app);
			currentCell.addChild(rect);
			currentCell.setNoStroke(true);
			currentCell.setFillColor(new MTColor(146,146,146));
			currentCell.registerInputProcessor(new TapProcessor(app));
			currentCell.addGestureListener(TapProcessor.class, new MTTapGestureCell(i+1, pdf) );

			list.addListElement(currentCell);
			
		}
		
		nextPage=new MTImageButton(app.loadImage("."+((String)File.separator)+"src"+((String)File.separator)+"pdfReader"+((String)File.separator)+"data"+((String)File.separator)+"arrow-right.png"),app);
		previousPage=new MTImageButton(app.loadImage("."+((String)File.separator)+"src"+((String)File.separator)+"pdfReader"+((String)File.separator)+"data"+((String)File.separator)+"arrow-left.png"),app);
		center = new MTImageButton(app.loadImage("."+((String)File.separator)+"src"+((String)File.separator)+"pdfReader"+((String)File.separator)+"data"+((String)File.separator)+"center.png"),app);
		this.displayPageNumber = new MTTextField(0, 0, 50, 26, FontManager.getInstance().getDefaultFont(app), app);
		subPane = new MTRectangle(0f,pdfHeight,0f,(float) (pdfWidth+ListWidth),subPaneHeight,app);
		subPane.setTexture(app.loadImage("."+((String)File.separator)+"src"+((String)File.separator)+"pdfReader"+((String)File.separator)+"data"+((String)File.separator)+"steal_texture.jpg"));
		subPane.setNoStroke(true);
		
		
		
		this.addChild(subPane);
		myInterfaceMemory=new InterfaceMemory(subPane);
		//subPane.removeAllGestureEventListeners();
		subPane.addChild(displayPageNumber);
		
		displayPageNumber.removeAllGestureEventListeners();
		displayPageNumber.setText(pdf.getPageNumber()+"/"+pdf.getNumberOfPage());
		displayPageNumber.setTextPositionRounding(true);
		displayPageNumber.setEnabled(true);
		subPane.addChild(nextPage);
		subPane.addChild(previousPage);
		subPane.addChild(center);
		nextPage.setNoStroke(true);
		previousPage.setNoStroke(true);
		center.setNoStroke(true);
		nextPage.addGestureListener(TapProcessor.class, new IGestureEventListener() {
			@Override
			public boolean processGestureEvent(MTGestureEvent ge) {
				TapEvent te = (TapEvent)ge;
				if(te.isTapped()){
					pdf.setPageNumber(pdf.getPageNumber()+1);
				}
				return false;
			}
		});
		
		previousPage.addGestureListener(TapProcessor.class, new IGestureEventListener() {
			@Override
			public boolean processGestureEvent(MTGestureEvent ge) {
				TapEvent te = (TapEvent)ge;
				if(te.isTapped()){
					pdf.setPageNumber(pdf.getPageNumber()-1);
				}
				return false;
			}
		});
		
		center.addGestureListener(TapProcessor.class, new IGestureEventListener(){

			@Override
			public boolean processGestureEvent(MTGestureEvent ge) {
				TapEvent te = (TapEvent)ge;
				if(te.isTapped()){
					
					circularContainer circularScene = new circularContainer(app,"circularContainer",pdfViewer.this);
					app.addScene(circularScene);
					app.pushScene();
					app.changeScene(circularScene);
				}
				return false;
			}
			
		});
		
		previousPage.setSizeXYGlobal(subPaneHeight/2f, subPaneHeight/2f);
		nextPage.setSizeXYGlobal(subPaneHeight/2f, subPaneHeight/2f);
		center.setSizeXYGlobal(subPaneHeight/2f, subPaneHeight/2f);
		
		displayPageNumber.setPositionRelativeToParent(subPane.getCenterPointRelativeToParent());
		nextPage.setPositionRelativeToParent(new Vector3D(
				(float) (
						subPane.getCenterPointRelativeToParent().x
						+displayPageNumber.getWidthXY(TransformSpace.GLOBAL)/2f
						+nextPage.getWidthXY(TransformSpace.GLOBAL)/2f
						+nextPage.getWidthXY(TransformSpace.GLOBAL)/8f
						),
						subPane.getCenterPointRelativeToParent().y,
						subPane.getCenterPointRelativeToParent().z));
		
		previousPage.setPositionRelativeToParent(new Vector3D(
				(float) (
						subPane.getCenterPointRelativeToParent().x
						-displayPageNumber.getWidthXY(TransformSpace.GLOBAL)/2f
						-nextPage.getWidthXY(TransformSpace.GLOBAL)/2f
						-nextPage.getWidthXY(TransformSpace.GLOBAL)/8f
						),
						subPane.getCenterPointRelativeToParent().y,
						subPane.getCenterPointRelativeToParent().z));
		
		center.setPositionRelativeToParent(new Vector3D(
				(float) (
						subPane.getCenterPointRelativeToParent().x
						+displayPageNumber.getWidthXY(TransformSpace.GLOBAL)/2f
						+3*nextPage.getWidthXY(TransformSpace.GLOBAL)/2f
						+2*nextPage.getWidthXY(TransformSpace.GLOBAL)/8f
						),
						subPane.getCenterPointRelativeToParent().y,
						subPane.getCenterPointRelativeToParent().z));
		
		subPane.addGestureListener(DragProcessor.class, new IGestureEventListener(){

			@Override
			public boolean processGestureEvent(MTGestureEvent ge) {
				DragEvent de=(DragEvent)ge;
				if(de!=null){
					pdfViewer.this.translateGlobal(de.getTranslationVect());
				}
				return false;
			}
			
		});
		
		
		myWindow = new MTWindow(0, 0, 0, pdfWidth, pdfHeight, 3, 3, app);
		
		
		myWindow.setSizeXYGlobal(pdfWidth, pdfHeight);
		myWindow.removeAllGestureEventListeners();
		myWindow.removeAllGestureEventListeners(DragProcessor.class);
		this.addChild(myWindow);
		MTRectangle bkgrd = new MTRectangle(0,0,pdfWidth,pdfHeight,app);
		bkgrd.setFillColor(MTColor.BLACK);
		bkgrd.setStrokeColor(MTColor.BLACK);
		bkgrd.removeAllGestureEventListeners();
		myWindow.addChild(bkgrd);	
		myWindow.addChild(pdf);		
		myWindow.setFillColor(MTColor.BLACK);
		pdf.removeAllGestureEventListeners(DragProcessor.class);
		subPane.setPickable(false);
		
		

		displayPageNumber.registerInputProcessor(new TapProcessor(app));
		displayPageNumber.addGestureListener(TapProcessor.class, new IGestureEventListener() {
			@Override
			public boolean processGestureEvent(MTGestureEvent ge) {
				TapEvent te = (TapEvent)ge;
				if (te.isTapped()){
					System.out.println("Called !");
					MTKeyboard keyboard = new MTKeyboard(app);
					keyboard.setFillColor(MTColor.GRAY);
					keyboard.scale((float)((pdfWidth+ListWidth)/keyboard.getWidthXY(TransformSpace.LOCAL)),(float)((pdfWidth+ListWidth)/keyboard.getWidthXY(TransformSpace.LOCAL)),(float)((pdfWidth+ListWidth)/keyboard.getWidthXY(TransformSpace.LOCAL)),keyboard.getCenterPointGlobal());
					pdfViewer.this.addChild(keyboard);
					keyboard.setVisible(true);
					keyboard.setPositionRelativeToOther(pdfViewer.this, new Vector3D(pdfViewer.this.getWidthXY(TransformSpace.LOCAL)/2f,pdfViewer.this.getHeightXY(TransformSpace.LOCAL)+keyboard.getHeightXY(TransformSpace.LOCAL)/4f,0));
					displayPageNumber.setText("");
					displayPageNumber.setEnableCaret(true);
					DisplayNumberInputKeyboardListener displayPageNumberInput = new DisplayNumberInputKeyboardListener(keyboard,displayPageNumber);
					keyboard.addTextInputListener(displayPageNumberInput);
				}
				return false;
			}
		});

		//DEBUG
		/*pdfCenter = new MTEllipse(app, pdf.getPosition(TransformSpace.GLOBAL),10,10);
		pdfCenter.setFillColor(MTColor.GREEN);
		
		this.addChild(pdfCenter);
		sceneCenter = new MTEllipse(app, myWindow.getCenterPointGlobal(),10,10);
		sceneCenter.setFillColor(MTColor.BLUE);
		sceneCenter.setPositionGlobal(new Vector3D(0,0,0));

		this.addChild(sceneCenter);*/
		//END DEBUG
		
		
		
		
		
		pdf.addGestureListener(DragProcessor.class, new IGestureEventListener(){
			
			


		@Override
		public boolean processGestureEvent(MTGestureEvent ge) {

			DragEvent de = (DragEvent)ge;
			
			if(de!=null){

					
					
					Vector3D relativePos = new Vector3D(pdf.getPosition(TransformSpace.GLOBAL).x-myWindow.getCenterPointGlobal().x,pdf.getPosition(TransformSpace.GLOBAL).y-myWindow.getCenterPointGlobal().y,pdf.getPosition(TransformSpace.GLOBAL).z);

					if(de.getTranslationVect().x>0){
						if(relativePos.x<(myWindow.getWidthXY(TransformSpace.LOCAL)*(9/10f))){
							de.getTargetComponent().translateGlobal(new Vector3D(de.getTranslationVect().x,0,0));
						}else
						{

					}
							
					
				
					}else if(de.getTranslationVect().x<0){
						if(relativePos.x>(myWindow.getWidthXY(TransformSpace.LOCAL)*(9/10f)*-1)){
							de.getTargetComponent().translateGlobal(new Vector3D(de.getTranslationVect().x,0,0));
						}else{

						}
					}
					
					if(de.getTranslationVect().y>0){
						if(relativePos.y<(myWindow.getHeightXY(TransformSpace.LOCAL)*(9/10f))){
							de.getTargetComponent().translateGlobal(new Vector3D(0,de.getTranslationVect().y,0));
						}else{

						}
							
				
					}else if(de.getTranslationVect().y<0){

						if(relativePos.y>myWindow.getHeightXY(TransformSpace.LOCAL)*(9/10f)*-1){
							de.getTargetComponent().translateGlobal(new Vector3D(0,de.getTranslationVect().y,0));
						}else{

						}
					}
					

			}
			
			return false;
		}
		
	});
		
		
		//System.out.println("pos : "+nextPage.getCenterPointGlobal().toString());

	}

	
	public void checkPageInput(String input){
		int a=0;
		try{
			a = Integer.parseInt(input);
			pdf.setPageNumber(a);
		}catch(Exception e){
			
		}
		

	}
	
	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		if(arg0.getPropertyName().compareTo("page_number")==0){
			this.displayPageNumber.setText(arg0.getNewValue()+"/"+pdf.getNumberOfPages());
			
			//Propagate to stubInterface
			pCp.propagate(arg0);
		}
		
	}


	@Override
	public Vector3D resize(circularContainer c) {
		
		//System.out.println("sceneRadius :"+c.getSceneRadius());
		//System.out.println("width : "+this.getWidthXY(TransformSpace.GLOBAL));
		//System.out.println("height : "+this.getHeightXY(TransformSpace.GLOBAL));
		this.setSizeLocal(pdfWidth+ListWidth, pdfHeight/*+subPaneHeight*/);
		float scale = (float) Math.sqrt(
				(
						Math.pow(c.getSceneRadius()*2,2)/
						(
								Math.pow(this.getHeightXY(TransformSpace.GLOBAL)/*-subPane.getHeightXY(TransformSpace.GLOBAL)*/,2)+
								Math.pow(this.getWidthXY(TransformSpace.GLOBAL),2)
						)
				)
				);
		//System.out.println("Scale :"+scale);
		this.scale(scale, scale, scale, this.getCenterPointGlobal(),TransformSpace.GLOBAL);
		this.setPositionGlobal(new Vector3D(app.width/2f,app.height/2f/*+subPane.getHeightXY(TransformSpace.GLOBAL)/2f*/));
		return new Vector3D(this.getWidthXY(TransformSpace.GLOBAL),this.getHeightXY(TransformSpace.GLOBAL));
		//System.out.println("Resized !");
		
	}


	@Override
	public void delegateInterface(circularContainer c) {

		//BY MOVING INTERFACE ITEMS
		
		//resize
		nextPage.setSizeXYGlobal(40, 40);
		previousPage.setSizeXYGlobal(40, 40);
		displayPageNumber.setSizeXYGlobal(70, 40);
		
		//add to circularContainer
		c.getCanvas().addChild(nextPage);
		c.getCanvas().addChild(previousPage);
		c.getCanvas().addChild(displayPageNumber);
		
		//move
		displayPageNumber.setPositionGlobal(new Vector3D(app.width/2f,app.height/2f+this.getHeightXY(TransformSpace.GLOBAL)/2f+displayPageNumber.getHeightXY(TransformSpace.GLOBAL)/2+10));
		previousPage.setPositionGlobal(new Vector3D(app.width/2f-displayPageNumber.getWidthXY(TransformSpace.GLOBAL)/2-previousPage.getWidthXY(TransformSpace.GLOBAL)-10,app.height/2f+this.getHeightXY(TransformSpace.GLOBAL)/2f+displayPageNumber.getHeightXY(TransformSpace.GLOBAL)/2+10));
		nextPage.setPositionGlobal(new Vector3D(app.width/2f+displayPageNumber.getWidthXY(TransformSpace.GLOBAL)/2+nextPage.getWidthXY(TransformSpace.GLOBAL)+10,app.height/2f+this.getHeightXY(TransformSpace.GLOBAL)/2f+displayPageNumber.getHeightXY(TransformSpace.GLOBAL)/2+10));
		
		
		/*
		//BY DUPLICATING INTERFACE ITEM
		
		//Duplicate page number display
		class StubDisplay extends MTTextField implements PropertyChangeListener{

			public StubDisplay(int i, float x, float y, float width,
					IFont iFont, MTApplication app) {
				super(i, x, y, width, iFont, app);
				// TODO Auto-generated constructor stub
			}

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				this.setText(evt.getNewValue()+"/"+pdf.getNumberOfPages());
				
			}
			
		}
		
		final StubDisplay stubDisplayPageNumber = new StubDisplay(0, 0, 50, 26, FontManager.getInstance().getDefaultFont(app), app);
		stubDisplayPageNumber.setText(pdf.getPageNumber()+"/"+pdf.getNumberOfPages());
		
		//subscribe to propagate propertychange listener
		pCp.subscribe(stubDisplayPageNumber);
		
		//Duplicate nextpage/previouspage buttons
		MTImageButton stubNextPage=new MTImageButton(app.loadImage("/Users/etiennegirot/Desktop/UTC/PR01/Connectable/ConnecTable/src/pdfReader/data/arrow-right.png"),app);
		MTImageButton stubPreviousPage=new MTImageButton(app.loadImage("/Users/etiennegirot/Desktop/UTC/PR01/Connectable/ConnecTable/src/pdfReader/data/arrow-left.png"),app);		
		stubNextPage.setNoStroke(true);
		stubPreviousPage.setNoStroke(true);
		
		//resize buttons
		stubNextPage.setSizeXYGlobal(40, 40);
		stubPreviousPage.setSizeXYGlobal(40, 40);
		stubDisplayPageNumber.setSizeXYGlobal(70, 40);
		
		
		//move duplicates to container
		c.getCanvas().addChild(stubNextPage);
		c.getCanvas().addChild(stubPreviousPage);
		c.getCanvas().addChild(stubDisplayPageNumber);
		
		
		stubDisplayPageNumber.setPositionGlobal(new Vector3D(app.width/2f,app.height/2f+this.getHeightXY(TransformSpace.GLOBAL)/2f+displayPageNumber.getHeightXY(TransformSpace.GLOBAL)/2+10));
		stubPreviousPage.setPositionGlobal(new Vector3D(app.width/2f-stubDisplayPageNumber.getWidthXY(TransformSpace.GLOBAL)/2-previousPage.getWidthXY(TransformSpace.GLOBAL)-10,app.height/2f+this.getHeightXY(TransformSpace.GLOBAL)/2f+stubDisplayPageNumber.getHeightXY(TransformSpace.GLOBAL)/2+10));
		stubNextPage.setPositionGlobal(new Vector3D(app.width/2f+stubDisplayPageNumber.getWidthXY(TransformSpace.GLOBAL)/2+nextPage.getWidthXY(TransformSpace.GLOBAL)+10,app.height/2f+this.getHeightXY(TransformSpace.GLOBAL)/2f+stubDisplayPageNumber.getHeightXY(TransformSpace.GLOBAL)/2+10));
		
		

		
		
		//add gesture listeners
		stubNextPage.addGestureListener(TapProcessor.class, new IGestureEventListener() {
			@Override
			public boolean processGestureEvent(MTGestureEvent ge) {
				TapEvent te = (TapEvent)ge;
				if(te.isTapped()){
					pdf.setPageNumber(pdf.getPageNumber()+1);
				}
				return false;
			}
		});
		
		stubPreviousPage.addGestureListener(TapProcessor.class, new IGestureEventListener() {
			@Override
			public boolean processGestureEvent(MTGestureEvent ge) {
				TapEvent te = (TapEvent)ge;
				if(te.isTapped()){
					pdf.setPageNumber(pdf.getPageNumber()-1);
				}
				return false;
			}
		});
		
		stubDisplayPageNumber.removeAllGestureEventListeners();
		stubDisplayPageNumber.registerInputProcessor(new TapProcessor(app));
		stubDisplayPageNumber.addGestureListener(TapProcessor.class, new IGestureEventListener() {
			@Override
			public boolean processGestureEvent(MTGestureEvent ge) {
				TapEvent te = (TapEvent)ge;
				if (te.isTapped()){
					System.out.println("Called !");
					MTKeyboard keyboard = new MTKeyboard(app);
					keyboard.setFillColor(MTColor.GRAY);
					keyboard.scale((float)((pdfWidth+ListWidth)/keyboard.getWidthXY(TransformSpace.LOCAL)),(float)((pdfWidth+ListWidth)/keyboard.getWidthXY(TransformSpace.LOCAL)),(float)((pdfWidth+ListWidth)/keyboard.getWidthXY(TransformSpace.LOCAL)),keyboard.getCenterPointGlobal());
					pdfViewer.this.addChild(keyboard);
					keyboard.setVisible(true);
					keyboard.setPositionRelativeToOther(pdfViewer.this, new Vector3D(pdfViewer.this.getWidthXY(TransformSpace.LOCAL)/2f,pdfViewer.this.getHeightXY(TransformSpace.LOCAL)-keyboard.getHeightXY(TransformSpace.LOCAL)/4f,0));
					stubDisplayPageNumber.setText("");
					stubDisplayPageNumber.setEnableCaret(true);
					DisplayNumberInputKeyboardListener displayPageNumberInput = new DisplayNumberInputKeyboardListener(keyboard,stubDisplayPageNumber);
					keyboard.addTextInputListener(displayPageNumberInput);
				}
				return false;
			}
		});
		
		
		*/
		subPane.setVisible(false);

	}





	

	@Override
	public void memorizeSizeAndPosition() {
		mySizeAndPosition=new ComponentSizeAndPositionMemory(this);	
		
	}


	@Override
	public void recoverSizeAndPosition() {
		mySizeAndPosition.recoverSizeAndPosition();
		
	}


	@Override
	public void recoverInterface() {	
		
		this.setSizeLocal(pdfWidth+ListWidth, pdfHeight+subPaneHeight);
		subPane.setVisible(true);
		
		myInterfaceMemory.recoverInterface();
		
	}
	
	class DisplayNumberInputKeyboardListener implements ITextInputListener{
		
		MTKeyboard myKB;
		MTTextField myField;

		public DisplayNumberInputKeyboardListener(MTKeyboard keyboard,MTTextField tF){
			myKB=keyboard;
			myField=tF;
		}

		@Override
		public void setText(String text) {
			myField.setText(text);	
		}
		
		@Override
		public void removeLastCharacter() {
			myField.removeLastCharacter();
		}

		@Override
		public void clear() {
			myField.clear();
		}
		
		@Override
		public void appendText(String text) {
			myField.appendText(text);	
		}
		
		@Override
		public void appendCharByUnicode(String unicode) {
			if (unicode.equals("\n")){
				myField.setEnableCaret(false);
				myKB.setVisible(false);
				pdfViewer.this.checkPageInput(myField.getText());
			}else{
				myField.appendCharByUnicode(unicode);
			}
		}
	}

	@Override
	public void memorizeInterface() {
		System.out.println("pos : "+nextPage.getCenterPointGlobal().toString());
		myInterfaceMemory.addItem(nextPage);
		myInterfaceMemory.addItem(previousPage);
		myInterfaceMemory.addItem(displayPageNumber);
		
	}

	

}
