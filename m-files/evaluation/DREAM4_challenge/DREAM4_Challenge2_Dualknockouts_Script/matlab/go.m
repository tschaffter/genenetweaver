% This script demonstrates how to call the function
% DREAM4_Challenge2_evaluate_dualknockouts()
%
% Gustavo A. Stolovitzky, Ph.D.
% Adj. Assoc Prof of Biomed Informatics, Columbia Univ
% Mngr, Func Genomics & Sys Biology, IBM  Research
% P.O.Box 218 					Office :  (914) 945-1292
% Yorktown Heights, NY 10598 	Fax     :  (914) 945-4217
% http://www.research.ibm.com/people/g/gustavo
% http://domino.research.ibm.com/comm/research_projects.nsf/pages/fungen.index.html 
% gustavo@us.ibm.com
%
% Robert Prill, Ph.D.
% Postdoctoral Researcher
% Computational Biology Center, IBM Research
% P.O.Box 218
% Yorktown Heights, NY 10598 	
% Office :  914-945-1377
% http://domino.research.ibm.com/comm/research_people.nsf/pages/rjprill.index.html
% rjprill@us.ibm.com
%
% Daniel Marbach
% MIT Computer Science and Artificial Intelligence Laboratory
% dmarbach@mit.edu
% http://gnw.sourceforge.net
%
% Dec 10 2009
%
%% %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

clear all

%% sub_cahllenge_number:
%% 	1 for size 10
%%  2 for size 100

sub_challenge_number = 1;
[ MSE_overall, MSE_of_each_network ] = DREAM4_Challenge2_evaluate_dualknockouts(sub_challenge_number)

sub_challenge_number = 2;
[ MSE_overall, MSE_of_each_network ] = DREAM4_Challenge2_evaluate_dualknockouts(sub_challenge_number)

