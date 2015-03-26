package ch.epfl.lis.gnw.evaluation;

import java.awt.Point;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;





import ch.epfl.lis.gnwgui.NetworkDesktop;

public class Score extends NetworkPrediction {

	
	/** number of link in the goldstandards */
	private int P = 0;
	/** AUPR score */
	private double AUPR_ = -1.0;
	/** AUROC score */
	private double AUROC_ = -1.0;
	/** recall array */
	private double[] recall_ = null;
	/** precision array */
	private double[] precision_ = null;
	/** True Positive Rate array */
	private double[] tpr_ = null;
	/** False Positive Rate array */
	private double[] fpr_ = null;
	/** P-R Points string */
	private String prString_;
	/** ROC Points string */
	private String rocString_;
	
	/** Logger for this class */
	private static Logger log_ = Logger.getLogger(NetworkDesktop.class.getName());
	
	/** Constructor */
	public Score(NetworkPrediction c) {
		
		super();

		copy(c);	
		
		for (int i=0;i<A_.length;i++) {
			for(int j=0;j<A_[i].length;j++) {
				if (A_[i][j])
					P++;
			}
		}
	}

	public void run() {
		
		//log_.log(Level.INFO,this.goldStandard_.getId());
		
		/** Get the prediction sorted by rank */
		TreeMap<Double, Point> mp = new TreeMap<Double, Point>();
		for ( int i=0;i<R_.length;i++) {
			for( int j=0;j<R_[i].length;j++) {
				mp.put(R_[i][j], new Point(i, j));
			}
		}
		
		Set<Map.Entry<Double,Point>> s = mp.descendingMap().entrySet();	
		Iterator<Entry<Double, Point>> it = s.iterator();
		
		// TODO Error if the gold standard includes auto-regulation
		// but here we should be able to cope with that without assumming that
		// there are N*(N-1) genes
		
		Point p = null;
		int TPk = 0,TPL = 0;
		double FPk = 0;
		int k = 0;
		int T = numGenes_*(numGenes_-1); // XXX  
		int N = T - P;
		int L;

		double delta,Ak=0,AL,rh,recL,AUC,AUROC,lc;
		double[] recall = new double[T];
		double[] precision = new double[T];
		double[] tpr = new double[T];
		double[] fpr = new double[T];
		double x1=-1.,y1=-1,x2=-1.,y2=-1.;
		prString_  = "";
		rocString_ = "0,0 ";
		
		while(it.hasNext()) {
			Map.Entry<Double, Point> m = (Map.Entry<Double, Point>)it.next();
			if ( m.getKey() >= 0.) {
				k++;
				p = m.getValue();
				if (A_[p.x][p.y]) {
					TPk++;
					if ( k == 1 )
						delta = 1./((double)P);
					else
						delta = (1.-(double)FPk*Math.log((double)k/(k-1.)))/(double)P;
					
					Ak = Ak + delta;
				}
				else 
					FPk++;
	
				recall[k-1] = TPk/((double)P);
				precision[k-1] = TPk/((double)k);
				tpr[k-1] = recall[k-1];
				fpr[k-1] = ((double)FPk)/((double)N);
				
				if (roundTwoDecimals(recall[k-1]) != x1 || roundTwoDecimals(precision[k-1]) != y1  ) {
					x1 = roundTwoDecimals(recall[k-1]);
					y1 = roundTwoDecimals(precision[k-1]);
					prString_ += x1 + "," + y1 + " ";
				}
				
				if (roundTwoDecimals(fpr[k-1]) != x2 || roundTwoDecimals(tpr[k-1]) != y2 ) {
					x2 = roundTwoDecimals(fpr[k-1]);
					y2 = roundTwoDecimals(tpr[k-1]);
					rocString_ += x2 + "," + y2 + " ";
				}
			}
		}
		
		TPL = TPk;
		L = k;
		if ( L < T)
			rh = (double)(P-TPL)/(double)(T-L);
		else
			rh = 0;
		
		if ( L > 0 )
			recL = recall[L-1];
		else
			recL = 0;
		
		while( TPk < P ) {
			k++;
			TPk++;
			recall[k-1] = (double)TPk/(double)P;
			if ( ((recall[k-1]-recL)*(double)P + L * rh) != 0. )
				precision[k-1] = rh * (double)P * recall[k-1]/(double)((recall[k-1]-recL)*(double)P + (double)L * rh);
			else
				precision[k-1] = 0;
			
			tpr[k-1] = recall[k-1];
			FPk = ((double)TPk * (1.-precision[k-1])/precision[k-1]);
			fpr[k-1] = FPk/(double)N;
			
			
			prString_ += roundTwoDecimals(recall[k-1]) + "," + roundTwoDecimals(precision[k-1]) + " ";
			rocString_ += roundTwoDecimals(fpr[k-1]) + "," + roundTwoDecimals(tpr[k-1]) + " ";
		}
		
		
		AL = Ak;		
		if ( rh != 0 && L != 0)
			AUC = AL + rh * (1. - recL) + rh * (recL -L * rh / P ) * Math.log((L*rh + P* (1. -recL))/(L*rh));
		else if( L == 0)
			AUC = P/T;
		else
			AUC = Ak;
		
		lc = fpr[0] * tpr[0] / 2.;	
		for ( int n=1;n<L+P-TPL;n++)
			lc = lc + (fpr[n]+fpr[n-1]) * (tpr[n] - tpr[n-1]) / 2.;
				
		AUROC = 1. - lc;
		
		log_.log(Level.INFO, String.format("%-35s AUPR = %1.4f AUROC = %1.4f", this.goldStandard_.getId(),  AUC, AUROC) );
		
		AUPR_ = AUC;
		AUROC_ = AUROC;
		recall_ = recall;
		precision_ = precision;
		tpr_ = tpr;
		fpr_ = fpr;
		
		prString_ += " 1,0";
		prString_ = "0,0 0," + precision[0] + " " + prString_;
		rocString_ += "1," + tpr[k-1] +" 1,0";
	}
	
	double roundTwoDecimals(double d) {
//		DecimalFormat twoDForm = new DecimalFormat("#.##");
//		return Double.valueOf(twoDForm.format(d));
		return Double.valueOf(String.format(Locale.ENGLISH, "%.2f", d));
	}
	
	public double getAUPR() { return AUPR_; }
	public double getAUROC() { return AUROC_; }
	public double[] getRecall() { return recall_; }
	public double[] getPrecision() { return precision_; }
	public double[] getTpr() { return tpr_; }
	public double[] getFpr() { return fpr_; }
	public String getPRString() { return prString_; }
	public String getROCString() { return rocString_; }
	
	public String getGoldStandard() { return goldStandard_.getId(); }
}
