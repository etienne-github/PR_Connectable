package pdfreader;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import org.mt4j.MTApplication;
import org.mt4j.components.TransformSpace;
import org.mt4j.components.visibleComponents.shapes.MTPolygon;
import org.mt4j.components.visibleComponents.shapes.MTRectangle;
import org.mt4j.components.visibleComponents.widgets.MTList;
import org.mt4j.components.visibleComponents.widgets.MTListCell;
import org.mt4j.components.visibleComponents.widgets.MTTextArea;
import org.mt4j.components.visibleComponents.widgets.MTTextField;
import org.mt4j.input.inputProcessors.IGestureEventListener;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor;
import org.mt4j.sceneManagement.AbstractScene;
import org.mt4j.util.MTColor;
import org.mt4j.util.font.FontManager;
import org.mt4j.util.math.Vector3D;
import org.mt4j.util.math.Vertex;
import org.mt4jx.components.visibleComponents.widgets.pdf.MTPDF;



import processing.core.PImage;

public class pdfReaderScene extends AbstractScene implements PropertyChangeListener {
	private MTApplication app;
	private MTTextField displayPageNumber;
	final myMTPDF pdf;
	
	public pdfReaderScene(final MTApplication mtApplication, String name, String path) {
		super(mtApplication, name);
		this.app = mtApplication;
		
		pdf = new myMTPDF(mtApplication, new File(path));
		pdf.scaleGlobal(.5f, .5f, .5f, pdf.getCenterPointGlobal());
		pdf.getSupport().addPropertyChangeListener(this);
//		pdf.addGestureListener(DragProcessor.class, new IGestureEventListener() {
//			@Override
//			public boolean processGestureEvent(MTGestureEvent ge) {
//				switch (ge.getId()) {
//				case DragEvent.GESTURE_ENDED:
//					pdf.setPageNumber(pdf.getPageNumber()+1);
//					break;
//				default:
//					break;
//				}
//				return false;
//			}
//
//		});
		



		//MTRectangle previewSlide=new MTRectangle(pdf.getWidthXY(TransformSpace.LOCAL),0,200,pdf.getHeightXY(TransformSpace.LOCAL),app);
		//previewSlide.removeAllGestureEventListeners();
		//previewSlide.setVisible(true);
		//previewSlide.setFillColor(MTColor.WHITE);
		
		float ListWidth=pdf.getWidthXY(TransformSpace.LOCAL)/3;
		float borderThickness=ListWidth*(1/5f);
		float CellWidth=ListWidth*(3/5f)+2*borderThickness;		
		float CellHeight=(pdf.getHeightXY(TransformSpace.LOCAL)/3)*(3/5f)+2*borderThickness;
		float contentWidth=ListWidth*(3/5f);
		float contentHeight=(pdf.getHeightXY(TransformSpace.LOCAL)/3)*(3/5f);
		float contentXOffset=borderThickness;
		float contentYOffset=borderThickness;
		
		
		
		MTList list=new MTList(pdf.getWidthXY(TransformSpace.LOCAL),0,ListWidth,pdf.getHeightXY(TransformSpace.LOCAL),0,app);
		list.setNoStroke(true);
		pdf.addChild(list);
		MTListCell currentCell;
		//Vertex[] vertices = new Vertex[]{
				//new Vertex(pdf.getWidthXY(TransformSpace.LOCAL)/10, pdf.getHeightXY(TransformSpace.LOCAL)/20,0, 0,0)};
		
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
					pdf.setPageNumber(myIndex);
				}
				
				return false;
			}
			
		}
		
		for(int i=0;i<pdf.getNumberOfPage();i++){
			System.out.println(i);
			MTRectangle rect=new MTRectangle(contentXOffset, contentYOffset/*pdf.getWidthXY(TransformSpace.LOCAL)/18*/, 0,contentWidth, contentHeight, getMTApplication());
			rect.setTexture(new PImage(pdf.getPreviewOfPage(i+1)));
			rect.setStrokeColor(new MTColor(80,80,80, 255));
			currentCell = new MTListCell(CellWidth,CellHeight,getMTApplication());
			currentCell.addChild(rect);
			currentCell.setNoStroke(true);
			currentCell.setFillColor(new MTColor(146,146,146));
			currentCell.registerInputProcessor(new TapProcessor(getMTApplication()));
			currentCell.addGestureListener(TapProcessor.class, new MTTapGestureCell(i+1, pdf) );

			list.addListElement(currentCell);
			
		}
		
		MTImageButton nextPage=new MTImageButton(getMTApplication().loadImage("."+((String)File.separator)+"src"+((String)File.separator)+"pdfReader"+((String)File.separator)+"data"+((String)File.separator)+"arrow-right.png"),getMTApplication());
		MTImageButton previousPage=new MTImageButton(getMTApplication().loadImage("."+((String)File.separator)+"src"+((String)File.separator)+"pdfReader"+((String)File.separator)+"data"+((String)File.separator)+"arrow-left.png"),getMTApplication());
		this.displayPageNumber = new MTTextField(0, 0, 50, 26, FontManager.getInstance().getDefaultFont(app), getMTApplication());
		MTRectangle subPane = new MTRectangle(0f,pdf.getHeightXY(TransformSpace.LOCAL),0f,(float) (pdf.getWidthXY(TransformSpace.LOCAL)+ListWidth),pdf.getWidthXY(TransformSpace.LOCAL)/6f,getMTApplication());
		subPane.setTexture(getMTApplication().loadImage("."+((String)File.separator)+"src"+((String)File.separator)+"pdfReader"+((String)File.separator)+"data"+((String)File.separator)+"steal_texture.jpg"));
		subPane.setNoStroke(true);
		
		
		
		pdf.addChild(subPane);
		subPane.removeAllGestureEventListeners();
		subPane.addChild(displayPageNumber);
		displayPageNumber.setPositionRelativeToParent(subPane.getCenterPointRelativeToParent());
		displayPageNumber.removeAllGestureEventListeners();
		displayPageNumber.setText(pdf.getPageNumber()+"/"+pdf.getNumberOfPage());
		displayPageNumber.setTextPositionRounding(true);
		displayPageNumber.setEnabled(true);
		subPane.addChild(nextPage);
		subPane.addChild(previousPage);
		nextPage.setNoStroke(true);
		previousPage.setNoStroke(true);
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
		
		
		nextPage.setPositionRelativeToParent(new Vector3D((float) (subPane.getCenterPointRelativeToParent().x+1.5*displayPageNumber.getWidthXY(TransformSpace.LOCAL)),subPane.getCenterPointRelativeToParent().y,subPane.getCenterPointRelativeToParent().z));
		previousPage.setPositionRelativeToParent(new Vector3D((float) (subPane.getCenterPointRelativeToParent().x-1.5*displayPageNumber.getWidthXY(TransformSpace.LOCAL)),subPane.getCenterPointRelativeToParent().y,subPane.getCenterPointRelativeToParent().z));

		this.getCanvas().addChild(pdf);
		
		
//		this.registerPreDrawAction(new IPreDrawAction() {
//			int cnt=0;
//			@Override
//			public void processAction() {
//				if(app.frameCount%60==0){
//					if(pdf.getNumberOfPages()>cnt){
//						pdf.setPageNumber(cnt++);
//					}else{
//						cnt = 0;
//						pdf.setPageNumber(cnt++);
//					}
//				}
//			}
//			@Override
//			public boolean isLoop() {
//				return true;
//			}
//		});
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		if(arg0.getPropertyName().compareTo("page_number")==0){
			this.displayPageNumber.setText(arg0.getNewValue()+"/"+pdf.getNumberOfPages());
		}
		
	}
}
