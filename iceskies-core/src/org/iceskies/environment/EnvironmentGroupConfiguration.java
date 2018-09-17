package org.iceskies.environment;

import java.io.OutputStream;
import java.util.Map;
import java.util.Objects;

import org.icelib.Icelib;
import org.icelib.beans.MappedMap;
import org.icescene.environment.EnvironmentPhase;
import org.icesquirrel.runtime.SquirrelPrintWriter;
import org.icesquirrel.runtime.SquirrelPrinter;
import org.icesquirrel.runtime.SquirrelTable;

import com.jme3.math.ColorRGBA;

@SuppressWarnings("serial")
public class EnvironmentGroupConfiguration extends AbstractEnvironmentConfiguration {

	private Map<EnvironmentPhase, String> phases = new MappedMap<>(EnvironmentPhase.class, String.class);

	public static final String PROP_PHASES = "Phases";

	public EnvironmentGroupConfiguration(String key) {
		super(key);
	}

	public Map<EnvironmentPhase, String> getPhases() {
		return phases;
	}

	@Override
	public void write(OutputStream out, Format format) {
		if (format == Format.SQUIRREL) {
			SquirrelTable st = new SquirrelTable();
			SquirrelTable at = new SquirrelTable();
			for (Map.Entry<EnvironmentPhase, String> en : phases.entrySet()) {
				at.insert(Icelib.toEnglish(en.getKey()), en.getValue());
			}
			st.insert("TimeOfDay", at);
			SquirrelPrintWriter pw = new SquirrelPrintWriter(out, true);
			pw.print("::Environments." + key + " <- ");
			SquirrelPrinter.format(pw, st, 1);
			pw.println(";");
			return;
		}
		super.write(out, format);
	}

	static SquirrelTable toSquirrelRGB(ColorRGBA c) {
		SquirrelTable t = new SquirrelTable();
		t.insert("r", c.r);
		t.insert("g", c.g);
		t.insert("b", c.b);
		return t;
	}

	@Override
	public Format[] getOutputFormats() {
		return new Format[] { Format.SQUIRREL };
	}

	@Override
	public int getActivateMusicDelay() {
		return 0;
	}

	@Override
	public int getAmbientMusicDelay() {
		return 0;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setPhase(EnvironmentPhase phase, String environmentConfiguration) {
		String old = phases.get(phase);
		if (!Objects.equals(old, environmentConfiguration)) {
			Map<EnvironmentPhase, String> oldPhases = new MappedMap<>(EnvironmentPhase.class, String.class);
			oldPhases.putAll(phases);
			if (environmentConfiguration == null)
				phases.remove(phase);
			else
				phases.put(phase, environmentConfiguration);
			firePropertyChange(PROP_PHASES, oldPhases, phases);
		}
	}

	public void copyFrom(AbstractEnvironmentConfiguration source) {
		super.copyFrom(source);
		for (EnvironmentPhase p : EnvironmentPhase.phases()) {
			setPhase(p, ((EnvironmentGroupConfiguration) source).getPhases().get(p));
		}
	}

	@Override
	public Object clone() {
		EnvironmentGroupConfiguration agc = new EnvironmentGroupConfiguration(key);
		for (Map.Entry<EnvironmentPhase, String> en : phases.entrySet()) {
			agc.phases.put(en.getKey(), en.getValue());
		}
		return agc;
	}

}
