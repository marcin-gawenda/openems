package io.openems.edge.pvmterminal;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(//
		name = "PVMTerminal REST API Client", //
		description = "Implements client of PVMTerminal TEST API")
@interface Config {

	@AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
	String id() default "pvmt0";

	@AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
	String alias() default "";

	@AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
	boolean enabled() default true;
	
	@AttributeDefinition(name = "URL", description = "The URL of PVMTerminal REST API")
	String url() default "http://192.168.15.17:8006/public";
	
	@AttributeDefinition(name = "CycleCnt", description = "Fetch data every N cycle")
	int cycleCnt() default 10;
	
	String webconsole_configurationFactory_nameHint() default "PVMTerminal REST API Client [{id}]";

}