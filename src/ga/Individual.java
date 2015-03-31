package ga;

import java.util.Random;

/**
 * Genera un genotipo con conexiones aleatorias (individuo)
 * @author Carlos Bailón y Daniel Castaño
 *
 */
public class Individual
{
	byte[] genotype;

	/**
	 * Genera un genotipo de una red neuronal artificial con conexiones aleatorias
	 * @param inputs -> número de neuronas de entrada
	 * @param hidden -> número de neuronas ocultas
	 * @param outputs -> número de neuronas de salida
	 */
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
	
	/**
	 * Retorna el genotipo de la red neuronal artificial
	 * @return -> genotipo de la red neuronal artificial
	 */
	public byte[] GetGenotype()
	{
		return genotype;
	}
}
