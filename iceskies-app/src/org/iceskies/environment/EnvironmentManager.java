package org.iceskies.environment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.icescene.ServiceRef;
import org.icescripting.Scripts;

import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;

import icemoon.iceloader.ServerAssetManager;

/**
 * Manages configuration of skies from scripts.
 */
public class EnvironmentManager {

	private static EnvironmentManager instance;

	public static EnvironmentManager get(AssetManager assetManager) {
		if (instance == null) {
			instance = new EnvironmentManager(assetManager);
		}
		return instance;
	}

	private AssetManager assetManager;

	private boolean loaded;

	@ServiceRef
	private static Environments environments;

	public EnvironmentManager(AssetManager assetManager) {
		this.assetManager = assetManager;
		// Load scripts needed by all environments
		try {
			Scripts.get().eval("Environment/Environment_Local.js");
		} catch (AssetNotFoundException anfe) {
		}
		Scripts.get().eval("Environment/Environments.js");

	}

	public AbstractEnvironmentConfiguration newConfiguration(String key, Class<? extends AbstractEnvironmentConfiguration> clazz) {
		try {
			AbstractEnvironmentConfiguration config = clazz.getConstructor(String.class).newInstance(key);
			if (!environments.addConfiguration(config)) {
				throw new IllegalArgumentException("Environment already exists.");
			}
			return config;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public EnvironmentGroupConfiguration getEnvironments(String key) {
		AbstractEnvironmentConfiguration ec = environments.get(key);
		if (ec == null) {
			throw new IllegalArgumentException(String.format("No environment with name %s", key));
		}
		if (!(ec instanceof EnvironmentGroupConfiguration)) {
			throw new IllegalArgumentException("Key must be an environment name only.");
		}
		return (EnvironmentGroupConfiguration) ec;
	}

	public List<String> getEnvironments() {
		loadEnvironments();
		List<String> l = new ArrayList<>();
		for (Map.Entry<String, AbstractEnvironmentConfiguration> en : environments.entrySet()) {
			if (en.getValue() instanceof EnvironmentGroupConfiguration) {
				l.add(en.getKey());
			}
		}
		Collections.sort(l);
		return l;
	}

	public List<String> getEnvironmentConfigurations() {
		List<String> l = new ArrayList<>();
		for (Map.Entry<String, AbstractEnvironmentConfiguration> en : environments.entrySet()) {
			if (!(en.getValue() instanceof EnvironmentGroupConfiguration)) {
				l.add(en.getKey());
			}
		}
		Collections.sort(l);
		return l;
	}

	public AbstractEnvironmentConfiguration getEnvironmentConfiguration(String key) {
		if (environments.containsKey(key)) {
			AbstractEnvironmentConfiguration abstractEnvironmentConfiguration = environments.get(key);
			if (abstractEnvironmentConfiguration instanceof EnvironmentGroupConfiguration) {
				throw new IllegalArgumentException(String.format("Must provide a key for a configuration, not a group '%s'.", key));
			}
			return abstractEnvironmentConfiguration;
		} else {
			for (String g : getEnvironments()) {
				EnvironmentGroupConfiguration gr = getEnvironments(g);
				if (gr.getPhases().containsValue(key)) {
					// We now have the group, so can get from the map. It should
					// not attempt to lazily create the configuration
					return environments.get(key);
				}
			}
		}
		throw new IllegalArgumentException(String.format("No environment configuration with key %s", key));
	}

	public boolean isEnvironment(String env) {
		return environments.containsKey(env) && environments.get(env) instanceof EnvironmentGroupConfiguration;
	}
	
	private void loadJsonEnvironment(String path) {
		
	}

	private void loadEnvironments() {
		if (!loaded) {
			loaded = true;

			// Load all scripted environments
			Set<String> all = Scripts.get().locateScript("Environment/.*");
			String local = null;
			for (String s : all) {
				if (!FilenameUtils.getBaseName(s).equals("Environment_Local")) {
					if (!Scripts.get().isLoaded(s)) {
						Scripts.get().eval(s);
					}
				} else {
					local = s;
				}
			}
			if (local != null) {
				// Do local script last so they can override existing
				// environments
				if (!Scripts.get().isLoaded(local)) {
					Scripts.get().eval(local);
				}
			}
			
			// Load JSON environments
			for(String json : ((ServerAssetManager)assetManager).getAssetNamesMatching("Environment/.*\\.json")) {
				loadJsonEnvironment(json);
			}
		}
	}

}
