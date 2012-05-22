package pdfReader;

import org.mt4j.MTApplication;





public class myStartPDFExample extends MTApplication {
	private static final long serialVersionUID = 1L;
	
	public static void main(String[] args) {
		initialize();
	}
	@Override
	public void startUp() {
		//getInputManager().registerInputSource(new MacTrackpadSource(this));
		addScene(new pdfReaderScene(this, "PDFExampleScene","./src/pdfReader/data/UTC.pdf"));
	}
}
