package com.reactnativevideohelper.video;

import android.os.AsyncTask;
import android.util.Log;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class VideoCompress {
    private static final String TAG = VideoCompress.class.getSimpleName();
    private static ArrayList<VideoCompressTask> tasks = new ArrayList<VideoCompressTask>();

    public static VideoCompressTask compressVideo(String srcPath, String destPath, String quality, long startTime, long endTime, CompressListener listener, int defaultOrientation) {
        int finalQuality = MediaController.COMPRESS_QUALITY_LOW;

        if (quality.equals("high")) {
            finalQuality = MediaController.COMPRESS_QUALITY_HIGH;
        } else if (quality.equals("medium")) {
            finalQuality = MediaController.COMPRESS_QUALITY_MEDIUM;
        }

        VideoCompressTask task = new VideoCompressTask(listener, finalQuality, startTime, endTime, defaultOrientation);
        tasks.add(task);
        task.execute(srcPath, destPath);
        return task;
    }

    public static String cancelAll() {
        int cancelled = 0;
        for (VideoCompressTask task : tasks) {
            task.cancel(true);
            cancelled += 1;
        }
        String returnValue = cancelled + "/" + tasks.size();

        tasks.clear();

        return returnValue;
    }

    private static class VideoCompressTask extends AsyncTask<String, Float, Boolean> {
        private CompressListener mListener;
        private int mQuality;
        private long mStartTime;
        private long mEndTime;
        private int defaultOrientation;

        public VideoCompressTask(CompressListener listener, int quality, long startTime, long endTime, int defaultOrientation) {
            mListener = listener;
            mQuality = quality;
            mStartTime = startTime;
            mEndTime = endTime;
            this.defaultOrientation = defaultOrientation;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mListener != null) {
                mListener.onStart();
            }
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            super.onCancelled(aBoolean);
            MediaController.getInstance().cancel();
        }

        @Override
        protected Boolean doInBackground(String... paths) {
            MediaController media = MediaController.getInstance();
            media.SetDefaultOrientation(defaultOrientation);

            try {
                return media.convertVideo(paths[0], paths[1], mQuality, mStartTime, mEndTime, new MediaController.CompressProgressListener() {
                    @Override
                    public void onProgress(float percent) {
                        publishProgress(percent);
                    }
                });
            } catch (FileNotFoundException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Float... percent) {
            super.onProgressUpdate(percent);
            if (mListener != null) {
                mListener.onProgress(percent[0]);
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (mListener != null) {
                if (result) {
                    mListener.onSuccess();
                } else {
                    mListener.onFail();
                }
            }
        }
    }

    public interface CompressListener {
        void onStart();
        void onSuccess();
        void onFail();
        void onProgress(float percent);
    }
}
