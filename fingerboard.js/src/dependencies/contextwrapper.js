
/******************************************************************************
 * ContextWrapper.js
 */
 
(function(Fingerboard) {

	Fingerboard.ContextWrapper = function (context) {
		var t = this;
		
		var functionify = function(obj, key) {
			return function() {
				obj[key].apply(obj, arguments);
				return t;
			};
		};
		
		var settify = function(obj, key) {
			return function(val) {
				obj[key] = val;
				return t;
			}
		};
		
		// basic wrapping
		for(var key in context) {
			if(typeof context[key] === 'function') {
				t[key] = functionify(context, key)
			} else {
				// Only interested in setters for method chains.
				t[key] = settify(context, key)
			}
		}
		
		t.context = context;
		
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