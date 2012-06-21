package net.floodlightcontroller.core.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService.Role;

import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;

/**
 * Handles write() with multiple objects implementing 
 * IFloodlightProviderService 
 * 
 * @author 
 */
public class OFProxySwitchImpl extends OFSwitchImpl {

	@Override
	public void write(OFMessage m, FloodlightContext bc) throws IOException {
    	log.debug("in write for {},{}", bc, m);
        Map<OFSwitchImpl,List<OFMessage>> msg_buffer_map = local_msg_buffer.get();
        List<OFMessage> msg_buffer = msg_buffer_map.get(this);
        if (msg_buffer == null) {
            msg_buffer = new ArrayList<OFMessage>();
            msg_buffer_map.put(this, msg_buffer);
        }
    	log.debug("write: msg type {}", m.getType());
        switch (m.getType()) {
    		case PACKET_OUT:
    		case FLOW_MOD:
    		case PORT_MOD:	
               	this.floodlightProxy.handleOutgoingMessage(this, m, bc);
               	break;
            default: 	
            	this.floodlightProvider.handleOutgoingMessage(this, m, bc);
        }
        msg_buffer.add(m);

        if ((msg_buffer.size() >= Controller.BATCH_MAX_SIZE) ||
            ((m.getType() != OFType.PACKET_OUT) && (m.getType() != OFType.FLOW_MOD))) {
            this.write(msg_buffer);
            msg_buffer.clear();
        }
    }
	
	@Override
	public void write(List<OFMessage> msglist, FloodlightContext bc) throws IOException {
    	log.debug("in write for {},{}", bc, msglist);
        for (OFMessage m : msglist) {
            if (role == Role.SLAVE) {
                switch (m.getType()) {
                    case PACKET_OUT:
                    case FLOW_MOD:
                    case PORT_MOD:
                        log.warn("Sending OF message that modifies switch state while in the slave role: {}", m.getType().name());
                        break;
                    default:
                        break;
                }
            }

        	log.debug("write: msg type {}", m.getType());
            // FIXME: Debugging code should be disabled!!!
            // log.debug("Sending message type {} with xid {}", new Object[] {m.getType(), m.getXid()});
            switch (m.getType()) {
            	case PACKET_OUT:
            	case FLOW_MOD:
            	case PORT_MOD:	
        			this.floodlightProxy.handleOutgoingMessage(this, m, bc);
        			break;
            	default: 
        			this.floodlightProvider.handleOutgoingMessage(this, m, bc);
            }     
        }
        this.write(msglist);
    }
}
