(function($, routes){

var $inputContainer = $('#input-container')
var notation = [
	'C', 'Db', 'D', 
	'Eb', 'E', 'F', 
	'Gb', 'G', 'Ab', 
	'A', 'Bb', 'B'
]

var $canvas = $('#tuning-view')

var fingerboard = new Fingerboard($canvas, {
	strings: 1,
	frets: 13,
	interval:{
		tuning: [0]
	},
	scale:{
		// Just want to have a view of C Major
		values: [1, 3, 5, 6, 8, 10, 12],
		root: 1,
		select: true
	},
	display:{
		// use the notation preset
		selector: Fingerboard.Selectors.notation()
	}
})

/**
 * Templates
 */
 
// Lets make it better for use with jquery...
Handlebars.$load = function(selector){
	var html = $(selector).html(),
		template = Handlebars.compile(html)
	
	return function(args){
		return $(template(args))
	}
}

var tuningValuesTemplate = (function(){
	var $init = Handlebars.$load('#tuning-values-template')
	return function($target, i){
		var $input = $init({
				notes: notation,
				indexes: [0, 1, 2, 3, 4, 5, 6, 7, 8],
				i: i
			})
		
		$target.append($input)
		
		$input.find('.tuning-note-input a').click(updateNote)
		$input.find('.tuning-index-input a').click(updateIndex)
	}
})()

var tuningInputTemplate = (function(){
	var $init = Handlebars.$load('#tuning-input-template')
	
	return function(args){
		
		var 
			i = args.strings
			tuning = [],
			$input = $init({})
		
		args.$tieTo.append($input)
		
		var $target = $('#tuning-dropdown-loc')
			
		while(i > 0){
			tuningValuesTemplate($target, i--)
			tuning.push(0)
		}
		
		// move the canvas to its appropriate location so it 
		// can be seen
		$('#tuning-values-loc').append($canvas)
		
		// The height will be relative to the number of strings
		// on the instrument.
		$canvas.height(args.strings * 55)
		
		fingerboard.set({
			strings: args.strings,
			interval:{
				tuning: tuning
			}
		})
		
		// Now the events for this scene...
		$input.find('#input-tuning-ok').click(args.onOk)
		$input.find('#input-tuning-cancel').click(args.onCancel)
	}
})()

var instrumentInputTemplate = (function(){
	var $init = Handlebars.$load('#instrument-input-template')
	
	return function(args){
		var $target = args.$tieTo,
			$input = $init(args)
			
		
		$target.html('')
		$target.append($input)
		
		var $okBtn = $input.find('#instrument-ok'),
			$cancelBtn = $input.find('#instrument-cancel')
		
		if(args.isUpdate)
			$okBtn.click(updateInstrument)
		else
			$okBtn.click(insertInstrument)
			
		$cancelBtn.click(cancelInstrument)
		
		return $input
	}
})()

var instrumentRowTemplate = (function(){
	var $init = Handlebars.$load('#instrument-row-template')
	return function(args){
		var $row = $init(args)
		
		args.$tieTo.prepend($row)
		
		$row.find('.mod-instrument').click(modifyInstrument)
	}
})()

var startTemplate = function(args){
	var $btn = $('<button class="btn btn-sm btn-success" id="inst-add-btn">Add</button>')
	args.$tieTo.append($btn)
	$btn.click(addInstrument)
}

/**
 * Initialization
 */

$('.mod-instrument').click(modifyInstrument)
//$('#inst-add-btn').click(addInstrument)
//tuningValuesTemplate('#input-tuning')
startTemplate({ $tieTo: $inputContainer })




/**
 * Event Handlers
 */

function onServerError(xhr){
	var response = JSON.parse(xhr.messageBody)
	alert(response.errorMessage)
}

/**
 * Event Handlers: Tunings
 */

function updateIndex(e){
	var $t = $(this),
		$group = $t.parents('.tuning-dropdown-group'),
		newIndex = Number($t.text())
		
	$group.data('index', newIndex)
	
	// now we update the tuning with the changes
	inputUpdateTuning($group)
	
	e.preventDefault()
}

function updateNote(e){
	var $t = $(this),
		$group = $t.parents('.tuning-dropdown-group'),
		newNote = notation.indexOf($t.text())
	
	$group.data('note', newNote)
	
	// now we update the tuning with the changes
	inputUpdateTuning($group)
	
	e.preventDefault()
}

function inputUpdateTuning($group){
	var 
		note = $group.data('note'),
		index = $group.data('index'),
		i = $group.data('i'),
		tuning = fingerboard.tuning()
	
	tuning[i] = index * 12 + note
	
	fingerboard.set({
		interval:{
			tuning: tuning
		}
	})
}

function withValidTuning(callback){
	// the tuning is easy, the fingerboard has it stored already.
	var tuning = fingerboard.tuning().join(','),
		$tuningInput = $('#input-tuning-name'),
		tuningName = $tuningInput.val(),
		$p = $tuningInput.parent()
	
	// clear out all possible error messages
	$inputContainer.find('.bad-input').remove()
	
	if(tuningName.match(CONST.tuning)){
		callback(tuningName, tuning)
	} else {
		var html = 
			'<small class="text-danger bad-input">' + 
				'&nbsp&nbsp;&nbsp;Your tuning\'s name is using illegal characters.'
			'</small>' 
		$inputContainer.find('#input-tuning-name-label').append(html)
	}
}

function addTuning(){

}

function removeTuning(){

}

function cancelTuning(){

}

/**
 * Event Handlers: Instruments
 */
function addInstrument(){
	instrumentInputTemplate({$tieTo: $inputContainer})
}


function modifyInstrument(e){
	
	var 
		$t = $(this),
		$tr = $t.parent().parent(),
		id = $tr.data('id'),
		name = $tr.find('td:first-child').text(),
		strings = $tr.find('td:nth-child(2)').text(),
		defaultTuning = $tr.data('default-tuning')
	
	//console.log('id',id,'name',name,'strings',strings)
	
	instrumentInputTemplate({
		$tieTo: $inputContainer,
		isUpdate: true,
		id: id,
		name: name,
		strings: strings,
		defaultTuning: defaultTuning
	})
	
}

function cancelInstrument(){
	$inputContainer.html('')
	var $btn = $(
		'<button class="btn btn-sm btn-success" id="inst-add-btn">' + 
			'Add' + 
		'</button>'
	)
	
	$inputContainer.append($btn)
	
	$btn.click(addInstrument)
}

function updateInstrument(){
	withValidInstrument(function(name, strings, id, defaultTuning){
		
		routes.Instruments.update(id, name, strings, defaultTuning).ajax({
			success: function(instrument){
				console.log(instrument)
				// update the name
				alert('W00t!')
			},
			error: onServerError
		})
		
		$inputContainer.html('')
	})
}

function insertInstrument(){
	withValidInstrument(function(name, strings){
		
		$inputContainer.html(
			'<div class="alert alert-info">' + 
				'Please enter the default tuning.' + 
			'</div>'
		)
		
		tuningInputTemplate({
			$tieTo: $inputContainer,
			strings: strings,
			onOk: function(e){
				console.log('on ok triggered')
				// validate, then we can send the data
				withValidTuning(function(tuningName, tuning){
					console.log('validation passed')
					routes.Instruments.insert(name, strings, tuningName, tuning).ajax({
						success: function(response){
							var inst = response.instrument,
								tuning = response.tuning
							
							console.log('server response:',response)
							
							// just add the instrument to the row
							instrumentRowTemplate({
								$tieTo: $('#instruments-table tbody'),
								instrument: inst
							})
							
							console.log(tunings)
							
							// add the tuning
							
							tunings[inst.id] = [tuning]
							
							$inputContainer.html('')
							startTemplate({$tieTo: $inputContainer})
							
						},
						error: onServerError
					})
				})
			},
			onCancel: function(e){
				$inputContainer.html('')
				startTemplate({$tieTo: $inputContainer})
			}
		})
		
	})
}

function withValidInstrument(callback){
	var
		id = $('#input-id').val(),
		name = $('#input-name').val(),
		strings = $('#input-strings').val(),
		defaultTuning = $('#input-default-tuning').val()
	
	var error = (function(){
		var $tgt = {
			name: $('#name-header'),
			strings: $('#strings-header')
		}
	
		return function(msg, tgtName){
				
			var html =
					'<small class="text-danger bad-input">' + 
						'&nbsp&nbsp;&nbsp;' + msg + 
					'</small>' 
			
			$tgt[tgtName].append($(html))
		}
	})()
	
	$inputContainer.find('.bad-input').remove()
	
	
	console.log('name:',name,' strings:',strings,' id:',id)
	
	if(!name.match(CONST.instrument))
		error('Your name is using illegal characters.', 'name')
	else if(isNaN(strings) || !strings.match(/^\d/g))
		error('Input must be a whole number.', 'strings')
	else if(Number(strings) > 16)
		error('Too many strings for us to display on the fingerboard, friend.', 'strings')
	else
		callback(name, strings, id, defaultTuning)
	
	
}


})(jQuery,jsRoutes.controllers)