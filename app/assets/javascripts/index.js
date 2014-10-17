$(function() {
    var $canvas = $('#fingerboard');
    $canvas.css('width', '100%');

    var fingerboard = new Fingerboard($canvas, {
        strings: 6,
        frets: 14,
        selectors : {
            playing: '#1975FF'
        },
        interval: {
        	westernSystem: true
        },
        scale: {
            values: [1, 3, 5, 6, 8, 10, 12],
            root: 1,
            select: true
        }
    });
    
    // Change the selector to blue and play the sound when clicked on
    var lastNote;
    fingerboard.noteclick(function(note) {
        if(lastNote) {
            var slct;
            if(lastNote.scaleValue)
                if(lastNote.scaleValue === 1)
                    slct = 'tonic';
                else
                    slct = 'selected';
            else 
                slct = '';
            
            fingerboard.setNoteFor(lastNote.fret, lastNote.string, { selector: slct });
        }
        
        if(!lastNote || lastNote.interval.freqId !== note.interval.freqId) {
            fingerboard.setNoteFor(note.fret, note.string, { selector: 'playing' } );
            player = soundBox.play(note.interval.freqId);
            lastNote = note;
        } else {
            soundBox.pause();
            lastNote = undefined;
        }
    });

    // Displays info about the note that the mouse is currently hovered on
    var $p = $('#last-selected');
    fingerboard.notehover(function(note) {
        $p.text(
            'note : ' + note.interval.notation +note.interval.index 
        );
    });
    
    // ----------------------------------------------------
    // Dropdown: basic functionality
    
    var onaclick = function(btnSelector, liSelector, callback) {
        var $btn = $(btnSelector);
        
        $(liSelector).find('a').click(function(ev) {
            var $t = $(ev.target);
            callback($t);
            $btn[0].innerHTML = $t.text() + ' &nbsp;<span class="caret"></span>';
            ev.preventDefault();
        });
    };
    
    // ----------------------------------------------------
    // Dropdown: scale
    
    onaclick('#scale-button', '#scale-selector', function($t) {
        fingerboard.set({
            scale: {
                values: $t.attr('value'),
                select: true
            }
        });
    });

    // ----------------------------------------------------
    // Dropdown: root
    
    onaclick('#root-button', '#root-selector', function($t) {
        var root = Number($t.attr('value'));

        fingerboard.set({
            scale: {
                root: root,
                select: true
            }
        });
    });
    
    // ----------------------------------------------------
    // Dropdown: tuning
    
    var $tuningButton = $('#tuning-button');
    
    var tuningOnClick = function (ev) {
		var $t = $(ev.target);

		fingerboard.set({
    		interval:{
    			tuning: $t.attr('value')
    		}
    	});
		
        $tuningButton[0].innerHTML = $t.text() + 
        	' &nbsp;<span class="caret"></span>';
        ev.preventDefault();  
	};
	
    var tooltipProps = {
    	placement: 'right',
    	container: 'body'
    };
    
    var tuningValuesToTitle = function(val) {
    	return val
    		.split(",")
    		.map(function(num){
    			return fingerboard.notationFromFreqId(Number(num))
    		})
    		.join(" - ")
    }
    
    var $tuningElem;
	$('#tuning-selector')
		.find('a')
		.each(function(key, elem){
			$tuningElem = $(elem);
			$tuningElem.attr('title', 
					tuningValuesToTitle($tuningElem.attr('value')));
		})
		.click(tuningOnClick)
		.tooltip(tooltipProps);
    
    // ----------------------------------------------------
    // Dropdown: instrument
    
    onaclick('#instrument-button', '#instrument-selector', function($t) {
        var name = $t.text();
        var strings = $t.attr('value');
		  console.log(jsRoutes);
        $.ajax({
			url:(function(){
				//var self = window.location.href,
				//	url = new URL(self);
				return '/tuning/of-instrument';
			})(),
			data:{
				name: name
			},
        	success: function(data){
        		// First, reset the button to simply display "Tuning" like when
        		// the page initially loads.
        		$('#tuning-button')[0].innerHTML = 
        			'Tuning &nbsp;<span class="caret"></span>';
        		
        		// Now we change the list items to the ones corresponding to
        		// the selected instrument.
        		var $ul = $('#tuning-selector');
        		$ul.empty();
        		
        		data.forEach(function(tuning) {
        			var $a = $('<a></a>', {
        				text: tuning.name,
        				href: "#",
        				value: tuning.values,
        				on:{
        					click: tuningOnClick
        				}, 
        				title: tuningValuesToTitle(tuning.values)
        			});
        			$ul.append(
        					$('<li></li>').append($a));
        		});
        		
        		// Annnd now we activate Bootstrap's tooltip.
        		$ul.find('a').tooltip(tooltipProps);
        		
        		// And then we need to update the fingerboard's data as
        		// there may be a different number of strings.
        		fingerboard.set({
        			strings: strings,
        			// lets change the tuning to the first one in the list,
        			// hopefully its going to be the "standard" tuning for the
        			// instrument.
        			interval: {
        				tuning: data[0].values
        			}
        		});
        	},
        	error:function(){
        		alert("Error fetching data from server.");
        	}
        });
    });
});