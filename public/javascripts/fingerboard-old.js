
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
	
};

/******************************************************************************
 * ContextWrapper.js
 */
 
(function(Fingerboard) {

	Fingerboard.ContextWrapper = function (context) {
		var t = this;
		
		// basic wrapping
		for(var key in context) {
			if(typeof context[key] === 'function') {
				t[key] = (function(key, context){
					return function(){
						context[key].apply(context, arguments);
						return t;
					}
				})(key, context)
			} else {
				// Only interested in setters for method chains.
				t[key] = (function(key, context){
					return function(val){
						context[key] = val;
						return t;
					}
				})(key, context)
			}
		}
		
		t.context = context;
		
		//context.translate(0.5,0.5);
		
		// Aliases/ Compressions
		t.begin = function() {
			context.beginPath();
			return t;
		};
		t.beginAt = function(x,y){
			context.beginPath();
			context.moveTo(x,y);
			return t;
		};
		t.color = function(color) {
			context.fillStyle = color;
			return t;
		};
		
		// Getter, no point in this but just in case.
		t.get = function(key) {
			return context[key]
		};
	};

})(Fingerboard);
// little plugin
/*
window.jQuery.fn.mousedrag = function(callback) {
	var holding = false,
		lastLoc = {}
	
	this.on('mousedrag', callback)
	
	this.on('mousedown', function(e){
		lastLoc.x = e.pageX
		lastLoc.y = e.pageY
		holding = true
	})
	
	this.on('mouseup', function(e){
		holding = false
	})
	
	this.on('mousemove', function(e){
		if(holding){
			lastLoc.x = e.pageX
			lastLoc.y = e.pageY
			callback(e)
		} 
	})
}*/;

/******************************************************************************
 * Polymorphy.js
 */
 
(function(Fingerboard){

// Constructs a clone of the object/native type. This is a deep clone.
Fingerboard.Clonify = Clonify = function (src) {
	switch(typeof src) {
		case 'undefined':
			return;
		case 'number': 
			return Number(src);
		case 'object':
			if(Array.isArray(src)) {
				return src.map(function(val) { return Clonify(val) } );
			} else {
				var clone = {};
				for(var key in src) 
					if(src.hasOwnProperty(key)) 
						clone[key] = Clonify(src[key]);
				return clone;
			}
		case 'string':
			return String(src);
		case 'function':
			var func = function sub() { return src.apply(this, arguments) };
			for(var key in src) 
				if(src.hasOwnProperty(key)) 
					func[key] = Clonify(src[key]);
			return func;
		case 'date':
			return new Date(src.valueOf());
		case 'boolean':
			return Boolean(src);
	}
	throw 'Type ' + typeof src + ' not clonable';
}

Fingerboard.Polymorphy = function(){};

// Based on Backbone.js/Undescore.js' version
Fingerboard.Polymorphy.extends = function(protoProps, staticProps) {
	var merge = function(obj) {
		for(var i = 1; i < arguments.length; i++)
			if(arguments[i])
				for(var key in arguments[i])
					obj[key] = Clonify(arguments[i][key]);
		return obj;
	};
	
	var parent = this,
		child;
	
	if(protoProps && 'constructor' in protoProps) {
		child = protoProps.constructor
	} else {
		child = function() { return parent.apply(this, arguments) }
	}
	
	var Surrogate = function() { this.constructor = child; };
	Surrogate.prototype = parent.prototype;
	child.prototype = new Surrogate;
	
	if(protoProps) merge(child.prototype, protoProps);
	
	child.__super__ = parent.prototype;
	
	return child;
}

Fingerboard.Polymorphy.prototype.clone = function() {
	return Clonify(this);
};

})(Fingerboard);

/******************************************************************************
 * Model
 */

/* -- Internal Data Structure --
	model: {
		settings: {
			tuning: [Int]
			notation: [String]
			scaleLength: Int,
			root: Int
		},
		# This is a 2 dim array of Note instances #
		fingerboard: [[ {
			fret: Integer
			string: Integer
			selector: [String]
			dimension: {
				x1: Integer,
				y1: Integer,
				x2: Integer,
				y2: Integer
			}
			interval:{
				freqId: Integer
				index: Integer
				value: Integer
				notation: String
				shift: Integer
				degree: String
			}
		} ]]
		
	}
*/
(function(Fingerboard, Clonify, Polymorphy) {

function Square (x1, y1, x2, y2) {
	this.x1 = x1 ? x1 : -1
	this.y1 = y1 ? y1 : -1
	this.x2 = x2 ? x2 : -1
	this.y2 = y2 ? y2 : -1
}

// Is it inside the quare?
Square.prototype.isPointWithinBounds = function(x, y) {
	return x > this.x1 && x < this.x2 && y > this.y1 && y < this.y2
}

// returns the public interface, creating it if it doesn't exist already.
Square.prototype.public = function(events){
	if(!this.__public__){
		var p = {}, S = this
		
		function pointing(name){
			return {
			//	writable: true,
				enumerable: true,
				get: function(){ return S[name] },
				set:function(val) { 
					S[name] = val 
					events.broadcast('modelchange',function(){ 
						return {
							name: 'Square:' + name,
							value: val
						}
					})
				},
			}
		}
		
		Object.defineProperties(p, {
			x1: pointing('x1'),
			y1: pointing('y1'),
			x2: pointing('x2'),
			y2: pointing('y2')
		})
		
		p.isPointWithinBounds = Square.prototype.isPointWithinBounds
		
		this.__public__ = p
	}
	return this.__public__
}

function Interval(){
	// Absolute value of this note. If two notes
	// have the same freqId, they would be played
	// at the exact same frequency.
	this.freqId = -1
	
	// index of the interval value.
	this.index = -1
	
	// integer reprentation of C,Db,D,E,F...
	this.value = -1
	
	// notational (view) of value (C,Db,D,E,F...)
	this.notation = ''
	
	// shift is used to 'push' the interval to where the
	// tonic should be.
	this.shift = -1
	
	// the degree is the displayed value of the shift
	// integer
	this.degree = ''
}

Interval.prototype.public = function(events){
	if(!this.__public__){
		var p = {}, S = this
		
		function pointing(name){
			return {
			//	writable: true,
				enumerable: true,
				get: function(){ return S[name] },
				set:function(val) { 
					S[name] = val 
					events.broadcast('modelchange',function(){ 
						return {
							name: 'Square:' + name,
							value: val
						}
					})
				},
			}
		}
		
		Object.defineProperties(p, {
			freqId: pointing('freqId'),
			index: pointing('index'),
			notation: pointing('notation'),
			value: pointing('value'),
			shift: pointing('shift'),
			degree: pointing('degree')
		})
		
		this.__public__ = p
	}
	return this.__public__
}

var Note = Polymorphy.extends({
	constructor: function(fret, string){
		this.fret = fret
		this.string = string
		this.selector = ''
		this.dimension = new Square()
		this.interval = new Interval()
	}
})

// Returns the public interface. This is lazy loaded.
Note.prototype.public = function(events){
	if(!this.__public__){
		var p = {}, N = this
		
		function pointing(name){
			return {
			//	writable: true,
				enumerable: true,
				get: function(){ return N[name] },
				set:function(val) { 
					S[name] = val 
					events.broadcast('modelchange',function(){ 
						return {
							name: 'Square:' + name,
							value: val
						}
					})
				}
			}
		}
		
		Object.defineProperties(p, {
			"frets": pointing("frets"),
			"string": pointing("string"),
			"dimension": {
				enumerable: true,
				writable: false,
				value: N.dimension.public(events)
			},
			"selector": pointing("selector"),
			"interval":{
				enumerable: true,
				writable: false,
				value: N.interval.public(events)
			}
		})
		console.log('public interface:',p)
		this.__public__ = p
	}
	
	return this.__public__

}

Fingerboard.Model = function(args, events) {
	// Array containing notes
	var fingerboard
	
	var defaultNotation = 
		['C', 'Db', 'D', 'Eb', 'E', 'F', 'Gb', 'G', 'Ab', 'A', 'Bb', 'B']

	this.settings = settings = {
		notation: defaultNotation,
		selectors: {}
	}
	
	// Argument processing helpers
	
	var asJSArray = function(arr) {
		if(typeof arr === 'string') {
			// smells like Json
			if(arr[0] === '[') 
				return JSON.parse(arr)
			// could be CSV
			else if(arr.indexOf(",") !== -1) 
				return arr.split(",").map(function(val) {
					if(isNaN(val)) 
						throw 'Invalid array input.'
					return Number(val)
				})
			// Otherwise throw an exception since I have no idea
			// what you're trying to say.
			throw 'Cannot process array argument ' + arr
		} 
		
		return arr
	}
	
	// Standard Accessors
	
	this.getNoteFor = getNoteFor = function(fret, string) {
		return fingerboard[fret][string-1]
	}
	
	this.frets = function() {
		return fingerboard.length
	}
	
	this.strings = strings = function () {
		return fingerboard[0].length
	}
	
	// Traversing functions
	
	this.find = find = function(traversor) {
		var result = undefined
		this.forEach(function(fret, string, value) {
			if(traversor(fret, string, value) === true) {
				result = value
				return false
			}
		})
		return result
	}
	
	this.forEach = forEach = function (traversor) {
		for(var fret = 0; fret < fingerboard.length; fret++) 
			for(var string = 1; string <= fingerboard[0].length; string++)
				if(traversor(fret, string, getNoteFor(fret, string)) === false) 
					return
	}
	
	// Do I even use this?
	
	this.select = select = function(arg) {
		if(arg === 'scale') {
			forEach(function(fret, string, note){
				if(note.interval.shift === 1)
					note.selector = 'tonic'
				else if(note.interval.degree)
					note.selector = 'selected'
				else
					note.selector = ''
			})
		}
	}
	
	// Construction Functions
	
	var fill = function(frets, strings) {
		
		if(frets === undefined) 
			frets = fingerboard.length - 1 || 15
		if(strings === undefined) 
			strings = fingerboard[0].length || 6
		
		if(typeof frets !== 'number'){
			if(isNaN(frets))
				throw 'Frets property is not a number.'
			else
				frets = Number(frets)
		}
		if(typeof strings !== 'number') {
			if(isNaN(strings))
				throw 'Strings property is not a number.'
			else
				strings = Number(strings)
		}
		if(frets < 1)
			throw 'Not enough frets.'
		if(strings < 1)
			throw 'Not enough strings.'
		
		fingerboard = []

		for(var fret = 0; fret <= frets; fret++) {
			fingerboard[fret] = []
			for(var string = 1; string <= strings; string++)
				fingerboard[fret][string-1] = new Note(fret, string, this)
		}
	}
	
	var buildInterval = function(args) {
		var 
			// This is what should be displayed in the view to the user.
			notation = args.notation || settings.notation,
			// 0 to 8 in the western system.
			maxIndex = args.maxIndex || 8,
			tuning = asJSArray(args.tuning) || settings.tuning,
			intervalValue = 1,
			index = 0,
			intervals = [],
			scaleLength = notation.length || args.scaleLength
		
		// Use a default value if its possible
		if(!tuning) {
			if(notation.every(function(val, i) { return defaultNotation[i] === val }) 
				&& strings() === 6) {
				
				var note = function(notation, index) {
					return index * 12 + defaultNotation.indexOf(notation)
				}
				tuning = [note('E', 2),note('A', 2),note('D', 3),note('G', 3),note('B', 3),note('E', 4)]
			} else {
				throw 'Interval needs more arguments.'
			}
		}
		
		// Now we can build....
		for(var i = 0; i < scaleLength * (maxIndex + 1); i++) {

			intervals.push({
				value: intervalValue,
				index: index,
				freqId: (i + 1),
				notation: notation[intervalValue - 1] || undefined
			})

			if(intervalValue >= scaleLength) {
				intervalValue = 1
				index++
			}
			else intervalValue++
		}
		//console.log('tuning is:',tuning)
		tuning = tuning.reverse()


		forEach(function(fret, string, note) {
			note.interval = intervals[tuning[string - 1] + fret]
		})

		settings.tuning = tuning
		settings.notation = notation
		settings.scaleLength = scaleLength
	}
	
	var buildRootedValue = function(root) {
		//console.log("rooting")
		var map,
			scaleLength = settings.scaleLength

		forEach(function(fret, string, note){
			note.interval.shift = note.interval.value - root + 1
			if(note.interval.shift < 1) 
				note.interval.shift += scaleLength
		})
		settings.root = root
	}
	
	/*args = {
	  values: Array<Number, Number>,
	  root: Number
	  select: boolean
	}*/
	this.buildScale = buildScale = function(args) {
		var 
			spacings = (args && asJSArray(args.values)) || settings.scale,
			scaleLength = settings.scaleLength,
			sc, degree,
			scale = []
	  
		if((args && args.root) || !settings.root){
			buildRootedValue((args && args.root) || settings.root || 1)
		}

		spacings.forEach(function(val, i) {
			scale[val] = i + 1
		})

		// Now we set the values (1st, 2nd, 3rd, etc of the scale).
		forEach(function(fret, string, note){
			if(degree = scale[note.interval.shift])
				note.interval.degree = degree
			else 
				// The note could have information from the previous
				// scale, so we need to clear that.
				note.interval.degree = undefined
		})
	  
		settings.scale = spacings
		
		if(args && args.select)
			select('scale')
	}
	
	this.set = set = function(args) {
		if(args.selectors) 
			for(var key in args.selectors)
				settings.selectors[key] = args.selectors[key]
		
		if(args.strings || args.frets) 
			fill(args.frets, args.strings)

		if(args.interval) 
			buildInterval(args.interval)

		if(args.scale)
			buildScale(args.scale)

		// Rebuild the scale even if its just a change to the
		// interval data.
		if(args.interval && !args.scale){
			// Force rebuilding the rooted value.
			buildRootedValue(settings.root)
			buildScale()
			select('scale')
		}

		// broadcast change to all listeners.
		events.broadcast('modelchange', function() { return Clonify(args) })

	}
	
	this.notationFromFreqId = function(id) {
		var ln = settings.notation.length
		return settings.notation[id % ln] + "" + 
			Math.floor(id / ln) 
	}
	
	this.get = get = function() {
		if(arguments.length === 1) {
			return Clonify(settings[arguments[0]])
		} else {
			var cln = {}
			for(var i = 0; i< arguments.length; i++) 
				cln[arguments[i]] = Clonify(settings[arguments[i]])
			
			return cln
		}
	}
	
	// Object Construction/Parsing Args
	if(args) set(args)
	else fill()
}

})(Fingerboard, Fingerboard.Clonify, Fingerboard.Polymorphy)

;
/**
 * Selector extras
 */
 
(function(Fingerboard){

Fingerboard.Selectors = {}

Fingerboard.Selectors.notation = function(colors, font, radius){
	var 
		colors = {
			selected: colors && colors.selected || 'gray',
			tonic: colors && colors.tonic || 'firebrick'
		}, 
		endArc = Math.PI * 2,
		ft = font || '700 11px tahoma',
		rad = radius || 12
	
	
	return function(context, note, x, y){
		context
			.begin()
			.color(colors[note.selector])
			.arc(x, y, rad, 0, endArc)
			.fill()
			
		context
			.begin()
			.color('white')
			.textAlign('center')
			.textBaseline('middle')
			.font(ft)
			.fillText(note.interval.notation + note.interval.index, x, y)
		
	}
}
/*
Fingerboard.Selectors.interval = function(colors){
	var 
		colors = {
			selected: colors && colors.selected || 'gray',
			tonic: colors && colors.tonic || 'firebrick'
		}, 
		endArc = Math.PI * 2
		
	return function(context, note, x, y){
		context
			.begin()
			.color(colors[note.selector])
			.arc(x, y, 10, 0, endArc)
			.fill()
			
		context
			.begin()
			.fillText(note.interval.notation, x, y)
	}
}
*/


})(Fingerboard);

/******************************************************************************
 * View
 */
 
(function(Fingerboard, 
ContextWrapper) {


Fingerboard.View = function(args, $canvas, model, events) {

	var 
		canvas = $canvas[0],
		context =  new ContextWrapper(canvas.getContext('2d')),
		width, height;
	
	var drawInlay = (args.display && args.display.inlay) || 
		function(context, x, y, width, height){
			context
				.beginAt(x - (width / 2), y)
				.lineTo(x, (y - (height / 2)))
				.lineTo(x + (width / 2), y)
				.lineTo(x, y + (height / 2))
				.fillStyle(colors.inlays)
				.fill();
		}
		
	
	
	model.settings.selectors.selected = 'gray';
	model.settings.selectors.tonic = 'firebrick';
	
	// internal colors
	var colors = {
		strings: 'gray',
		inlays:'#D1A319',
		frets: 'gray'
	};
	
	/*function drawDiamond(context, x, y, width, height){
		context
			.beginAt(x - (width / 2), y)
			.lineTo(x, (y - (height / 2)))
			.lineTo(x + (width / 2), y)
			.lineTo(x, y + (height / 2))
			.fillStyle(colors.inlays)
			.fill();
	}*/

	/**
	 * private function definitions
	 */
	
	function paint() {
		var 
			openWidth = width / (model.frets() * 2),
			leftover = width - openWidth,
			fretWidth = leftover / (model.frets()-2),
			heightRatio = height / model.strings(),
			stringH, fretStart, fretEnd, circle,
			selectors = model.settings.selectors,
			endArc = Math.PI * 2, inlayX, color;
		
		// radius for circles :D
		var radius = (heightRatio > openWidth ? 
			openWidth / 4 : heightRatio / 4),
			helperRadius = radius * 2 / 3;
		
		var drawSelector = (args.display && args.display.selector) ||
			function(context, note, x, y){
				if(color = selectors[note.selector]){
					context
						.beginPath()
						.color(color)
						.arc(x, y, radius, 0, endArc)
						.fill();
				}
			}
		
		context.lineWidth = 1;
		
		/* console.log(
			'width ratio', width / (model.frets()),
			'\nopenWidth', openWidth,
			'\nleftover', leftover,
			'\nfretwidth', fretWidth
		);*/
	
		model.forEach(function(fret, string, note) {
			fretStart = !fret ? 1 : ((fret - 1) * fretWidth) + openWidth;
			fretEnd = fretStart + fretWidth -1;		
			stringH = ((string-1) * heightRatio) + (heightRatio / 2);
			
			inlayX = fretStart + ((!fret ? openWidth : fretWidth) / 2);
			
			// I need the dimension of each note on the fingerboard
			// to be stored for later access for the event service.
			note.dimension.x1 = fretStart;
			note.dimension.y1 = stringH - (heightRatio / 2);
			note.dimension.x2 = fretEnd;
			note.dimension.y2 = stringH + (heightRatio / 2);
			
			// draw the string
			if(fret === 0)
				context
					.beginPath()
					.fillStyle(colors.strings)
					.moveTo(openWidth, stringH)
					.lineTo(width, stringH)
					.stroke()
					
			if(string === 1) {
				if(fret === 1){
					context.context.lineWidth = 5;
					context
						.beginPath()
						.fillStyle(colors.frets)
						.moveTo(fretStart, 0)
						.lineTo(fretStart, height)
						.stroke()
						;
					context.context.lineWidth = 1;	
				} else if(fret !== 0)
				// draw the fret
					context
						.beginPath()
						.fillStyle(colors.frets)
						.moveTo(fretStart, 0)
						.lineTo(fretStart, height)
						.stroke();
				
				switch(fret) {
					// draw the inlay visual helpers
					case 3: case 5: case 7: case 9:
						drawInlay(context, inlayX, height / 2, radius * 3, radius *6);
						break;
					// draw the double inlay
					case 12:
						drawInlay(context, inlayX, height / 3, radius * 3, radius *6);
						drawInlay(context, inlayX, 2 * (height / 3), radius * 3, radius *6);
						break;
				}
			}
			
			// Trigger selector draw selector if there is one for the note
			if(note.selector !== ''){
				drawSelector(context, note, inlayX, stringH)
			}
			/*if(note.selector !== '' && (color = selectors[note.selector])) {
				context.beginPath()
					.color(color)
					.arc(inlayX, stringH, radius, 0, endArc)
					.fill();
			}*/
		});
	}
	
	function updateDimensions() {
		width = $canvas.width();
		height = $canvas.height();
		// prevent stretching
		context.get('canvas').height = height;
		context.get('canvas').width = width;
	}
	
	// refresh view.
	function repaint () {
		updateDimensions();
		context
			.begin()
			.clearRect(0, 0, width, height)
			.fill();
		paint();
	}
	
	
	// disable highlighting for the canvas(looks ridiculous)
	canvas.style['user-select'] = 'none';
	canvas.style['-webkit-user-select'] = 'none';
	canvas.style['-moz-user-select'] = 'none';
	// I'm going to need to remove this eventually...
	//canvas.style['border-top'] = '1px solid';
	//canvas.style['border-bottom'] = '1px solid';
	//canvas.style['border-style'] = 'solid';
	//canvas.style['color'] = 'gray';
	
	// Object construction
	
	updateDimensions();
	paint();
	
	// The View has event handling logic since it wraps the on-click event
	// to provide its own service to the controller.
	$canvas.click(function(ev) {
		events.broadcast('noteclick', function() {
			return model.mouseHoveredNote.clone()
		});
	});
	
	var stateChanged = false,
		x, y;
	
	$canvas.mousemove(function(ev) {
		x = ev.pageX - $(this).offset().left;
		y = ev.pageY - $(this).offset().top;
		
		// If there is no note that the mouse is hovered on or
		// if the mouse is no longer inside of the square we should
		// try and find the note that the mouse is on top of.
		if(model.mouseHoveredNote) {
			stateChanged = !model
				.mouseHoveredNote
				.dimension
				.isPointWithinBounds(x, y);
		}
		else stateChanged = true;
		
		if(stateChanged) {
		
			var newHovered = model.find(function(fret, string, value) {
				return value.dimension.isPointWithinBounds(x, y)
			});
			
			if(newHovered) {
				model.mouseHoveredNote = newHovered;
		
				events.broadcast('notehover', function(){
					// We want to ensure that the event receiver isn't able to
					// change the internal state, therefore the note is cloned.
					return model.mouseHoveredNote.clone()
				});
			}
		}
	});
	
	// The view should also abstract away all resize handling.
	$(window).resize(repaint);
	
	// Gotta manually trigger the event though...
	$canvas.on('resize', repaint);
	
	// We'll need to make sure that the view listens for changes in the model
	events.modelchange(repaint);
};


})(Fingerboard, Fingerboard.ContextWrapper)