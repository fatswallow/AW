�������ִ�����ɣ�libSoundPitch.so��Demo apk��

����:
libSoundPitch.so����ѡ��
1������apk������apkʱ����LOCAL_JNI_SHARED_LIBRARIES := libSoundPitch
2������ϵͳ���ڷ���mk������H8Ϊandroid/device/softwinner/eagle-fvd-p1/eagle_fvd_p1.mk��������PRODUCT_PACKAGES += libSoundPitch

Demo apk����ѡ�����Ӧjar��Ҳ����ֱ����apk�е��ã�

������Է���android��������ɨ�赽��Ŀ¼����android/frameworks/av/media/soundpitch��android/external/soundpitch��


������demo��apk�е�doPitch()����������pitch��ΧΪ[-60,60]����Ҫע��out buffer����Ϊin buffer��С��4����������in.pcm���ԣ�doPitch()ǰ��1K��������
������demo��apk�е�doMix()�������Ľ�����shortֵȡƽ��ֵ�����㣬������mixin.pcm���ԣ�doMix()ǰ������Ϊ1K��������Ϊ500Hz��doMix()������������1K+500Hz��

