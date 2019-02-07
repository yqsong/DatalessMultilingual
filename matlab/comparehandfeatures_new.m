clear all;
clc

lrldata = importdata('lrlSummary_new.txt');
a = lrldata.data(:,1);
b = max(lrldata.data(:,2:50)');

display('mean and std of original dataless')
meana = mean(a)
stda = std(a)


votingdata = importdata('majority_voting.txt');
v = votingdata.data;

display('mean and std of voting dataless')
meana = mean(v)
stda = std(v)

display('ttest between original and majority_voting dataless')
[h p] = ttest(a, v)

display('correlation between voting and best')
correlation = corr(v, b')



display('correlation between original and best')
correlation = corr(a, b')

display('mean and std of best bridged dataless')
meanb = mean(b)
stdb = std(b)

display('ttest between original and best briged dataless')
[h p] = ttest(a, b')

lrlSim1 = importdata('lrlSimilarities_lingustic_AAAI.txt');
[max1 xIndex] = max(lrlSim1.data(:,2:50)');
mat = lrldata.data(:,2:50);
for i=1:size(xIndex, 2)
    c1(i) = mat(i, xIndex(i));
end

display('mean and std of languistic feature based dataless')
meanc1 = mean(c1)
stdc1 = std(c1)

display('correlation between linguistic and best')
correlation1 = corr(c1', b')

display('ttest between original and linguistic briged dataless')
[h p] = ttest(a, c1')

lrlSim2 = importdata('lrlSimilarities_wikisize_AAAI.txt');%lrlSimilarities2_new.txt
[max2 xIndex] = max(lrlSim2.data(:,2:50)');
mat = lrldata.data(:,2:50);
for i=1:size(xIndex, 2)
    c2(i) = mat(i, xIndex(i));
end

display('mean and std of wikisize based dataless')
meanc2 = mean(c2)
stdc2 = std(c2)

display('correlation between wikisize based and best')
correlation2 = corr(c2', b')

display('ttest between wikisize and original dataless')
[h p] = ttest(a, c2')

display('correlation between wikisize based and linguistic')
correlation12 = corr(c2', c1');

display('correlation ttest between wikisize based and linguistic')
[score p] = dependent_corr(correlation1, correlation2, correlation12, length(c1), true, 0.95, 'steiger')
%0.2420

lrlSim3 = importdata('lrlSimilarities_combinedsize_AAAI.txt');
[max3 xIndex] = max(lrlSim3.data(:,2:50)');
mat = lrldata.data(:,2:50);
for i=1:size(xIndex, 2)
    c3(i) = mat(i, xIndex(i));
end

display('mean and std of combined dataless')
meanc3 = mean(c3)
stdc3 = std(c3)

display('correlation between combined and best')
correlation3 = corr(c3', b')

display('ttest between combined and original dataless')
[h p] = ttest(a, c3')


display('ttest between combined and linguistic dataless')
correlation13 = corr(c1', c3');

display('correlation ttest between combined and wikisize based')
[score p] = dependent_corr(correlation1, correlation3, correlation13, length(c1), true, 0.95, 'steiger')

display('ttest between combined and wikisize dataless')
correlation23 = corr(c2', c3');

display('correlation ttest between combined and wikisize based')
[score p] = dependent_corr(correlation2, correlation3, correlation23, length(c1), true, 0.95, 'steiger')


