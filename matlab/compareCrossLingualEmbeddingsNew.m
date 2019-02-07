clear all;
%lrldata = importdata('compareMUSEembeddings.txt');
lrldata = importdata('compare-sew-embeddings.txt');

%lrldata = importdata('compare-sew-vectors.txt');
a = lrldata(:,1);
b = lrldata(:,2);

fig = figure();
% fig = figure('XVisual',...
%     '0x11d (TrueColor, depth 24, RGB mask 0xff0000 0xff00 0x00ff)');
%hold on;
% Create axes
axes1 = axes('Parent',fig,'FontSize',20);
box(axes1,'on');
hold(axes1,'all');

% Create plot
plot(a, b, 'MarkerSize',10,'Marker','x','LineWidth',2,'LineStyle','none', 'Color','k');

% Create plot
plot([0:0.01:1],[0:0.01:1],'Color',[0 0 1]);

% Create xlabel
xlabel({'SEW-Vectors'},'FontSize',20);

% Create ylabel
ylabel({'CLESA'},'FontSize',20);

correlation = corr(a, b)

[h p] = ttest(a, b)

meana= mean(a)
meanb= mean(b)
