%
% This reads a problem (in svmlight format) and
% return label vector and features in sparse matrix.
%

function [lbl, sparsedata] = read_sparse(fname); 

fid = fopen(fname); 
line = 0; 
elements = 0;
lbl = {''}; 
sparsedata = []; 
min_index = 1;

while 1 
	tline = fgetl(fid); 
	if ~ischar(tline), break, end 
	line = line + 1; 
	[label, data] = strtok(tline); 

	lbl{line} = label; 
	[c, v] = strread(data, '%d:%f'); 
	% index is in ascending order
%     c = c + 1;
	if length(c) > 0
		min_index = min(c(1), min_index);
	end
	elements = elements + length(c);
end

frewind(fid);

row = zeros(elements,1);
col = zeros(elements,1);
value = zeros(elements,1);
elements = 1;
for i = 1:line
	tline = fgetl(fid); 
	if ~ischar(tline), break, end 
	[label, data] = strtok(tline); 
    
	lbl{i} = sscanf(label, '%s'); 
	[c, v] = strread(data, '%d:%f');    
%     c = c + 1;
	s = elements;
	e = elements + length(c) - 1;	
	row(s:e, 1) = i * ones(length(c), 1);
	% input format for precomputed kernel has <index> start from 0
	if min_index < 1
		c = c + abs(min_index - 1);
	end
	col(s:e, 1) = c;
	value(s:e, 1) = v;
	elements = elements + length(c);
end

sparsedata = sparse(row, col, value); 

fclose(fid); 
