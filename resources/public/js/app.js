

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


	var gameState = {};

	this.startGame = function(name, callback) {

		console.log('name '+name);

		$http.get(
			'/mog/startGame',
			{
				params: {
					name: name				
				}
			}
		)
		.success(function(data, status, headers, config) {
			console.log('success');
			console.log(data);

			gameState.gameId = data.gameId;

			gameState.player = {};
			gameState.player.name = data.player.name;
			gameState.player.hp = data.player.hp;
			gameState.player.score = data.player.score;

			callback(data);

		})
		.error(function(data, status, headers, config) {
			console.log('error');
			console.log(data);
			console.log(status);
			console.log(headers);
			console.log(config);
		});
	};


	this.nextRound = function(gameId, callback) {

		console.log('gameId '+gameId);

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

			gameState.player = {};
			gameState.player.name = data.player.name;
			gameState.player.hp = data.player.hp;
			gameState.player.score = data.player.score;

			gameState.monster = {};
			gameState.monster.name = data.monster.name;
			gameState.monster.hp = data.monster.hp;

			gameState.letters = data.letters;
			gameState.highScore = data.highScore;
			gameState.initiative = data.initiative;

			gameState.info = data.info;

			callback(data);

		})
		.error(function(data, status, headers, config) {
			console.log('error');
			console.log(data);
			console.log(status);
			console.log(headers);
			console.log(config);
		});
	};

	this.playerAttack = function(gameId, word, callback) {
		
		console.log('gameId '+gameId);

		$http.get(
			'/mog/playerAttack',
			{
				params: {
					gameId: gameId,
					word: word
				}
			}
		)
		.success(function(data, status, headers, config) {
			console.log('success');
			console.log(data);

			if(data.error) {
				gameState.error = data.error;
			}
			else {

	//			gameState.player = {};
	//			gameState.player.name = data.player.name;
	//			gameState.player.hp = data.player.hp;
				gameState.player.score = data.player.score;
	//
	//			gameState.monster = {};
	//			gameState.monster.name = data.monster.name;
				gameState.monster.hp = data.monster.hp;
	//
				gameState.letters = data.letters;
				gameState.highScore = data.highScore;
	//			gameState.initiative = data.initiative;
	//
				gameState.info = data.info;
	//
			}
			callback(data);

		})
		.error(function(data, status, headers, config) {
			console.log('error');
			console.log(data);
			console.log(status);
			console.log(headers);
			console.log(config);
		});

	};

	this.getGameId = function() {
		return gameState.gameId;
	};

	this.getPlayer = function() {
		return gameState.player;
	};

	this.getMonster = function() {
		return gameState.monster;
	};

	this.getLetters = function() {
		return gameState.letters;
	};

	this.getInfo = function() {
		return gameState.info;
	};

})
 
.controller('StartController', function($scope, $location, mogService) {
     
	$scope.startGame = function(event) {

		console.log("startGame event");

		if($scope.player && $scope.player.name) {
			console.log('Hi '+$scope.player.name);

			mogService.startGame($scope.player.name,

				function(data) {
					console.log('startGame callback');

					mogService.nextRound(mogService.getGameId(), 

						function(data, status, headers, config) {
							console.log('nextRound callback');

							$location.path("/game");
						});

				});

		}
	};
})
 
.controller('GameController', function($scope, mogService) {

	$scope.player = mogService.getPlayer();
	$scope.monster = mogService.getMonster();
	$scope.letters = mogService.getLetters();
	$scope.info = mogService.getInfo();
 
	$scope.playerAttack = function(event) {
		console.log("playerAttack event");

		if($scope.player && $scope.player.word) {
			console.log('word '+$scope.player.word);

			mogService.playerAttack(mogService.getGameId(), $scope.player.word, 
				function(data) {
					console.log('playerAttack callback');

					console.log(data);

					if(data.error) {
						$scope.error = data.error;
					}
					else {
						$scope.error = '';
						$scope.player = mogService.getPlayer();
						$scope.monster = mogService.getMonster();
						$scope.letters = mogService.getLetters();
						$scope.info = mogService.getInfo();
					}

				});
		}
	};

});


