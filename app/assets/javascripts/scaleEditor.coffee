do (server = jsRoutes.controllers) ->

	angular
	.module('scaleEditor', [])
	.filter('interval', ->
		(input) ->
			input
				.split(",")
				.sort (a, b) -> a - b
				.map (e) -> CONST.intervals[e]
				.join(" - ")
	)
	.controller('ScaleCtrl', ['$scope', '$http', ($scope, $http) ->

		emptyErr = ->
			#Clear the errors from the view
			$scope.errorValues = undefined
			$scope.errorName = undefined
			$scope.errorGlobal = undefined

		emptyForm = ->
			$scope.selectedIntervals = [ $scope.intervals[0] ]
			$scope.scaleName = ''
			$scope.scaleId = undefined

		angular.extend($scope,
			mode: 'add'
			editingScale: []
			intervals: [
				'',
				'1', '2b', '2',
				'b3', '3', '4',
				'b5', '5', 'b6',
				'6', 'b7', '7'
			].map (e, i) -> id: i, name: e
			scales: []
			deleteScale: (id) ->
				server.Scales.remove(id).ajax(
					success: (data) ->
						index = $scope.scales.reduce((r, e, i) ->
							if e.id == id then i else r
						, undefined)
						$scope.scales.splice(index, 1)
						$scope.$digest()
					error: ({responseJSON: data}) ->
						alert(data.message)
				)

			resetForm: ->
				emptyForm()
				emptyErr()
				$scope.mode = 'add'

			modifyScale: (scale) ->
				$scope.selectedIntervals = scale
					.values
					.split(',')
					.map (e) -> $scope.intervals[e]
				$scope.scaleId = scale.id
				$scope.scaleName = scale.name
				$scope.mode = 'update'

			onFormError: ({responseJSON: {message, fieldName, fieldValue}}) ->
				if fieldName == "values" then $scope.errorValues = message
				else if fieldName == "name" then $scope.errorName = message
				else $scope.errorGlobal = message
				$scope.$digest()

			formatedInterval: ->
				$scope.selectedIntervals.reduce((accu, e, i, arr) ->
					if e.id == 0 then accu else accu.concat(e.id)
				,[]).join(',')

			addClicked: ->
				emptyErr()
				server.Scales.insert($scope.scaleName, @formatedInterval()).ajax(
					success: (data) ->
						$scope.scales.push(data)
						emptyForm()
						$scope.$digest()
					error: @onFormError
				)

			updateClicked: ->
				emptyErr()
				server.Scales.update($scope.scaleId, $scope.scaleName, @formatedInterval()).ajax(
					success: (data) ->
						index = $scope.scales.reduce((r, e, i, a) ->
							if e.id == $scope.scaleId then i else r
						, undefined)

						$scope.scales[index].values = $scope.formatedInterval()
						$scope.scales[index].name = $scope.scaleName

						$scope.resetForm()

						$scope.$digest()

					error: @onFormError
				)


			memberSelected: (selected, index) ->
				if selected.id != 0
					if $scope.selectedIntervals[index].id == 0
						$scope.selectedIntervals[index] = selected
						$scope.selectedIntervals.push($scope.intervals[0])
					else
						$scope.selectedIntervals[index] = selected
				else if selected.id == 0 && $scope.selectedIntervals.length - 1 != index
					$scope.selectedIntervals.splice(index, 1)

		)

		emptyForm()
		emptyErr()

		server.Scales.list().ajax(
			success: (data) ->
				$scope.scales = data
				$scope.$digest()
			error: ({responseJSON}) ->
				alert(responseJSON.message)
		)

	])
