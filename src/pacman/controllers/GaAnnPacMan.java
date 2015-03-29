package pacman.controllers;

import java.util.ArrayList;

import ann.Ann;
import ann.Trainer;
import ann_ga.utils.Const;
import pacman.game.Game;
import static pacman.game.Constants.*;

/*
 * Pac-Man controller as part of the starter package - simply upload this file as a zip called
 * MyPacMan.zip and you will be entered into the rankings - as simple as that! Feel free to modify 
 * it or to start from scratch, using the classes supplied with the original software. Best of luck!
 * 
 * This controller utilises 3 tactics, in order of importance:
 * 1. Get away from any non-edible ghost that is in close proximity
 * 2. Go after the nearest edible ghost
 * 3. Go to the nearest pill/power pill
 */
public class GaAnnPacMan extends Controller<MOVE>
{	
	private static final int MIN_DISTANCE=20;	//if a ghost is this close, run away
	Ann ann;
	Trainer trainer;
	
	public GaAnnPacMan(Ann ann, Trainer trainer) {
		this.trainer = trainer;
		this.ann = ann;
	}

	public MOVE getMove(Game game,long timeDue)
	{	
		double[][] dataset = new double[1][Const.INPUTS];
		GHOST[] ghosts = new GHOST[4];
		
		int current=game.getPacmanCurrentNodeIndex();
		
		int index = 0;
		for(GHOST ghost : GHOST.values())
		{
			int i_distance = game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost));
			double d_distance = normalizeValue(i_distance,150);
			
			int i_time = game.getGhostEdibleTime(ghost);
			double d_time = normalizeValue(i_time,200);
	
			if(d_distance < 0)
				d_distance = 0.9;

			dataset[0][index] 		= d_distance;
			dataset[0][index + 4] 	= d_time;
			
			ghosts[index] = ghost;			
			
			index++;
		}
		
		trainer.FeedForward(dataset, 0);
		//Ann ann = trainer.GetAnn();
		
		int highest_pos = 0;
		double highest = 0;
		for( int i = 0, max = ann.neurons_O.length; i < max; i++ )
		{
		     if (Math.abs(ann.neurons_O[i]) > highest)
		     {
		    	 highest = Math.abs(ann.neurons_O[i]);
		    	 highest_pos = i;
		     }
		}
		
		System.out.println("Blinky : D -> " + dataset[0][0] + " T -> " + dataset[0][4] + " VAL -> " + ann.neurons_O[0]);
		System.out.println("Pinky : D -> " + dataset[0][1] + " T -> " + dataset[0][5] + " VAL -> " + ann.neurons_O[1]);
		System.out.println("Inky : D -> " + dataset[0][2] + " T -> " + dataset[0][6] + " VAL -> " + ann.neurons_O[2]);
		System.out.println("Sue : D -> " + dataset[0][3] + " T -> " + dataset[0][7] + " VAL -> " + ann.neurons_O[3]);
		
		if(highest < 0.2)
		{
			System.out.println("o");
			//System.out.println("o highest " + Math.abs(highest));
			int[] pills=game.getPillIndices();
			int[] powerPills=game.getPowerPillIndices();		
			
			ArrayList<Integer> targets=new ArrayList<Integer>();
			
			for(int i=0;i<pills.length;i++)					//check which pills are available			
				if(game.isPillStillAvailable(i))
					targets.add(pills[i]);
			
			for(int i=0;i<powerPills.length;i++)			//check with power pills are available
				if(game.isPowerPillStillAvailable(i))
					targets.add(powerPills[i]);				
			
			int[] targetsArray=new int[targets.size()];		//convert from ArrayList to array
			
			for(int i=0;i<targetsArray.length;i++)
				targetsArray[i]=targets.get(i);
			
			//return the next direction once the closest target has been identified
			return game.getNextMoveTowardsTarget(current,game.getClosestNodeIndexFromNodeIndex(current,targetsArray,DM.PATH),DM.PATH);
			
		}
		else if(ann.neurons_O[highest_pos] > 0)
		{
			System.out.println("-");
			return game.getNextMoveAwayFromTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(ghosts[highest_pos]),DM.PATH);
		}
		else
		{
			System.out.println("+");
			return game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(ghosts[highest_pos]),DM.PATH);
		}
		/*
		//LINEAL FUNCTION
		////////////////////////////////////////////////
		double[] outputs = new double [4];
		GHOST[] ghosts = new GHOST[4];
		
		int current=game.getPacmanCurrentNodeIndex();
		
		int index = 0;
		for(GHOST ghost : GHOST.values())
		{
			//System.out.println(ghost + "distance -> " + game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost)));
			int i_distance = game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost));
			double d_distance = normalizeValue(i_distance,150);
			
			int i_time = game.getGhostEdibleTime(ghost);
			double d_time = normalizeValue(i_time,200);
	
			if(d_distance < 0)
				d_distance = 0.9;

			outputs[index] = (1 - Math.sqrt(d_distance)) * (1 - 2 * Math.sqrt(d_time));
			
			//System.out.println("GHOST " + i + " -> " + outputs[i]);
			ghosts[index] = ghost;			
			//System.out.println(ghost + " : T -> " + d_time + " D -> " + d_distance + " VAL -> " + outputs[index]);
			index++;
		}
		
		int highest_pos = 0;
		double highest = 0;
		for( int i = 0, max = outputs.length; i < max; i++ )
		{
		     if (Math.abs(outputs[i]) > highest)
		     {
		    	 highest = Math.abs(outputs[i]);
		    	 highest_pos = i;
		     }
		}
		
		if(highest < 0.2)
		{
			//System.out.println("o highest " + Math.abs(highest));
			int[] pills=game.getPillIndices();
			int[] powerPills=game.getPowerPillIndices();		
			
			ArrayList<Integer> targets=new ArrayList<Integer>();
			
			for(int i=0;i<pills.length;i++)					//check which pills are available			
				if(game.isPillStillAvailable(i))
					targets.add(pills[i]);
			
			for(int i=0;i<powerPills.length;i++)			//check with power pills are available
				if(game.isPowerPillStillAvailable(i))
					targets.add(powerPills[i]);				
			
			int[] targetsArray=new int[targets.size()];		//convert from ArrayList to array
			
			for(int i=0;i<targetsArray.length;i++)
				targetsArray[i]=targets.get(i);
			
			//return the next direction once the closest target has been identified
			return game.getNextMoveTowardsTarget(current,game.getClosestNodeIndexFromNodeIndex(current,targetsArray,DM.PATH),DM.PATH);
			
		}
		else if(outputs[highest_pos] > 0)
		{
			//System.out.println("-");
			return game.getNextMoveAwayFromTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(ghosts[highest_pos]),DM.PATH);
		}
		else
		{
			//System.out.println("+");
			return game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(ghosts[highest_pos]),DM.PATH);
		}
		*/
		
		////////////////////////////////////////////////////////////////////////////////////
		/*
		int current=game.getPacmanCurrentNodeIndex();
		
		//Strategy 1: if any non-edible ghost is too close (less than MIN_DISTANCE), run away
		for(GHOST ghost : GHOST.values())
			if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0)
				if(game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost))<MIN_DISTANCE)
					return game.getNextMoveAwayFromTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(ghost),DM.PATH);
		
		//Strategy 2: find the nearest edible ghost and go after them 
		int minDistance=Integer.MAX_VALUE;
		GHOST minGhost=null;		
		
		for(GHOST ghost : GHOST.values())
			if(game.getGhostEdibleTime(ghost)>0)
			{
				int distance=game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost));
				
				if(distance<minDistance)
				{
					minDistance=distance;
					minGhost=ghost;
				}
			}
		
		if(minGhost!=null)	//we found an edible ghost
			return game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(minGhost),DM.PATH);
		
		//Strategy 3: go after the pills and power pills
		int[] pills=game.getPillIndices();
		int[] powerPills=game.getPowerPillIndices();		
		
		ArrayList<Integer> targets=new ArrayList<Integer>();
		
		for(int i=0;i<pills.length;i++)					//check which pills are available			
			if(game.isPillStillAvailable(i))
				targets.add(pills[i]);
		
		for(int i=0;i<powerPills.length;i++)			//check with power pills are available
			if(game.isPowerPillStillAvailable(i))
				targets.add(powerPills[i]);				
		
		int[] targetsArray=new int[targets.size()];		//convert from ArrayList to array
		
		for(int i=0;i<targetsArray.length;i++)
			targetsArray[i]=targets.get(i);
		
		//return the next direction once the closest target has been identified
		return game.getNextMoveTowardsTarget(current,game.getClosestNodeIndexFromNodeIndex(current,targetsArray,DM.PATH),DM.PATH);
		*/
	}
	//150 dist
	//200 tiempo
	public double normalizeValue(int value, int max_value)
	{
		return ((value - 0) / (double) (max_value - 0)) * (1 - 0) + 0;
	}
}























