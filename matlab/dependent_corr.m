% translated from python code https://github.com/psinger/CorrelationStats/blob/master/corrstats.py
% used for p-value of correlation test http://www.philippsinger.info/?p=347

% function corr3 = dependent_corr(xy, xz, yz, n, twotailed=True, conf_level=0.95, method='steiger')
function [score p] = dependent_corr(xy, xz, yz, n, twotailed, conf_level, method)
%     Calculates the statistic significance between two dependent correlation coefficients
%     @param xy: correlation coefficient between x and y
%     @param xz: correlation coefficient between x and z
%     @param yz: correlation coefficient between y and z
%     @param n: number of elements in x, y and z
%     @param twotailed: whether to calculate a one or two tailed test, only works for 'steiger' method
%     @param conf_level: confidence level, only works for 'zou' method
%     @param method: defines the method uses, 'steiger' or 'zou'
%     @return: t and p-val

score = 0;
p = 0;
    if strcmp(method, 'steiger') == true
        d = xy - xz;
        determin = 1 - xy * xy - xz * xz - yz * yz + 2 * xy * xz * yz;
        av = (xy + xz)/2;
        cube = (1 - yz) * (1 - yz) * (1 - yz);

        score = d * sqrt((n - 1) * (1 + yz)/(((2 * (n - 1)/(n - 3)) * determin + av * av * cube)));
        p = 1 - cdf('t', abs(score), n - 3);

        if twotailed == true
            p = p * 2;
        end
    end
    if strcmp(method, 'zou') == true
        [a1 b1] = rz_ci(xy, n, conf_level);
        L1 = a1;
        U1 = b1;
        [a2 b2] = rz_ci(xz, n, conf_level);
        L2 = a2;
        U2 = b2;
        rho_r12_r13 = rho_rxy_rxz(xy, xz, yz);
        lower = xy - xz - power((power((xy - L1), 2) + power((U2 - xz), 2) - 2 * rho_r12_r13 * (xy - L1) * (U2 - xz)), 0.5);
        upper = xy - xz + power((power((U1 - xy), 2) + power((xz - L2), 2) - 2 * rho_r12_r13 * (U1 - xy) * (xz - L2)), 0.5);
        score = lower;
        p = upper;
    end
    