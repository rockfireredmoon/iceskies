package org.iceskies.environment;

import java.util.prefs.Preferences;

import icetone.core.StyledContainer;
import icetone.core.undo.UndoManager;
import icetone.core.BaseScreen;

public abstract class AbstractEnvironmentConfigurationEditorPanel<T extends AbstractEnvironmentConfiguration> extends StyledContainer {

	private boolean adjusting;
	protected T environmentConfiguration;
	protected final Preferences prefs;
	protected final UndoManager undoManager;

	public AbstractEnvironmentConfigurationEditorPanel(UndoManager undoManager, BaseScreen screen, Preferences prefs,
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
