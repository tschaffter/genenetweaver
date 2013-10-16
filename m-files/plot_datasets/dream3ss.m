% DREAM3SS    Load and plot the DREAM3 challenge steady-state data.
%             (version 1.0)
%
%    DREAM3SS('name') plots the null-mutant and heterozygous
%    knockdown steady-state data produced by GNW (gnw.sf.net) for the
%    DREAM3 In Silico Challenges using the Matlabe function 'clustergram'.
%    The data must be located in the files named:
%    <name>-null-mutants.tsv
%    <name>-heterozygous.tsv
%
%    DREAM3SS('name', 'type') plots only the specified data types,
%    where 'type' is either 'nm' (null-mutants), 'hz' (heterozygous), or
%    'nm+hz' (both).
%    
%    Detailed documentation of this script can be found in the GNW user
%    guide, which is available at: gnw.sf.net.
%
%    Example: >> dream3ss('InSilicoSize10-Ecoli1-nonoise')

% Copyright (c) 2008 Thomas Schaffter & Daniel Marbach
% 
% We release this software open source under an MIT license (see below). If this
% software was useful for your scientific work, please cite our paper(s) listed
% on http://gnw.sourceforge.net.
% 
% Permission is hereby granted, free of charge, to any person obtaining a copy
% of this software and associated documentation files (the "Software"), to deal
% in the Software without restriction, including without limitation the rights
% to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
% copies of the Software, and to permit persons to whom the Software is
% furnished to do so, subject to the following conditions:
% 
% The above copyright notice and this permission notice shall be included in
% all copies or substantial portions of the Software.
% 
% THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
% IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
% FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
% AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
% LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
% OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
% THE SOFTWARE.

function dream3ss(networkName, type)

    % User settings
    % -------------
    % Extension of the file
    extension = '.tsv';

    % Special Inner settings
    % ----------------------
    % Tag associated to null-mutant data
    tagNm = '-null-mutants';
    % Tag associated to heterozygous data
    tagHz = '-heterozygous';

    % Option to enable null-mutants data
    tagNmType = 'nm';
    % Option to enable heterozygous data
    tagHzType = 'hz';
    % Option to enable both null-mutants and heterozygous datas
    tagNmHzType = 'nm+hz';


    filenameNm = '';
    filenameHz = '';

    if (nargin < 1)
        error('No network name given, see help');
    elseif (nargin < 2)
        type = tagNmHzType;
    end
        
    if strcmp(type,tagNmType) == 1 % null-mutant case
        filenameNm = [networkName tagNm extension];
    elseif strcmp(type,tagHzType) == 1 % heterozygous case
        filenameHz = [networkName tagHz extension];
    elseif strcmp(type,tagNmHzType) == 1 % null-mutants + heterozygous
        filenameNm = [networkName tagNm extension];
        filenameHz = [networkName tagHz extension];
    else
        error('Type must be: nm, hz, or nm+hz.');
    end


    % Open the file and load its content
    % ==================================
    % Found the number of genes and get the header
    % --------------------------------------------

    % 1. Open null-mutants and/or heterozygous files.
    % 2. Extract numGenes and the labels of all genes
    % 3. Load steady-states numerical data
    numGenes = 0;
    labelsStr = 0;
    dataNm = 0;
    dataHz = 0;

    if strcmp(filenameNm,'') ~= 1 % filenameNm is set
        finNm = openfile(filenameNm);
        [numGenes, labelsStr] = extractGeneLabels(finNm);
        dataNm = extractSsData(finNm, numGenes);
    end

    if strcmp(filenameHz,'') ~= 1 % filenameHz is set
        finHz = openfile(filenameHz);
        if numGenes == 0 % Not already load through null-mutants process
            [numGenes, labelsStr] = extractGeneLabels(finHz);
        end
        dataHz = extractSsData(finHz, numGenes);
    end

    % Now, data of null-mutants and/or heterozygous experiments are saved into
    % (N+1) X N matrix, with N=numGenes (N+1 experiments for N genes).
    % -> Concatenation of all available experiments (not showing the wild type twice).
    ssData = 0;

    if strcmp(type,tagNmType) == 1
        ssData = dataNm;
    elseif strcmp(type,tagHzType) == 1
        ssData = dataHz;
    elseif strcmp(type,tagNmHzType) == 1
        % The first line will be the wt, then G1(-/-), G1(+/-), G2(-/-), etc.
        ssData = dataNm(1,:); % the wt
        for i = 2:numGenes+1
            ssData = [ssData; dataNm(i,:); dataHz(i,:)];
        end
    end
    %ssData % Uncomment to see the data


    % Graphs
    % ------
    % labels for the experiments (columns)
    expLabels = 'wt';

    if strcmp(type,tagNmType) == 1
        expLabels = [expLabels;stringAddAppendix(labelsStr, '(-/-)')];
    elseif strcmp(type,tagHzType) == 1
        expLabels = [expLabels;stringAddAppendix(labelsStr, '(+/-)')];
    elseif strcmp(type,tagNmHzType) == 1
        for i = 1:numGenes
            expLabels = [expLabels; labelsStr(i); ' '];
        end
        %expLabels = [expLabels;stringAddAppendix(labelsStr, '(-/-)');...
        %stringAddAppendix(labelsStr, '(+/-)')];
    end

    % convert to log scale
    ssData(ssData < 1e-4) = 1e-4;
    %ssData = log10(ssData);

    figure;
    % transpose because clustergram takes experiments as columns
    heatMapData = ssData';
    % For clustergram, rows correspond to genes and rows to experiments
    clustergram(heatMapData, 'RowLabels', labelsStr, 'ColumnLabels',...
      expLabels, 'Pdist', 'euclidean', 'SymmetricRange', false, 'Colormap', 'jet');

    colorbar('peer',gca,[0.9089 0.07595 0.02122 0.3207]);
    set(gcf, 'Name', 'Steady-state concentration');
    
    figure;
    heatMapData = ssData;
    wt = heatMapData(1,:);
    for i = 1:size(heatMapData,1)
        heatMapData(i,:) = heatMapData(i,:) - wt;
    end
    % transpose because clustergram takes experiments as columns
    heatMapData = heatMapData';
    % For clustergram, rows correspond to genes and rows to experiments
    clustergram(heatMapData, 'RowLabels', labelsStr, 'ColumnLabels',...
        expLabels, 'Pdist', 'euclidean');
    
    colorbar('peer',gca,[0.9089 0.07595 0.02122 0.3207]);
    set(gcf, 'Name', 'Difference to wild-type concentration');

end



% =========================================================================
% SUBFUNCTIONS

% String modifier
% "'Gene1'" -> 'Gene1'
function list = stringRemoveQuotes(input)
    s = size(input, 1);
    for i=1:s
        input(i) = regexprep(input(i), '"', '');
    end
    list = input;
end

% -------------------------------------------------------------------------

% String modifier
% 'Gene1' -> ['Gene1' appendix]
% input is a cellstr array
function input = stringAddAppendix(input, appendix)
    s = size(input, 1);  
    for i=1:s
        input(i) = strcat(input(i),appendix);
    end
end

% -------------------------------------------------------------------------

% Open the file associated to filename and return the stream.
function fin = openfile(filename)

    fin = fopen(filename,'r');
    if fin < 0
       error(['Could not open ',filename,' for input']);
    end
end

% -------------------------------------------------------------------------

% Read the file associated to the stream fin and extract all the experi-
% ments. The layout of the output is a matrix with rows=experiments and
% columns=genes perturbed
function data = extractSsData(fin, numGenes)
    rawData = fscanf(fin,'%c'); % Load the numerical values into one long
                                % vector
    stop = 0;
    index = 1;
    data = '';
    while stop == 0
        [next,rawData] = strtok(rawData); %  parse next column label

        k = findstr(next, '"');

        if isempty(k)
        data(index,1:length(next)) = next; %  append to the labels matrix
        index = index + 1;
        end
        if isempty(next) || length(rawData) < 2
          stop = 1;
        end
    end

    data = cellstr(data);
    data = str2double(data);
    
    nd = length(data); %  total number of data points
    nr = nd/numGenes; %  number of rows; check (next statement) to make sure
    if nr ~= round(nd/numGenes)
       fprintf(1,'\ndata: nrow = %f\tncol = %d\n',nr,numGenes);
       fprintf(1,'number of data points = %d does not equal nrow*ncol\n',nd);
       error('data is not rectangular')
    end

    data = reshape(data,numGenes, nr)'; %  notice the transpose operator
end

% -------------------------------------------------------------------------

% Extract all the gene labels. Output is a cellstr array.
function [numGenes, labelsStr] = extractGeneLabels(fin)

    header = fgetl(fin); % Get the first line, the horizontal header
    maxlen = 0;
    stop = 0;
    numCol = 0;

    while stop == 0
      [next,header] = strtok(header); %  parse next column label
      nextLen = length(next);
      maxlen = max(maxlen,nextLen); %  find the longest so far
      if nextLen == 0
          stop = 1;
      else
         numCol = numCol+1;
      end
    end

    numGenes = numCol-1;
%     sprintf('Number of genes: %d', numGenes)

    labels = blanks(maxlen);
    frewind(fin); % rewind in preparation for actual reading of labels and data
    buffer = fgetl(fin); %  get next line as a string
    for j=1:numCol
      [next,buffer] = strtok(buffer); %  parse next column label
      n = j; %  pointer into the label array for next label
      labels(n,1:length(next)) = next; %  append to the labels matrix
    end

    % Conversion labels(char) -> labels(string)
    labelsStr = cellstr(labels);
    labelsStr = labelsStr(2:size(labelsStr,1),:);
    labelsStr = stringRemoveQuotes(labelsStr);
end
