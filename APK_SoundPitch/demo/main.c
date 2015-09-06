////////////////////////////////////////////////////////////////////////////////
///
/// SoundStretch main routine.
///
/// Author        : Copyright (c) Olli Parviainen
/// Author e-mail : oparviai 'at' iki.fi
/// SoundTouch WWW: http://www.surina.net/soundtouch
///
////////////////////////////////////////////////////////////////////////////////
//
// Last changed  : $Date: 2011-07-16 11:55:23 +0300 (Sat, 16 Jul 2011) $
// File revision : $Revision: 4 $
//
// $Id: main.cpp 121 2011-07-16 08:55:23Z oparviai $
//
////////////////////////////////////////////////////////////////////////////////
//
// License :
//
//  SoundTouch audio processing library
//  Copyright (c) Olli Parviainen
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
////////////////////////////////////////////////////////////////////////////////

//#include <stdexcept>
#include <stdio.h>

#include <string.h>
//#include "RunParameters_1.h"
#include "TouchToStretch.h"
//#include "WavFile.h"
//#include "SoundTouch.h"
//#include "BPMDetect.h"

//using namespace soundtouch;
//using namespace std;

// Processing chunk size
#define BUFF_SIZE           2048
/*
#if WIN32
    #include <io.h>
    #include <fcntl.h>

    // Macro for Win32 standard input/output stream support: Sets a file stream into binary mode
    #define SET_STREAM_TO_BIN_MODE(f) (_setmode(_fileno(f), _O_BINARY))
#else
    // Not needed for GNU environment... 
    #define SET_STREAM_TO_BIN_MODE(f) {}
#endif
*/

static const char _helloText[] = 
    "\n"
    "   SoundStretch v%s -  Written by Olli Parviainen 2001 - 2011\n"
    "==================================================================\n"
    "author e-mail: <oparviai"
    "@"
    "iki.fi> - WWW: http://www.surina.net/soundtouch\n"
    "\n"
    "This program is subject to (L)GPL license. Run \"soundstretch -license\" for\n"
    "more information.\n"
    "\n";
/*
static void openFiles(WavInFile **inFile, WavOutFile **outFile, const RunParameters *params)
{
    int bits, samplerate, channels;

    if (strcmp(params->inFileName, "stdin") == 0)
    {
        // used 'stdin' as input file
        SET_STREAM_TO_BIN_MODE(stdin);
        *inFile = new WavInFile(stdin);
    }
    else
    {
        // open input file...
        *inFile = new WavInFile(params->inFileName);
    }

    // ... open output file with same sound parameters
    bits = (int)(*inFile)->getNumBits();
    samplerate = (int)(*inFile)->getSampleRate();
    channels = (int)(*inFile)->getNumChannels();

    if (params->outFileName)
    {
        if (strcmp(params->outFileName, "stdout") == 0)
        {
            SET_STREAM_TO_BIN_MODE(stdout);
            *outFile = new WavOutFile(stdout, samplerate, bits, channels);
        }
        else
        {
            *outFile = new WavOutFile(params->outFileName, samplerate, bits, channels);
        }
    }
    else
    {
        *outFile = NULL;
    }
}



// Sets the 'SoundTouch' object up according to input file sound format & 
// command line parameters
static void setup(SoundTouch *pSoundTouch, const WavInFile *inFile, const RunParameters *params)
{
    int sampleRate;
    int channels;

    sampleRate = (int)inFile->getSampleRate();
    channels = (int)inFile->getNumChannels();
    pSoundTouch->setSampleRate(sampleRate);
    pSoundTouch->setChannels(channels);

    pSoundTouch->setTempoChange(params->tempoDelta);
    pSoundTouch->setPitchSemiTones(params->pitchDelta);
    pSoundTouch->setRateChange(params->rateDelta);

    pSoundTouch->setSetting(SETTING_USE_QUICKSEEK, params->quick);
    pSoundTouch->setSetting(SETTING_USE_AA_FILTER, !(params->noAntiAlias));

    if (params->speech)
    {
        // use settings for speech processing
        pSoundTouch->setSetting(SETTING_SEQUENCE_MS, 40);
        pSoundTouch->setSetting(SETTING_SEEKWINDOW_MS, 15);
        pSoundTouch->setSetting(SETTING_OVERLAP_MS, 8);
        fprintf(stderr, "Tune processing parameters for speech processing.\n");
    }

    // print processing information
    if (params->outFileName)
    {
#ifdef SOUNDTOUCH_INTEGER_SAMPLES
        fprintf(stderr, "Uses 16bit integer sample type in processing.\n\n");
#else
    #ifndef SOUNDTOUCH_FLOAT_SAMPLES
        #error "Sampletype not defined"
    #endif
        fprintf(stderr, "Uses 32bit floating point sample type in processing.\n\n");
#endif
        // print processing information only if outFileName given i.e. some processing will happen
        fprintf(stderr, "Processing the file with the following changes:\n");
        fprintf(stderr, "  tempo change = %+g %%\n", params->tempoDelta);
        fprintf(stderr, "  pitch change = %+g semitones\n", params->pitchDelta);
        fprintf(stderr, "  rate change  = %+g %%\n\n", params->rateDelta);
        fprintf(stderr, "Working...");
    }
    else
    {
        // outFileName not given
        fprintf(stderr, "Warning: output file name missing, won't output anything.\n\n");
    }

    fflush(stderr);
}




// Processes the sound
static void process(SoundTouch *pSoundTouch, WavInFile *inFile, WavOutFile *outFile)
{
    int nSamples;
    int nChannels;
    int buffSizeSamples;
    SAMPLETYPE sampleBuffer[BUFF_SIZE];

    if ((inFile == NULL) || (outFile == NULL)) return;  // nothing to do.

    nChannels = (int)inFile->getNumChannels();
    assert(nChannels > 0);
    buffSizeSamples = BUFF_SIZE / nChannels;

    // Process samples read from the input file
    while (inFile->eof() == 0)
    {
        int num;

        // Read a chunk of samples from the input file
        num = inFile->read(sampleBuffer, BUFF_SIZE);
        nSamples = num / (int)inFile->getNumChannels();

        // Feed the samples into SoundTouch processor
        pSoundTouch->putSamples(sampleBuffer, nSamples);

        // Read ready samples from SoundTouch processor & write them output file.
        // NOTES:
        // - 'receiveSamples' doesn't necessarily return any samples at all
        //   during some rounds!
        // - On the other hand, during some round 'receiveSamples' may have more
        //   ready samples than would fit into 'sampleBuffer', and for this reason 
        //   the 'receiveSamples' call is iterated for as many times as it
        //   outputs samples.
        do 
        {
            nSamples = pSoundTouch->receiveSamples(sampleBuffer, buffSizeSamples);
            outFile->write(sampleBuffer, nSamples * nChannels);
        } while (nSamples != 0);
    }

    // Now the input file is processed, yet 'flush' few last samples that are
    // hiding in the SoundTouch's internal processing pipeline.
    pSoundTouch->flush();
    do 
    {
        nSamples = pSoundTouch->receiveSamples(sampleBuffer, buffSizeSamples);
        outFile->write(sampleBuffer, nSamples * nChannels);
    } while (nSamples != 0);
}



// Detect BPM rate of inFile and adjust tempo setting accordingly if necessary
static void detectBPM(WavInFile *inFile, RunParameters *params)
{
    float bpmValue;
    int nChannels;
    BPMDetect bpm(inFile->getNumChannels(), inFile->getSampleRate());
    SAMPLETYPE sampleBuffer[BUFF_SIZE];

    // detect bpm rate
    fprintf(stderr, "Detecting BPM rate...");
    fflush(stderr);

    nChannels = (int)inFile->getNumChannels();
    assert(BUFF_SIZE % nChannels == 0);

    // Process the 'inFile' in small blocks, repeat until whole file has 
    // been processed
    while (inFile->eof() == 0)
    {
        int num, samples;

        // Read sample data from input file
        num = inFile->read(sampleBuffer, BUFF_SIZE);

        // Enter the new samples to the bpm analyzer class
        samples = num / nChannels;
        bpm.inputSamples(sampleBuffer, samples);
    }

    // Now the whole song data has been analyzed. Read the resulting bpm.
    bpmValue = bpm.getBpm();
    fprintf(stderr, "Done!\n");

    // rewind the file after bpm detection
    inFile->rewind();

    if (bpmValue > 0)
    {
        fprintf(stderr, "Detected BPM rate %.1f\n\n", bpmValue);
    }
    else
    {
        fprintf(stderr, "Couldn't detect BPM rate.\n\n");
        return;
    }

    if (params->goalBPM > 0)
    {
        // adjust tempo to given bpm
        params->tempoDelta = (params->goalBPM / bpmValue - 1.0f) * 100.0f;
        fprintf(stderr, "The file will be converted to %.1f BPM\n\n", params->goalBPM);
    }
}*/


SAMPLETYPE InputsampleBuffer[BUFF_SIZE];
SAMPLETYPE OutputsampleBuffer[BUFF_SIZE*64];
int main(const int nParams, const char * const paramStr[])
{
    //WavInFile *inFile;
    //WavOutFile *outFile;
	FILE *inFile;
    FILE *outFile;
    RunParameters params;
    //SoundTouch soundTouch;
	 int nSamples;
    //int nChannels;
    int buffSizeSamples;   
	int num;

    //fprintf(stderr, _helloText, SoundTouch::getVersionString());
	inFile = fopen("/data/camera/in.pcm","rb");
	if(0)//nParams<3)
	{
		printf("input params too little!\n");
		return -1;
	}
	if(!inFile)
	{
		printf("can not open input file!\n");
		return -1;
	}
	outFile = fopen("/data/camera/out.pcm","wb");
	if(!outFile)
	{
		printf("can not open out file!\n");
		return -1;
	}
	memset(&params,0,sizeof(RunParameters));
	params.channel_number = 2;
	params.sample_rate = 48000;
	params.pitchDelta = -12;//[-60.0 60.0]
	params.tempoDelta = 0;//[-95.0 5000.0]
	params.rateDelta = 0;//[-95.0 5000.0]
	params.quick = 1;//'q','-quick'
    params.noAntiAlias = 1;//'n','-naa'
    params.goalBPM = 0;//'b','bpm=xx'
    params.detectBPM = 0;//'b,'bpm=xx'
    params.speech = 0;
	RunParameters_checkLimits(&params);
    setupSoundTouch(&params,ALL);
	num = BUFF_SIZE;
   while (num == BUFF_SIZE)//(inFile->eof() == 0)
    {
        

        // Read a chunk of samples from the input file
         num = fread(InputsampleBuffer, sizeof(SAMPLETYPE),BUFF_SIZE,inFile);//num = inFile->read(sampleBuffer, BUFF_SIZE);
        nSamples = num / params.channel_number;//(int)inFile->getNumChannels();
	   nSamples = processSoundTouch(InputsampleBuffer, OutputsampleBuffer,nSamples );
	   fwrite(OutputsampleBuffer, sizeof(SAMPLETYPE),nSamples * params.channel_number,outFile);
   }
    // Process the sound
    

    if(inFile)
	{
		fclose(inFile);
		inFile = 0;
	}
	if(outFile)
	{
		fclose(outFile);
		outFile = 0;
	}
	fprintf(stderr, "Done!\n");

    return 0;
}
