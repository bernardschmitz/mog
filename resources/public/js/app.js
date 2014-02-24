

var menagerieApp = angular.module('menagerieApp', ['ngRoute']);
 

menagerieApp.config(['$routeProvider',
  function($routeProvider) {
    $routeProvider.
      when('/start', {
        templateUrl: 'start.html',
        controller: 'StartController'
    }).
      when('/game', {
        templateUrl: 'game.html',
        controller: 'GameController'
      }).
      otherwise({
        redirectTo: '/start'
      });
}]);
 
 
menagerieApp.controller('StartController', function($scope) {
     
    $scope.message = 'This is Add new order screen';
});
 
 
menagerieApp.controller('GameController', function($scope) {
 
    $scope.message = 'This is Show orders screen';
});


