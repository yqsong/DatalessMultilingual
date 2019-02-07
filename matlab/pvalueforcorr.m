function [a b] = rz_ci(r, n, conf_level)% = 0.95
    zr_se = pow(1/(n - 3), .5);
    moe = norm.ppf(1 - (1 - conf_level)/float(2)) * zr_se;
    zu = atanh(r) + moe;
    zl = atanh(r) - moe;
    a = tanh(zl);
    b = tanh(zu);

function b = rho_rxy_rxz(rxy, rxz, ryz)
    num = (ryz-1/2.*rxy*rxz)*(1-pow(rxy,2)-pow(rxz,2)-pow(ryz,2))+pow(ryz,3);
    den = (1 - pow(rxy,2)) * (1 - pow(rxz,2));
    b= num/float(den);

% function corr3 = dependent_corr(xy, xz, yz, n, twotailed=True, conf_level=0.95, method='steiger')
function [score p] = dependent_corr(xy, xz, yz, n, twotailed, conf_level, method)
%     """
%     Calculates the statistic significance between two dependent correlation coefficients
%     @param xy: correlation coefficient between x and y
%     @param xz: correlation coefficient between x and z
%     @param yz: correlation coefficient between y and z
%     @param n: number of elements in x, y and z
%     @param twotailed: whether to calculate a one or two tailed test, only works for 'steiger' method
%     @param conf_level: confidence level, only works for 'zou' method
%     @param method: defines the method uses, 'steiger' or 'zou'
%     @return: t and p-val
%     """
score = 0;
p = 0;
    if strcmp(method, 'steiger') == true
        d = xy - xz;
        determin = 1 - xy * xy - xz * xz - yz * yz + 2 * xy * xz * yz;
        av = (xy + xz)/2;
        cube = (1 - yz) * (1 - yz) * (1 - yz);

        score = d * np.sqrt((n - 1) * (1 + yz)/(((2 * (n - 1)/(n - 3)) * determin + av * av * cube)));
        p = 1 - t.cdf(abs(score), n - 3);

        if twotailed == true
            p = p * 2
        end
    end
    if strcmp(method, 'zou') == true
        L1 = rz_ci(xy, n, conf_level)(0);
        U1 = rz_ci(xy, n, conf_level)(1);
        L2 = rz_ci(xz, n, conf_level)(0);
        U2 = rz_ci(xz, n, conf_level)(1);
        rho_r12_r13 = rho_rxy_rxz(xy, xz, yz);
        lower = xy - xz - pow((pow((xy - L1), 2) + pow((U2 - xz), 2) - 2 * rho_r12_r13 * (xy - L1) * (U2 - xz)), 0.5);
        upper = xy - xz + pow((pow((U1 - xy), 2) + pow((xz - L2), 2) - 2 * rho_r12_r13 * (U1 - xy) * (xz - L2)), 0.5);
        score = lower;
        p = upper;
    end
    
% function [score p] = independent_corr(xy, ab, n, n2 = None, twotailed=True, conf_level=0.95, method='fisher'):
function [score p] = independent_corr(xy, ab, n, n2e, twotailed, conf_level, method)
%     """
%     Calculates the statistic significance between two independent correlation coefficients
%     @param xy: correlation coefficient between x and y
%     @param xz: correlation coefficient between a and b
%     @param n: number of elements in xy
%     @param n2: number of elements in ab (if distinct from n)
%     @param twotailed: whether to calculate a one or two tailed test, only works for 'fisher' method
%     @param conf_level: confidence level, only works for 'zou' method
%     @param method: defines the method uses, 'fisher' or 'zou'
%     @return: z and p-val

    if strcmp(method, 'fisher') == true
        xy_z = 0.5 * np.log((1 + xy)/(1 - xy));
        ab_z = 0.5 * np.log((1 + ab)/(1 - ab));
        if n2 == -1
            n2 = n
        end
        se_diff_r = np.sqrt(1/(n - 3) + 1/(n2 - 3));
        diff = xy_z - ab_z;
        z = abs(diff / se_diff_r);
        p = (1 - norm.cdf(z));
        if twotailed == true
            p = p * 2;
        end
    end
    if strcmp(method, 'zou') == true
        L1 = rz_ci(xy, n, conf_level)(0);
        U1 = rz_ci(xy, n, conf_level)(1);
        L2 = rz_ci(ab, n2, conf_level)(0);
        U2 = rz_ci(ab, n2, conf_level)(1);
        lower = xy - ab - pow((pow((xy - L1), 2) + pow((U2 - ab), 2)), 0.5);
        upper = xy - ab + pow((pow((U1 - xy), 2) + pow((ab - L2), 2)), 0.5);
        score = lower;
        p = upper;
    end
%     else:
%         raise Exception('Wrong method!')