package org.iceskies.environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.script.Bindings;

import org.icescene.IcesceneApp;
import org.icescene.ServiceRef;
import org.icescripting.Scripts;

import com.google.gson.Gson;

public class LocalEnvironments extends Environments {

	private static final long serialVersionUID = 1L;

	private File file;

	@ServiceRef
	private static Environments environments;

	public LocalEnvironments() {
	}

	public LocalEnvironments(File file) {
		setFile(file);
	}

	public void setFile(File file) {
		this.file = file;
		if (file.exists()) {
			load();
		}
	}

	public void load() {
		Bindings b = Scripts.get().createBindings();
		b.put("__Environments", environments);
		Scripts.get().eval(file, b);
	}

	public void write() throws IOException {
		PrintWriter pw = new PrintWriter(new FileOutputStream(file), true);
		try {
			pw.println("__Environments = __Environments");
			pw.println();
			pw.println();
			pw.println("with (JavaImporter(com.jme3.math, org.icelib.beans)) {");

			for (Map.Entry<String, AbstractEnvironmentConfiguration> en : entrySet()) {
				pw.print("\t__Environments.env(\"" + en.getKey() + "\", ");
				Gson gson = new Gson();
				pw.print(gson.toJson(en.getValue()));
				pw.println("\t);");
			}

			pw.println("}");

		} finally {
			pw.close();
		}
	}

}
