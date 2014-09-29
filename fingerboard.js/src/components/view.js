
/******************************************************************************
 * View
 */
 
(function(Fingerboard, ContextWrapper) {


Fingerboard.View = function($canvas, model, events) {

	var 
		canvas = $canvas[0],
		context =  new ContextWrapper(canvas.getContext('2d')),
		width, height;

	model.settings.selectors.selected = 'gray';
	model.settings.selectors.tonic = 'firebrick';
	
	// internal colors
	var colors = {
		strings: 'gray',
		inlays:'#D1A319',
		frets: 'gray'
	};
	
	function drawDiamond(x, y, width, height){
		context
			.beginAt(x - (width / 2), y)
			.lineTo(x, (y - (height / 2)))
			.lineTo(x + (width / 2), y)
			.lineTo(x, y + (height / 2))
			.fillStyle(colors.inlays)
			.fill();
	}

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
			endArc = Math.PI * 2, inlayX;
		
		// radius for circles :D
		var radius = (heightRatio > openWidth ? 
			openWidth / 4 : heightRatio / 4),
			helperRadius = radius * 2 / 3;
		
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
						drawDiamond(inlayX, height / 2, radius * 3, radius *6);
						break;
					// draw the double inlay
					case 12:
						drawDiamond(inlayX, height / 3, radius * 3, radius *6);
						drawDiamond(inlayX, 2 * (height / 3), radius * 3, radius *6);
						break;
				}
			}
			
			// Draw the circle if its selected
			if(note.selector !== '' && (color = selectors[note.selector])) {
				context.beginPath()
					.color(color)
					.arc(inlayX, stringH, radius, 0, endArc)
					.fill();
			}
		});
	
	// Don't use, this will be removed eventually.
	/*var oldRenderer = function(fret, string, note) {
		fretStart = fret * widthRatio;
		fretEnd = fretStart + widthRatio -1;		
		stringH = ((string-1) * heightRatio) + (heightRatio / 2);
		
		// I need the dimension of each note on the fingerboard
		// to be stored for later access for the event service.
		note.dimension.x1 = fretStart;
		note.dimension.y1 = stringH - (heightRatio / 2);
		note.dimension.x2 = fretEnd;
		note.dimension.y2 = stringH + (heightRatio / 2);
		
	
		// draw the string
		if(fret === 0)
			context.beginPath()
				.color('gray')
				.moveTo(0, stringH)
				.lineTo(width, stringH)
				.stroke();
			
		
		if(string == 1) {
			// draw the fret
			fretW = fret * widthRatio + 1;
			context.beginPath()
				.color('gray')
				.moveTo(fretStart, 0)
				.lineTo(fretStart, height)
				.stroke();
			
			switch(fret) {
				// draw the helper dots
				case 3: case 5: case 7: case 9:
					context.beginPath()
						.color('#D1A319')
						.arc(fretStart + (widthRatio / 2), height / 2, helperRadius, 0, endArc)
						.fill();
					break;
				// draw the double dot
				case 12:
					context.beginPath()
						.color('#D1A319')
						.arc(fretStart + (widthRatio / 2), height / 3, helperRadius, 0, endArc)
						.fill();
					context.beginPath()
						.color('#D1A319')
						.arc(fretStart + (widthRatio / 2), 2*(height/3), helperRadius, 0, endArc)
						.fill();
					break;
			}
		}
		
		// Draw the circle if its selected
		if(note.selector !== '' && (color = selectors[note.selector])) {
			context.beginPath()
				.color(color)
				.arc(fretStart + (widthRatio / 2), stringH, radius, 0, endArc)
				.fill();
		}
	};
	*/
	
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
			stateChanged = !model.mouseHoveredNote.dimension.isPointWithinBounds(x, y);
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
	
	// We'll need to make sure that the view listens for changes in the model
	events.modelchange(repaint);
};


})(Fingerboard, Fingerboard.ContextWrapper);