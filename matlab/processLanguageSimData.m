clear all;

lrldata = importdata('lrlSummary.txt');
lrlSim = importdata('lrlSimilarities3.txt');
a = lrldata.data(:,1);

[max xIndex] = max(lrlSim.data(:,2:49)');
mat = lrldata.data(:,2:49);
for i=1:size(xIndex, 2)
    c(i) = mat(i, xIndex(i));
end
fig = figure();
%figure('XVisual',...
%    '0x11d (TrueColor, depth 24, RGB mask 0xff0000 0xff00 0x00ff)');
%hold on;
% Create axes
axes1 = axes('Parent',fig,'FontSize',20);
box(axes1,'on');
hold(axes1,'all');

% Create plot
plot(a, c', 'MarkerSize',10,'Marker','x','LineWidth',2,'LineStyle','none', 'Color','k');

% Create plot
plot([0:0.01:1],[0:0.01:1],'Color',[0 0 1]);

% Create xlabel
xlabel({'Original CLDDC'},'FontSize',20);

% Create ylabel
ylabel({'Combination Ranked CLDDC'},'FontSize',20);

correlation = corr(a, c')