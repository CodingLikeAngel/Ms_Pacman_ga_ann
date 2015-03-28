package ann;

import java.util.Random;

public class DataGen
{
	double[][] dataset;	//inputs and sets
	
	public DataGen(int inputs, int sets, int min, boolean binary)
	{
		dataset = Generate(inputs,sets, min, binary);
	}
	
	private double[][] Generate(int inputs, int sets, int min, boolean binary)
	{
		double[][] dataset = new double[sets][inputs];
		
		int r_max = 1;
		int r_min = min;
		
		Random rand = new Random();
		
		for (int i = 0; i < sets; i++)
		{
			for (int j = 0; j < inputs; j++)
			{
				dataset[i][j] = rand.nextDouble() * (r_max - r_min) + r_min;
				
				//Clamp for binary values.
				if(binary)
				{
					if(dataset[i][j] < 0.5)
						dataset[i][j] = 0;
					else
						dataset[i][j] = 1;
				}
			}
		}	
		
		/*
		dataset[0][0] = 0;
		dataset[0][1] = 0;
		
		dataset[1][0] = 0;
		dataset[1][1] = 1;
		
		dataset[2][0] = 1;
		dataset[2][1] = 0;
		
		dataset[3][0] = 1;
		dataset[3][1] = 1;
		*/
		return dataset;
	}
	
	public double[][] GetDataset()
	{
		return dataset;
	}
	
	//TESTING METHODS
	public void PrintDataSet()
	{
		System.out.println("###_DATASET_### iteration -> INPUT\n");
		for(int i = 0, max = dataset.length; i < max ; i++ )
		{
			for(int j = 0, max2 = dataset[0].length; j < max2 ; j++ )
			{
				System.out.print("i[" + i + "] -> I[" + j + "]:__ ");
				System.out.print(dataset[i][j]);
				if(j + 1 < max2)
					System.out.print("\n");
				else
					System.out.print("\n\n");
			}
		}
		System.out.print("\n\n");
	}
}
