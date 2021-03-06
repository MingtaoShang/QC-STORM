/*
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU LESSER GENERAL PUBLIC LICENSE as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU LESSER GENERAL PUBLIC LICENSE for more details.

You should have received a copy of the GNU LESSER GENERAL PUBLIC LICENSE
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/


package hust.whno.SMLM;

import ij.ImagePlus;
import ij.io.Opener;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.util.Calendar;



public class QC_STORM_Parameters {
    
    // for debug, read image from local data file
    public static int CurImgId = 1;

    public static int GetSRImageSize(int ImageSize, float PixelZoom)
    {
        int SRImageSize = (int) (ImageSize*PixelZoom);
        
        SRImageSize = (SRImageSize+3)/4*4;
        
        return SRImageSize;
        
    }
    
    public static class DevicePortInf
    {
        int IsEnableI;
        int PortID;
        int DataRateI;
        
        public DevicePortInf()
        {
            IsEnableI = 0;
            PortID = 0;
            DataRateI = 38400;
        }
    }
    
    public static class PIDCtlPara
    {
        float KP;
        float KI;
        float KD;
        public PIDCtlPara()
        {
            KP=0;
            KI=0;
            KD=0;
        }
    }
    
    // parameters for localization
    public static class LocalizationPara
    {   
        float Kadc = 0.45f;
        float Offset = 100;
        float QE = 0.72f;
        int RegionSize = 7;

        float RawImgPixelSize = 100;
        float RenderingPixelSize =10;
        float RenderingPixelZoom = 10;
        
        
        int LocType = 0;
        
        int MultiEmitterFitEn = 0;
        
        int WLEEn = 1;
        
        int ConsecutiveFitEn = 0;
        float ConsecFilterRadius = 80;
        
        float MinZDepth = -500;
        float MaxZDepth = 500;
        
        float ZDepthCorrFactor = 1.0f;
        

        // calibration of sigma X >= sigma Y
    	float p4_XGY = 0.0f;
    	float p3_XGY = 0.0f;
    	float p2_XGY = 0.0f;
    	float p1_XGY = 1.0f;
    	float p0_XGY = 0.0f;

    	// calibration of sigma X < sigma Y
    	float p4_XLY = 0.0f;
    	float p3_XLY = 0.0f;
    	float p2_XLY = 0.0f;
    	float p1_XLY = 1.0f;
    	float p0_XLY = 0.0f;
        

        // rendering
        float SNR_th = 5; // used in rendering
        
        // spatial resolution
        float StructureSize2D = 40;
        
        // statastic information display enable
        int StatDispSel = 0;
        int SpatialResolutionEn = 0;

    }
    
    public static String SelectDisk()
    {
        final int DiskNum=8; 
        String [] DiskName={
        "D",
        "E",
        "F",
        "G",
        "H",
        "I",
        "J",
        "C"
        };
        
        int i;
        for(i=0;i<DiskNum;i++)
        {
            String CurDiskName=DiskName[i]+":\\";
            File f = new File(CurDiskName);
            if(f.exists()){
                return CurDiskName;
            }  
        }
        return null;
    }    
    
    public static short [] GetSimuImage512x512()
    {
                    
        ///////////////////////////////////////////////////////////////////////////////////////////
        // simu read from hard disk
        // delete simu code for real experiments
        // also set image width previously
        
        Opener tifOpener = new Opener();

        // read image from hard disk

        int TotalFrame = 1000;
        ImagePlus tifImagePlus = tifOpener.openTiff("G:\\MMStack_512x512x1500.tif", CurImgId);
//        tifImagePlus = tifOpener.openTiff("G:\\MMStack_Pos0_512X512X1.tif",1);

        short [] pImgData =(short[])tifImagePlus.getProcessor().getPixels();

        CurImgId++;
        if((CurImgId>TotalFrame)||(CurImgId<1)){
            CurImgId=1;
        }
        // simu end
        ///////////////////////////////////////////////////////////////////////////////////////////
                 
         return pImgData;
    }
    
    public static String GetCreateTimeIdx()
    {
        Calendar c = Calendar.getInstance();//可以对每个时间域单独修改   

        int year = c.get(Calendar.YEAR);  
        int month = c.get(Calendar.MONTH)+1;   
        int date = c.get(Calendar.DATE);    
        int hour = c.get(Calendar.HOUR_OF_DAY);   
        int minute = c.get(Calendar.MINUTE);   
        int second = c.get(Calendar.SECOND);    

        String TimeIdxStr = String.format("%d%02d%02d_%02d%02d%02d", year, month, date, hour, minute, second);
        
        return TimeIdxStr;
    }
    
    public static String GetCreateTimeIdx_Sum()
    {
        Calendar c = Calendar.getInstance();//可以对每个时间域单独修改   


        int hour = c.get(Calendar.HOUR_OF_DAY);   
        int minute = c.get(Calendar.MINUTE);   

        String TimeIdxStr = String.format("%d", hour*60 + minute);
        
        return TimeIdxStr;
    }
    
    public static ColorModel GetHotColorModel()
    {
                // construct hot colormap
		byte[] r = new byte[256], g = new byte[256], b = new byte[256];
		for (int q = 0; q < 85; q++) {
			r[q] = (byte) (3 * q);
			g[q] = 0;
			b[q] = 0;
		}
		for (int q = 85; q < 170; q++) {
			r[q] = (byte) (255);
			g[q] = (byte) (3 * (q - 85));
			b[q] = 0;
		}
		for (int q = 170; q < 256; q++) {
			r[q] = (byte) (255);
			g[q] = (byte) 255;
			b[q] = (byte) (3 * (q - 170));
		}
		 return new IndexColorModel(3, 256, r, g, b);
    }
        
}
