clear all;
lrldata = importdata('lrlSummary-panlex.txt');
a = lrldata.data(:,1);
b = max(lrldata.data(:,2:49)');

fig = figure();
% fig = figure('XVisual',...
%     '0x11d (TrueColor, depth 24, RGB mask 0xff0000 0xff00 0x00ff)');
%hold on;
% Create axes
axes1 = axes('Parent',fig,'FontSize',20);
box(axes1,'on');
hold(axes1,'all');

% Create plot
plot(a, b', 'MarkerSize',10,'Marker','x','LineWidth',2,'LineStyle','none', 'Color','k');

% Create plot
plot([0:0.01:1],[0:0.01:1],'Color',[0 0 1]);

% Create xlabel
xlabel({'Original CLDDC'},'FontSize',20);

% Create ylabel
ylabel({'Best Bridged CLDDC'},'FontSize',20);

correlation = corr(a, b')

[h p] = ttest(a, b')