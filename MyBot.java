package bots;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pirates.*;


public class MyBot implements PirateBot {

	public class PossibleMove{
		private Aircraft aircraft;
		private GameObject destAircraft;
		private Location destination;
		private int distance;
		
		public PossibleMove(Aircraft aircraft, GameObject destAircraft, Location destination, int distance) {
			this.aircraft = aircraft;
			this.destAircraft = destAircraft;
			this.destination = destination;
			this.distance = distance;
		}
		
		
	}
	
	public void doTurn(PirateGame game) 
	{
		PossibleMove thisMove;
		List<PossibleMove> possibleMoves = allPossibleMoves(game);
		sortList(possibleMoves);
		
		while(!possibleMoves.isEmpty())
		{
			thisMove = possibleMoves.get(0);
			
			if(!tryAttack((Pirate)thisMove.aircraft, game))
			{
				List<Location> sailOptions = game.getSailOptions(thisMove.aircraft, thisMove.destination);
				if(sailOptions.size() > 1)
				{
					game.setSail(thisMove.aircraft, sailOptions.get(1));
				}
				else
				{
					game.setSail(thisMove.aircraft, sailOptions.get(0));
				}
			}
			
			updateList(possibleMoves, thisMove);
		}
		
		dronesMove(game);
	}
		
	/*
	 * This function will setup the list with all the possible moves 
	 * Input: PirateGame object
	 * Output: list with all the possible moves
	 */
	private List<PossibleMove> allPossibleMoves(PirateGame game)
	{
		List<PossibleMove> allPossibleMovesList =  new ArrayList<PossibleMove>();
		
		List<Pirate> myPirates = game.getMyLivingPirates();
		
		List<Aircraft> enemyAircrafts = game.getEnemyLivingAircrafts();
		List<Island> islands = game.getNotMyIslands();
		
		for (Pirate pirate : myPirates) {
			
			for (Aircraft aircraft : enemyAircrafts) {
				allPossibleMovesList.add(new PossibleMove(pirate, aircraft, aircraft.location, pirate.distance(aircraft)));
			}
			
			for (Island island : islands) {
				allPossibleMovesList.add(new PossibleMove(pirate, island, island.location, pirate.distance(island)));
			}
			
		}	
		
		return allPossibleMovesList;
	}
	
	/*
	 * This function will sort the possible moves list
	 * Input: the list with the possible moves
	 * Output: none
	 */
	private void sortList(List<PossibleMove> allPossibleMoves) {
		PossibleMove temp;
		for (int i = 0; i < allPossibleMoves.size(); i++) {
			for (int j = i + 1; j < allPossibleMoves.size() - 1; j++) {
				if (allPossibleMoves.get(i).distance > allPossibleMoves.get(j).distance) {
					temp = allPossibleMoves.get(i);
					allPossibleMoves.set(i, allPossibleMoves.get(j));
					allPossibleMoves.set(j, temp);
				}
			}
		}
	}
	
	/*
	 * This function will remove moves from the list
	 * Input: all possible moves and this move
	 * Output: none
	 */
	private void updateList(List<PossibleMove> allPossibleMoves, PossibleMove thisMove)
	{
		PossibleMove currMove;
		
		for (int i = 0; i < allPossibleMoves.size(); i++) {
			currMove = allPossibleMoves.get(i);
			if (thisMove.aircraft == currMove.aircraft || thisMove.destination == currMove.destination)
			{
				allPossibleMoves.remove(i);
				i--;
			}
		}
		
	}
	
	/*
	 * This function will check if pirate can attack enemy aircraft
	 * Input: pirate and PirateGame object
	 * Output: if the pirate attack
	 */
	private boolean tryAttack(Pirate pirate, PirateGame game) {
		for (Aircraft enemy : game.getEnemyLivingAircrafts()) {
			if (pirate.inAttackRange(enemy)) {
				game.attack(pirate, enemy);
				return true;
			}
		}
		return false;
	}
	
	private List<PossibleMove> setDronesMovesList(PirateGame game)
	{
		List<PossibleMove> allDronesMoves = new ArrayList<PossibleMove>();
		List<Drone> myDrone = game.getMyLivingDrones();
		
		for (Drone drone1 : myDrone) {
			for (Drone drone2 : myDrone) {
				if (drone1.location != drone2.location)
				{
					allDronesMoves.add(new PossibleMove(drone1, drone2, drone2.location, drone1.distance(drone2)));
				}
			}
		}
		
		return allDronesMoves;
	}
	
	private void updateDroneList(List<PossibleMove> allDronesPossibleMoves, PossibleMove move, PirateGame game)
	{
		PossibleMove currMove;
		
		for (int i = 0; i < allDronesPossibleMoves.size(); i++) {
			currMove = allDronesPossibleMoves.get(i);
			if (move.aircraft == currMove.aircraft || move.destAircraft == currMove.aircraft)
			{
				allDronesPossibleMoves.remove(i);
				i--;
			}
		}
	}
	
	
	private boolean isCityRight(PirateGame game){
		
		List<City> myCityList = game.getMyCities();
		List<City> enemyCityList = game.getEnemyCities();

		if (myCityList.isEmpty() || enemyCityList.isEmpty())
		{
			return false;
		}
		boolean ret = true;
		if(myCityList.get(0).location.col>enemyCityList.get(0).location.col)
			//if true our city side is right
			ret = true;
		else
			//if false our city side is left
			ret =  false;
		
		return ret;
	}
	
	
	/*
	 * This function will find which city is the closest to my aircraft
	 * Input: aircraft and PirateGame object
	 * Output: the closest city
	 */
    public City findTheClosestCityToMyAircraft(Aircraft aircraft, PirateGame game)
    {
    	int minDistance = 0;
    	City closestCity = null;
		List<City> cityList = game.getMyCities();
		
		if (cityList.isEmpty())
		{
			return null;
		}
		minDistance = aircraft.distance(cityList.get(0));
		closestCity = cityList.get(0);
		
		for (City city : cityList) 
		{
			if(aircraft.distance(city) <= minDistance)
			{
				minDistance = aircraft.distance(city);
				closestCity = city;
			}
		}
		return closestCity;
	}
	
    /*
     * This function will handle the drones moves
     * Input:PirateGame object
     * Output:none
     */
    private void dronesMove(PirateGame game)
    {
		boolean enemyInTheWay = false;
		int moveChoice = 0;
		
		int distanceDroneRow;
		int distanceDroneCol;
		
		int distanceCityeRow;
		int distanceCityeCol;
		
		Drone drone;
	
		List<PossibleMove> dronesMoves = setDronesMovesList(game);
		
		Location destination =  game.getMyCities().get(0).location;
		
		sortList(setDronesMovesList(game));
		
		while(!dronesMoves.isEmpty())
		{
			drone = (Drone)dronesMoves.get(0).aircraft;
			
			destination = findTheClosestCityToMyAircraft(drone, game).location;
			
			if(destination == null)
			{
				destination =  game.getMyCities().get(0).location;
			}
			if(game.getAircraftsOn(drone.location).size() < 15 && !drone.inRange(destination, 15) && game.getMaxDronesCount() != game.getAircraftsOn(drone.location).size())
			{
				if (!game.getEnemyCities().isEmpty())
				{
					if (isCityRight(game))
					{
						destination = new Location(game.getEnemyCities().get(0).location.row + 5, game.getEnemyCities().get(0).location.col + 5);
					}
					else
					{
						destination = new Location(game.getEnemyCities().get(0).location.row + 5, game.getEnemyCities().get(0).location.col - 5);
					}
				}
				else if(!game.getMyCities().isEmpty())
				{
						destination = new Location(26, 20);
				}
			}
			
			dronesMoves.get(0).destAircraft = dronesMoves.get(0).aircraft; //To delete

			List<Location> sailOptions = game.getSailOptions(drone, destination);
			
			moveChoice = 0;
			enemyInTheWay = false;
			
			if (sailOptions.size() > 1)
			{
				for (Pirate pirate : game.getAllEnemyPirates())
				{
					distanceDroneRow = drone.location.row - pirate.location.row;
					distanceDroneCol = drone.location.col - pirate.location.col;
					
					distanceCityeRow = drone.location.row - destination.row;
					distanceCityeCol = drone.location.col - destination.col;
					
					if (distanceDroneCol <= 1 && distanceDroneCol >= -1)
					{
						moveChoice = 1;
						enemyInTheWay = true;
					}
					
					if (distanceDroneRow <= 1 && distanceDroneRow >= -1)
					{
						moveChoice = 0;
						enemyInTheWay = true;
					}
					
					if(distanceCityeCol <= 1 && distanceCityeCol >= -1 && enemyInTheWay == false)
					{
						moveChoice = 0;
						enemyInTheWay = true;
					}
					
					if(distanceCityeRow <= 1 && distanceCityeRow >= -1 && enemyInTheWay == false)
					{
						moveChoice = 1;
						enemyInTheWay = true;
					}
				}
			}	
			game.setSail(drone, sailOptions.get(moveChoice));
			updateDroneList(dronesMoves, dronesMoves.get(0), game);
		}
    }
}
