package org.iceskies.environment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.icelib.Icelib;
import org.icescene.HUDMessageAppState;
import org.icescene.environment.EnvironmentPhase;
import org.iceskies.app.SkiesSwitcherAppState;
import org.iceskies.environment.EnvironmentSwitcherAppState.EnvPriority;

import com.jme3.input.event.MouseButtonEvent;

import icetone.controls.buttons.ButtonAdapter;
import icetone.controls.form.Form;
import icetone.controls.lists.ComboBox;
import icetone.controls.text.Label;
import icetone.controls.text.TextField;
import icetone.core.Element;
import icetone.core.ElementManager;
import icetone.core.layout.mig.MigLayout;

public class EnvironmentEditPanel extends Element {

	private TextField name;
	private Map<EnvironmentPhase, ComboBox<String>> phases = new HashMap<EnvironmentPhase, ComboBox<String>>();
	private EnvironmentGroupConfiguration environmentData = new EnvironmentGroupConfiguration("");

	public EnvironmentEditPanel(ElementManager screen) {
		super(screen);

		// Layout
		setLayoutManager(new MigLayout(screen, "wrap 3, fill", "[][][]", "[][][][][]"));
		Form f = new Form(screen);

		// Name
		addChild(new Label("Name", screen));
		name = new TextField(screen);
		addChild(name, "growx, span 2");
		f.addFormElement(name);

		//
		List<String> envs = EnvironmentManager.get(screen.getApplication().getAssetManager()).getEnvironmentConfigurations();

		// Phases
		for (final EnvironmentPhase p : EnvironmentPhase.phases()) {
			addChild(new Label(Icelib.toEnglish(p), screen));
			ComboBox<String> phase = new ComboBox<String>(screen) {
				@Override
				public void onChange(int selectedIndex, String value) {
					environmentData.getPhases().put(p, value);
				}
			};
			for (String k : envs) {
				phase.addListItem(k, k, false, false);
			}
			phase.pack(false);
			environmentData.getPhases().put(p, envs.get(0));
			addChild(phase);
			f.addFormElement(phase);
			phases.put(p, phase);

			addChild(new ButtonAdapter(screen, "Edit") {
				@Override
				public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {

					SkiesSwitcherAppState sas = app.getStateManager().getState(SkiesSwitcherAppState.class);
					AbstractEnvironmentConfiguration envConfig = EnvironmentManager.get(app.getAssetManager())
							.getEnvironmentConfiguration(phase.getSelectedValue());
					if (envConfig.isEditable()) {
						sas.setEnvironment(EnvPriority.EDITING, phase.getSelectedValue());
						sas.setEdit(true);
						onEditConfiguration();
					} else {
						HUDMessageAppState hud = app.getStateManager().getState(HUDMessageAppState.class);
						hud.message(Level.SEVERE,
								"This type of environment is not currently editable using the sky editor. You may be able to manually create them (for example 'Legacy' environments can be created using a JME3 .material file).");
					}
				}
			});

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
			phases.get(en.getKey()).setSelectedByValue(en.getValue(), false);
		}
	}
	
	protected void onEditConfiguration() {
	}

}
