
/******************************************************************************
 * Model
 */


(function(Fingerboard, Clonify, Polymorphy) {

function Square (x1, y1, x2, y2) {
	this.x1 = x1 ? x1 : -1;
	this.y1 = y1 ? y1 : -1;
	this.x2 = x2 ? x2 : -1;
	this.y2 = y2 ? y2 : -1;
	// Is it inside the quare?
	this.isPointWithinBounds = function(x, y) {
		return x > this.x1 && x < this.x2 && y > this.y1 && y < this.y2
	};
}

var Note = Polymorphy.extends({
	constructor: function(fret, string){
		this.fret = fret;
		this.string = string;
		this.selector = '';
		this.dimension = new Square();
		this.interval = {};
	}
});

Fingerboard.Model = function(args, events) {
	// Array containing notes
	var fingerboard;
	
	var defaultNotation = 
		['C', 'Db', 'D', 'Eb', 'E', 'F', 'Gb', 'G', 'Ab', 'A', 'Bb', 'B'];

	this.settings = settings = {
		notation: Clonify(defaultNotation),
		selectors: {}
	};
	
	
	// Argument processing helpers
	
	var asJSArray = function(arr) {
		if(typeof arr === 'string') {
			// smells like Json
			if(arr[0] === '[') 
				return JSON.parse(arr);
			// could be CSV
			else if(arr.indexOf(",") !== -1) 
				return arr.split(",").map(function(val) {
					return Number(val)
				});
			// Otherwise throw an exception since I have no idea
			// what you're trying to say.
			throw 'Cannot process array argument ' + arr;
		} 
		
		return arr;
	};
	
	// Standard Accessors
	
	this.getNoteFor = getNoteFor = function(fret, string) {
		return fingerboard[fret][string-1]
	};
	
	this.frets = function() {
		return fingerboard.length
	};
	
	this.strings = strings = function () {
		return fingerboard[0].length
	};
	
	// Traversing functions
	
	this.find = find = function(traversor) {
		var result = undefined;
		this.forEach(function(fret, string, value) {
			if(traversor(fret, string, value) === true) {
				result = value;
				return false;
			}
		});
		return result;
	};
	
	this.forEach = forEach = function (traversor) {
		for(var fret = 0; fret < fingerboard.length; fret++) 
			for(var string = 1; string <= fingerboard[0].length; string++)
				if(traversor(fret, string, getNoteFor(fret, string)) === false) 
					return;
	};
	
	// Do I event use this?
	
	this.select = select = function(arg) {
		if(arg === 'scale') {
			forEach(function(fret, string, note){
				if(note.rootedValue === 1)
					note.selector = 'tonic'
				else if(note.scaleValue)
					note.selector = 'selected'
				else
					note.selector = ''
			})
		}
	};
	
	// Construction Functions
	
	var fill = function(frets, strings) {
		if(!frets) frets = fingerboard.length - 1 || 15;
		if(!strings) strings = fingerboard[0].length || 6;
		
		fingerboard = [];

		for(var fret = 0; fret <= frets; fret++) {
			fingerboard[fret] = [];
			for(var string = 1; string <= strings; string++)
				fingerboard[fret][string-1] = new Note(fret, string, this);
		}
	};
	
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
			scaleLength = notation.length || args.scaleLength;
		
		// Use a default value if its possible
		if(!tuning) {
			if(notation.every(function(val, i) { return defaultNotation[i] === val }) 
				&& strings() === 6) {
				
				var note = function(notation, index) {
					return index * 12 + defaultNotation.indexOf(notation);
				};
				tuning = [note('E', 2),note('A', 2),note('D', 3),note('G', 3),note('B', 3),note('E', 4)];
			} else {
				throw 'Interval needs more arguments.';
			}
		}
		
		// Now we can build....
		for(var i = 0; i < scaleLength * (maxIndex + 1); i++) {

			intervals.push({
				value: intervalValue,
				index: index,
				freqId: (i + 1),
				notation: notation[intervalValue - 1] || undefined
			});

			if(intervalValue >= scaleLength) {
				intervalValue = 1;
				index++;
			}
			else intervalValue++;
		}
		//console.log('tuning is:',tuning);
		tuning = tuning.reverse();


		forEach(function(fret, string, note) {
			note.interval = intervals[tuning[string - 1] + fret];
		});

		settings.tuning = tuning;
		settings.notation = notation;
		settings.scaleLength = scaleLength;
	};
	
	var buildRootedValue = function(root) {
		//console.log("rooting");
		var map,
			scaleLength = settings.scaleLength;

		forEach(function(fret, string, note){
			note.rootedValue = note.interval.value - root + 1;
			if(note.rootedValue < 1) 
				note.rootedValue += scaleLength
		})
		settings.root = root;
	};
	
	/*args = {
	  values: Array<Number, Number>,
	  root: Number
	  select: boolean
	}*/
	this.buildScale = buildScale = function(args) {
		var 
			spacings = (args && asJSArray(args.values)) || settings.scale,
			scaleLength = settings.scaleLength,
			sc, scaleValue,
			scale = [];
	  
		if((args && args.root) || !settings.root){
			buildRootedValue((args && args.root) || settings.root || 1)
		}

		spacings.forEach(function(val, i) {
			scale[val] = i + 1;
		});

		// Now we set the values (1st, 2nd, 3rd, etc of the scale).
		forEach(function(fret, string, note){
			if(scaleValue = scale[note.rootedValue])
				note.scaleValue = scaleValue
			else 
				// The note could have information from the previous
				// scale, so we need to clear that.
				note.scaleValue = undefined
		});
	  
		settings.scale = spacings;
		
		if(args && args.select)
			select('scale')
	}
	
	this.set = set = function(args) {
		if(args.selectors) 
			for(var key in args.selectors)
				settings.selectors[key] = args.selectors[key];
		
		if(args.strings || args.frets) 
			fill(args.frets, args.strings);

		if(args.interval) 
			buildInterval(args.interval);

		if(args.scale)
			buildScale(args.scale);

		// Rebuild the scale even if its just a change to the
		// interval data.
		if(args.interval && !args.scale){
			// Force rebuilding the rooted value.
			buildRootedValue(settings.root);
			buildScale();
			select('scale');
		}

		// broadcast change to all listeners.
		events.broadcast('modelchange', function() { return Clonify(args) });

	};
	
	this.notationFromFreqId = function(id) {
		var ln = settings.notation.length;
		return settings.notation[id % ln] + "" + 
			Math.floor(id / ln) 
	};
	
	this.get = get = function() {
		if(arguments.length === 1) {
			return Clonify(settings[arguments[0]]);
		} else {
			var cln = {};
			for(var i = 0; i< arguments.length; i++) 
				cln[arguments[i]] = Clonify(settings[arguments[i]]);
			
			return cln;
		}
	};
	
	// Object Construction/Parsing Args
	if(args) set(args);
	else fill();
}

})(Fingerboard, Fingerboard.Clonify, Fingerboard.Polymorphy);

