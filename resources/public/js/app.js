

var menagerieApp = angular.module('menagerieApp', ['ngRoute'])
 

.config(['$routeProvider',
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
}])

.service('mogService', function() {

	var player = {
		hp:50,
		score: 100
	};

	var monster = {
		name: 'Satan',
		hp: 1000
	};

	var letters = 'anxmvcmdhrutuueeeldf';

	this.startGame = function(name) {
		player.name = name;
	};

	this.getPlayer = function() {
		return player;
	};

	this.getMonster = function() {
		return monster;
	};

	this.getLetters = function() {
		return letters;
	};

})
 
.controller('StartController', function($scope, $location, mogService) {
     
	$scope.startGame = function(event) {

		console.log(event);
		if($scope.player && $scope.player.name) {
			console.log('Hi '+$scope.player.name);

			console.log(mogService);

			mogService.startGame($scope.player.name);

			$location.path("/game");

//			 $location.search('name','Freewind').path('/game');
		}
	};
})
 
.controller('GameController', function($scope, mogService) {

	$scope.player = mogService.getPlayer();
	$scope.monster = mogService.getMonster();
	$scope.letters = mogService.getLetters();
 
});


