
/******************************************************************************
 * Init/Core
 */

 
/*
	argsObj = {
		frets: Number,
		strings: Number,
		selectors: Array<String, String>,
		interval: {
			notation: Array<Number, String>,
			maxIndex: Number,
			tuning: Array<Number, Number> || CSV || Json Array(String),
		},
		scale: {
			values: Array<Number, Number> || CSV || Json Array(String),
			root: Number
			select: boolean
		},
		display: {
			selector: function(ContextWrapper, Note, x, y),
			inlay: function(ContextWrapper, x, y, width, height)
		}
	}
*/
var Fingerboard = window.Fingerboard = function($canvas, argsObj) {
  
	var events = (function(){
		var listeners = {
			// Returns the note that the mouse is parked on when the canvas is clicked.
			"noteclick": [],
			// Triggers every time that the mouse hovers on a different note.
			"notehover": [],
			// Whenever a change is done to an element, this event is triggered.
			// Some of the internal logic won't trigger this event at all.
			"modelchange": []
		}
    
		var self = {
			broadcast: function(event, callback) {
				//console.log('event',event)
				if(listeners[event]) {
					listeners[event].forEach(function(listener) {
						listener(callback())
					})
				}
			},
			on: function(event, callback) {
				if(!listeners[event]) 
					listeners[event] = []
				listeners[event].push(callback)
			}
		}
    
		// Construct event stuff
		for(var key in listeners) {
			self[key] = (function(key){
				return function(callback){
					listeners[key].push(callback)
				}
			})(key)
		}
		
		return self
	})()
  
	// Expose the events to the Fingerboard object.
	for(var key in events) {
		this[key] = events[key]
	}
  
	var model = new Fingerboard.Model(argsObj, events),
		view = new Fingerboard.View(argsObj, $canvas, model, events)
		
	// This is basically to change model "settings" only.
	this.set = model.set
	
	// Get the note from the 2 dimensional array of notes.
	this.getNoteFor = function(fret, string) {
		return getNoteFor(fret, string).clone()
	}
	
	// Set the note from the 2 dimensional array of notes. This will trigger the
	// modelchange event.
	this.setNoteFor = function(fret, string, args) {
		var internal = getNoteFor(fret, string)
		for(var key in args)
			internal[key] = args[key]
		// broadcast changes to listeners
		events.broadcast('modelchange', function() { return args })
	}
	
	this.frets = function(){
		return model.frets
	}
	
	this.strings = function(){
		return model.strings
	}
	
	this.tuning = function(){
		// the tuning is reversed internally.
		var tuning = model.settings.tuning.slice()
		tuning.reverse()
		return tuning
	}
	
	this.select = function(arg) {
		model.select(arg)
		events.broadcast('modelchange', function() { return arg })
	}
	
	this.notationFromFreqId = model.notationFromFreqId
	
}