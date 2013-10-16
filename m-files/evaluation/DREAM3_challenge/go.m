%
% This script demonstrates how to call the function 
% DREAM3_Challenge3_Evaluate().
%
%
% Gustavo A. Stolovitzky, Ph.D.
% Adj. Assoc Prof of Biomed Informatics, Columbia Univ
% Mngr, Func Genomics & Sys Biology, IBM  Research
% P.O.Box 218 					Office :  (914) 945-1292
% Yorktown Heights, NY 10598 	Fax     :  (914) 945-4217
% http://www.research.ibm.com/people/g/gustavo
% http://domino.research.ibm.com/comm/research_projects.nsf/pages/fungen.index.html 
%
% Robert Prill, Ph.D.
% Postdoctoral Researcher
% Computational Biology Center, IBM Research
% P.O.Box 218
% Yorktown Heights, NY 10598 	
% Office :  914-945-1377
% http://domino.research.ibm.com/researchpeople/rjprill.index.html
%


clear all

%% predictions to be evaluated
testfile = 'predictions/example_ExpressionChallenge.txt';

goldfile = 'gold_standards/DREAM3GoldStandard_ExpressionChallenge.txt';

[score, ...
	overall_gene_profile_pval, ...
	overall_time_profile_pval, ...
	rho_gene_profile, ...
	pval_gene_profile, ...
	rho_time_profile, ...
	pval_time_profile] = DREAM3_Challenge3_Evaluation(testfile, goldfile)
