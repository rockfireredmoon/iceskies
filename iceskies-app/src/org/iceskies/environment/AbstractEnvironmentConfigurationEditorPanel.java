package org.iceskies.environment;

import java.util.prefs.Preferences;

import org.icelib.UndoManager;

import icetone.core.Container;
import icetone.core.ElementManager;

public abstract class AbstractEnvironmentConfigurationEditorPanel<T extends AbstractEnvironmentConfiguration> extends Container {

	protected boolean adjusting;
	protected T environmentConfiguration;
	protected final Preferences prefs;
	protected final UndoManager undoManager;

	public AbstractEnvironmentConfigurationEditorPanel(UndoManager undoManager, ElementManager screen, Preferences prefs,
			T environmentConfiguration) {
		super(screen);
		this.undoManager = undoManager;
		this.prefs = prefs;
		this.environmentConfiguration = environmentConfiguration;
	}

	public void setEnvironment(T environmentConfiguration) {
		this.environmentConfiguration = environmentConfiguration;
		rebuild();
	}

	protected abstract void rebuild();
}
