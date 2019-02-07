function crossvalidation ()

wikidata = importdata('wikisize.txt');
wikiSizeMap = containers.Map(wikidata.textdata, log(wikidata.data));

lrldata = importdata('lrlSummary_new.txt');
lrlSim = importdata('lrlSimilarities_lingustic_AAAI.txt');
languageFeatureCateMap = importdata('languageFeatureMapping.txt');

featureCateMap = containers.Map(languageFeatureCateMap(:,1),languageFeatureCateMap(:,2))
cateFeatureNum = max(cell2mat(featureCateMap.values));

a = lrldata.data(:,1);
b = max(lrldata.data(:,2:50)');

[maxvalue xIndex] = max(lrlSim.data(:,2:50)');
mat = lrldata.data(:,2:50);
for i=1:size(xIndex, 2)
    cHand(i) = mat(i, xIndex(i));
end
cBestHand = corr(cHand', b')

meancHand = mean(cHand)
stdcHand = std(cHand)

votingdata = importdata('majority_voting.txt');
v = votingdata.data;

c = cHand*0;

[lgNameList, sparseData] = read_sparse('languageFeatures.txt');

numTarget = size(lrldata.data, 1);
numSource = size(lrldata.data, 2);

fold = 5;
wAll = {};
for f = 0:(fold-1)
    fprintf('fold = %f\n', f) 
    indexTrain = 1;
    indexTest = 1;
    languageTrain = 1;
    languageTest = 1;
    for i = 1:numTarget
        [values sortedIndex] = sort(lrldata.data(i, :), 'ascend');
        for j = 1:numSource
            index = sortedIndex(j);
            targetName = lrldata.textdata(i+1, 1);
            if index == 1
                sourceName = targetName;
            else
                sourceName = lrldata.textdata(1, index+1);
            end
            score = lrldata.data(i, index);

            sourceFeatures = findFeatures (lgNameList, sparseData, sourceName);
            targetFeatures = findFeatures (lgNameList, sparseData, targetName);

            feature = zeros(1, cateFeatureNum);
            for k = 1:length(sourceFeatures)
                if sourceFeatures(k) == targetFeatures(k) && sourceFeatures(k) > 0
                    cate = featureCateMap(k);
                    feature(cate) = feature(cate) + 1;
                end
            end
%             feature(cateFeatureNum + 1) = wikiSizeMap(char(sourceName));
%             feature(cateFeatureNum + 2) = wikiSizeMap(char(targetName));
            
            data = {sourceName, targetName, score, feature};
            
            if mod(i, fold) == f
                Xtest(indexTest, :) = [feature];
                if j == 1
                    Ytest{languageTest} = j;
                    sourceTest{languageTest} = sourceName;
                    targetTest{languageTest} = targetName;
                    sourceClassificationValueTest{languageTest} = values(j);
                else
                    Ytest{languageTest} = [Ytest{languageTest}; j];
                    sourceTest{languageTest} = [sourceTest{languageTest}; sourceName];                
                    sourceClassificationValueTest{languageTest} = [sourceClassificationValueTest{languageTest}; values(j)];              
                end
                indexTest = indexTest + 1;
            else 
                Xtrain(indexTrain, :) = [feature];
                if j == 1
                    Ytrain{languageTrain} = j;
                else
                    Ytrain{languageTrain} = [Ytrain{languageTrain}; j];
                end
                indexTrain = indexTrain + 1;
            end
        end
        if mod(i, fold) == f
            languageTest = languageTest + 1;
        else
            languageTrain = languageTrain + 1;
        end
    end
    % Generate the preference pairs; see ranksvm.m for the format of this matrix.
    A = generate_constraints(Ytrain);
    for model=1:7 % Model selection
        opt.lin_cg=1;
        C = 10^(model-3)/size(A,1); % Dividing C by the number of pairs
        w(:,model) = ranksvm(Xtrain, A, C*ones(size(A,1),1),zeros(size(Xtrain,2),1),opt);
        map(model) = compute_map(Xtest*w(:,model),Ytest); % MAP value on the validation set
    end;
    fprintf('C = %f, MAP = %f\n',[10.^[-2:4]; map]) 
    [foo, model] = max(map); % Best MAP value
    w = w(:,model);
    wAll{f+1} = w;
    
    Y = Xtest*w(:);
    ind = 0;
    for i = 1:length(Ytest)
        ind = ind(end)+[1:length(Ytest{i})];
        score = Y(ind);
        [foo,ind2] = sort(score);
        targetLgName = targetTest{i};
        bestIndex = ind2(length(ind2));
        bestSourceLgName = sourceTest{i}(bestIndex);
        bestValue = sourceClassificationValueTest{i}(bestIndex);
        
        for j = 2:size(lrldata.textdata, 1)
           
            if strcmp(targetLgName, lrldata.textdata(j, 1))
                c(j - 1) = bestValue;
            end
        end
    end
    
    clear Xtrain Xtest Ytrain Ytest w Y map;
end

waverage = wAll{1};
for ii = 2:fold
    waverage = waverage + wAll{ii};
end
waverage = waverage/fold;
waverageAbs = 0-abs(waverage);
[value ind] = sort(waverageAbs);
for jj = 1:10
    ind(jj)
    waverage(ind(jj))
end

c;
fig = figure();
% fig = figure('XVisual',...
%     '0x11d (TrueColor, depth 24, RGB mask 0xff0000 0xff00 0x00ff)');
% Create axes
axes1 = axes('Parent',fig,'FontSize',20);
box(axes1,'on');
hold(axes1,'all');

% Create plot
plot(c, b, 'MarkerSize',10,'Marker','x','LineWidth',2,'LineStyle','none');

% Create plot
plot([0:0.01:1],[0:0.01:1],'Color',[1 0 0]);

% Create xlabel
xlabel({'Dataless with RankSVM Predicted Bridge Languages'},'FontSize',20);

% Create ylabel
ylabel({'Dataless with Best Bridge Languages'},'FontSize',20);

display('correlation between best and RankSVM')
cBestRankSVM = corr(c', b')
display('correlation between best and hand features')
cBestHand = corr(cHand', b');
display('correlation between RankSVM and hand features')
cRankSVMHand = corr(cHand', c');

display('mean and std of RankSVM')
meanc = mean(c)
stdc = std(c)

display('ttest between RankSVM and original dataless')
[h p] = ttest(a, c')

display('ttest between RankSVM and voting')
[h p] = ttest(v, c')

display('correlatioin ttest between RankSVM and hand')
[score p] = dependent_corr(cBestRankSVM, cBestHand, cRankSVMHand, length(c), true, 0.95, 'steiger')
%[score p] = dependent_corr(cBestRandSVM, cBestHand, cRankSVMHand, length(c), true, 0.95, 'zou')

% dependent_corr(0.9604, 0.9072, 0.8942, 39, True, 0.95, 'steiger')
% Out[9]: (2.6536568130348939, 0.01177650616725967)
% 
% dependent_corr(0.9604, 0.9072, 0.8942, 39, True, 0.95, 'zou')
% Out[10]: (0.01233679189377155, 0.12445017333127054)


function feature = findFeatures (lgNameList, sparseData, lgName)
for i = 1:size(lgNameList, 2)
    if strcmp(lgNameList{i}, char(lgName))
        feature = sparseData(i, :);
    end
end

function write_out(output,i,name,dataset)
  output = output + 1e-10*randn(length(output),1);  % Break ties at random
  fname = [dataset '/ranksvm/' name '.fold' num2str(i)];
  save(fname,'output','-ascii');
  % Either copy the evaluation script in the current directory or
  % change the line below with the correct path 
  system(['perl Eval-Score-3.0.pl ' dataset '/Fold' num2str(i) '/' name ...
          '.txt ' fname ' ' fname '.metric 0']);

function A = generate_constraints(Y)
  nq = length(Y);
  
  I=zeros(1e7,1); J=I; V=I; nt = 0;
  
  ind = 0;
  for i=1:nq
    ind = ind(end)+[1:length(Y{i})]';
    Y2 = Y{i};
    n = length(ind);
    [I1,I2] = find(repmat(Y2,1,n)>repmat(Y2',n,1));
    n = length(I1);
    I(2*nt+1:2*nt+2*n) = nt+[1:n 1:n]'; 
    J(2*nt+1:2*nt+2*n) = [ind(I1); ind(I2)];
    V(2*nt+1:2*nt+2*n) = [ones(n,1); -ones(n,1)];
    nt = nt+n;
  end;
  A = sparse(I(1:2*nt),J(1:2*nt),V(1:2*nt));    

function map = compute_map(Y,Yt)
%   ind = 0;
%   for i=1:length(Yt)
%     ind = ind(end)+[1:length(Yt{i})];
%     [foo,ind2] = sort(-Y(ind));
%     r = Yt{i}(ind2)>0;
%     p = cumsum(r) ./ [1:length(r)]';
%     if sum(r)> 0 
%       map(i) = r'*p / sum(r);
%     else
%       map(i)=0;
%     end;
%   end;
%   map=mean(map);

ind = 0;
for i = 1:length(Yt)
    ind = ind(end)+[1:length(Yt{i})];
    score = Y(ind);
    [foo,ind2] = sort(score);
    map(i) = corr(Yt{i}, ind2, 'type', 'Spearman');
end
map=mean(map);

