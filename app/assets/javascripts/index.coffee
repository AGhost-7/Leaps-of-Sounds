# convenient alias
server = jsRoutes.controllers

$canvas = $('#fingerboard');
$canvas.css('width', '100%');

angular
.module('index', [])
.filter('userDefined', ->
  (input, isDefined) ->
    isDefined ?= true
    if isDefined then input.filter (e) -> e.user != undefined
    else input.filter (e) -> e.user == undefined
)
.controller('MainCtrl', ['$scope', ($scope) ->

  fb = undefined

  angular.extend($scope,
    instruments: []
    scales: []
    tunings: []
    roots: [
      "C", "Db", "D"
      "Eb", "E", "F"
      "Gb", "G", "Ab"
      "A", "Bb", "B"
    ]

    instrumentSelected: (instrument) ->
      server.Tunings.ofInstrument(instrument.id).ajax(
        success: (data) ->
          $scope.tunings = data
          $scope.userTunings =
            if instrument.user == undefined
              data.some (e) -> e.user != undefined
            else
              false
          $scope.$digest()
          fb.set(
            model:
              strings: instrument.strings
              tuning: data[0].values
          )

        error: ->
          alert('Error communicating with the server.')
      )
    tuningSelected: (tuning) ->
      fb.set(model: tuning: tuning.values)
    scaleSelected: (scale) ->
      fb.set(model: scale: values: scale.values)
    rootSelected: (root) ->
      fb.set(model: scale: root: root)
  )

  angular.forEach({instruments: 'instrumentsTitles', scales: 'scalesTitles'},
    (fieldToUpdate, watch) ->
      $scope.$watch(watch, (ls) ->
        $scope[fieldToUpdate] = ls.some (e) -> !!e.user
      )
  )

  $scope.$watch('tunings', (tunings) ->
    if(tunings.every (e) -> e.user != undefined)
      $scope.tuningsTitles = false
    else
      $scope.tuningsTitles = tunings.some (e) -> e.user != undefined
  )

  server.Application.indexJson().ajax(
    success: (data) ->
      angular.extend($scope, data)
      fb = new Fingerboard($canvas[0],
        model:
          strings: data.instruments[0].strings
          frets: 14
          tuning: data.tunings[0].values
          scale:
            values: data.scales[0].values
            root: 1
      )
      fb.notehover (note) ->
        $scope.noteHovered = note
        $scope.$digest()

      $scope.$digest()
    error: ({responseJson: js}) ->
      alert('There was an error loading application resources.')

  )
])
