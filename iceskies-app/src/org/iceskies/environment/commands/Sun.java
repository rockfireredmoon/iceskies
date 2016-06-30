package org.iceskies.environment.commands;

import org.apache.commons.cli.CommandLine;
import org.icescene.console.AbstractCommand;
import org.icescene.console.Command;
import org.icescene.environment.PostProcessAppState;
import org.iceskies.environment.EnvironmentAppState;

import com.jme3.app.state.AppStateManager;

@Command(names = "sun")
public class Sun extends AbstractCommand {

	public boolean run(String cmdName, CommandLine commandLine) {
		String[] args = commandLine.getArgs();
		AppStateManager stateManager = console.getApp().getStateManager();
		PostProcessAppState game = stateManager.getState(PostProcessAppState.class);
		if (args.length == 0) {
			console.output(String.format("Sun is at pos: %s dir: %s", game.getLight().getSunPosition(),
					game.getLight().getSunDirection()));
			return false;
		} else if (args[0].equals("cam")) {
			game.getLight().setSunToLocation(console.getApp().getCamera().getLocation());
			EnvironmentAppState env = stateManager.getState(EnvironmentAppState.class);
			env.getEnvironment().setDirectionalPosition(game.getLight().getSunPosition());
			console.output(String.format("Sun is now at pos: %s dir: %s", game.getLight().getSunPosition(),
					game.getLight().getSunDirection()));
		} else {
			console.outputError(
					"The sun command either requires no arguments (to print location), or the word 'cam' to set the light direction to the location of the camera.");
			return false;
		}
		return true;
	}
}
