package net.floodlightcontroller.fvacceptor;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;
import org.openflow.protocol.factory.BasicFactory;
import org.openflow.util.HexString;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IHAListener;
import net.floodlightcontroller.core.IInfoProvider;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchFilter;
import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.IListener.Command;
import net.floodlightcontroller.core.util.ListenerDispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base for a proxy layer between core controller and modules. 
 * @author 
 */
public class FVController implements IFloodlightProviderService {

	protected BasicFactory factory;
	protected IFloodlightProviderService controller;
	
	protected static Logger log = LoggerFactory.getLogger(FVController.class);
	
	protected ConcurrentMap<OFType, 
		ListenerDispatcher<OFType,IOFMessageListener>> 
        	messageListeners;
	protected Set<IOFSwitchListener> switchListeners;
	protected ConcurrentHashMap <Long, IOFSwitch> switches;
	
	@Override
	public void addHAListener(IHAListener listener) {
		// TODO Auto-generated method stub
		controller.addHAListener(listener);
	}
	
	/**
	 * propagates switch join/leave to modules 
	*/
	public void updateSwitch(IOFSwitch sw, boolean add) {
		if (add)
			this.switches.put(sw.getId(), sw);
		else
			this.switches.remove(sw.getId(), sw);

		if (switchListeners != null) {
			for (IOFSwitchListener listener : switchListeners) {
				if (add)
					listener.addedSwitch(sw);
				else
					listener.removedSwitch(sw);
			}
		}
	}
	
	@Override
	public void addInfoProvider(String type, IInfoProvider provider) {
		// TODO Auto-generated method stub
		controller.addInfoProvider(type, provider);
	}

	@Override
	public synchronized void addOFMessageListener(OFType type, IOFMessageListener listener) {
        ListenerDispatcher<OFType, IOFMessageListener> ldd = 
            messageListeners.get(type);
        if (ldd == null) {
            ldd = new ListenerDispatcher<OFType, IOFMessageListener>();
            messageListeners.put(type, ldd);
        }
        ldd.addListener(type, listener);
        log.debug("added message listener {}", listener);		
	}

	@Override
	public synchronized void addOFSwitchListener(IOFSwitchListener listener) {
		// TODO Auto-generated method stub
		log.debug("added switch listener {}", listener);
		this.switchListeners.add(listener);
	}

	@Override
	public String getControllerId() {
		// TODO Auto-generated method stub
		return controller.getControllerId();
	}

	@Override
	public Map<String, Object> getControllerInfo(String type) {
		// TODO Auto-generated method stub
		return controller.getControllerInfo(type);
	}

	@Override
	public Map<String, String> getControllerNodeIPs() {
		// TODO Auto-generated method stub
		return controller.getControllerNodeIPs();
	}

	/**
	 * return just the listeners that this proxy knows about 
	 */
	@Override
	public Map<OFType, List<IOFMessageListener>> getListeners() {
		// TODO Auto-generated method stub
		Map<OFType, List<IOFMessageListener>> lers = 
            new HashMap<OFType, List<IOFMessageListener>>();
        for(Entry<OFType, ListenerDispatcher<OFType, IOFMessageListener>> e : 
            messageListeners.entrySet()) {
            lers.put(e.getKey(), e.getValue().getOrderedListeners());
        }
        return Collections.unmodifiableMap(lers);
	}

	@Override
	public BasicFactory getOFMessageFactory() {
		// TODO Auto-generated method stub
		return controller.getOFMessageFactory();
	}

	@Override
	public Map<Long, IOFSwitch> getSwitches() {
		// TODO Auto-generated method stub
		
		return Collections.unmodifiableMap(this.switches);
	}

	@Override
	public void handleOutgoingMessage(IOFSwitch sw, OFMessage m,
			FloodlightContext bc) {
		// TODO Auto-generated method stub
		log.debug("handleOutgoing");
		
		List<IOFMessageListener> listeners = null;
        if (messageListeners.containsKey(m.getType())) {
            listeners = 
                    messageListeners.get(m.getType()).getOrderedListeners();
        }
            
        if (listeners != null) {                
            for (IOFMessageListener listener : listeners) {
                if (listener instanceof IOFSwitchFilter) {
                    if (!((IOFSwitchFilter)listener).isInterested(sw)) {
                        continue;
                    }
                }
                if (Command.STOP.equals(listener.receive(sw, m, bc))) {
                    break;
                }
            }
        }
	}

	@Override
	public boolean injectOfMessage(IOFSwitch sw, OFMessage msg) {
		// TODO Auto-generated method stub
		return injectOfMessage(sw, msg, null);
	}

	@Override
	public boolean injectOfMessage(IOFSwitch sw, OFMessage msg,
			FloodlightContext bContext) {
		log.debug("in injectOFMessage");
        
        if (controller.getSwitches().containsKey(sw.getId())) return false;
        
        try {
            // Pass Floodlight context to the handleMessages()
            handleMessage(sw, msg, bContext);
        } catch (IOException e) {
            log.error("Error reinjecting OFMessage on switch {}", 
                      HexString.toHexString(sw.getId()));
            return false;
        }
        return true;
	}

	public void handleMessage(IOFSwitch sw, OFMessage msg, 
			FloodlightContext ctxt) throws IOException {
		log.debug("in handleMessage");
		Command cmd = null;
		List<IOFMessageListener> listeners = null;
		
        if (messageListeners.containsKey(msg.getType())) {
            listeners = messageListeners.get(msg.getType()).
                    getOrderedListeners();
        }
        log.debug("handleMsg: listeners for {}: {}",msg, listeners);
        
        if (listeners != null) {
        	for (IOFMessageListener listener : listeners) {
        		if (listener instanceof IOFSwitchFilter) {
        			if (!((IOFSwitchFilter)listener).isInterested(sw)) {
        				continue;
        			}
        		}
        		log.debug("handleMsg recv on listener {}", listener);
        		cmd = listener.receive(sw, msg, ctxt);
        		if (Command.STOP.equals(cmd)) {
                    break;
                }
        	}
        } else {
        	log.error("Unhandled OF Message: {} from {}", msg, sw);
        }      
	}
	
	@Override
	public void removeHAListener(IHAListener listener) {
		// TODO Auto-generated method stub
		controller.removeHAListener(listener);
	}

	@Override
	public void removeInfoProvider(String type, IInfoProvider provider) {
		// TODO Auto-generated method stub
		controller.removeInfoProvider(type, provider);
	}

	@Override
    public synchronized void removeOFMessageListener(OFType type,
                                                     IOFMessageListener listener) {
        ListenerDispatcher<OFType, IOFMessageListener> ldd = 
            messageListeners.get(type);
        if (ldd != null) {
            ldd.removeListener(listener);
        }
    }
	
	public void removeOFMessageListeners(OFType type, IOFMessageListener listener) {
		// TODO Auto-generated method stub
		messageListeners.remove(type);
	}

	@Override
	public void removeOFSwitchListener(IOFSwitchListener listener) {
		// TODO Auto-generated method stub
		this.switchListeners.remove(listener);
	}
	
	//based on what's in Controller.java
	public void init(IFloodlightProviderService controller) {
		this.controller = controller;
		this.messageListeners =
            new ConcurrentHashMap<OFType, 
            	ListenerDispatcher<OFType, IOFMessageListener>>();
		this.switchListeners = new CopyOnWriteArraySet<IOFSwitchListener>();
		this.switches = new ConcurrentHashMap<Long, IOFSwitch>();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void terminate() {
		// TODO Auto-generated method stub
		System.exit(1);
	}

	@Override
	public Role getRole() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRole(Role role) {
		// TODO Auto-generated method stub
		
	}

}
