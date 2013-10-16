function [score, ...
	overall_gene_profile_pval, ...
	overall_time_profile_pval, ...
	rho_gene_profile, ...
	pval_gene_profile, ...
	rho_time_profile, ...
	pval_time_profile] = DREAM3_Challenge3_Evaluation(testfile, goldfile)
%
% This function evaluates the accuracy of a prediction compared 
% to a gold standard.
% 
% USAGE: [score, ...
%	overall_gene_profile_pval, ...
%	overall_time_profile_pval, ...
%	rho_gene_profile, ...
%	pval_gene_profile, ...
%	rho_time_profile, ...
%	pval_time_profile] = DREAM3_Challenge3_Evaluation(goldfile, testfile)
%
% INPUTS:
% 	testfile:	the file containing the predictions to be evaluated
%	goldfile:	the gold standard
%
% OUTPUTS: 
%	overall_gene_profile_pval:	geometric mean of the gene-profile p-values
%	overall_time_profile_pval:	geometric mean of the time-profile p-values
%	rho_gene_profile:		Spearman correlation coeff of gene-profiles
%	pval_gene_profile:		p-value of Spearman correlation coeff
%	rho_time_profile:		Spearman correlation coeff of time-profiles
%	pval_time_profile:		p-value of Spearman correlation coeff
%
% A "time-profile" is a vector of all genes at a given time.
% A "gene-profile" is a vector of all times for a given gene.
%
% All further information about the actual calculations can be found elsewhere.

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


%% read and parse files
[G, g_col_names, g_probe_id, g_gene_name] = read_challenge3_file(goldfile);
[T, col_names, probe_id, gene_name] = read_challenge3_file(testfile);
	
%% column correlation
for ri = 1:size(G,2)
	g = G(:,ri);
	t = T(:,ri);
	[rho, pval] = corr(g,t,'type','Spearman','tail','gt');  % rho greater than zero
	rho_col(ri) = rho;
	pval_col(ri) = pval;
end

%% row correlation
for ci = 1:size(G,1)
	g = G(ci,:)';
	t = T(ci,:)';
	[rho, pval] = corr(g,t,'type','Spearman','tail','gt');  % re-ranks before computing corr coeff 
	rho_row(ci) = rho;
	pval_row(ci) = pval;
end

%% pretty names
%% gene profile:  all genes at a given time-point (a col)
%% time profile:  a single gene across all times (a row)
rho_gene_profile = rho_col;
rho_time_profile = rho_row;
pval_gene_profile = pval_col;
pval_time_profile = pval_row;


%% %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% summary statistics

%% overall pval is the geometric mean
N = length(pval_time_profile);
overall_time_profile_pval = exp(sum(log(pval_time_profile))/N);

N = length(pval_gene_profile);
overall_gene_profile_pval = exp(sum(log(pval_gene_profile))/N);

%% SCORE
score = sum(-log10([ overall_time_profile_pval overall_gene_profile_pval ]))/2;


%% %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% helper functions

function [A, col_names, probe_id, gene_name] = read_challenge3_file(file)

	d = importdata(file);
	col_names = d.textdata(1,3:end);	% first row
	probe_id = d.textdata(2:end,1); 	% first col
	gene_name = d.textdata(2:end,2); 	% second col
	A = d.data;
	
	%% strip blanks from names
	gene_name = regexprep(gene_name, ' $', '');

end

end
