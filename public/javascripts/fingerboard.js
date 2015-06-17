(function() {
  var ContextWrapper, Fingerboard, Interval, Model, Note, View, endArc, mkEvents,
    __slice = [].slice,
    __bind = function(fn, me){ return function(){ return fn.apply(me, arguments); }; };

  mkEvents = function() {
    var key, listeners, self;
    listeners = {
      noteclick: [],
      notehover: [],
      modelchange: []
    };
    self = {
      broadcast: function() {
        var event, listener, pass, _i, _len, _ref, _results;
        event = arguments[0], pass = 2 <= arguments.length ? __slice.call(arguments, 1) : [];
        _ref = listeners[event];
        _results = [];
        for (_i = 0, _len = _ref.length; _i < _len; _i++) {
          listener = _ref[_i];
          _results.push(listener.apply(void 0, pass));
        }
        return _results;
      },
      on: function(event, callback) {
        return listeners[event].push(callback);
      }
    };
    for (key in listeners) {
      self[key] = (function(key) {
        return function(callback) {
          return listeners[key].push(callback);
        };
      })(key);
    }
    return self;
  };


  /* Constructor Arguments
  		model:
  			frets : Number
  			strings: Number
  			tuning: Array<Number>
  			interval:
  				notation: Array<String>
  				maxIndex: Number
  			scale:
  				values: Array<Number>
  				root: Number
  			selector: Function (Note) => String
  		view:
  			drawSelector
  			drawString
  			drawFret
   */

  Fingerboard = (function() {
    function Fingerboard($canvas, args) {
      var events, key, model, view;
      events = mkEvents();
      for (key in events) {
        this[key] = events[key];
      }
      args = args || {};
      model = new Model(args.model || {}, events);
      view = new View(args.view || {}, $canvas, model, events);
      view.updateDimensions();
      view.paint();
      this.forEach = function(traversor) {
        return model.forEach(function(note, fret, string) {
          return traversor(note, fret, string);
        });
      };
      this.set = function(args) {
        if (args.view) {
          view.set(args.view);
        }
        if (args.model) {
          model.set(args.model);
        }
        if (args.view && !args.model) {
          return view.repaint();
        }
      };
    }

    return Fingerboard;

  })();

  window.Fingerboard = Fingerboard;


  /*
  	Just a little wrapper for the HTML5 canvas 2d context.
  	Method chains! \o/
   */

  ContextWrapper = (function() {
    function ContextWrapper(context) {
      var key;
      for (key in context) {
        if (key !== 'webkitImageSmoothingEnabled') {
          if (typeof context[key] === 'function') {
            this[key] = (function(key) {
              return function() {
                context[key].apply(context, arguments);
                return this;
              };
            })(key);
          } else {
            this[key] = (function(key) {
              return function(val) {
                context[key] = val;
                return this;
              };
            })(key);
          }
        }
      }
      this.context = context;
    }

    ContextWrapper.prototype.begin = function() {
      this.context.beginPath();
      return this;
    };

    ContextWrapper.prototype.beginAt = function(x, y) {
      this.context.beginPath();
      this.context.moveTo(x, y);
      return this;
    };

    ContextWrapper.prototype.color = function(col) {
      this.context.fillStyle = col;
      return this;
    };

    ContextWrapper.prototype.get = function(key) {
      return this.context[key];
    };

    return ContextWrapper;

  })();


  /*
   * The Mother of All Models in this project.
   */

  Model = (function() {

    /*
    	 * construction functions
     */

    /*
    		~Options~
    
    		frets : Number
    		strings: Number
    		tuning: Array<Number>
    		interval:
    			notation: Array<String>
    			maxIndex: Number
    		scale:
    			values: Array<Number>
    			root: Number
     */
    function Model(args, events) {
      this.events = events;
      this.notes = [[]];
      this.notation = ['C', 'Db', 'D', 'Eb', 'E', 'F', 'Gb', 'G', 'Ab', 'A', 'Bb', 'B'];
      this.root = 1;
      this.tuning = [28, 33, 38, 43, 47, 52];
      this.scale = void 0;
      this.scaleLength = this.notation.length;
      this.strings = 6;
      this.frets = 16;
      this.maxIndex = 8;
      this.set(args);
    }

    Model.prototype.fill = function() {
      var fret, string, _i, _ref, _results;
      this.notes = [];
      _results = [];
      for (fret = _i = 0, _ref = this.frets; 0 <= _ref ? _i <= _ref : _i >= _ref; fret = 0 <= _ref ? ++_i : --_i) {
        this.notes[fret] = [];
        _results.push((function() {
          var _j, _ref1, _results1;
          _results1 = [];
          for (string = _j = 1, _ref1 = this.strings; 1 <= _ref1 ? _j <= _ref1 : _j >= _ref1; string = 1 <= _ref1 ? ++_j : --_j) {
            _results1.push(this.notes[fret][string - 1] = new Note(fret, string));
          }
          return _results1;
        }).call(this));
      }
      return _results;
    };

    Model.prototype.buildInterval = function() {
      var intervals, ln, _i, _results;
      if (this.tuning.length !== this.strings) {
        throw 'Tuning is invalid for the number of strings given.';
      }
      ln = (this.scaleLength * (this.maxIndex + 1)) - 1;
      intervals = (function() {
        _results = [];
        for (var _i = 0; 0 <= ln ? _i <= ln : _i >= ln; 0 <= ln ? _i++ : _i--){ _results.push(_i); }
        return _results;
      }).apply(this).map((function(_this) {
        return function(i) {
          var f, index, intervalValue;
          f = i + 1;
          index = Math.floor(i / _this.scaleLength);
          intervalValue = f - (index * _this.scaleLength);
          return {
            value: intervalValue,
            index: index,
            freqId: f,
            notation: _this.notation[intervalValue - 1]
          };
        };
      })(this));
      return this.forEach((function(_this) {
        return function(note, fret, string) {
          var interval, key, _results1;
          interval = intervals[_this.tuning[string - 1] + fret];
          _results1 = [];
          for (key in interval) {
            _results1.push(note.interval[key] = interval[key]);
          }
          return _results1;
        };
      })(this));
    };

    Model.prototype.buildRootedValue = function() {
      return this.forEach((function(_this) {
        return function(note, fret, string) {
          note.interval.shift = note.interval.value - _this.root + 1;
          if (note.interval.shift < 1) {
            return note.interval.shift += _this.scaleLength;
          }
        };
      })(this));
    };

    Model.prototype.buildScale = function() {
      var degree, scale, _i, _ref, _results;
      degree = void 0;
      scale = (function() {
        _results = [];
        for (var _i = 1, _ref = this.scaleLength; 1 <= _ref ? _i <= _ref : _i >= _ref; 1 <= _ref ? _i++ : _i--){ _results.push(_i); }
        return _results;
      }).apply(this).map((function(_this) {
        return function(i) {
          return _this.scale.indexOf(i) + 1;
        };
      })(this));
      return this.forEach((function(_this) {
        return function(note, fret, string) {
          if (degree = scale[note.interval.shift - 1]) {
            return note.interval.degree = degree;
          } else {
            return note.interval.degree = void 0;
          }
        };
      })(this));
    };


    /*
    		~Options~
    
    		frets : Number
    		strings: Number
    		tuning: Array<Number>
    		interval:
    			notation: Array<String>
    			maxIndex: Number
    		scale:
    			values: Array<Number>
    			root: Number
     */

    Model.prototype.set = function(args) {
      var buildInterval, buildRootedValue, buildScale, fill, i, s;
      if (args === void 0) {
        throw 'Oye, forgot something? I need an options object.';
      }
      if (args.strings) {
        this.strings = args.strings;
      }
      if (args.frets) {
        this.frets = args.frets;
      }
      if (args.tuning) {
        this.tuning = this.asJSArray(args.tuning);
      }
      if (args.interval) {
        i = args.interval;
        if (i.notation) {
          this.notation = this.asJSArray(i.notation);
          this.scaleLength = this.notation.length;
        }
        if (i.maxIndex) {
          this.maxIndex = i.maxIndex;
        }
      }
      if (args.scale) {
        s = args.scale;
        if (s.values) {
          this.scale = this.asJSArray(s.values);
        }
        if (s.root) {
          this.root = s.root;
        }
      }
      fill = args.strings || args.frets || !this.notes[0][0];
      buildInterval = args.tuning || args.interval || fill;
      buildRootedValue = buildInterval || (args.scale && args.scale.root);
      buildScale = args.scale || (buildRootedValue && this.scale);
      if (fill) {
        this.fill();
      }
      if (buildInterval) {
        this.buildInterval();
      }
      if (buildRootedValue) {
        this.buildRootedValue();
      }
      if (buildScale) {
        this.buildScale();
      }
      if (fill || buildInterval || buildRootedValue || buildScale) {
        return this.events.broadcast('modelchange');
      }
    };

    Model.prototype.asJSArray = function(arr) {
      if (typeof arr === 'string') {
        if (arr[0] === '[') {
          return JSON.parse(arr);
        } else if (arr.indexOf(',') !== -1) {
          return arr.split(',').map(function(val) {
            if (isNaN(val)) {
              throw 'Invalid array input.';
            } else {
              return Number(val);
            }
          });
        } else {
          throw 'Could not parse array argument';
        }
      } else {
        return arr;
      }
    };


    /*
    	 * Traversing functions
     */

    Model.prototype.forEach = function(traversor) {
      var fret, fretArr, note, string, _i, _j, _len, _len1, _ref;
      _ref = this.notes;
      for (fret = _i = 0, _len = _ref.length; _i < _len; fret = ++_i) {
        fretArr = _ref[fret];
        for (string = _j = 0, _len1 = fretArr.length; _j < _len1; string = ++_j) {
          note = fretArr[string];
          if (traversor(note, fret, string + 1) === false) {
            return;
          }
        }
      }
    };

    Model.prototype.find = function(traversor) {
      var result;
      result = void 0;
      this.forEach(function(note, fret, string) {
        if (traversor(note, fret, string) === true) {
          result = note;
          return false;
        }
      });
      return result;
    };

    return Model;

  })();

  Interval = (function() {
    function Interval() {
      this.freqId = -1;
      this.index = -1;
      this.value = -1;
      this.notation = '';
      this.shift = -1;
      this.degree = -1;
    }

    return Interval;

  })();

  Note = (function() {
    function Note(fret, string) {
      this.fret = fret;
      this.string = string;
      this.interval = new Interval;
    }

    Note.prototype.log = function() {
      return console.log(this);
    };

    return Note;

  })();


  /*
    Draw thing
   */

  View = (function() {
    View.prototype.endArc = Math.PI * 2;

    function View(args, canvas, model, events) {
      this.canvas = canvas;
      this.model = model;
      this.events = events;
      this.repaint = __bind(this.repaint, this);
      this.onMouseMove = __bind(this.onMouseMove, this);
      this.onMouseClick = __bind(this.onMouseClick, this);
      this.context = new ContextWrapper(this.canvas.getContext('2d'));
      this.canvas.style['user-select'] = 'none';
      this.canvas.style['-webkit-user-select'] = 'none';
      this.canvas.style['-moz-user-select'] = 'none';
      this.colors = {
        strings: 'gray',
        inlays: '#D1A319',
        frets: 'gray'
      };
      window.addEventListener('resize', this.repaint);
      this.canvas.addEventListener('resize', this.repaint);
      this.events.modelchange(this.repaint);
      this.canvas.addEventListener("click", this.onMouseClick);
      this.canvas.addEventListener('mousemove', this.onMouseMove);
      this.set(args);
    }

    View.prototype.set = function(args) {
      var c;
      if (args.drawInlay) {
        this.drawInlay = args.drawInlay;
      }
      if (args.drawSelector) {
        this.drawSelector = args.drawSelector;
      }
      if (args.drawString) {
        this.drawString = args.drawString;
      }
      if (args.drawFret) {
        this.drawFret = args.drawFret;
      }
      if (args.colors) {
        c = args.colors;
        if (c.strings) {
          this.colors.strings = c.strings;
        }
        if (c.frets) {
          this.colors.frets = c.frets;
        }
        if (c.inlays) {
          return this.colors.inlays = c.inlays;
        }
      }
    };

    View.prototype.relativePosition = function(e) {
      var b, canvX, canvY, docE;
      docE = document.documentElement;
      b = this.canvas.getBoundingClientRect();
      canvX = b.left - window.pageXOffset - docE.clientLeft;
      canvY = b.top - window.pageYOffset - docE.clientTop;
      return {
        x: e.pageX - canvX,
        y: e.pageY - canvY
      };
    };

    View.prototype.pinpointNote = function(x, y) {
      var fret, fretWidth, heightRatio, openFretWidth, string, _ref;
      _ref = this.fretWidths(), openFretWidth = _ref.openFretWidth, fretWidth = _ref.fretWidth;
      fret = x < openFretWidth ? 0 : Math.floor((x - openFretWidth) / fretWidth) + 1;
      heightRatio = this.height / this.model.strings;
      string = this.model.strings - Math.floor(y / heightRatio);
      return {
        string: string,
        fret: fret
      };
    };

    View.prototype.onMouseClick = function(e) {
      var fret, note, string, x, y, _ref, _ref1;
      _ref = this.relativePosition(e), x = _ref.x, y = _ref.y;
      _ref1 = this.pinpointNote(x, y), string = _ref1.string, fret = _ref1.fret;
      note = this.model.notes[fret][string - 1];
      return this.events.broadcast('noteclick', note);
    };

    View.prototype.onMouseMove = function(e) {
      var fret, string, x, y, _ref, _ref1;
      _ref = this.relativePosition(e), x = _ref.x, y = _ref.y;
      _ref1 = this.pinpointNote(x, y), fret = _ref1.fret, string = _ref1.string;
      if (!this.hoveredNote || this.hoveredNote.fret !== fret || this.hoveredNote.string !== string) {
        if (this.model.notes[fret] && this.model.notes[fret][string - 1]) {
          this.hoveredNote = this.model.notes[fret][string - 1];
          return this.events.broadcast('notehover', this.hoveredNote);
        }
      }
    };

    View.prototype.updateDimensions = function() {
      this.width = this.canvas.offsetWidth;
      this.height = this.canvas.offsetHeight;
      this.context.get('canvas').height = this.height;
      return this.context.get('canvas').width = this.width;
    };

    View.prototype.drawInlay = function(context, color, x, y, width, height) {
      return context.color(color).beginPath().moveTo(x - (width / 2), y).lineTo(x, y - (height / 2)).lineTo(x + (width / 2), y).lineTo(x, y + (height / 2)).fill();
    };

    View.prototype.drawSelector = function(context, note, x, y, radius) {
      var color;
      if (note.interval.degree) {
        color = note.interval.degree === 1 ? 'firebrick' : 'gray';
        return context.beginPath().color(color).arc(x, y, radius, 0, this.endArc).fill();
      }
    };

    View.prototype.drawString = function(context, color, width, stringY, openX) {
      return context.beginPath().lineWidth(1).fillStyle(color).moveTo(openX, stringY).lineTo(width, stringY).stroke();
    };

    View.prototype.drawFret = function(context, fret, fretStart, height, color) {
      if (fret === 1) {
        context.lineWidth(5);
      } else {
        context.lineWidth(1);
      }
      if (fret > 0) {
        return context.beginPath().fillStyle(color).moveTo(fretStart, 0).lineTo(fretStart, height).stroke();
      }
    };

    View.prototype.fretWidths = function() {
      var fretWidth, leftover, openFretWidth;
      openFretWidth = this.width / (this.model.frets * 2);
      leftover = this.width - openFretWidth;
      fretWidth = leftover / (this.model.frets - 2);
      return {
        openFretWidth: openFretWidth,
        fretWidth: fretWidth
      };
    };

    View.prototype.paint = function() {
      var circle, fretEnd, fretStart, fretWidth, heightRatio, openFretWidth, radius, stringH, _ref;
      _ref = this.fretWidths(), openFretWidth = _ref.openFretWidth, fretWidth = _ref.fretWidth;
      heightRatio = this.height / this.model.strings;
      radius = heightRatio > openFretWidth ? openFretWidth / 4 : heightRatio / 4;
      stringH = 0;
      fretStart = 0;
      fretEnd = 0;
      circle = 0;
      return this.model.forEach((function(_this) {
        return function(note, fret, string) {
          var inlayX, stringInvert, stringY;
          fretStart = !fret ? 1 : ((fret - 1) * fretWidth) + openFretWidth;
          fretEnd = fretStart + fretWidth - 1;
          stringInvert = _this.model.strings - string + 1;
          stringY = ((stringInvert - 1) * heightRatio) + (heightRatio / 2);
          inlayX = fretStart + ((!fret ? openFretWidth : fretWidth) / 2);
          if (fret === 0) {
            _this.drawString(_this.context, _this.colors.strings, _this.width, stringY, openFretWidth);
          }
          if (string === 1) {
            _this.drawFret(_this.context, fret, fretStart, _this.height, _this.colors.frets);
            switch (fret) {
              case 3:
              case 5:
              case 7:
              case 9:
                _this.drawInlay(_this.context, _this.colors.inlays, inlayX, _this.height / 2, radius * 3, radius * 6);
                break;
              case 12:
                _this.drawInlay(_this.context, _this.colors.inlays, inlayX, _this.height / 3, radius * 3, radius * 6);
                _this.drawInlay(_this.context, _this.colors.inlays, inlayX, 2 * (_this.height / 3), radius * 3, radius * 6);
            }
          }
          return _this.drawSelector(_this.context, note, inlayX, stringY, radius);
        };
      })(this));
    };

    View.prototype.repaint = function() {
      this.updateDimensions();
      this.context.begin().clearRect(0, 0, this.width, this.height).fill();
      return this.paint();
    };

    return View;

  })();

  if (Fingerboard.View == null) {
    Fingerboard.View = {};
  }

  endArc = Math.PI * 2;

  Fingerboard.View.selectors = {
    "default": function(_arg) {
      var color, elseColor, ratio, tonicColor;
      tonicColor = _arg.tonicColor, elseColor = _arg.elseColor, ratio = _arg.ratio;
      if (ratio == null) {
        ratio = 1;
      }
      color = void 0;
      if (tonicColor == null) {
        tonicColor = 'firebrick';
      }
      if (elseColor == null) {
        elseColor = 'gray';
      }
      return function(context, note, x, y, radius) {
        if (note.interval.degree) {
          color = note.interval.degree === 1 ? tonicColor : elseColor;
          return context.begin().color(color).arc(x, y, radius * ratio, 0, endArc).fill();
        }
      };
    },
    notationDots: function(_arg) {
      var color, elseColor, font, ratio, text, textColor, tonicColor, withIndex;
      tonicColor = _arg.tonicColor, elseColor = _arg.elseColor, textColor = _arg.textColor, withIndex = _arg.withIndex, ratio = _arg.ratio, font = _arg.font;
      if (ratio == null) {
        ratio = 1.4;
      }
      if (font == null) {
        font = '600 9px tahoma';
      }
      if (tonicColor == null) {
        tonicColor = 'fireBrick';
      }
      if (elseColor == null) {
        elseColor = 'gray';
      }
      if (textColor == null) {
        textColor = 'white';
      }
      if (withIndex == null) {
        withIndex = true;
      }
      color = void 0;
      text = void 0;
      return function(context, note, x, y, radius) {
        if (note.interval.degree) {
          color = note.interval.degree === 1 ? tonicColor : elseColor;
          context.begin().color(color).arc(x, y, radius * ratio, 0, endArc).fill();
          text = withIndex ? note.interval.notation + note.interval.index : note.interval.notation;
          return context.begin().color(textColor).textAlign('center').textBaseline('middle').font(font).fillText(text, x, y);
        }
      };
    },
    degreeDots: function(_arg) {
      var color, elseColor, font, ratio, textColor, tonicColor;
      tonicColor = _arg.tonicColor, elseColor = _arg.elseColor, textColor = _arg.textColor, ratio = _arg.ratio, font = _arg.font;
      if (ratio == null) {
        ratio = 1.2;
      }
      if (font == null) {
        font = '600 9px tahoma';
      }
      if (tonicColor == null) {
        tonicColor = 'fireBrick';
      }
      if (elseColor == null) {
        elseColor = 'gray';
      }
      if (textColor == null) {
        textColor = 'white';
      }
      color = void 0;
      return function(context, note, x, y, radius) {
        if (note.interval.degree) {
          color = note.interval.degree === 1 ? tonicColor : elseColor;
          context.begin().color(color).arc(x, y, radius * ratio, 0, endArc).fill();
          return context.begin().color(textColor).textAlign('center').textBaseline('middle').font(font).fillText('' + note.interval.degree, x, y);
        }
      };
    }
  };

}).call(this);

//# sourceMappingURL=fingerboard.js.map
