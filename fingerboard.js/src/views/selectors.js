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


})(Fingerboard)