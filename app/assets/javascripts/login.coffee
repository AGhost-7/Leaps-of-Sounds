
navbar = angular.module('navbar', ->
  console.log 'hello!'
)
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

    template = (loggedIn) ->
      url =
        if loggedIn then '/assets/tpml/nav-logged.html'
        else '/assets/tmpl/nav-login.html'
      $http.get(url, {cache: $templateCache})

    restrict: 'E'
    scope:
      loggedIn: false
      username: undefined
      login: ->
        alert('login! ' + $scope.username + " " + $scope.password)

    link: (scope, element, attrs) ->
      template(scope.loggedIn).success (html) ->
        element.html($compile(html)(scope))

])
