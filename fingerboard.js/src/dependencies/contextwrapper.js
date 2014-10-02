
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