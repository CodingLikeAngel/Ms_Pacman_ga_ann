package ga;

import java.util.Random;

import ann.Ann;
import ann.Trainer;
import ann_ga.utils.Const;

public class GaController
{
	byte[][] individuals;
	Ann[] anns;
	double[] errors;
	
	byte[][] selected_parents;
			
	public GaController()
	{
		int individual_size = Const.INDIVIDUALS;
		
		errors = new double[individual_size];
		anns = new Ann[individual_size];
		individuals = new byte [individual_size][];
		
		CreateIndividuals();
		TrainingIndividuals();
		SelectionTurn();
	}
	
	private void CreateIndividuals()
	{
		for (int i = 0, max = individuals.length; i < max; i++)
		{
			individuals[i] = new Individual(Const.INPUTS,  Const.HIDDEN, Const.OUPUTS).GetGenotype();
		}
	}
	
	private void TrainingIndividuals()
	{
		for (int i = 0, max = individuals.length; i < max; i++)
		{
			anns[i] = new Ann(individuals[i], Const.INPUTS, Const.OUPUTS);
			
			Trainer trainer = new Trainer(anns[i], Const.LEARN_FACTOR);
			trainer.Training(Const.TRAININGS);
			
			errors[i] = trainer.GetError(); 
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
			     if (errors[selected_individuals_indexs[i]] < minimum)
			     {
			    	 minimum = errors[selected_individuals_indexs[i]];
			    	 minimum_pos = i;
			     }
			}
			
			selected_parents_index[j] = selected_individuals_indexs[minimum_pos];
		}
		
		//store selected parents
		selected_parents = new byte[Const.SELEC_PARENTS][];
		
		for (int i = 0, max = selected_parents.length; i < max; i++)
		{
			selected_parents[i] =  individuals[selected_parents_index[i]];
		}
	}
}
