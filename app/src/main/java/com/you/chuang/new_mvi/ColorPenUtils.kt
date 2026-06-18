package com.you.chuang.new_mvi

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import kotlin.math.abs

object ColorPenUtils {

    private fun isFenceColor(color: Int, targetHsv: FloatArray, tolerance: Float): Boolean {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)

        val dh = angleDiff(hsv[0], targetHsv[0]) / 360f
        val ds = abs(hsv[1] - targetHsv[1])
        val dv = abs(hsv[2] - targetHsv[2])

        return (dh + ds + dv) < tolerance
    }

    private fun angleDiff(a: Float, b: Float): Float {
        val diff = abs(a - b)
        return if (diff > 180f) 360f - diff else diff
    }

    fun makeOutsideTransparentByFence(
        bitmap: Bitmap,
        fenceColor: Int = "#9D6B47".toColorInt(),
        tolerance: Float = 0.12f
    ): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val size = width * height

        val pixels = IntArray(size)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // 预计算目标颜色 HSV，避免在循环中反复转换
        val targetHsv = FloatArray(3)
        Color.colorToHSV(fenceColor, targetHsv)

        // 预计算每个像素是否为围栏色，避免后续重复调用 HSV 转换
        val isFence = BooleanArray(size) { isFenceColor(pixels[it], targetHsv, tolerance) }

        // 用 IntArray 模拟栈，避免 ArrayDeque<Int> 的装箱开销
        val stack = IntArray(size)
        var stackTop = -1

        val visited = BooleanArray(size)

        // 初始化边缘像素入栈（入栈前判断 visited，避免重复）
        fun tryPush(index: Int) {
            if (!visited[index]) {
                visited[index] = true
                stack[++stackTop] = index
            }
        }

        for (x in 0 until width) {
            tryPush(x)                          // top row
            tryPush((height - 1) * width + x)  // bottom row
        }
        for (y in 0 until height) {
            tryPush(y * width)                  // left col
            tryPush(y * width + (width - 1))    // right col
        }

        // Flood fill（非递归栈）
        while (stackTop >= 0) {
            val index = stack[stackTop--]

            // 围栏色：停止扩散（但 visited 已标记，不会重复处理）
            if (isFence[index]) continue

            val x = index % width
            val y = index / width

            if (x > 0)          tryPush(index - 1)
            if (x < width - 1)  tryPush(index + 1)
            if (y > 0)          tryPush(index - width)
            if (y < height - 1) tryPush(index + width)
        }

        // 外部非围栏像素变透明
        for (i in 0 until size) {
            if (visited[i] && !isFence[i]) {
                pixels[i] = pixels[i] and 0x00FFFFFF.toInt()
            }
        }

        val result = createBitmap(width, height)
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }
}