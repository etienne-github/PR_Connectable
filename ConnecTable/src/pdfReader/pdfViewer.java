package pdfreader;

import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

import org.mt4j.AbstractMTApplication;
import org.mt4j.MTApplication;
import org.mt4j.components.MTCanvas;
import org.mt4j.components.MTComponent;
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

import circularcontainer.Circularizable;
import circularcontainer.ComponentSizeAndPositionMemory;
import circularcontainer.InterfaceMemory;
import circularcontainer.CircularContainer;


import advanced.mtShell.ICreateScene;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import scenes.WaterSceneExportObf;

/**
 * PdfViewer a pdf reader component with preview list.
 * This component can be nested in a circularContainer.
 * @author Etienne Girot
 *
 */
public class PdfViewer extends MTRectangle implements PropertyChangeListener, Circularizable{

	private MTTextField displayPageNumber;
	private final myMTPDF pdf;	
	private MTApplication app;
	private MTSceneTexture myScene;
	private MTEllipse pdfCenter;
	private MTEllipse sceneCenter;
	private MTWindow myWindow;
	private MTImageButton nextPage;
	private MTImageButton previousPage;
	private MTImageButton center;
	private MTRectangle subPane;

	
	private float pdfWidth;
	private float pdfHeight;
	
	private float ListWidth;
	private float borderThickness;
	private float CellWidth;		
	private float CellHeight;
	private float contentWidth;
	private float contentHeight;
	private float contentXOffset;
	private float contentYOffset;
	private float subPaneHeight;
	
	
	private ComponentSizeAndPositionMemory mySizeAndPosition;
	private InterfaceMemory myInterfaceMemory;

	
	
	/**
	 * Constructor
	 * @param scale component size/pdf nominative size
	 * @param path path of the file to be viewed
	 * @param appl
	 */
	public PdfViewer(float scale,String path, MTApplication appl){
		super(0,0,appl);

		this.app = appl;

		

		class ScalableScene extends AbstractScene{

			public ScalableScene(AbstractMTApplication mtApplication,
					String name) {
				super(mtApplication, name);

				
			}
			
		}

		this.setFillColor(MTColor.BLACK);
		
		//getting pdf renderer
		pdf = new myMTPDF(app, new File(path));
		
		pdf.getSupport().addPropertyChangeListener(this);
		
		//remove unused gestures
		pdf.removeAllGestureEventListeners(Rotate3DProcessor.class);
		pdf.removeAllGestureEventListeners(RotateProcessor.class);
		
		
		
		
		

		//calculation viewer sizes
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
		
		//Scene within the pdf will be set
		myScene = new MTSceneTexture(app, 0, 0, new ScalableScene(app,"pdf scalable scene"));
		
		float scaledH=app.height/pdfHeight;
		float scaledW=app.width/pdfWidth;

		this.setSizeLocal(pdfWidth+ListWidth, pdfHeight+subPaneHeight);
		
		

		
		
		
		//preview list
		PreviewList list=new PreviewList(pdfWidth,0,ListWidth,pdfHeight,0,app);
		list.setNoStroke(true);
		this.addChild(list);
		PreviewListCell currentCell;
		
		//preview list tap gesture listener (go to a determined page) 
		class MTTapGestureCell implements IGestureEventListener{

			int myIndex;
			pdfreader.myMTPDF myMTPDF;
			public MTTapGestureCell(int i, pdfreader.myMTPDF pdf){
				myIndex=i;
				myMTPDF=pdf;
			}
			@Override
			public boolean processGestureEvent(MTGestureEvent ge) {
				TapEvent te = (TapEvent)ge;
				if (te.isTapped()){
					
					pdf.setSizeXYGlobal(myWindow.getWidthXY(TransformSpace.GLOBAL),myWindow.getHeightXY(TransformSpace.GLOBAL));
					pdf.setPageNumber(myIndex);
					pdf.setPositionRelativeToOther(myWindow, myWindow.getCenterPointLocal());
					
				}
				
				return false;
			}
			
		}
		
		//init preview list
		for(int i=0;i<pdf.getNumberOfPage();i++){
			//.out.println(i);
			final MTRectangle rect=new MTRectangle(contentXOffset, contentYOffset/*pdf.getWidthXY(TransformSpace.LOCAL)/18*/, 0,contentWidth, contentHeight, app);
			
			rect.setStrokeColor(new MTColor(80,80,80, 255));
			currentCell = new PreviewListCell(CellWidth,CellHeight,app,pdf);
			currentCell.addPreview(rect,i+1);
			currentCell.setNoStroke(true);
			currentCell.setFillColor(new MTColor(146,146,146));
			currentCell.registerInputProcessor(new TapProcessor(app));
			currentCell.addGestureListener(TapProcessor.class, new MTTapGestureCell(i+1, pdf) );

			list.addListElement(currentCell);
			
			//System.out.println((rect.getCenterPointGlobal().y-CellHeight)+" > "+(list.getCenterPointGlobal().y+pdfHeight)+" ?");
			
			//TODO Faire une fonction de contr™le si la preview est dans la barre
			
			

			list.refreshPreviews();


			
			/*if(rect.getCenterPointGlobal().y-CellHeight/2f<list.getCenterPointGlobal().y+pdfHeight/2f){
				rect.setTexture(new PImage(pdf.getPreviewOfPage(i+1)));
			}*/

		}
		
		//set buttons previous/next page + switch to circularcontainer
		nextPage=new MTImageButton(app.loadImage("."+((String)File.separator)+"src"+((String)File.separator)+"pdfreader"+((String)File.separator)+"data"+((String)File.separator)+"arrow-right.png"),app);
		previousPage=new MTImageButton(app.loadImage("."+((String)File.separator)+"src"+((String)File.separator)+"pdfreader"+((String)File.separator)+"data"+((String)File.separator)+"arrow-left.png"),app);
		center = new MTImageButton(app.loadImage("."+((String)File.separator)+"src"+((String)File.separator)+"pdfreader"+((String)File.separator)+"data"+((String)File.separator)+"center.png"),app);
		
		//current page display
		this.displayPageNumber = new MTTextField(0, 0, 50, 26, FontManager.getInstance().getDefaultFont(app), app);
		
		//interface pane
		subPane = new MTRectangle(0f,pdfHeight,0f,(float) (pdfWidth+ListWidth),subPaneHeight,app);
		subPane.setTexture(app.loadImage("."+((String)File.separator)+"src"+((String)File.separator)+"pdfreader"+((String)File.separator)+"data"+((String)File.separator)+"steal_texture.jpg"));
		subPane.setNoStroke(true);
		
		
		
		this.addChild(subPane);
		
		//init interface memory for possible integration into a circular container
		
		myInterfaceMemory=new InterfaceMemory(subPane);
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
		
		//button listener
		nextPage.addGestureListener(TapProcessor.class, new IGestureEventListener() {
			@Override
			public boolean processGestureEvent(MTGestureEvent ge) {
				TapEvent te = (TapEvent)ge;
				if(te.isTapped()){
					//reset size
					pdf.setSizeXYGlobal(myWindow.getWidthXY(TransformSpace.GLOBAL),myWindow.getHeightXY(TransformSpace.GLOBAL));
					//change page
					pdf.setPageNumber(pdf.getPageNumber()+1);
					//reset position
					pdf.setPositionRelativeToOther(myWindow, myWindow.getCenterPointLocal());
					
				}
				return false;
			}
		});
		
		previousPage.addGestureListener(TapProcessor.class, new IGestureEventListener() {
			@Override
			public boolean processGestureEvent(MTGestureEvent ge) {
				TapEvent te = (TapEvent)ge;
				if(te.isTapped()){
					//reset size
					pdf.setSizeXYGlobal(myWindow.getWidthXY(TransformSpace.GLOBAL),myWindow.getHeightXY(TransformSpace.GLOBAL));
					//change page
					pdf.setPageNumber(pdf.getPageNumber()-1);
					//reset position
					pdf.setPositionRelativeToOther(myWindow, myWindow.getCenterPointLocal());

				}
				return false;
			}
		});
		
		

		center.addGestureListener(TapProcessor.class, new IGestureEventListener(){

			@Override
			public boolean processGestureEvent(MTGestureEvent ge) {
				TapEvent te = (TapEvent)ge;
				if(te.isTapped()){
					
					CircularContainer circularScene = new CircularContainer(app,"circularContainer",PdfViewer.this);
					
					//switch to circularContainer
					app.addScene(circularScene);
					app.pushScene();	
					app.changeScene(circularScene);
				}
				return false;
			}
			
		});
		
		//set buttons size and positon
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
		
		//Gestures on interface pan are bubbled to viewer
		subPane.addGestureListener(DragProcessor.class, new IGestureEventListener(){

			@Override
			public boolean processGestureEvent(MTGestureEvent ge) {
				DragEvent de=(DragEvent)ge;
				if(de!=null){
					PdfViewer.this.translateGlobal(de.getTranslationVect());
				}
				return false;
			}
			
		});
		
		
		//set pdf container 
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
		
		
		//set page display interaction with MTKeyboard
		displayPageNumber.registerInputProcessor(new TapProcessor(app));
		displayPageNumber.addGestureListener(TapProcessor.class, new IGestureEventListener() {
			@Override
			public boolean processGestureEvent(MTGestureEvent ge) {
				TapEvent te = (TapEvent)ge;
				if (te.isTapped()){
					//System.out.println("Called !");
					MTKeyboard keyboard = new MTKeyboard(app);
					keyboard.setFillColor(MTColor.GRAY);
					keyboard.scale((float)((pdfWidth+ListWidth)/keyboard.getWidthXY(TransformSpace.LOCAL)),(float)((pdfWidth+ListWidth)/keyboard.getWidthXY(TransformSpace.LOCAL)),(float)((pdfWidth+ListWidth)/keyboard.getWidthXY(TransformSpace.LOCAL)),keyboard.getCenterPointGlobal());
					PdfViewer.this.addChild(keyboard);
					keyboard.setVisible(true);
					keyboard.setPositionRelativeToOther(PdfViewer.this, new Vector3D(PdfViewer.this.getWidthXY(TransformSpace.LOCAL)/2f,PdfViewer.this.getHeightXY(TransformSpace.LOCAL)+keyboard.getHeightXY(TransformSpace.LOCAL)/4f,0));
					displayPageNumber.setText("");
					displayPageNumber.setEnableCaret(true);
					DisplayNumberInputKeyboardListener displayPageNumberInput = new DisplayNumberInputKeyboardListener(keyboard,displayPageNumber);
					keyboard.addTextInputListener(displayPageNumberInput);
				}
				return false;
			}
		});

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
		
		

	}

	
	/**
	 * Check if text entered into the display is valid
	 * @param input
	 */
	public void checkPageInput(String input){
		int a=0;
		try{
			a = Integer.parseInt(input);
			pdf.setSizeXYGlobal(myWindow.getWidthXY(TransformSpace.GLOBAL),myWindow.getHeightXY(TransformSpace.GLOBAL));
			pdf.setPageNumber(a);
			pdf.setPositionRelativeToOther(myWindow, myWindow.getCenterPointLocal());
			
		}catch(Exception e){
			
		}
		

	}
	
	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		if(arg0.getPropertyName().compareTo("page_number")==0){
			this.displayPageNumber.setText(arg0.getNewValue()+"/"+pdf.getNumberOfPages());

		}
		
	}

	

	@Override
	public Vector3D resize(CircularContainer c) {
		
		float scale = (float) Math.sqrt(
				(
						Math.pow(c.getSceneRadius()*2,2)/
						(
								Math.pow(this.getHeightXY(TransformSpace.GLOBAL)-subPane.getHeightXY(TransformSpace.GLOBAL),2)+
								Math.pow(this.getWidthXY(TransformSpace.GLOBAL),2)
						)
				)
				);
		this.scale(scale, scale, scale, this.getCenterPointGlobal(),TransformSpace.GLOBAL);
		this.setPositionGlobal(new Vector3D(app.width/2f,app.height/2f+subPane.getHeightXY(TransformSpace.GLOBAL)/2f));
		return new Vector3D(this.getWidthXY(TransformSpace.GLOBAL),this.getHeightXY(TransformSpace.GLOBAL));
		
	}


	@Override
	public void delegateInterface(CircularContainer c) {

		//BY MOVING INTERFACE ITEMS
		
		//resize

		

		//add to circularContainer
		c.getCanvas().addChild(nextPage);
		c.getCanvas().addChild(previousPage);
		c.getCanvas().addChild(displayPageNumber);
		
		//move
		
		
		/*displayPageNumber.setPositionGlobal(new Vector3D(app.width/2f,app.height/2f+this.getHeightXY(TransformSpace.GLOBAL)/2f+displayPageNumber.getHeightXY(TransformSpace.GLOBAL)/2+10-subPaneHeight));
		previousPage.setPositionGlobal(new Vector3D(app.width/2f-displayPageNumber.getWidthXY(TransformSpace.GLOBAL)/2-previousPage.getWidthXY(TransformSpace.GLOBAL)-10,app.height/2f+this.getHeightXY(TransformSpace.GLOBAL)/2f+displayPageNumber.getHeightXY(TransformSpace.GLOBAL)/2+10-subPaneHeight));
		nextPage.setPositionGlobal(new Vector3D(app.width/2f+displayPageNumber.getWidthXY(TransformSpace.GLOBAL)/2+nextPage.getWidthXY(TransformSpace.GLOBAL)+10,app.height/2f+this.getHeightXY(TransformSpace.GLOBAL)/2f+displayPageNumber.getHeightXY(TransformSpace.GLOBAL)/2+10-subPaneHeight));*/
		
		nextPage.setSizeXYGlobal(30, 30);
		previousPage.setSizeXYGlobal(30, 30);
		displayPageNumber.setSizeXYGlobal(50, 30);
		
		displayPageNumber.setPositionGlobal(new Vector3D(512.0f,678.6432f));
		previousPage.setPositionGlobal(new Vector3D(456.8383f,678.6432f));
		nextPage.setPositionGlobal(new Vector3D(567.1616f,678.6432f));
		
		
		System.out.println("disp "+displayPageNumber.getPosition(TransformSpace.GLOBAL));
		System.out.println("prev "+previousPage.getPosition(TransformSpace.GLOBAL));
		System.out.println("nex "+nextPage.getPosition(TransformSpace.GLOBAL));

		subPane.setVisible(false);
		subPane.setPickable(false);
		this.setPickable(false);
		this.setNoFill(true);
		this.setNoStroke(true);
		//this.setVisible(false);

	}





	

	@Override
	public void memorizeSizeAndPosition() {
		mySizeAndPosition=new ComponentSizeAndPositionMemory(this);	
		//System.err.println("meme this pos : "+this.getPosition(TransformSpace.GLOBAL)+" ("+this.getWidthXY(TransformSpace.GLOBAL)+"*"+this.getHeightXY(TransformSpace.GLOBAL)+")");

		
	}


	@Override
	public void recoverSizeAndPosition() {
		
		mySizeAndPosition.recoverSizeAndPosition(false);
		//System.err.println("reco this pos : "+this.getPosition(TransformSpace.GLOBAL)+" ("+this.getWidthXY(TransformSpace.GLOBAL)+"*"+this.getHeightXY(TransformSpace.GLOBAL)+")");

	}


	@Override
	public void recoverInterface() {	
        subPane.setVisible(true);
		this.setPickable(true);
		this.setNoFill(false);
		this.setNoStroke(false);
		
		
		//this.setSizeLocal(pdfWidth+ListWidth, pdfHeight+subPaneHeight);
		Vector3D translationStore = new Vector3D();
        Vector3D rotationStore = new Vector3D();
        Vector3D scaleStore = new Vector3D();
        this.getGlobalMatrix().decompose(translationStore, rotationStore, scaleStore);
        float Zrotation=rotationStore.z;
        //this.rotateZGlobal(getCenterPointGlobal(), 0);
        
        //TO ZERO
        this.rotateZGlobal(getCenterPointGlobal(), (float) Math.toDegrees(Zrotation)*-1);  
        myInterfaceMemory.recoverInterface();
        this.rotateZGlobal(getCenterPointGlobal(), (float) Math.toDegrees(Zrotation));
		
        
		
		
		
		//System.err.println("reco interface nextPage pos : "+nextPage.getPosition(TransformSpace.GLOBAL)+" ("+nextPage.getWidthXY(TransformSpace.GLOBAL)+"*"+nextPage.getHeightXY(TransformSpace.GLOBAL)+") visible ? "+nextPage.isVisible());
		//System.err.println("reco interface this pos : "+this.getPosition(TransformSpace.GLOBAL)+" ("+this.getWidthXY(TransformSpace.GLOBAL)+"*"+this.getHeightXY(TransformSpace.GLOBAL)+")");
		
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
				PdfViewer.this.checkPageInput(myField.getText());
			}else{
				myField.appendCharByUnicode(unicode);
			}
		}
	}

	@Override
	public void memorizeInterface() {

				Vector3D translationStore = new Vector3D();
		        Vector3D rotationStore = new Vector3D();
		        Vector3D scaleStore = new Vector3D();
		        this.getGlobalMatrix().decompose(translationStore, rotationStore, scaleStore);
		        float Zrotation=rotationStore.z;


		        this.rotateZGlobal(getCenterPointGlobal(), (float) Math.toDegrees(Zrotation)*-1);  
		        		myInterfaceMemory.addItem(nextPage);
		        		myInterfaceMemory.addItem(previousPage);
		        		myInterfaceMemory.addItem(displayPageNumber);
		        this.rotateZGlobal(getCenterPointGlobal(), (float) Math.toDegrees(Zrotation));
		
		

		
	}


	@Override
	public MTComponent getMyParent() {
		return this.getParent();
	}


	@Override
	public void giveBackToParent(MTComponent parent) {
		parent.addChild(this);		
	}

	

}
