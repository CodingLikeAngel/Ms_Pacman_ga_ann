package ga;

import java.util.Random;

import ann.Ann;
import ann.Trainer;
import ann_ga.utils.Const;

public class GaController
{
	byte[][] individuals;
	Ann[] anns;
	double[] fitness;
	byte[][] new_individuals;
			
	public GaController()
	{
		int individual_size = Const.INDIVIDUALS;
		
		fitness = new double[individual_size];
		anns = new Ann[individual_size];
		individuals = new byte [individual_size][];
		
		CreateIndividuals();
		
		for (int i = 0, max = Const.GA_LOOPS; i < max; i++)
		{
			CreationTrainingAnns();
			SelectionTurn(); //Callback cross and mutation
			Replacement();
		}
		CreationTrainingAnns();
	}

	private void CreateIndividuals()
	{
		for (int i = 0, max = individuals.length; i < max; i++)
		{
			individuals[i] = new Individual(Const.INPUTS,  Const.HIDDEN, Const.OUPUTS).GetGenotype();
		}
	}
	
	private void CreationTrainingAnns()
	{
		for (int i = 0, max = individuals.length; i < max; i++)
		{
			//only if is a new ann
			if(fitness[i] == 0)
			{
				anns[i] = new Ann(individuals[i], Const.INPUTS, Const.OUPUTS);
				
				Trainer trainer = new Trainer(anns[i], Const.LEARN_FACTOR);
				trainer.Training(Const.TRAININGS);
				
				fitness[i] = trainer.GetError();
			}
		}
	}
	
	private void SelectionTurn()
	{	
		Random rand = new Random();
		int Low = 0;
		int High = individuals.length;
		
		int[] selected_parents_index = new int [Const.SELEC_PARENTS];
		
		for (int j = 0, max2 = selected_parents_index.length; j < max2 ; j++)
		{
			int[] selected_individuals_indexs = new int[Const.SELEC_TURN_PRESELECTION];
			
			if(High < Const.SELEC_TURN_PRESELECTION)
				System.out.println("ERROR: There are less INDIVIDUALS than SELEC_TURN_PRESELECTION");
			
			for (int i = 0, max = Const.SELEC_TURN_PRESELECTION; i < max; i++)
			{			
				//Non repeating index.
				int index;
				boolean index_repeated;
				do
				{
					index_repeated = false;
					index = rand.nextInt(High-Low) + Low;
					
					//non repeating one previous selected parent
					for (int k = 0; k < j; k++)
					{
						if(selected_parents_index[k] == index)
						{
							index_repeated = true;
							break;
						}
					}
					
					if(!index_repeated)
					{
						//non repeating one inside the list for choosing the parent
						for (int k = 0; k < i; k++)
						{
							if(selected_individuals_indexs[k] == index)
							{
								index_repeated = true;
								break;
							}
						}
					}
					
				} while (index_repeated);
				selected_individuals_indexs[i] = index;
			}
			
			//Selec the best
			int minimum_pos= 0;
			double minimum = 9999999;
			
			for (int i = 0, max = selected_individuals_indexs.length; i < max; i++)
			{
			     if (fitness[selected_individuals_indexs[i]] < minimum)
			     {
			    	 minimum = fitness[selected_individuals_indexs[i]];
			    	 minimum_pos = i;
			     }
			}
			
			selected_parents_index[j] = selected_individuals_indexs[minimum_pos];
		}
		
		//store selected parents
		byte[][] selected_parents = new byte[Const.SELEC_PARENTS][];
		
		for (int i = 0, max = selected_parents.length; i < max; i++)
		{
			selected_parents[i] =  individuals[selected_parents_index[i]];
		}
		
		Cross_ObjectiveFunction(selected_parents, selected_parents_index);
	}
	
	private void Cross_ObjectiveFunction(byte[][] selected_parents, int[] selected_parents_index)
	{
		byte[][] children = new byte[Const.CROSS_CHILDREN][];
		//Cross probability because less fitness is better.
		double parent_0_prob = fitness[selected_parents_index[1]]/(fitness[selected_parents_index[0]] + fitness[selected_parents_index[1]]);
	
		Random rand = new Random();
		
		for (int i = 0, max = children.length; i < max; i++)
		{
			byte[] individual_bytes = new byte[selected_parents[0].length];
			
			for (int j = 0, max2 = individual_bytes.length; j < max2; j++)
			{
				if(rand.nextDouble() * (1 - 0) + 0 < parent_0_prob)
					individual_bytes[j] = selected_parents[0][j];
				else
					individual_bytes[j] = selected_parents[1][j];
			}
			children[i] = individual_bytes;
		}
		
		Mutation(children);
	}
	
	private void Mutation(byte[][] children)
	{
		Random rand = new Random();
		
		for (int i = 0, max = children.length; i < max; i++)
		{
			for (int j = 0, max2 = children[0].length; j < max2; j++)
			{
				if(rand.nextDouble() * (1 - 0) + 0 <= Const.MUTATION_PROB)
				{
					if(children[i][j] == 1)
						children[i][j] = 0;
					else
						children[i][j] = 1;
				}
			}
		}
		
		new_individuals = children;
	}
	
	private void Replacement()
	{
		//100% tasa
		if(new_individuals.length == individuals.length)
		{
			individuals = new_individuals;
			
			for (int i = 0, max = individuals.length; i < max; i++)
			{
				fitness[i] = 0;
			}
		}
		//elitist
		else
		{
			//Selec the worst and replace him
			for (int i = 0, length = new_individuals.length; i < length; i++)
			{
				int max_pos= 0;
				double max = 0;
				
				for (int j = 0, length2 = individuals.length; j < length2; j++)
				{
				     if (fitness[j] > max)
				     {
				    	 max = fitness[j];
				    	 max_pos = j;
				     }
				}
				//max fitness position, aka worst individual
				individuals[max_pos] = new_individuals[i];
				//reset the error to 0, The algorithm only trains individuals with 0 error on every iteration.
				fitness[max_pos] = 0;
			}
		}
	}
	
	public Ann GetBestAnn()
	{
		//Selec the best
		int minimum_pos= 0;
		double minimum = 9999999;
		
		for (int i = 0, max = individuals.length; i < max; i++)
		{
		     if (fitness[i] < minimum)
		     {
		    	 minimum = fitness[i];
		    	 minimum_pos = i;
		     }
		}
		
		return anns[minimum_pos];
	}
}
