(function($, routes){

var $inputContainer = $('#input-container')

// Templates

// Lets make it better for use with jquery...
Handlebars.$load = function(selector){
	var html = $(selector).html(),
		template = Handlebars.compile(html)
	
	return function(args){
		return $(template(args))
	}
}

var instrumentTemplate = (function(){
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



$('.mod-instrument').click(modifyInstrument)
$('#inst-add-btn').click(addInstrument)


function onServerError(xhr){
	var response = JSON.parse(xhr.messageBody);
	alert(response.errorMessage)
}

function addInstrument(){
	instrumentTemplate({$tieTo: $inputContainer})
	
}


function modifyInstrument(e){
	
	var 
		$t = $(this),
		$tr = $t.parent().parent(),
		id = $tr.attr('data-id'),
		name = $tr.find('td:first-child').text(),
		strings = $tr.find('td:nth-child(2)').text()
	
	console.log('id',id,'name',name,'strings',strings)
	
	instrumentTemplate({
		$tieTo: $inputContainer,
		isUpdate: true,
		id: id,
		name: name,
		strings: strings
	})
	
}

function cancelInstrument(){
	$inputContainer.html('')
	var $btn = $('<button class="btn btn-sm btn-success" id="inst-add-btn">Add</button>')
	
	$inputContainer.append($btn)
	
	$btn.click(addInstrument)
}

function updateInstrument(){
	withValidInstrument(function(name, strings, id){
		
		routes.Instruments.update(id, name, strings).ajax({
			success: function(){
				alert('W00t!')
			},
			error: onServerError
		})
		
		
		$inputContainer.html('')
	})
}

function insertInstrument(){
	withValidInstrument(function(name, strings){
		
		
		$inputContainer.html('')
	})
}

function withValidInstrument(callback){
	var
		id = $('#input-id').val(),
		name = $('#input-name').val(),
		strings = $('#input-strings').val()
	
	
	
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
		callback(name, strings, id)
	
	
}


})(jQuery,jsRoutes.controllers)