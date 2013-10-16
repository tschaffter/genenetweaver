% DREAM3TS    Load and plot the DREAM3 challenge time series data.
%             (version 1.0)
%
%    DREAM3TS(name, numClusters, numRows, numCols) plots time series
%    data produced by GNW (gnw.sf.net) for the DREAM3 In Silico Challenges.
%    The data must be located in the files named: <name>-trajectories.tsv
%    Each time series is plotted in a separate figure, the profiles of the
%    genes are clustered into numClusters groups. The clusters are plotted
%    in the same figure on a grid of numRows x numCols.
%     
%    Detailed documentation of this script can be found in the GNW user
%    guide, which is available at: gnw.sf.net.
%
%    Example: >> dream3ts('InSilicoSize50-Ecoli1-nonoise', 10, 2, 5)
%        Plots the time series of the file
%        'InSilicoSize50-Ecoli1-nonoise-trajectories.tsv', clustered into
%        10 groups. The 10 subplots are arranged on a 2 by 5 grid.

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

function dream3ts(networkName, numClusters, numRows, numCols)
% timeseriesDREAM3   Representation of timeseries experimental data,
%                    process them in different cluster and graph them.
%                    (version 05/18/08)
%
%   Example: >> timeseriesDREAM3('s')
%            - 's' name of the network

% Check arguments
if (nargin ~= 4)
    error('Not all four arguments have been specified');
end
if (numRows*numCols < numClusters)
    error('numRows*numCols < numClusters: increase the number of rows or the number of columns to fit all clusters');
end

% Extension of the file
extension = '.tsv';

% Special Inner settings
% ----------------------
% Tag associated to null-mutant data
tagTs = '-trajectories';
% Graph x label
xLabel = 'Time';
% Graph y label
yLabel = 'Expression level';

% plot on log scale
plotLog = 0;

filename = [networkName tagTs extension];


% Open the file and load its content
% ==================================
% Found the number of genes and get the header
% --------------------------------------------

% 1. Open file.
% 2. Extract numGenes and the labels of all genes
% 3. Load time-series numerical data
fin = openfile(filename);
[numGenes,labelsStr] = extractGeneLabels(fin);
% data: first line is first time series of first gene, then comes
% first time series of second gene, etc. for all time series
[timescale,data] = extractTsData(fin, numGenes);

if (plotLog)
    data = log10(data);
end

% the number of time series
numTimeSeries = size(data,1) / numGenes;

% plot every time series in a different figure
for i = 0:numTimeSeries-1
    figure;
    set(gcf, 'Name', ['Time series ' int2str(i+1)]);
    
    % Cluster
    timeSeries = data(i*numGenes+1:(i+1)*numGenes,:);
    corrDist = pdist(timeSeries, 'corr');
    clusterTree = linkage(corrDist, 'average');
    clusters = cluster(clusterTree, 'maxclust', numClusters);

    % Graphs
    for c=1:numClusters
        subplot(numRows,numCols,c);
        hold on;
        if (plotLog)
            ylim([-2 0]);
        else
            ylim([0 1]);
        end
        plot(timescale,timeSeries((clusters == c),:)');
        %xlabel(xLabel);
        %ylabel(yLabel);
        %title(['CLUSTER ' num2str(c)]);
    end
end

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
% ments. The layout of the output data is a (M X TP) with:
% - M the total number of time series curves
% - TP is the number of time point in the time scale
%
% Hypothesis: All experiments have the same time scale.
function [timescale, data] = extractTsData(fin, numGenes)

    rawData = fscanf(fin,'%f'); % Load the numerical values into one long
                                % vector
                                
    % timescale and time-series data are mixed.
    % The output data will not contain the time scale. 
    [timescale, data] = extractTimeScale(rawData, numGenes);
    
    % Set the layout described above for data
    TP = size(timescale,1);
    M = size(data,1)/TP;
    data = reshape(data,TP,M)';
end

% -------------------------------------------------------------------------

% Extract the time scale from the raw data and remove this same time scale
% from the experimental time series data.
% The time scale is only extract from the first experiment (see above
% hypothesis).
function [timescale, data] = extractTimeScale(rawData, numGenes)

    timescale = rawData(1,1);
    stop = 0;
    
    while stop == 0
        index = size(timescale,1);
        potentialTimePoint = rawData(index*(numGenes+1)+1,1);
        if potentialTimePoint ~= 0
            timescale = [timescale;potentialTimePoint];
        else
            stop = 1;
        end
    end
    data = removeTimeScale(rawData,numGenes);
    data = repartitionInCurves(data,numGenes,size(timescale,1));
end

% -------------------------------------------------------------------------

% Finally, data is a vector with time series curves one after one.
function correctData = repartitionInCurves(data,numGenes,numTimePoints)

    numExp = size(data,1)/(numGenes*numTimePoints);
    curve = zeros(numTimePoints,1);
    correctData = zeros(numTimePoints,1);
    
    for e=1:numExp
        expDataIndex = (e-1)*numGenes*numTimePoints+1;
        for g=1:numGenes
            geneDataIndex = expDataIndex+(g-1);
            for pt=1:numTimePoints
               pointDataIndex = geneDataIndex+(pt-1)*(numGenes);
               curve(pt) = data(pointDataIndex,1);
            end
            if e ==1 && g==1
               correctData = curve;
            else
               correctData = [correctData;curve];
            end
        end
    end
end

% -------------------------------------------------------------------------

% Remove the time scale of the raw time-series data.
function data = removeTimeScale(rawData,numGenes)

    numRows = size(rawData,1)/(numGenes+1);
    data = rawData(2:numGenes+1,1);
    
    for i=1:numRows-1
        start = i*(numGenes+1)+2;
        finish = start+numGenes-1;
        data = [data;rawData(start:finish,1)];
    end
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
