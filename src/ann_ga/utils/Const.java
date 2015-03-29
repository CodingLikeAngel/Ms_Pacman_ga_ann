package ann_ga.utils;

public interface Const {
	public enum Activation {
	    UMBRAL,
	    SIGMOID,
	    TANH
	}
	
	public enum EvalType {
	    FITNESS,
	    EARLY_STOP
	}
	
	public enum Purpose {
	    XOR,
	    PACMAN
	}
	
	//DEBUG
	public static final boolean		DEBUG 						= false;
	
	//NEURON
	public static final int 		INPUTS 						= 8;				//XOR -> 2
	public static final int 		HIDDEN 						= 2*INPUTS + 1;		//XOR -> 1
	public static final int 		OUPUTS 						= 4;				//XOR -> 1
	
	//TRAINING
	public static final Activation	AFUNC 						= Activation.TANH;
	public static final int			TRAININGS					= 3000;
	public static final double		LEARN_FACTOR 				= 0.1;
	
	//EVALUATION
	public static final EvalType	ETYPE 						= EvalType.EARLY_STOP;
	public static final double		FITNESS 					= 0.01;
	public static final int 		RANGE 						= 100;
	
	//DATASET
	public static final int 		MIN 						= 0;
	public static final int 		SETS 						= 1;		//PACMAN -> 1		XOR -> 4
	public static final boolean 	BINARY 						= false;	//PACMAN -> false	XOR -> true
	
	//PURPOSE
	public static final Purpose		PURPOSE 					= Purpose.PACMAN;
	
	//GA
	public static final int			GA_LOOPS					= 500;
	public static final int			INDIVIDUALS					= 25;
	public static final int			SELEC_TURN_PRESELECTION		= 12;
	public static final int			SELEC_PARENTS				= 2;
	public static final int			CROSS_CHILDREN				= 2;
	public static final double		MUTATION_PROB				= 0.05;
}
