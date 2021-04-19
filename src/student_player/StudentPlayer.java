package student_player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import boardgame.Move;
import pentago_twist.PentagoCoord;
import pentago_twist.PentagoPlayer;
import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoMove;

/** A player file submitted by a student. */
public class StudentPlayer extends PentagoPlayer {

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("260846306");
    }
    

    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(PentagoBoardState boardState) {
    	

    	long timeStart = System.currentTimeMillis();

        // You probably will make separate functions in MyTools.
        // For example, maybe you'll need to load some pre-processed best opening
        // strategies...
    	
    	
    	//clone boardstate
    	PentagoBoardState clonePbs = (PentagoBoardState) boardState.clone();
    	
    	//save opponent number
    	int opponent=1-clonePbs.getTurnPlayer();
    	
    	
    	//first and second round
//    	int turn=clonePbs.getTurnNumber();
//    	System.out.println("turn-------------------: "+turn);
    	
    	//*************************One Step Win Check ***********************************
    	if (clonePbs.getTurnNumber()>=4) {
    		
    	//check if can be won in next move 
    	ArrayList<PentagoMove> allNextMoves = clonePbs.getAllLegalMoves();
    	 

    	for (PentagoMove m: allNextMoves) {
    		
            PentagoBoardState b = (PentagoBoardState) clonePbs.clone();

            b.processMove(m);

            if(b.getWinner()==(1-opponent)) {
//            	System.out.println("Win check");
            	return m;
            }           
    	}
	
   	 }  	
    	
    	//**********************Perform MCT*************************************
    	long timeCost=System.currentTimeMillis()-timeStart;
    	
    	Move myMove=Node.MCT(clonePbs,timeCost);
//
//    	if(System.currentTimeMillis()-timeStart<=2000 && turn!=0) {
//    		System.out.println("TIME PASS: "+ (System.currentTimeMillis()-timeStart));
//    	}else {
//    		System.out.println("TIME FAIL***: "+ (System.currentTimeMillis()-timeStart));
//    	}
    	
    	if(myMove==null) {
    		return clonePbs.getRandomMove();
    	}

        return myMove;
    }
}
