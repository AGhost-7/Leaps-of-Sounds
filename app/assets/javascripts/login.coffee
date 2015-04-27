

navbar = angular.module('navbar',[])
.controller('MainController', ['$scope', 'username', ($scope, username) ->
	$scope.login = ->
		alert('login! ' + $scope.username + " " + $scope.password)
])
.factory('username', ->
	value = undefined
	listeners = []
	
	listen: (l) ->
		listeners.push(l)
	get: ->
		value
	set: (v) ->
		value = v
		for l in listeners
			l(v)
)
.directive('inner-nav', ['$compile', '$http', '$templateCache', 'username',
	($compile, $http, $templateCache, username) ->
		console.log('directive')
		template = (loggedIn) ->
			url =
				if loggedIn then '/assets/tpml/nav-logged.html'
				else '/assets/tmpl/nav-login.html'
			$http.get(url, {cache: $templateCache})
		scope =
			loggedIn: false
			username: undefined
			login: ->
				alert('login! ' + $scope.username + " " + $scope.password)
		restrict: 'E'
		scope: scope
		link: (scope, element, attrs) ->

			template(scope.loggedIn).success (html) ->
				console.log('loaded!')
				element.html($compile(html)(scope))

])
