package dimcho.proj.sftpfilemanager.filemanager;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import dimcho.proj.sftpfilemanager.recycler.FileInfo;
import dimcho.proj.sftpfilemanager.recycler.FileInfoKt;
import dimcho.proj.sftpfilemanager.recycler.AlphaNumericComparator;


/**
 * Created by dimcho on 12.03.18.
 */

public class RemoteFileHandlerThread extends HandlerThread {

    public static final int RESULT_LISTED_DIRECTORY = 5;
    public static final int RESULT_CHANNEL_CONNECTED = 2;

    private Handler mainHandler;
    private Handler messageHandler;
    private Session session;
    private final UserInfo userInfo;
    private ChannelSftp sftpChannel;

    private List<FileInfo> listDirectory(final String dirPath) throws Exception {
        final List<FileInfo> converted = new ArrayList<>(100);
        final ChannelSftp.LsEntrySelector selector = new ChannelSftp.LsEntrySelector() {
            @Override
            public int select(ChannelSftp.LsEntry entry) {
                final String fileName = entry.getFilename();
                if(!fileName.matches("\\.+")) {
                    converted.add(FileInfoKt.getFileInfoFromSftp(entry, dirPath));
                }

                return CONTINUE;
            }
        };

        sftpChannel.ls(dirPath, selector);

        // TODO use comparator from preferences preferences
        Collections.sort(converted, new AlphaNumericComparator());
        return converted;
    }

    private void sendFilesToMainThread(List<FileInfo> files) {
        mainHandler.sendMessage(mainHandler.obtainMessage(RESULT_LISTED_DIRECTORY, files));
    }

    private void quitHandlerThread() {
        sftpChannel.disconnect();
        session.disconnect();

        Log.wtf("Test", "DISCONNECTED");
        Log.wtf("Test", "DEAD");

        quit();
    }

    public LinkedList<String> finishingTaskIdQueue = new LinkedList<>();
    public enum TaskStatus {
        PENDING, RUNNING, FINISHED
    }

//    public String currentlyRunningTaskId = null;
//    public final ConcurrentHashMap<String, TaskStatus> taskStatusMap = new ConcurrentHashMap<>();
    public RemoteFileHandlerThread(Handler mainHandler, UserInfo userInfo) {
        super("FileHandler");

        this.mainHandler = mainHandler;
        this.userInfo = userInfo;
    }

    public Handler getHandler() {
        return messageHandler;
    }

//    public void updateMainHandlerRef(final Handler handler) {
//        this.mainHandler = handler;
//    }
//    public ChannelSftp getSftpChannel() { return sftpChannel; }
//    public Session getSession() { return session; }

    @Override
    protected void onLooperPrepared() {
        sftpChannel = null;
        try {
            messageHandler = new MessageHandler(getLooper());
            session = new JSch().getSession("none", "192.168.1.240");
            session.setUserInfo(userInfo);
            session.connect();
            Log.wtf("Test", "CONNECTED");

            Channel channel = session.openChannel("sftp");
            channel.connect();

            sftpChannel = (ChannelSftp) channel;

            mainHandler.sendEmptyMessage(RESULT_CHANNEL_CONNECTED);

        } catch(Exception exc) {
            exc.printStackTrace();

            if(sftpChannel != null) {
                sftpChannel.disconnect();
            }

            if(session != null) {
                session.disconnect();
                Log.wtf("Test", "DISCONNECTED Exception");
            }

            Log.wtf("Test", "DEAD Exception");
            quit();
        }
    }

    private class MessageHandler extends Handler {
        MessageHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RemoteFileManagerKt.ACTION_LIST_DIRECTORY:
                    try {
                        String taskID = (String) msg.obj;
                        String dirPath = taskID.split(":")[1];
                        finishingTaskIdQueue.add(taskID);
//                        String taskId = RemoteFileManager.TaskType.LIST.constructTaskId(dirPath);
//                        currentlyRunningTaskId = taskId;
//                        taskStatusMap.put(taskId, TaskStatus.RUNNING);

                        List<FileInfo> files = listDirectory(dirPath);
                        // Quit action will be executed after the ACTION_LIST_DIRECTORY
                        // because of the handler queue
                        Log.wtf("Test", "Sending complete");
                        sendFilesToMainThread(files);
                    } catch (Exception e) {
                        e.printStackTrace();
//                        taskStatusMap.clear();
                        Log.wtf("Test", "DEAD Exception");
                        quitHandlerThread();

                    }

                    break;

                case RemoteFileManagerKt.ACTION_QUIT_HANDLER_THREAD:
                    quitHandlerThread();

                    Log.wtf("Test", "Thread quit");
                    break;
                default: super.handleMessage(msg);
            }
        }
    }
}
