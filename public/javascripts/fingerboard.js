(function() {
  var Fingerboard;

  Fingerboard = window.Fingerboard;

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

}).call(this);

//# sourceMappingURL=fingerboard.js.map
