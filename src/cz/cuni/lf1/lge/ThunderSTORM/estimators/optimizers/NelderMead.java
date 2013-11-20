package cz.cuni.lf1.lge.ThunderSTORM.estimators.optimizers;

import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.sqr;
import org.apache.commons.math3.analysis.MultivariateFunction;

public class NelderMead {

    public double[] xmin;   // estimated parameters
    public double ynewlo;   // minimal value
    public int numres;      // number of restarts
    public int icount;      // number of evaluations of the objective function
    public int ifault;      // error flag; 0 -> no error
    
    private int minmax;     // this can be either +1 or -1; the purpose is to provide
                            // both minimization (+1) and maximization (-1)
    private MultivariateFunction fn;

    public static enum Objective {

        MINIMIZE(+1), MAXIMIZE(-1);
        private int minmax;

        private Objective(int minmax) {
            this.minmax = minmax;
        }

        public int toInteger() {
            return minmax;
        }
    }
    
    public void optimize(MultivariateFunction fn, Objective obj, double[] start, double reqmin, double[] step, int konvge, int kcount) {
        minmax = obj.toInteger();
        this.fn = fn;
        nelmin(start.length, start, reqmin, step, konvge, kcount);
    }
    
    public double getValue(double [] point) {
        return minmax*fn.value(point);
    }

    /**
     * NELMIN minimizes a function using the Nelder-Mead algorithm.
     *
     * Discussion:
     *
     * This routine seeks the minimum value of a user-specified function.
     *
     * Simplex function minimisation procedure due to Nelder+Mead(1965), as
     * implemented by O'Neill(1971, Appl.Statist. 20, 338-45), with subsequent
     * comments by Chambers+Ertel(1974, 23, 250-1), Benyon(1976, 25, 97) and
     * Hill(1978, 27, 380-2)
     *
     * This routine does not include a termination test using the fitting of a
     * quadratic surface.
     *
     * Licensing:
     *
     * This code is distributed under the GNU LGPL license.
     *
     * Modified:
     *
     * 27 October 2011
     *
     * Author:
     *
     * Original FORTRAN77 version by R ONeill. MATLAB version by John Burkardt.
     * Java version by Martin Ovesny.
     *
     * Reference:
     *
     * John Nelder, Roger Mead, A simplex method for function minimization,
     * Computer Journal, Volume 7, 1965, pages 308-313.
     *
     * R ONeill, Algorithm AS 47: Function Minimization Using a Simplex
     * Procedure, Applied Statistics, Volume 20, Number 3, 1971, pages 338-345.
     *
     * Parameters:
     *
     * Input, real value = FN ( X ), the name of the MATLAB function which
     * evaluates the function to be minimized, preceded by an "
     *
     * @" sign.
     *
     * Input, integer N, the number of variables.
     *
     * Input, real START(N). On input, a starting point for the iteration. On
     * output, this data may have been overwritten.
     *
     * Input, real REQMIN, the terminating limit for the variance of function
     * values.
     *
     * Input, real STEP(N), determines the size and shape of the initial
     * simplex. The relative magnitudes of its elements should reflect the units
     * of the variables.
     *
     * Input, integer KONVGE, the convergence check is carried out every KONVGE
     * iterations.
     *
     * Input, integer KCOUNT, the maximum number of function evaluations.
     *
     * Output, real XMIN(N), the coordinates of the point which is estimated to
     * minimize the function.
     *
     * Output, real YNEWLO, the minimum value of the function.
     *
     * Output, integer ICOUNT, the number of function evaluations.
     *
     * Output, integer NUMRES, the number of restarts.
     *
     * Output, integer IFAULT, error indicator. 0, no errors detected. 1,
     * REQMIN, N, or KONVGE has an illegal value. 2, iteration terminated
     * because KCOUNT was exceeded without convergence.
     */
    private void nelmin(int n, double[] start, double reqmin, double[] step, int konvge, int kcount) {
        xmin = new double[n];
        ynewlo = 0.0;
        icount = 0;
        numres = 0;

        double ccoeff = 0.5;
        double ecoeff = 2.0;
        double eps = 0.001;
        double rcoeff = 1.0;
        //
        // Check the input parameters.
        //
        if(reqmin <= 0.0) {
            ifault = 1;
            return;
        }

        if(n < 1) {
            ifault = 1;
            return;
        }

        if(konvge < 1) {
            ifault = 1;
            return;
        }

        int jcount = konvge;
        int nn = n + 1;
        double dn = n;
        double dnn = nn;
        double del = 1.0;
        double rq = reqmin * dn;

        double[][] p = new double[n][nn];
        double[] y = new double[nn];
        double[] pbar = new double[n];
        double[] pstar = new double[n];
        double[] p2star = new double[n];
        double ystar, y2star;

        //
        // Initial or restarted loop.
        //
        while(true) {
            
            for(int i = 0; i < n; i++) {
                p[i][nn - 1] = start[i];
            }

            y[nn - 1] = getValue(start);
            icount++;

            for(int j = 0; j < n; j++) {
                double x = start[j];
                start[j] = start[j] + step[j] * del;
                for(int i = 0; i < n; i++) {
                    p[i][j] = start[i];
                }
                y[j] = getValue(start);
                icount++;
                start[j] = x;
            }
            //
            // The simplex construction is complete.
            //
            // Find highest and lowest Y values.  YNEWLO = Y(IHI) indicates
            // the vertex of the simplex to be replaced.
            //
            double ylo = y[0];
            int ilo = 0;
            for(int i = 1; i < nn; i++) {
                if(y[i] < ylo) {
                    ylo = y[i];
                    ilo = i;
                }
            }
            //
            // Inner loop.
            //
            while(true) {
                
                if(kcount <= icount) {
                    break;
                }

                ynewlo = y[0];
                int ihi = 0;
                for(int i = 1; i < nn; i++) {
                    if(ynewlo < y[i]) {
                        ynewlo = y[i];
                        ihi = i;
                    }
                }
                //
                // Calculate PBAR, the centroid of the simplex vertices
                // excepting the vertex with Y value YNEWLO.
                //
                for(int i = 0; i < n; i++) {
                    double z = 0.0;
                    for(int j = 0; j < nn; j++) {
                        z = z + p[i][j];
                    }
                    z = z - p[i][ihi];
                    pbar[i] = z / dn;
                }
                //
                //  Reflection through the centroid.
                //
                for(int i = 0; i < n; i++) {
                    pstar[i] = pbar[i] + rcoeff * (pbar[i] - p[i][ihi]);
                }
                ystar = getValue(pstar);
                icount++;
                //
                // Successful reflection, so extension.
                //
                if(ystar < ylo) {
                    for(int i = 0; i < n; i++) {
                        p2star[i] = pbar[i] + ecoeff * (pstar[i] - pbar[i]);
                    }
                    y2star = getValue(p2star);
                    icount++;
                    //
                    // Check extension.
                    //
                    if(ystar < y2star) {
                        for(int i = 0; i < n; i++) {
                            p[i][ihi] = pstar[i];
                        }
                        y[ihi] = ystar;
                    //
                    // Retain extension or contraction.
                    //
                    } else {
                        for(int i = 0; i < n; i++) {
                            p[i][ihi] = p2star[i];
                        }
                        y[ihi] = y2star;
                    }
                //
                // No extension.
                //
                } else {
                    int l = 0;
                    for(int i = 0; i < nn; i++) {
                        if(ystar < y[i]) {
                            l++;
                        }
                    }

                    if(l > 1) {
                        for(int i = 0; i < n; i++) {
                            p[i][ihi] = pstar[i];
                        }
                        y[ihi] = ystar;
                    //
                    // Contraction on the Y(IHI) side of the centroid.
                    //
                    } else if(l < 1) {
                        for(int i = 0; i < n; i++) {
                            p2star[i] = pbar[i] + ccoeff * (p[i][ihi] - pbar[i]);
                        }
                        y2star = getValue(p2star);
                        icount++;
                        //
                        // Contract the whole simplex.
                        //
                        if(y[ihi] < y2star) {
                            for(int j = 0; j < nn; j++) {
                                for(int i = 0; i < n; i++) {
                                    p[i][j] = (p[i][j] + p[i][ilo]) * 0.5;
                                    xmin[i] = p[i][j];
                                }
                                y[j] = getValue(xmin);
                                icount++;
                            }
                            ylo = y[0];
                            ilo = 0;
                            for(int i = 1; i < nn; i++) {
                                if(y[i] < ylo) {
                                    ylo = y[i];
                                    ilo = i;
                                }
                            }
                            continue;
                        //
                        // Retain contraction.
                        //
                        } else {
                            for(int i = 0; i < n; i++) {
                                p[i][ihi] = p2star[i];
                            }
                            y[ihi] = y2star;
                        }
                    //
                    // Contraction on the reflection side of the centroid.
                    //
                    } else if(l == 1) {
                        for(int i = 0; i < n; i++) {
                            p2star[i] = pbar[i] + ccoeff * (pstar[i] - pbar[i]);
                        }
                        y2star = getValue(p2star);
                        icount = icount + 1;
                        //
                        // Retain reflection?
                        //
                        if(y2star <= ystar) {
                            for(int i = 0; i < n; i++) {
                                p[i][ihi] = p2star[i];
                            }
                            y[ihi] = y2star;
                        } else {
                            for(int i = 0; i < n; i++) {
                                p[i][ihi] = pstar[i];
                            }
                            y[ihi] = ystar;
                        }
                    }
                }
                //
                // Check if YLO improved.
                //
                if(y[ihi] < ylo) {
                    ylo = y[ihi];
                    ilo = ihi;
                }
                jcount--;
                if(jcount > 0) {
                    continue;
                }
                //
                // Check to see if minimum reached.
                //
                if(icount <= kcount) {
                    jcount = konvge;
                    double z = 0.0;
                    for(int i = 0; i < nn; i++) {
                        z = z + y[i];
                    }
                    double x = z / dnn;
                    z = 0.0;
                    for(int i = 0; i < nn; i++) {
                        z = z + sqr(y[i] - x);
                    }
                    if(z <= rq) {
                        break;
                    }
                }
            }
            //
            // Factorial tests to check that YNEWLO is a local minimum.
            //
            for(int i = 0; i < n; i++) {
                xmin[i] = p[i][ilo];
            }
            ynewlo = y[ilo];

            if(kcount < icount) {
                ifault = 2;
                break;
            }

            ifault = 0;

            for(int i = 0; i < n; i++) {
                del = step[i] * eps;
                xmin[i] = xmin[i] + del;
                double z = getValue(xmin);
                icount++;
                if(z < ynewlo) {
                    ifault = 2;
                    break;
                }
                xmin[i] = xmin[i] - del - del;
                z = getValue(xmin);
                icount++;
                if(z < ynewlo) {
                    ifault = 2;
                    break;
                }
                xmin[i] = xmin[i] + del;
            }

            if(ifault == 0) {
                break;
            }
            //
            // Restart the procedure.
            //
            System.arraycopy(xmin, 0, start, 0, n);
            del = eps;
            numres++;
        }
    }
}