//package bots;

import java.util.*;

import pirates.*;

public class MyBot implements PirateBot {

	/*
	 * TODO:
	 * 1. add commend to the code
	 * 2. find which island is the closest to my city
	 * 4. find which city is the closest
	 */
		
	/*
	 * Class for the move list
	 */
	public class PirateAndDestination {
		Pirate p;
		Location d;
		int dist;

		public PirateAndDestination(Pirate p, Location d, int dist) {
			super();
			this.p = p;
			this.d = d;
			this.dist = dist;
		}

	}

	Random rnd = new Random();

	boolean lose = false;

	public void doTurn(PirateGame game) {

		List<PirateAndDestination> myList = setList(game);
		PirateAndDestination thisMove;

		sortList(myList);

		// if the list isn't empty
		while (myList.size() > 0) {
			// get the closet destination
			thisMove = myList.get(0);
			// if the pirate can attack
			if (tryAttack(thisMove.p, game) == false) {
				List<Location> sailOptions = game.getSailOptions(thisMove.p, thisMove.d);
				// sail to the destination
				if (sailOptions.size() > 1) {
					game.setSail(thisMove.p, sailOptions.get(1));
				} else {
					game.setSail(thisMove.p, sailOptions.get(0));
				}
			}
			// remove all the object that have the pirate or the location
			myList = updateList(myList, thisMove.d, thisMove.p, game);
		}

		droneMove(game);

	}

	/*
	 * This function will sort the list Input: the list Output: none
	 */
	public void sortList(List<PirateAndDestination> destList) {
		PirateAndDestination temp;
		for (int i = 0; i < destList.size(); i++) {
			for (int j = i + 1; j < destList.size() - 1; j++) {
				if (destList.get(i).dist > destList.get(j).dist) {
					temp = destList.get(i);
					destList.set(i, destList.get(j));
					destList.set(j, temp);
				}
			}
		}
	}

	/*
	 * This function will create the move list (not sorted) Input: game(for
	 * debug) Outpt: the list
	 */
	public List<PirateAndDestination> setList(PirateGame game) {

		List<PirateAndDestination> destList = new ArrayList<PirateAndDestination>();

		List<Pirate> myPirate = game.getMyLivingPirates();

		int mul = 1;
		int dev = 1;
		List<Pirate> enemyPirates = game.getEnemyLivingPirates();
		List<Island> islandList = game.getNeutralIslands();
		List<Drone> droneList = game.getEnemyLivingDrones();
		List<Island> enemyIsland = game.getEnemyIslands();

		for (int i = 0; i < myPirate.size(); i++) {

			if (enemyIsland.size() > 2 && lose == false) {
				mul = 9;
				dev = 24;
			}

			if (game.getMyScore() < game.getEnemyScore()) {
				lose = true;
				mul = 25;
				dev = 48;
			}

			for (int j = 0; j < enemyPirates.size(); j++) {
				destList.add(new PirateAndDestination(myPirate.get(i), enemyPirates.get(j).location,
						myPirate.get(i).distance(enemyPirates.get(j))));
			}

			for (int k = 0; k < islandList.size(); k++) {
				destList.add(new PirateAndDestination(myPirate.get(i), islandList.get(k).location,
						myPirate.get(i).distance(islandList.get(k)) * mul / dev));
			}

			for (int k = 0; k < droneList.size(); k++) {
				destList.add(new PirateAndDestination(myPirate.get(i), droneList.get(k).location,
						myPirate.get(i).distance(droneList.get(k))));
			}
			for (int j = 0; j < enemyIsland.size(); j++) {

				if (game.getEnemyIslands().size() > 0)
				{
					if (enemyIsland.get(j).id == findClosestEnemyIslandToEnemyCity(game).id) {
						mul = 100;
						dev = 105;
					}
				}
				destList.add(new PirateAndDestination(myPirate.get(i), enemyIsland.get(j).location,
						myPirate.get(i).distance(enemyIsland.get(j)) * mul / dev));
			}
		}

		return destList;
	}

	private List<PirateAndDestination> updateList(List<PirateAndDestination> destList, Location loc, Pirate p,
			PirateGame game) {
		PirateAndDestination pirateDest;
		for (int i = 0; i < destList.size(); i++) {
			pirateDest = destList.get(i);
			if (pirateDest.d == loc || pirateDest.p == p) {
				destList.remove(i);
				i = i - 1;
			}
		}

		return destList;
	}
	
	private Island findClosestEnemyIslandToEnemyCity(PirateGame game)
	{

		Island closest = game.getAllIslands().get(0);
		City myCity = game.getEnemyCities().get(0);
		
		for (Island thisIsland : game.getAllIslands()) 
		{
			if(myCity.distance(thisIsland) < myCity.distance(closest))
			{
				closest = thisIsland;
			}
		}
		
		
		return closest;
	}

	private boolean tryAttack(Pirate pirate, PirateGame game) {
		// Go over all enemies
		for (Aircraft enemy : game.getEnemyLivingAircrafts()) {
			// Check if the enemy is in attack range
			if (pirate.inAttackRange(enemy)) {
				// Fire!
				game.attack(pirate, enemy);
				// Print a message
				return true;
			}
		}
		return false;
	}

	private void droneMove(PirateGame game) {
		int choice = 0;
		int counter = 0;
		boolean flag = false;
		// Go over all of my drones
		for (Drone drone : game.getMyLivingDrones()) {
			choice = 0;
			flag = false;
			City destination = game.getMyCities().get(0);
			List<Location> sailOptions = game.getSailOptions(drone, destination);
			if (sailOptions.size() > 1) {
				for (Pirate pirate : game.getEnemyLivingPirates()) {
					if (drone.location.row == pirate.location.row) {
						choice = 0;
						flag = true;
					}

					if (drone.location.col == pirate.location.col) {
						choice = 1;
						flag = true;
					}

					if ((destination.location.row == pirate.location.row
							|| destination.location.row == pirate.location.row + 1
							|| destination.location.row == pirate.location.row - 1) && flag == false
							&& destination.location.col >= pirate.location.col - 2) {
						if (destination.inRange(pirate, 5)) {
							flag = true;
						}
						choice = 1;
					}
					if ((destination.location.col == pirate.location.col
							|| destination.location.col == pirate.location.col + 1
							|| destination.location.col == pirate.location.col - 1) && flag == false
							&& destination.location.row >= pirate.location.row - 2) {
						if (destination.inRange(pirate, 5)) {
							flag = true;
						}
						choice = 0;
					}
				}

			}
			game.setSail(drone, sailOptions.get(choice));

		}
	}

}
