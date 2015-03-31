package ann;

import java.util.Random;

import ann_ga.utils.Const;
import ann_ga.utils.Const.Activation;
import ann_ga.utils.Const.EvalType;
import ann_ga.utils.Const.Purpose;

/**
 * Clase de entrenamiento de una red neuronal artificial.
 * @author Carlos Bail�n y Daniel Casta�o
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
	
	/**
	 * Constructor del entrenador. Almacena datos importantes como la propia red.
	 * @param ann -> Red neuronal artificial
	 * @param learn_factor -> Constante de aprendizaje
	 */
	public Trainer(Ann ann, double learn_factor)
	{
		this.learn_factor = learn_factor;					//store the learn factor
		
		this.ann = ann;
		this.length_I = ann.neurons_I.length;
		this.length_H = ann.neurons_H.length;
		this.length_O = ann.neurons_O.length;

	}
	
	/**
	 * Asignaci�n inicial de los pesos de las conexiones entre neuronas
	 * Valores aleatorios  
	 */
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

		if(Const.DEBUG)
			System.out.println("First random weights calculated.");
	}
	
	/***
	 * Funci�n principal para la iteraciones de entrenamiento.
	 * Se llaman a las funciones
	 * WeightsGen() solamente una vez
	 * FeedForward()
	 * BackPropagation() 
	 * DeltaWeights()
	 * ExpectedValue ()
	 * @param training_iterations -> n�mero de iteraciones para el entrenamiento.
	 */
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
					ann_last = ann;
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
					for (int k = 0; k < length_I ; k++)
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
			error /= length_O;
			general_error += error;
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
	/**
	 * C�lculo de salida de cada neurona hasta llegar a las de salida, obteniendo el valor de salida de la red en esa iteraci�n.
	 * @param dataset datos de las neuronas de entrada
	 * @param dataset_iteration -> el numero de iteraci�n del dataset actual  
	 */
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
	/**
	 * C�lculo de los errores de las capas de salida y ocultas de la red neuronal para posteriormente calcular los pesos delta
	 */
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
	/**
	 * Establece la varianza de pesos en los deltas en funci�n del valor de error en cada conexi�n para la posterior correcci�n de los pesos [WeightsCorrection()]
	 */
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
	
	/**
	 * Asignaci�n de los nuevos pesos a las conexiones entre neuronas
	 * Se realiza al final de cada iteraci�n (una vez completado el dataset)
	 */
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
	
	/**
	 * Dos tipos de sistema neuronal
	 * - XOR
	 * - PACMAN
	 * Dependiendo de el tipo de sistema neuronal en el que nos encontremos llamaremos a distintas funciones.
	 * @param output -> valor de salida de la red neuronal en una neurona
	 * @return double-> El resultado de la f�rmula de valores esperados
	 */
	private double ExpectedValue(int output)
	{
		if(Const.PURPOSE == Purpose.XOR)
			return ExpectedValue_XOR(output);
		else if(Const.PURPOSE == Purpose.PACMAN)
			return ExpectedValue_PacMan(output);
		
		return Integer.MAX_VALUE;
	}
	
	//XOR
	/**
	 * Solamente hay una salida, as�q ue hay un �nico valor esperado.
	 * @param output -> uno de los valores de salida de la red neuronal
	 * @return int -> El resultado de la f�rmula de valores esperados
	 */
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
				//not possible
				return 999999;
		}
	}
	
	//PACMAN
	/**
	 * E = ( 1 - ra�z(D) ) ( 1 - 2 * ra�z(T) );
	 * Donde E es la estrategia resultante( 1 -> huir del fantasma; -1 -> ir a por el fantasma )
	 * D es la distancia al fantasma en un rango de 0 a 1
	 * T es el tiempo que le queda de ser comestible en un rango de 0 a 1 
	 * 
	 * @param output -> unos de los valores de salida de la red neuronal
	 * @return double -> El resultado de la formula de valores esperados
	 */
	private double ExpectedValue_PacMan(int output)
	{
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
				//not possible
				return 999999;
		}
	}

	
	//GETTERS
	/**
	 * Devuelve la red neuronal artificial
	 * @return -> red neuronal artificial
	 */
	public Ann GetAnn()
	{
		return ann;
	}
	/**
	 * Devuelve el error de la red neuronal artificial
	 * @return -> error de la red neuronal artificial
	 */
	public double GetError()
	{
		return general_error;
	}
	
	/////////////////////////////////////TESTING METHODS/////////////////////////////////
	/**
	 * Imprime por consola el valor de las neuronas
	 * @param dataset_iteration
	 */
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
	
	/**
	 * Imprime por consola los valores de los pesos de todas las conexiones
	 */
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
	
	/**
	 *  Imprime por consola los valores de los deltas
	 */
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
	/**
	 * Retorna el resultado de la funci�n sigmoide
	 * @param x
	 * @return resultado de la operaci�n
	 */
	public static double Sigmoid(double x) {
	    return (1/( 1 + Math.pow(Math.E,(-1*x))));
	}
	
	//from -1 to 1
	/**
	 * Retorna el resultado de la funci�n hiperb�lica tangencial
	 * @param x
	 * @return resultado de la operaci�n
	 */
	public static double HyperbolicTan(double x) {
	    return ((1 - Math.pow(Math.E,(-2*x))) / (1 + Math.pow(Math.E,(-2*x))));
	}
		
	/**
	 * Retorna el resultado de la funci�n hiperbolica tangencial inversa
	 * @param x
	 * @return resultado de la operaci�n
	 */
	public static double ArcHyperbolicTan(double x) 
	{ 
		return 0.5*Math.log( (x + 1.0) / (x - 1.0) ); 
	} 
}
