
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