package org.iceskies.environment;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.map.LazyMap;
import org.icelib.beans.MappedMap;
import org.icelib.beans.ObjectMapper;
import org.icescene.IcesceneApp;
import org.icescene.IcesceneService;
import org.icescene.Service;
import org.icescene.ServiceRef;
import org.icescripting.Scripts;
import org.iceskies.environment.enhanced.EnhancedEnvironmentConfiguration;
import org.iceskies.environment.legacy.LegacyEnvironmentConfiguration;

/**
 * Model definitions. In order to only load definitions as and when they are
 * needed, the elements of this map are initialised lazily from the creatures
 * script the first time the key is used. Ordinary only the the
 * {@link Environments#get(Object)} should be called meaning only individual
 * scripts are loaded, but some of the design tools may want to load all of the
 * definitions to get a list of names (or objects). In this case all unloaded
 * scripts will be loaded.
 *
 */
@Service
public class Environments extends MappedMap<String, AbstractEnvironmentConfiguration> implements IcesceneService {

	private static final long serialVersionUID = 1L;

	public Environments() {
		super(LazyMap.lazyMap(new HashMap<String, AbstractEnvironmentConfiguration>(), new ContentFactory()), String.class,
				AbstractEnvironmentConfiguration.class);
	}

	static class ContentFactory implements Transformer<String, AbstractEnvironmentConfiguration> {

		@ServiceRef
		private static Environments contentDef;

		@Override
		public AbstractEnvironmentConfiguration transform(String input) {
			String scriptPath = String.format("Environment/Environment_%s.js", input);
			if (Scripts.get().isLoaded(scriptPath)) {
				throw new IllegalStateException("Should not happen.");
			}
			Scripts.get().eval(scriptPath);
			return contentDef.get(input);
		}

	}

	@Override
	public void init(IcesceneApp app) {
	}

	public boolean addConfiguration(AbstractEnvironmentConfiguration config) {
		boolean put = checkPuttable(config.getKey());
		if (put)
			backingMap.put(config.getKey(), config);
		return put;
	}

	@Override
	protected Class<? extends AbstractEnvironmentConfiguration> getValueClassForValue(Object v) {
		if (v instanceof Map) {
			if ("enhanced".equals(((Map<?,?>) v).get("type"))) {
				return EnhancedEnvironmentConfiguration.class;
			}
			return LegacyEnvironmentConfiguration.class;
		} else {
			return super.getValueClassForValue(v);
		}
	}

	public void env(EnvironmentGroupConfiguration data) {
		if (checkPuttable(data.getKey()))
			backingMap.put(data.getKey(), data);
	}

	@Override
	protected boolean checkPuttable(String key) {
		return !backingMap.containsKey(key);
	}

	public EnvironmentGroupConfiguration env(String name, Map<String, Object> data) {
		EnvironmentGroupConfiguration egc = new EnvironmentGroupConfiguration(name);
		ObjectMapper<EnvironmentGroupConfiguration> map = new ObjectMapper<>(egc);
		map.map(data);
		env(egc);
		return egc;
	}
}
