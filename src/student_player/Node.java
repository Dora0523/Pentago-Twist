package student_player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import boardgame.Board;
import boardgame.Move;
import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoMove;


public class Node { 
		public static long time;
		
	   	Node parent;
	   	
	   	ArrayList<Node> children = new ArrayList<Node>();
	  	
	   	double wins=0.0;
	   	double visits=0.0;
	   	
	   	/* 
	   	 * 0-> not expanded
	   	 * -1-> fully expanded
	   	 * x-> expanded halfway stopping at x
	   	 */
	   	int expansion=0;  

	   	PentagoMove nMove;
	  	PentagoBoardState nState;

	  	//initialize	
	  	Node (Node parent, PentagoBoardState state,PentagoMove move){
	  		this.parent=parent;
	  		this.nMove=move;
	  		this.nState=state;
	  		
	  	}
	    
	  	//getters 
	  	double getWins() {return this.wins;}
	  	double getVisits() {return this.visits;}
	  	
	  	ArrayList<Node> getChildren() {return this.children;}
	    Node getParent() {return this.parent;}
	    PentagoBoardState getState() {return this.nState;}
	    PentagoMove getMove() { return this.nMove;}
	    
	    double getUCT() {

	    	if (this.getVisits()==0.0){
	    		
	    	//if the node have not been visited,
	    	// return the unvisited nodes first
	    		return (double)Integer.MAX_VALUE;
	    		
	    	}else {    	
	    	double p=this.getVisits();
	    	double uct=this.getWins()/p + 1.41*Math.sqrt(Math.log(this.parent.getVisits())/p);	    	
	    	return uct;
	    	}
	    }
	    
	    
	    //setters
	    void setChild(Node n) {
	    	this.children.add(n);
	    }
	    
	    
public static Node selection(Node root) { 		

	Node n = root;
	n.visits++;

	//tree policy, select most promising node by uct scores
	
	//n is fully expanded
	while(n.expansion==-1) {
		
		//select child with best uct that is closest to leaf
		n=selectBestChild(n,false);
		
		//update n.visits
		n.visits++;

	}

	return n;
}

public static void expansion(Node n) {  
	//expand selected node

	ArrayList<PentagoMove> allMoves = n.getState().getAllLegalMoves();

	int halfExpand=0;	
	
	int startMove=n.expansion;
	PentagoBoardState state=n.getState();
	
	
		for (int i=startMove; i<allMoves.size();i++) {
			
			//check time constraint
			if(System.currentTimeMillis()+100>=time) {
				n.expansion=i+1;
				halfExpand=1;

				break;
			}
						
			PentagoMove m=allMoves.get(i);
			
			//new state

			PentagoBoardState newState = (PentagoBoardState)state.clone();

			newState.processMove(m);


			//For newNode
			  // add parent, board, move
			Node newNode = new Node(n,newState,m);
			
			//For Node n
			  // add new child
			n.setChild(newNode);
	
	}
		
		//all expansion finished
		if(halfExpand==0) {n.expansion=-1;}

}

public static double simulation(Node n,int opponent) {
	//simulate result randomly
	
	PentagoBoardState tempState=n.getState();
	Node tempN = new Node(n,tempState,n.getMove());
	int winner=tempState.getWinner();
	double score;
	
	
	while (!tempState.gameOver() && tempState.getAllLegalMoves().size() >0) {
		
		if(System.currentTimeMillis()+5>=time) {
			return -1.0;
		}
		
		tempState.processMove((PentagoMove)tempState.getRandomMove());
		
	}
	
	winner=tempState.getWinner();
	
	if(winner==opponent) {
		score=0.0;  //lose
	}else if(winner == Board.NOBODY){
		score=0.5;  //tied
	}else {
		score=1.0;   //win
	}
	
	return score;
}

public static void backPropagation(Node n,double score) {
	
	//add visits/success to all the parents of node n
	
	while (n.getParent()!=null) {
		n.visits=n.visits+1;
		n.wins=n.wins+score;
		
		n=n.getParent();
	}
}

public static boolean opponentWin(Node n) {
	if (n==null) {
		return true;
	}
	//*************************One Step lose Check ***********************************
	//check if opponent will win in one step if node N is processed
	long start=System.currentTimeMillis();
		//cloneOpponent = state when move n is placed for student player
		int opponent=n.getState().getTurnPlayer();
		PentagoBoardState cloneOpponent = (PentagoBoardState)n.getState().clone();	
		
		//all possible moves for opponent
		ArrayList<PentagoMove> allNextOpponentMoves = cloneOpponent.getAllLegalMoves();
	    
		
		for (PentagoMove m: allNextOpponentMoves) {
	
	        PentagoBoardState bOp = (PentagoBoardState) cloneOpponent.clone();
	        bOp.processMove(m);
	        
	        //if opponent can win in next step, return true --> change bestNode
	        if(bOp.getWinner()==(opponent)) {	        	
	        	return true;
	        }

	        }
	return false;
}
public static Node selectBestChild(Node n,boolean finalCheck) {  
	//input node, return child of node with best uct scores
	Node bestN=null;
	double bestUCT=-1.0;
	
	//****** make arraylist with component (Node,uct) ---> sort by uct
	
	//no final check
	if(finalCheck==false) {
		//loop through all children
		for (Node i: n.getChildren()) {
			
			//get child node with best UCT score
			if(i.getUCT()>bestUCT) {					
			bestN=i;
			bestUCT=bestN.getUCT();
			}
		}
	}
	
	//final check	
	else {
		//sort arraylist of children by uct
		
		ArrayList<Node> children=n.getChildren();
		if(children.size()==0) {
			return n;
		}
		
		Collections.sort(n.getChildren(), Comparator.comparing(c -> c.getUCT()));
		
		Node max=null;
		Node t=null;

		
		//keep getting child with best uct if the top one causes opponent win
		while(children.size()>0 && opponentWin(bestN)==true) {
			t=children.remove(0);
			if(t.getUCT()==(double)Integer.MAX_VALUE) {
				max=t;
			}else {
				bestN=t;
			}

		}
		
		if(bestN==null) {
			if(max!=null) {
				bestN=max;
			}
		}
	}
	
	return bestN;
}

public static Node selectRandomChild(Node n) {
	int childrenSize=n.getChildren().size();
    int selectRandom = (int) (Math.random() * childrenSize);
    
    return n.getChildren().get(selectRandom);

	
    }

public static Move MCT(PentagoBoardState pbs,long timeCost) { 		
	//declare root node
	Node root= new Node(null,pbs,null);
	Node nodeSelect=null;
	Node nodeExplore=null;
	
	int opponent=1-pbs.getTurnNumber();
	
	//set time constraint and opponent win check
	
	//default: turnNumber >=4
	long timeConstraint=1850;
	boolean OpponentCheck=true;
	
	//first round
	if(root.getState().getTurnNumber()==0) {
		OpponentCheck=false;
		timeConstraint=20000;

	}
	//second and third round
	else if(root.getState().getTurnNumber()<4) {
		OpponentCheck=false;}
	
	
	time=System.currentTimeMillis()+timeConstraint-timeCost;	
	
	//*************************************
	//explore nodes within time constraint
	
		
	/******* SELECTION */

	//select node by tree policy
	nodeSelect = selection(root);
		

	/******** EXPANSION */

	//if not gameover, expand nodeSelect 
		
		
	if (nodeSelect.getState().gameOver() != true) {
		expansion(nodeSelect);
	}
		
	while (System.currentTimeMillis() - time > 0){
	// within time constraint, run random simulations from selected node and update UCT
		
		/********* SIMULATION */
		nodeExplore = nodeSelect;
		
		if (nodeSelect.children.size()>0) {
			nodeExplore=selectRandomChild(nodeExplore);	
		}

		//random simulation of selected node
		double score=simulation(nodeExplore,opponent);

		/********** BACK PROPAGATION */
		backPropagation(nodeExplore,score);
		
	}
	// select child node with best UCT score
	Node bestNode = Node.selectBestChild(root,OpponentCheck);

	return bestNode.getMove();

}



}
