package com.example.main;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.graphics.Bitmap.CompressFormat;
import android.util.Base64;
import android.util.Log;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;

public class MainApplication extends Application {
	
	private static MainApplication sMainApplication;
	
	@Override
	public void onCreate() {
		super.onCreate();
		sMainApplication = this;
		
		initImageLoader(sMainApplication);
		getKeyHash();
	}

	public static MainApplication getMainApplication() {
		return sMainApplication;
	}
	
	private void getKeyHash(){
		PackageInfo info;
		try {
		    info = getPackageManager().getPackageInfo("com.example.asasd", PackageManager.GET_SIGNATURES);
		    for (Signature signature : info.signatures) {
		        MessageDigest md;
		        md = MessageDigest.getInstance("SHA");
		        md.update(signature.toByteArray());
		        String something = new String(Base64.encode(md.digest(), 0));
		        //String something = new String(Base64.encodeBytes(md.digest()));
		        Log.d("TAG", something);
		    }
		} catch (NameNotFoundException e1) {
		    Log.e("name not found", e1.toString());
		} catch (NoSuchAlgorithmException e) {
		    Log.e("no such an algorithm", e.toString());
		} catch (Exception e) {
		    Log.e("exception", e.toString());
		}
	}
	
	private void initImageLoader(Context context) {
		// Initialize Universal Image Loader
		// Create global configuration and initialize ImageLoader with this
		// configuration
		DisplayImageOptions options = new DisplayImageOptions.Builder()
		// .showImageOnLoading(R.drawable.ic_stub) // resource or drawable
		// .showImageForEmptyUri(R.drawable.ic_empty) // resource or drawable
		// .showImageOnFail(R.drawable.ic_error) // resource or drawable
		// .resetViewBeforeLoading(true) // default
		// .delayBeforeLoading(1000)
				.cacheInMemory(true) // default
				.cacheOnDisc(true) // default
				// .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) //
				// default
				// .bitmapConfig(Bitmap.Config.ARGB_8888) // default
				// .decodingOptions(...)
				.displayer(new SimpleBitmapDisplayer()) // default
														// SimpleBitmapDisplayer,RoundedBitmapDisplayer(10),FadeInBitmapDisplayer
				// .handler(new Handler()) // default
				.build();

		File cacheDir = StorageUtils.getCacheDirectory(context);
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
				.memoryCacheExtraOptions(480, 800)
				// default = device screen dimensions
				.discCacheExtraOptions(480, 800, CompressFormat.PNG, 100, null)
				.threadPoolSize(10)
				// default
				.threadPriority(Thread.NORM_PRIORITY - 1)
				// default
				.denyCacheImageMultipleSizesInMemory()
				.discCache(new UnlimitedDiscCache(cacheDir))
				// default
				.discCacheFileNameGenerator(new Md5FileNameGenerator()).tasksProcessingOrder(QueueProcessingType.LIFO)
				.memoryCache(new LruMemoryCache(2 * 1024 * 1024)).memoryCacheSize(2 * 1024 * 1024)
				.memoryCacheSizePercentage(13)
				// default
				.discCacheSize(50 * 1024 * 1024).discCacheFileCount(100)
				.imageDownloader(new BaseImageDownloader(context)).defaultDisplayImageOptions(options) // default
				// .writeDebugLogs() // Remove for release app
				.build();

		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
    }
}
