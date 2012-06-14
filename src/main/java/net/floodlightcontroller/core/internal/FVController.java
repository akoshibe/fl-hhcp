package net.floodlightcontroller.core.internal;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;
import org.openflow.protocol.factory.BasicFactory;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProxy;
import net.floodlightcontroller.core.IHAListener;
import net.floodlightcontroller.core.IInfoProvider;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.util.ListenerDispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FVController implements IFloodlightProxy {

	protected BasicFactory factory;
	protected static Logger log = LoggerFactory.getLogger(FVController.class);
	
	protected ConcurrentMap<OFType, 
		ListenerDispatcher<OFType,IOFMessageListener>> 
        	messageListeners;
	protected Set<IOFSwitchListener> switchListeners;
	
	@Override
	public void addHAListener(IHAListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addInfoProvider(String type, IInfoProvider provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public synchronized void addOFMessageListener(OFType type, IOFMessageListener listener) {
        ListenerDispatcher<OFType, IOFMessageListener> ldd = 
            messageListeners.get(type);
        log.info("ldd = {}", ldd);
        if (ldd == null) {
            ldd = new ListenerDispatcher<OFType, IOFMessageListener>();
            messageListeners.put(type, ldd);
        }
        ldd.addListener(type, listener);
        log.info("ldd = {}", ldd);
        log.info("added message listener {}", listener);		
	}

	@Override
	public synchronized void addOFSwitchListener(IOFSwitchListener listener) {
		// TODO Auto-generated method stub
		log.info("added switch listener {}", listener);
		this.switchListeners.add(listener);
	}

	@Override
	public String getControllerId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> getControllerInfo(String type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> getControllerNodeIPs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<OFType, List<IOFMessageListener>> getListeners() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BasicFactory getOFMessageFactory() {
		// TODO Auto-generated method stub
		return factory;
	}

	@Override
	public Role getRole() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Long, IOFSwitch> getSwitches() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleOutgoingMessage(IOFSwitch sw, OFMessage m,
			FloodlightContext bc) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean injectOfMessage(IOFSwitch sw, OFMessage msg) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean injectOfMessage(IOFSwitch sw, OFMessage msg,
			FloodlightContext bContext) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeHAListener(IHAListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeInfoProvider(String type, IInfoProvider provider) {
		// TODO Auto-generated method stub

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
	
	//based ion what's in Controller.java
	public void init() {
		this.messageListeners =
            new ConcurrentHashMap<OFType, 
            	ListenerDispatcher<OFType, IOFMessageListener>>();
		this.switchListeners = new CopyOnWriteArraySet<IOFSwitchListener>();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRole(Role role) {
		// TODO Auto-generated method stub

	}

	@Override
	public void terminate() {
		// TODO Auto-generated method stub

	}

}
