package controllers;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.plannifico.PlannificoFactory;
import org.plannifico.PlannificoFactoryProvider;
import org.plannifico.data.PlanningSet;
import org.plannifico.data.UniverseNotExistException;
import org.plannifico.server.ActionNotPermittedException;
import org.plannifico.server.PlanningEngine;
import org.plannifico.server.ServerAlreadyRunningException;
import org.plannifico.server.configuration.XMLBasedConfigurationManager;
import org.plannifico.server.response.RecordsCollectionResponse;

import com.fasterxml.jackson.databind.node.ObjectNode;

import play.*;
import play.libs.Json;
import play.mvc.*;
import play.data.*;

import views.html.*;

public class Application extends Controller {
	
	private static PlannificoFactory factory = PlannificoFactoryProvider.getInstance();
	
	private static PlanningEngine engine = factory.getPlanningEngine();
	
	private static String configurationFile = 
			XMLBasedConfigurationManager.DEFAULT_CONFIGURATION_FILE;
	
	
    public static Result index() {    	
    	
    	Collection<String> universes = engine.getUniverses();
    	
    	Map <String, Long> measureset_records_count = new HashedMap();
    	
    	Map <String, Collection<String>> measuresets = new HashedMap();
    	
    	for (String universe : universes) {    	
    		
    		Logger.debug ("Display universe: " + universe);
    		Logger.debug ("Display measuresets: " + engine.getMeasureSetsNames (universe));
    		
    		Collection <String> measureset_names = engine.getMeasureSetsNames (universe);    		

    		measuresets.put(universe, engine.getMeasureSetsNames (universe));
    	}
	
        return ok (index.render (
        		engine.getStatus(), 
        		universes,
        		measuresets
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
			
			Logger.debug ("getDimRelationships rels.size = " + rels.size());
			
		} catch (UniverseNotExistException e) {
			
			Logger.warn ("Universe does not exist: " + universe);
		}
		
	    return ok (showDimRelationships.render (universe, dimension, rels));
		
	}	
	
	public static Result showDataNavigator (/*
			String universe, 
			String measureset*/) {
    	
		DynamicForm bindedForm = Form.form().bindFromRequest();
		
		String universe = bindedForm.get("universe");
		String measureset = bindedForm.get("measureset");
		
		Logger.info("measureset: " + bindedForm.get("measureset"));
		
    	Map <String, Collection<String>> dimensions = new HashedMap();    	
    	
    	Map <String, Collection<String>> measures = new HashedMap();
        
		Logger.info("showDataNavigator (" + universe + ", " + measureset + ")");
		
		
		try {
			
			dimensions.put(universe, engine.getPlanningDimensions (universe));
			
		} catch (UniverseNotExistException e1) {
			
			Logger.warn ("Universe does not exist: " + universe);
			
		}
			
		try {
			
			measures.put(universe + measureset, 
					engine.getMeasureSetsMeasureNames(universe, measureset));
			
		} catch (UniverseNotExistException e) {
			
			Logger.warn ("Universe does not exist: " + universe);
		}
		
	    return ok (datanavigator.render (universe, measureset, dimensions, measures));		
	}	
	
	public static Result getDataset () {

		DynamicForm bindedForm = Form.form().bindFromRequest();
		
		String universe = bindedForm.get("universe");
		String measureset = bindedForm.get("measureset");
		String measures = bindedForm.get("measures").replace(",", ";");
		String filters = bindedForm.get("filters").replace(",", ";").replace(":", "=");
		String groupby = bindedForm.get("groupby").replace(",", ";");
        
		Logger.debug ("getDataset (" + 
				universe + ", " + 
				measureset + ", " + 
				measures + ", " + 
				filters + ", " + 
				groupby + ")");
		
		ObjectNode result = Json.newObject();
		
		PlanningSet dataset = null;
		
		try {
			
			dataset = engine.getDataSet (universe, measureset, measures, filters, groupby);
			
			Logger.debug ("dataset records.size = " + dataset.getData().size());
			
		} catch (UniverseNotExistException e) {
			
			Logger.warn ("Universe does not exist: " + universe);
		}
		
	    return ok (Json.toJson(new RecordsCollectionResponse (dataset.getData())));
		
	}
	
}

	
