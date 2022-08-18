package org.yameida.worktool.utils

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityService.GestureResultCallback
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.annotation.TargetApi
import android.app.Notification
import android.app.PendingIntent
import android.graphics.Path
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.blankj.utilcode.util.LogUtils
import org.yameida.worktool.service.getRoot
import java.lang.Exception
import java.lang.Thread.sleep
import androidx.annotation.RequiresApi

/**
 * 1.查询类
 * findOneByClazz 按类名寻找节点和子节点内的一个匹配项
 * findAllByClazz 按类名寻找节点和子节点内的所有匹配项
 * findFrontNode 查找节点的前兄弟节点
 * findBackNode 查找节点的后兄弟节点
 * findCanScrollNode 返回可滚动元素集合
 * findOneByDesc 按描述寻找节点和子节点内的一个匹配项
 * findOneByText 按文本(关键词)寻找节点和子节点内的一个匹配项
 * findAllByText 按文本(关键词)寻找节点和子节点内的所有匹配项
 *
 * 2.全局操作
 * globalGoBack 回退
 * globalGoHome 回桌面
 *
 * 3.窗口操作
 * printNodeClazzTree 深度搜索打印节点及其子节点
 * performScrollUp 对某个节点向上滚动 未生效
 * performScrollDown 对某个节点向下滚动 未生效
 * performClick 对某个节点进行点击
 * performXYClick 输入x, y坐标模拟点击事件
 * editTextInput 编辑EditView(非粘贴 推荐)
 * findTextAndClick 寻找第一个文本匹配(关键词)并点击
 * findTextInput 寻找第一个EditView编辑框并输入文本
 * findListOneAndClick 寻找第一个列表并点击指定条目(默认点击第一个条目)
 * scrollAndFindByText 滚动并按文本寻找第一个控件
 * performClickWithSon 对某个节点或子节点进行点击
 * performLongClick 对某个节点或父节点进行长按
 * performLongClickWithSon 对某个节点或子节点进行长按
 *
 */
object AccessibilityUtil {
    private const val tag = "AccessibilityUtil"
    private const val SHORT_INTERVAL = 100L
    private const val SCROLL_INTERVAL = 300L

    //编辑EditView(非粘贴 推荐)
    fun editTextInput(nodeInfo: AccessibilityNodeInfo?, text: String): Boolean {
        val nodeInfo: AccessibilityNodeInfo = nodeInfo ?: return false
        val arguments = Bundle()
        arguments.putCharSequence(
            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
            text
        )
        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        return true
    }

    //寻找第一个文本匹配(关键词)并点击
    fun findTextAndClick(nodeInfo: AccessibilityNodeInfo?, vararg textList: String): Boolean {
        val textView = findOneByText(nodeInfo, *textList) ?: return false
        return performClick(textView)
    }

    //寻找第一个EditView编辑框并输入文本
    fun findTextInput(nodeInfo: AccessibilityNodeInfo?, text: String, root: Boolean = true, append: Boolean = false): Boolean {
        val editText = if (root) {
            findOneByClazz(nodeInfo, "android.widget.EditText") ?: return false
        } else {
            findOnceByClazz(nodeInfo, "android.widget.EditText") ?: return false
        }
        editText.refresh()
        val oldText = if (editText.text != null) editText.text.toString() else ""
        LogUtils.v("findTextInput oldText: $oldText")
        val arguments = Bundle()
        arguments.putCharSequence(
            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
            if (append) (oldText + text) else text
        )
        editText.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        return true
    }

    //寻找第一个列表并点击指定条目(默认点击第一个条目)
    fun findListOneAndClick(nodeInfo: AccessibilityNodeInfo, index: Int = 0): Boolean {
        val rv = findOnceByClazz(nodeInfo, "androidx.recyclerview.widget.RecyclerView")
        val lv = findOnceByClazz(nodeInfo, "android.widget.ListView")
        if (rv == null && lv == null) return false
        if (rv != null && rv.childCount > index) {
            performClick(rv.getChild(index))
        } else if (lv != null && lv.childCount > index) {
            performClick(lv.getChild(index))
        }
        return true
    }

    //滚动并按文本寻找第一个控件
    fun scrollAndFindByText(
        nodeInfo: AccessibilityNodeInfo,
        vararg textList: String,
        maxRetry: Int = 3
    ): AccessibilityNodeInfo? {
        var index = 0
        while (index++ < maxRetry) {
            performScrollUp(nodeInfo, 0)
            val node = findOnceByText(nodeInfo, *textList)
            if (node != null) {
                return node
            }
        }
        while (index++ < maxRetry * 2) {
            performScrollDown(nodeInfo, 0)
            val node = findOnceByText(nodeInfo, *textList)
            if (node != null) {
                return node
            }
        }
        return null
    }

    //输入x, y坐标模拟点击事件
    @TargetApi(Build.VERSION_CODES.N)
    fun performXYClick(service: AccessibilityService, x: Float, y: Float): Boolean {
        val path = Path()
        path.moveTo(x, y)
        val builder = GestureDescription.Builder()
        builder.addStroke(StrokeDescription(path, 0, 1))
        val gesture = builder.build()
        return service.dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription) {
                LogUtils.v("click okk onCompleted")
            }

            override fun onCancelled(gestureDescription: GestureDescription) {
                LogUtils.v("click okk onCancelled")
            }
        }, null)
    }

    /**
     * 对某个节点或父节点进行点击
     */
    fun performClick(nodeInfo: AccessibilityNodeInfo?): Boolean {
        var nodeInfo: AccessibilityNodeInfo? = nodeInfo ?: return false
        while (nodeInfo != null) {
            if (nodeInfo.isClickable) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return true
            }
            nodeInfo = nodeInfo.parent
        }
        return false
    }

    /**
     * 对某个节点或子节点进行点击
     */
    fun performClickWithSon(nodeInfo: AccessibilityNodeInfo?): Boolean {
        var nodeInfo: AccessibilityNodeInfo? = nodeInfo ?: return false
        while (nodeInfo != null) {
            if (nodeInfo.isClickable) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return true
            }
            if (nodeInfo.childCount > 0) {
                for (i in 0 until nodeInfo.childCount) {
                    if (performClickWithSon(nodeInfo.getChild(i))) {
                        return true
                    }
                }
            } else {
                nodeInfo = null
            }
        }
        return false
    }

    /**
     * 对某个节点或父节点进行长按
     */
    fun performLongClick(nodeInfo: AccessibilityNodeInfo?): Boolean {
        var nodeInfo: AccessibilityNodeInfo? = nodeInfo ?: return false
        while (nodeInfo != null) {
            if (nodeInfo.isLongClickable) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
                return true
            }
            nodeInfo = nodeInfo.parent
        }
        return false
    }

    /**
     * 对某个节点或子节点进行长按
     */
    fun performLongClickWithSon(nodeInfo: AccessibilityNodeInfo?): Boolean {
        var nodeInfo: AccessibilityNodeInfo? = nodeInfo ?: return false
        while (nodeInfo != null) {
            if (nodeInfo.isLongClickable) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
                return true
            }
            if (nodeInfo.childCount > 0) {
                for (i in 0 until nodeInfo.childCount) {
                    if (performLongClickWithSon(nodeInfo.getChild(i))) {
                        return true
                    }
                }
            } else {
                nodeInfo = null
            }
        }
        return false
    }

    //对某个节点向上滚动
    fun performScrollUp(nodeInfo: AccessibilityNodeInfo?): Boolean {
        var nodeInfo: AccessibilityNodeInfo? = nodeInfo ?: return false
        while (nodeInfo != null) {
            if (nodeInfo.isScrollable) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
                return true
            }
            nodeInfo = nodeInfo.parent
        }
        return false
    }

    //对某个节点或父节点向下滚动
    fun performScrollDown(node: AccessibilityNodeInfo?): Boolean {
        var node: AccessibilityNodeInfo? = node ?: return false
        while (node != null) {
            if (node.isScrollable) {
                node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                return true
            }
            node = node.parent
        }
        return false
    }

    //对第几个节点向上滚动
    fun performScrollUp(node: AccessibilityNodeInfo?, index: Int): Boolean {
        if (node == null) return false
        val canScrollNodeList = findCanScrollNode(node)
        if (canScrollNodeList.size > index) {
            canScrollNodeList[index].performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
            sleep(SCROLL_INTERVAL)
            return true
        }
        return false
    }

    //对第几个节点向下滚动
    fun performScrollDown(node: AccessibilityNodeInfo?, index: Int): Boolean {
        if (node == null) return false
        val canScrollNodeList = findCanScrollNode(node)
        if (canScrollNodeList.size > index) {
            canScrollNodeList[index].performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
            sleep(SCROLL_INTERVAL)
            return true
        }
        return false
    }

    //返回可滚动元素集合
    fun findCanScrollNode(
        node: AccessibilityNodeInfo?,
        list: ArrayList<AccessibilityNodeInfo> = ArrayList()
    ): ArrayList<AccessibilityNodeInfo> {
        if (node == null) return list
        if (node.isScrollable) list.add(node)
        for (i in 0 until node.childCount) {
            findCanScrollNode(node.getChild(i), list)
        }
        return list
    }

    //通知栏事件进入应用
    fun gotoApp(event: AccessibilityEvent) {
        val data = event.parcelableData
        if (data != null && data is Notification) {
            val intent = data.contentIntent
            try {
                intent.send()
            } catch (e: PendingIntent.CanceledException) {
                e.printStackTrace()
            }
        }
    }

    //回退
    fun globalGoBack(service: AccessibilityService) {
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
    }

    //回首页
    fun globalGoHome(service: AccessibilityService) {
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
    }

    /**
     * 按描述寻找节点和子节点内的一个匹配项
     * @param node 节点
     * @param desc 描述
     * @param timeout 检查超时时间
     */
    fun findOneByDesc(
        node: AccessibilityNodeInfo?,
        desc: String,
        timeout: Long = 5000
    ): AccessibilityNodeInfo? {
        var node = node ?: return null
        val description = node.contentDescription?.toString()
        if (description == desc) {
            return node
        }
        val startTime = System.currentTimeMillis()
        var currentTime = startTime
        while (currentTime - startTime <= timeout) {
            val result = findOnceByDesc(node, desc)
            if (result != null) return result
            sleep(SHORT_INTERVAL)
            node = getRoot(true)
            currentTime = System.currentTimeMillis()
        }
        Log.e(tag, "findOneByDesc: not found: $desc")
        return null
    }

    fun findOnceByDesc(
        node: AccessibilityNodeInfo?,
        desc: String
    ): AccessibilityNodeInfo? {
        if (node == null) return null
        val description = node.contentDescription?.toString()
        if (description == desc) {
            return node
        }
        for (i in 0 until node.childCount) {
            val result = findOnceByDesc(node.getChild(i), desc)
            if (result != null) return result
        }
        return null
    }

    /**
     * 按文本(关键词)寻找节点和子节点内的一个匹配项
     * @param node 节点
     * @param textList 关键词
     * @param timeout 检查超时时间
     */
    fun findOneByText(
        node: AccessibilityNodeInfo?,
        vararg textList: String,
        exact: Boolean = false,
        timeout: Long = 5000,
        root: Boolean = true
    ): AccessibilityNodeInfo? {
        var node = node ?: return null
        val startTime = System.currentTimeMillis()
        var currentTime = startTime
        while (currentTime - startTime <= timeout) {
            val result = findOnceByText(node, *textList, exact = exact)
            LogUtils.v("text: ${textList.joinToString()} result == null: ${result == null}")
            if (result != null) return result
            sleep(SHORT_INTERVAL)
            if (root) {
                node = getRoot(true)
            } else {
                node.refresh()
            }
            currentTime = System.currentTimeMillis()
        }
        Log.e(tag, "findOneByText: not found: ${textList.joinToString()}")
        return null
    }

    fun findOnceByText(
        node: AccessibilityNodeInfo?,
        vararg textList: String,
        exact: Boolean = false
    ): AccessibilityNodeInfo? {
        if (node == null) return null
        val textNodeList = findAllOnceByText(node, *textList, exact = exact)
        LogUtils.v("text: ${textList.joinToString()} count: " + textNodeList.size)
        if (exact) return textNodeList[0]
        else if (textNodeList.size > 0) {
            for (textNode in textNodeList) {
                for (text in textList) {
                    if (textNode.text == text) {
                        return textNode
                    }
                }
            }
            return textNodeList[0]
        }
        return null
    }

    /**
     * 按文本(关键词)寻找节点和子节点内的所有匹配项
     * @param node 节点
     * @param textList 关键词
     * @param timeout 检查超时时间
     */
    fun findAllByText(
        node: AccessibilityNodeInfo?,
        vararg textList: String,
        exact: Boolean = false,
        timeout: Long = 5000,
        root: Boolean = true,
        minSize: Int = 1
    ): List<AccessibilityNodeInfo> {
        var node = node ?: return arrayListOf()
        val startTime = System.currentTimeMillis()
        var currentTime = startTime
        while (currentTime - startTime <= timeout) {
            val result = findAllOnceByText(node, *textList, exact = exact)
            LogUtils.v("text: ${textList.joinToString()} count: " + result.size)
            if (result.size >= minSize) return result
            sleep(SHORT_INTERVAL)
            if (root) {
                node = getRoot(true)
            } else {
                node.refresh()
            }
            currentTime = System.currentTimeMillis()
        }
        Log.e(tag, "findAllByText: not found: ${textList.joinToString()}")
        return arrayListOf()
    }

    /**
     * 按文本(关键词)寻找节点和子节点内的所有匹配项
     * node 节点
     * clazz 类名
     * limitDepth 深度 限制深度搜索深度必须匹配提供值且类名相同才返回 不填默认不限制
     */
    fun findAllOnceByText(
        node: AccessibilityNodeInfo?,
        vararg textList: String,
        exact: Boolean = false,
        list: ArrayList<AccessibilityNodeInfo> = ArrayList()
    ): ArrayList<AccessibilityNodeInfo> {
        if (node == null) return list
        val nodeText = node.text
        if (nodeText != null) {
            for (text in textList) {
                if (exact && nodeText == text) {
                    list.add(node)
                } else if (!exact && nodeText.contains(text)) {
                    list.add(node)
                }
            }
        }
        for (i in 0 until node.childCount) {
            findAllOnceByText(node.getChild(i), *textList, exact = exact, list = list)
        }
        return list
    }

    /**
     * 按类名寻找节点和子节点内的一个匹配项
     * node 节点
     * clazz 类名
     * limitDepth 深度 限制深度搜索深度必须匹配提供值且类名相同才返回 不填默认不限制
     * timeout 检查超时时间
     */
    fun findOneByClazz(
        node: AccessibilityNodeInfo?,
        vararg clazzList: String,
        limitDepth: Int? = null,
        depth: Int = 0,
        timeout: Long = 5000,
        root: Boolean = true
    ): AccessibilityNodeInfo? {
        var node = node ?: return null
        val startTime = System.currentTimeMillis()
        var currentTime = startTime
        while (currentTime - startTime <= timeout) {
            val result = findOnceByClazz(node, *clazzList, limitDepth = limitDepth, depth = depth)
            LogUtils.v("clazz: ${clazzList.joinToString()} result == null: ${result == null}")
            if (result != null) return result
            sleep(SHORT_INTERVAL)
            if (root) {
                node = getRoot(true)
            } else {
                node.refresh()
            }
            currentTime = System.currentTimeMillis()
        }
        LogUtils.e("findOneByClazz Exception()")
        Exception().printStackTrace()
        return null
    }

    /**
     * 按类名寻找节点和子节点内的一个匹配项
     * node 节点
     * clazz 类名
     * limitDepth 深度 限制深度搜索深度必须匹配提供值且类名相同才返回 不填默认不限制
     */
    fun findOnceByClazz(
        node: AccessibilityNodeInfo?,
        vararg clazzList: String,
        limitDepth: Int? = null,
        depth: Int = 0
    ): AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.className in clazzList) {
            if (limitDepth == null || limitDepth == depth)
                return node
        }
        for (i in 0 until node.childCount) {
            val result = findOnceByClazz(node.getChild(i), *clazzList, limitDepth = limitDepth, depth = depth + 1)
            if (result != null) return result
        }
        return null
    }

    /**
     * 按类名寻找节点和子节点内的所有匹配项
     * node 节点
     * clazz 类名
     * limitDepth 深度 限制深度搜索深度必须匹配提供值且类名相同才返回 不填默认不限制
     */
    fun findAllByClazz(
        node: AccessibilityNodeInfo?,
        vararg clazzList: String,
        timeout: Long = 5000,
        root: Boolean = true,
        minSize: Int = 1
    ): ArrayList<AccessibilityNodeInfo> {
        var node = node ?: return arrayListOf()
        val startTime = System.currentTimeMillis()
        var currentTime = startTime
        while (currentTime - startTime <= timeout) {
            val result = findAllOnceByClazz(node, *clazzList)
            LogUtils.v("clazz: ${clazzList.joinToString()} count: " + result.size)
            if (result.size >= minSize) return result
            sleep(SHORT_INTERVAL)
            if (root) {
                node = getRoot(true)
            } else {
                node.refresh()
            }
            currentTime = System.currentTimeMillis()
        }
        LogUtils.e("findAllByClazz Exception()")
        Exception().printStackTrace()
        return arrayListOf()
    }

    /**
     * 按类名寻找节点和子节点内的所有匹配项
     * node 节点
     * clazz 类名
     * limitDepth 深度 限制深度搜索深度必须匹配提供值且类名相同才返回 不填默认不限制
     */
    fun findAllOnceByClazz(
        node: AccessibilityNodeInfo?,
        vararg clazzList: String,
        list: ArrayList<AccessibilityNodeInfo> = ArrayList()
    ): ArrayList<AccessibilityNodeInfo> {
        if (node == null) return list
        if (node.className in clazzList) list.add(node)
        for (i in 0 until node.childCount) {
            findAllOnceByClazz(node.getChild(i), *clazzList, list = list)
        }
        return list
    }

    /**
     * 查找节点的前兄弟节点 直到该节点满足子节点数
     * node 节点
     */
    fun findFrontNode(node: AccessibilityNodeInfo?, minChildCount: Int = 0): AccessibilityNodeInfo? {
        var findFrontNode = findFrontNode(node) ?: return null
        while (findFrontNode.childCount < minChildCount) {
            findFrontNode = findFrontNode(findFrontNode) ?: return null
        }
        return findFrontNode
    }

    /**
     * 查找节点的前兄弟节点
     * node 节点
     */
    private fun findFrontNode(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        var parent: AccessibilityNodeInfo? = node.parent
        var son: AccessibilityNodeInfo? = node
        while (parent != null) {
            var index = -1
            for (i in 0 until parent.childCount) {
                if (parent.getChild(i) == son) {
                    index = i
                    break
                }
            }
            if (index > 0) {
                return parent.getChild(index - 1)
            }
            son = parent
            parent = parent.parent
        }
        return null
    }

    /**
     * 查找节点的后兄弟节点 直到该节点满足子节点数
     * node 节点
     */
    fun findBackNode(node: AccessibilityNodeInfo?, minChildCount: Int = 0): AccessibilityNodeInfo? {
        var findBackNode = findBackNode(node) ?: return null
        while (findBackNode.childCount < minChildCount) {
            findBackNode = findFrontNode(findBackNode) ?: return null
        }
        return findBackNode
    }

    /**
     * 查找节点的后兄弟节点
     * node 节点
     */
    private fun findBackNode(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        var parent: AccessibilityNodeInfo? = node.parent
        var son: AccessibilityNodeInfo? = node
        while (parent != null) {
            var index = -1
            for (i in 0 until parent.childCount) {
                if (parent.getChild(i) == son) {
                    index = i
                    break
                }
            }
            if (index < parent.childCount - 1) {
                return parent.getChild(index + 1)
            }
            son = parent
            parent = parent.parent
        }
        return null
    }

    /**
     * 深度搜索打印节点及其子节点
     * node 节点
     */
    fun printNodeClazzTree(
        node: AccessibilityNodeInfo?,
        printText: Boolean = true,
        depth: Int = 0
    ) {
        if (node == null) return
        var s = ""
        for (i in 0 until depth) {
            s += "---"
        }
        Log.d(tag, "$s depth: $depth className: " + node.className)
        if (printText && node.text != null) {
            Log.d(tag, "$s depth: $depth text: " + node.text)
        }
        if (printText && node.contentDescription != null) {
            Log.d(tag, "$s depth: $depth desc: " + node.contentDescription)
        }
        for (i in 0 until node.childCount) {
            printNodeClazzTree(node.getChild(i), printText, depth + 1)
        }
    }

    /**
     * Gesture手势实现点击(Android7+)
     * 解决 clickable=false 无法点击问题
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    fun clickByNode(
        service: AccessibilityService,
        nodeInfo: AccessibilityNodeInfo
    ): Boolean {
        val rect = Rect()
        nodeInfo.getBoundsInScreen(rect)
        val x: Int = (rect.left + rect.right) / 2
        val y: Int = (rect.top + rect.bottom) / 2
        val point = Point(x, y)
        val builder = GestureDescription.Builder()
        val path = Path()
        path.moveTo(point.x.toFloat(), point.y.toFloat())
        builder.addStroke(StrokeDescription(path, 0L, 10L))
        val gesture = builder.build()
        return service.dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription) {
                LogUtils.v("click okk onCompleted")
            }

            override fun onCancelled(gestureDescription: GestureDescription) {
                LogUtils.v("click okk onCancelled")
            }
        }, null)
    }
}