package org.iceskies.environment.commands;

import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.icescene.console.AbstractCommand;
import org.icescene.console.Command;
import org.icescene.console.ConsoleAppState;
import org.icescene.environment.EnvironmentPhase;
import org.iceskies.environment.AbstractEnvironmentConfiguration;
import org.iceskies.environment.EnvironmentAppState;
import org.iceskies.environment.EnvironmentManager;
import org.iceskies.environment.EnvironmentSwitcherAppState;
import org.iceskies.environment.EnvironmentSwitcherAppState.EnvPriority;

@Command(names = "env")
public class Env extends AbstractCommand {
	private EnvironmentManager manager;

	@Override
	public void init(ConsoleAppState console) {
		super.init(console);

		manager = EnvironmentManager.get(app.getAssetManager());

		description = "View or change the currently selected environment (one of SUNRISE,DAY,SUNSET,NIGHT or NONE)";
		options.addOption("p", "phase", true, "Set the phase, or time of day. ");
		options.addOption("r", "priority", true, "Set the priority to use (defaults to USER). ");
		options.addOption("c", "configurations", false, "List all environment configurations.");
		options.addOption("e", "environments", false, "List all environments.");
		options.addOption("f", "force", false, "Force and environment configuration rather than an environment.");
		argHelp = "<environment|configuration|NONE>";
	}

	public boolean run(String cmdName, CommandLine commandLine) {
		String[] args = commandLine.getArgs();
		EnvironmentSwitcherAppState swas = app.getStateManager().getState(EnvironmentSwitcherAppState.class);
		EnvironmentAppState eas = app.getStateManager().getState(EnvironmentAppState.class);
		if (swas == null) {
			console.outputError("No environment switcher is active.");
		} else {
			EnvironmentPhase phase = null;
			EnvPriority pri = EnvPriority.USER;

			if (commandLine.hasOption('p')) {
				phase = EnvironmentPhase.valueOf(commandLine.getOptionValue('p').toUpperCase());
			}
			if (commandLine.hasOption('r')) {
				pri = EnvPriority.valueOf(commandLine.getOptionValue('r'));
			}

			if (commandLine.hasOption('e')) {
				for (String e : manager.getEnvironments()) {
					console.output(e);
				}
				return true;
			} else if (commandLine.hasOption('c')) {
				for (String e : manager.getEnvironmentConfigurations()) {
					console.output(e);
				}
				return true;
			} else {
				if (args.length > 0) {
					if (phase != null && commandLine.hasOption('f')) {
						console.outputError("Cannot set phase when forcing an environment configuration.");
					} else {
						String name = args[0].equalsIgnoreCase("none") ? null : args[0];
						if (commandLine.hasOption('f')) {
							if (eas == null)
								console.outputError("No environment switcher.");
							else {
								AbstractEnvironmentConfiguration environmentConfiguration = manager
										.getEnvironmentConfiguration(name);
								if (environmentConfiguration == null) {
									console.outputError(String.format("No environment configuration %s", name));
								} else {
									eas.setEnvironment(environmentConfiguration);
									console.output(String.format("Environment configuration now %s", name));
									return true;
								}
							}
						} else {
							swas.setEnvironment(pri, name, phase);
							console.output(String.format("Environment now %s (%s)", name, pri));
						}
						return true;
					}
				} else {
					if (phase != null) {
						swas.setPhase(phase);
						console.output(String.format("Phase now %s", phase));
						return true;
					} else
						outputEnvDetails(swas);
				}
			}

		}

		return false;
	}

	protected void outputEnvDetails(EnvironmentSwitcherAppState swas) {
		console.output(String.format("Environment: %s, Configuration: %s, Phase: %s", swas.getEnvironment(),
				swas.getEnvironmentConfiguration(), swas.getPhase()));
	}
}
