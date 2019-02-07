% translated from python code https://github.com/psinger/CorrelationStats/blob/master/corrstats.py
% used for p-value of correlation test http://www.philippsinger.info/?p=347

% function [score p] = independent_corr(xy, ab, n, n2 = None, twotailed=True, conf_level=0.95, method='fisher'):
function [score p] = independent_corr(xy, ab, n, n2e, twotailed, conf_level, method)
%     Calculates the statistic significance between two independent correlation coefficients
%     @param xy: correlation coefficient between x and y
%     @param xz: correlation coefficient between a and b
%     @param n: number of elements in xy
%     @param n2: number of elements in ab (if distinct from n)
%     @param twotailed: whether to calculate a one or two tailed test, only works for 'fisher' method
%     @param conf_level: confidence level, only works for 'zou' method
%     @param method: defines the method uses, 'fisher' or 'zou'
%     @return: z and p-val

score = 0;
p = 0;
    if strcmp(method, 'fisher') == true
        xy_z = 0.5 * np.log((1 + xy)/(1 - xy));
        ab_z = 0.5 * np.log((1 + ab)/(1 - ab));
        if n2 == -1
            n2 = n
        end
        se_diff_r = sqrt(1/(n - 3) + 1/(n2 - 3));
        diff = xy_z - ab_z;
        score = abs(diff / se_diff_r);
        p = (1 - cdf('norm', score));
        if twotailed == true
            p = p * 2;
        end
    end
    if strcmp(method, 'zou') == true
        [a1 b1] = rz_ci(xy, n, conf_level);
        L1 = a1;
        U1 = b1;
        [a2 b2] = rz_ci(ab, n, conf_level);
        L2 = a2;
        U2 = b2;
        
        lower = xy - ab - power((power((xy - L1), 2) + power((U2 - ab), 2)), 0.5);
        upper = xy - ab + power((power((U1 - xy), 2) + power((ab - L2), 2)), 0.5);
        score = lower;
        p = upper;
    end
%     else:
%         raise Exception('Wrong method!'