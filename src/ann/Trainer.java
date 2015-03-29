package ann;

import java.util.Random;

import ann_ga.utils.Const;
import ann_ga.utils.Const.Activation;
import ann_ga.utils.Const.EvalType;
import ann_ga.utils.Const.Purpose;

/**
 * Artificial neural network
 * @author Daniel Castaño Estrella
 *
 */
public class Trainer
{
	//NEURONS
	final int length_I;
	final int length_H;
	final int length_O;
	
	//LEARN FACTOR
	final double learn_factor;
	
	//ERRORS
	double[] 	errors_H;			//length_H errors
	double[] 	errors_O;			//output errors
	
	//DELTAS
	double[][] 	deltas_I_O;			//input -> output deltas
	double[][] 	deltas_H_I;			//input -> length_H deltas
	double[][] 	deltas_H_O;			//length_H -> output deltas
	
	double[] 	deltas_H_BIAS;		//length_H bias deltas
	double[] 	deltas_O_BIAS;		//output bias deltas	
	
	Ann ann;
	Ann ann_last;
	
	double	general_error;
	
	public Trainer(Ann ann, double learn_factor)
	{
		this.learn_factor = learn_factor;					//store the learn factor
		
		this.ann = ann;
		this.length_I = ann.neurons_I.length;
		this.length_H = ann.neurons_H.length;
		this.length_O = ann.neurons_O.length;

	}
	
	private void WeightsGen()
	{		
		Random rand = new Random();
		
		for (int i = 0; i < length_H ; i++)
		{
			//bias
			ann.weights_H_BIAS[i] = rand.nextDouble() * (1 - -1) + -1;
			
			for (int j = 0; j < length_I ; j++)
			{
				if(ann.mapping_H_I[i][j] == 1)
				{	
					ann.weights_H_I[i][j] = rand.nextDouble() * (1 - -1) + -1;
				}
			}
			
			for (int j = 0; j < length_O ; j++)
			{
				if(ann.mapping_H_O[i][j] == 1)
				{
					ann.weights_H_O[i][j] = rand.nextDouble() * (1 - -1) + -1;
				}
			}
		}
		
		for (int i = 0; i < length_O ; i++)
		{
			//bias
			ann.weights_O_BIAS[i] = rand.nextDouble() * (1 - -1) + -1;
			
			for (int j = 0; j < length_I ; j++)
			{
				if(ann.mapping_I_O[j][i] == 1)
				{	
					ann.weights_I_O[j][i] = rand.nextDouble() * (1 - -1) + -1;
				}
			}
		}
		/*
		//weights to debug
		annx.weights_I_O[0][0] = 0.0193;
		annx.weights_I_O[1][0] = 0.3838;
		
		annx.weights_H_I[0][0] = -0.9561;
		annx.weights_H_I[0][1] = -0.3124;
		annx.weights_H_BIAS[0] = -0.3428;
		
		annx.weights_H_O[0][0] = -0.8003;
		annx.weights_O_BIAS[0] = 0.0914;
		*/
		if(Const.DEBUG)
			System.out.println("First random weights calculated.");
	}
	
	public void Training(int training_iterations)
	{	
		//first random weights
		WeightsGen();
		
		DataGen datagen = new DataGen(length_I, Const.SETS, Const.MIN, Const.BINARY);
		if(Const.DEBUG)
			datagen.PrintDataSet();
		
		double[][] dataset = datagen.GetDataset();
		
		double last_general_error = Integer.MAX_VALUE;
		general_error = 0;
		
		for(int i = 0 ; i < training_iterations ; i++)
		{
			datagen.Generate(length_I, Const.SETS, Const.MIN, Const.BINARY);
			dataset = datagen.GetDataset();
			
			if(Const.DEBUG)
				System.out.println("____________________________ITERATION___"  + (i + 1) + " of "+ training_iterations);
			
			if(Const.DEBUG)
				PrintWeights();
			
			if(Const.ETYPE == EvalType.EARLY_STOP && i % Const.RANGE == 0)
			{
				ann_last = ann;
				general_error /= Const.RANGE;
				if(general_error > last_general_error)
				{
					System.out.println("VICTORYYYYYYYY");
					System.out.println("FINISHED IN ITERATION:__  " + (i - 1));
					
					ann = ann_last;
					
					for (int j = 0, max = dataset.length; j < max; j++) {
						FeedForward(dataset,j);
					}
					
					break;
				}
				else if (i != 0)
				{
					last_general_error = general_error;
					general_error = 0;
				}
			}
			
			//RESET DELTAS
			deltas_I_O = new double[length_I][length_O];		//input -> output weight
			deltas_H_I = new double[length_H][length_I];		//input -> length_H weight
			deltas_H_O = new double[length_H][length_O];		//length_H -> output weight
			
			deltas_H_BIAS = new double[length_H];
			deltas_O_BIAS = new double[length_O];
			
			double error = 0;
			for (int j = 0, max = dataset.length; j < max ; j++)
			{	
				
				FeedForward(dataset,j);
				BackPropagation();
				DeltaWeights();
				
				double current_error = 0;
				
				if(Const.DEBUG)
				{
					for (int k = 0; k < 8 ; k++)
						System.out.println("__________________________________________________________INPUTS " + k + "_____" + ann.neurons_I[k]);
					System.out.println("\n");
				}
				
				for (int k = 0; k < length_O ; k++)
				{
					current_error = ExpectedValue(k) - ann.neurons_O[k];
					error += Math.pow(current_error,2);
					
					if(Const.DEBUG)
					{
						System.out.println("________________________________________________________EXPECTED_____" + ExpectedValue(k));
						System.out.println("__________________________________________________________NEURON_____" + ann.neurons_O[k]);
						System.out.println("");
					}
				}				
			}
			error /= dataset.length;
			general_error += Math.pow(error,2);
			if(Const.DEBUG)
				System.out.println("________________________________________________________GLOBAL_ERROR___" + error + "\n\n");
			
			if(Const.ETYPE == EvalType.FITNESS && error < Const.FITNESS)
			{
				System.out.println("VICTORYYYYYYYY");
				System.out.println("FINISHED IN ITERATION:__  " + i);
				
				for (int j = 0, max = dataset.length; j < max; j++) {
					FeedForward(dataset,j);
				}
				break;
			}
			
			WeightsCorrection();
		}
		
		if(Const.ETYPE == EvalType.EARLY_STOP)
		{
			for (int j = 0, max = dataset.length; j < max; j++) {
				FeedForward(dataset,j);
			}
		}
		
		System.out.println("END");
	}
	
	public void FeedForward(double[][] dataset, int dataset_iteration)
	{	
		//reset neurons
		ann.neurons_I = new double[length_I];					//input value
		ann.neurons_H = new double[length_H];					//length_H value
		ann.neurons_O = new double[length_O];					//output value
		
		//output of input neurons
		for (int i = 0; i < length_I; i++)
		{
			//fill input neurons with values in this iteration of dataset 
			ann.neurons_I[i] = dataset[dataset_iteration][i];
		}
		
		//real output of length_H neurons
		for (int i = 0; i < length_H ; i++)
		{
			for (int j = 0; j < length_I ; j++)
			{
				if(ann.mapping_H_I[i][j] == 1)
				{	
					ann.neurons_H[i] += ann.neurons_I[j] *  ann.weights_H_I[i][j];
				}
			}
			
			ann.neurons_H[i] += ann.weights_H_BIAS[i];
			
			//Activation function
			if(Const.AFUNC == Activation.TANH)
			{
				//mytan
				//annx.neurons_H[i] = HyperbolicTan(annx.neurons_H[i]);
				//tanh
				ann.neurons_H[i] = Math.tanh(ann.neurons_H[i]);
			}
			else if(Const.AFUNC == Activation.SIGMOID)
			{
				//sigmoid
				ann.neurons_H[i] = Sigmoid(ann.neurons_H[i]);
			}
			else if(Const.AFUNC == Activation.UMBRAL)
			{
				//jump
				if(ann.neurons_H[i] < 0.5)
					ann.neurons_H[i] = 0;
				else if (ann.neurons_H[i] >= 0.5)
					ann.neurons_H[i] = 1;
			}
		}
		
		//real output of the output neurons
		for (int i = 0; i < length_O ; i++)
		{
			//first i_o
			for (int j = 0; j < length_I ; j++)
			{
				if(ann.mapping_I_O[j][i] == 1)
				{	
					ann.neurons_O[i] += ann.neurons_I[j] *  ann.weights_I_O[j][i];
				}
			}
			//then h_o
			for (int j = 0; j < length_H ; j++)
			{
				if(ann.mapping_H_O[j][i] == 1)
				{	
					ann.neurons_O[i] += ann.neurons_H[j] *  ann.weights_H_O[j][i];
				}
			}
			
			ann.neurons_O[i] += ann.weights_O_BIAS[i];
			
			//Activation function
			if(Const.AFUNC == Activation.TANH)
			{
				//mytan
				//annx.neurons_O[i] = HyperbolicTan(annx.neurons_O[i]);
				//tanh
				ann.neurons_O[i] = Math.tanh(ann.neurons_O[i]);
			}
			else if(Const.AFUNC == Activation.SIGMOID)
			{
				//sigmoid
				ann.neurons_O[i] = Sigmoid(ann.neurons_O[i]);
			}
			else if(Const.AFUNC == Activation.UMBRAL)
			{
				//jump
				if(ann.neurons_O[i] < 0.5)
					ann.neurons_O[i] = 0;
				else if (ann.neurons_O[i] >= 0.5)
					ann.neurons_O[i] = 1;
			}
			
				
		}
		if(Const.DEBUG)
			PrintNeuronsValues(dataset_iteration);
	}
	
	private void BackPropagation()
	{		
		errors_O = new double[length_O];
		
		for (int i = 0; i < length_O ; i++)
		{
			if(Const.AFUNC == Activation.TANH)
				errors_O[i] = (1 - Math.pow(ann.neurons_O[i], 2)) * (ExpectedValue(i) - ann.neurons_O[i]);
			else if(Const.AFUNC == Activation.SIGMOID)
				errors_O[i] = ann.neurons_O[i] * (1 - ann.neurons_O[i]) * (ExpectedValue(i) - ann.neurons_O[i]);
			else if(Const.AFUNC == Activation.UMBRAL)
				errors_O[i] = 1 * (ExpectedValue(i) - ann.neurons_O[i]);
			
			if(Const.DEBUG)
				System.out.println("output error_ " + i + "____" + errors_O[i]);
		}
		
		//length_H errors
		errors_H = new double[length_H];
		
		for (int i = 0; i < length_H ; i++)
		{
			
			double sum_Eo_Who = 0;
			
			for (int j = 0; j < length_O ; j++)
			{
				if(ann.mapping_H_O[i][j] == 1)
				{	
					sum_Eo_Who += ann.weights_H_O[i][j] * errors_O[j];
				}
			}
			
			if(Const.AFUNC == Activation.TANH)
				errors_H[i] = (1 - Math.pow(ann.neurons_H[i], 2)) * sum_Eo_Who;
			else if(Const.AFUNC == Activation.SIGMOID)
				errors_H[i] = ann.neurons_H[i] * (1 - ann.neurons_H[i]) * sum_Eo_Who;
			else if(Const.AFUNC == Activation.UMBRAL)
				errors_H[i] = 1 * sum_Eo_Who;
			
			if(Const.DEBUG)
				System.out.println("hidden error_ " + i + "____" + errors_H[i]);
		}
	}
	
	private void DeltaWeights()
	{	
		//deltas of i_o
		for(int i = 0; i < length_O ; i++)
		{
			//bias
			deltas_O_BIAS[i] += learn_factor * errors_O[i];
			
			for(int j = 0; j < length_I ; j++)
			{
				if(ann.mapping_I_O[j][i] == 1)
					deltas_I_O[j][i] += learn_factor * errors_O[i] *  ann.neurons_I[j];
			}
		}
		
		//deltas of h_o
		for(int i = 0; i < length_H ; i++)
		{
			//bias
			deltas_H_BIAS[i] += learn_factor * errors_H[i];
			
			for(int j = 0; j < length_O ; j++)
			{
				if(ann.mapping_H_O[i][j] == 1)
					deltas_H_O[i][j] += learn_factor * errors_O[j] *  ann.neurons_H[i];
			}
		}
		
		//deltas of i_h
		for(int i = 0; i < length_H ; i++)
		{
			for(int j = 0; j < length_I ; j++)
			{
				if(ann.mapping_H_I[i][j] == 1)
					deltas_H_I[i][j] += learn_factor * errors_H[i] *  ann.neurons_I[j];
			}
		}
		
		if(Const.DEBUG)
			PrintDeltas();
	}
	
	private void WeightsCorrection()
	{		
		//weights of i_o
		for(int i = 0; i < length_I ; i++)
		{
			for(int j = 0; j < length_O ; j++)
			{
				if(ann.mapping_I_O[i][j] == 1)
					ann.weights_I_O[i][j] += deltas_I_O[i][j];
			}
		}
		
		//weights of h_o
		for(int i = 0; i < length_O ; i++)
		{
			//bias
			ann.weights_O_BIAS[i] += deltas_O_BIAS[i];
			
			for(int j = 0; j < length_H ; j++)
			{
				if(ann.mapping_H_O[j][i] == 1)
					ann.weights_H_O[j][i] += deltas_H_O[j][i];
			}
		}
		
		//weights of i_h
		for(int i = 0; i < length_H ; i++)
		{
			//bias
			ann.weights_H_BIAS[i] += deltas_H_BIAS[i];
			
			for(int j = 0; j < length_I ; j++)
			{
				if(ann.mapping_H_I[i][j] == 1)
					ann.weights_H_I[i][j] += deltas_H_I[i][j];
			}
		}
		
		if(Const.DEBUG)
			System.out.println("WEIGHTS CORRECTED");
	}
	
	private double ExpectedValue(int output)
	{
		if(Const.PURPOSE == Purpose.XOR)
			return ExpectedValue_XOR(output);
		else if(Const.PURPOSE == Purpose.PACMAN)
			return ExpectedValue_PacMan(output);
		
		return Integer.MAX_VALUE;
	}
	
	//XOR
	private int ExpectedValue_XOR(int output)
	{
		switch (output)
		{
			case 0:
			//We don't use different expected values for different OUTPUTS because there is only one.
			if(ann.neurons_I[0] != ann.neurons_I[1])
			{
				if(Const.DEBUG)
					System.out.println("EXPECTED___" + 1);
				return 1;
			}
			else
			{
				if(Const.DEBUG)
					System.out.println("EXPECTED___" + 0);
				return 0;
			}
			default:
				//?????????
				return 999999;
		}
	}
	
	//PACMAN
	private double ExpectedValue_PacMan(int output)
	{
		/*
		 * E = ( 1 - D ) ( 1 - 2 T );
		 * Donde E es la estrategia resultante( 1 -> huir del fantasma; -1 -> ir a por el fantasma )
		 * D es la distancia al fantasma en un rango de 0 a 1
		 * T es el tiempo que le queda de ser comestible en un rango de 0 a 1 
		 */
		switch (output)
		{
			case 0:
				return (1 - Math.sqrt(ann.neurons_I[0])) * (1 - 2 * Math.sqrt(ann.neurons_I[4]));
			case 1:
				return (1 - Math.sqrt(ann.neurons_I[1])) * (1 - 2 * Math.sqrt(ann.neurons_I[5]));
			case 2:
				return (1 - Math.sqrt(ann.neurons_I[2])) * (1 - 2 * Math.sqrt(ann.neurons_I[6]));
			case 3:
				return (1 - Math.sqrt(ann.neurons_I[3])) * (1 - 2 * Math.sqrt(ann.neurons_I[7]));
			default:
				//?????????
				return 999999;
		}
	}

	
	//GETTERS
	public Ann GetAnn()
	{
		return ann;
	}
	
	public double GetError()
	{
		return general_error;
	}
	
	/////////////////////////////////////TESTING METHODS/////////////////////////////////
	
	public void PrintNeuronsValues(int dataset_iteration)
	{
		System.out.println("###_NEURONS VALUES_### DATASET_ITERATION____"+ dataset_iteration +"\n");
		System.out.println("I_values: ");
		
		for(int i = 0; i < length_I ; i++ )
		{
			System.out.print("I[" + i + "]:__ ");
			System.out.print(ann.neurons_I[i]);
			if(i + 1 < length_I)
				System.out.print("\n");
			else
				System.out.print("\n\n");
		}
		
		for(int i = 0; i < length_H ; i++ )
		{
			System.out.print("H[" + i + "]:__ ");
			System.out.print(ann.neurons_H[i]);
			if(i + 1 < length_H)
				System.out.print("\n");
			else
				System.out.print("\n\n");
		}
		
		for(int i = 0; i < length_O ; i++ )
		{
			System.out.print("O[" + i + "]:__ ");
			System.out.print(ann.neurons_O[i]);
			if(i + 1 < length_O)
				System.out.print("\n");
			else
				System.out.print("\n\n");
		}
	}
	
	public void PrintWeights()
	{
		System.out.println("###_WEIGHTS_###\n");
		System.out.println("IO_WEIGHTS: ");
		for(int i = 0; i < length_I ; i++ )
		{
			for(int j = 0; j < length_O ; j++ )
			{
				System.out.print("I[" + i + "] -> O[" + j + "]:__ ");
				System.out.print(ann.weights_I_O[i][j]);
				if(j + 1 < length_O)
					System.out.print("\n");
				else
					System.out.print("\n\n");
			}
		}
		System.out.print("\n\n");
		
		System.out.println("HI_WEIGHTS: ");
		for(int i = 0; i < length_H ; i++ )
		{
			for(int j = 0; j < length_I ; j++ )
			{
				System.out.print("H[" + i + "] -> I[" + j + "]:__ ");
				System.out.print(ann.weights_H_I[i][j]);
				if(j + 1 < length_I)
					System.out.print("\n");
				else
					System.out.print("\n\n");
			}
		}
		System.out.print("\n\n");
		
		System.out.println("H_BIAS_WEIGHTS: ");
		for(int i = 0; i < length_H ; i++ )
		{
			System.out.print("H_BIAS[" + i + "]:__ ");
			System.out.print(ann.weights_H_BIAS[i]);
				System.out.print("\n");
		}
		System.out.print("\n\n");
		
		System.out.println("HO_WEIGHTS: ");
		for(int i = 0; i < length_H ; i++ )
		{
			for(int j = 0; j < length_O ; j++ )
			{
				System.out.print("H[" + i + "] -> O[" + j + "]:__ ");
				System.out.print(ann.weights_H_O[i][j]);
				if(j + 1 < length_O)
					System.out.print("\n");
				else
					System.out.print("\n\n");
			}
		}
		
		System.out.println("O_BIAS_WEIGHTS: ");
		for(int i = 0; i < length_O ; i++ )
		{
			System.out.print("O_BIAS[" + i + "]:__ ");
			System.out.print(ann.weights_O_BIAS[i]);
				System.out.print("\n");
		}
		System.out.print("\n\n");
		
		System.out.print("#############\n");
	}
	
	public void PrintDeltas()
	{
		System.out.println("###_DELTAS_###\n");
		System.out.println("IO_DELTAS: ");
		for(int i = 0; i < length_I ; i++ )
		{
			for(int j = 0; j < length_O ; j++ )
			{
				System.out.print("I[" + i + "] -> O[" + j + "]:__ ");
				System.out.print(deltas_I_O[i][j]);
				if(j + 1 < length_O)
					System.out.print("\n");
				else
					System.out.print("\n\n");
			}
		}
		System.out.print("\n\n");
		
		System.out.println("HI_DELTAS: ");
		for(int i = 0; i < length_H ; i++ )
		{
			for(int j = 0; j < length_I ; j++ )
			{
				System.out.print("H[" + i + "] -> I[" + j + "]:__ ");
				System.out.print(deltas_H_I[i][j]);
				if(j + 1 < length_I)
					System.out.print("\n");
				else
					System.out.print("\n\n");
			}
		}
		System.out.print("\n\n");
		
		System.out.println("H_BIAS_DELTAS: ");
		for(int i = 0; i < length_H ; i++ )
		{
			System.out.print("H_BIAS[" + i + "]:__ ");
			System.out.print(deltas_H_BIAS[i]);
				System.out.print("\n");
		}
		System.out.print("\n\n");
		
		System.out.println("HO_DELTAS: ");
		for(int i = 0; i < length_H ; i++ )
		{
			for(int j = 0; j < length_O ; j++ )
			{
				System.out.print("H[" + i + "] -> O[" + j + "]:__ ");
				System.out.print(deltas_H_O[i][j]);
				if(j + 1 < length_O)
					System.out.print("\n");
				else
					System.out.print("\n\n");
			}
		}
		
		System.out.println("O_BIAS_DELTAS: ");
		for(int i = 0; i < length_O ; i++ )
		{
			System.out.print("O_BIAS[" + i + "]:__ ");
			System.out.print(deltas_O_BIAS[i]);
				System.out.print("\n");
		}
		System.out.print("\n\n");
		
		System.out.print("#############\n");
	}
	
	//from 0 to 1
	public static double Sigmoid(double x) {
	    return (1/( 1 + Math.pow(Math.E,(-1*x))));
	}
	
	//from -1 to 1
		public static double HyperbolicTan(double x) {
		    return ((1 - Math.pow(Math.E,(-2*x))) / (1 + Math.pow(Math.E,(-2*x))));
		}
		
	public static double ArcHyperbolicTan(double x) 
	{ 
		return 0.5*Math.log( (x + 1.0) / (x - 1.0) ); 
	} 
}
