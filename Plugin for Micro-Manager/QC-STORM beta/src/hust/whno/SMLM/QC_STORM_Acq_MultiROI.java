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

import mmcorej.CMMCore;
import org.micromanager.ScriptController;
import org.micromanager.Studio;



public class QC_STORM_Acq_MultiROI {
    
    private Studio studio_;
    CMMCore mmc;
    ScriptController gui;
   
    public QC_STORM_Configurator MyConfigurator; // reference to configurator

    
    // multi ROI acquisition
    private MultiROIAcqThread MultiAcqThread;

    public int ImageWidthI,ImageHighI;    
    
    
    QC_STORM_Acq_MultiROI(Studio studio, QC_STORM_Configurator iConfigurator)
    {
        studio_=studio;
        mmc=studio_.getCMMCore();
        gui=studio_.scripter();
        
        MyConfigurator=iConfigurator;
        
        ImageWidthI = (int) mmc.getImageWidth();
        ImageHighI = (int) mmc.getImageHeight();              
        
//		gui.message("into QC_STORM_BurstLiveProc");
    }

    public void StartMultiROIAcq()
    {
        MyConfigurator.SendLocalizationPara();
        
        
        // start loc thread
        MultiAcqThread = new MultiROIAcqThread();
        MultiAcqThread.start();
    }    

    public class MultiROIAcqThread extends Thread
    {
        @Override
        public void run()
        {
            try {
                
                int ROINum_X   = MyConfigurator.GetMultiAcq_ROINum_X();
                int ROINum_Y   = MyConfigurator.GetMultiAcq_ROINum_Y();
                
                int ROISteps_X = MyConfigurator.GetMultiAcq_StepsNum_X();
                int ROISteps_Y = MyConfigurator.GetMultiAcq_StepsNum_Y();
                
                int X_id = 0;
                int Y_id = 0;
                
                String TimeStamp = QC_STORM_Parameters.GetCreateTimeIdx_Sum();
                
                gui.message("into MultiROIAcqThread");

                for(int ycnt = 0; ycnt < ROINum_Y; ycnt++)
                {
                    if(!MyConfigurator.IsMultiROIAcqActive())break;
                    
                    ROINum_Y   = MyConfigurator.GetMultiAcq_ROINum_Y();
                    
                    Y_id = ycnt;

                    
                    for(int xcnt = 0; xcnt < ROINum_X; xcnt++)
                    {
                        if(!MyConfigurator.IsMultiROIAcqActive())break;
                        
                        X_id = xcnt;
                        if(ycnt%2 != 0){
                            X_id = ROINum_X - 1 - xcnt;
                        }
                        
                        
                        // if this is selected, don't perform acquisition initialization
                        if(!MyConfigurator.IsNoAcqIni())
                        {
                            // initial before acquisition
                            NewAcqInitial acqInitial = new NewAcqInitial();
                            acqInitial.start();
                            acqInitial.join();
                        }
                        
                        // start acquisition
                        
                        String NamePostFix = String.format("%s_Y%d_X%d_", TimeStamp, Y_id, X_id);
                        
                        MyConfigurator.CurBurstLiveActive = true;
                        
                        QC_STORM_Acq_BurstAcq BurstAcq = new QC_STORM_Acq_BurstAcq(studio_, MyConfigurator, NamePostFix);
                        
                        BurstAcq.StartBurstAcq();
                        BurstAcq.WaitBurstAcqFinish();
                       
                        
                        // move ROI first and then save raw images to save time, such as wait density down
                        if(xcnt < ROINum_X-1)
                        {
                            // s shape move
                            // x move a ROI
                            if(ycnt%2 == 0)
                            {
                                QC_STORM_Plug.lm_TranslationStageMove(ROISteps_X, 0, 0);   
                            }
                            else
                            {
                                QC_STORM_Plug.lm_TranslationStageMove(-ROISteps_X, 0, 0);   
                            }
                        }     
                    }
                    if(ycnt < ROINum_Y-1)
                    {
                        // move to new line
                        QC_STORM_Plug.lm_TranslationStageMove(0, ROISteps_Y, 0);       
                    }
                }
                
                if(MyConfigurator.IsStageReturn())
                {
                    QC_STORM_Plug.lm_TranslationStageMove(0, -ROISteps_Y*(ROINum_Y - 1), 0);
                    
                    if(ROINum_Y%2 != 0)
                    {
                        QC_STORM_Plug.lm_TranslationStageMove(-ROISteps_X*(ROINum_X - 1), 0, 0);
                    }
                }
                
            } catch (Exception ex) {
                gui.message("get exception");
            }
            
            MyConfigurator.ResetMultiROIAcqBtn();                  
        }
    }
    
    public class NewAcqInitial extends Thread
    {

        @Override
        public void run()
        {
            // initial acquisition befor acquisition of each ROI

            QC_STORM_ZDriftCorrection ZDriftCorrThread;

            int WaitTime = MyConfigurator.GetMultiAcqWaitTime();
            
            try {
                    QC_STORM_Plug.lm_ResetFeedback();

                    
                    if(WaitTime > 0)
                    {
                        Thread.currentThread().sleep(WaitTime);
                    }
                    
                    
                    if(MyConfigurator.IsZDriftCtlEn())
                    {
                        // make sure z drift is corrected and the density is ok  
                        // correct z drift befor acquistion
                        ZDriftCorrThread = new QC_STORM_ZDriftCorrection(studio_, MyConfigurator, MyConfigurator.ZCorrMode(), 6, 2, 1);
                        ZDriftCorrThread.start();
                        ZDriftCorrThread.join();   
                        
                        
                        ZDriftCorrThread = new QC_STORM_ZDriftCorrection(studio_, MyConfigurator, MyConfigurator.ZCorrMode(), 3, 1, 1);
                        ZDriftCorrThread.start();
                        ZDriftCorrThread.join();

                        ZDriftCorrThread = new QC_STORM_ZDriftCorrection(studio_, MyConfigurator, MyConfigurator.ZCorrMode(), 1, 2, 1);
                        ZDriftCorrThread.start();
                        ZDriftCorrThread.join();
                    }
                    
                    float MaxTolerableDensity = MyConfigurator.GetMaxTolerableLocDensity();
                    

                    // wait localization density down to software's ability
                    WaitLocDensityDown WaitLocDensityThread = new WaitLocDensityDown(true, MaxTolerableDensity);
                    WaitLocDensityThread.start();
                    WaitLocDensityThread.join();

                    if(WaitLocDensityThread.IsWaited == true)
                    {
                                            
                        float LocDensityTarget = MyConfigurator.GetLocDensityTarget();

                        SetOptimalActivationDensity ActivationDensityIni = new SetOptimalActivationDensity(LocDensityTarget);

                        ActivationDensityIni.start();
                        ActivationDensityIni.join();

                        if(MyConfigurator.IsZDriftCtlEn())
                        {
                            ZDriftCorrThread = new QC_STORM_ZDriftCorrection(studio_, MyConfigurator, MyConfigurator.ZCorrMode(), 1, 1, 1);
                            ZDriftCorrThread.start();
                            ZDriftCorrThread.join();               
                        }
                    }

                } catch (InterruptedException ex) {              
            }
        }
    }
    
    public class WaitLocDensityDown extends Thread
    {
        boolean WaitDensityEn;
        float DensityTh;
        boolean IsWaited = false;
        
        WaitLocDensityDown(boolean iWaitDensityEn, float iDensityTh)
        {
            WaitDensityEn = iWaitDensityEn;
            DensityTh = iDensityTh;
            IsWaited = false;
        }
        
        @Override
        public void run()
        {
            
            QC_STORM_SmallBatchImageAcq SmallBatchImageAcq = new QC_STORM_SmallBatchImageAcq(studio_, MyConfigurator);
            
            
            while(WaitDensityEn)
            {
                if(!MyConfigurator.IsMultiROIAcqActive())break;
                
                // also correct PSF when wait density down
                try {
                    
                    // get localization density

                    float [] LocResults = SmallBatchImageAcq.GetLocResultsOfBatchedImageDat_Default(4);
                    
                    float CurLocDensity = LocResults[QC_STORM_Plug.LocInfID_LocDensity];
                    
                    if(CurLocDensity <= DensityTh)
                    {
                        break;
                    }
                    else
                    {
                        if(MyConfigurator.IsZDriftCtlEn())
                        {
                            // z drift correction in the wait process
                            QC_STORM_ZDriftCorrection ZdriftCorrThread = new QC_STORM_ZDriftCorrection(studio_, MyConfigurator, MyConfigurator.ZCorrMode(), 1, 2, 1); 

                            ZdriftCorrThread.start();
                            ZdriftCorrThread.join();
                        }
                        else
                        {
                            Thread.currentThread().sleep(500);
                        }
                        
                        IsWaited = true;
                    }
                    
                } catch (InterruptedException ex) {
                    
                }
            }
        }
    }
    
    public class SetOptimalActivationDensity extends Thread
    {
        float LocDensityTarget = 0.0f;
        
        float PowerFullRange = 100.0f;
        
        final int SearchSteps = 10;

        
        SetOptimalActivationDensity(float iLocDensityTarget)
        {
            LocDensityTarget = iLocDensityTarget;
        }
        
        
        @Override
        public void run()
        {
            QC_STORM_SmallBatchImageAcq SmallBatchImageAcq = new QC_STORM_SmallBatchImageAcq(studio_, MyConfigurator);
            
            
            int FoundPos = 0;
            
            for(int cnt = 0; cnt <= SearchSteps; cnt++)
            {
                float PowerPercentage = PowerFullRange*cnt/SearchSteps;
                
                QC_STORM_Plug.lm_SetActivationLaserPower(PowerPercentage);
                
                // get localization density

                float [] LocResults = SmallBatchImageAcq.GetLocResultsOfBatchedImageDat_Default(4);

                float CurLocDensity = LocResults[QC_STORM_Plug.LocInfID_LocDensity];
                
                
                if(CurLocDensity >= LocDensityTarget)
                {
                    FoundPos = cnt;
                    break;
                }
            }
            
            if(FoundPos > 0)
            {
                 for(int cnt = 0; cnt <= SearchSteps; cnt++)
                {
                    float PowerPercentage = PowerFullRange*((FoundPos-1)*1.0f/SearchSteps + cnt*1.0f/SearchSteps/SearchSteps);

                    QC_STORM_Plug.lm_SetActivationLaserPower(PowerPercentage);

                    // get localization density

                    float [] LocResults = SmallBatchImageAcq.GetLocResultsOfBatchedImageDat_Default(4);

                    float CurLocDensity = LocResults[QC_STORM_Plug.LocInfID_LocDensity];


                    if(CurLocDensity >= LocDensityTarget - 0.02f)
                    {
                        break;
                    }
                }   
            }
        }
    }
}
