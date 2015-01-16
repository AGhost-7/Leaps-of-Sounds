
class Fingerboard
	constructor: ($canvas, args) ->
		events = do ->
			listeners = 
				# Returns the note that the mouse is parked on when the canvas 
				# is clicked.
				noteclick: [],
				# Triggers every time that the mouse hovers on a different note.
				notehover: [],
				# Whenever a change is done to an element, this event is triggered.
				# Some of the internal logic won't trigger this event at all.
				modelchange: []
			
			self = 
				broadcast: (event, callback) ->
					listener(callback()) for listener in listeners[event]
				
				on: (event, callback) ->
					listeners[event].push(callback)
			
			# lets make some shortcuts for the various events
			for key of listeners
				self[key] = do (key) ->
					(callback) -> 
						listeners[key].push(callback)
			
			self
		
		# And now I gotta expose this at the Fingerboard level...
		@[key] = events[key] for key of events
		
		model = new Model(args, events)
		#view = new View(args, $canvas, model, events)
		
		# the rest of the public functions are going to be mainly exposing
		# the model part.
		
		@forEach = (traversor) ->
			model.forEach (note, fret, string) ->
				console.log(note)
				traversor(note.public(events), fret, string)
			
window.Fingerboard = Fingerboard