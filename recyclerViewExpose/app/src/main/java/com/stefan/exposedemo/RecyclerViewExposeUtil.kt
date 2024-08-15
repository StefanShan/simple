package com.stefan.exposedemo

import android.graphics.Rect
import android.os.SystemClock
import android.util.Log
import android.view.View
import androidx.core.view.get
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager


fun String.toLog() = Log.d("stefan", this)

class RecyclerViewExposeUtil(private val recyclerView: RecyclerView) {
    data class ExposeData(val view: View, val index: Int)


    private val set = mutableSetOf<ExposeData>()
    private var exposeListener: ((List<ExposeData>) -> Unit)? = null
    private val cacheViewWithExposeTimeMap = mutableMapOf<View, Long>()
    private val cachePositionWithViewMap = mutableMapOf<Int, View>()
    //有效时间
    private val validTime = 1500L
    //有效区域占比
    private val validAreaProportion = 1 / 2f
    init {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        set.addAll(cacheViewWithExposeTimeMap.keys.map {
                            ExposeData(it, recyclerView.getChildAdapterPosition(it))
                        })
                        cacheViewWithExposeTimeMap.clear()
                        cachePositionWithViewMap.clear()
                        notifyExposeListener()
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                recyclerView.layoutManager?.let {
                    when (it::class) {
                        LinearLayoutManager::class -> {
                            it as LinearLayoutManager
                            val firstPosition = it.findFirstVisibleItemPosition()
                            val lastPosition = it.findLastVisibleItemPosition()
                            if (dy > 0 || dx > 0) {
                                "firstPosition -> $firstPosition, lastPosition -> $lastPosition".toLog()
                                markLinearLayoutExpose(recyclerView, lastPosition, firstPosition, it.orientation == RecyclerView.VERTICAL)
                            } else {
                                markLinearLayoutExpose(recyclerView, firstPosition, lastPosition, it.orientation == RecyclerView.VERTICAL)
                            }
                        }

                        GridLayoutManager::class -> {
                            it as GridLayoutManager
                            val firstPosition = it.findFirstVisibleItemPosition()
                            val lastPosition = it.findLastVisibleItemPosition()
                            if (dy > 0 || dx > 0) {
                                markGridLayoutExpose(
                                    recyclerView,
                                    it.spanCount,
                                    lastPosition,
                                    firstPosition,
                                    it.orientation == RecyclerView.VERTICAL,
                                    false
                                )
                            } else {
                                markGridLayoutExpose(
                                    recyclerView,
                                    it.spanCount,
                                    firstPosition,
                                    lastPosition,
                                    it.orientation == RecyclerView.VERTICAL,
                                    true
                                )
                            }
                        }

                        StaggeredGridLayoutManager::class -> {
                            it as StaggeredGridLayoutManager
                            val firstPosition = it.findFirstVisibleItemPositions(null)
                            val lastPosition = it.findLastVisibleItemPositions(null)
                            if (dy > 0 || dx > 0) {
                                markStaggerGridLayoutExpose(
                                    recyclerView,
                                    lastPosition,
                                    firstPosition,
                                    it.orientation == RecyclerView.VERTICAL
                                )
                            } else {
                                markStaggerGridLayoutExpose(
                                    recyclerView,
                                    firstPosition,
                                    lastPosition,
                                    it.orientation == RecyclerView.VERTICAL
                                )
                            }
                        }

                        else -> return
                    }
                }
            }
        })


        //首屏数据，直接上报
        recyclerView.viewTreeObserver.apply {
            addOnWindowFocusChangeListener {
                if (!it) return@addOnWindowFocusChangeListener
                recyclerView.layoutManager?.let { layoutManager ->
                    val isVertical = layoutManager.canScrollVertically()
                    for (i in 0 until recyclerView.childCount) {
                        val view = recyclerView[i]
                        if (checkExposeView(view, isVertical)) {
                            set.add(ExposeData(view, i))
                        }
                    }
                    notifyExposeListener()
                }
            }
        }
    }

    private fun markStaggerGridLayoutExpose(recyclerView: RecyclerView, intoPosition: IntArray, outPosition: IntArray, vertical: Boolean) {
        for (position in intoPosition) {
            if (!cachePositionWithViewMap.containsKey(position)) {
                cachePositionWithViewMap[position] = recyclerView.findViewHolderForAdapterPosition(position)?.itemView ?: return
            }
            val intoView = cachePositionWithViewMap[position] ?: return
            if (!cacheViewWithExposeTimeMap.contains(intoView) && checkAttach(intoView, vertical)) {
                cacheViewWithExposeTimeMap[intoView] = SystemClock.elapsedRealtime()
            }
        }
        for (position in outPosition) {
            val outView = cachePositionWithViewMap[position] ?: return
            checkAndMark(outView, position, vertical)
        }
    }

    private fun markGridLayoutExpose(
        recyclerView: RecyclerView,
        spanCount: Int,
        intoPosition: Int,
        outPosition: Int,
        vertical: Boolean,
        headScroll: Boolean
    ) {
        if (!cachePositionWithViewMap.containsKey(intoPosition)) {
            for (i in 0 until spanCount) {
                val index = if (headScroll) -i else i
                cachePositionWithViewMap[intoPosition - index] =
                    recyclerView.findViewHolderForAdapterPosition(intoPosition - index)?.itemView ?: return
            }
        }
        for (i in 0 until spanCount) {
            val index = if (headScroll) -i else i
            val intoView = cachePositionWithViewMap[intoPosition - index] ?: return
            if (!cacheViewWithExposeTimeMap.contains(intoView) && checkAttach(intoView, vertical)) {
                cacheViewWithExposeTimeMap[intoView] = SystemClock.elapsedRealtime()
            }
        }
        for (i in 0 until spanCount) {
            val index = if (headScroll) -i else i
            val outView = cachePositionWithViewMap[outPosition + index] ?: return
            checkAndMark(outView, outPosition + index, vertical)
        }
    }

    private fun markLinearLayoutExpose(recyclerView: RecyclerView, intoPosition: Int, outPosition: Int, vertical: Boolean) {
        if (!cachePositionWithViewMap.containsKey(intoPosition)) {
            cachePositionWithViewMap[intoPosition] = recyclerView.findViewHolderForAdapterPosition(intoPosition)?.itemView ?: return
        }
        val intoView = cachePositionWithViewMap[intoPosition] ?: return
        if (!cacheViewWithExposeTimeMap.contains(intoView) && checkAttach(intoView, vertical)) {
            cacheViewWithExposeTimeMap[intoView] = SystemClock.elapsedRealtime()
        }
        val outView = cachePositionWithViewMap[outPosition] ?: return
        checkAndMark(outView, outPosition, vertical)
    }

    private fun checkAndMark(view: View, position: Int, vertical: Boolean) {
        if (cacheViewWithExposeTimeMap.contains(view) && checkDetach(view, vertical)) {
            val attachTime = cacheViewWithExposeTimeMap[view] ?: SystemClock.elapsedRealtime()
            if (SystemClock.elapsedRealtime() - attachTime > validTime) {
                set.add(ExposeData(view, position))
            }
            cacheViewWithExposeTimeMap.remove(view)
        }
    }

    private fun checkAttach(view: View, vertical: Boolean): Boolean {
        return checkExposeView(view, vertical)
    }

    private fun checkDetach(view: View, vertical: Boolean): Boolean {
        val rect = Rect()
        val visibility = view.getGlobalVisibleRect(rect)
        return !visibility || (vertical && rect.height() <= view.height * validAreaProportion) || (!vertical && rect.width() <= view.width * validAreaProportion)
    }

    private fun checkExposeView(view: View, vertical: Boolean): Boolean {
        val rect = Rect()
        val visibility = view.getGlobalVisibleRect(rect)
        return visibility && (vertical && rect.height() >= view.height * validAreaProportion) || (!vertical && rect.width() >= view.width * validAreaProportion)
    }

    fun setExposeListener(listener: (List<ExposeData>) -> Unit) {
        exposeListener = listener
    }

    private fun notifyExposeListener() {
        exposeListener?.apply {
            invoke(set.sortedBy { it.index }.filter { it.index >= 0 }.toList())
            set.clear()
        }
    }
}