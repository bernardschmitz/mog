

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

.service('mogService', function($http) {

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

		$http.get(
			'/mog/startGame',
			{
				params: {
					name: player.name				
				}
			}
		)
		.success(function(data, status, headers, config) {
			console.log('success');
			console.log(data);
			console.log(status);
			console.log(headers);
			console.log(config);

				

		})
		.error(function(data, status, headers, config) {
			console.log('error');
			console.log(data);
			console.log(status);
			console.log(headers);
			console.log(config);
		});
	};


	this.nextRound = function(gameId) {

		$http.get(
			'/mog/nextRound',
			{
				params: {
					gameId: gameId
				}
			}
		)
		.success(function(data, status, headers, config) {
			console.log('success');
			console.log(data);
			console.log(status);
			console.log(headers);
			console.log(config);
		})
		.error(function(data, status, headers, config) {
			console.log('error');
			console.log(data);
			console.log(status);
			console.log(headers);
			console.log(config);
		});
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
		}
	};
})
 
.controller('GameController', function($scope, mogService) {

	$scope.player = mogService.getPlayer();
	$scope.monster = mogService.getMonster();
	$scope.letters = mogService.getLetters();
 
});


