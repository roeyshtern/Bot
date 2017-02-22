package bots;
 
import java.util.*;

import pirates.*;
 
 
public class MyBot implements PirateBot {
 
    public class PossibleMove implements Comparable<PossibleMove>{
        private Aircraft aircraft;
        private GameObject destAircraft;
        private Location destination;
        private boolean toMove;
        
        public PossibleMove(Aircraft aircraft, GameObject destAircraft, Location destination) {
            this.aircraft = aircraft;
            this.destAircraft = destAircraft;
            this.destination = destination;
            toMove = true;
        }
        
        public int compareTo(PossibleMove other)
        {
        	int ret = -1;
        	if (aircraft.distance(destAircraft) > other.aircraft.distance(other.destAircraft))
        		ret = 1;
        	else if (aircraft.distance(destAircraft) == other.aircraft.distance(other.destAircraft))
        		ret = 0;
        	
        	return ret;
        } 
    }
   
    public void doTurn(PirateGame game)
    {
        boolean decoyedThisTurn = false;
        List<PossibleMove> possibleMoves = allPossibleMoves(game);
        
        Collections.sort(possibleMoves);
               
        for (PossibleMove thisMove : possibleMoves) {
        	
        	if(thisMove.toMove == false)
        		continue;
        	
            if (!decoyedThisTurn && tryDecoy((Pirate)thisMove.aircraft, game)) {
                decoyedThisTurn = true;
            }
            else if(!tryAttack((Pirate)thisMove.aircraft, game))
            {
                if(game.getEnemyCities().size() == 0)
                {
                    if (game.getAircraftsOn(thisMove.aircraft.location).size() > game.getAllMyPirates().size() && game.getAllIslands().size() > 0)
                        thisMove.destination = new Location(game.getAllIslands().get(0).location.row + 2, game.getAllIslands().get(0).location.col);
                    
                    else
                    {
                        if (game.getMyLivingDrones().size() > 0)
                            thisMove.destination = new Location(game.getMyLivingDrones().get(0).location.row, game.getMyLivingDrones().get(0).location.col - 5);
                        
                        else
                            thisMove.destination = new Location(game.getAllIslands().get(0).location.row + 2 , game.getAllIslands().get(0).location.col);
                    }
                }
                List<Location> sailOptions = game.getSailOptions(thisMove.aircraft, thisMove.destination);
                if(sailOptions.size() > 1)
                    game.setSail(thisMove.aircraft, sailOptions.get(1));
                
                else
                    game.setSail(thisMove.aircraft, sailOptions.get(0));
            }
           
            updateList(possibleMoves, thisMove);
        }
       
        dronesMove(game);
        handleDecoy(game);
        
    }
   
   
    private void handleDecoy(PirateGame game) {
        Decoy decoy = game.getMyself().decoy;
   
        // if we have a decoy
        if (decoy != null) {
            // Choose destination
            if(!game.getEnemyLivingPirates().isEmpty())
            {
                Pirate destination = game.getMyLivingPirates().get(0);
                game.debug(destination);
                // Get sail options
                List<Location> sailOptions = game.getSailOptions(decoy, destination);
                // Set sail!
                game.setSail(decoy, sailOptions.get(0));
                // Print a message
                game.debug("decoy " + decoy + " sails to " + sailOptions.get(0));
            }
           
        }
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
                allPossibleMovesList.add(new PossibleMove(pirate, aircraft, aircraft.location));
            }
           
            for (Island island : islands) {
                allPossibleMovesList.add(new PossibleMove(pirate, island, island.location));
            }
           
        }  
       
        return allPossibleMovesList;
    }
   
    /*
     * This function will remove moves from the list
     * Input: all possible moves and this move
     * Output: none
     */
    private void updateList(List<PossibleMove> allPossibleMoves, PossibleMove thisMove)
    {
        for (PossibleMove possibleMove : allPossibleMoves) {
			if (possibleMove.aircraft == thisMove.aircraft || thisMove.destination == possibleMove.destination)
			{
				possibleMove.toMove = false;
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
                    allDronesMoves.add(new PossibleMove(drone1, drone2, drone2.location));
                }
            }
        }
       
        return allDronesMoves;
    }
   
    private void updateDroneList(List<PossibleMove> allDronesPossibleMoves, PossibleMove thisMove, PirateGame game)
    {   
        for (PossibleMove possibleMove : allDronesPossibleMoves) {
			if(thisMove.aircraft == possibleMove.aircraft || thisMove.destAircraft == possibleMove.aircraft)
			{
				possibleMove.toMove = false;
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
    	
    	int moveChoice = 0;

        Drone drone;
   
        List<PossibleMove> dronesMoves = setDronesMovesList(game);
       
        Location destination;
       
        Collections.sort(dronesMoves);
        
        for (PossibleMove possibleMove : dronesMoves) { 		
        	
        	if (possibleMove.toMove == false)
        		continue;
        	
            drone = (Drone)possibleMove.aircraft;
          
            destination = chooseDroneDest(drone, game);   

            dronesMoves.get(0).destAircraft = possibleMove.aircraft; //To delete
            
            List<Location> sailOptions = game.getSailOptions(drone, destination);
        
            if (sailOptions.size() > 1)
            	moveChoice = checkDroneSail(drone, destination, game);
            else
            	moveChoice = 0;

        	game.setSail(drone, sailOptions.get(moveChoice));
        
            updateDroneList(dronesMoves, possibleMove, game);
        }
    }
    
    private Location chooseDroneDest(Drone drone, PirateGame game)
    {
    	Location destination = findTheClosestCityToMyAircraft(drone, game).location;
    	
        if(game.getAircraftsOn(drone.location).size() < 28 && !drone.inRange(destination, 15) && game.getMaxDronesCount() != game.getAircraftsOn(drone.location).size())
        {
            if (!game.getEnemyCities().isEmpty())
            {
                if (isCityRight(game))
                    destination = new Location(game.getEnemyCities().get(0).location.row + 3, game.getEnemyCities().get(0).location.col + 3);
                
                else
                    destination = new Location(game.getEnemyCities().get(0).location.row + 3, game.getEnemyCities().get(0).location.col - 3 );
            }
            
            else if(!game.getMyCities().isEmpty())
                    destination = new Location(18, 26);  
        }
        
        if (game.getEnemyCities().size() == 0 && game.getAircraftsOn(new Location(9, 6)).size() == 1)
        {
        	if (game.getAircraftsOn(new Location(9, 6)).get(0).currentHealth != 1 )
        		destination = new Location(10, 9);
        }
        
       return destination; 
   }
    
    
    private int checkDroneSail(Drone drone,Location destination, PirateGame game)
    {
        int moveChoice = 0;

        boolean enemyInTheWay = false;

        int distanceDroneRow;
        int distanceDroneCol;
       
        int distanceCityeRow;
        int distanceCityeCol;
       
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
        
        return moveChoice;
    }
    private boolean tryDecoy(Pirate pirate, PirateGame game) {
    // Check if the player can decoy a pirate
        if (pirate.owner.turnsToDecoyReload == 0) {
            // Whoosh
            game.decoy(pirate);
            // print a message
            game.debug("pirate " + pirate + " decoys itself");
            // Did decoy
            return true;
        }
   
        // didn't decoy
        return false;
    }
 
}