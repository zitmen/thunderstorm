package cz.cuni.lf1.lge.ThunderSTORM.estimators;

//import cz.cuni.lf1.lge.ThunderSTORM.CameraSetupPlugIn;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.DefocusCalibration;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params;
import ij.IJ;
import java.util.Arrays;

/**
 * Rewritten to java from the matlab implementation 
 * By Koen Martens
 * March 2017
 *
 */
public class PhasorFitter implements IOneLocationFitter {
    DefocusCalibration calibration = null;
    double fi, a1, b1, c1, d1, a2, b2, c2, imsizedouble, dist, centersize, ringsize, totsignal, backgroundstd, fitcos[], fitsin[], fitomega[], noisearray[][], signalarray[][];
    int fitradius, imsizeint;
    String calname;
    boolean Astigmatism;
    
    public PhasorFitter(int fitradius, DefocusCalibration calibration) {
        this.calibration = calibration;
        this.calname = calibration.getName();
        this.a1 = calibration.getA1();
        this.b1 = calibration.getB1();
        this.c1 = calibration.getC1();
        this.a2 = calibration.getA2();
        this.b2 = calibration.getB2();
        this.c2 = calibration.getC2();
        this.d1 = calibration.getD1();
        Astigmatism = true;
        this.fitradius = fitradius;
        //Calculate parts of the fitting that are only dependant on the fitradius
        this.imsizeint = 2*fitradius+1;
        this.imsizedouble = 2*fitradius+1;
        this.fitomega = new double[imsizeint];
        this.fitcos = new double[imsizeint];
        this.fitsin = new double[imsizeint];
        for (int indi = 0; indi < imsizeint; indi++){
            fitomega[indi] = (indi/imsizedouble)*2*Math.PI;
            fitcos[indi] = Math.cos(fitomega[indi]);
            fitsin[indi] = Math.sin(fitomega[indi]);
        }
        //Calculate regions for background and signal
        centersize = imsizeint-2;
        ringsize = 2;
        this.noisearray = new double[imsizeint][imsizeint];
        this.signalarray = new double[imsizeint][imsizeint];
        for (int xx = 0; xx < imsizeint; xx++){
            for (int yy = 0; yy < imsizeint; yy++){
                //get distance to center
                dist = Math.sqrt(Math.abs(xx-(imsizedouble-1)/2)*Math.abs(xx-(imsizedouble-1)/2)+Math.abs(yy-(imsizedouble-1)/2)*Math.abs(yy-(imsizedouble-1)/2));
                if (dist > (centersize+1)/2){
                    if (dist <= (ringsize+(centersize+1)/2)){
                        noisearray[xx][yy] = 1;
                    }else{
                        noisearray[xx][yy] = 0;
                    }
                }else{
                    noisearray[xx][yy] = 0;
                }
                if (dist <= (centersize+1)/2){
                    signalarray[xx][yy] = 1;
                }else{
                    signalarray[xx][yy] = 0;
                }
            }
        }
    }
    public PhasorFitter(int fitradius){
        Astigmatism = false;
        this.fitradius = fitradius;
        //Calculate parts of the fitting that are only dependant on the fitradius
        this.imsizeint = 2*fitradius+1;
        this.imsizedouble = 2*fitradius+1;
        this.fitomega = new double[imsizeint];
        this.fitcos = new double[imsizeint];
        this.fitsin = new double[imsizeint];
        for (int indi = 0; indi < imsizeint; indi++){
            fitomega[indi] = (indi/imsizedouble)*2*Math.PI;
            fitcos[indi] = Math.cos(fitomega[indi]);
            fitsin[indi] = Math.sin(fitomega[indi]);
        }
        //Calculate regions for background and signal
        centersize = imsizeint-2;
        ringsize = 2;
        this.noisearray = new double[imsizeint][imsizeint];
        this.signalarray = new double[imsizeint][imsizeint];
        for (int xx = 0; xx < imsizeint; xx++){
            for (int yy = 0; yy < imsizeint; yy++){
                //get distance to center
                dist = Math.sqrt(Math.abs(xx-(imsizedouble-1)/2)*Math.abs(xx-(imsizedouble-1)/2)+Math.abs(yy-(imsizedouble-1)/2)*Math.abs(yy-(imsizedouble-1)/2));
                if (dist > (centersize+1)/2){
                    if (dist <= (ringsize+(centersize+1)/2)){
                        noisearray[xx][yy] = 1;
                    }else{
                        noisearray[xx][yy] = 0;
                    }
                }else{
                    noisearray[xx][yy] = 0;
                }
                if (dist <= (centersize+1)/2){
                    signalarray[xx][yy] = 1;
                }else{
                    signalarray[xx][yy] = 0;
                }
            }
        }
    }
    //Fitting below
    @Override
    public Molecule fit(SubImage img) {
    //long startTime = System.nanoTime();
        
        //Variable initiation
        double totResult[] = new double[6];   
        double omega = 2.0 * Math.PI / img.size_x;
        double axy[][] = new double[img.size_x][img.size_y];
        double totalint = 0;
        //Loop through all positions in array but in an X/Y manner:
        //First calculate total intensity of matrix, then set each point, normalized by total intensity
        for (int y = 0; y < img.size_x; y++) {
            for (int x = 0; x < img.size_x; x++) {
                totalint += img.values[x+y*img.size_x];
            }
	}
        for (int y = 0; y < img.size_x; y++) {
            for (int x = 0; x < img.size_x; x++) {
                axy[x][y] = img.values[x+y*img.size_x]/totalint;
            }
	}
        //Do the partial Fourier transformation (only acquiring first order harmonics)
        totResult = twoDfft(axy,fitcos,fitsin);
        //Re-assign variables to something readable
        double FirstHarmonicXRe = totResult[0];
        double FirstHarmonicXIm = totResult[1];
        double AmplitudeX = totResult[2];
        double FirstHarmonicYRe = totResult[3];
        double FirstHarmonicYIm = totResult[4];
        double AmplitudeY = totResult[5];
        //Backup next line from change 19/3/2018
        //double angY=(Math.PI-Math.atan(FirstHarmonicYIm/FirstHarmonicYRe))*-1;
        //New line from change 19/3/2018 - atan2 resembles matlab more
        double angY=Math.atan2(FirstHarmonicYIm,FirstHarmonicYRe);
        //Removed strange part when 3x3 matrix is used at 19/3/2018
        //This used to be adding 1 PI when angle was below -PI
        if (angY>0){
            angY=angY-2*Math.PI;
        }
        //Backup next line from change 19/3/2018
        //double angX=(Math.PI-Math.atan(FirstHarmonicXIm/FirstHarmonicXRe))*-1;
        //New line from change 19/3/2018 - atan2 resembles matlab more
        double angX=Math.atan2(FirstHarmonicXIm,FirstHarmonicXRe);
        //Removed strange part when 3x3 matrix is used at 19/3/2018
        //This used to be adding 1 PI when angle was below -PI
        if (angX>0){
            angX=angX-2*Math.PI;
        }
        
        //Calculate X and Y positions
        double xpos = (Math.abs(angY)/omega) - (img.size_x-1)/2;// -0.125;//+0.25;
        double ypos = (Math.abs(angX)/omega) - (img.size_y-1)/2;// +0.125*1.5;
        //Calculate corrected amplitudes for astigmatism
        //double abslengthx = -1*AmplitudeX+1;//*-1+1;
        //double abslengthy = -1*AmplitudeY+1;//*-1+1;
        double abslengthx = AmplitudeX;//*-1+1;
        double abslengthy = AmplitudeY;//*-1+1;
      
        //Set z-position to 0 if no astigmatism is used
        double zpos = 0;
        //If a 3D calibration astigmatism file is loaded
        if (Astigmatism) {
            // Old astigmatism calibration based on 2 curves
            //Calculate subparts of the 3rd-factor function (done with Wolfram Alpha)
            //double div = abslengthx/abslengthy;
            //double part1 = a1*c1*c1*a2*div-2*a1*c1*a2*c2*div-a1*b1+a1*a2*c2*c2*div+a1*b2*div+b1*a2*div-a2*b2*div*div;
            //double zpos1 = (-1*Math.sqrt(part1)+a1*c1-a2*c2*div)/(a1-a2*div);
            //double zpos2 = (Math.sqrt(part1)+a1*c1-a2*c2*div)/(a1-a2*div);
            //double zpos3 = (c1*c1*a2*div+b1-a2*c2*c2*div+b2*div)/(2*a2*div*(c1-c2));
            //Choose the correct zpos - the one that is closest to 0.
            //zpos3 is chosen if div lies below the curve - error preventing
            //if(div == a1/a2){zpos = zpos3;}
            //else if (Math.abs(zpos2) < Math.abs(zpos1))
            //{
            //    zpos = zpos2;
            //}
            //else{
            //    zpos = zpos1;
            //}
            //Prevent NaN-errors
            //if(Double.isNaN(zpos)){zpos=0;}
            //End old astigmatism calibration 
            
            //New astigmatism calibration: If magn1>magn2, then use sigma1Cali, otherwise sigma2Cali (i.e. 'linear' piece of calibration curves)
            //ATM assuming ThunderSTORM calibration is used!
            //Check if ThunderSTORM calibration is used - if not, don't give any Zpos and warn the user
            if (calname.equals("ThunderSTORM")){
                //If clearly R1, use R1
                //double b = b1;
                //double a = a1;
                if (AmplitudeX/AmplitudeY > 1){//THIS Is the positive part in Christophe's data!
                    if (c1>c2){
                        zpos = -1*Math.sqrt(((AmplitudeX/AmplitudeY)-b1)/a1)+c1;
                    }else{
                        zpos = Math.sqrt(((AmplitudeX/AmplitudeY)-b2)/a2)-c2;
                    }
                }else{ 
                    if (c1>c2){
                        zpos = Math.sqrt(((AmplitudeY/AmplitudeX)-b2)/a2)+c2;
                    }else{
                        zpos = -1*Math.sqrt(((AmplitudeY/AmplitudeX)-b1)/a1)-c1;
                    }
                }
                //Prevent NaN-errors
                if(Double.isNaN(zpos)){zpos=0;}

            }else{
                zpos = 0;
                IJ.log("Please use the ThunderSTORM defocus curve option for calibration curves if using pSMLM-3D!");
            }
            
        }
        
        //Get background and signal levels
        int totsignalarraysize = 0;
        totsignal = 0;
        int totbgnonzeros = 0;
        int totbgzeros = 0;
        double totbgarray[] = new double[axy.length*axy.length];
        for (int i = 0; i < axy.length; i++) { 
            for (int j = 0; j < axy.length; j++) { 
                //Noise
                if (noisearray[i][j] > 0){
                    totbgarray[i*axy.length+j] = axy[i][j]*totalint;
                    totbgnonzeros += 1;
                }else{
                    totbgzeros +=1;
                }
                //Signal
                if (signalarray[i][j] > 0){
                    totsignal += axy[i][j]*totalint;
                    totsignalarraysize += 1;
                }
            }
        }
        //Get Xth percentile
        Arrays.sort(totbgarray);
        int arraypercentile = (int) Math.ceil(totbgnonzeros*.56);
        double xthpercentile = totbgarray[arraypercentile + totbgzeros];
        
        //Get value for intensity, all > 0
        double intensity = Math.max(0,totsignal - xthpercentile * totsignalarraysize);
        double background = (xthpercentile);
        int id = 0;
        double backgroundstdarr[] = new double[totbgarray.length-(totbgzeros)];
        for (int i = totbgzeros; i < totbgarray.length; i++){
            backgroundstdarr[id] = totbgarray[i];
            id+=1;
        }
        backgroundstd = getStdDev(backgroundstdarr);
        double[] parameters = new double[]{xpos, ypos, zpos, intensity, background, backgroundstd, abslengthx, abslengthy};
        
        return new Molecule(new Params(new int[]{PSFModel.Params.X, PSFModel.Params.Y, PSFModel.Params.Z, PSFModel.Params.INTENSITY, PSFModel.Params.OFFSET, PSFModel.Params.BACKGROUND, PSFModel.Params.SIGMA1, PSFModel.Params.SIGMA2}, parameters,  false));
    }
    public static double[] twoDfft(double[][] inputData, double[] cos, double[] sin) 
    {
        //Initialize variables
        double height = inputData.length;
        double width = inputData[0].length;
        double[] totxloopRe = new double[inputData.length];
        double[] totxloopIm = new double[inputData.length];
        double FirstHarmonicXRe = 0;
        double FirstHarmonicXIm = 0;
        double[] totyloopRe = new double[inputData.length];
        double[] totyloopIm = new double[inputData.length];
        double FirstHarmonicYRe = 0;
        double FirstHarmonicYIm = 0;
        double[] totalArray = new double[6];
        //loop over the rows of the input data, multiply by real and imag parts of omega1 column
        for (int rowloop = 0; rowloop < height; rowloop++) 
        {
            for (int colloop = 0; colloop < width; colloop++) 
            {
                //Calculate real (cos) and imag (-sin) parts of the current row.
                totxloopRe[colloop] += cos[colloop]*inputData[rowloop][colloop];
                totxloopIm[colloop] -= sin[colloop]*inputData[rowloop][colloop];
           }
        }
        //Sum all the rows to get the first order harmonic in X
        for (int colloop = 0; colloop < width; colloop++) {
            FirstHarmonicXRe += totxloopRe[colloop];
            FirstHarmonicXIm += totxloopIm[colloop];
        }
        //Calculate amplitude
        double AmplitudeX = Math.sqrt(FirstHarmonicXRe/height*FirstHarmonicXRe/height+FirstHarmonicXIm/height*FirstHarmonicXIm/height);
        
        //loop over the columnss of the input data, multiply by real and imag parts of omega1 row       
        for (int colloop = 0; colloop < width; colloop++) 
        {
            for (int rowloop = 0; rowloop < height; rowloop++) 
            {
                //Calculate real (cos) and imag (-sin) parts of the current column.
                totyloopRe[rowloop] += cos[rowloop]*inputData[rowloop][colloop];
                totyloopIm[rowloop] -= sin[rowloop]*inputData[rowloop][colloop];
            }
        }
        //Sum all the rows to get the first order harmonic in Y 
        for (int rowloop = 0; rowloop < width; rowloop++) {
            FirstHarmonicYRe += totyloopRe[rowloop];
            FirstHarmonicYIm += totyloopIm[rowloop];
        } 
        //Calculate amplitude
        double AmplitudeY = Math.sqrt(FirstHarmonicYRe/height*FirstHarmonicYRe/height+FirstHarmonicYIm/height*FirstHarmonicYIm/height);
 
        //Placing the 6 variables in a array to be provided to the user
        totalArray[0]=FirstHarmonicXRe;
        totalArray[1]=FirstHarmonicXIm;
        totalArray[2]=AmplitudeX;
        totalArray[3]=FirstHarmonicYRe;
        totalArray[4]=FirstHarmonicYIm;
        totalArray[5]=AmplitudeY;
        
        //Return the values
        return totalArray;
    }
    double getMean(double[] inputData){
        double sum = 0;
        for(int a = 0; a < inputData.length; a++)
            sum += inputData[a];
        return sum/inputData.length;
    }

    double getVariance(double[] inputData){
        double mean = getMean(inputData);
        double temp = 0;
        for(int a = 0; a < inputData.length; a++)
            temp += (inputData[a]-mean)*(inputData[a]-mean);
        return temp/(inputData.length);
    }

    double getStdDev(double[] inputData){
        return Math.sqrt(getVariance(inputData));
    }
}