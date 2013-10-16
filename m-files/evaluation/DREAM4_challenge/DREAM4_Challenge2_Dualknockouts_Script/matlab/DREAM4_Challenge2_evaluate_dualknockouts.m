function [MSE_overall, MSE] = DREAM4_Challenge2_evaluate_dualknockouts(sub_challenge_number)

% This function computes the mean squared error of the predicted gene 
% expression values for the dual-knockout strains. It returns the 
% MSE for each network, and the overall MSE (mean of the individual
% MSEs).
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

%% %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%% pick a number:
%% 	1 for size 10
%%  2 for size 100
ii = sub_challenge_number;

%% %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%% where the gold standards are located
GOLDDIRS = {'../INPUT/gold_standards/10/',...
			'../INPUT/gold_standards/100/'};

%% where the predictions are located
INDIRS = {'../INPUT/my_predictions/10/',...
		 '../INPUT/my_predictions/100/'};

%% %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

INDIR = INDIRS{ii};
GOLDDIR = GOLDDIRS{ii};

%% %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

files = directory_list(INDIR);

%% for each file
for fi = 1:length(files)

	file = [ INDIR files{fi} ];
	disp([ '  ' files{fi} ])

	%% figure out the name of the network
	[ temp temp2 temp3 ] = fileparts(file);
	fields = lower(strsplit('_',temp2));
	network_name = [ fields{end-2} '_' fields{end-1} ];

	%% figure out the name of the gold standard
	goldfile = [ GOLDDIR 'insilico_' network_name '_nonoise_dualknockouts.tsv' ];
	
	%% load gold standard
	gold_data = load(goldfile);

	%% load predictions
	test_data = load(file);

	%% mean squared error
	sq_err = (test_data - gold_data) .^ 2;
	mse = sum(sum(sq_err)) / numel(sq_err);

	%% remember
	MSE(fi) = mse;

end

MSE_overall = mean(MSE);



function filenames = directory_list(varargin)
%% filenames = directory_list('.','README.txt')

%% set location
if nargin < 1
	there = '.'
else
	there = varargin{1};
end

filenames = {};
directory = dir(there);
j = 1;
for i = 1:size(directory,1)
	entry = directory(i).name;
	if isempty(regexp(entry, '^\.'))
		%% the first char is not a dot "."
  		filenames{j} = entry;
  		j = j+1;
	end
end

%% ignore some files
if nargin > 1
	ignore_list = varargin{2};

	%% strip the files that we want to ignore
	idx = 1:length(filenames);
	ignoreme = [];
	for i = 1:length(ignore_list)
		ignoreme = [ ignoreme strmatch(ignore_list{i},filenames) ];
	end
	keepers = setdiff(idx,ignoreme);
	filenames = filenames(keepers);
end	

function parts = strsplit(splitstr, str, option)
%STRSPLIT Split string into pieces.
%
%   STRSPLIT(SPLITSTR, STR, OPTION) splits the string STR at every occurrence
%   of SPLITSTR and returns the result as a cell array of strings.  By default,
%   SPLITSTR is not included in the output.
%
%   STRSPLIT(SPLITSTR, STR, OPTION) can be used to control how SPLITSTR is
%   included in the output.  If OPTION is 'include', SPLITSTR will be included
%   as a separate string.  If OPTION is 'append', SPLITSTR will be appended to
%   each output string, as if the input string was split at the position right
%   after the occurrence SPLITSTR.  If OPTION is 'omit', SPLITSTR will not be
%   included in the output.

%   Author:      Peter J. Acklam
%   Time-stamp:  2003-10-13 11:09:44 +0200
%   E-mail:      pjacklam@online.no
%   URL:         http://home.online.no/~pjacklam

nargsin = nargin;
error(nargchk(2, 3, nargsin));
if nargsin < 3
   option = 'omit';
else
   option = lower(option);
end

splitlen = length(splitstr);
parts = {};

while 1

   k = strfind(str, splitstr);
   if length(k) == 0
      parts{end+1} = str;
      break
   end

   switch option
      case 'include'
         parts(end+1:end+2) = {str(1:k(1)-1), splitstr};
      case 'append'
         parts{end+1} = str(1 : k(1)+splitlen-1);
      case 'omit'
         parts{end+1} = str(1 : k(1)-1);
      otherwise
         error(['Invalid option string -- ', option]);
   end


   str = str(k(1)+splitlen : end);

end
