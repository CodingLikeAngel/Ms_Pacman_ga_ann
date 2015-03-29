package ga;

import java.util.Random;

public class Individual
{
	byte[] genotype;

	public Individual(final int inputs, final int hidden, final int outputs)
	{
		int genotype_size = inputs * outputs * (hidden + 1);
		this.genotype = new byte[genotype_size];
		Random rand = new Random();
		
		for (int i = 0; i < genotype_size; i++)
		{
			double num = rand.nextDouble() * (1 - 0) + 0;

			if(num < 0.5)
				genotype[i] = 0;
			else
				genotype[i] = 1;
		};
	}
	
	public byte[] GetGenotype()
	{
		return genotype;
	}
}
