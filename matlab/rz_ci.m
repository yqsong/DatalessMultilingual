% translated from python code https://github.com/psinger/CorrelationStats/blob/master/corrstats.py
% used for p-value of correlation test http://www.philippsinger.info/?p=347
function [a b] = rz_ci(r, n, conf_level)% = 0.95
    zr_se = power(1/(n - 3), .5);
    moe = pdf('norm', 1 - (1 - conf_level)/(2 + eps('single')), 0, 1) * zr_se;
    zu = atanh(r) + moe;
    zl = atanh(r) - moe;
    a = tanh(zl);
    b = tanh(zu);