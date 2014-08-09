package controllers;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.plannifico.PlannificoFactory;
import org.plannifico.PlannificoFactoryProvider;
import org.plannifico.data.UniverseNotExistException;
import org.plannifico.server.ActionNotPermittedException;
import org.plannifico.server.PlanningEngine;
import org.plannifico.server.ServerAlreadyRunningException;
import org.plannifico.server.configuration.XMLBasedConfigurationManager;


import play.*;
import play.mvc.*;
import play.data.*;

import views.html.*;

public class Application extends Controller {
	
	private static PlannificoFactory factory = PlannificoFactoryProvider.getInstance();
	
	private static PlanningEngine engine = factory.getPlanningEngine();
	
	private static String configurationFile = 
			XMLBasedConfigurationManager.DEFAULT_CONFIGURATION_FILE;
	
    public static Result index() {
    	
    	Map <String, Collection<String>> measuresets = new HashedMap();
    	
    	Map <String, Collection<String>> dimensions = new HashedMap();
    	
    	Collection<String> universes = engine.getUniverses();
    	Map <String, Collection<String>> measures = new HashedMap();
    	
    	for (String universe : universes) {    	
    		
    		measuresets.put(universe, engine.getMeasureSetsNames (universe));
    		
    		try {
				
    			dimensions.put(universe, engine.getPlanningDimensions (universe));
				
			} catch (UniverseNotExistException e1) {
				
				Logger.warn ("Universe does not exist: " + universe);
				
			}
    		
    		Logger.debug ("Display universe: " + universe);
    		Logger.debug ("Display measuresets: " + engine.getMeasureSetsNames (universe));
    		
    		for (String measureset: engine.getMeasureSetsNames (universe)) {
    			
    			try {
					
    				measures.put(universe + measureset, 
							engine.getMeasureSetsMeasureNames(universe, measureset));
					
				} catch (UniverseNotExistException e) {
					
					Logger.warn ("Universe does not exist: " + universe);
				}
    		}
    	}
	
        return ok (index.render (
        		engine.getStatus(), 
        		universes,
        		dimensions,
        		measuresets,
        		measures
        		));
    }

	public static Result submitStart () {
        
		Logger.info("starting...");
		
		try {
			
			if( engine.start (configurationFile) == 1)
				return ok (showresult.render ("Severe Error starting the server: read log for more information"));
			
		} catch (ServerAlreadyRunningException e) {
			
			return ok (showresult.render ("Failure: Server already running"));
		}
		
		Logger.info ("starting [DONE]");
		
		return index();
    }
	
	public static Result submitStop () {
        
		Logger.info("stopping...");
		
		engine.stop();
		
		return index();		
    }
	
	public static Result getMeasureSetCount () {
        
		Logger.info("get measureset count...");
		
		DynamicForm bindedForm = Form.form().bindFromRequest();
		
		if (engine.getStatus () != PlanningEngine.STARTED)
			return ok(showresult.render ("Error: Server is not started"));
		
		int result = engine.getMeasureSetsNumber (bindedForm.get("universe"));
	    
	    return ok (showresult.render ("Measure set count = " + result));		
	}
	

	public static Result getMeasureSetRecordCount () {
        
		Logger.info("get measureset count...");
		
		DynamicForm bindedForm = Form.form().bindFromRequest();
		
		Logger.info("universe: " + bindedForm.get("universe"));
		Logger.info("measureset: " + bindedForm.get("measureset"));
		
		if (engine.getStatus () != PlanningEngine.STARTED)
		return ok(showresult.render ("Error: Server is not started"));
		
		long result;
		
		try {
		
			result = engine.getMasureSetRecordsNumber (bindedForm.get("universe"), bindedForm.get("measureset"));
		
			return ok (showresult.render ("Measure set count = " + result));
			
		} catch (ActionNotPermittedException | UniverseNotExistException e) {
			
			return ok (showresult.render ("Error = " + e.getMessage()));
		}    
	    		
	}
	
	public static Result getAggregatedValue () {
        
		Logger.info("getAggregatedValue");
		
		DynamicForm bindedForm = Form.form().bindFromRequest();
		
		Logger.debug (bindedForm.get("universe"));
	    
	    return ok (showresult.render ("Aggregated"));
		
	}	
	
	public static Result getDimRelationships (String universe, String dimension) {
        
		Logger.info("getDimRelationships (" + universe + ", " + dimension + ")");
		
		Map<String, Collection<String>> rels = new HashedMap();
		
		try {
			
			rels = engine.getAllDimensionRelationships(universe, dimension);
			
			Logger.debug("rels.size = " + rels.size());
			
		} catch (UniverseNotExistException e) {
			
			Logger.warn ("Universe does not exist: " + universe);
		}
		
	    return ok (showDimRelationships.render (universe, dimension, rels));
		
	}	
	
}

	
