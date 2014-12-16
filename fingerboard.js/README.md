Fingerboard.js
==============

Little library for rendering fingerboards of musical instruments. 

The constructor takes in a jQuery object pointing to a canvas html element and an options object.

```javascript
new Fingerboard($canvas, options)
```

Options are as follows

```
options: {
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
```

Simple example:

```javascript

var 
	$canvas = $('#my-canvas'),
	fingerboard = new Fingerboard($canvas,{
		frets: 14,
		strings: 6,
		interval:{
		},
		scale:{ // lets select C major
			values: [1, 3, 5, 6, 8, 10, 12],
			root: 1,
			select: true
		}
	})
```
	