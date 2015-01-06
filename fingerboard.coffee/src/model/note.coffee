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
		@dimension = new Square
		@interval = new Interval

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
			
			
		@__public__
		
		