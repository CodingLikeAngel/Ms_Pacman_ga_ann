package ann;

import ann_ga.utils.Const;

public class Ann
{
	//MAPPING
	public final Byte[][] 	mapping_I_O;	//input -> output mapping
	public final Byte[][] 	mapping_H_I;	//input -> hidden mapping
	public final Byte[][]	mapping_H_O;	//hidden -> output mapping
	
	//WEIGHTS
	public double[][] 	weights_I_O;		//input -> output weight
	public double[][] 	weights_H_I;		//input -> hidden weight
	public double[][] 	weights_H_O;		//hidden -> output weight
	
	public double[] 	weights_H_BIAS;		//hidden bias weight
	public double[] 	weights_O_BIAS;		//output bias weight
	
	//VALUES
	public double[] 	neurons_I;			//input values
	public double[] 	neurons_H;			//hidden values
	public double[] 	neurons_O;			//output values
	
	public Ann(byte[] genotype, final int inputs, final int outputs)
	{
		final int gen_size = genotype.length;				//store the genontype size
		final int blocks_length = inputs * outputs;			//length of the blocks of the genotype
		final int hidden = gen_size / blocks_length - 1;	// -1 because of the i_o
		
		//arrays for storing values of the neurons
		neurons_I = new double[inputs];						//input value
		neurons_H = new double[hidden];						//hidden value
		neurons_O = new double[outputs];					//output value
		
		//arrays for storing mapping of weights
		mapping_I_O = new Byte[inputs][outputs];			//input -> output mapping
		mapping_H_I = new Byte[hidden][inputs];				//hidden -> input mapping
		mapping_H_O = new Byte[hidden][outputs];			//hidden -> output mapping
		
		//arrays for storing values of the weights
		weights_I_O = new double[inputs][outputs];			//input -> output weight
		weights_H_I = new double[hidden][inputs];			//hidden -> input weight
		weights_H_O = new double[hidden][outputs];			//hidden -> output weight
		
		weights_H_BIAS = new double[hidden];				//hidden bias weight
		weights_O_BIAS = new double[outputs];				//output bias weight
		
		WeightMapping(genotype, gen_size, blocks_length, inputs, hidden, outputs);
	}
	
	private void WeightMapping(byte[] genotype, final int gen_size, final int blocks_length,final int inputs, final int hidden, final int outputs)
	{	
		for (int i = 0; i < gen_size; i++)
		{
			Byte val = genotype[i];
			
			int input_index = 0;
			int output_index = i;
			int hidden_index = -1;
			
			int h_threshold = i;
			
			while(h_threshold >= blocks_length)
			{
				hidden_index++;
				h_threshold -= blocks_length;
			}
			
			while(output_index >= outputs)
			{
				input_index++;
				output_index -= outputs;
			}
			
			while(input_index >= inputs)
			{
				input_index -= inputs;
			}	
			
			if(i < blocks_length )
			{
				//System.out.println("I___ " + input_index + "    O___ " + output_index);
				//input ->  output connections mapping
				mapping_I_O[input_index][output_index] = val;
			}
			
			if(i >= blocks_length )
			{
				//System.out.println("I___ " + input_index + "    H___ " + hidden_index + "    O___ " + output_index);
				//input ->  hidden connections mapping
				if(mapping_H_I[hidden_index][input_index] == null || mapping_H_I[hidden_index][input_index] == 0)
					mapping_H_I[hidden_index][input_index] = val;
				
				//hidden ->  output connections mapping
				if(mapping_H_O[hidden_index][output_index] == null || mapping_H_O[hidden_index][output_index] == 0)
					mapping_H_O[hidden_index][output_index] = val;
			}
		}
		
		if(Const.DEBUG)
			PrintWeightMapping(inputs, hidden, outputs);
	}
	
	public void PrintWeightMapping(final int inputs, final int hidden, final int outputs)
	{
		System.out.println("###_WEIGHT MAPPING_###\n");
		System.out.println("IO_WM: ");
		for(int i = 0; i < inputs ; i++ )
		{
			for(int j = 0; j < outputs ; j++ )
			{
				System.out.print("I[" + i + "] -> O[" + j + "]:__ ");
				System.out.print(mapping_I_O[i][j]);
				if(j + 1 < outputs)
					System.out.print("\n");
				else
					System.out.print("\n\n");
			}
		}
		System.out.print("\n\n");
		
		System.out.println("HI_WM: ");
		for(int i = 0; i < hidden ; i++ )
		{
			for(int j = 0; j < inputs ; j++ )
			{
				System.out.print("H[" + i + "] -> I[" + j + "]:__ ");
				System.out.print(mapping_H_I[i][j]);
				if(j + 1 < inputs)
					System.out.print("\n");
				else
					System.out.print("\n\n");
			}
		}
		System.out.print("\n\n");
		
		System.out.println("HO_WM: ");
		for(int i = 0; i < hidden ; i++ )
		{
			for(int j = 0; j < outputs ; j++ )
			{
				System.out.print("H[" + i + "] -> O[" + j + "]:__ ");
				System.out.print(mapping_H_O[i][j]);
				if(j + 1 < outputs)
					System.out.print("\n");
				else
					System.out.print("\n\n");
			}
		}
		System.out.print("#############\n");
	}
}
