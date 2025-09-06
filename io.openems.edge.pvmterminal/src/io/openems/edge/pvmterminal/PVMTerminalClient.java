package io.openems.edge.pvmterminal;

import org.osgi.service.event.EventHandler;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.PersistencePriority;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.OpenemsComponent;


public interface PVMTerminalClient extends OpenemsComponent, EventHandler {

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
		
		/*
		 * "info.pvsno": "0163022F" 
		 */
		SN(Doc.of(OpenemsType.STRING) //
				.accessMode(AccessMode.READ_ONLY)//				
				.persistencePriority(PersistencePriority.VERY_LOW)
				.text("PVMonitor Terminal serial number")),		

		
		LAST_SUCCESS_AT(Doc.of(OpenemsType.STRING) //
				.accessMode(AccessMode.READ_ONLY)//				
				.persistencePriority(PersistencePriority.VERY_LOW)
				.text("Timestamp of last successfull response")),			

		LAST_FAILED_AT(Doc.of(OpenemsType.STRING) //
				.accessMode(AccessMode.READ_ONLY)//				
				.persistencePriority(PersistencePriority.VERY_LOW)
				.text("Timestamp of last failed response")),			

		RS485_DATA(Doc.of(OpenemsType.STRING) //
				.accessMode(AccessMode.READ_ONLY)//				
				.persistencePriority(PersistencePriority.VERY_LOW)
				.text("Part of JSON response rs485.emetersread")),			
		;

		private final Doc doc;

		private ChannelId(Doc doc) {
			this.doc = doc;
		}

		@Override
		public Doc doc() {
			return this.doc;
		}
	}

}
