由两部分代码组成：libSoundPitch.so与Demo apk；

其中:
libSoundPitch.so可以选择
1）跟随apk，编译apk时设置LOCAL_JNI_SHARED_LIBRARIES := libSoundPitch
2）跟随系统，在方案mk（公版H8为android/device/softwinner/eagle-fvd-p1/eagle_fvd_p1.mk）中设置PRODUCT_PACKAGES += libSoundPitch

Demo apk可以选抽出相应jar，也可以直接在apk中调用；

代码可以放在android编译树能扫描到的目录，如android/frameworks/av/media/soundpitch或android/external/soundpitch；


音调的demo见apk中的doPitch()，可以设置pitch范围为[-60,60]，需要注意out buffer至少为in buffer大小的4倍，可以用in.pcm测试，doPitch()前是1K的音调；
混音的demo见apk中的doMix()，单纯的将两个short值取平均值的运算，可以用mixin.pcm测试，doMix()前左声道为1K，右声道为500Hz，doMix()后两声道包含1K+500Hz；

