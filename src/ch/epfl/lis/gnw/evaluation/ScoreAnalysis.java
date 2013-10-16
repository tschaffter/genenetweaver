package ch.epfl.lis.gnw.evaluation;

import java.util.ArrayList;


public class ScoreAnalysis {
	/** The motif predictions for each network of the batch */
	private ArrayList<Score> scores_ = null;
	
	public ScoreAnalysis(BatchPerformanceEvaluator batch) {
		scores_ = new ArrayList<Score>();		
		for(int i=0;i<batch.getEvaluators().size();i++) {
			scores_.add(batch.getEvaluators().get(i).getScore());
		}
	}
	
	public void run() {
		for(int i=0;i<scores_.size();i++) {
			
			scores_.get(i).run();
		}
	}
	
	public ArrayList<Score> getAllScores() { return scores_; }
}
