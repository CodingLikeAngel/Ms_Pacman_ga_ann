package ga;

import java.util.Random;

public class Genotype
{
	byte[] genotype;

	public Genotype(final int inputs, final int hidden, final int outputs)
	{
		int genotype_size = inputs * outputs * (hidden + 1);
		this.genotype = new byte[genotype_size];
		Random rand = new Random();
		
		for (int i = 0; i < genotype_size; i++)
		{
			double num = rand.nextDouble() * (1 - 0) + 0;

			//System.out.println("" + num);
			if(num < 0.5)
				genotype[i] = 0;
			else
				genotype[i] = 1;
		};
		/*
		//i8 h4 o4
		//direct disconnected
		for (int i = 0; i < inputs*outputs; i++) {
			genotype[i] = 0;
		}
		
		for (int i = inputs*outputs; i < 2*inputs*outputs*hidden; i++) {
			if(genotype[i] == inputs*outputs || genotype[i] == (inputs*outputs + 16))
				genotype[i] = 1;
			else
				genotype[i] = 0;
		}
		*/
	}
	
	public byte[] GetGenotype()
	{
		return genotype;
	}
}
