(function() {
  var ContextWrapper, Fingerboard, Interval, Model, Note, PublicInterface, Square;

  Fingerboard = (function() {
    function Fingerboard($canvas, args) {
      var events, key, model, view;
      events = (function() {
        var key, listeners, self;
        listeners = {
          noteclick: [],
          notehover: [],
          modelchange: []
        };
        self = {
          broadcast: function(event, callback) {
            var listener, _i, _len, _ref, _results;
            _ref = listeners[event];
            _results = [];
            for (_i = 0, _len = _ref.length; _i < _len; _i++) {
              listener = _ref[_i];
              _results.push(listener(callback()));
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
      })();
      for (key in events) {
        this[key] = events[key];
      }
      model = new Model(args, events);
      view = new View(args, $canvas, model, events);
      this.tuning = function() {};
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
      context.fillStyle = col;
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
    var asJSArray;

    function Model(args) {
      this.notes = [[]];
      this.notation = ['C', 'Db', 'D', 'Eb', 'E', 'F', 'Gb', 'G', 'Ab', 'A', 'Bb', 'B'];
      this.selectors = {};
      this.root = 1;
      this.tuning = [28, 33, 38, 43, 47, 52];
      this.scale = void 0;
      this.scaleLength = this.notation.length;
      this.strings = this.tuning.length;
      this.frets = 16;
      this.set(args);
    }

    Model.prototype.fill = function() {
      var fret, string, _i, _ref, _results;
      this.notes = [];
      _results = [];
      for (fret = _i = 0, _ref = this.frets; 0 <= _ref ? _i <= _ref : _i >= _ref; fret = 0 <= _ref ? ++_i : --_i) {
        notes[fret] = [];
        _results.push((function() {
          var _j, _ref1, _results1;
          _results1 = [];
          for (string = _j = 1, _ref1 = this.strings; 1 <= _ref1 ? _j <= _ref1 : _j >= _ref1; string = 1 <= _ref1 ? ++_j : --_j) {
            _results1.push(notes[fret][string - 1] = new Note(fret, string));
          }
          return _results1;
        }).call(this));
      }
      return _results;
    };

    Model.prototype.buildInterval = function() {
      var index, intervalValue, intervals, _i, _ref, _results;
      if (this.tuning.length !== this.strings) {
        throw 'Tuning is invalid for the number of strings given.';
      }
      intervalValue = -1;
      index = 0;
      intervals = (function() {
        _results = [];
        for (var _i = 0, _ref = this.scaleLength * (this.maxIndex + 1); 0 <= _ref ? _i <= _ref : _i >= _ref; 0 <= _ref ? _i++ : _i--){ _results.push(_i); }
        return _results;
      }).apply(this).map(function(i) {
        if (intervalValue >= this.scaleLength) {
          intervalValue = 1;
          index++;
        } else {
          intervalValue++;
        }
        return {
          value: intervalValue,
          index: index,
          freqId: i + 1,
          notation: this.notation[intervalValue - 1]
        };
      });
      return this.forEach(function(note, fret, string) {
        return note.interval = intervals[this.tuning[string - 1] + fret];
      });
    };

    Model.prototype.buildRootedValue = function() {
      return this.forEach(function(note, fret, string) {
        note.interval.shift = note.interval.value - this.root + 1;
        if (note.interval.shift < 1) {
          return note.interval.shift += scaleLength;
        }
      });
    };

    Model.prototype.buildScale = function() {
      var degree, sc;
      sc = void 0;
      degree = void 0;
      return this.forEach(function(note, fret, string) {
        if (degree = scale[note.interval.shift]) {
          return note.interval.degree = degree;
        } else {
          return note.interval.degree = void 0;
        }
      });
    };

    Model.prototype.set = function(args) {
      var a;
      if (args === void 0) {
        throw 'Oye, forgot something? I need an options object.';
      }
      if (args.strings !== void 0) {
        this.strings = args.strings;
      }
      if (args.frets !== void 0) {
        this.frets = args.frets;
      }
      if (args.strings !== void 0 || args.frets !== void 0) {
        this.fill();
      }
      if (args.interval !== void 0) {
        a = interval;
        if (a.notation !== void 0) {
          this.notation = a.notation;
          this.scaleLength = notation.length;
        }
        if (a.maxIndex !== void 0) {
          this.maxIndex = a.maxIndex;
        }
        if (a.tuning !== void 0) {
          this.tuning = a.tuning;
        }
        this.buildInterval();
      } else if (this.notes[0][0].interval.value === -1) {
        this.buildInterval();
      }
      if (args.scale !== void 0) {
        a = args.scale;
        if (a.scale !== void 0) {
          this.scale = a.scale;
        }
        if (a.root !== void 0) {
          this.root = a.root;
        }
        return buildScale();
      } else if (this.scale !== void 0 && this.root !== void 0) {
        return buildScale();
      }
    };


    /*
    	 * private static functions
     */

    asJSArray = function(arr) {
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
        }
      } else {
        return arr;
      }
    };


    /*	
    	 * Traversing functions
     */

    Model.prototype.forEach = function(traversor) {
      var fret, fretArr, note, string, _i, _j, _len, _len1;
      for (fret = _i = 0, _len = notes.length; _i < _len; fret = ++_i) {
        fretArr = notes[fret];
        for (string = _j = 0, _len1 = fretArr.length; _j < _len1; string = ++_j) {
          note = fretArr[string];
          if (traversor(note, fret, string) === false) {
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

  PublicInterface = (function() {
    function PublicInterface(events, obj, keys) {
      var args, key, _i, _len;
      args = {};
      for (_i = 0, _len = keys.length; _i < _len; _i++) {
        key = keys[_i];
        if (typeof key === 'object') {
          args[key.name] = key;
        } else {
          args[key] = {
            enumerable: true,
            get: function() {
              return S[key];
            },
            set: function(val) {
              S[key] = val;
              return events.broadcast('modelchange', function() {
                return {
                  name: key,
                  value: val
                };
              });
            }
          };
        }
      }
      Object.defineProperties(this, args);
    }

    return PublicInterface;

  })();

  Interval = (function() {
    function Interval() {
      this.freqId = -1;
      this.index = -1;
      this.value = -1;
      this.notation = '';
      this.shift = -1;
      this.degree = '';
    }

    Interval.prototype["public"] = function(events) {
      if (this.__public__ === void 0) {
        this.__public__ = new PublicInterface(events, this, ['freqId', 'index', 'notation', 'value', 'shift', 'degree']);
      }
      return this.__public__;
    };

    return Interval;

  })();

  Square = (function() {
    function Square(x1, y1, x2, y2) {
      this.x1 = x1;
      this.y1 = y1;
      this.x2 = x2;
      this.y2 = y2;
    }

    Square.prototype.isPointWithinBounds = function(x, y) {
      return x > this.x1 && x < this.x2 && this.y1;
    };

    Square.prototype["public"] = function(events) {
      if (this.__public__ === void 0) {
        this.__public__ = new PublicInterface(events, this, ['x1', 'y1', 'x2', 'y2']);
      }
      return this.__public__;
    };

    return Square;

  })();

  Note = (function() {
    function Note(fret, string) {
      this.fret = fret;
      this.string = string;
      this.selector = '';
      this.dimension = new Square();
      this.interval = new Interval();
    }

    Note.prototype["public"] = function(events) {
      if (this.__public__ === void 0) {
        this.__public__ = new PublicInterface(events, this, [
          'frets', 'strings', {
            name: 'dimension',
            enumerable: true,
            writable: false,
            value: this.dimension["public"](events)
          }, {
            name: 'interval',
            enumerable: true,
            writable: false,
            value: this.interval["public"](events)
          }, 'selector'
        ]);
        this.__public__.toString = function() {
          return JSON.stringify(this);
        };
      }
      return this.__public__;
    };

    return Note;

  })();

}).call(this);

//# sourceMappingURL=fingerboard.js.map
