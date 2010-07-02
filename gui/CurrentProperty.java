/**
 * This class holds a property and can notify other objects when it has changed.
 * 
 * @author Andrea Chiavazza
 * Copyright 2010 licensed under GPL version 3 obtainable from http://www.gnu.org/licenses/gpl.html
 */
package cuxna.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.event.EventListenerList;

public class CurrentProperty<T> {
	private T value;
	private EventListenerList eventListeners = new EventListenerList();

	public void setValue(T newValue) {
		if (newValue == null || !newValue.equals(value)) {
			T oldValue = value;
			value = newValue;
			firePropertyChange(oldValue, value);
		}
	}

	public T getValue() {
		return value;
	}

	// Listener notification support
	public void addPropertyChangeListener(PropertyChangeListener x) {
		eventListeners.add(PropertyChangeListener.class, x);
		// bring it up to date with current state
		// x.propertyChange(new PropertyChangeEvent(this, null, null, value));
	}

	public void removePropertyChangeListener(PropertyChangeListener x) {
		eventListeners.remove(PropertyChangeListener.class, x);
	}

	public void firePropertyChange(T oldValue, T newValue) {
		// Get the listener list
		Object[] listeners = eventListeners.getListenerList();
		// Process the listeners last to first
		// List is in pairs, Class and instance
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == PropertyChangeListener.class) {
				PropertyChangeListener cl = (PropertyChangeListener) listeners[i + 1];
				cl.propertyChange(new PropertyChangeEvent(this, null, oldValue,
						newValue));
			}
		}
	}
}
