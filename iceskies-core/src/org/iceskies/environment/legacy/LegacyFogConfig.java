package org.iceskies.environment.legacy;

import java.io.Serializable;
import java.util.Objects;

import org.icelib.beans.AbstractPropertyChangeSupport;
import org.iceskies.environment.AbstractEnvironmentConfiguration.Format;
import org.icesquirrel.runtime.SquirrelTable;

import com.jme3.math.ColorRGBA;

public class LegacyFogConfig extends AbstractPropertyChangeSupport implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String PROP_COLOR = "Fog.Color";
	private static final String PROP_EXP = "Fog.Exp";
	private static final String PROP_START = "Fog.Start";
	private static final String PROP_END = "Fog.End";
	private static final String PROP_EXCLUDE_SKY = "Fog.ExcludeSky";
	private static final String PROP_ENABLED = "Fog.Enabled";

	private ColorRGBA color = ColorRGBA.Black.clone();
	private float exp;
	private float start;
	private float end;
	private boolean excludeSky = false;
	private boolean enabled = true;

	public ColorRGBA getColor() {
		return color;
	}

	public LegacyFogConfig clone() {
		LegacyFogConfig lfc = new LegacyFogConfig();
		lfc.color = color.clone();
		lfc.start = start;
		lfc.exp = exp;
		lfc.end = end;
		lfc.excludeSky = excludeSky;
		lfc.enabled = enabled;
		return lfc;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		boolean was = isEnabled();
		if (!Objects.equals(was, enabled)) {
			this.enabled = enabled;
			firePropertyChange(PROP_ENABLED, was, enabled);
		}
	}

	public void setColor(ColorRGBA color) {
		ColorRGBA was = getColor();
		if (!Objects.equals(was, color)) {
			this.color = color;
			firePropertyChange(PROP_COLOR, was, color);
		}
	}

	public void setExp(float exp) {
		float was = getExp();
		if (!Objects.equals(was, exp)) {
			this.exp = exp;
			firePropertyChange(PROP_EXP, was, exp);
		}
	}

	public void setStart(float start) {
		float was = getStart();
		if (!Objects.equals(was, start)) {
			this.start = start;
			firePropertyChange(PROP_START, was, start);
		}
	}

	public void setEnd(float end) {
		float was = getEnd();
		if (!Objects.equals(was, end)) {
			this.end = end;
			firePropertyChange(PROP_END, was, end);
		}
	}

	public boolean isExcludeSky() {
		return excludeSky;
	}

	public void setExcludeSky(boolean excludeSky) {
		boolean was = isExcludeSky();
		if (!Objects.equals(was, excludeSky)) {
			this.excludeSky = excludeSky;
			firePropertyChange(PROP_EXCLUDE_SKY, was, excludeSky);
		}
		this.excludeSky = excludeSky;
	}

	public float getExp() {
		return exp;
	}

	public float getStart() {
		return start;
	}

	public float getEnd() {
		return end;
	}

	public Object to(Format format) {
		if (format == Format.SQUIRREL) {
			SquirrelTable t = new SquirrelTable();
			t.insert("color", LegacyEnvironmentConfiguration.toSquirrelRGB(color));
			t.insert("exp", exp);
			t.insert("start", start);
			t.insert("end", end);
			t.insert("enabled", enabled);
			return t;
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return "LegacyFogConfig [color=" + color + ", exp=" + exp + ", start=" + start + ", end=" + end + ", excludeSky="
				+ excludeSky + "]";
	}
}