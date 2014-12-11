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
}*/