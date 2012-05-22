package circularContainer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;


public class PropertyChangePropagator {
	PropertyChangeSupport support;
	
	public PropertyChangePropagator(){
		support=new PropertyChangeSupport(this);
	}
	
	public void subscribe(PropertyChangeListener pC){
		support.addPropertyChangeListener(pC);
	}
	public void propagate(PropertyChangeEvent evt){
		support.firePropertyChange(evt);
	}
}
