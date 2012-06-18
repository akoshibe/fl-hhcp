package net.floodlightcontroller.fvacceptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IFloodlightProxy;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.internal.FVController;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * prototype FlowVisor OFSwitchAcceptor Module  
 * @author native
 */
public class FVAcceptor implements IFloodlightModule, IOFMessageListener,
		IOFSwitchListener {
	
	FVController fvcontroller; 
	
	protected static Logger log = LoggerFactory.getLogger(FVAcceptor.class);
	protected IFloodlightProviderService floodlightProvider;
	

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		// TODO Auto-generated method stub
		Collection<Class<? extends IFloodlightService>> l =
	        new ArrayList<Class<? extends IFloodlightService>>();
	    l.add(IFloodlightProviderService.class);
	    return l;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		// TODO Auto-generated method stub
		Collection<Class<? extends IFloodlightService>> services =
              new ArrayList<Class<? extends IFloodlightService>>(1);
		services.add(IFloodlightProxy.class);
		return services;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		// TODO Auto-generated method stub
		fvcontroller = new FVController();
        
        Map<Class<? extends IFloodlightService>,
            IFloodlightService> m = 
                new HashMap<Class<? extends IFloodlightService>,
                            IFloodlightService>();
        m.put(IFloodlightProxy.class, fvcontroller);
        return m;
	}

	@Override
	public void init(FloodlightModuleContext context)
			throws FloodlightModuleException {
		// TODO Auto-generated method stub
		
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
		fvcontroller.init(floodlightProvider);
	}

	@Override
	public void startUp(FloodlightModuleContext context) {
		// TODO Auto-generated method stub
		//add all OFTypes...is this legal?
		for(OFType type : OFType.values())
			floodlightProvider.addOFMessageListener(type, this);
		
		floodlightProvider.addOFSwitchListener(this);
	}

	@SuppressWarnings("finally")
	@Override
	public net.floodlightcontroller.core.IListener.Command receive(
			IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		// TODO Auto-generated method stub
		log.debug("got message {}, sw={}", msg, sw);
		log.debug("cntx={}", cntx);
		try {
			fvcontroller.handleMessage(sw, msg, cntx);
		} 
		finally {
			return Command.CONTINUE;
		}
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "FVAcceptor";
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addedSwitch(IOFSwitch sw) {
		// TODO Auto-generated method stub
		fvcontroller.updateSwitch(sw, true);
		log.info("switch {} joined", sw.getId());	
	}

	@Override
	public void removedSwitch(IOFSwitch sw) {
		// TODO Auto-generated method stub
		fvcontroller.updateSwitch(sw, false);
		log.info("switch {} left", sw.getId());	
	}

}
