package io.openems.edge.bridge.modbus;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Objects;

import io.openems.edge.bridge.modbus.api.LogVerbosity;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.service.event.propertytypes.EventTopics;
import org.osgi.service.metatype.annotations.Designate;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.io.ModbusRTUTCPTransport;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.io.ModbusTransaction;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;

import io.openems.common.exceptions.OpenemsException;
import io.openems.common.utils.InetAddressUtils;
import io.openems.edge.bridge.modbus.api.AbstractModbusBridge;
import io.openems.edge.bridge.modbus.api.BridgeModbus;
import io.openems.edge.bridge.modbus.api.BridgeModbusTcp;
import io.openems.edge.bridge.modbus.api.Config;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a service for connecting to, querying and writing to a Modbus/TCP
 * device.
 */
@Designate(ocd = ConfigRtuOverTcp.class, factory = true)
@Component(//
		name = "Bridge.Modbus.RtuOverTcp", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
@EventTopics({ //
		EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE, //
		EdgeEventConstants.TOPIC_CYCLE_EXECUTE_WRITE //
})
public class BridgeModbusRtuOverTcpImpl extends AbstractModbusBridge
		implements BridgeModbus, BridgeModbusTcp, OpenemsComponent, EventHandler {

	private final static Logger log = LoggerFactory.getLogger(BridgeModbusRtuOverTcpImpl.class);
	
	/** The configured IP address. */
	private InetAddress ipAddress = null;
	private int port;	
	private boolean shouldSkip = false;
	private int noSkipIdx = 0;
	private long cycleIdx = 0;

	@Reference
	protected ConfigurationAdmin cm;

	public BridgeModbusRtuOverTcpImpl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				BridgeModbus.ChannelId.values(), //
				BridgeModbusTcp.ChannelId.values() //
		);
	}

	@Activate
	private void activate(ComponentContext context, ConfigRtuOverTcp config) throws UnknownHostException {
		super.activate(context, new Config(config.id(), config.alias(), config.enabled(), config.logVerbosity(),
				config.invalidateElementsAfterReadErrors()));
		this.applyConfig(config);
	}

	@Modified
	private void modified(ComponentContext context, ConfigRtuOverTcp config) throws UnknownHostException {
		super.modified(context, new Config(config.id(), config.alias(), config.enabled(), config.logVerbosity(),
				config.invalidateElementsAfterReadErrors()));
		this.applyConfig(config);
		this.closeModbusConnection();
	}

	private void applyConfig(ConfigRtuOverTcp config) {
		this.setIpAddress(InetAddressUtils.parseOrNull(config.ip()));
		this.port = config.port();
        int coreCycleTime = 1000;
        try {
			// the default cycleTime is not persisted and won't be returned, only modified cycleTime will!
			coreCycleTime = (int) (Integer) this.cm.getConfiguration("Core.Cycle").getProperties().get("cycleTime");
        } catch (Exception e) {
			this.logCycle("cycleTime reading failed, use default value " + coreCycleTime);
        }
        this.noSkipIdx = (int)Math.ceil(config.intervalBetweenAccesses() * 1.0 / coreCycleTime);
		this.noSkipIdx = Math.max(this.noSkipIdx, 1);
		this.logCycle("applyConfig: cycleTime=" + coreCycleTime
			+ ", interval=" + config.intervalBetweenAccesses()
			+ ", noSkipIdx=" + this.noSkipIdx);
	}

	@Override
	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	public void closeModbusConnection() {
		if (this._connection != null) {
			this._connection.close();
			this._connection = null;
		}
	}

	@Override
	public ModbusTransaction getNewModbusTransaction() throws OpenemsException {
		var connection = this.getModbusConnection();
		var transaction = new ModbusTCPTransaction(connection);
		transaction.setRetries(AbstractModbusBridge.DEFAULT_RETRIES);
//		transaction.setRetries(1);
		return transaction;
		
//		ModbusTransaction transaction;
//		
//        try (Socket socket = new Socket(ipAddress.getHostAddress(), port)) {
//            socket.setSoTimeout(5000);
//
//            // Używamy specjalnej klasy transportu RTU-over-TCP
//            ModbusRTUTCPTransport transport = new ModbusRTUTCPTransport(socket);            
//            transport.setTimeout(10000); // Ustawiamy timeout na 10 sekund
//            
//            transaction = transport.createTransaction();
//            transaction.setRetries(AbstractModbusBridge.DEFAULT_RETRIES);
//            
//        } catch (Exception e) {
//            log.error("Błąd połączenia lub komunikacji: id: ", e);
//            throw new OpenemsException(
//					"Exception - ModbusException - Setting RTU over TCP for connection to [" + ipAddress.getHostAddress() + "] failed: " + e.getMessage());
//        } 
//        
//        return transaction;
		
	}

	private TCPMasterConnection _connection = null;

	private synchronized TCPMasterConnection getModbusConnection() throws OpenemsException {
		log.debug("Mag: getModbusConnection");
		if (this._connection == null) {
			/*
			 * create new connection
			 */
			var connection = new TCPMasterConnection(this.getIpAddress());
			connection.setPort(this.port);
			connection.setTimeout(5*AbstractModbusBridge.DEFAULT_TIMEOUT);
			try {
				connection.setUseRtuOverTcp(true);
			} catch (Exception e) {
				throw new OpenemsException(
						"Setting RTU over TCP for connection to [" + this.getIpAddress().getHostAddress() + "] failed: " + e.getMessage());
			}
			
			this._connection = connection;
		}
		if (!this._connection.isConnected()) {
			try {
				this._connection.connect();
			} catch (Exception e) {
				
				log.error("Mag:", e);
				throw new OpenemsException(
						"Connection to [" + this.getIpAddress().getHostAddress() + "] failed: " + e.getMessage());
			}
			this._connection.getModbusTransport().setTimeout(5*AbstractModbusBridge.DEFAULT_TIMEOUT); // TODO
		}
		return this._connection;
	}

	@Override
	public InetAddress getIpAddress() {
		return this.ipAddress;
	}

	public void setIpAddress(InetAddress ipAddress) {
		this.ipAddress = ipAddress;
	}

	private boolean isNewCycle(Event event) {
		return Objects.equals(event.getTopic(), EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE);
	}

	private void logCycle(String msg) {
		if (this.getLogVerbosity() == LogVerbosity.DEBUG_LOG) {
			this.logInfo(this.log, msg);
		}
	}

	@Override
	public void handleEvent(Event event) {
		if (!this.isEnabled()) {
			return;
		}

		if (this.isNewCycle(event)) {
			this.shouldSkip = this.cycleIdx % this.noSkipIdx != 0;
			this.logCycle("handleEvent: Cycle " + this.cycleIdx + (this.shouldSkip ? " will" : " won't") + " be skipped");
			this.cycleIdx++;
		}
		if (this.shouldSkip) {
			return;
		}

		switch (event.getTopic()) {
			case EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE:
				this.worker.onBeforeProcessImage();
				break;
			case EdgeEventConstants.TOPIC_CYCLE_EXECUTE_WRITE:
				this.worker.onExecuteWrite();
				break;
		}
	}
}