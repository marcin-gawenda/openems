package io.openems.edge.pvmterminal;

import static java.util.Collections.emptyMap;

import java.time.Instant;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.event.propertytypes.EventTopics;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;

import io.openems.edge.bridge.http.api.BridgeHttp;
import io.openems.edge.bridge.http.api.BridgeHttpFactory;
import io.openems.edge.bridge.http.api.HttpError;
import io.openems.edge.bridge.http.api.HttpMethod;
import io.openems.edge.bridge.http.api.HttpResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonParseException;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "io.openems.edge.pvmterminal", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
@EventTopics({ //
		EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE, //
})
public class PVMTerminalClientImpl extends AbstractOpenemsComponent implements PVMTerminalClient, OpenemsComponent, EventHandler {

	private final Logger log = LoggerFactory.getLogger(PVMTerminalClientImpl.class);

	@Reference
	private BridgeHttpFactory httpBridgeFactory;
	
	private BridgeHttp httpBridge;

	private Config config = null;

	public PVMTerminalClientImpl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				PVMTerminalClient.ChannelId.values() //
		);
	}

	@Activate
	private void activate(ComponentContext context, Config config) {
		super.activate(context, config.id(), config.alias(), config.enabled());
		this.config = config;
		
		this.httpBridge = this.httpBridgeFactory.get();
		// Setup Basic Authentication headers
//		String auth = config.username() + ":" + config.password();
//		String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
//		this.headers = Map.of("Authorization", "Basic " + encodedAuth, "Accept", "text/html");

		if (this.isEnabled()) {
			this.logInfo(this.log, "PVMTerminal client activated to poll '" + this.config.url() + "' every X cycle(s)");
			
			// Subscribe for updates every N cycles
			this.httpBridge.subscribeCycle(this.config.cycleCnt(),
					() -> new BridgeHttp.Endpoint(this.config.url(), HttpMethod.GET, BridgeHttp.DEFAULT_CONNECT_TIMEOUT,
							BridgeHttp.DEFAULT_READ_TIMEOUT, null, emptyMap()), this::handleSuccess, this::handleError);
		}
	}
	
	@Deactivate
	protected void deactivate() {
		super.deactivate();
		if (this.httpBridge != null) {
			this.httpBridgeFactory.unget(this.httpBridge);
			this.httpBridge = null;
		}
	}

	@Override
	public void handleEvent(Event event) {
		if (!this.isEnabled()) {
			return;
		}
		this.logInfo(this.log, "Received event response" + event.getTopic() + "....\n" + event);
		switch (event.getTopic()) {
		case EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE:
			// TODO: fill channels
			this.logInfo(this.log, "Received event response ....\n" + event);
			break;
		}
	}

	@Override
	public String debugLog() {
		return "\n\tid: " + this.id()						
		+ ", SN: " + this.channel(PVMTerminalClient.ChannelId.SN).value().asString()
		+ ", LAST_SUCCESS_AT: " + this.channel(PVMTerminalClient.ChannelId.LAST_SUCCESS_AT).value().asString()
		+ ", LAST_FAILED_AT: " + this.channel(PVMTerminalClient.ChannelId.LAST_FAILED_AT).value().asString()
		+ ", RS485_DATA: " + this.channel(PVMTerminalClient.ChannelId.RS485_DATA).value().asString()
		;		
	}
	
	private void handleSuccess(HttpResponse<String> result) {
		if (result == null || result.data() == null) {
			this.channel(PVMTerminalClient.ChannelId.SN).setNextValue(null);
			this.channel(PVMTerminalClient.ChannelId.RS485_DATA).setNextValue(null);
			this.channel(PVMTerminalClient.ChannelId.LAST_FAILED_AT).setNextValue(Instant.now());
			this.logError(this.log, "Received null response");
			return;
		}

		try {			
            JsonNode json = new ObjectMapper().readTree(result.data());
            String sn = json.get("info").get("pvsno").asText(null);
            this.channel(PVMTerminalClient.ChannelId.SN).setNextValue(sn);
            
            String rs485Data = json.get("rs485").toString();
            this.channel(PVMTerminalClient.ChannelId.RS485_DATA).setNextValue(rs485Data);
			
			this.channel(PVMTerminalClient.ChannelId.LAST_SUCCESS_AT).setNextValue(Instant.now());
		} catch (JsonParseException parseEx) { 
			this.channel(PVMTerminalClient.ChannelId.SN).setNextValue(null);
			this.channel(PVMTerminalClient.ChannelId.LAST_FAILED_AT).setNextValue(Instant.now());
			this.channel(PVMTerminalClient.ChannelId.RS485_DATA).setNextValue(null);			
			this.logError(this.log, "Failed to parse response: " + parseEx.getMessage());
		} catch (Exception e) {
			this.channel(PVMTerminalClient.ChannelId.SN).setNextValue(null);
			this.channel(PVMTerminalClient.ChannelId.LAST_FAILED_AT).setNextValue(Instant.now());
			this.channel(PVMTerminalClient.ChannelId.RS485_DATA).setNextValue(null);
			this.logError(this.log, "Failed to get response: " + e.getClass() + " " + e.getMessage());
		}
	}

	private void handleError(HttpError error) {
		this.channel(PVMTerminalClient.ChannelId.SN).setNextValue(null);
		this.channel(PVMTerminalClient.ChannelId.LAST_FAILED_AT).setNextValue(Instant.now());
		this.channel(PVMTerminalClient.ChannelId.RS485_DATA).setNextValue(null);
		this.logError(this.log, "HTTP request failed: " + error.getMessage());
	}	
	
}
