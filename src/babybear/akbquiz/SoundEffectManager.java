package babybear.akbquiz;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class SoundEffectManager{
	private SoundPool soundPool;
	public int sound_right,
		sound_wrong,
		sound_click;
	private boolean isOn=true;
	private static float volume = 0.8f,//0.0~1.0
		rate = 1.0f ;//0.5~2.0
	
	
	SoundEffectManager(Context context){

		soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM,5);
        sound_click = soundPool.load(context, R.raw.click, 0);
        sound_wrong = soundPool.load(context, R.raw.wrong, 0);
        sound_right = soundPool.load(context, R.raw.right, 0);
	}
	@Override
	protected void finalize(){
		soundPool.release();	
    }
	void release(){
		soundPool.release();	
	}    	
	void play(int soundPoolId){
		if(!isOn)return;
		soundPool.play(soundPoolId, volume, volume , 0, 0, rate);
	}
	void setVolume(float vol){
		volume=vol;
	}
	void setVolume(int vol){
		volume=((float)vol/15)*0.8f;
	}
	void setVolumeUp(){
		volume+=0.1f;
		if(volume>0.8f)volume=0.8f;
	}
	void setVolumeDown(){
		volume-=0.2f;
		if(volume<0.0f)volume=0.0f;
	}
	void setRate(float ratein){
		if(ratein<0.5f){
			rate=0.5f;
			return;
		}
		if(ratein>2.0f){
			rate=2.0f;
			return;
		}
		rate=ratein;
	}
	void powerOn(){
		isOn=true;
	}
	void powerOff(){
		isOn=false;
	}
	void setSwitch(boolean flag){
		isOn=flag;
	}
}

