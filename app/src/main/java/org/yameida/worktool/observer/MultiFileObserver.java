package org.yameida.worktool.observer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Stack;

import android.os.FileObserver;
import android.util.Log;

public class MultiFileObserver extends FileObserver {

    public HashMap<String, Long> map = new HashMap<>();
    public static HashSet<String> createSet = new LinkedHashSet<>();
    public static HashSet<String> finishSet = new LinkedHashSet<>();
    public static HashSet<String> saveSet = new LinkedHashSet<>();

    /** Only modification events */
    public static int CHANGES_ONLY = CREATE | MODIFY | DELETE | CLOSE_WRITE
            | DELETE_SELF | MOVE_SELF | MOVED_FROM | MOVED_TO;

    private List<SingleFileObserver> mObservers;
    private String mPath;
    private int mMask;

    public MultiFileObserver(String path) {
        this(path, ALL_EVENTS);
    }

    public MultiFileObserver(String path, int mask) {
        super(path, mask);
        mPath = path;
        mMask = mask;
    }

    @Override
    public void startWatching() {
        if (mObservers != null)
            return;

        mObservers = new ArrayList<SingleFileObserver>();
        Stack<String> stack = new Stack<String>();
        stack.push(mPath);

        while (!stack.isEmpty()) {
            String parent = stack.pop();
            mObservers.add(new SingleFileObserver(parent, mMask));
            File path = new File(parent);
            File[] files = path.listFiles();
            if (null == files)
                continue;
            for (File f : files) {
                if (f.isDirectory() && !f.getName().equals(".")
                        && !f.getName().equals("..")) {
                    stack.push(f.getPath());
                }
            }
        }

        for (int i = 0; i < mObservers.size(); i++) {
            SingleFileObserver sfo =  mObservers.get(i);
            sfo.startWatching();
        }
    }

    @Override
    public void stopWatching() {
        if (mObservers == null)
            return;

        for (int i = 0; i < mObservers.size(); i++) {
            SingleFileObserver sfo = mObservers.get(i);
            sfo.stopWatching();
        }

        mObservers.clear();
        mObservers = null;
    }

    @Override
    public void onEvent(int event, String path) {
        switch (event) {
            case FileObserver.ACCESS:
            case FileObserver.CLOSE_WRITE:
            case FileObserver.CLOSE_NOWRITE:
                if (path.endsWith(".0") && !map.containsKey(path) && createSet.contains(path)) {
                    map.put(path, System.currentTimeMillis());
                    Log.i("RecursiveFileObserver", "发现新图片: " + path);
                    finishSet.add(path);
//                    try {
//                        ImageInfo imageInfo = new ImageInfo();
//                        imageInfo.setDetermineImageNumber(true);
//                        ImageInfo.run(path, new FileInputStream(path), imageInfo, true);
//                    } catch (Exception e) { e.printStackTrace(); }
                }
                break;
            case FileObserver.CREATE:
                Log.i("RecursiveFileObserver", "CREATE: " + path);
                if (path.endsWith(".0")) {
                    createSet.add(path);
                }
                break;
            case FileObserver.ATTRIB:
//                Log.i("RecursiveFileObserver", "ATTRIB: " + path);
                break;
//            case FileObserver.CLOSE_WRITE:
//                Log.i("RecursiveFileObserver", "CLOSE_WRITE: " + path);
//                break;
            case FileObserver.DELETE:
//                Log.i("RecursiveFileObserver", "DELETE: " + path);
                break;
            case FileObserver.DELETE_SELF:
//                Log.i("RecursiveFileObserver", "DELETE_SELF: " + path);
                break;
            case FileObserver.MODIFY:
//                Log.i("RecursiveFileObserver", "MODIFY: " + path);
                break;
            case FileObserver.MOVE_SELF:
//                Log.i("RecursiveFileObserver", "MOVE_SELF: " + path);
                break;
            case FileObserver.MOVED_FROM:
//                Log.i("RecursiveFileObserver", "MOVED_FROM: " + path);
                break;
            case FileObserver.MOVED_TO:
//                Log.i("RecursiveFileObserver", "MOVED_TO: " + path);
                break;
            case FileObserver.OPEN:
//                Log.i("RecursiveFileObserver", "OPEN: " + path);
                break;
            default:
//                Log.i("RecursiveFileObserver", "DEFAULT(" + event + " : " + path);
                break;
        }
    }

    /**
     * Monitor single directory and dispatch all events to its parent, with full
     * path.
     */
    class SingleFileObserver extends FileObserver {
        String mPath;

        public SingleFileObserver(String path) {
            this(path, ALL_EVENTS);
            mPath = path;
        }

        public SingleFileObserver(String path, int mask) {
            super(path, mask);
            mPath = path;
        }

        @Override
        public void onEvent(int event, String path) {
            String newPath = mPath + "/" + path;
            MultiFileObserver .this.onEvent(event, newPath);
        }
    }
}