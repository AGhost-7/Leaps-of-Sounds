(function(){
	
var soundBox = window.soundBox = {};

var freqTable = new FreqTable();

// setter, I'll need to make it adjust dependees later if I ever use this.
soundBox.freqTable = function(args) {
	 freqTable = new FreqTable(args);
};

/*
	Problems may still occur as, from what I've read thus far, it 
	seems that even method names are inconsistent across 
	browsers, and some calls may return different object types
	as well... A more elaborate Shim will be needed.
*/
function normalizeAPI() {

	// Essentially toolkit for the audio api.
	window.AudioContext = window.AudioContext || 
		window.webkitAudioContext || 
		window.mozAudioContext || 
		window.oAudioContext || 
		window.msAudioContext;

	// Shim for hooking into the browser's draw exec time
	// This is strictly for performance.
	if(!window.requestAnimationFrame) {
		window.requestAnimationFrame = 
			window.webkitRequestAnimationFrame || 
			window.mozRequestAnimationFrame || 
			window.oRequestAnimationFrame || 
			window.msRequestAnimationFrame ||
			function( callback ){
				window.setTimeout(callback, 1000 / 60)
			};
	}
}

soundBox.listen = function(stream) {
	// execute shim.
	normalizeAPI();
	
	var context = new AudioContext();
	var input = context.createMediaStreamSource(stream);
	var analyser = context.createAnalyser();
	input.connect(analyser);
	
	// Recursive function call which "schedules" to execute
	// at the same time as the browser's redraw(60 fps~).
	var trackSound = function() {
		var freqArray = new Uint8Array(analyser.frequencyBinCount);
		analyser.getFloatFrequencyData(freqArray);
	
		readFrequencies(freqArray);
		
		window.requestAnimationFrame(trackSound);
	}
	
	window.requestAnimationFrame(trackSound);
	
	
	var once = false;
	
	function readFrequencies(freqArray) {
	
		/*if(!once) {
			once = true;
			console.log(freqArray);
			
		}
		// find the highest freqId in the table.
		var highest, 
			db, 
			nyquist = context.sampleRate / 2, 
			index;
		
		freqTable.forEach(function(freq, i) {
			index = Math.round(freq / nyquist * freqArray.length);
			db = freqArray[index];
			
			if(!highest || db > highest.db ) {
				highest = {
					freq: freq,
					freqId: i + 1,
					db:db
				}
			}
		});	
		// print...
		console.log(highest);*/
		
	}
};



/*
soundBox.connect = function() {
  // build the freq table
	var freqTable = new FreqTable(args),
		// audio context for all of teh magic.
		context = new _AudioContext(),
		// Buffer for smooth audio?
		source = context.createBufferSource(),
		// Gain node. Ye.
		gain = context.createGain();
	
	// Wire up.
	source.connect(gain);
	gain.connect(context.destination);
};*/



var playing;
	
soundBox.play = function(freqId) {
	if(playing) {
		playing.pause();
	}
	playing = T("sin", { 
		freq : freqTable.get(freqId)
	});
	playing.play();
};

soundBox.pause = function() {
	if(playing) playing.pause();
};




/* 
    build frequency list :D
*/
function FreqTable(args) {
	// parse arguments
	var scaleLength = args && args.scaleLength || 12,
		highestIndex = args && args.highestIndex || 9,
		// starting point for the scale
		refPoint = args && args.refPoint || 16.352, 
    	ratio = Math.pow(2, 1 / (1.0 * scaleLength)),
    	frequencies = [];
	
    frequencies[0] = refPoint;
    for (var i = 1; i < highestIndex * scaleLength; i++) {
        frequencies[i] = frequencies[i - 1] * ratio;
    }

    this.get = function (i) {
        return frequencies[i-1];
    };
	
	
	// Find the closest note to the captured frequency.
	this.findClosest = function(freq){
		var 
			// The closest frequencyId.
			closest,
			// The distance in terms of freq that the closest 
			// frequencyId found has to the captured frequency.
			distance,
			// temp...
			diff;
			
		frequencies.forEach(function(val){
			diff = Math.abs(freq - val);
			
			if(!closest || diff < distance){
				distance = diff;
				closest = val;
			}
		});
		return closest;
	};
	
	this.forEach = frequencies.forEach;
}

})();
