package test.zjb.com.weixinhongbaotest.service;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by khb on 2016/12/6.
 */
public class HongBaoService extends AccessibilityService {

    private ArrayList<AccessibilityNodeInfo> parents;

    private final static String WeiXinInterface = "com.tencent.mm.ui.LauncherUI";           //微信界面
    private final static String WeiXinOpenInterface = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI";    //打开红包界面
    private final static String WeiXinDetailInterface = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI";   //红包详情界面，可返回

    private final static String OpenButtonId = "com.tencent.mm:id/bg7";
    private final static String BackButtonId = "com.tencent.mm:id/gd";

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.i("hongbao","service started");
        parents = new ArrayList<>();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        switch (eventType){
            //监控通知栏
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                List<CharSequence> texts =  event.getText();
                if (!texts.isEmpty()){
                    for (CharSequence text : texts){
                        String content = text.toString();
                        //如果通知是微信红包通知就执行里面的intent
                        if (content.contains("[微信红包]")){
                            if (event.getParcelableData() != null
                                    && event.getParcelableData() instanceof Notification){
                                Notification notification = (Notification) event.getParcelableData();
                                PendingIntent pi = notification.contentIntent;
                                try {
                                    pi.send();
                                    Log.i("hongbao", "进入微信");
                                } catch (PendingIntent.CanceledException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                break;
            //监控窗口状态
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                String className = event.getClassName().toString();
                Log.i("hongbao", "class : "+className);
                if (className.equals(WeiXinInterface)){
                    Log.i("hongbao", "点击最新红包");
                    getLastPacket();
                }else if (className.equals(WeiXinOpenInterface)){
                    Log.i("hongbao", "打开红包");
                    click(OpenButtonId);
                }
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void click(String id) {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null){
            List<AccessibilityNodeInfo> nodeList = nodeInfo.findAccessibilityNodeInfosByViewId(id);
            for (AccessibilityNodeInfo node:
                 nodeList) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void getLastPacket() {
        AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
        recycle(rootInActiveWindow);
        if (parents.size()>0){
            parents.get(parents.size() -1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    private void recycle(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo.getChildCount() == 0){
            if (nodeInfo.getText() != null){
                if ("领取红包".equals(nodeInfo.getText().toString())){
                    if (nodeInfo.isClickable()){
                        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                    AccessibilityNodeInfo parent = nodeInfo.getParent();
                    while (parent != null){
                        if (parent.isClickable()) {
                            parents.add(parent);
                            break;
                        }
                        parent = parent.getParent();
                    }
                }
            }
        } else {
            for (int i = 0; i<nodeInfo.getChildCount(); i++){
                if (nodeInfo.getChild(i) != null) {
                    recycle(nodeInfo.getChild(i));
                }
            }
        }
    }

    @Override
    public void onInterrupt() {

    }
}
