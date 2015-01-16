# 
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
		view = new View(args, $canvas, model, events)
		
		# the rest of the public functions are going to be mainly exposing
		# the model part.
		
		@tuning = ->
			
window.Fingerboard = Fingerboard
###
	Just a little wrapper for the HTML5 canvas 2d context.
	Method chains! \o/
###

class ContextWrapper
	constructor: (context) ->
		for key of context
			if typeof context[key] == 'function'
				@[key] = do(key) ->
					() ->
						context[key].apply(context, arguments)
						@
			else
				@[key] = do(key) ->
					(val) ->
						context[key] = val
						@
	
	
		@context = context
		
	begin: ->
		@context.beginPath()
		@
		
	beginAt: (x ,y) ->
		@context.beginPath()
		@context.moveTo(x, y)
		@
	
	color: (col) ->
		context.fillStyle = col
		@
	
	get: (key) ->
		@context[key]
		

###
# The Mother of All Models in this project.
###

class Model
	
	###
	# construction functions	
	###
	
	constructor: (args) ->
		# default values...
		@notes = [[]]
		@notation = 
			['C', 'Db', 'D', 'Eb', 'E', 'F', 'Gb', 'G', 'Ab', 'A', 'Bb', 'B']
		@selectors = {}
		@root = 1
		@tuning = [28,33,38,43,47,52]
		@scale = undefined
		@scaleLength = @notation.length
		@strings = @tuning.length
		@frets = 16
		# And finally we build the whole thing by using the set
		# method which will parse all of the arguments properly.
		@set(args)
		
	# This will simply fill the notes array with a fresh coat of Note 
	# instances based on its current state.
	fill: ->
		@notes = []
		for fret in [0..@frets]
			notes[fret] = []
			for string in [1..@strings]
				notes[fret][string-1] = new Note(fret, string)
				
				
	# This will handle the rebuilding of the interval-related data, but unlike 
	# the original this doesn't take care of the arguments parsing.
	buildInterval: ->
		if @tuning.length != @strings
			throw 'Tuning is invalid for the number of strings given.'
		
		intervalValue = -1
		index = 0
		
		intervals = [0..@scaleLength * (@maxIndex + 1)]
			.map (i) ->
				if intervalValue >= @scaleLength
					intervalValue = 1
					index++
				else
					intervalValue++
					
				value: intervalValue,
				index: index,
				freqId: i + 1,
				notation: @notation[intervalValue - 1]
		
		# using the generated data, I can
		@forEach (note, fret, string) ->
			note.interval = intervals[@tuning[string-1] + fret]
	
	
	# This will set up the shifted interval value for building the scale.
	buildRootedValue: ->
		@forEach (note, fret, string) ->
			note.interval.shift = note.interval.value - @root + 1
			if note.interval.shift < 1
				note.interval.shift += scaleLength
				
	buildScale: ->
		sc = undefined
		degree = undefined
		#I will put this call higher up, where the args processing occurs
		#buildRootedValue
		
		@forEach (note, fret, string) ->
			if degree = scale[note.interval.shift]
				note.interval.degree = degree
			else
				note.interval.degree = undefined
	
	set: (args) ->
		throw 'Oye, forgot something? I need an options object.' if args == undefined
			
		@strings = args.strings if args.strings != undefined
		@frets = args.frets if args.frets != undefined
		
		# We dont need to look into it more than that, we're going to have to fill
		# it with new notes.
		@fill() if args.strings != undefined || args.frets != undefined
		
		if args.interval != undefined
			a = interval
			if a.notation != undefined
				@notation = a.notation
				# we can derive the scale length of the note system by taking it
				# from the number of notation values we have in there.
				@scaleLength = notation.length
			@maxIndex = a.maxIndex if a.maxIndex != undefined
			@tuning = a.tuning if a.tuning != undefined
			# Since there was a change in the interval data, we call buildInterval
			@buildInterval()
		else if @notes[0][0].interval.value == -1
			# The notes aren't initialized since this is either the initial
			# construction or there was a call done to fill
			@buildInterval()
		
		if args.scale != undefined
			a = args.scale
			@scale = a.scale if a.scale != undefined
			@root = a.root if a.root != undefined
			buildScale()
		else if @scale != undefined && @root != undefined
			buildScale()
			
	###
	# private static functions
	###
	
	asJSArray = (arr) ->
		if typeof arr == 'string'
			if arr[0] == '['
				JSON.parse(arr) 
			else if arr.indexOf(',') != -1
				arr
					.split(',')
					.map (val) ->
						if isNaN(val)
							throw 'Invalid array input.'
						else Number(val)
		else
			arr
			
	###	
	# Traversing functions
	###
	
	forEach: (traversor) ->
		for fretArr, fret in notes
			for note, string in fretArr
				if traversor(note, fret, string) == false
					return
	
	find: (traversor) ->
		result = undefined
		@forEach (note, fret, string) ->
			if traversor(note, fret, string) == true
				result = note
				return false
		result
		
		
class PublicInterface
	constructor: (events, obj, keys) ->
		args = {}
		
		for key in keys
			if typeof key == 'object'
				args[key.name] = key
			else
				args[key] =
					enumerable: true,
					get: -> S[key],
					set: (val) ->
						S[key] = val
						events.broadcast('modelchange', ->
							name: key,
							value: val
						)
						
		Object.defineProperties(@, args)
	
class Interval
	constructor: ->
		# Absolute value of this note. If two notes
		# have the same freqId, they would be played
		# at the exact same frequency.
		@freqId = -1
		
		# index of the interval value.
		@index = -1
		
		# integer representation of C,Db,D,E,F...
		@value = -1
		
		# notational (view) of value (C,Db,D,E,F...)
		@notation = ''
		
		# shift is used to 'push' the interval to where the
		# tonic should be.
		@shift = -1
		
		# the degree is the displayed value of the shift
		# integer
		@degree = ''
	
	# define the public interface
	public: (events) ->
		if(@__public__ == undefined)
			@__public__ = new PublicInterface(events, @, [
				'freqId', 'index', 'notation', 
				'value',  'shift', 'degree'
			])
			
		@__public__
		
class Square
	
	constructor: (@x1, @y1, @x2, @y2) ->
	
	isPointWithinBounds: (x, y) ->
		x > @x1 && x < @x2 && @y1 
	
	# Define the public interface
	public: (events) ->
		if(@__public__ == undefined)
			@__public__ = new PublicInterface(events, @, [
				'x1', 'y1', 'x2', 'y2'
			])
			
		@__public__


class Note
	constructor: (@fret, @string) ->
		@selector = ''
		@dimension = new Square()
		@interval = new Interval()

	public: (events) ->
		if(@__public__ == undefined)
			@__public__ = new PublicInterface(events, @, [
				'frets', 
				'strings', 
				{ name: 'dimension', 
				enumerable: true, 
				writable: false, 
				value: @dimension.public(events) },
				{ name: 'interval',
				enumerable: true,
				writable: false,
				value: @interval.public(events) },
				'selector'
			])
			@__public__.toString = ->
				JSON.stringify(@)
			
		@__public__
		
		