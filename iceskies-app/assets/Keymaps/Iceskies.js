__KeyMaps = __KeyMaps;
with(JavaImporter(org.icescene.io, com.jme3.input.controls, com.jme3.input)) {
	__KeyMaps.Console = {
		    trigger : new KeyTrigger(KeyInput.KEY_GRAVE),
		    category : "Other"
	};
	__KeyMaps.Options = {
		    trigger : new KeyTrigger(KeyInput.KEY_O),
		    category : "Windows"
	};
};
