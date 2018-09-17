package org.iceskies.environment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.icelib.Icelib;
import org.icescene.HUDMessageAppState;
import org.icescene.HUDMessageAppState.Channel;
import org.icescene.environment.EnvironmentPhase;
import org.iceskies.environment.EnvironmentSwitcherAppState.EnvPriority;

import icetone.controls.buttons.PushButton;
import icetone.controls.lists.ComboBox;
import icetone.controls.text.Label;
import icetone.controls.text.TextField;
import icetone.core.BaseScreen;
import icetone.core.Form;
import icetone.core.Element;
import icetone.core.ToolKit;
import icetone.core.layout.mig.MigLayout;

public class EnvironmentEditPanel extends Element {

	private TextField name;
	private Map<EnvironmentPhase, ComboBox<String>> phases = new HashMap<EnvironmentPhase, ComboBox<String>>();
	private EnvironmentGroupConfiguration environmentData = new EnvironmentGroupConfiguration("");

	public EnvironmentEditPanel(BaseScreen screen) {
		super(screen);

		// Layout
		setLayoutManager(new MigLayout(screen, "wrap 3, fill", "[][grow][]", "[][][][][]"));
		Form f = new Form(screen);

		// Name
		addElement(new Label("Name", screen));
		name = new TextField(screen);
		addElement(name, "growx, span 2");
		f.addFormElement(name);

		//
		List<String> envs = EnvironmentManager.get(screen.getApplication().getAssetManager())
				.getEnvironmentConfigurations();

		// Phases
		for (final EnvironmentPhase p : EnvironmentPhase.phases()) {
			addElement(new Label(Icelib.toEnglish(p), screen));
			ComboBox<String> phase = new ComboBox<String>(screen);
			phase.onChange(evt -> {
				if (!evt.getSource().isAdjusting())
					environmentData.getPhases().put(p, evt.getNewValue());
			});
			for (String k : envs) {
				phase.addListItem(k, k);
			}
			environmentData.getPhases().put(p, envs.get(0));
			addElement(phase, "growx");
			f.addFormElement(phase);
			phases.put(p, phase);

			addElement(new PushButton(screen, "Edit").onMouseReleased(evt -> {

				EditableEnvironmentSwitcherAppState sas = ToolKit.get().getApplication().getStateManager()
						.getState(EditableEnvironmentSwitcherAppState.class);
				AbstractEnvironmentConfiguration envConfig = EnvironmentManager
						.get(ToolKit.get().getApplication().getAssetManager())
						.getEnvironmentConfiguration(phase.getSelectedValue());
				if (envConfig.isEditable()) {
					sas.setEnvironment(EnvPriority.EDITING, phase.getSelectedValue());
					sas.setEdit(true);
					onEditConfiguration();
				} else {
					HUDMessageAppState hud = ToolKit.get().getApplication().getStateManager()
							.getState(HUDMessageAppState.class);
					hud.message(Channel.ERROR,
							"This type of environment is not currently editable using the sky editor. You may be able to manually create them (for example 'Legacy' environments can be created using a JME3 .material file).");
				}
			}));

		}
	}

	public EnvironmentGroupConfiguration getEnvironmentData() {
		return environmentData;
	}

	public String getEnvironmentKey() {
		return name.getText();
	}

	public void setEnvironment(EnvironmentGroupConfiguration environmentData) {
		this.environmentData = environmentData;
		name.setText(environmentData.getKey());
		for (Map.Entry<EnvironmentPhase, String> en : environmentData.getPhases().entrySet()) {
			ComboBox<String> comboBox = phases.get(en.getKey());
			comboBox.runAdjusting(() -> comboBox.setSelectedByValue(en.getValue()));
		}
	}

	protected void onEditConfiguration() {
	}

}
