package pdfreader;


import org.mt4j.components.visibleComponents.shapes.MTRectangle;
import org.mt4j.components.visibleComponents.widgets.MTClipRectangle;
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor;
import org.mt4j.util.MTColor;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * The Class MTListCell.
 */
public class PreviewListCell extends MTClipRectangle{
	MTRectangle myPreview;
	int myPageNumber;
	myMTPDF myViewer;
	private boolean isShown;
	PApplet myApp;

	 
	 
	 /**
 	 * Instantiates a new mT list cell.
 	 *
 	 * @param width the width
 	 * @param height the height
 	 * @param applet the applet
	 * @param pdf 
	 * @deprecated constructor will be deleted! Please , use the constructor with the PApplet instance as the first parameter.
 	 */
 	public PreviewListCell (float width, float height, PApplet applet, myMTPDF pdf) {
		 this(applet, width, height);
		 myViewer = pdf;
		 myApp=applet;
	 }

	/**
	 * Instantiates a new mT list cell.
	 *
	 * @param applet the applet
	 * @param width the width
	 * @param height the height
	 */
	public PreviewListCell (PApplet applet, float width, float height) {
		super(applet, 0, 0, 0, width, height);
		this.setStrokeColor(new MTColor(0,0,0));
		this.setComposite(true);
		
	}
	
	/* (non-Javadoc)
	 * @see org.mt4j.components.visibleComponents.shapes.AbstractShape#setDefaultGestureActions()
	 */
	@Override
	protected void setDefaultGestureActions() {
		this.registerInputProcessor(new DragProcessor(getRenderer()));
//		this.addGestureListener(DragProcessor.class, new DefaultDragAction());
	}

	public void show() {
		if(!isShown){

					myPreview.setTexture(new PImage(myViewer.getPreviewOfPage(myPageNumber)));
					PreviewListCell.this.isShown=true;	


		}
	}
	
	public void addPreview(MTRectangle rect,int pageNumber) {
		this.addChild(rect);
		myPreview=rect;
		myPageNumber=pageNumber;
	}

	public void hide() {
		PImage tex = myPreview.getTexture();
		myPreview.setTexture(null);
		if(tex!=null){
			tex.delete();
		}
		this.isShown=false;
	}
	
}
