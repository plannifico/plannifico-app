//onload function
$(function() {
	
	var dataNavigationInvoker = new DataNavigationInvoker();
	
	console.log ("$('.universe-name-container').length " + $(".universe-name-container").length);
	
	if ($(".universe-name-container").length == 1) {
		
		dataNavigationInvoker.universe = $(".universe-name-container")[0].id;
		
		console.log ("dataNavigationInvoker.universe = " + $(".universe-name-container")[0].id);
	}
	
	if ($(".measureset-name-container").length == 1) {
		
		dataNavigationInvoker.measureSet = $(".measureset-name-container")[0].id;
		
		console.log ("dataNavigationInvoker.measureSet = " + $(".measureset-name-container")[0].id);
	}
	
	$(".draggable-dim").draggable({
		revert:"invalid", 
		scope: ".pivot-selection",
		ursor: 'move'
	});
	
	$(".droppable").droppable({
		
		scope: ".pivot-selection",
		
		greedy: "true",
			
		drop: function( event, ui ) {
			
			console.log ("dropped " + $(ui.draggable).attr ("id"));
			
			var dropTarget = $(this);

			jQuery('<div>',{                                                
				}).appendTo(dropTarget)
					.attr("id",$(ui.draggable).attr ("id"))
					.append($(ui.draggable).text());
			
			ui.draggable.remove();
			
			console.log ("dropTarget.id " + $(dropTarget).attr ("id"));
			
			var target_id = $(dropTarget).attr ("id");
			
			if ((target_id == "pivot-selection-rows") || (target_id == "pivot-selection-cols")) {
				
				console.log ("dataNavigationInvoker.groupby add " + $(ui.draggable).text());
						
				dataNavigationInvoker.groupby.push ($(ui.draggable).text());					
			}	
				
			if (target_id == "pivot-selection-measures") {
				
				console.log ("dataNavigationInvoker.measures add " + $(ui.draggable).text());
				
				dataNavigationInvoker.measures.push ($(ui.draggable).text());
			}
				
			
			//dropTarget.append ("<p>test</p>");
			
			//$( this ).addClass( "ui-state-highlight" ).find( "p" ).html( "Dropped!" );
		}
	});	
	
	$("#invoke-get-data-btn").click(function () {
		alert ("click");
		dataNavigationInvoker.invoke ();
	});
});

//Datanavigation invoker object

function DataNavigationInvoker () {
	
	this.URL = "getDataset?";
	this.universe = "";
	this.measureSet = "";
	this.measures = [];
	this.groupby = [];
	this.filters = [];
}

DataNavigationInvoker.prototype.invoke = function () {
	
	console.log ("DataNavigationInvoker::invoke");
	
	var measure_par = "";
	var groupby_par = "";
	
	this.measures.map( function (measure) {
		
		measure_par += measure + ",";
	});
	
	measure_par = measure_par.substring(0, measure_par.length - 1);
	
	this.groupby.map( function (groupby_attr) {
		
		groupby_par += groupby_attr + ",";
	});
	
	groupby_par = groupby_par.substring(0, groupby_par.length - 1);
	
	console.log ("measure_par " + measure_par);
	console.log ("groupby_par " + groupby_par);
	
	var url_par = this.URL;
	var universe_par = this.universe;
	var measureset_par = this.measureset;
	
	var complete_url = 
		this.URL + 
		"universe=" + this.universe + 
		"&measureset=" + this.measureset + 
		"&measures=" + measure_par +
		"&groupby=" + groupby_par +
		"&filters=" + "Period.Year:2013,Scenario.Scenario_Name:Budget";
		
	console.log ("URL " + complete_url);
	
	$.ajax({
	    url: complete_url,
	    
	    dataType: 'jsonp',
	    	
	    success: function (results) {
	    	
	        console.log (results.records[0].measures[0].QUANTITY);
	    }
	});
	
}