(function(jsRoutes, CONST, $){



// variables

var $scaleEditor = $('#scale-value-editor'),
	$nameEditor = $('#scale-name-editor'),
	$lastElem,
	editorMode = 'add',
	selectedId = undefined,
	$updateBtn = $('#btn-update'),
	$cancelBtn = $('#btn-cancel'),
	$addBtn = $('#btn-add')

// templating stuff

// Lets make it better for use with jquery...
Handlebars.$load = function(selector){
	var html = $(selector).html(),
		template = Handlebars.compile(html)
	
	return function(args){
		return $(template(args))
	}
}

var rowTemplate = Handlebars.$load('#row-template')

var valuesInputTemplate = (function(){
	var baseHtml = $('#values-input-template').html(),
		local = Handlebars.compile(baseHtml)
	
	return function(values){
	
		var html = values.map(function(val){
			var obj = {options:[]}
			
			for(var key in CONST.intervals){
				if(CONST.intervals.hasOwnProperty(key)){
					obj.options.push({ 
						key: key, 
						'value': CONST.intervals[key],
						selected: key == val
					})
				}
			}
			
			return local(obj)
			
		}).join('')
		
		return $(html)
	}
})()


// event handlers

var onDropdownChange = function(){
	var $t = $(this)

	if($lastElem === undefined || $lastElem[0] === $t[0]){

		var $c = $t.clone(true)
		$scaleEditor.append($c)
		$lastElem = $c
		
	} else if($t.find('select').val() === ''){
		$t.remove()
	}
}

var onDelete = function (){
	var $t = $(this).parent().parent(), 
		id = $t.attr('data-id')
	
	if(selectedId == id ){
		emptyInput()
	}
	
	jsRoutes.controllers.Scales.remove(id).ajax({
		success: function(result){
			$t.remove()
		},
		error: defaultError
	})
}

var onUpdate = withValidInput(function(name, strValues){
	
	jsRoutes.controllers.Scales.update(selectedId, name, strValues).ajax({
		
		success: function(scale){
		
			scale.prettyValues = scale
				.values
				.split(',')
				.map(function(val) { return CONST.intervals[val] }, "")
				.join(" - ")
				
			// update the row with the matching id.
			var $tr = $('tr[data-id="' + scale.id + '"]'),
				$tds = $tr.find('td')
			
			$tr.attr('data-values', scale.values)
			$tds.eq(0).text(scale.name)
			$tds.eq(1).text(scale.prettyValues)
			
			
			// We should clear everything just a though 
			// the person pressed the cancel button.
			onCancel()
		
		},
		error: defaultError
	})
	
})

var onCancel = function(){
	selectedId = undefined
	emptyInput()
	$updateBtn.addClass('hide')
	$cancelBtn.addClass('hide')
	$addBtn.removeClass('hide')
}

var onRowModify = function(){

	emptyInput()
	
	// lets load dat info
	
	var $t = $(this),
		$tr = $t.parent().parent()
		values = $tr.attr('data-values').split(','),
		name = $tr.find('td:first-child').text()
		
	selectedId = Number($tr.attr('data-id'))
	console.log('selected id: ', selectedId)
	
	// place stored data into inputs
	valuesInputTemplate(values).prependTo('#scale-value-editor')
	$nameEditor.val(name)
	
	
	// show the appropriate buttons
	$updateBtn.removeClass('hide')
	$cancelBtn.removeClass('hide')
	$addBtn.addClass('hide')
	
}

var onAdd = withValidInput(function(name, strValues){

	jsRoutes.controllers.Scales.add(name, strValues).ajax({
		success: function(scale){

			scale.prettyValues = scale
				.values
				.split(',')
				.map(function(val) { return CONST.intervals[val] }, "")
				.join(" - ")
				
			var $row = rowTemplate(scale)
			
			$('#scales-list').prepend($row)
			$row.find('.row-delete').click(onDelete)
			$row.find('.row-modify').click(onButtonModify)
			
			emptyInput()
		},
		error: defaultError
	
	})
})

// event handler wireups



$('.row-delete').click(onDelete)
$('.row-modify').click(onRowModify)

$addBtn.click(onAdd)
$updateBtn.click(onUpdate)
$cancelBtn.click(onCancel)

$scaleEditor.find('div.col-md-2').change(onDropdownChange)

// helper functions

function emptyInput(){
	$nameEditor.val('')
	
	$scaleEditor
		.find('select')
		.filter(function(i, e) { return e.value !== "" })
		.map(function(i ,e) { return e.parentNode })
		.remove()
	
}

function withValidInput(callback){
	return function(){
	
		var values = $scaleEditor
			.find('select')
			.map(function(i, e) { return e.value })
			.toArray()
			.filter(function(val) { return val !== "" })
			
		var name = $nameEditor.val()
		
		
		if(validateInput(name, values)){
		
			values.sort()
			var strValues = values.join(',')
			
			callback(name, strValues)
			
		}
	}
}

function validateInput(name, values){
	
	var error = (function(){
		var $tgt = {
			name: $('#name-header'),
			values: $('#values-header')
		}
	
		return function(msg, tgtName){
				
			var html =
					'<small class="text-danger bad-input">' + 
						'&nbsp&nbsp;&nbsp;' + msg + 
					'</small>' 
			
			$tgt[tgtName].append($(html))
		}
	})()
	
	$('.bad-input').remove()
	
	if(!name.match(CONST.nameConstraint)){
		error('Name is invalid or too short.', 'name')
		return false
	} else if(values.length < 2){
		error('Scale must have at least two values', 'values')
		return false
	} else {
	
		// we got some duplicate entries?
		var duplicates = values.reduce(function(has, val, i, arr){
			if(has === true || has[val]) return true 
			else if(i == arr.length - 1) return false
			else {
				has[val] = true
				return has
			}
		}, {})
		
		
		if(duplicates){
			error('The scale you have given has duplicate values.', 'values')
			return false
		}
	}
	
	return true
}

function defaultError(x, status, err){
	alert(
		'- Error -\n' +
		'Status: ' + status + '\n' + 
		'Error Thrown: ' + err
		)
}

})(jsRoutes, CONST, jQuery)









